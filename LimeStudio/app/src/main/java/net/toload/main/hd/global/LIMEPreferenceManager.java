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


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import net.toload.main.hd.Lime;
import net.toload.main.hd.data.Im;

import java.util.HashMap;
import java.util.List;

public class LIMEPreferenceManager {
	
	private Context ctx; 
	
	public LIMEPreferenceManager(Context context){		
		this.ctx = context;
		
	}
	
	public String getTableTotalRecords(String table){
		table = preProcessTableName(table);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String records = sp.getString(table + "total_record", "");
		if(records.equals("")){
			SharedPreferences ssp = ctx.getSharedPreferences(table + "total_record", 0);
			records = ssp.getString(table + "total_record", "");
			if(!records.equals("")) setTableTotalRecords(table, records);
		}
		return records;
	}
	public void setTableTotalRecords(String table, String records){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "total_record", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString(table + "total_record", records).commit();	
	}
	
	
	
	public String getTableVersion(String table){
		table = preProcessTableName(table);
		
		SharedPreferences sdp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String version = sdp.getString(table + "mapping_version", "");
		// retain mapping_version saved in shared Preference and saved to default reference
		if(version.equals("")){
			SharedPreferences ssp = ctx.getSharedPreferences(table + "mapping_version", 0);
			version = ssp.getString(table + "mapping_version", "");
			if(!version.equals("")) setTableVersion(table, version);
		}
		return version;
	}
	public void setTableVersion(String table, String version){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "mapping_version", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString(table + "mapping_version", version).commit();	
	}
	
	public String getTableMappingFilename(String table){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "mapping_file", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString(table + "mapping_file", "");
	}
	
	public void setTableMappingFilename(String table, String filename){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "mapping_file", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString(table + "mapping_file", filename).commit();	
	}
	
	public String getTableMappingTempFilename(String table){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "mapping_file_temp", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString(table + "mapping_file_temp", "");
	}
	
	public void setTableTempMappingFilename(String table, String filename){
		table = preProcessTableName(table);
		//SharedPreferences sp = ctx.getSharedPreferences(table + "mapping_file_temp", 0);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString(table + "mapping_file_temp", filename).commit();	
	}
	
	
	public String getTotalUserdictRecords(){

		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String records = sp.getString("total_userdict_record", "0");
		if(records.equals("0") ){
			SharedPreferences ssp = ctx.getSharedPreferences("total_userdict_record", 0);
			records = ssp.getString("total_userdict_record", "0");
			if(records.equals("0")) setTotalUserdictRecords(String.valueOf(records));
		}
		return records;
			
	}
	public void setTotalUserdictRecords(String records){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString("total_userdict_record", records).commit();	
	}
	@Deprecated
	public boolean getDatabaseOnHold(){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("mapping_loadg", "no").equals("yes");
	}
	@Deprecated
	public void holdDatabaseCoonection(boolean loading){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String loadingStatus = loading?"yes":"no";	
		sp.edit().putString("mapping_loadg",loadingStatus).commit();
		
	}
	
	public boolean getLanguageMode(){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("language_mode", "no").equals("yes");
	}
	public void setLanguageMode(boolean englishOnly){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String loadingStatus = englishOnly?"yes":"no";
		
		sp.edit().putString("language_mode",loadingStatus).commit();
		
	}
	
	
	public int getMappingFileImportLines(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt( sp.getString( "mapping_import_line", "0"));
	}
	public void setMappingFileImportLines(int lines){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString( "mapping_import_line", String.valueOf(lines)).commit();	
	}
	
	public String getRerverseLookupTable(String table){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(table.equals("phonetic")){
			return sp.getString("bpmf_im_reverselookup", "none");
		}else{
			return sp.getString(table + "_im_reverselookup", "none");
		}
	}
	
	
	
	public boolean getFixedCandidateViewDisplay(){

		return true;
		/*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

		// force user to checked the fixed_candidate_view_display setting
		boolean forceactive = sp.getBoolean("fixed_candidate_view_display_force", true);
		if(forceactive){
			sp.edit().putBoolean("fixed_candidate_view_display_force", false).commit();
			sp.edit().putBoolean("fixed_candidate_view_display", true).commit();
		}

		return sp.getBoolean("fixed_candidate_view_display", true);  //Jeremy '15,6,4 set default to  true.*/
	}

	/*public boolean getEnableTransparentCandidateView(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("enable_transparent_candidate_view", false);
	}*/

	public boolean getDisableSoftwareKeyboard(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("disable_software_keyboard", false);
	}
	
