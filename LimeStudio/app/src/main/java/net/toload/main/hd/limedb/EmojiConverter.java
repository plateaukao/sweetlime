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


package net.toload.main.hd.limedb;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.util.Log;

import net.toload.main.hd.Lime;
import info.plateaukao.sweetlime.R;
import net.toload.main.hd.data.Mapping;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Art Hung
 */
public class EmojiConverter extends SQLiteOpenHelper {

	private final static boolean DEBUG = false;
	private final static String TAG = "EmojiConverter";


	private final static String DATABASE_NAME = "emoji.db";
	private final static int DATABASE_VERSION = 1;


	private static final String FIELD_TAG = "tag";
	private static final String FIELD_VALUE = "value";


	public EmojiConverter(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Create SQLite Database and create related tables
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	/**
	 * Upgrade current database
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/** One distinct emoji from the database, with the id of its first keyword row. */
	public static class EmojiItem {
		public final String value;
		public final int firstId;

		public EmojiItem(String value, int firstId) {
			this.value = value;
			this.firstId = firstId;
		}
	}

	/**
	 * All distinct emoji in database order. The en table follows Unicode block
	 * order, so consecutive firstId ranges form the emoji picker categories.
	 */
	public List<EmojiItem> getAllEmoji() {
		List<EmojiItem> output = new LinkedList<>();
		Cursor cursor = null;
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			cursor = db.rawQuery("SELECT " + FIELD_VALUE + ", MIN(id) FROM en GROUP BY "
					+ FIELD_VALUE + " ORDER BY MIN(id)", null);
			while (cursor.moveToNext()) {
				String word = cursor.getString(0);
				if (word != null && !word.isEmpty() && !word.equals(" "))
					output.add(new EmojiItem(word, cursor.getInt(1)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) cursor.close();
		}
		return output;
	}

	public List<Mapping> convert(String tag, Integer emoji){

		List<Mapping> output = new LinkedList<Mapping>();

		if(tag!=null && !tag.equals("")){
			String tablename = "";
			Cursor cursor = null;
			if(emoji == Lime.EMOJI_CN ) {
				tablename = "cn";
			}else if(emoji == Lime.EMOJI_EN ) {//
				tablename = "en";
			}else if(emoji == Lime.EMOJI_TW ) {//
				tablename = "tw";
			}

			try {
				SQLiteDatabase db = this.getReadableDatabase();

				cursor = db.query(tablename, null, Lime.EMOJI_FIELD_TAG + " = '" + tag + "' "
							, null, null, null, null, null);
				
				if (cursor.moveToFirst()) {
					int wordColumn = cursor.getColumnIndex(Lime.EMOJI_FIELD_VALUE);
					while (!cursor.isAfterLast()) {
						String word = cursor.getString(wordColumn);
						if(word != null && !word.isEmpty() && !word.equals(" ")){
							Mapping mapping = new Mapping();
									mapping.setCode("");
									mapping.setWord(word);
									mapping.setEmojiRecord();

							output.add(mapping);

						}
						cursor.moveToNext();
					}
				}
					
				if (cursor != null) {cursor.close();}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return output;
	}
}
