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
import android.os.Environment;
import android.os.RemoteException;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

	// Monitoring thread.
	//	private Thread thread = null;

	//	public class DBServiceImpl extends IDBService.Stub {
	//
	//		Context ctx = null;
	//		//private Thread thread = null;
	//
	//		DBServiceImpl(Context ctx) {
	//			this.ctx = ctx;
	//			mLIMEPref = new LIMEPreferenceManager(ctx);
	//			loadLimeDB();
	//		}
	public DBServer(Context context) {
		DBServer.ctx = context;
		mLIMEPref = new LIMEPreferenceManager(ctx);
		//loadLimeDB();
		if (datasource == null)
			datasource = new LimeDB(ctx);
	}
/* deprecated by jeremy '12,5,2
	public void loadLimeDB(){	
		if(datasource==null)
			datasource = new LimeDB(ctx); 
	}
*/

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
			//mLIMEPref.setResetCacheFlag(true);
			resetCache();
			return count;
		}
	}


	public int getLoadingMappingCount() {
		return datasource.getCount();
	}


	public static void backupDatabase() throws RemoteException {
		if (DEBUG)
			Log.i(TAG, "backupDatabase()");
		//showNotificationMessage(ctx.getText(R.string.l3_initial_backup_start) + "");

		File limedir = new File(LIME.LIME_SDCARD_FOLDER + File.separator);
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
			LIMEUtilities.zip(LIME.LIME_SDCARD_FOLDER+ LIME.DATABASE_BACKUP_NAME, backupFileList, LIME.getLimeDataRootFolder() , true);
		} catch (Exception e) {
			e.printStackTrace();
			showNotificationMessage(ctx.getText(R.string.l3_initial_backup_error) + "");
		} finally {
			showNotificationMessage(ctx.getText(R.string.l3_initial_backup_end) + "");
		}


		/*
		File dbFile=null, jounralFilel=null;
		String dbtarget = mLIMEPref.getParameterString("dbtarget");
		//try {
			if (dbtarget.equals("device")) {

				//LIMEUtilities.zipFolder(LIME.LIME_SDCARD_FOLDER + LIME.DATABASE_BACKUP_NAME, LIME.getLIMEDatabaseFolder(), true);
				dbFile = new File(LIME.getLIMEDatabaseFolder() + File.separator + LIME.DATABASE_NAME);
				jounralFilel = new File(LIME.getLIMEDatabaseFolder() + File.separator + LIME.DATABASE_JOURNAL);
			} else {
				//LIMEUtilities.zipFolder(LIME.LIME_SDCARD_FOLDER + LIME.DATABASE_BACKUP_NAME, LIME.DATABASE_DECOMPRESS_FOLDER_SDCARD, true);
				dbFile = new File(LIME.DATABASE_DECOMPRESS_FOLDER_SDCARD + File.separator + LIME.DATABASE_NAME);
				jounralFilel = new File(LIME.DATABASE_DECOMPRESS_FOLDER_SDCARD + File.separator + LIME.DATABASE_JOURNAL);
			}
		//} catch (Exception e) {			e.printStackTrace();		}
		compressFile(dbFile, LIME.LIME_SDCARD_FOLDER, LIME.DATABASE_BACKUP_NAME);
		compressFile(jounralFilel, LIME.LIME_SDCARD_FOLDER, LIME.DATABASE_JOURNAL_BACKUP_NAME);
		*/

		// backup finished.  unhold the database connection and false reopen the database.
		datasource.unHoldDBConnection(); //Jeremy '15,5,23
		//mLIMEPref.holdDatabaseConnection(false);
		datasource.openDBConnection(true);

		//cleanup the shared preference backup file.
		if( fileSharedPrefsBackup!=null && fileSharedPrefsBackup.exists() ) fileSharedPrefsBackup.delete();


	}

