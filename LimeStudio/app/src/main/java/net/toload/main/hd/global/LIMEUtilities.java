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

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import net.toload.main.hd.LIMEService;
import net.toload.main.hd.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//

/**
 * @author jrywu
 *
 */
public class LIMEUtilities {
	static final String TAG = "LIMEUtilities";
	static final boolean DEBUG = false;

	public static boolean isUnicodeSurrogate(String word){  // emoji icons are within these surrogate areas
		if(word!=null && word.length()==2 ){
			char[] chArray = word.toCharArray();
			return Character.isSurrogatePair(chArray[0],chArray[1]);
		}
		return false;

	}

	/**
	 * Return the filepath if the file not exist in the target path
	 * @param filepath
	 * @return
	 */
	public static File isFileNotExist(String filepath){
		
		File mfile = new File(filepath);
		if(mfile.exists())
			return null;
		else
			return mfile;
	}
	
	public static File isFileExist(String filepath){
		
		File mfile = new File(filepath);
		if(mfile.exists())
			return mfile;
		else
			return null;
	}

	/*
*   Zip singl file into single zip.
*   sourceFile should be assigned with absolute path.
*
 */
	public static void zip(String zipFilePath, String sourceFile, Boolean OverWrite) 	throws Exception {
		zip(zipFilePath,sourceFile,"",OverWrite);
	}
	/*
	*   Zip singl file into single zip.
	*   sourceFile is specify relative to the baseFolderPath.
	*   sourceFile should be assigned with absolute path if baseFolderPath is null of empty
	*
 	*/
	public static void zip(String zipFilePath, String sourceFile, String baseFolderPath, Boolean OverWrite) 	throws Exception {
		List<String> sourceFileList = new ArrayList<>();
		sourceFileList.add(sourceFile);
		zip(zipFilePath,sourceFileList,baseFolderPath,OverWrite);
	}
	/*
	*   Zip multile files into single zip.
	*   sourceFiles should be assigned with absolute path.
	*
	 */
	public static void zip(String zipFilePath, List<String> sourceFiles, Boolean OverWrite) 	throws Exception {
		zip(zipFilePath, sourceFiles, "", OverWrite);
	}
	/*
	*   Zip multile files into single zip.
	*   sourceFiles is specify relative to the baseFolderPath.
	*   sourceFiles should be assigned with absolute path if baseFolderPath is null of empty
	*
	 */
	public static void zip(String zipFilePath, List<String> sourceFiles, String baseFolderPath, Boolean OverWrite) 	throws Exception {
		File zipFile = new File(zipFilePath);
		if( zipFile.exists() && !OverWrite) return;
		else if(zipFile.exists() && OverWrite) zipFile.delete();

		ZipOutputStream zos = null;
		FileOutputStream outStream = null;
		outStream = new FileOutputStream(zipFile);
		zos = new ZipOutputStream(outStream);

		if(baseFolderPath == null) baseFolderPath="";

		for(String item : sourceFiles) {
			String itemName = (item.startsWith(File.separator) || baseFolderPath.endsWith(File.separator) )?item:(File.separator + item) ;

			if(baseFolderPath.equals("")) //absolute path
				addFileToZip(baseFolderPath + itemName, zos);
			else  //relative path
				addFileToZip(baseFolderPath + itemName, baseFolderPath, zos);


		}
		zos.flush();
		zos.close();

	}

	private static void addFileToZip(String sourceFilePath, String baseFolderPath, ZipOutputStream zos) throws Exception {
		addFileToZip("", sourceFilePath, baseFolderPath, zos);
	}
	private static void addFileToZip( String sourceFilePath, ZipOutputStream zos) throws Exception {
		addFileToZip("", sourceFilePath, "", zos);
	}

