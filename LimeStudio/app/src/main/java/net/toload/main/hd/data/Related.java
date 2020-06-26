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

public class Related {

	private int id;
	private String pword;
	private String cword;
	private int basescore;
	private int userscore;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPword() {
		return pword;
	}

	public void setPword(String pword) {
		this.pword = pword;
	}

	public String getCword() {
		return cword;
	}

	public void setCword(String cword) {
		this.cword = cword;
	}

	public int getBasescore() {
		return basescore;
	}

	public void setBasescore(int basescore) {
		this.basescore = basescore;
	}

	public int getUserscore() {return userscore;}

	public void setUserscore(int userscore) {this.userscore = userscore;}

	public static Related get(Cursor cursor){
		Related record = new Related();
				record.setId(cursor.getInt(cursor.getColumnIndex(Lime.DB_RELATED_COLUMN_ID)));
				record.setPword(cursor.getString(cursor.getColumnIndex(Lime.DB_RELATED_COLUMN_PWORD)));
				record.setCword(cursor.getString(cursor.getColumnIndex(Lime.DB_RELATED_COLUMN_CWORD)));
				record.setUserscore(cursor.getInt(cursor.getColumnIndex(Lime.DB_RELATED_COLUMN_USERSCORE)));
				record.setBasescore(cursor.getInt(cursor.getColumnIndex(Lime.DB_RELATED_COLUMN_BASESCORE)));
		return record;
	}

	public static List<Related> getList(Cursor cursor){
		List<Related> list = new ArrayList<Related>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			list.add(get(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public static String getInsertQuery(Related record){
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO " + Lime.DB_RELATED + "(");
		sb.append(Lime.DB_RELATED_COLUMN_PWORD +", ");
		sb.append(Lime.DB_RELATED_COLUMN_CWORD +", ");
		sb.append(Lime.DB_RELATED_COLUMN_USERSCORE +", ");
		sb.append(Lime.DB_RELATED_COLUMN_BASESCORE +") VALUES(");
		sb.append("\""+record.getPword()+"\",");
		sb.append("\""+record.getCword()+"\",");
		sb.append("\""+record.getUserscore()+"\",");
		sb.append("\""+record.getBasescore()+"\"");
		sb.append(")");
		return sb.toString();
	}

}
