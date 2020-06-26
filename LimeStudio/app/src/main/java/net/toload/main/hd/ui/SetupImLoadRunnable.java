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

package net.toload.main.hd.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import net.toload.main.hd.DBServer;
import net.toload.main.hd.Lime;
import net.toload.main.hd.R;
import net.toload.main.hd.data.KeyboardObj;
import net.toload.main.hd.data.Word;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.limedb.LimeDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class SetupImLoadRunnable implements Runnable{
    private final static boolean DEBUG = false;
    private final static String TAG = "SetupImLoadRunnable";

    // Global
    private String url = null;
    private String imtype = null;
    private String type = null;

    private Activity activity;
    private DBServer dbsrv = null;
    private SetupImHandler handler;

    private LimeDB datasource;
    private LIMEPreferenceManager mLIMEPref;

    private Context mContext;
    private boolean restorePreference;

    public SetupImLoadRunnable(Activity activity, SetupImHandler handler, String imtype, String type, String url, boolean restorePreference) {
        this.handler = handler;
        this.imtype = imtype;
        this.type = type;
        this.url = url;
        this.activity = activity;
        this.dbsrv = new DBServer(activity);
        this.datasource = new LimeDB(activity);
        this.mLIMEPref = new LIMEPreferenceManager(activity);
        this.restorePreference = restorePreference;
        this.mContext = activity.getBaseContext();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void run() {

        Looper.prepare();

        //Log.i("LIME", "showProgress Runnable:");
        handler.showProgress(false, activity.getResources().getString(R.string.setup_load_download));

        // Download DB File
        //handler.updateProgress(activity.getResources().getString(R.string.setup_load_download));
        File tempfile = downloadRemoteFile(mContext, url);

        if(tempfile == null || tempfile.length() < 100000){

            switch (type) {
                case Lime.IM_ARRAY:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_ARRAY;
                    break;
                case Lime.IM_ARRAY10:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_ARRAY10;
                    break;
                case Lime.IM_CJ_BIG5:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_CJ_BIG5;
                    break;
                case Lime.IM_CJ:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_CJ;
                    break;
                case Lime.IM_CJHK:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_CJHK;
                    break;
                case Lime.IM_CJ5:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_CJ5;
                    break;
                case Lime.IM_DAYI:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_DAYI;
                    break;
                case Lime.IM_DAYIUNI:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_DAYIUNI;
                    break;
                case Lime.IM_DAYIUNIP:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_DAYIUNIP;
                    break;
                case Lime.IM_ECJ:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_ECJ;
                    break;
                case Lime.IM_ECJHK:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_ECJHK;
                    break;
                case Lime.IM_EZ:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_EZ;
                    break;
                case Lime.IM_PHONETIC_BIG5:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PHONETIC_BIG5;
                    break;
                case Lime.IM_PHONETIC_ADV_BIG5:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PHONETICCOMPLETE_BIG5;
                    break;
                case Lime.IM_PHONETIC:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PHONETIC;
                    break;
                case Lime.IM_PHONETIC_ADV:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PHONETICCOMPLETE;
                    break;
                case Lime.IM_PINYIN:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PINYIN;
                    break;
                case Lime.IM_PINYINGB:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_PINYINGB;
                    break;
                case Lime.IM_SCJ:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_SCJ;
                    break;
                case Lime.IM_WB:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_WB;
                    break;
                case Lime.IM_HS:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_HS;
                    break;
                case Lime.IM_HS_V1:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_HS_V1;
                    break;
                case Lime.IM_HS_V2:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_HS_V2;
                    break;
                case Lime.IM_HS_V3:
                    url = Lime.DATABASE_OPENFOUNDRY_IM_HS_V3;
                    break;
            }
            tempfile = downloadRemoteFile(mContext, url);
        }


        // Load DB
        handler.updateProgress(activity.getResources().getString(R.string.setup_load_migrate_load));
        dbsrv.importMapping(tempfile, imtype);

        mLIMEPref.setParameter("_table", "");
        //mLIMEPref.setResetCacheFlag(true);
        DBServer.resetCache();

        if(restorePreference){
            handler.updateProgress(activity.getResources().getString(R.string.setup_im_restore_learning_data));
            handler.updateProgress(0);
            boolean check = datasource.checkBackuptable(imtype);
            handler.updateProgress(5);

            if(check){

                String backupTableName = imtype + "_user";

                // check if user data backup table is present and have valid records
                int userRecordsCount = datasource.countMapping(backupTableName);
                handler.updateProgress(10);
                if (userRecordsCount == 0) return;

                try {
                    // Load backuptable records
                                /*
                                Cursor cursorsource = datasource.rawQuery("select * from " + imtype);
                                List<Word> clist = Word.getList(cursorsource);
                                cursorsource.close();

                                HashMap<String, Word> wordcheck = new HashMap<String, Word>();
                                for(Word w : clist){
                                    String key = w.getCode() + w.getWord();
                                    wordcheck.put(key, w);
                                }
                                handler.updateProgress(20);
                                */
                    Cursor cursorbackup = datasource.rawQuery("select * from " + backupTableName);
                    List<Word> backuplist = Word.getList(cursorbackup);
                    cursorbackup.close();

                    int progressvalue = 0;
                    int recordcount = 0;
                    int recordtotal = backuplist.size();

                    for(Word w: backuplist){

                        recordcount++;

                        datasource.addOrUpdateMappingRecord(imtype,w.getCode(),w.getWord(),w.getScore());
                                    /*
                                    // update record
                                    String key = w.getCode() + w.getWord();

                                    if(wordcheck.containsKey(key)){
                                        try{
                                            datasource.execSQL("update " + imtype + " set " + Lime.DB_COLUMN_SCORE + " = " + w.getScore()
                                                            + " WHERE " + Lime.DB_COLUMN_CODE + " = '" + w.getCode() + "'"
                                                            + " AND " + Lime.DB_COLUMN_WORD + " = '" + w.getWord() + "'"
                                            );
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                        try{
                                            Word temp = wordcheck.get(key);
                                            String insertsql = Word.getInsertQuery(imtype, temp);
                                            datasource.execSQL(insertsql);
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                    */
                        // Update Progress
                        int progress =(int) ((double)recordcount / recordtotal   * 90 +10 ) ;

                        if(progress != progressvalue){
                            progressvalue = progress;
                            handler.updateProgress(progressvalue);
                        }

                    }

                    //   wordcheck.clear();

                }catch(Exception e){
                    e.printStackTrace();
                }

               // datasource.restoreUserRecordsStep2(imtype);
                handler.updateProgress(100);
            }
        }

        handler.finishLoading(imtype);
        handler.initialImButtons();

    }

    public int migrateDb(File tempfile, String imtype){

        List<Word> results = null;

        String sourcedbfile = Lime.DATABASE_FOLDER_EXTERNAL + imtype;

        handler.updateProgress(activity.getResources().getString(R.string.setup_load_migrate_load));
        DBServer.decompressFile(tempfile, Lime.DATABASE_FOLDER_EXTERNAL, imtype, true);
        SQLiteDatabase sourcedb = SQLiteDatabase.openDatabase(sourcedbfile, null, //SQLiteDatabase.OPEN_READWRITE |   //redundant
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        results = loadWord(sourcedb, imtype);
        sourcedb.close();


        // Remove Imtype and related info
        try {
            dbsrv.resetMapping(imtype);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int total = results.size();
        int c = 0;

        //datasource.open();
        datasource.beginTransaction();

        for(Word w: results){
            c++;
            String insert = Word.getInsertQuery(imtype, w);
            datasource.add(insert);
            if(c % 100 == 0){
                int p = (c * 100 / total);
                handler.updateProgress(activity.getResources().getString(R.string.setup_load_migrate_import) + " " + p + "%");
            }
        }
        datasource.endTransaction();
        return results.size();

        //datasource.close();
        /*
        try {

        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        //return 0;
    }

    public List<Word> loadWord(SQLiteDatabase sourcedb, String code) {
        List<Word> result = new ArrayList<Word>();
        if(sourcedb != null && sourcedb.isOpen()){
            Cursor cursor;
            String order = Lime.DB_COLUMN_CODE + " ASC";

            cursor = sourcedb.query(code, null, null, null, null, null, order);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Word r = Word.get(cursor);
                result.add(r);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return result;
    }

    @Deprecated public synchronized void setImInfo(String im, String field, String value) {

        ContentValues cv = new ContentValues();
        cv.put("code", im);
        cv.put("title", field);
        cv.put("desc", value);

        removeImInfo(im, field);

        datasource.insert("im", cv);
        /*try {
            datasource.open();
            datasource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

    private void setIMKeyboardOnDB(String im, String value, String keyboard) {

        ContentValues cv = new ContentValues();
        cv.put("code", im);
        cv.put("title", "keyboard");
        cv.put("desc", value);
        cv.put("keyboard", keyboard);

        removeImInfoOnDB(im, "keyboard");

        datasource.insert("im", cv);

    }

    private void removeImInfoOnDB(String im, String field) {
        String removeString = "DELETE FROM im WHERE code='"+im+"' AND title='"+field+"'";
        datasource.remove(removeString);

    }


    @Deprecated public synchronized void setIMKeyboard(String im, String value,  String keyboard) {
        try{
            setIMKeyboardOnDB(im, value, keyboard);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void removeImInfo(String im, String field) {
        try{
            removeImInfoOnDB(im, field);
        }catch(Exception e){e.printStackTrace();}
    }


    @Deprecated public KeyboardObj getKeyboardObj(String keyboard){

        if( keyboard == null || keyboard.equals(""))
            return null;
        KeyboardObj kobj=null;

        if(!keyboard.equals("wb") && !keyboard.equals("hs")){
            try {
                //SQLiteDatabase db = this.getSqliteDb(true);


               // datasource.open();
                Cursor cursor = datasource.query("keyboard", "code" +" = '"+keyboard+"'");
                if (cursor.moveToFirst()) {
                    kobj = new KeyboardObj();
                    kobj.setCode(cursor.getString(cursor.getColumnIndex("code")));
                    kobj.setName(cursor.getString(cursor.getColumnIndex("name")));
                    kobj.setDescription(cursor.getString(cursor.getColumnIndex("desc")));
                    kobj.setType(cursor.getString(cursor.getColumnIndex("type")));
                    kobj.setImage(cursor.getString(cursor.getColumnIndex("image")));
                    kobj.setImkb(cursor.getString(cursor.getColumnIndex("imkb")));
                    kobj.setImshiftkb(cursor.getString(cursor.getColumnIndex("imshiftkb")));
                    kobj.setEngkb(cursor.getString(cursor.getColumnIndex("engkb")));
                    kobj.setEngshiftkb(cursor.getString(cursor.getColumnIndex("engshiftkb")));
                    kobj.setSymbolkb(cursor.getString(cursor.getColumnIndex("symbolkb")));
                    kobj.setSymbolshiftkb(cursor.getString(cursor.getColumnIndex("symbolshiftkb")));
                    kobj.setDefaultkb(cursor.getString(cursor.getColumnIndex("defaultkb")));
                    kobj.setDefaultshiftkb(cursor.getString(cursor.getColumnIndex("defaultshiftkb")));
                    kobj.setExtendedkb(cursor.getString(cursor.getColumnIndex("extendedkb")));
                    kobj.setExtendedshiftkb(cursor.getString(cursor.getColumnIndex("extendedshiftkb")));
                }
                cursor.close();
                //datasource.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(keyboard.equals("wb")){
            kobj = new KeyboardObj();
            kobj.setCode("wb");
            kobj.setName("筆順五碼");
            kobj.setDescription("筆順五碼輸入法鍵盤");
            kobj.setType("phone");
            kobj.setImage("wb_keyboard_preview");
            kobj.setImkb("lime_wb");
            kobj.setImshiftkb("lime_wb");
            kobj.setEngkb("lime_abc");
            kobj.setEngshiftkb("lime_abc_shift");
         }else if(keyboard.equals("hs")){
            kobj = new KeyboardObj();
            kobj.setCode("hs");
            kobj.setName("華象直覺");
            kobj.setDescription("華象直覺輸入法鍵盤");
            kobj.setType("phone");
            kobj.setImage("hs_keyboard_preview");
            kobj.setImkb("lime_hs");
            kobj.setImshiftkb("lime_hs_shift");
            kobj.setEngkb("lime_abc");
            kobj.setEngshiftkb("lime_abc_shift");
         }

        return kobj;
    }


    /*
	 * Download Remote File
	 */
    public File downloadRemoteFile(Context ctx, String url){

        try {
            URL downloadUrl = new URL(url);
            URLConnection conn = downloadUrl.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();

            int size = conn.getContentLength();
            int downloadSize = 0;

            //long remoteFileSize = conn.getContentLength();
            //long downloadedSize = 0;

            if(is == null){
                throw new RuntimeException("stream is null");
            }

            //File downloadFolder = new File(Lime.DATABASE_FOLDER_EXTERNAL);
            //downloadFolder.mkdirs();
            //File downloadedFile = new File(downloadFolder.getAbsolutePath() + File.separator + Lime.DATABASE_IM_TEMP);

            File downloadFolder = ctx.getCacheDir();
            File downloadedFile = File.createTempFile(Lime.DATABASE_IM_TEMP, Lime.DATABASE_IM_TEMP_EXT, downloadFolder);
            downloadedFile.deleteOnExit();

            FileOutputStream fos;
            fos = new FileOutputStream(downloadedFile);


            byte buf[] = new byte[4096];
            do{
                int numread = is.read(buf);
                if(numread <=0){break;}
                fos.write(buf, 0, numread);
                if(size > 0){
                    downloadSize += 4096;
                    float percent = (float)downloadSize / (float)size;
                            percent *= 100;
                    handler.updateProgress((int)percent);
                }
                if(DEBUG)
                    Log.i(TAG, numread +  "bytes download.");

            }while(true);

            is.close();

            return downloadedFile;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
