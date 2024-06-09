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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import android.util.Log;

import net.toload.main.hd.data.KeyboardObj;
import net.toload.main.hd.data.Word;
import net.toload.main.hd.global.LIME;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.global.LIMEProgressListener;
import net.toload.main.hd.global.LIMEUtilities;
import net.toload.main.hd.limedb.LimeDB;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import info.plateaukao.sweetlime.R;

//Jeremy '12,5,1 renamed from DBServer and change from service to ordinary class.
public class  DBServer {
	private static final boolean DEBUG = false;
	private static final String TAG = "LIME.DBServer";
	//private NotificationManager notificationMgr;

	protected static LimeDB datasource = null;  //static LIMEDB for shared LIMEDB between DBServer instances
	protected static LIMEPreferenceManager mLIMEPref = null;

	private static boolean remoteFileDownloading = false;

	private static String loadingTablename = "";  //Jeremy '12,6,2 change all variable to static for all instances to share these variables
	private static boolean abortDownload = false;


	protected static Context ctx = null;

	public DBServer(Context context) {
		DBServer.ctx = context;
		mLIMEPref = new LIMEPreferenceManager(ctx);
		//loadLimeDB();
		if (datasource == null)
			datasource = new LimeDB(ctx);
	}

	public void loadMapping(String filename, String tablename, LIMEProgressListener progressListener) throws RemoteException {

		File sourcefile = new File(filename);
		loadMapping(sourcefile, tablename, progressListener);
	}

	public void loadMapping(File sourcefile, String tablename, LIMEProgressListener progressListener) throws RemoteException {
		if (DEBUG)
			Log.i(TAG, "loadMapping() on " + loadingTablename);


		loadingTablename = tablename;


		datasource.setFinish(false);
		datasource.setFilename(sourcefile);

		//showNotificationMessage(ctx.getText(R.string.lime_setting_notification_loading) + "");
		datasource.loadFileV2(tablename, progressListener);
		//datasource.close();

		// Reset for SearchSrv
		//mLIMEPref.setResetCacheFlag(true);
		resetCache();
	}

	public void resetMapping(final String tablename) throws RemoteException {

		if (DEBUG)
			Log.i(TAG, "resetMapping() on " + loadingTablename);

		datasource.deleteAll(tablename);

		// Reset cache in SearchSrv
		//mLIMEPref.setResetCacheFlag(true);
		resetCache();
	}
	public static void resetCache(){
		SearchServer.resetCache(true);
	}

	public boolean importBackupRelatedDb(File sourcedb){
		return datasource.importBackupRelatedDb(sourcedb);
	}

	public boolean importBackupDb(File sourcedb, String imtype) {
		return datasource.importBackupDb(sourcedb, imtype);
	}