	private static void addFileToZip(String sourceFolderPath, String sourceFilePath, String baseFolderPath, ZipOutputStream zos) throws Exception {

		File item = new File(sourceFilePath);
		if( item==null || !item.exists()) return; //skip if the file is not exist
		if (isSymLink(item)) return ; // do nothing to symbolic links.

		if(baseFolderPath == null) baseFolderPath = "";

		if (item.isDirectory()) {
			for (String subItem : item.list()) {
				addFileToZip(sourceFolderPath + File.separator + item.getName(), sourceFilePath + File.separator  + subItem, baseFolderPath, zos);
			}
		} else {
			byte[] buf = new byte[102400]; //100k buffer
			int len;
			FileInputStream inStream = new FileInputStream(sourceFilePath);
			if(baseFolderPath.equals(""))  //sourceFiles in absolute path, zip the file with absolute path
				zos.putNextEntry(new ZipEntry(sourceFilePath));
			else {//relative path
				String relativePath = sourceFilePath.substring(baseFolderPath.length() );
				zos.putNextEntry(new ZipEntry(relativePath));
			}

			while ((len = inStream.read(buf)) > 0) {
				zos.write(buf, 0, len);
			}

		}
	}

	public static boolean isSymLink(File filePath) throws IOException {
		if (filePath == null)
			throw new NullPointerException("filePath cannot be null");
		File canonical;
		if (filePath.getParent() == null) {
			canonical = filePath;
		} else {
			File canonDir = filePath.getParentFile().getCanonicalFile();
			canonical = new File(canonDir, filePath.getName());
		}
		return !canonical.getCanonicalFile().equals(canonical.getAbsoluteFile());
	}

	public static List<String> unzip(String zipFilePath, String targetFolder, Boolean OverWrite) throws IOException {
		return unzip(new File(zipFilePath), new File(targetFolder), OverWrite);
	}

