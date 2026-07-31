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

package net.toload.main.hd.skin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Holds the imported .cskin skin. The skin file is copied into the app's
 * files dir on import and lazily parsed; the parsed settings are cached
 * until {@link #reload()}.
 *
 * The skin is applied when the keyboard_theme preference selects the
 * "Custom skin" theme ({@link #THEME_INDEX_CUSTOM}); light/dark style is
 * chosen from the system night mode.
 */
public class SkinManager {

    private static final String TAG = "SkinManager";
    private static final String SKIN_FILENAME = "custom.cskin";

    /** Index of the custom-skin entry in LIMEService.KEYBOARD_THEMES. */
    public static final int THEME_INDEX_CUSTOM = 6;

    private static SkinManager sInstance;

    private SkinSettings mSkin;
    private boolean mLoadFailed;
    private int mGeneration = 0;

    public static synchronized SkinManager getInstance() {
        if (sInstance == null) sInstance = new SkinManager();
        return sInstance;
    }

    private SkinManager() {
    }

    public static File getSkinFile(Context context) {
        return new File(context.getFilesDir(), SKIN_FILENAME);
    }

    public synchronized boolean hasSkin(Context context) {
        return getSkinFile(context).exists();
    }

    public synchronized SkinSettings getSkin(Context context) {
        if (mSkin != null) return mSkin;
        if (mLoadFailed) return null;
        File f = getSkinFile(context);
        if (!f.exists()) return null;
        try {
            mSkin = CskinParser.parse(f);
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse skin file", e);
            mLoadFailed = true;
        }
        return mSkin;
    }

    public synchronized void reload() {
        mSkin = null;
        mLoadFailed = false;
        mGeneration++;
    }

    /** Bumped on every successful import so cached views know to rebuild. */
    public synchronized int getGeneration() {
        return mGeneration;
    }

    /**
     * Copies the picked .cskin content into the app files dir and validates it.
     * Returns the parsed skin, or null on failure (a previously imported valid
     * skin is preserved).
     */
    public synchronized SkinSettings importSkin(Context context, InputStream in) {
        File target = getSkinFile(context);
        File temp = new File(context.getFilesDir(), SKIN_FILENAME + ".tmp");
        try {
            FileOutputStream out = new FileOutputStream(temp);
            try {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            } finally {
                out.close();
            }
            SkinSettings parsed = CskinParser.parse(temp);
            if (!temp.renameTo(target)) {
                if (target.exists() && target.delete() && temp.renameTo(target)) {
                    // retried after removing the old file
                } else {
                    throw new IOException("Cannot move skin file into place");
                }
            }
            mSkin = parsed;
            mLoadFailed = false;
            mGeneration++;
            return parsed;
        } catch (Exception e) {
            Log.w(TAG, "Skin import failed", e);
            temp.delete();
            return null;
        }
    }

    /** True when the theme preference selects the custom skin and a skin is loaded. */
    public boolean isActive(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int theme;
        try {
            theme = Integer.parseInt(sp.getString("keyboard_theme", "0"));
        } catch (NumberFormatException e) {
            theme = 0;
        }
        if (theme != THEME_INDEX_CUSTOM) return false;
        SkinSettings skin = getSkin(context);
        return skin != null && skin.enableCustomColors;
    }

    public static boolean isNightMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    /** The style to apply right now, or null when the custom skin is not active. */
    public SkinStyle getActiveStyle(Context context) {
        if (!isActive(context)) return null;
        SkinSettings skin = getSkin(context);
        if (skin == null) return null;
        return skin.forNight(isNightMode(context));
    }

    /** The active skin settings (toolbar/gestures), or null when not active. */
    public SkinSettings getActiveSkin(Context context) {
        if (!isActive(context)) return null;
        return getSkin(context);
    }
}
