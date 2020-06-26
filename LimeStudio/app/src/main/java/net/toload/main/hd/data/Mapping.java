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

package net.toload.main.hd.data;

/**
 * @author Art Hung
 */
public class Mapping {

	private String id;
	private String code;
	private String codeorig;
	private String word;
	private String pword;
	//Jeremy '12,5,30 changed from string to boolean to indicate if it's from highLighted list or exact match result
	//Jeremy '15,6,4 renamed to highLighted.
	private Boolean highLighted =true;
	private int score;
	private int basescore;
	private int recordType;


	public static final int RECORD_COMPOSING_CODE = 1;
	public static final int RECORD_EXACT_MATCH_TO_CODE = 2;
	public static final int RECORD_PARTIAL_MATCH_TO_CODE = 3;
	public static final int RECORD_RELATED_PHRASE = 4;
	public static final int RECORD_ENGLISH_SUGGESTION = 5;
	public static final int RECORD_RUNTIME_BUILT_PHRASE = 6;
	public static final int RECORD_CHINESE_PUNCTUATION_SYMBOL = 7;
	public static final int RECORD_HAS_MORE_RECORDS_MARK = 8;
	public static final int RECORD_EXACT_MATCH_TO_WORD = 9;
	public static final int RECORD_PARTIAL_MATCH_TO_WORD = 10;
	public static final int RECORD_COMPLETION_SUGGESTION_WORD = 11;
	public static final int RECORD_EMOJI_WORD = 12;

	// empty constructor
	public Mapping(){}

	/**
	 *  constructor for clone mapping '12,6,5 Jeremy.
	 */
	public Mapping(Mapping mapping) {
		this.setId(mapping.id);
		this.setCode(mapping.code);
		this.setCodeorig(mapping.codeorig);
		this.setWord(mapping.word);
		this.setPword(mapping.pword);
		this.setScore(mapping.score);
		this.setBasescore(mapping.basescore);
		this.setHighLighted(mapping.isHighLighted());
		this.setRecordType(mapping.recordType);
	}

	public Boolean isHighLighted() {
		return highLighted;
	}
	public void setHighLighted(Boolean related) {
		this.highLighted = related;
	}

	public int getBasescore() { return basescore;}
	public void setBasescore(int score) {this.basescore=score;}


	/**
	 * @return previous word.  used in highLighted phrase
	 */
	public String getPword() {
		return pword;
	}
	/**
	 * @param pword the pword to set
	 */
	public void setPword(String pword) {
		this.pword = pword;
	}


	private void setRecordType(int recordType) {this.recordType = recordType;	}
	public int getRecordType(){ return  recordType;}

	public boolean isComposingCodeRecord() { return recordType == RECORD_COMPOSING_CODE; }
	public boolean isExactMatchToCodeRecord(){ return recordType == RECORD_EXACT_MATCH_TO_CODE;};
	public boolean isPartialMatchToCodeRecord(){ return recordType == RECORD_PARTIAL_MATCH_TO_CODE;};
	public boolean isRelatedPhraseRecord(){return recordType == RECORD_RELATED_PHRASE;	}
	public boolean isEnglishSuggestionRecord() { return recordType == RECORD_ENGLISH_SUGGESTION;	}
	public boolean isChinesePunctuationSymbolRecord(){ return recordType == RECORD_CHINESE_PUNCTUATION_SYMBOL;}
	public boolean isHasMoreRecordsMarkRecord(){ return recordType == RECORD_HAS_MORE_RECORDS_MARK; }
	public boolean isRuntimeBuiltPhraseRecord(){ return recordType == RECORD_RUNTIME_BUILT_PHRASE;	}
	public boolean isEmojiRecord(){ return recordType == RECORD_EMOJI_WORD; }

	// Identify exactly or partially match to the word queried ( reverse query codes by word)
	public boolean isExactMatchToWordRecord(){ return recordType == RECORD_EXACT_MATCH_TO_WORD;	}
	public boolean isPartialMatchToWordRecord(){return recordType == RECORD_PARTIAL_MATCH_TO_WORD;}
	public boolean isCompletionSuggestionRecord(){ return recordType == RECORD_COMPLETION_SUGGESTION_WORD;	}

	//Identify the record to be the current code typed by user and can be used to type English in mixed mode..
	public void setComposingCodeRecord() { 	this.recordType = RECORD_COMPOSING_CODE; }
	public void setExactMatchToCodeRecord() {this.recordType = RECORD_EXACT_MATCH_TO_CODE;}
	public void setPartialMatchToCodeRecord(){ this.recordType = RECORD_PARTIAL_MATCH_TO_CODE;	}
	public void setRelatedPhraseRecord()	{
		this.recordType = RECORD_RELATED_PHRASE;
	}
	public void setEnglishSuggestionRecord() {
		this.recordType = RECORD_ENGLISH_SUGGESTION;
	}
	public void setChinesePunctuationSymbolRecord(){	this.recordType = RECORD_CHINESE_PUNCTUATION_SYMBOL;}
	public void setHasMoreRecordsMarkRecord(){ this.recordType = RECORD_HAS_MORE_RECORDS_MARK; }
	public void setRuntimeBuiltPhraseRecord(){	this.recordType = RECORD_RUNTIME_BUILT_PHRASE;	}
	// Identify exactly or partially match to the word queried ( reverse query codes by word)
	public void setExactMatchToWordRecord() { this.recordType = RECORD_EXACT_MATCH_TO_WORD; }
	public void setPartialMatchToWordRecord(){
		this.recordType = RECORD_PARTIAL_MATCH_TO_WORD;
	}
	public void setCompletionSuggestionRecord(){ this.recordType = RECORD_COMPLETION_SUGGESTION_WORD;	}
	public void setEmojiRecord(){	this.recordType = RECORD_EMOJI_WORD;}



	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		if(code != null){
			return code.toLowerCase();
		}
		return null;
	}


	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public String getCodeorig() {
		return codeorig;
	}

	public void setCodeorig(String codeorig) {
		this.codeorig = codeorig;
	}

	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}
	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}
	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}


	
}