/*
	public static void restoreDatabase(File srcFile, Boolean removeSourceFile) throws RemoteException {

		mLIMEPref.holdDatabaseCoonection(true);
		closeDatabse();

		String dbtarget = mLIMEPref.getParameterString("dbtarget");
			if (dbtarget.equals("device")) {
				decompressFile(srcFile, LIME.getLIMEDatabaseFolder(), LIME.DATABASE_NAME, removeSourceFile);
			} else {decompressFile(srcFile, LIME.DATABASE_DECOMPRESS_FOLDER_SDCARD, LIME.DATABASE_NAME, removeSourceFile);
			}


		mLIMEPref.holdDatabaseCoonection(false);

		datasource.openDBConnection(true);
	}
*/
	public static void restoreDatabase() throws RemoteException {
		restoreDatabase(LIME.LIME_SDCARD_FOLDER + LIME.DATABASE_BACKUP_NAME);
	}
	public static void restoreDatabase(String srcFilePath) throws RemoteException {

		File check = new File(srcFilePath);

		if(check.exists()){

			//showNotificationMessage(ctx.getText(R.string.l3_initial_restore_start) + "");
			//mLIMEPref.holdDatabaseCoonection(true);
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


	public String getKeyboardCode(String im)
			throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		return datasource.getKeyboardCode(im);
	}




	/*
	 * Select Remote File to download
	 */
	public File downloadRemoteFile(String backup_url, String url, String folder, String filename){

		File olddbfile = new File(folder + filename);
		if(olddbfile.exists()){
			olddbfile.delete();
		}

		//mLIMEPref.setParameter("reload_database", true);
		abortDownload = false;
		remoteFileDownloading = true;
		File target = downloadRemoteFile(url, folder, filename);
		if(!target.exists() || target == null || target.length() == 0){
			target = downloadRemoteFile(backup_url, folder, filename);
		}
		remoteFileDownloading = false;
		return target;
	}

	/*
	 * Download Remote File
	 */
	public File downloadRemoteFile(String url, String folder, String filename){

		if(DEBUG)
			Log.i(TAG,"downloadRemoteFile() Starting....");

		try {
			URL downloadUrl = new URL(url);
			URLConnection conn = downloadUrl.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			long remoteFileSize = conn.getContentLength();
			long downloadedSize = 0;

			if(DEBUG)
				Log.i(TAG, "downloadRemoteFile() contentLength:");

			if(is == null){
				throw new RuntimeException("stream is null");
			}

			File downloadFolder = new File(folder);
			downloadFolder.mkdirs();

			//Log.i("ART","downloadFolder Folder status :"+ downloadFolder.exists());

			File downloadedFile = new File(downloadFolder.getAbsolutePath() + File.separator + filename);
			if(downloadedFile.exists()){
				downloadedFile.delete();
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(downloadedFile);
			// '04,12,27 Jeremy modified buf size from 128 to 128k and dramatically speed-up downloading speed on modern devices
			byte buf[] = new byte[128000];
			do{
				Thread.sleep(300);
				int numread = is.read(buf);
				downloadedSize += numread;

				if(DEBUG)
					Log.i(TAG, "downloadRemoteFile(), contentLength:"
							+ remoteFileSize+ ". downloadedSize:" + downloadedSize);

				if(numread <=0){break;}
				fos.write(buf, 0, numread);
			}while(!abortDownload);
			fos.close();
			is.close();

			return downloadedFile;

		} catch (FileNotFoundException e) {
			Log.d(TAG,"downloadRemoteFile(); can't open temp file on sdcard for writing.");
			showNotificationMessage(ctx.getText(R.string.l3_initial_download_write_sdcard_failed)+ "");
			e.printStackTrace();

		} catch (MalformedURLException e) {
			Log.d(TAG, "downloadRemoteFile() MalformedURLException....");
			showNotificationMessage(ctx.getText(R.string.l3_initial_download_failed)+ "");
			e.printStackTrace();
		} catch (IOException e){
			Log.d(TAG, "downloadRemoteFile() IOException....");
			showNotificationMessage(ctx.getText(R.string.l3_initial_download_failed)+ "");
			e.printStackTrace();
		} catch (Exception e){
			Log.d(TAG, "downloadRemoteFile() Others....");
			showNotificationMessage(ctx.getText(R.string.l3_initial_download_failed)+ "");
			e.printStackTrace();
		}
		if(DEBUG)
			Log.i(TAG, "downloadRemoteFile() failed.");
		return null;
	}

	/*
	 * Decompress retrieved file to target folder
	 */
	public static boolean decompressFile(File sourceFile, String targetFolder, String targetFile, boolean removeOriginal){
		if(DEBUG)
			Log.i(TAG, "decompressFile(), srouce = " + sourceFile.toString() + "" +	", target = " + targetFolder.toString()+ "/" + targetFile.toString());

		try {

			File targetFolderObj = new File(targetFolder);
			if(!targetFolderObj.exists()){
				targetFolderObj.mkdirs();
			}

			FileInputStream fis = new FileInputStream(sourceFile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			//ZipEntry entry; 
			while (( zis.getNextEntry()) != null) {

				int size;
				byte[] buffer = new byte[2048];

				File OutputFile = new File(targetFolderObj.getAbsolutePath() + File.separator + targetFile);
				OutputFile.delete();

				FileOutputStream fos = new FileOutputStream(OutputFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
				while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();

				//Log.i("ART","uncompress Output File:"+OutputFile.getAbsolutePath() + " / " + OutputFile.length());

			}
			zis.close();
			fis.close();

			if(removeOriginal) {
				sourceFile.delete();
			}
			return true;
		} catch (Exception e) {
			showNotificationMessage(ctx.getText(R.string.l3_initial_download_failed)+ "");
			e.printStackTrace();
		}
		return false;
	}

	public void compressFile(File sourceFile, String targetFolder, String targetFile){
		if(DEBUG)
			Log.i(TAG, "compressFile(), srouce = " + sourceFile.toString() + "" +
					", target = " + targetFolder.toString()+ "/" + targetFile.toString());
		try{
			final int BUFFER = 2048;

			File targetFolderObj = new File(targetFolder);
			if(!targetFolderObj.exists()){
				targetFolderObj.mkdirs();
			}


			File OutputFile = new File(targetFolderObj.getAbsolutePath() + File.separator + targetFile);
			OutputFile.delete();

			FileOutputStream dest = new FileOutputStream(OutputFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[BUFFER];

			FileInputStream fi = new  FileInputStream(sourceFile);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(sourceFile.getAbsolutePath());
			out.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();

			//Log.i("ART","compress Output File:"+OutputFile.getAbsolutePath() + " / " + OutputFile.length());

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Check the consistency of phonetic keyboard setting in preference and db.
	 * Jeremy '12,6,8
	 *
	 */
	public void checkPhoneticKeyboardSetting(){
		datasource.checkPhoneticKeyboardSetting();
	}


	public int getLoadingMappingPercentageDone() throws RemoteException {
		if(remoteFileDownloading) return 0;
		else return datasource.getProgressPercentageDone();
	}
/*
	@Deprecated //by Jeremy '12,6,6
	public void forceUpgrad() throws RemoteException {
		//if (datasource == null) {loadLimeDB();}
		datasource.forceUpgrade();
	}*/

	//	}


	//	public IBinder onBind(Intent arg0) {
	//		return new DBServiceImpl(this);
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see android.app.Service#onCreate()
	//	 */
	//	
	//	public void onCreate() {
	//		super.onCreate();
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see android.app.Service#onDestroy()
	//	 */
	//	
	//	public void onDestroy() {
	//		if (datasource != null) {
	//			datasource.close();
	//			datasource = null;
	//
	//		}
	//		notificationMgr.cancelAll();
	//		super.onDestroy();
	//	}

	//Jeremy '12,4,23 rewriting using alert notification builder in LIME utilities to replace the deprecated method
	public static void showNotificationMessage(String message) {

		Intent i = null;
		i = new Intent(ctx, MainActivity.class);

		LIMEUtilities.showNotification(
				ctx, true, ctx.getText(R.string.ime_setting), message, i);

	}

	public void renameTableName(String source, String target) {
		if(datasource != null){
			datasource.renameTableName(source, target);
		}
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
