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

package net.toload.main.hd.global;

import android.os.Environment;

public class LIME {
	public static String PACKAGE_NAME;
	
	public static final String LIME_SDCARD_FOLDER = Environment.getExternalStorageDirectory() + "/limehd/";
	public static String getLimeDataRootFolder(){ return Environment.getDataDirectory() + "/data/"+LIME.PACKAGE_NAME; }
	public static final String DATABASE_RELATIVE_FOLDER = "/databases";
	public static final String DATABASE_NAME = "lime.db";
	public static final String DATABASE_JOURNAL = "lime.db-journal";
	public static final String DATABASE_DECOMPRESS_FOLDER_SDCARD = Environment.getExternalStorageDirectory() + "/limehd";
	public static final String DATABASE_BACKUP_NAME = "backup.zip";
	public static final String SHARED_PREFS_BACKUP_NAME=  "shared_prefs.bak";
	public final static int SEARCHSRV_RESET_CACHE_SIZE = 1024;
	// original size
	//public final static int SEARCHSRV_RESET_CACHE_SIZE = 256;
	public final static int LIMEDB_CACHE_SIZE = 1024;
}