	public boolean getLearnRelatedWord(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("candidate_suggestion", true);
	}

	public boolean getLearnPhrase(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("learn_phrase", true);
	}
	
	public boolean getDisablePhysicalSelKeyOption(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("disable_physical_selkey_option", false);
	}
	
	public boolean getEnglishPrediction(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("english_dictionary_enable", true);
	}
	
	public boolean getPhysicalKeyboardEnable(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("physical_keyboard_enable", true);
	}
	
	public boolean getEnglishPredictionOnPhysicalKeyboard(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("english_dictionary_physical_keyboard", false);
	}
	
	public boolean getSortSuggestions(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("learning_switch", true);
	}

	public boolean getCandidateSuggestionPunctutation(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("candidate_suggestion_punctuation", true);
	}
	
	public boolean getPhysicalKeyboardSortSuggestions(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("physical_keyboard_sort", true);
	}

	public boolean getSimiliarEnable(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("similiar_enable", true);
	}
	
	public boolean getSelectDefaultOnSliding(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("candidate_switch", true);
	}
	
	public boolean getVibrateOnKeyPressed(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("vibrate_on_keypress", true);
	}
	
	
	
	public boolean getSoundOnKeyPressed(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("sound_on_keypress", false);
	}

	public boolean getEmojiMode(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		//Jeremy '16,7,30 Emoji support is limited before API 16
		return sp.getBoolean("enable_emoji", (Build.VERSION.SDK_INT >= 16)?true:false);
	}