	public static List<String> unzip(File zipFile, File targetDirectory, Boolean OverWrite) throws IOException {
		ZipInputStream zis = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(zipFile)));
		List<String> returnFilePaths = new ArrayList<>();
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[102400];
			while ((ze = zis.getNextEntry()) != null) {
				String itemName = ze.getName();
				File targetFile = null;

				if(itemName.startsWith("/sdcard/") || itemName.startsWith(String.valueOf(Environment.getExternalStorageDirectory())+File.separator)){
					targetFile = new File(ze.getName());  //target is zipped with absolute path on /sdcard
				}
				else if(itemName.startsWith("/data/") || itemName.startsWith(String.valueOf(Environment.getDataDirectory())+File.separator)){
					//target is zipped with absolute path on /data, we need to confirm the targetfolder is within our package.
					String packageRoot = LIME.getLimeDataRootFolder();
					if(!itemName.startsWith(packageRoot))  //skip if the target path is not under our package root
						continue;
					targetFile = new File(ze.getName());  //target is zipped with absolute path on /sdcard
				}
				else {
					targetFile = new File(targetDirectory, ze.getName());
				}

				File dir = ze.isDirectory() ? targetFile : targetFile.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException("Failed to ensure target directory: " +	dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				if(targetFile.exists() && OverWrite)
					targetFile.delete();
				FileOutputStream outStream = new FileOutputStream(targetFile);
				try {
					while ((count = zis.read(buffer)) != -1)
						outStream.write(buffer, 0, count);
				} catch (IOException e){
					e.printStackTrace();
				} finally {
					outStream.close();
				}

				returnFilePaths.add(targetFile.getAbsolutePath());
			}
		} finally {
			zis.close();
		}
		return returnFilePaths;
	}
	public static boolean copyFile(String sourceFilePath, String targetFilePath, Boolean overWrite) {
		File sourceFile = isFileExist(sourceFilePath);
		if(sourceFilePath == null || sourceFile == null || targetFilePath == null) return false;
		File targetFile = isFileExist(targetFilePath);
		if(targetFile!=null && !overWrite ) return false;
		if(targetFile == null) targetFile = new File(targetFilePath);
		try{
			FileInputStream inStream = new FileInputStream(sourceFile);
			FileOutputStream outSteram = new FileOutputStream(targetFile);
			copyRAWFile(inStream, outSteram);
			return true;
		}
		catch(Exception ignored){
			return false;
		}

	}
	public static void copyRAWFile(InputStream	inStream, File newfile){
		try{
			FileOutputStream fs = new FileOutputStream(newfile);
			copyRAWFile(inStream, fs);
			fs.close();
		}
		catch(Exception e){      
    		e.printStackTrace();   
           }   
	}
	public static void copyRAWFile(InputStream	inStream, FileOutputStream outStream){
	    	try{
	    		int bytesum=0, byteread=0;

	            byte[] buffer  = new byte[102400]; //100k buffer
	            while((byteread = inStream.read(buffer))!=-1){   
	               	   bytesum     +=     byteread;
	                   System.out.println(bytesum);
	                   outStream.write(buffer, 0, byteread);   
	             	}   
	            inStream.close();
				outStream.close();
	        	}   
	    	catch(Exception e){      
	    		e.printStackTrace();   
	           }   
	     }

	
	/** Add by Jeremy '12,4,23 Show notification with notification builder in compatibility package replacing the deprecated alert dialog creation 

	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showNotification(Context context, Boolean autoCancel,  CharSequence title, CharSequence message, Intent intent){

		//Intent resultIntent = new Intent(context, MainActivity.class);
		//PendingIntent pi = PendingIntent.getActivity(context, 0, resultIntent, 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
						.setLargeIcon(getNotificationIconBitmap(context))
						.setContentTitle(title)
						.setAutoCancel(autoCancel)
						.setTicker(message)
						.setContentText(message);

		boolean lollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		if(lollipop){
			mBuilder.setSmallIcon(R.drawable.logobw);
		}else{
			mBuilder.setSmallIcon(R.drawable.logo);
		}

		NotificationManager mNotificationManager =
						(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(501, mBuilder.build());
	}

	private static int getNotificationIcon() {
		boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		return whiteIcon ? R.drawable.logobw : R.drawable.logo;
	}

	private static Bitmap getNotificationIconBitmap(Context context) {
		boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		Bitmap bm = null;
		if(whiteIcon){
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
		}else{
			bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
		}
		return bm;
	}

	
	public static String isVoiceSearchServiceExist(Context context){
		if(DEBUG) Log.i(TAG, "isVoiceSearchServiceExist()");
		
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
	
		//boolean isVoiceSearchServiceEnabled = false;
		for (int i = 0; i < mInputMethodProperties.size(); i++) {
			InputMethodInfo imi = mInputMethodProperties.get(i);
			if(DEBUG) Log.i(TAG, "enabled IM " + i + ":" + imi.getId());

			if(imi.getId().equals("com.google.android.voicesearch/.ime.VoiceInputMethodService")){
				return "com.google.android.voicesearch/.ime.VoiceInputMethodService";
			}else if(imi.getId().equals("com.google.android.googlequicksearchbox/com.google.android.voicesearch.ime.VoiceInputMethodService")){
				return "com.google.android.googlequicksearchbox/com.google.android.voicesearch.ime.VoiceInputMethodService";
			}
		}
		return null;
		
	}
	
	public static boolean isLIMEEnabled(Context context){
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
		String limeID = getLIMEID(context);

		boolean isLIMEActive = false; 
		
		for (int i = 0; i < mInputMethodProperties.size(); i++) {
			InputMethodInfo imi = mInputMethodProperties.get(i);
			if(DEBUG) Log.i(TAG, "enabled IM " + i + ":" + imi.getId());
			if(imi.getId().equals(limeID)){
				isLIMEActive = true;
				break;
			}
		}
		return isLIMEActive;
	}

	public static boolean isLIMEActive(Context context){
		String activeIM = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD); 
		String limeID = getLIMEID(context);
		
		if(DEBUG) Log.i(TAG, "active IM:" + activeIM + " LIME IM:" + limeID);
		return activeIM.equals(limeID);
	}
	
	public static String getLIMEID(Context context){
		ComponentName LIMEComponentName = new ComponentName(context, LIMEService.class);
		return LIMEComponentName.flattenToShortString();
	}
	
	public static String getVoiceSearchIMId(Context context){
		ComponentName voiceInputComponent = 
				new ComponentName("com.google.android.voicesearch", "com.google.android.voicesearch.ime.VoceInputMethdServce");
		if(DEBUG)
			Log.i(TAG, "getVoiceSearchIMId(), Comonent name = " 
					+ voiceInputComponent.flattenToString() + ", id = " 
					+ voiceInputComponent.flattenToShortString());
		return voiceInputComponent.flattenToShortString();
	}
	
	public static void showInputMethodSettingsPage(Context context){
		Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   	 	context.startActivity(intent);
	}
	public static void showInputMethodPicker(Context context){
		((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}
	
	

}
