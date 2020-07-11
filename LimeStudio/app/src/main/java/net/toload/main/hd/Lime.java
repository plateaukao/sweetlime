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

import android.os.Environment;

import java.text.DecimalFormat;

/**
 * Created by Art Hung on 2015/4/24.
 */
public class Lime {

    // Database Setting
    final public static String DATABASE_NAME = "lime.db";
    final public static String DATABASE_DEVICE_FOLDER =  Environment.getDataDirectory() + "/data/net.toload.main.hd/databases";
    final public static String DATABASE_DECOMPRESS_FOLDER_SDCARD = Environment.getExternalStorageDirectory() + "/limehd/databases";
    final public static String DATABASE_FOLDER_EXTERNAL = Environment.getExternalStorageDirectory() + "/limehd/";
    final public static String DATABASE_BACKUP_NAME = "backup.zip";

    public static final String DATABASE_IM_TEMP = "temp";
    public static final String DATABASE_IM_TEMP_EXT = "zip";

    public static final String DATABASE_OPENFOUNDRY_URL_BASED = "https://www.openfoundry.org/websvn/filedetails.php?repname=limeime&path=%2F";
    public static final String DATABASE_CLOUD_URL_BASED = "https://github.com/lime-ime/limeime/raw/master/Database/";


    public static final String DATABASE_CLOUD_IM_WB = DATABASE_CLOUD_URL_BASED + "wb.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_WB = DATABASE_OPENFOUNDRY_URL_BASED + "wb.zip";

    public static final String DATABASE_CLOUD_IM_PINYINGB = DATABASE_CLOUD_URL_BASED + "pinyingb.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PINYINGB = DATABASE_OPENFOUNDRY_URL_BASED + "pinyingb.zip";

    public static final String DATABASE_CLOUD_IM_PINYIN = DATABASE_CLOUD_URL_BASED + "pinyin.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PINYIN = DATABASE_OPENFOUNDRY_URL_BASED + "pinyin.zip";

    public static final String DATABASE_CLOUD_IM_PHONETICCOMPLETE_BIG5 = DATABASE_CLOUD_URL_BASED + "phoneticcompletebig5.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PHONETICCOMPLETE_BIG5 = DATABASE_OPENFOUNDRY_URL_BASED + "phoneticcompletebig5.zip";

    public static final String DATABASE_CLOUD_IM_PHONETICCOMPLETE = DATABASE_CLOUD_URL_BASED + "phoneticcomplete.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PHONETICCOMPLETE = DATABASE_OPENFOUNDRY_URL_BASED + "phoneticcomplete.zip";

    public static final String DATABASE_CLOUD_IM_PHONETIC_BIG5 = DATABASE_CLOUD_URL_BASED + "phoneticbig5.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PHONETIC_BIG5 = DATABASE_OPENFOUNDRY_URL_BASED + "phoneticbig5.zip";

    public static final String DATABASE_CLOUD_IM_PHONETIC = DATABASE_CLOUD_URL_BASED + "phonetic.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_PHONETIC = DATABASE_OPENFOUNDRY_URL_BASED + "phonetic.zip";

    public static final String DATABASE_CLOUD_IM_EZ = DATABASE_CLOUD_URL_BASED + "ez.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_EZ = DATABASE_OPENFOUNDRY_URL_BASED + "ez.zip";

    public static final String DATABASE_CLOUD_IM_ECJHK = DATABASE_CLOUD_URL_BASED + "ecjhk.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_ECJHK = DATABASE_OPENFOUNDRY_URL_BASED + "ecjhk.zip";

    public static final String DATABASE_CLOUD_IM_ECJ = DATABASE_CLOUD_URL_BASED + "ecj.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_ECJ = DATABASE_OPENFOUNDRY_URL_BASED + "ecj.zip";

    public static final String DATABASE_CLOUD_IM_DAYI = DATABASE_CLOUD_URL_BASED + "dayi.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_DAYI = DATABASE_OPENFOUNDRY_URL_BASED + "dayi.zip";

    public static final String DATABASE_CLOUD_IM_DAYIUNI = DATABASE_CLOUD_URL_BASED + "dayiuni.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_DAYIUNI = DATABASE_OPENFOUNDRY_URL_BASED + "dayiuni.zip";

    public static final String DATABASE_CLOUD_IM_DAYIUNIP = DATABASE_CLOUD_URL_BASED + "dayiunip.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_DAYIUNIP = DATABASE_OPENFOUNDRY_URL_BASED + "dayiunip.zip";

    public static final String DATABASE_CLOUD_IM_CJHK = DATABASE_CLOUD_URL_BASED + "cjhk.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_CJHK = DATABASE_OPENFOUNDRY_URL_BASED + "cjhk.zip";

