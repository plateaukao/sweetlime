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

package net.toload.main.hd.limesettings;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;

import net.toload.main.hd.DBServer;
import info.plateaukao.sweetlime.R;
import net.toload.main.hd.SearchServer;
import net.toload.main.hd.data.KeyboardObj;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.skin.SkinManager;
import net.toload.main.hd.skin.SkinSettings;


public class LIMEPreferenceHC extends Activity {

	private SearchServer SearchSrv = null;

	@Override
	protected void onPause() {
		super.onPause();

		this.SearchSrv.initialCache();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.SearchSrv = new SearchServer(this);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new PrefsFragment()).commit();

	}

	public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{ 
		private final boolean DEBUG = false;
		private final String TAG = "LIMEPreferenceHC";
		private Context ctx = null;
		private DBServer DBSrv = null;
		private LIMEPreferenceManager mLIMEPref = null;
		private static final int REQUEST_IMPORT_CSKIN = 4301;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preference);

			if (ctx == null) {
				ctx = getActivity().getApplicationContext();
			}
			mLIMEPref = new LIMEPreferenceManager(ctx);
			DBSrv = new DBServer(ctx);

			Preference importCskin = findPreference("import_cskin");
			if (importCskin != null) {
				importCskin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						try {
							startActivityForResult(Intent.createChooser(intent,
									getString(R.string.import_cskin_title)), REQUEST_IMPORT_CSKIN);
						} catch (android.content.ActivityNotFoundException e) {
							Log.w(TAG, "No file picker available", e);
						}
						return true;
					}
				});
			}
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == REQUEST_IMPORT_CSKIN) {
				if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
					SkinSettings skin = null;
					try {
						InputStream in = ctx.getContentResolver().openInputStream(data.getData());
						if (in != null) {
							try {
								skin = SkinManager.getInstance().importSkin(ctx, in);
							} finally {
								in.close();
							}
						}
					} catch (Exception e) {
						Log.w(TAG, "cskin import failed", e);
					}
					if (skin != null) {
						// Activate the custom skin theme right away.
						getPreferenceScreen().getSharedPreferences().edit()
								.putString("keyboard_theme",
										Integer.toString(SkinManager.THEME_INDEX_CUSTOM))
								.commit();
						Toast.makeText(ctx, R.string.import_cskin_success, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(ctx, R.string.import_cskin_failed, Toast.LENGTH_LONG).show();
					}
				}
				return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public void onResume() {
			super.onResume();

			// Set up a listener whenever a key changes            
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();

			// Unregister the listener whenever a key changes            
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

	

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if(DEBUG) 
				Log.i(TAG,"onSharedPreferenceChanged(), key:" + key);

			if(key.equals("phonetic_keyboard_type")){
				String selectedPhoneticKeyboardType = mLIMEPref.getPhoneticKeyboardType();
				try {

					KeyboardObj kobj = DBSrv.getKeyboardObj("phonetic");
					
					if(selectedPhoneticKeyboardType.equals("standard")){
						kobj = 	DBSrv.getKeyboardObj("phonetic");
					}else if(selectedPhoneticKeyboardType.equals("eten")){
						kobj = 	DBSrv.getKeyboardObj("phoneticet41");
					}else if(selectedPhoneticKeyboardType.equals("eten26")){		
						if(mLIMEPref.getParameterBoolean("number_row_in_english", false)){ 
							kobj = 	DBSrv.getKeyboardObj("limenum");
						}else{
							kobj = 	DBSrv.getKeyboardObj("lime");
						}
					}else if(selectedPhoneticKeyboardType.equals("eten26_symbol")){
						kobj = 	DBSrv.getKeyboardObj("et26");
					}else if(selectedPhoneticKeyboardType.equals("hsu")){ //Jeremy '12,7,6 Add HSU english keyboard support
						if(mLIMEPref.getParameterBoolean("number_row_in_english", false)){ 
							kobj = 	DBSrv.getKeyboardObj("limenum");
						}else{
							kobj = 	DBSrv.getKeyboardObj("lime");
						}
					}else if(selectedPhoneticKeyboardType.equals("hsu_symbol")){
						kobj = 	DBSrv.getKeyboardObj("hsu");
					}
					DBSrv.setIMKeyboard("phonetic", kobj.getDescription(), kobj.getCode());
					if(DEBUG) Log.i(TAG, "onSharedPreferenceChanged() PhoneticIMInfo.kyeboard:" +
							DBSrv.getImInfo("phonetic", "keyboard"));	
				} catch (RemoteException e) {
					Log.i(TAG, "onSharedPreferenceChanged(), WriteIMinfo for selected phonetic keyboard failed!!");
					e.printStackTrace();
				}

			}
			BackupManager backupManager = new BackupManager(ctx);
			backupManager.dataChanged();  //Jeremy '12,4,29 call backup manager to backup the changes.


		}
	}
}