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

import android.database.Cursor;

import net.toload.main.hd.Lime;

import java.util.ArrayList;
import java.util.List;

public class Word {

	private int id;
	private String code;
	private String code3r;
	private String word;
	private String related;
	private int score;
	private int basescore;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode3r() {
		return code3r;
	}

	public void setCode3r(String code3r) {
		this.code3r = code3r;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getRelated() {
		return related;
	}

	public void setRelated(String related) {
		this.related = related;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getBasescore() {
		return basescore;
	}

	public void setBasescore(int basescore) {
		this.basescore = basescore;
	}


	public static Word get(Cursor cursor){
		Word record = new Word();
			record.setId(cursor.getInt(cursor.getColumnIndex(Lime.DB_COLUMN_ID)));
			record.setCode(cursor.getString(cursor.getColumnIndex(Lime.DB_COLUMN_CODE)));
			//record.setCode3r(cursor.getString(cursor.getColumnIndex(Lime.DB_COLUMN_CODE3R)));  Jeremy '15,6,6 may not present in old db.
			record.setWord(cursor.getString(cursor.getColumnIndex(Lime.DB_COLUMN_WORD)));
			record.setRelated(cursor.getString(cursor.getColumnIndex(Lime.DB_COLUMN_RELATED)));
			record.setScore(cursor.getInt(cursor.getColumnIndex(Lime.DB_COLUMN_SCORE)));
			record.setBasescore(cursor.getInt(cursor.getColumnIndex(Lime.DB_COLUMN_BASESCORE)));
		return record;
	}
	
	public static List<Word> getList(Cursor cursor){
		List<Word> list = new ArrayList<Word>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			list.add(get(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public static String getInsertQuery(String table, Word record){
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO " + table + "(");
		sb.append(Lime.DB_COLUMN_CODE + ", ");
		if(table.equals("phonetic"))  sb.append(Lime.DB_COLUMN_CODE3R +", ");
		sb.append(Lime.DB_COLUMN_WORD +", ");
		sb.append(Lime.DB_COLUMN_RELATED +", ");
		sb.append(Lime.DB_COLUMN_SCORE +", ");
		sb.append(Lime.DB_COLUMN_BASESCORE +") VALUES(");
		sb.append("\""+Lime.formatSqlValue(record.getCode())+"\",");
		if(table.equals("phonetic")) sb.append("\""+Lime.formatSqlValue(record.getCode().replaceAll("[ 3467]", ""))+"\","); //Jeremy '15,6,6. remove 3467 tone keys from code as code3r
		sb.append("\""+Lime.formatSqlValue(record.getWord())+"\",");
		sb.append("\""+Lime.formatSqlValue(record.getRelated())+"\",");
		sb.append("\""+record.getScore()+"\",");
		sb.append("\""+record.getBasescore()+"\"");;
		sb.append(")");
		return sb.toString();
	}

	public static String getUpdateScoreQuery(String table, Word w){
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE " + table + " SET ");
		sb.append(Lime.DB_COLUMN_SCORE +"='");
		sb.append(w.getScore() +"', ");
		sb.append(Lime.DB_COLUMN_BASESCORE +"='");
		sb.append(w.getBasescore() +"' ");
		sb.append(" WHERE " + Lime.DB_COLUMN_ID + " ='");
		sb.append(w.getId() + "'");
		return sb.toString();
	}

}