    public static final String DATABASE_CLOUD_IM_SCJ = DATABASE_CLOUD_URL_BASED + "scj.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_SCJ = DATABASE_OPENFOUNDRY_URL_BASED + "scj.zip";

    public static final String DATABASE_CLOUD_IM_CJ5 = DATABASE_CLOUD_URL_BASED + "cj5.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_CJ5 = DATABASE_OPENFOUNDRY_URL_BASED + "cj5.zip";

    public static final String DATABASE_CLOUD_IM_CJ_BIG5 = DATABASE_CLOUD_URL_BASED + "cjbig5.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_CJ_BIG5 = DATABASE_OPENFOUNDRY_URL_BASED + "cjbig5.zip";

    public static final String DATABASE_CLOUD_IM_CJ = DATABASE_CLOUD_URL_BASED + "cj.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_CJ = DATABASE_OPENFOUNDRY_URL_BASED + "cj.zip";

    public static final String DATABASE_CLOUD_IM_ARRAY10 = DATABASE_CLOUD_URL_BASED + "array10.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_ARRAY10 = DATABASE_OPENFOUNDRY_URL_BASED + "array10.zip";

    public static final String DATABASE_CLOUD_IM_ARRAY = DATABASE_CLOUD_URL_BASED + "array.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_ARRAY = DATABASE_OPENFOUNDRY_URL_BASED + "array.zip";

    public static final String DATABASE_CLOUD_IM_HS = DATABASE_CLOUD_URL_BASED + "hs.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_HS = DATABASE_OPENFOUNDRY_URL_BASED + "hs.zip";
    public static final String DATABASE_CLOUD_IM_HS_V1 = DATABASE_CLOUD_URL_BASED + "hs1.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_HS_V1 = DATABASE_OPENFOUNDRY_URL_BASED + "hs1.zip";
    public static final String DATABASE_CLOUD_IM_HS_V2 = DATABASE_CLOUD_URL_BASED + "hs2.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_HS_V2  = DATABASE_OPENFOUNDRY_URL_BASED + "hs2.zip";
    public static final String DATABASE_CLOUD_IM_HS_V3 = DATABASE_CLOUD_URL_BASED + "hs3.zip";
    public static final String DATABASE_OPENFOUNDRY_IM_HS_V3 = DATABASE_OPENFOUNDRY_URL_BASED + "hs3.zip";

    // Database Tables and columns

    public static final String DB_TABLE_ARRAY = "array";
    public static final String DB_TABLE_ARRAY10 = "array10";
    public static final String DB_TABLE_CJ = "cj";
    public static final String DB_TABLE_CJ5 = "cj5";
    public static final String DB_TABLE_CUSTOM = "custom";
    public static final String DB_TABLE_DAYI = "dayi";
    public static final String DB_TABLE_ECJ = "ecj";
    public static final String DB_TABLE_EZ = "ez";
    public static final String DB_TABLE_HS = "hs";
    public static final String DB_TABLE_PHONETIC = "phonetic";
    public static final String DB_TABLE_PINYIN = "pinyin";
    public static final String DB_TABLE_SCJ = "scj";
    public static final String DB_TABLE_WB = "wb";
    
    public static final String IM_ARRAY = "array";
    public static final String IM_ARRAY10 = "array10";
    public static final String IM_CJ_BIG5 = "cjbig5";
    public static final String IM_CJ = "cj";
    public static final String IM_CJHK = "cjhk";
    public static final String IM_CJ5 = "cj5";
    public static final String IM_CUSTOM = "custom";
    public static final String IM_DAYI = "dayi";
    public static final String IM_DAYIUNI = "dayiuni";
    public static final String IM_DAYIUNIP = "dayiunip";
    public static final String IM_ECJ = "ecj";
    public static final String IM_ECJHK = "ecjhk";
    public static final String IM_EZ = "ez";
    public static final String IM_HS = "hs";
    public static final String IM_HS_V1 = "hs1";
    public static final String IM_HS_V2 = "hs2";
    public static final String IM_HS_V3 = "hs3";
    public static final String IM_PHONETIC = "phonetic";
    public static final String IM_PHONETIC_ADV = "phoneticadv";
    public static final String IM_PHONETIC_BIG5 = "phoneticbig5";
    public static final String IM_PHONETIC_ADV_BIG5 = "phoneticadvbig5";
    public static final String IM_PINYIN = "pinyin";
    public static final String IM_PINYINGB = "pinyingb";
    public static final String IM_SCJ = "scj";
    public static final String IM_WB = "wb";

    public static final String DB_COLUMN_ID = "_id";

    public static final String DB_COLUMN_CODE = "code";
    public static final String DB_COLUMN_CODE3R = "code3r";
    public static final String DB_COLUMN_WORD = "word";
    public static final String DB_COLUMN_RELATED = "related";
    public static final String DB_COLUMN_SCORE = "score";
    public static final String DB_COLUMN_BASESCORE = "basescore";

