/*
 *
 *  *
 *  **    Copyright 2015, The LimeIME Open Source Project
 *  **
 *  **    Project Url: http://github.com/lime-ime/limeime/
 *  **                 http://android.toload.net/
 *  **
 *  **    This program is free software: you can redistribute it and/or modify
 *  **    it under the terms of the GNU General Public License as published by
 *  **    the Free Software Foundation, either version 3 of the License, or
 *  **    (at your option) any later version.
 *  *
 *  **    This program is distributed in the hope that it will be useful,
 *  **    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  **    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  **    GNU General Public License for more details.
 *  *
 *  **    You should have received a copy of the GNU General Public License
 *  **    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package net.toload.main.hd;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import net.toload.main.hd.data.ImObj;
import net.toload.main.hd.data.KeyboardObj;
import net.toload.main.hd.data.Mapping;
import net.toload.main.hd.global.LIME;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.global.LIMEUtilities;
import net.toload.main.hd.limedb.LimeDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class SearchServer {

    private static boolean DEBUG = false;
    private static final String TAG = "LIME.SearchServer";
    private static LimeDB dbadapter = null;

    //Jeremy '12,5,1 shared single LIMEDB object
    //Jeremy '12,4,6 Combine updatedb and quierydb into db,
    //Jeremy '12,4,7 move db open/close back to LimeDB

    //Jeremy '12,6,9 make run-time suggestion phrase
    private static final boolean doRunTimeSuggestion = true;

    private static List<Mapping> scorelist = null;

    //Jeremy '15,6,2 preserve the exact match mapping with the code user typed.
    private static List<List<Pair<Mapping, String>>> suggestionLoL;
    private static Stack<Pair<Mapping, String>> bestSuggestionStack;
    private static String lastCode; // preserved the last code queried from LIMEService

    private static String confirmedBestSuggestion = null;
    private static String lastConfirmedBestSuggestion = null;

    //Jeremy '15,6,21
    private static int maxCodeLength = 4;

    private static boolean mResetCache;

    private static List<List<Mapping>> LDPhraseListArray = null;
    private static List<Mapping> LDPhraseList = null;

    private static String tablename = "";

    private LIMEPreferenceManager mLIMEPref;

    private static boolean isPhysicalKeyboardPressed; // Sync to LIMEService and LIMEDB
    //Jeremy '11,6,10
    private static boolean hasNumberMapping;
    private static boolean hasSymbolMapping;

    //Jeremy '11,6,6
    private HashMap<String, String> selKeyMap = new HashMap<>();

    private static ConcurrentHashMap<String, List<Mapping>> cache = null;
    private static ConcurrentHashMap<String, List<Mapping>> engcache = null;
    private static ConcurrentHashMap<String, List<Mapping>> emojicache = null;
    private static ConcurrentHashMap<String, String> keynamecache = null;
    /**
     * Store the mapping of typing code and mapped code from getMappingByCode on db  Jeremy '12,6,5
     */
    private static ConcurrentHashMap<String, List<String>> coderemapcache = null;

    private Context mContext = null;

    // deprecated and using exact match stack to get real code length now. Jerey '15,6,2
    //private static List<Pair<Integer, Integer>> codeLengthMap = new LinkedList<>();

    public SearchServer(Context context) {


        this.mContext = context;

        mLIMEPref = new LIMEPreferenceManager(mContext.getApplicationContext());
        if (dbadapter == null) dbadapter = new LimeDB(mContext);
        initialCache();


    }

    public static void resetCache(boolean resetCache) {
        mResetCache = resetCache;
    }

    public String hanConvert(String input) {
        return dbadapter.hanConvert(input, mLIMEPref.getHanCovertOption());
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String table, boolean numberMapping, boolean symbolMapping) {
        if (DEBUG)
            Log.i(TAG, "SearchService.setTablename()");

        dbadapter.setTablename(table);
        tablename = table;
        hasNumberMapping = numberMapping;
        hasSymbolMapping = symbolMapping;

        //run prefetch on first keys thread to feed the data into cache first for better response on large table.  Jeremy '15, 6,7
        if (cache.get(cacheKey("a")) == null) {  // no cache records present. do prefetch now.  '15,6,7
            prefetchCache(numberMapping, symbolMapping);
        }

        //Jeremy '15,6,21 set max code length
        if (tablename.startsWith("cj")) {
            maxCodeLength = 5;
        }
    }

    private static Thread prefetchThread;

    private void prefetchCache(boolean numberMapping, boolean symbolMapping) {
        if(DEBUG)
            Log.i(TAG, "prefetchCache() on table :" + tablename);

        String keys = "abcdefghijklmnoprstuvwxyz";
        if (numberMapping)
            keys += "01234567890";
        if (symbolMapping)
            keys += ",./;";
        final String finalKeys = keys;

        if (prefetchThread != null && prefetchThread.isAlive()) return;

        prefetchThread = new Thread() {
            public void run() {
                long startime = System.currentTimeMillis();
                for (int i = 0; i < finalKeys.length(); i++) {
                    String key = finalKeys.substring(i, i + 1);
                    try {
                        //bypass run-time suggestion for prefetch queries
                        getMappingByCode(key, true, false, true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "prefetchCache() on table :" + tablename + " finished.  Elapsed time = "
                        + (System.currentTimeMillis() - startime) + " ms.");
            }
        };
        prefetchThread.start();

    }


    //TODO: Should cache related phrase 15,6,8 Jeremy
    public List<Mapping> getRelatedPhrase(String word, boolean getAllRecords) throws RemoteException {

        return dbadapter.getRelatedPhrase(word, getAllRecords);
    }

    //Add by jeremy '10, 4,1
    public void getCodeListStringFromWord(final String word) throws RemoteException {

        String result = dbadapter.getCodeListStringByWord(word);
        if (result != null && !result.equals("")) {
            LIMEUtilities.showNotification(
                    mContext, true, mContext.getText(R.string.ime_setting), result, new Intent(mContext, MainActivity.class));

            if(mLIMEPref.getReverseLookupNotify()){
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private String cacheKey(String code) {
        String key;

        //Jeremy '11,6,17 Seperate physical keyboard cache with keybaordtype
        if (isPhysicalKeyboardPressed) {
            if (tablename.equals("phonetic")) {
                key = mLIMEPref.getPhysicalKeyboardType() + dbadapter.getTablename()
                        + mLIMEPref.getPhoneticKeyboardType() + code;
            } else {
                key = mLIMEPref.getPhysicalKeyboardType() + dbadapter.getTablename() + code;
            }
        } else {
            if (tablename.equals("phonetic"))
                key = dbadapter.getTablename() + mLIMEPref.getPhoneticKeyboardType() + code;
            else
                key = dbadapter.getTablename() + code;
        }
        return key;
    }


    private void clearRunTimeSuggestion(boolean abandonSuggestion)
    {
        for (List<Pair<Mapping, String>> suggestList : suggestionLoL) {
            suggestList.clear();
        }
        suggestionLoL.clear();
        if (bestSuggestionStack != null) bestSuggestionStack.clear();
        confirmedBestSuggestion = null;
        lastConfirmedBestSuggestion = null;
        abandonPhraseSuggestion =abandonSuggestion;
    }

    private static boolean dumpRunTimeSuggestion = false;

    private synchronized void makeRunTimeSuggestion(String code, List<Mapping> completeCodeResultList) {

        long startTime=0;
        if (DEBUG || dumpRunTimeSuggestion) {
            Log.i(TAG, "makeRunTimeSuggestion() code = " + code);
            startTime = System.currentTimeMillis();
        }
        //check if the composing is start over or user pressed backspace
        if (suggestionLoL != null && !suggestionLoL.isEmpty()) {
            // code is start over, clear the stack.  The composition is start over.   Jeremy'15,6,4.
            if (code.length() == 1) {
                clearRunTimeSuggestion(false);

            } else if (code.length() == lastCode.length() - 1) {  //user press backspace.
                for (List<Pair<Mapping, String>> suggestList : suggestionLoL) {
                    //check the last element in each list
                    if (!suggestList.isEmpty() && suggestList.get(suggestList.size() - 1).second.equals(lastCode)) {
                        suggestList.remove(suggestList.size() - 1);
                    }
                }
                //remove best suggestion stack last element if last element is with lastCode
                if (bestSuggestionStack != null && !bestSuggestionStack.isEmpty() && bestSuggestionStack.lastElement().second.equals(lastCode)) {
                    bestSuggestionStack.pop();
                }
            }

        }
        lastCode = code;


        if (DEBUG || dumpRunTimeSuggestion)
            Log.i(TAG, "makeRunTimeSuggestion(): Finish checking for the composing is start over or user pressed backspace. Time elapsed  = " + (System.currentTimeMillis() - startTime) );


            //15,6,8  Jeremy. Check exact match records first.
        if (completeCodeResultList != null && !completeCodeResultList.isEmpty() && completeCodeResultList.get(0).isExactMatchToCodeRecord()) {
            Mapping exactMatchMapping;
            int k = 0, highestScore = 0, initialSize = suggestionLoL.size(), highestScoreIndex = initialSize;
            List<List<Pair<Mapping, String>>> suggestLoLSnapshot = null;
            do {
                exactMatchMapping = completeCodeResultList.get(k);
                int score = exactMatchMapping.getBasescore();
                if (score < 120) {
                    score = 120;
                } else if (score > 200) {
                    score = 200;
                }
                int codeLenBonus = exactMatchMapping.getCode().length() / exactMatchMapping.getWord().length() * 30;
                int newScore = score + codeLenBonus;

                exactMatchMapping.setBasescore(newScore * exactMatchMapping.getWord().length());

                if (DEBUG || dumpRunTimeSuggestion)
                    Log.i(TAG, "makeRunTimeSuggestion() complete code = " + code + "" +
                            ", got exact match  = " + exactMatchMapping.getWord()
                            + " score =" + exactMatchMapping.getScore() + ", bases core=" + exactMatchMapping.getBasescore()
                            +", time elapsed  =" +(System.currentTimeMillis() - startTime));


                //push the exact match mapping with current code into exact match stack. '15,6,2 Jeremy
                if (exactMatchMapping.getBasescore() > 0) {
                    if (k == 0 && exactMatchMapping.getWord().length() > 1) { //clear all previous traces if exact match phrase found
                        suggestLoLSnapshot = new LinkedList<>();
                        for (List<Pair<Mapping, String>> lpm : suggestionLoL) {
                            suggestLoLSnapshot.add(new LinkedList<>(lpm));
                            lpm.clear();
                        }
                        suggestionLoL.clear();
                        initialSize = 0;

                    }

                    if (newScore > highestScore) {
                        highestScore = newScore;
                        highestScoreIndex = k + initialSize;
                    }
                    List<Pair<Mapping, String>> suggestionList = new LinkedList<>();

                    //trace back to mappings in snapshot if the exact matching word is start with it.
                    if (suggestLoLSnapshot != null) {
                        for (int i = 0; i < suggestLoLSnapshot.size(); i++) {
                            if (suggestLoLSnapshot.get(i) != null && !suggestLoLSnapshot.get(i).isEmpty()
                                    && exactMatchMapping.getWord().startsWith(suggestLoLSnapshot.get(i).get(0).first.getWord())) {
                                suggestionList.add(suggestLoLSnapshot.get(i).get(0));
                                if (suggestLoLSnapshot.get(i).size() > 1) {
                                    for (int j = 1; j < suggestLoLSnapshot.get(i).size(); j++) {
                                        if (exactMatchMapping.getWord().startsWith(suggestLoLSnapshot.get(i).get(j).first.getWord()))
                                            suggestionList.add(suggestLoLSnapshot.get(i).get(j));
                                    }
                                }
                            }
                        }
                    }

                    suggestionList.add(new Pair<>(exactMatchMapping, code));
                    suggestionLoL.add(suggestionList);
                }
                k++;
                if (DEBUG || dumpRunTimeSuggestion)
                    Log.i(TAG, "makeRunTimeSuggestion(): Check  "+ k +"th exact match records. Time elapsed  = " + (System.currentTimeMillis() - startTime) );

            }while (completeCodeResultList.size() > k && completeCodeResultList.get(k).isExactMatchToCodeRecord() && k < 5); //process at most 5 exact match items.


            // clear suggestLoLSnapshot if it's not empty
            if (suggestLoLSnapshot != null) {
                for (List<Pair<Mapping, String>> lpm : suggestLoLSnapshot) {
                    lpm.clear();
                }
                suggestLoLSnapshot.clear();
            }
            if (!suggestionLoL.isEmpty() && highestScoreIndex != suggestionLoL.size() - 1) {//move bestSuggestionList to the last element
                List<Pair<Mapping, String>> bestSuggestionList = suggestionLoL.remove(highestScoreIndex);
                suggestionLoL.add(bestSuggestionList);

            }

        } else if (!suggestionLoL.isEmpty()) {  // no exact match recoreds found. search remaining code

            if (DEBUG || dumpRunTimeSuggestion)
                Log.i(TAG, "makeRunTimeSuggestion() no exact match on complete code = " + code + ", time elapsed = " + (System.currentTimeMillis() - startTime));

            /*
            // if confirmed best suggestion found and contains last confirmed best suggestion (double confirm) remove all other list not start with last confirmed best suggestion
            if( lastConfirmedBestSuggestion!=null && confirmedBestSuggestion!=null
                    &&lastConfirmedBestSuggestion.length()>1 && confirmedBestSuggestion.startsWith(lastConfirmedBestSuggestion)){
                Iterator<List<Pair<Mapping,String>>> it = suggestionLoL.iterator();
                while (it.hasNext()) {
                     List<Pair<Mapping,String>> item = it.next();
                    if(item.isEmpty()|| !item.get(item.size()-1).first.getWord().startsWith(lastConfirmedBestSuggestion)){
                        it.remove();
                    }
                }
            }
            */


            int highestScore = 0, highestRelatedScore = 0, i = 0, highestScoreIndex = 0;
            //iterate all previous exact match mapping and check for exact match on remaining code.
            List<List<Pair<Mapping, String>>> suggestionLoLSnapShot = new LinkedList<>(suggestionLoL);
            for (List<Pair<Mapping, String>> suggestionList : suggestionLoLSnapShot) {
                List<Pair<Mapping, String>> seedSuggestionList = suggestionLoL.remove(0);
                if (highestScoreIndex > 0) highestScoreIndex--;
                int lolSize = suggestionLoL.size();

                for (Pair<Mapping, String> p : suggestionList) {
                    String pCode = p.second;
                    if (pCode.length() < code.length() && code.startsWith(pCode) && code.length() - pCode.length() <= maxCodeLength) {
                        String remainingCode = code.substring(pCode.length(), code.length());
                        if (DEBUG || dumpRunTimeSuggestion)
                            Log.i(TAG, "makeRunTimeSuggestion() working on previous exact match item = " + p.first.getWord() +
                                    " with base score = " + p.first.getBasescore() + ", average score = " + p.first.getBasescore() / p.first.getWord().length() +
                                    ", remainingCode =" + remainingCode + " , highestScoreIndex = " + highestScoreIndex + ", time elapsed =" + (System.currentTimeMillis() - startTime));


                        List<Mapping> resultList =  //do remaining code query
                                getMappingByCodeFromCacheOrDB(remainingCode, false);
                        if (resultList == null) continue;

                        if (DEBUG || dumpRunTimeSuggestion)
                            Log.i(TAG, "makeRunTimeSuggestion() finish query on previous exact match item = " + p.first.getWord() +
                                    " , time elapsed =" + (System.currentTimeMillis() - startTime));

                        if (resultList.size() > 0
                                && resultList.get(0).isExactMatchToCodeRecord()) {  //remaining code search got exact match
                            Mapping remainingCodeExactMatchMapping = resultList.get(0);
                            Mapping previousMapping = p.first;
                            String phrase = previousMapping.getWord() + remainingCodeExactMatchMapping.getWord();
                            int phraseLen = phrase.length();
                            if (phraseLen < 2 || remainingCodeExactMatchMapping.getBasescore() < 2)
                                continue;
                            int remainingScore = remainingCodeExactMatchMapping.getBasescore();
                            int codeLenBonus = remainingCodeExactMatchMapping.getCode().length() /
                                    remainingCodeExactMatchMapping.getWord().length() * 30;
                            if (remainingScore > 120) remainingScore = 120;
                            remainingScore = remainingScore / remainingCodeExactMatchMapping.getWord().length() + codeLenBonus;

                            int previousScore = previousMapping.getBasescore() / previousMapping.getWord().length();
                            int averageScore = (previousScore + remainingScore) / 2;

                            if (DEBUG || dumpRunTimeSuggestion)
                                Log.i(TAG, "makeRunTimeSuggestion() remaining code = " + remainingCode + "" +
                                        ", got exact match  = " + remainingCodeExactMatchMapping.getWord() + " with base score = "
                                        + remainingScore + " average score =" + averageScore + " , highestScoreIndex = " + highestScoreIndex + ", time elapsed =" + (System.currentTimeMillis() - startTime));

                            //verify if the new phrase is in related table.
                            // check up to four characters phrase 1-3, 1-2 , 1-1
                            Mapping relatedMapping = null;
                            for (int k = ((phraseLen < 4) ? phraseLen - 1 : 3); k > 0; k--) {
                                String pword = phrase.substring(phraseLen - k - 1, phraseLen - k);
                                String cword = phrase.substring(phraseLen - k, phraseLen);
                                relatedMapping = dbadapter.isRelatedPhraseExist(pword, cword);
                                if (relatedMapping != null) break;
                            }
                            if (relatedMapping != null
                                    && relatedMapping.getBasescore() >= highestRelatedScore
                                    && (averageScore + 50) > highestScore
                                    ) {
                                Mapping suggestMapping = new Mapping();
                                suggestMapping.setRuntimeBuiltPhraseRecord();
                                suggestMapping.setCode(code);
                                suggestMapping.setWord(phrase);
                                highestRelatedScore = relatedMapping.getBasescore();
                                suggestMapping.setScore(highestRelatedScore);
                                highestScore = (averageScore + 50);
                                suggestMapping.setBasescore(highestScore * phraseLen);
                                List<Pair<Mapping, String>> newSuggestionList = new LinkedList<>(seedSuggestionList);
                                newSuggestionList.add(new Pair<>(suggestMapping, code));
                                suggestionLoL.add(newSuggestionList);
                                highestScoreIndex = suggestionLoL.size() - 1;
                                if (DEBUG || dumpRunTimeSuggestion)
                                    Log.i(TAG, "makeRunTimeSuggestion()  run-time suggest phrase verified from related table ="
                                            + phrase + ", basescore from related table = " + highestRelatedScore + " " +
                                            ", new average score = " + highestScore + " , highestScoreIndex = " + highestScoreIndex+ ", time elapsed =" + (System.currentTimeMillis() - startTime));
                            } else if (//highestRelatedScore == 0 &&// no mapping is verified from related table
                                    averageScore > highestScore) {
                                Mapping suggestMapping = new Mapping();
                                suggestMapping.setRuntimeBuiltPhraseRecord();
                                suggestMapping.setCode(code);
                                suggestMapping.setWord(phrase);
                                highestScore = averageScore;
                                suggestMapping.setBasescore(highestScore * phraseLen);

                                List<Pair<Mapping, String>> newSuggestionList = new LinkedList<>(seedSuggestionList);
                                newSuggestionList.add(new Pair<>(suggestMapping, code));
                                suggestionLoL.add(newSuggestionList);
                                highestScoreIndex = suggestionLoL.size() - 1;

                                if (DEBUG || dumpRunTimeSuggestion)
                                    Log.i(TAG, "makeRunTimeSuggestion()  run-time suggest phrase =" + phrase
                                            + ", new average score = " + highestScore + " , highestScoreIndex = " + highestScoreIndex+ ", time elapsed =" + (System.currentTimeMillis() - startTime));
                            }
                        }
                    }
                }
                if (lolSize == suggestionLoL.size()) {
                    suggestionLoL.add(seedSuggestionList);
                    if (DEBUG || dumpRunTimeSuggestion)
                        Log.i(TAG, "makeRunTimeSuggestion()  no new suggestion list. add back the seed suggestion list to location 0 because of last run.");
                }
                i++;
                if (DEBUG || dumpRunTimeSuggestion)
                    Log.i(TAG, "makeRunTimeSuggestion() : remaing cod search +" + i +"th run.  time elapsed = " + (System.currentTimeMillis()-startTime));
            }
            if (!suggestionLoL.isEmpty() && highestScoreIndex != suggestionLoL.size() - 1) {//move bestSuggestionList to the last element
                List<Pair<Mapping, String>> bestSuggestionList = suggestionLoL.remove(highestScoreIndex);
                suggestionLoL.add(bestSuggestionList);
            }

        }

        //push best suggestion to stack
        List<Pair<Mapping, String>> bestSuggestionList;
        if (!suggestionLoL.isEmpty()) {
            bestSuggestionList = suggestionLoL.get(suggestionLoL.size() - 1);
            if (bestSuggestionList != null && !bestSuggestionList.isEmpty()) {
                bestSuggestionStack.push(bestSuggestionList.get(bestSuggestionList.size() - 1));
            }
        }
        /*
        //find confirmed best suggestion with longest common string
        if (bestSuggestionStack != null && !bestSuggestionStack.isEmpty() && bestSuggestionStack.size() > 1) {
            for (int i = bestSuggestionStack.size() - 1; i > 0; i--) {
                if (code.length() - bestSuggestionStack.get(i).first.getCode().length() > maxCodeLength) {
                    String lastBestSuggestion = bestSuggestionStack.get(i - 1).first.getWord(), bestSuggestion = bestSuggestionStack.get(i).first.getWord();
                    if (lastBestSuggestion != null &&
                            lastBestSuggestion.length() > 1 && bestSuggestion.length() >= lastBestSuggestion.length()) {
                        String tempBestSuggestion = lcs(lastBestSuggestion, bestSuggestion);
                        if (confirmedBestSuggestion == null) {
                            confirmedBestSuggestion = tempBestSuggestion;
                        } else if (lastConfirmedBestSuggestion == null
                                || tempBestSuggestion.length() > lastConfirmedBestSuggestion.length()) {
                            lastConfirmedBestSuggestion = confirmedBestSuggestion;
                            confirmedBestSuggestion = tempBestSuggestion;
                        }
                    }
                    break;
                }
            }
            if ((DEBUG || dumpRunTimeSuggestion)) {
                if (lastConfirmedBestSuggestion != null)
                    Log.i(TAG, "makeRunTimeSuggestion() last confirmed best suggestion = " + lastConfirmedBestSuggestion);
                if (confirmedBestSuggestion != null)
                    Log.i(TAG, "makeRunTimeSuggestion() confirmed best suggestion = " + confirmedBestSuggestion);
                if (!bestSuggestionStack.isEmpty()) {
                    int i = 0;
                    for (Pair<Mapping, String> it : bestSuggestionStack) {
                        Log.i(TAG, "makeRunTimeSuggestion() best suggestion stack (" + (i) + ")= " + bestSuggestionStack.get(i).first.getWord());
                        i++;
                    }
                }
            }
        }
        */

        // dump suggestion list of list
        if ((DEBUG || dumpRunTimeSuggestion) &&
                suggestionLoL != null && !suggestionLoL.isEmpty()) {
            for (int i = 0; i < suggestionLoL.size(); i++) {
                if (suggestionLoL.get(i) != null && !suggestionLoL.get(i).isEmpty()) {
                    for (int j = 0; j < suggestionLoL.get(i).size(); j++) {
                        //Log.i(TAG, "makeRunTimeSuggestion() suggestionLoL(" + i + ")(" + j + "): word="
                        //        + suggestionLoL.get(i).get(j).first.getWord() + ", code=" + suggestionLoL.get(i).get(j).second
                        //        + ", base score=" + suggestionLoL.get(i).get(j).first.getBasescore()
                        //        + ", average base score=" + suggestionLoL.get(i).get(j).first.getBasescore() / suggestionLoL.get(i).get(j).first.getWord().length()
                        //        + ", score=" + suggestionLoL.get(i).get(j).first.getScore());
                    }
                }
            }

            //Log.i(TAG,"makeRunTimeSuggestion() time elapsed = " +  (System.currentTimeMillis()- startTime ) );
        }
    }

    /*
    *   return longest common substring with recursive method.
     */

    private String lcs(String a, String b) {
        int aLen = a.length();
        int bLen = b.length();
        if (aLen == 0 || bLen == 0) {
            return "";
        } else if (a.charAt(aLen - 1) == b.charAt(bLen - 1)) {
            return lcs(a.substring(0, aLen - 1), b.substring(0, bLen - 1))
                    + a.charAt(aLen - 1);
        } else {
            String x = lcs(a, b.substring(0, bLen - 1));
            String y = lcs(a.substring(0, aLen - 1), b);
            return (x.length() > y.length()) ? x : y;
        }
    }

    /*
    * Jeremy '15,7,12 synchronized the method called from LIMEService only
    */
    public synchronized List<Mapping> getMappingByCode(String code, boolean softkeyboard, boolean getAllRecords) throws RemoteException {
        return getMappingByCode(code, softkeyboard, getAllRecords, false);
    }

    private static boolean  abandonPhraseSuggestion = false;

    public List<Mapping> emojiConvert(String code, int type){
        if(code != null){
            if(emojicache == null){
                emojicache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);
            }
            List<Mapping> results = emojicache.get(code);
            if(emojicache.get(code) != null){
                return results;
            }else{
                //Log.i("EMOJI :" , "Run search emoji ...");
                results = dbadapter.emojiConvert(code, type);
                emojicache.put(code, results);
                return results;
            }
        }
        return null;
    }

    public List<Mapping> getMappingByCode(String code, boolean softkeyboard, boolean getAllRecords, boolean prefetchCache)
            throws RemoteException {
        if (DEBUG||dumpRunTimeSuggestion)
            Log.i(TAG, "getMappingByCode(): code=" + code);
        // Check if system need to reset cache

        //check reset cache with local variable instead of reading from shared preference for better performance
        if (mResetCache) {
            initialCache();
            mResetCache = false;
        }

        //codeLengthMap.clear();//Jeremy '12,6,2 reset the codeLengthMap

        List<Mapping> result = new LinkedList<>();
        if (code != null) {
            // clear mappingidx when user switching between softkeyboard and hard keyboard. Jeremy '11,6,11
            if (isPhysicalKeyboardPressed == softkeyboard)
                isPhysicalKeyboardPressed = !softkeyboard;

            // Jeremy '11,9, 3 remove cached keyname when request full records
            if (getAllRecords && keynamecache.get(cacheKey(code)) != null)
                keynamecache.remove(cacheKey(code));

            int size = code.length();

            //boolean hasMore = false;


            // 12,6,4 Jeremy. Ascending a ab abc... looking up db if the cache is not exist
            //'15,6,4 Jeremy. Do exact search only in between search mode (1 time only).
            List<Mapping> resultList
                    = getMappingByCodeFromCacheOrDB(code, getAllRecords);



            //Jeremy '15,7,16 reset abandonPhraseSuggestion if code length ==1
            if(mLIMEPref.getSmartChineseInput() && abandonPhraseSuggestion && code.length()==1){
                clearRunTimeSuggestion(false);
            }
            // make run-time suggestion '15, 6, 9 Jeremy.
            if (!abandonPhraseSuggestion && !prefetchCache && mLIMEPref.getSmartChineseInput()) {
                makeRunTimeSuggestion(code, resultList);
            }

            // 12,6,4 Jeremy. Descending  abc ab a... Build the result candidate list.
            //'15,6,4 Jeremy. Do exact search only in between search mode.
            //for (int i = 0; i < ((LimeDB.getBetweenSearch()) ? 1 : size); i++) {
            String cacheKey = cacheKey(code);
            List<Mapping> cacheTemp = cache.get(cacheKey);


            if (cacheTemp != null) {
                List<Mapping> resultlist = cacheTemp;

                //if getAllRecords is true and result list or related list has has more mark in the end
                // recall LimeDB.GetMappingByCode with getAllRecords true.
                if (getAllRecords &&
                        resultlist.size() > 1 && resultlist.get(resultlist.size() - 1).isHasMoreRecordsMarkRecord()) {
                    try {
                        cacheTemp = dbadapter.getMappingByCode(code, !isPhysicalKeyboardPressed, true);
                        cache.remove(cacheKey);
                        cache.put(cacheKey, cacheTemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            if (cacheTemp != null) {
                List<Mapping> resultlist = cacheTemp;
                //List<Mapping> relatedtlist = cacheTemp.second;

                if (DEBUG || dumpRunTimeSuggestion)
                    Log.i(TAG, "getMappingByCode() code=" + code + " resultlist.size()=" + resultlist.size() + ", abandonPhraseSuggestion:" + abandonPhraseSuggestion);


                //if (i == 0) {
                if (resultlist.size() == 0 && code.length() > 1) {
                    //If the result list is empty we need to go back to last result list with nonzero result list
                    String wayBackCode = code;
                    do {
                        wayBackCode = wayBackCode.substring(0, wayBackCode.length() - 1);
                        cacheTemp = cache.get(cacheKey(wayBackCode));
                        if (cacheTemp != null)
                            resultlist = cacheTemp;
                    } while (resultlist.size() == 0 && wayBackCode.length() > 1);
                }


                Mapping self = new Mapping();
                self.setWord(code);
                self.setCode(code);
                self.setComposingCodeRecord();
                // put run-time built suggestion if it's present
                        /*List<Pair<Mapping, String>> bestSuggestionList = null;
                        Mapping bestSuggestion = null;
                        if (!suggestionLoL.isEmpty()) {
                            bestSuggestionList = suggestionLoL.get(suggestionLoL.size() - 1);
                        }
                        if (bestSuggestionList != null && !bestSuggestionList.isEmpty()) {
                            bestSuggestion = bestSuggestionList.get(bestSuggestionList.size() - 1).first;
                        }*/

                //Jeremy '15,7,16 check english suggestion if code length > maxCodeLength
                Mapping englishSuggestion = null;
                if(code.length() > maxCodeLength) {
                    List<Mapping> englishSuggestions = getEnglishSuggestions(code);
                    if(englishSuggestions!=null && !englishSuggestions.isEmpty()) {
                        englishSuggestion = englishSuggestions.get(0);
                        englishSuggestion.setRuntimeBuiltPhraseRecord();
                        englishSuggestion.setCode(code);
                    }
                }


                Mapping bestSuggestion = null;
                if (bestSuggestionStack != null && !bestSuggestionStack.isEmpty()) {
                    bestSuggestion = bestSuggestionStack.lastElement().first;
                }
                int averageScore =(bestSuggestion==null)?0: (bestSuggestion.getBasescore()  / bestSuggestion.getWord().length());

                if (bestSuggestion != null    // the last element is run-time built suggestion from remaining code query
                        && !abandonPhraseSuggestion
                        && !bestSuggestion.isExactMatchToCodeRecord() //will be the first item of result list, dont' add duplicated item
                        && bestSuggestion.getWord().length() > 1
                        && ( (englishSuggestion==null && averageScore  > 120) || (englishSuggestion!=null && averageScore > 200 ))  ) {
                    result.add(self);
                    result.add(bestSuggestion);

                } else if( englishSuggestion!=null && averageScore <= 200){
                    clearRunTimeSuggestion(true);
                    result.add(self);
                    result.add(englishSuggestion);
                } else {
                    // put self into the first mapping for mixed input.
                    result.add(self);
                }
                // }

                if (resultlist.size() > 0) {
                    result.addAll(resultlist);
                    /*
                    int rsize = result.size();
                    if (result.get(rsize - 1).isHasMoreRecordsMarkRecord()) {
                        //do not need to touch the has more record in between search mode. Jeremy '15,6,4
                        result.remove(rsize - 1);
                        hasMore = true;

                        }
                        */
                    if (DEBUG)
                        Log.i(TAG, "getMappingByCode() code=" + code + "  result list added resultlist.size()="
                                + resultlist.size());

                }

            }
            //codeLengthMap is deprecated and replace by exact match stack scheme '15,6,3 jeremy
            //codeLengthMap.add(new Pair<>(code.length(), result.size()));  //Jeremy 12,6,2 preserve the code length in each loop.
            //if (DEBUG) 	Log.i(TAG, "getMappingByCode() codeLengthMap  code length = " + code.length() + ", result size = " + result.size());

            code = code.substring(0, code.length() - 1);
        }
        if (DEBUG)
            Log.i(TAG, "getMappingByCode() code=" + code + " result.size()=" + result.size());

        return result;

    }




	/*
    *   Get mapping list from cache or from db if it's not in cache. Separated from getMappingByCode() Jeremy '15,6,8
	 */

    private List<Mapping> getMappingByCodeFromCacheOrDB(String queryCode, Boolean getAllRecords) {
        String cacheKey = cacheKey(queryCode);
        List<Mapping> cacheTemp = cache.get(cacheKey);

        if (DEBUG)
            Log.i(TAG, " getMappingByCode() check if cached exist on code = '" + queryCode + "'");

        if (cacheTemp == null) {
            // 25/Jul/2011 by Art
            // Just ignore error when something wrong with the result set
            try {
                cacheTemp = dbadapter.getMappingByCode(queryCode, !isPhysicalKeyboardPressed, getAllRecords);
                if (cacheTemp != null) cache.put(cacheKey, cacheTemp);
                //Jeremy '12,6,5 check if need to update code remap cache
                if (cacheTemp != null && cacheTemp != null
                        && cacheTemp.size() > 0 && cacheTemp.get(0) != null
                        && cacheTemp.get(0).isExactMatchToCodeRecord()) {
                    String remappedCode = cacheTemp.get(0).getCode();
                    if (!queryCode.equals(remappedCode)) {
                        List<String> codeList = coderemapcache.get(remappedCode);
                        String key = cacheKey(remappedCode);
                        if (codeList == null) {
                            List<String> newlist = new LinkedList<>();
                            newlist.add(remappedCode); //put self in the list
                            newlist.add(queryCode);
                            coderemapcache.put(key, newlist);
                            if (DEBUG)
                                Log.i(TAG, "getMappingByCode() build new remap code = '"
                                        + remappedCode + "' to code = '" + queryCode + "'"
                                        + " coderemapcache.size()=" + coderemapcache.size());
                        } else {
                            codeList.add(queryCode);
                            coderemapcache.remove(key);
                            coderemapcache.put(key, codeList);
                            if (DEBUG)
                                Log.i(TAG, "getMappingByCode() remappedCode: add new remap code = '" + remappedCode + "' to code = '" + queryCode + "'");
                        }

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cacheTemp;


}

    /**
     * get real code length
     */
    int getRealCodeLength(final Mapping selectedMapping, String currentCode) {
        if (DEBUG)
            Log.i(TAG, "getRealCodeLength()");

        String code = selectedMapping.getCode();
        int realCodeLen = code.length();
        if (LimeDB.isCodeDualMapped()) { //abandon LD support for dual mapped codes. Jeremy '15,6,5
            realCodeLen = currentCode.length();
        } else {
            if (tablename.equals("phonetic")) {
                String selectedPhoneticKeyboardType =
                        mLIMEPref.getParameterString("phonetic_keyboard_type", "standard");
                String lcode = currentCode;
                if (selectedPhoneticKeyboardType.startsWith("eten")) {
                    lcode = dbadapter.preProcessingRemappingCode(currentCode);
                }
                String noToneCode = code.replaceAll("[3467 ]", "");
                if (code.equals(noToneCode)) {
                    realCodeLen = code.length();
                } else if (!lcode.startsWith(code) && lcode.startsWith(noToneCode)) {
                    realCodeLen = noToneCode.length();
                } else {
                    realCodeLen = currentCode.length(); //unexpected condition.
                }
            }
        }

        //remove elements in suggestionLoL with code length smaller than current code length - submitted code length
        if (realCodeLen < currentCode.length()) {
            Iterator<List<Pair<Mapping, String>>> itl = suggestionLoL.iterator();
            while (itl.hasNext()) {
                List<Pair<Mapping, String>> lpe = itl.next();
                Iterator<Pair<Mapping, String>> it = lpe.iterator();
                while (it.hasNext()) {
                    Pair<Mapping, String> pe = it.next();
                    if (pe.second.length() > currentCode.length() - realCodeLen) {
                        it.remove();
                    }
                }
                if (lpe.isEmpty()) itl.remove();
            }
            Iterator<Pair<Mapping, String>> it = bestSuggestionStack.iterator();
            while (it.hasNext()) {
                Pair<Mapping, String> pe = it.next();
                if (pe.second.length() > currentCode.length() - realCodeLen) {
                    it.remove();
                }
            }
        }

        // learn ld phrase if the select mapping is run-time suggestion
        if (selectedMapping != null && selectedMapping.isRuntimeBuiltPhraseRecord() &&
                suggestionLoL != null && !suggestionLoL.isEmpty()) {

            final List<Pair<Mapping, String>> bestSuggestionList = new LinkedList<>(suggestionLoL.get(suggestionLoL.size() - 1));
            final String selectedWord = selectedMapping.getWord();

            Thread learnLDPhraseThread = new Thread() {
                public void run() {

                    if (!bestSuggestionList.isEmpty()) {
                        for (int j = 0; j < bestSuggestionList.size(); j++) {
                            //TODO:should learn QP code for phonetic table
                            if (selectedWord.startsWith(bestSuggestionList.get(j).first.getWord())) {
                                if (bestSuggestionList.get(j).first.getWord().length() > 8)
                                    break; //stop learning if word length > 8
                                dbadapter.addOrUpdateMappingRecord(bestSuggestionList.get(j).second, bestSuggestionList.get(j).first.getWord());
                                removeRemappedCodeCachedMappings(bestSuggestionList.get(j).second);
                            }

                            if ((DEBUG || dumpRunTimeSuggestion))// dump best suggestion list
                                Log.i(TAG, "getRealCodeLength() best suggestion list(" + j + "): word="
                                        + bestSuggestionList.get(j).first.getWord() + ", code=" + bestSuggestionList.get(j).second);

                        }

                    }

                }
            };
            learnLDPhraseThread.start();

        }

        return realCodeLen;
    }


    /**
     * This method is to initial/reset the cache of im.
     */
    public void initialCache() {
        try {
            clear();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        cache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);
        engcache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);
        emojicache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);
        keynamecache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);
        coderemapcache = new ConcurrentHashMap<>(LIME.SEARCHSRV_RESET_CACHE_SIZE);

        //  initial exact match stack here
        suggestionLoL = new LinkedList<>();
        bestSuggestionStack = new Stack<>();

    }


    private void updateScoreCache(Mapping cachedMapping) {
        if (DEBUG) Log.i(TAG, "updateScoreCache(): code=" + cachedMapping.getCode());

        dbadapter.addScore(cachedMapping);
        // Jeremy '11,7,29 update cached here
        if (!cachedMapping.isRelatedPhraseRecord()) {
            String code = cachedMapping.getCode().toLowerCase(Locale.US);
            String cachekey = cacheKey(code);
            List<Mapping> cachedList = cache.get(cachekey);
            // null id denotes target is selected from the related list (not exact match)
            if ((cachedMapping.getId() == null || cachedMapping.isPartialMatchToCodeRecord()) //Jeremy '15,6,3 new record type to identify partial match
                    && cachedList != null && !cachedList.isEmpty()) {
                if (DEBUG) Log.i(TAG, "updateScoreCache(): updating related list");
                if (cache.remove(cachekey) == null) {
                    removeRemappedCodeCachedMappings(code);
                }
                // non null id denotes target is in exact match result list.
            } else if ((cachedMapping.getId() != null || cachedMapping.isExactMatchToCodeRecord()) //Jeremy '15,6,3 new record type to identify exact match
                    && cachedList != null && !cachedList.isEmpty()) {

                boolean sort;
                if (isPhysicalKeyboardPressed)
                    sort = mLIMEPref.getPhysicalKeyboardSortSuggestions();
                else
                    sort = mLIMEPref.getSortSuggestions();

                if (sort) { // Jeremy '12,5,22 do not update the order of exact match list if the sort option is off
                    int size = cachedList.size();
                    if (DEBUG) Log.i(TAG, "updateScoreCache(): cachedList.size:" + size);
                    // update exact match cache
                    for (int j = 0; j < size; j++) {
                        Mapping cm = cachedList.get(j);
                        if (DEBUG)
                            Log.i(TAG, "updateScoreCache(): cachedList at :" + j + ". score=" + cm.getScore());
                        if (cachedMapping.getId().equals(cm.getId())) {
                            int score = cm.getScore() + 1;
                            if (DEBUG)
                                Log.i(TAG, "updateScoreCache(): cachedMapping found at :" + j + ". new score=" + score);
                            cm.setScore(score);
                            if (j > 0 && score > cachedList.get(j - 1).getScore()) {
                                cachedList.remove(j);
                                for (int k = 0; k < j; k++) {
                                    if (cachedList.get(k).getScore() <= score) {
                                        cachedList.add(k, cm);
                                        break;
                                    }
                                }

                            }
                            break;
                        }
                    }
                }
                // Jeremy '11,7,31
                // exact match score was changed, related list in similar codes should be rebuild
                // (eg. d, de, and def for code, defg)
                updateSimilarCodeCache(code);


            } else {//Jeremy '12,6,5 code not in cache do removeRemappedCodeCachedMappings and removed cached items of  ramped codes.

                removeRemappedCodeCachedMappings(code);
            }
        }


    }

// '11,8,1 renamed from updateuserdict()
List<Mapping> scorelistSnapshot = null;

    public void postFinishInput() throws RemoteException {

        if (scorelistSnapshot == null) scorelistSnapshot = new LinkedList<>();
        else scorelistSnapshot.clear();


        if (DEBUG) Log.i(TAG, "postFinishInput(), creating offline updating thread");
        // Jeremy '11,7,31 The updating process takes some time. Create a new thread to do this.
        Thread UpdatingThread = new Thread() {
            public void run() {
                // for thread-safe operation, duplicate local copy of scorelist and LDphraselistarray
                //List<Mapping> localScorelist = new LinkedList<Mapping>();
                if (scorelist != null) {
                    scorelistSnapshot.addAll(scorelist);
                    scorelist.clear();
                }
                //Jeremy '11,7,28 combine to adduserdict and addscore
                //Jeremy '11,6,12 do adduserdict and add score if diclist.size > 0 and only adduserdict if diclist.size >1
                //Jeremy '11,6,11, always learn scores, but sorted according preference options

                // Learn the consecutive two words as a related phrase).
                learnRelatedPhrase(scorelistSnapshot);

                ArrayList<List<Mapping>> localLDPhraseListArray = new ArrayList<>();
                if (LDPhraseListArray != null) {
                    localLDPhraseListArray.addAll(LDPhraseListArray);
                    LDPhraseListArray.clear();
                }

                // Learn LD Phrase
                learnLDPhrase(localLDPhraseListArray);

            }
        };
        UpdatingThread.start();

    }

    private void learnRelatedPhrase(List<Mapping> localScorelist) {
        if (localScorelist != null) {
            if (DEBUG)
                Log.i(TAG, "learnRelatedPhrase(), localScorelist.size=" + localScorelist.size());
            if (mLIMEPref.getLearnRelatedWord() && localScorelist.size() > 1) {
                for (int i = 0; i < localScorelist.size(); i++) {
                    Mapping unit = localScorelist.get(i);
                    if (unit == null) {
                        continue;
                    }
                    if (i + 1 < localScorelist.size()) {
                        Mapping unit2 = localScorelist.get((i + 1));
                        if (unit2 == null) {
                            continue;
                        }
                        if (unit.getWord() != null && !unit.getWord().equals("")

                                && unit2.getWord() != null && !unit2.getWord().equals("")

                                &&
                                (unit.isExactMatchToCodeRecord() || unit.isPartialMatchToCodeRecord()
                                || unit.isRelatedPhraseRecord()) // use record type to identify records. Jeremy '15,6,4

                                &&
                                (unit2.isExactMatchToCodeRecord() || unit2.isPartialMatchToCodeRecord()
                                || unit.isRelatedPhraseRecord() || unit2.isChinesePunctuationSymbolRecord()
                                || unit.isEmojiRecord() || unit2.isEmojiRecord() )

                                //allow unit2 to be chinese punctuation symbols.
                                //&& !unit.getCode().equals(unit.getWord())//Jeremy '12,6,13 avoid learning mixed mode english
                                //&& !unit2.getCode().equals(unit2.getWord())
                                ///&& unit2.getId() !=null
                                ) {

                            int score;

                            //if (unit.getId() != null && unit2.getId() != null) //Jeremy '12,7,2 eliminate learning english words.
                            score = dbadapter.addOrUpdateRelatedPhraseRecord(unit.getWord(), unit2.getWord());
                            if (DEBUG)
                                Log.i(TAG, "learnRelatedPhrase(), the return score = " + score);
                            //Jeremy '12,6,7 learn LD phrase if the score of userdic is > 20
                            if (score > 20 && mLIMEPref.getLearnPhrase()) {
                                addLDPhrase(unit, false);
                                addLDPhrase(unit2, true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Jeremy '12,6,9 Rewrite to support word with more than 1 characters
     */

    private void learnLDPhrase(ArrayList<List<Mapping>> localLDPhraseListArray) {
        if (DEBUG)
            Log.i(TAG, "learnLDPhrase()");

        if (localLDPhraseListArray != null && localLDPhraseListArray.size() > 0) {
            if (DEBUG)
                Log.i(TAG, "learnLDPhrase(): LDPhrase learning, arraysize =" + localLDPhraseListArray.size());


            for (List<Mapping> phraselist : localLDPhraseListArray) {
                if (DEBUG)
                    Log.i(TAG, "learnLDPhrase(): LDPhrase learning, current list size =" + phraselist.size());
                if (phraselist.size() > 0 && phraselist.size() < 5) { //Jeremy '12,6,8 limit the phrase to have 4 chracters


                    String baseCode, LDCode="", QPCode = "", baseWord;

                    Mapping unit1 = phraselist.get(0);

                    if (DEBUG)
                        Log.i(TAG, "learnLDPhrase(): unit1.getId() = " + unit1.getId()
                                + ", unit1.getCode() =" + unit1.getCode()
                                + ", unit1.getWord() =" + unit1.getWord());

                    if (unit1 == null || unit1.getWord().length() == 0
                            || unit1.getCode().equals(unit1.getWord())) //Jeremy '12,6,13 avoid learning mixed mode english
                    {
                        break;
                    }

                    baseCode = unit1.getCode();
                    baseWord = unit1.getWord();

                    if (baseWord.length() == 1) {
                        if (unit1.getId() == null //Jeremy '12,6,7 break if id is null (selected from related list)
                                || unit1.isPartialMatchToCodeRecord() //Jeremy '15,6,3 new record identification
                                || unit1.getCode() == null //Jeremy '12,6,7 break if code is null (selected from related phrase)
                                || unit1.getCode().length() == 0
                                || unit1.isRelatedPhraseRecord()) {
                            List<Mapping> rMappingList = dbadapter.getMappingByWord(baseWord, tablename);
                            if (rMappingList.size() > 0)
                                baseCode = rMappingList.get(0).getCode();
                            else
                                break; //look-up failed, abandon.
                        }
                        if (baseCode != null && baseCode.length() > 0)
                            QPCode += baseCode.substring(0, 1);
                        else
                            break;//abandon the phrase learning process;

                        //if word length >0, lookup all codes and rebuild basecode and QPCode
                    } else if (baseWord.length() > 1 && baseWord.length() < 5) {
                        baseCode = "";
                        for (int i = 0; i < baseWord.length(); i++) {
                            String c = baseWord.substring(i, i + 1);
                            List<Mapping> rMappingList = dbadapter.getMappingByWord(c, tablename);
                            if (rMappingList.size() > 0) {
                                baseCode += rMappingList.get(0).getCode();
                                QPCode += rMappingList.get(0).getCode().substring(0, 1);
                            } else {
                                baseCode = ""; //r-lookup failed. abandon the phrase learning
                                break;
                            }
                        }
                    }


                    for (int i = 0; i < phraselist.size(); i++) {
                        if (i + 1 < phraselist.size()) {

                            Mapping unit2 = phraselist.get((i + 1));
                            if (unit2 == null || unit2.getWord().length() == 0 || unit2.isComposingCodeRecord() || unit2.isEnglishSuggestionRecord()) //Jeremy 15,6,4 exclude composing code
                            //|| unit2.getCode().equals(unit2.getWord())) //Jeremy '12,6,13 avoid learning mixed mode english
                            {
                                break;
                            }

                            String word2 = unit2.getWord();
                            String code2 = unit2.getCode();
                            baseWord += word2;

                            if (word2.length() == 1 && baseWord.length() < 5) { //limit the phrase size to 4
                                if (unit2.getId() == null //Jeremy '12,6,7 break if id is null (selected from related phrase)
                                        || unit2.isPartialMatchToCodeRecord() //Jeremy '15,6,3 new record identification
                                        || code2 == null //Jeremy '12,6,7 break if code is null (selected from relatedphrase)
                                        || code2.length() == 0
                                        || unit2.isRelatedPhraseRecord()) {
                                    List<Mapping> rMappingList = dbadapter.getMappingByWord(word2, tablename);
                                    if (rMappingList.size() > 0)
                                        code2 = rMappingList.get(0).getCode();
                                    else
                                        break;
                                }
                                if (code2 != null && code2.length() > 0) {
                                    baseCode += code2;
                                    QPCode += code2.substring(0, 1);
                                } else
                                    break; //abandon the phrase learning process;

                                //if word length >0, lookup all codes and rebuild basecode and QPCode
                            } else if (word2.length() > 1 && baseWord.length() < 5) {
                                for (int j = 0; j < word2.length(); j++) {
                                    String c = word2.substring(j, j + 1);
                                    List<Mapping> rMappingList = dbadapter.getMappingByWord(c, tablename);
                                    if (rMappingList.size() > 0) {
                                        baseCode += rMappingList.get(0).getCode();
                                        QPCode += rMappingList.get(0).getCode().substring(0, 1);
                                    } else //r-lookup failed. abandon the phrase learning
                                        break;
                                }
                            } else  // abandon the learing process.
                                break;


                            if (DEBUG)
                                Log.i(TAG, "learnLDPhrase(): code1 = " + unit1.getCode()
                                        + ", code2 = '" + code2
                                        + "', word1 = " + unit1.getWord()
                                        + ", word2 = " + word2
                                        + ", basecode = '" + baseCode
                                        + "', baseWord = " + baseWord
                                        + ", QPcode = '" + QPCode
                                        + "'.");
                            if (i + 1 == phraselist.size() - 1) {//only learn at the end of the phrase word '12,6,8
                                if (tablename.equals("phonetic")) {// remove tone symbol in phonetic table
                                    LDCode = baseCode.replaceAll("[3467 ]", "").toLowerCase(Locale.US);
                                    QPCode = QPCode.toLowerCase(Locale.US);
                                    if (LDCode.length() > 1) {
                                        dbadapter.addOrUpdateMappingRecord(LDCode, baseWord);
                                        removeRemappedCodeCachedMappings(LDCode);
                                        updateSimilarCodeCache(LDCode);
                                    }
                                    if (QPCode.length() > 1) {
                                        dbadapter.addOrUpdateMappingRecord(QPCode, baseWord);
                                        removeRemappedCodeCachedMappings(QPCode);
                                        updateSimilarCodeCache(QPCode);
                                    }
                                } else if (baseCode.length() > 1) {
                                    baseCode = baseCode.toLowerCase(Locale.US);
                                    dbadapter.addOrUpdateMappingRecord(baseCode, baseWord);
                                    removeRemappedCodeCachedMappings(baseCode);
                                    updateSimilarCodeCache(baseCode);
                                }
                                if (DEBUG)
                                    Log.i(TAG, "learnLDPhrase(): LDPhrase learning, baseCode = '" + baseCode
                                            + "', LDCode = '" + LDCode + "', QPCode=" + QPCode + "'."
                                            + ", baseWord" + baseWord);

                            }


                        }
                    }
                }
            }


        }
    }

    /**
     *
     */
    private void removeRemappedCodeCachedMappings(String code) {
        if (DEBUG)
            Log.i(TAG, "removeRemappedCodeCachedMappings() on code ='" + code + "' coderemapcache.size=" + coderemapcache.size());
        List<String> codelist = coderemapcache.get(cacheKey(code));
        if (codelist != null) {
            for (String entry : codelist) {
                if (DEBUG)
                    Log.i(TAG, "removeRemappedCodeCachedMappings() remove code= '" + entry + "' from cache.");
                cache.remove(cacheKey(entry));
            }
        } else
            cache.remove(cacheKey(code)); //Jeremy '12,6,6 no remap. remove the code mapping from cache.
    }

    private void updateSimilarCodeCache(String code) {
        if (DEBUG)
            Log.i(TAG, "updateSimilarCodeCache(): code = '" + code + "'");
        String cachekey;
        List<Mapping> cachedList;// = cache.get(cachekey);
        int len = code.length();
        if (len > 5) len = 5; //Jeremy '12,6,7 change max backward level to 5.
        for (int k = 1; k < len; k++) {
            String key = code.substring(0, code.length() - k);
            cachekey = cacheKey(key);
            cachedList = cache.get(cachekey);
            if (DEBUG)
                Log.i(TAG, "updateSimilarCodeCache(): cachekey = '" + cachekey + "' cachedList == null :" + (cachedList == null));
            if (cachedList != null) {
                cache.remove(cachekey);
            } else {
                if (DEBUG)
                    Log.i(TAG, "updateSimilarCodeCache(): code not in cache. update to db only on code = '" + key + "'");
                removeRemappedCodeCachedMappings(key);
            }
            if (code.length() == 1)// prefetch if code length ==1
                try {
                    getMappingByCode(code, !isPhysicalKeyboardPressed, false, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    }


    public String keyToKeyname(String code) {
        //Jeremy '11,6,21 Build cache according using cachekey

        String cacheKey = cacheKey(code);
        String result = keynamecache.get(cacheKey);
        if (result == null) {
            //loadDBAdapter(); openLimeDatabase();
            result = dbadapter.keyToKeyname(code, tablename, true);
            keynamecache.put(cacheKey, result);
        }
        return result;
    }

    /**
     * Renamed from addUserDict and pass parameter with mapping directly Jeremy '12,6,5
     * Renamed to learnRelatedPhraseAndUpdateScore Jeremy '15,6,4
     */

    public void learnRelatedPhraseAndUpdateScore(Mapping updateMapping)
    //String id, String code, String word,
    //String pword, int score, boolean isDictionary)
            throws RemoteException {
        if (DEBUG) Log.i(TAG, "learnRelatedPhraseAndUpdateScore() ");

        if (scorelist == null) {
            scorelist = new ArrayList<>();
        }

        // Temp final Mapping Object For updateMapping thread.
        if (updateMapping != null) {
            final Mapping updateMappingTemp = new Mapping(updateMapping);

            // Jeremy '11,6,11. Always update score and sort according to preferences.
            scorelist.add(updateMappingTemp);
            Thread UpdatingThread = new Thread() {
                public void run() {
                    updateScoreCache(updateMappingTemp);
                }
            };
            UpdatingThread.start();
        }
    }

    public void addLDPhrase(Mapping mapping,//String id, String code, String word, int score,
                            boolean ending) {
        if (LDPhraseListArray == null)
            LDPhraseListArray = new ArrayList<>();
        if (LDPhraseList == null)
            LDPhraseList = new LinkedList<>();


        if (mapping != null) { // force interruped if mapping=null
            LDPhraseList.add(mapping);
        }

        if (ending) {
            if (LDPhraseList.size() > 1)
                LDPhraseListArray.add(LDPhraseList);
            LDPhraseList = new LinkedList<>();
        }

        if (DEBUG) Log.i(TAG, "addLDPhrase()"//+mapping.getCode() + ". id=" + mapping.getId()
                + ". ending:" + ending
                + ". LDPhraseListArray.size=" + LDPhraseListArray.size()
                + ". LDPhraseList.size=" + LDPhraseList.size());


    }

    public List<KeyboardObj> getKeyboardList() throws RemoteException {
        //if(dbadapter == null){dbadapter = new LimeDB(ctx);}
        return dbadapter.getKeyboardList();
    }

    public List<ImObj> getImList() throws RemoteException {
        //if(dbadapter == null){dbadapter = new LimeDB(ctx);}
        return dbadapter.getImList();
    }


    public void clear() throws RemoteException {
        if (scorelist != null) {
            scorelist.clear();
        }
        if (scorelist != null) {
            scorelist.clear();
        }
        if (cache != null) {
            cache.clear();
        }
        if (engcache != null) {
            engcache.clear();
        }
        if (emojicache != null) {
            emojicache.clear();
        }
        if (keynamecache != null) {
            keynamecache.clear();
        }

        if (coderemapcache != null) {
            coderemapcache.clear();
        }
    }

    private static String lastEnglishWord = null;
    private static boolean noSuggestionsForLastEnglishWord = false;

    public synchronized List<Mapping> getEnglishSuggestions(String word) throws RemoteException {

        long startTime=0;
        if(DEBUG||dumpRunTimeSuggestion){
            startTime = System.currentTimeMillis();
            Log.i(TAG,"getEnglishSuggestions()");
        }

        List<Mapping> result = new LinkedList<>();

        //Jeremy '15,7,16 return zero result if last query returns no result
        if(!( word.length()>1 &&lastEnglishWord!=null &&word.startsWith(lastEnglishWord) && noSuggestionsForLastEnglishWord  ) ) {

            List<Mapping> cacheTemp = engcache.get(word);

            if (cacheTemp != null) {
                result.addAll(cacheTemp);
            } else {
                List<String> tempResult = dbadapter.getEnglishSuggestions(word);
                for (String u : tempResult) {
                    Mapping temp = new Mapping();
                    temp.setWord(u);
                    temp.setEnglishSuggestionRecord();
                    result.add(temp);
                }
                if (result.size() > 0) {
                    engcache.put(word, result);
                }
            }

            noSuggestionsForLastEnglishWord = result.isEmpty();
            lastEnglishWord = word;
        }

        if(DEBUG||dumpRunTimeSuggestion){
            Log.i(TAG,"getEnglishSuggestions() time elapsed =" + (System.currentTimeMillis() - startTime));
        }

        return result;

    }

    /*
        public boolean isImKeys(char c) throws RemoteException {
            if (imKeysMap.get(tablename) == null || imKeysMap.size() == 0) {
                //if(dbadapter == null){dbadapter = new LimeDB(ctx);}
                imKeysMap.put(tablename, dbadapter.getImInfo(tablename, "imkeys"));
            }
            String imkeys = imKeysMap.get(tablename);
            return !(imkeys == null || imkeys.equals("")) && (imkeys.indexOf(c) >= 0);
        }
    */
    public String getSelkey() throws RemoteException {
        if (DEBUG)
            Log.i(TAG, "getSelkey():hasNumber:" + hasNumberMapping + "hasSymbol:" + hasSymbolMapping);
        String selkey;
        String table = tablename;
        if (tablename.equals("phonetic")) {
            table = tablename + mLIMEPref.getPhoneticKeyboardType();
        }
        if (selKeyMap.get(table) == null || selKeyMap.size() == 0) {
            //if(dbadapter == null){dbadapter = new LimeDB(ctx);}
            selkey = dbadapter.getImInfo(tablename, "selkey");
            if (DEBUG)
                Log.i(TAG, "getSelkey():selkey from db:" + selkey);
            boolean validSelkey = true;
            if (selkey != null && selkey.length() == 10) {
                for (int i = 0; i < 10; i++) {
                    if (Character.isLetter(selkey.charAt(i)) ||
                            (hasNumberMapping && Character.isDigit(selkey.charAt(i))))
                        validSelkey = false;

                }
            } else
                validSelkey = false;
            //Jeremy '11,6,19 Rewrite for IM has symbol mapping like ETEN
            if (!validSelkey || tablename.equals("phonetic")) {
                if (hasNumberMapping && hasSymbolMapping) {
                    if (tablename.equals("dayi")
                            || (tablename.equals("phonetic") && mLIMEPref.getPhoneticKeyboardType().equals("standard"))) {
                        selkey = "'[]-\\^&*()";
                    } else {
                        selkey = "!@#$%^&*()";
                    }
                } else if (hasNumberMapping) {
                    selkey = "'[]-\\^&*()";
                } else {
                    selkey = "1234567890";
                }
            }
            if (DEBUG)
                Log.i(TAG, "getSelkey():selkey:" + selkey);
            selKeyMap.put(table, selkey);
        }
        return selKeyMap.get(table);
    }
/*
    private class runTimeSuggestion {

        private List<List<Pair<Mapping, String>>> suggestionLoL;
        private int level;

        public runTimeSuggestion() {
            suggestionLoL = new LinkedList<>();
        }

        public void addExactMatch(String code, List<Mapping> completeCodeResultList) {
            Mapping exactMatchMapping;
            level++;

            int i = 0;
            do {
                exactMatchMapping = completeCodeResultList.get(i);
                int score = exactMatchMapping.getBasescore();
                if (score < 120) {
                    score = 120;
                } else if (score > 200) {
                    score = 200;
                }
                int codeLenBonus = exactMatchMapping.getCode().length() / exactMatchMapping.getWord().length() * 30;
                exactMatchMapping.setBasescore((score + codeLenBonus) * exactMatchMapping.getWord().length());

                if (DEBUG || dumpRunTimeSuggestion)
                    Log.i(TAG, "addExactMatch() complete code = " + code + "" +
                            ", got exact match  = " + exactMatchMapping.getWord()
                            + " score =" + exactMatchMapping.getScore() + ", basescore=" + exactMatchMapping.getBasescore());


                //push the exact match mapping with current code into exact match stack. '15,6,2 Jeremy
                if (exactMatchMapping.getBasescore() > 0) {
                    List<Pair<Mapping, String>> suggestionList = new LinkedList<>();
                    suggestionList.add(new Pair<>(exactMatchMapping, code));
                    suggestionLoL.add(suggestionList);
                }
                i++;
            }
            while (completeCodeResultList.size() > i
                    && completeCodeResultList.get(i).isExactMatchToCodeRecord() && i < 5); //process at most 5 exact match items.

        }

        public void checkRemainingCode(String code) {

            int highestScore = 0, highestRelatedScore = 0;
            //iterate all previous exact match mapping and check for exact match on remaining code.
            for (List<Pair<Mapping, String>> suggestionList : suggestionLoL) {
                for (Pair<Mapping, String> p : suggestionList) {
                    String pCode = p.second;
                    if (pCode.length() < code.length() && code.startsWith(pCode) && code.length() - pCode.length() <= 5) {
                        String remainingCode = code.substring(pCode.length(), code.length());
                        Log.i(TAG, "makeRunTimeSuggestion() working on previous exact match item = " + p.first.getWord() +
                                " with base score = " + p.first.getBasescore() + ", average score = " + p.first.getBasescore() / p.first.getWord().length() +
                                ", remainingCode =" + remainingCode);


                        Pair<List<Mapping>, List<Mapping>> resultPair =  //do remaining code query
                                getMappingByCodeFromCacheOrDB(remainingCode, false);
                        if (resultPair == null) continue;

                        List<Mapping> resultList = resultPair.first;
                        if (resultList.size() > 0
                                && resultList.get(0).isExactMatchToCodeRecord()) {  //remaining code search got exact match
                            Mapping remainingCodeExactMatchMapping = resultList.get(0);
                            Mapping previousMapping = p.first;
                            String phrase = previousMapping.getWord() + remainingCodeExactMatchMapping.getWord();
                            int phraseLen = phrase.length();
                            if (phraseLen < 2 || remainingCodeExactMatchMapping.getBasescore() < 2)
                                continue;
                            int remainingScore = remainingCodeExactMatchMapping.getBasescore();
                            int codeLenBonus = remainingCodeExactMatchMapping.getCode().length() /
                                    remainingCodeExactMatchMapping.getWord().length() * 30;
                            if (remainingScore > 120) remainingScore = 120;
                            remainingScore = remainingScore / remainingCodeExactMatchMapping.getWord().length() + codeLenBonus;

                            int previousScore = previousMapping.getBasescore() / previousMapping.getWord().length();
                            int averageScore = (previousScore + remainingScore) / 2;

                            if (DEBUG || dumpRunTimeSuggestion)
                                Log.i(TAG, "makeRunTimeSuggestion() remaining code = " + remainingCode + "" +
                                        ", got exact match  = " + remainingCodeExactMatchMapping.getWord() + " with base score = "
                                        + remainingScore + " average score =" + averageScore);

                            //verify if the new phrase is in related table.
                            // check up to four characters phrase 1-3, 1-2 , 1-1
                            Mapping relatedMapping = null;
                            for (int i = ((phraseLen < 4) ? phraseLen - 1 : 3); i > 0; i--) {
                                String pword = phrase.substring(phraseLen - i - 1, phraseLen - i);
                                String cword = phrase.substring(phraseLen - i, phraseLen);
                                relatedMapping = dbadapter.isRelatedPhraseExist(pword, cword);
                                if (relatedMapping != null) break;
                            }
                            if (relatedMapping != null
                                    && relatedMapping.getScore() >= highestRelatedScore
                                //&& averageScore > highestScore
                                    ) {
                                Mapping suggestMapping = new Mapping();
                                suggestMapping.setRuntimeBuiltPhraseRecord();
                                suggestMapping.setCode(code);
                                suggestMapping.setWord(phrase);
                                highestRelatedScore = relatedMapping.getBasescore();
                                suggestMapping.setScore(highestRelatedScore);

                                suggestMapping.setBasescore((averageScore + 50) * phraseLen);
                                suggestionList.add(new Pair<>(suggestMapping, code));
                                if (DEBUG || dumpRunTimeSuggestion)
                                    Log.i(TAG, "makeRunTimeSuggestion()  run-time suggest phrase verified from related table ="
                                            + phrase + "score from related table = " + highestRelatedScore + " , new base score = " + suggestMapping.getBasescore());
                            } else if (highestRelatedScore == 0// no mapping is verified from related table
                                    && averageScore > highestScore) {
                                Mapping suggestMapping = new Mapping();
                                suggestMapping.setRuntimeBuiltPhraseRecord();
                                suggestMapping.setCode(code);
                                suggestMapping.setWord(phrase);
                                highestScore = averageScore;
                                suggestMapping.setBasescore(highestScore * phraseLen);
                                suggestionList.add(new Pair<>(suggestMapping, code));
                                if (DEBUG || dumpRunTimeSuggestion)
                                    Log.i(TAG, "makeRunTimeSuggestion()  run-time suggest phrase =" + phrase
                                            + ", new base score = " + highestScore);
                            }
                        }
                    }
                }
            }
        }

        public void clear() {
            level = 0;
            for (List<Pair<Mapping, String>> item : suggestionLoL) {
                if (item != null) item.clear();
            }
            suggestionLoL.clear();

        }

        public Mapping getBestSuggestion() {
            return null;
        }

    }
    */

}