	public int importMapping(File compressedSourceDB, String imtype) {

		List<Word> results = null;

		//String sourcedbfile = LIME.LIME_SDCARD_FOLDER + imtype;
		List<String> unzipFilePaths = new ArrayList<>();
		try {
			unzipFilePaths = LIMEUtilities.unzip(compressedSourceDB.getAbsolutePath(), ctx.getCacheDir().getAbsolutePath()+"limehd",true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//decompressFile(compressedSourceDB, LIME.LIME_SDCARD_FOLDER, imtype, true);
		if(unzipFilePaths.size()!=1){
			//TODO: Process exception here.
			return -1;
		}
		else {
			int count = datasource.importDb(unzipFilePaths.get(0), imtype);
			resetCache();
			return count;
		}
	}

	public static void backupDatabase(Uri uri) throws RemoteException {
		if (DEBUG) Log.i(TAG, "backupDatabase()");

		File limedir = ContextCompat.getExternalFilesDirs(ctx, null)[0];

		if (!limedir.exists()) {
			limedir.mkdirs();
		}

		//backup shared preferences
		File fileSharedPrefsBackup = new File(LIME.getLimeDataRootFolder(), LIME.SHARED_PREFS_BACKUP_NAME);
		if(fileSharedPrefsBackup.exists())  fileSharedPrefsBackup.delete();
		backupDefaultSharedPreference(fileSharedPrefsBackup);

		// create backup file list.
		List<String> backupFileList = new ArrayList<>();
		backupFileList.add(LIME.DATABASE_RELATIVE_FOLDER + File.separator + LIME.DATABASE_NAME);
		backupFileList.add(LIME.DATABASE_RELATIVE_FOLDER + File.separator + LIME.DATABASE_JOURNAL);
		backupFileList.add(LIME.SHARED_PREFS_BACKUP_NAME);

		// hold database connection and close database.
		//mLIMEPref.holdDatabaseCoonection(true);
		datasource.holdDBConnection(); //Jeremy '15,5,23
		closeDatabse();

		//ready to zip backup file list
		try {
			LIMEUtilities.zip(
					limedir.getAbsolutePath() + File.separator + LIME.DATABASE_BACKUP_NAME, backupFileList,
					LIME.getLimeDataRootFolder(),
					true
			);

			DocumentFile pickedDir = DocumentFile.fromTreeUri(ctx, uri);
			DocumentFile backupFile = pickedDir.createFile("application/zip", "backup.zip");
			File file = new File(limedir, LIME.DATABASE_BACKUP_NAME);
			FileInputStream fileInputStream = new FileInputStream(file);
			OutputStream fileOutputStream = ctx.getContentResolver().openOutputStream(backupFile.getUri());
			copyFile(fileInputStream, fileOutputStream);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
			showNotificationMessage(ctx.getText(R.string.l3_initial_backup_error) + "");
		} finally {
			showNotificationMessage(ctx.getText(R.string.l3_initial_backup_end) + "");
		}

		// backup finished.  unhold the database connection and false reopen the database.
		datasource.unHoldDBConnection(); //Jeremy '15,5,23
		//mLIMEPref.holdDatabaseConnection(false);
		datasource.openDBConnection(true);

		//cleanup the shared preference backup file.
		if( fileSharedPrefsBackup!=null && fileSharedPrefsBackup.exists() ) fileSharedPrefsBackup.delete();
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}


	public static void restoreDatabase(String srcFilePath) throws RemoteException {
		File check = new File(srcFilePath);

		if(check.exists()){
			datasource.holdDBConnection(); //Jeremy '15,5,23
			closeDatabse();
			try {
				LIMEUtilities.unzip(srcFilePath, LIME.getLimeDataRootFolder(), true);
			} catch (Exception e) {
				e.printStackTrace();
				showNotificationMessage(ctx.getText(R.string.l3_initial_restore_error) + "");
			}
			finally {
				showNotificationMessage(ctx.getText(R.string.l3_initial_restore_end) + "");
			}

			datasource.unHoldDBConnection(); //Jeremy '15,5,23
			datasource.openDBConnection(true);

			//restore shared preference
			File checkpref = new File(LIME.getLimeDataRootFolder(), LIME.SHARED_PREFS_BACKUP_NAME);
			if(checkpref.exists()){
				restoreDefaultSharedPreference(checkpref);
			}

			//mLIMEPref.setResetCacheFlag(true);
			resetCache();

			// Check and upgrade the database table
			datasource.checkAndUpdateRelatedTable();

		}else{
			showNotificationMessage(ctx.getText(R.string.error_restore_not_found) + "");
		}
	}

	public static void backupDefaultSharedPreference(File sharePrefs) {

		if(sharePrefs.exists()) sharePrefs.delete();

		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(sharePrefs));
			SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE);
			output.writeObject(pref.getAll());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}


	public static void restoreDefaultSharedPreference(File sharePrefs )
	{
		ObjectInputStream inputStream = null;

		try {
			inputStream = new ObjectInputStream(new FileInputStream(sharePrefs));
			SharedPreferences.Editor prefEdit = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", Context.MODE_PRIVATE).edit();
			prefEdit.clear();
			Map<String, ?> entries = (Map<String, ?>) inputStream.readObject();
			for (Map.Entry<String, ?> entry : entries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					prefEdit.putBoolean(key, (Boolean) v);
				else if (v instanceof Float)
					prefEdit.putFloat(key, (Float) v);
				else if (v instanceof Integer)
					prefEdit.putInt(key, (Integer) v);
				else if (v instanceof Long)
					prefEdit.putLong(key, (Long) v);
				else if (v instanceof String) {
					if(!v.equals("PAYMENT_FLAG"))
						prefEdit.putString(key, ((String) v));
				}
			}
			prefEdit.commit();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String getImInfo(String im, String field) throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		return datasource.getImInfo(im, field);
	}


	public String getKeyboardInfo(String keyboardCode, String field) throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		return datasource.getKeyboardInfo(keyboardCode, field);
	}


	public void removeImInfo(String im, String field)
			throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		datasource.removeImInfo(im, field);

	}


	public void resetImInfo(String im) throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		datasource.resetImInfo(im);

	}


	public void setImInfo(String im, String field, String value)
			throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		datasource.setImInfo(im, field, value);

	}




	public static void closeDatabse() throws RemoteException {
		Log.i(TAG,"closeDatabase()");
		if (datasource != null) {
			datasource.close();
		}
	}


	public void setIMKeyboard(String im, String value,
							  String keyboard) throws RemoteException {

		datasource.setIMKeyboard(im, value, keyboard);
	}


	//Jeremy '12,4,23 rewriting using alert notification builder in LIME utilities to replace the deprecated method
	public static void showNotificationMessage(String message) {

		Intent i = null;
		i = new Intent(ctx, MainActivity.class);

		LIMEUtilities.showNotification(
				ctx, true, ctx.getText(R.string.ime_setting), message, i);

	}

	/**
	 * Jeremy '12,7,6 get keyboard object from table name
	 * @param table
	 * @return
	 */
	public KeyboardObj getKeyboardObj(String table){
		KeyboardObj kobj = null;
		if(datasource != null)
			kobj = datasource.getKeyboardObj(table);
		return kobj;
	}

	public boolean isDatabseOnHold() {
		return datasource.isDatabseOnHold();
	}


}