    public static final String DB_IM = "im";
    public static final String DB_IM_COLUMN_ID = "_id";
    public static final String DB_IM_COLUMN_CODE = "code";
    public static final String DB_IM_COLUMN_TITLE = "title";
    public static final String DB_IM_COLUMN_DESC = "desc";
    public static final String DB_IM_COLUMN_KEYBOARD = "keyboard";
    public static final String DB_IM_COLUMN_DISABLE = "disable";
    public static final String DB_IM_COLUMN_SELKEY = "selkey";
    public static final String DB_IM_COLUMN_ENDKEY = "endkey";
    public static final String DB_IM_COLUMN_SPACESTYLE = "spacestyle";


    public static final String DB_RELATED = "related";
    public static final String DB_RELATED_COLUMN_ID = "_id";
    public static final String DB_RELATED_COLUMN_PWORD = "pword";
    public static final String DB_RELATED_COLUMN_CWORD = "cword";
    public static final String DB_RELATED_COLUMN_BASESCORE = "basescore";
    public static final String DB_RELATED_COLUMN_USERSCORE = "score";

    public static final String DB_KEYBOARD = "keyboard";
    public static final String DB_KEYBOARD_COLUMN_ID = "_id";
    public static final String DB_KEYBOARD_COLUMN_CODE = "code";
    public static final String DB_KEYBOARD_COLUMN_NAME = "name";
    public static final String DB_KEYBOARD_COLUMN_DESC = "desc";
    public static final String DB_KEYBOARD_COLUMN_TYPE = "type";
    public static final String DB_KEYBOARD_COLUMN_IMAGE = "image";
    public static final String DB_KEYBOARD_COLUMN_IMKB = "imkb";
    public static final String DB_KEYBOARD_COLUMN_IMSHIFTKB = "imshiftkb";
    public static final String DB_KEYBOARD_COLUMN_ENGKB = "engkb";
    public static final String DB_KEYBOARD_COLUMN_ENGSHIFTKB = "engshiftkb";
    public static final String DB_KEYBOARD_COLUMN_SYMBOLKB = "symbolkb";
    public static final String DB_KEYBOARD_COLUMN_SYMBOLSHIFTKB = "symbolshiftkb";
    public static final String DB_KEYBOARD_COLUMN_DEFAULTKB = "defaultkb";
    public static final String DB_KEYBOARD_COLUMN_DEFAULTSHIFTKB = "defaultshiftkb";
    public static final String DB_KEYBOARD_COLUMN_EXTENDEDKB = "extendedkb";
    public static final String DB_KEYBOARD_COLUMN_EXTENDEDSHIFTKB = "extendedshiftkb";
    public static final String DB_KEYBOARD_COLUMN_DISABLE = "disable";
    public static final String DB_TOTAL_COUNT ="count";

    public static final String IM_TYPE_NAME = "name";
    public static final String IM_TYPE_KEYBOARD = "keyboard";

    public static final int IM_MANAGE_DISPLAY_AMOUNT = 100;

    public static final String DB_CHECK_RELATED_USERSCORE = "db_user_score_check";

    // Cloud Backup/Restore
    public static final String BACKUP = "backup";
    public static final String RESTORE = "restore";

    public static final String GOOGLE = "GOOGLE";

    public static final String LOCAL = "LOCAL";

    public static final String DROPBOX = "DROPBOX";

    public static final String DEVICE = "device";
    public static final float HALF_ALPHA_VALUE = .5f;
    public static final float NORMAL_ALPHA_VALUE = 1f;

    public static final String SHARE_TYPE_TXT = "text/plain";
    public static final String SHARE_TYPE_ZIP = "application/zip";

    public static final String IMPORT_TEXT = "import_text";

    public static final String SUPPORT_FILE_EXT_TXT = "txt";
    public static final String SUPPORT_FILE_EXT_LIME = "lime";
    public static final String SUPPORT_FILE_EXT_LIMEDB = "limedb";
    public static final String SUPPORT_FILE_EXT_CIN= "cin";

    // Emoji Parameter

    public static final int EMOJI_EN = 1;
    public static final int EMOJI_TW = 2;
    public static final int EMOJI_CN = 3;

    public static final String EMOJI_FIELD_TAG= "tag";
    public static final String EMOJI_FIELD_VALUE= "value";

    // Global Utility Methods

    public static String format(int number){
        try {
            DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,##0");
            return df.format(number);
        }catch(Exception e){
            e.printStackTrace();
            return "0";
        }
    }

    public static String formatSqlValue(String value){
        if(value != null) {
            value = value.replaceAll("\"", "\"\"");
            value = value.replaceAll("'", "\\\'");
            return value;
        }else{
            return "";
        }
    }

}
