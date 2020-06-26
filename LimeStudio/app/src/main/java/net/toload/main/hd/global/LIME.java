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
	
	// Special Version CJK Mapping Table Provided By Julian
	//OpenFoundry
	public static final String CJK_CJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fcj_CJK.lime.zip";
	public static final String CJK_ECJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fecj_CJK.lime.zip";
	public static final String CJK_PHONETIC_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphonetic_CJK.lime.zip";
	public static final String CJK_PHONETICADV_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphonetic_CJK.lime.zip";
	public static final String CJK_PINYIN_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fpinyin_CJK.cin.zip";
	public static final String CJK_HK_CJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fcj_CJK_HKSCS.lime.zip";
	public static final String CJK_HK_ECJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fecj_CJK_HKSCS.lime.zip";
	//Google
	public static final String G_CJK_CJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/cj_CJK.lime";
	public static final String G_CJK_ECJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/ecj_CJK.lime";
	public static final String G_CJK_PHONETIC_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/phonetic_CJK.lime";
	public static final String G_CJK_PHONETICADV_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/phonetic_adv_CJK.lime";
	public static final String G_CJK_PINYIN_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/pinyin_CJK.cin";
	public static final String G_CJK_HK_CJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/cj_CJK_HKSCS.lime";
	public static final String G_CJK_HK_ECJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/ecj_CJK_HKSCS.lime";
	
	// OV CIN files download URL
	public static final String DAYI_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/dayi3.cin";
	public static final String PINYI_TW_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/pinyinbig5.cin";
	public static final String PINYI_CN_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/pinyin.cin";
	
	// OpenFoundary
	public static final String CJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fcj.zip";
	public static final String SCJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fscj.zip";
	public static final String ARRAY_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Farray.zip";
	public static final String ARRAY10_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Farray10.zip";
	public static final String EZ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fez.zip";
	public static final String PHONETIC_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphonetic.zip";
	public static final String PHONETICADV_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphonetic_adv_CJK.zip";
	public static final String CJ5_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fcj5.zip";
	public static final String ECJ_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fecj.zip";
	public static final String WB_DOWNLOAD_URL = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fwb.zip";
	//public static final String HS_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/hs.cin";
	//public static final String IM_DOWNLOAD_TARGET_PRELOADED = "http://limeime.googlecode.com/svn/branches/database/lime1206.zip";
	public static final String IM_DOWNLOAD_TARGET_PRELOADED = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Flime1207.zip";
	public static final String IM_DOWNLOAD_TARGET_EMPTY = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fempty1109.zip";
	public static final String IM_DOWNLOAD_TARGET_PHONETIC_ONLY = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphoneticonly1207.zip";
	//public static final String IM_DOWNLOAD_TARGET_PHONETIC_ONLY = "http://limeime.googlecode.com/svn/branches/database/phoneticonly1206.zip";
	//public static final String IM_DOWNLOAD_TARGET_PHONETIC_HS_ONLY = "http://limeime.googlecode.com/svn/branches/database/phonetichs1206.zip";
	public static final String IM_DOWNLOAD_TARGET_PHONETIC_HS_ONLY = "http://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2Fdatabases%2Flimehd%2Fphonetichs1207.zip";
	
	// Google Code
	public static final String G_CJ_11643_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/cangjie.cin";
	public static final String G_PHONETIC_11643_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/bopomofo.cin";
	public static final String G_CJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/cj.zip";
	public static final String G_SCJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/scj.zip";
	public static final String G_ARRAY_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/array.zip";
	public static final String G_ARRAY10_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/array10.zip";
	public static final String G_EZ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/ez.zip";
	public static final String G_PHONETIC_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/phonetic.zip";
	public static final String G_PHONETICADV_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/phonetic_adv_CJK.zip";
	public static final String G_CJ5_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/cj5.zip";
	public static final String G_ECJ_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/ecj.zip";
	public static final String G_WB_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/wb.zip";
	//public static final String G_HS_DOWNLOAD_URL = "http://limeime.googlecode.com/svn/branches/database/hs.cin";
	public static final String G_IM_DOWNLOAD_TARGET_PRELOADED = "http://limeime.googlecode.com/svn/branches/database/lime1207.zip";
	public static final String G_IM_DOWNLOAD_TARGET_EMPTY = "http://limeime.googlecode.com/svn/branches/database/empty1109.zip";
	public static final String G_IM_DOWNLOAD_TARGET_PHONETIC_ONLY = "http://limeime.googlecode.com/svn/branches/database/phoneticonly1207.zip";
	//public static final String G_IM_DOWNLOAD_TARGET_PHONETIC_HS_ONLY = "http://limeime.googlecode.com/svn/branches/database/phonetichs1207.zip";
	
	public static final String LIME_SDCARD_FOLDER = Environment.getExternalStorageDirectory() + "/limehd/";
	public static String getLimeDataRootFolder(){ return Environment.getDataDirectory() + "/data/"+LIME.PACKAGE_NAME; }
	public static final String DOWNLOAD_START = "download_start";
	public static final String DATABASE_DOWNLOAD_STATUS = "database_download_status";
	public static final String DATABASE_SOURCE_DAYI = "dayi.cin";
	public static final String DATABASE_SOURCE_PHONETIC = "phonetic.lime";
	public static final String DATABASE_SOURCE_PHONETIC_CNS = "bopomofo.cin";
	public static final String DATABASE_SOURCE_PHONETICADV = "phonetic_adv_CJK.lime";
	public static final String DATABASE_SOURCE_CJ = "cj.lime";
	public static final String DATABASE_SOURCE_CJ_CNS = "cangjie.cin";
	public static final String DATABASE_SOURCE_CJ5 = "cj5.lime";
	public static final String DATABASE_SOURCE_ECJ = "ecj.lime";
	public static final String DATABASE_SOURCE_SCJ = "scj.lime";
	public static final String DATABASE_SOURCE_ARRAY = "array.lime";
	public static final String DATABASE_SOURCE_ARRAY10 = "array10.lime";
	//public static final String DATABASE_SOURCE_HS = "hs.lime";
	public static final String DATABASE_SOURCE_WB = "stroke5.cin";
	public static final String DATABASE_SOURCE_EZ = "ez.lime";
	public static final String DATABASE_SOURCE_PINYIN_BIG5 = "pinyinbig5.cin";
	public static final String DATABASE_SOURCE_PINYIN_GB = "pinyin.cin";
	public static final String DATABASE_SOURCE_PINYIN_LIME = "pinyin_CJK.cin";
	public static final String DATABASE_SOURCE_CJ_LIME = "cj_CJK.lime";
	public static final String DATABASE_SOURCE_ECJ_LIME = "ecj_CJK.lime";
	public static final String DATABASE_SOURCE_PHONETIC_LIME = "phonetic_CJK.lime";
	public static final String DATABASE_SOURCE_FILENAME = "lime.zip";
	public static final String DATABASE_SOURCE_FILENAME_EMPTY = "empty.zip";
	public static final String DATABASE_RELATIVE_FOLDER = "/databases";
	public static String getLIMEDatabaseFolder(){ return  Environment.getDataDirectory() + "/data/"+LIME.PACKAGE_NAME + LIME.DATABASE_RELATIVE_FOLDER;};
	public static final String DATABASE_NAME = "lime.db";
	public static final String DATABASE_JOURNAL = "lime.db-journal";
	public static final String DATABASE_DECOMPRESS_FOLDER_SDCARD = Environment.getExternalStorageDirectory() + "/limehd";
	public static final String DATABASE_BACKUP_NAME = "backup.zip";
	public static final String DATABASE_JOURNAL_BACKUP_NAME = "backupJournal.zip";
	public static final String SHARED_PREFS_BACKUP_NAME=  "shared_prefs.bak";
	public static final String DATABASE_CLOUD_TEMP = "cloudtemp.zip";
	public static final String IM_CJ_STATUS = "im_cj_status";
	public static final String IM_SCJ_STATUS = "im_scj_status";
	public static final String IM_PHONETIC_STATUS = "im_phonetic_status";
	public static final String IM_DAYI_STATUS = "im_dayi_status";
	public static final String IM_CUSTOM_STATUS = "im_custom_status";
	public static final String IM_EZ_STATUS = "im_ez_status";
	
	public static final String IM_MAPPING_FILENAME = "im_mapping_filename";
	public static final String IM_MAPPING_VERSION = "im_mapping_version";
	public static final String IM_MAPPING_TOTAL = "im_mapping_total";
	public static final String IM_MAPPING_DATE = "im_mapping_date";

	public static final String CANDIDATE_SUGGESTION = "candidate_suggestion";
	public static final String TOTAL_USERDICT_RECORD = "total_userdict_record";
	public static final String LEARNING_SWITCH = "learning_switch";

	


	//public final static String SEARCHSRV_RESET_CACHE = "searchsrv_reset_cache";
	public final static int SEARCHSRV_RESET_CACHE_SIZE = 256;
	public final static int LIMEDB_CACHE_SIZE = 1024;

	// ADMOB
	public final static String publisher = "ca-app-pub-6429718170873338/7028669804";

}