	public Integer getEmojiDisplayPosition(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("enable_emoji_position", "3"));
	}

	public boolean getReverseLookupNotify(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("reverse_lookup_notify", true);
	}

	public boolean getHanConvertNotify(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("han_convert_notify", true);
	}

	public boolean getPersistentLanguageMode(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("persistent_language_mode", false);
	}
	
	public boolean getShowNumberRowInEnglish(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("number_row_in_english", true);
	}
	public void syncIMActivatedState(List<Im> imlist){
		String state = "";
		HashMap<String, String> imhm = new HashMap<String, String>();
		for(Im i :imlist){
			imhm.put(i.getCode(), i.getCode());
		}

		if(imhm.get(Lime.IM_CUSTOM) != null){
			state += "0";
		}

		if(imhm.get(Lime.IM_CJ) != null){
			if(!state.equals("")){state += ";";}
			state += "1";
		}
		if(imhm.get(Lime.IM_SCJ) != null){
			if(!state.equals("")){state += ";";}
			state += "2";
		}
		if(imhm.get(Lime.IM_CJ5) != null){
			if(!state.equals("")){state += ";";}
			state += "3";
		}
		if(imhm.get(Lime.IM_ECJ) != null){
			if(!state.equals("")){state += ";";}
			state += "4";
		}
		if(imhm.get(Lime.IM_DAYI) != null){
			if(!state.equals("")){state += ";";}
			state += "5";
		}
		if(imhm.get(Lime.IM_PHONETIC) != null){
			if(!state.equals("")){state += ";";}
			state += "6";
		}
		if(imhm.get(Lime.IM_EZ) != null){
			if(!state.equals("")){state += ";";}
			state += "7";
		}
		if(imhm.get(Lime.IM_ARRAY) != null){
			if(!state.equals("")){state += ";";}
			state += "8";
		}
		if(imhm.get(Lime.IM_ARRAY10) != null){
			if(!state.equals("")){state += ";";}
			state += "9";
		}
		if(imhm.get(Lime.IM_WB) != null){
			if(!state.equals("")){state += ";";}
			state += "10";
		}
		if(imhm.get(Lime.IM_HS) != null){
			if(!state.equals("")){state += ";";}
			state += "11";
		}
		if(imhm.get(Lime.IM_PINYIN) != null){
			if(!state.equals("")){state += ";";}
			state += "12";
		}

		setIMActivatedState(state);
	}
	
	public String getIMActivatedState(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("keyboard_state", "0;1;2;3;4;5;6;7;8;9;10;11;12");
	}
	public void setIMActivatedState(String state){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString( "keyboard_state", String.valueOf(state)).commit();	
	}
	
	public String getActiveIM(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("keyboard_list", "phonetic");
	}
	
	
	public void setActiveIM(String activeIM){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString( "keyboard_list", String.valueOf(activeIM)).commit();	
	}
	
	public boolean getThreerowRemapping(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("three_rows_remapping", false);
	}
	
	public String getPhysicalKeyboardType(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("physical_keyboard_type", "normal_keyboard");
	}
	
	public int getAutoCommitValue(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("auto_commit", "0"));
	}
	
	public String getPhoneticKeyboardType(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString("phonetic_keyboard_type", "standard");
	}
	
	public boolean getAutoCaptalization(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("auto_cap", true);
	}
	
	public boolean getQuickFixes(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("quick_fixes", true);
	}
	
	public boolean getAutoComplete(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("auto_complete", true);
	}
	
	public boolean getDisablePhysicalSelkey(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("disable_physical_selkey", false);
	}
	
	
	public Integer getHanCovertOption(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("han_convert_option", "0"));
	}
	
	public void setHanCovertOption(int value){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString( "han_convert_option", String.valueOf(value)).commit();	
		
	}
	
	public Integer getSelkeyOption(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("selkey_option", "0"));
	}
	
	public Integer getSimilarCodeCandidates(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("similiar_list", "20"));
	}
	
	public float getFontSize(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Float.parseFloat(sp.getString("font_size", "1"));
		
	}
	
	public float getKeyboardSize(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Float.parseFloat(sp.getString("keyboard_size", "1"));
		
	}

	public boolean getSmartChineseInput(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("smart_chinese_input", false);
	}
	
	public boolean getAutoChineseSymbol(){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("auto_chinese_symbol", false);
	}
	
	
	public Integer getVibrateLevel(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("vibrate_level", "40"));
	}
	
	
	public boolean getShowNumberKeypard(){
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("display_number_keypads", false);
	}
	
	
	public boolean getAllowNumberMapping(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("accept_number_index", false);
	}
	
	public boolean getAllowSymoblMapping(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("accept_symbol_index", false);
	}
	
	
	
	public boolean getSwitchEnglishModeHotKey(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("switch_english_mode", false);
	}

	public boolean getShiftSwitchEnglishMode(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("switch_english_mode_shift", true);
	}
	
	
	public boolean getAutoHideSoftKeyboard(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("hide_software_keyboard_typing_with_physical", true);

	}
	
	public int getShowArrowKeys(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("show_arrow_key", "0"));
		
	}
	
	public void setShowArrowKeys(int mode){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString("show_arrow_key", Integer.toString(mode)).commit();	
		
	}
	
	public int getSplitKeyboard(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("split_keyboard_mode", "0"));
	}

	public int getKeyboardTheme(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return Integer.parseInt(sp.getString("keyboard_theme", "0"));
	}
	
	public void setSplitKeyboard(int mode){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString("split_keyboard_mode", Integer.toString(mode)).commit();	
		
	}
	
	public boolean getResetCacheFlag(boolean defaultvalue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean("searchsrv_reset_cache", defaultvalue);
	}
	
	
	
	public void setResetCacheFlag(boolean value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putBoolean("searchsrv_reset_cache", value).commit();	
	}
	
	
	
	/*
	 * INT Parameter SET/GET
	 */
	public void setParameter(String label, int value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putInt(label, value).commit();	
	}
	public int getParameterInt(String label){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getInt(label, 0);
	}

	public int getParameterInt(String label, int defaultvalue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getInt(label, defaultvalue);
	}
	
	/*
	 * LONG Parameter SET/GET
	 */
	public long getParameterLong(String label, long defaultvalue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getLong(label, defaultvalue);
	}
	
	public long getParameterLong(String label){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getLong(label, 0);
	}
	
	public void setParameter(String label, long value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putLong(label, value).commit();	
	}
	
	/*
	 * String Parameter SET/GET
	 */
	public void setParameter(String label, String value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putString(label, value).commit();	
	}
	public String getParameterString(String label){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString(label, "");
	}
	
	public String getParameterString(String label, String defaultstring){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString(label, defaultstring);
	}


	/*
	 * Boolean Parameter SET/GET
	 */
	public void setParameter(String label, boolean value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		sp.edit().putBoolean(label, value).commit();	
	}
	public boolean getParameterBoolean(String label){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getBoolean(label, false);
	}
	public boolean getParameterBoolean(String label, boolean defaultvalue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		try{
			return sp.getBoolean(label, defaultvalue);
		}catch(Exception e){
			return defaultvalue;
		}
	}
	
	private String preProcessTableName(String table){
		if(table.endsWith("_")|| table.equals("")){ 
			return table; // processed already.
		}else if(table.equals("phonetic")) {
			return "bpmf_";
		}else if(table.equals("mapping")||table.equals("lime") || table.equals("phone") ){
			return "";
		}else{ 
			return table+"_";
		}
	}
	
}
