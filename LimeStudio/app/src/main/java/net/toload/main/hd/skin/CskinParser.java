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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Parses a .cskin file (a zip exported by the 蝦米 skin web designer) into
 * {@link SkinSettings}. Only the embedded settings.json is read; the jsonnet
 * sources and iOS image resources in the zip are ignored.
 *
 * Colors in settings.json are CSS ordered (#RRGGBB or #RRGGBBAA); they are
 * converted here to Android ARGB ints.
 */
public class CskinParser {

    private static final long MAX_SETTINGS_JSON_SIZE = 2 * 1024 * 1024;

    private CskinParser() {
    }

    public static SkinSettings parse(File cskinFile) throws IOException, JSONException {
        String json = readSettingsJson(cskinFile);
        if (json == null)
            throw new IOException("No settings.json found in " + cskinFile.getName());
        return parseSettingsJson(json);
    }

    private static String readSettingsJson(File cskinFile) throws IOException {
        ZipFile zip = new ZipFile(cskinFile);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name.endsWith("settings.json") && entry.getSize() <= MAX_SETTINGS_JSON_SIZE) {
                    InputStream in = zip.getInputStream(entry);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                        return out.toString("UTF-8");
                    } finally {
                        in.close();
                    }
                }
            }
            return null;
        } finally {
            zip.close();
        }
    }

    public static SkinSettings parseSettingsJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        SkinSettings skin = new SkinSettings();

        JSONObject skinInfo = root.optJSONObject("skinInfo");
        if (skinInfo != null) {
            skin.name = skinInfo.optString("name", "");
            skin.author = skinInfo.optString("author", "");
        }
        skin.enableCustomColors = root.optBoolean("enableCustomColors", true);

        JSONObject palette = root.optJSONObject("palette");
        JSONObject groups = root.optJSONObject("groups");
        skin.light = parseStyle(palette == null ? null : palette.optJSONObject("light"), groups, false);
        skin.dark = parseStyle(palette == null ? null : palette.optJSONObject("dark"), groups, true);

        JSONObject overrides = root.optJSONObject("overrides");
        if (overrides != null) {
            if (overrideEnabled(overrides.opt("enable26ChineseOverride"), "light"))
                applyKeyboard26Override(skin.light, path(overrides, "keyboard26Chinese", "light"));
            if (overrideEnabled(overrides.opt("enable26ChineseOverride"), "dark"))
                applyKeyboard26Override(skin.dark, path(overrides, "keyboard26Chinese", "dark"));
        }

        JSONArray toolbar = root.optJSONArray("toolbarButtons");
        if (toolbar != null) {
            int n = Math.min(toolbar.length(), skin.toolbarButtons.length);
            for (int i = 0; i < n; i++)
                skin.toolbarButtons[i] = toolbar.optInt(i, SkinSettings.TB_SPACER);
        }

        parseGestures(root, skin);
        return skin;
    }

    // ---- styles -------------------------------------------------------------

    private static SkinStyle parseStyle(JSONObject p, JSONObject groups, boolean darkDefaults) {
        SkinStyle s = new SkinStyle();
        // Defaults mirror the designer's initialSettings.
        if (!darkDefaults) {
            s.keyboardBackground = 0x01D0D3DA;
            s.keyNormal = 0xFFFFFFFF;
            s.keyNormalPressed = 0xFFABB0BA;
            s.keySystem = 0x80979FAF;
            s.keySystemPressed = 0xE6FFFFFF;
            s.keyEnter = 0x80979FAF;
            s.keyEnterPressed = 0xE6FFFFFF;
            s.textMain = 0xFF000000;
            s.textSub = 0x55000000;
            s.toolbarColor = 0xFF666666;
            s.candidateSelectedText = 0xFF000000;
            s.candidateUnselectedText = 0xFF000000;
            s.candidateSelectedBackground = 0xFFFFFFFF;
            s.bubbleTextSelected = 0xFFFFFFFF;
            s.bubbleTextUnselected = 0xFF000000;
            s.shadow = 0xFF9A9C9A;
            s.borderColor = 0xFFFFFFFF;
        } else {
            s.keyboardBackground = 0x01474747;
            s.keyNormal = 0x65D1D1D1;
            s.keyNormalPressed = 0x24D1D1D6;
            s.keySystem = 0x24D1D1D6;
            s.keySystemPressed = 0x59D1D1D6;
            s.keyEnter = 0x24D1D1D6;
            s.keyEnterPressed = 0x59D1D1D6;
            s.textMain = 0xFFFFFFFF;
            s.textSub = 0x55FFFFFF;
            s.toolbarColor = 0xFFCCCCCC;
            s.candidateSelectedText = 0xFFFFFFFF;
            s.candidateUnselectedText = 0xFFFFFFFF;
            s.candidateSelectedBackground = 0x65D1D1D1;
            s.bubbleTextSelected = 0xFFFFFFFF;
            s.bubbleTextUnselected = 0xFFFFFFFF;
            s.shadow = 0xFF1E1E1E;
            s.borderColor = 0x65D1D1D1;
        }
        s.toolbarBackground = s.keyboardBackground;
        s.borderSize = 0;
        s.alphabetSize = 21;
        s.lowercaseSize = 23;
        s.systemSize = 16;
        s.spaceSize = 14;
        s.numberSize = 24;
        s.toolbarSize = 20;
        s.swipeSize = 8;

        if (p != null) {
            s.keyboardBackground = color(p, "bg", s.keyboardBackground);
            s.toolbarBackground = s.keyboardBackground;
            s.keyNormal = color(p, "keyNormal", s.keyNormal);
            s.keyNormalPressed = color(p, "keyNormalHighlight", s.keyNormalPressed);
            s.keySystem = color(p, "keySystem", s.keySystem);
            s.keySystemPressed = color(p, "keySystemHighlight", s.keySystemPressed);
            s.keyEnter = color(p, "keyEnter", s.keyEnter);
            s.keyEnterPressed = color(p, "keyEnterHighlight", s.keyEnterPressed);
            s.textMain = color(p, "textMain", s.textMain);
            s.textSub = color(p, "textSub", s.textSub);
            s.toolbarColor = color(p, "toolbarColor", s.toolbarColor);
            s.candidateSelectedText = color(p, "candidateSelectedText", s.candidateSelectedText);
            s.candidateUnselectedText = color(p, "candidateUnselectedText", s.candidateUnselectedText);
            s.candidateSelectedBackground = color(p, "candidateSelectedBg", s.candidateSelectedBackground);
            s.bubbleTextSelected = color(p, "bubbleTextSelected", s.bubbleTextSelected);
            s.bubbleTextUnselected = color(p, "bubbleTextUnselected", s.bubbleTextUnselected);
            s.shadow = color(p, "shadow", s.shadow);
            s.borderColor = color(p, "border", s.borderColor);
            s.borderSize = (float) p.optDouble("borderSize", s.borderSize);
        }
        if (groups != null) {
            s.alphabetSize = (float) groups.optDouble("alphabetSize", s.alphabetSize);
            s.lowercaseSize = (float) groups.optDouble("lowercaseSize", s.lowercaseSize);
            s.systemSize = (float) groups.optDouble("systemSize", s.systemSize);
            s.spaceSize = (float) groups.optDouble("spaceSize", s.spaceSize);
            s.numberSize = (float) groups.optDouble("numberSize", s.numberSize);
            s.toolbarSize = (float) groups.optDouble("toolbarSize", s.toolbarSize);
            s.swipeSize = (float) groups.optDouble("swipeSize", s.swipeSize);
        }
        return s;
    }

    private static void applyKeyboard26Override(SkinStyle s, JSONObject o) {
        if (s == null || o == null) return;
        JSONObject alphabet = o.optJSONObject("alphabet");
        if (alphabet != null) {
            s.keyNormal = color(alphabet, "bg", s.keyNormal);
            s.keyNormalPressed = color(alphabet, "highlight", s.keyNormalPressed);
            s.textMain = color(alphabet, "color", s.textMain);
            s.alphabetSize = (float) alphabet.optDouble("labelSize", s.alphabetSize);
            s.lowercaseSize = (float) alphabet.optDouble("lowercaseSize", s.lowercaseSize);
            s.borderColor = color(alphabet, "borderColor", s.borderColor);
            s.borderSize = (float) alphabet.optDouble("borderWidth", s.borderSize);
            s.shadow = color(alphabet, "shadowColor", s.shadow);
            s.textSub = color(alphabet, "swipeColor", s.textSub);
            s.swipeSize = (float) alphabet.optDouble("swipeSize", s.swipeSize);
        }
        JSONObject systemKeys = o.optJSONObject("systemKeys");
        if (systemKeys != null) {
            s.keySystem = color(systemKeys, "bg", s.keySystem);
            s.keySystemPressed = color(systemKeys, "highlight", s.keySystemPressed);
            s.systemSize = (float) systemKeys.optDouble("labelSize", s.systemSize);
        }
        JSONObject enterKey = o.optJSONObject("enterKey");
        if (enterKey != null) {
            s.keyEnter = color(enterKey, "bg", s.keyEnter);
            s.keyEnterPressed = color(enterKey, "highlight", s.keyEnterPressed);
        }
        JSONObject spaceKey = o.optJSONObject("spaceKey");
        if (spaceKey != null) {
            s.spaceSize = (float) spaceKey.optDouble("labelSize", s.spaceSize);
        }
        JSONObject candidates = o.optJSONObject("candidates");
        if (candidates != null) {
            s.candidateSelectedText = color(candidates, "selectedText", s.candidateSelectedText);
            s.candidateUnselectedText = color(candidates, "unselectedText", s.candidateUnselectedText);
            s.candidateSelectedBackground = color(candidates, "selectedBg", s.candidateSelectedBackground);
        }
        JSONObject bubbleText = o.optJSONObject("bubbleText");
        if (bubbleText != null) {
            s.bubbleTextSelected = color(bubbleText, "selected", s.bubbleTextSelected);
            s.bubbleTextUnselected = color(bubbleText, "unselected", s.bubbleTextUnselected);
        }
        JSONObject keyboardBackground = o.optJSONObject("keyboardBackground");
        if (keyboardBackground != null)
            s.keyboardBackground = color(keyboardBackground, "bg", s.keyboardBackground);
        JSONObject toolbarBackground = o.optJSONObject("toolbarBackground");
        if (toolbarBackground != null)
            s.toolbarBackground = color(toolbarBackground, "bg", s.toolbarBackground);
        JSONObject toolbarButtons = o.optJSONObject("toolbarButtons");
        if (toolbarButtons != null) {
            s.toolbarColor = color(toolbarButtons, "color", s.toolbarColor);
            s.toolbarSize = (float) toolbarButtons.optDouble("size", s.toolbarSize);
        }
    }

    // ---- gestures -----------------------------------------------------------

    private static void parseGestures(JSONObject root, SkinSettings skin) {
        SkinSettings.RowGesture global = new SkinSettings.RowGesture();
        global.swipeUp = root.optBoolean("enableSwipeUpActions", true);
        global.swipeDown = root.optBoolean("enableSwipeDownActions", true);
        global.longPress = root.optBoolean("enableLongPressActions", true);
        global.showSwipeUpText = root.optBoolean("showSwipeUpText", true);
        global.showSwipeDownText = root.optBoolean("showSwipeDownText", true);

        for (int i = 0; i < skin.rows.length; i++)
            skin.rows[i] = global.copy();

        if (!root.optBoolean("enableAdvancedRowControl", false)) return;
        JSONObject rows = root.optJSONObject("advancedRowControl");
        if (rows == null) return;
        String[] names = {"row1", "row2", "row3", "row4"};
        for (int i = 0; i < names.length && i < skin.rows.length; i++) {
            JSONObject r = rows.optJSONObject(names[i]);
            if (r == null) continue;
            SkinSettings.RowGesture g = skin.rows[i];
            g.swipeUp = r.optBoolean("enableSwipeUpActions", g.swipeUp);
            g.swipeDown = r.optBoolean("enableSwipeDownActions", g.swipeDown);
            g.longPress = r.optBoolean("enableLongPressActions", g.longPress);
            g.showSwipeUpText = r.optBoolean("showSwipeUpText", g.showSwipeUpText);
            g.showSwipeDownText = r.optBoolean("showSwipeDownText", g.showSwipeDownText);
        }
    }

    // ---- helpers ------------------------------------------------------------

    /** enable26ChineseOverride is {light: bool, dark: bool} in settings.json, or a plain bool. */
    private static boolean overrideEnabled(Object flag, String mode) {
        if (flag instanceof Boolean) return (Boolean) flag;
        if (flag instanceof JSONObject) return ((JSONObject) flag).optBoolean(mode, false);
        return false;
    }

    private static JSONObject path(JSONObject o, String a, String b) {
        JSONObject x = o.optJSONObject(a);
        return x == null ? null : x.optJSONObject(b);
    }

    private static int color(JSONObject o, String key, int fallback) {
        String v = o.optString(key, null);
        return parseCssColor(v, fallback);
    }

    /** Parses #RGB, #RGBA, #RRGGBB or #RRGGBBAA (CSS order) into Android ARGB. */
    static int parseCssColor(String s, int fallback) {
        if (s == null) return fallback;
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            if (s.length() == 3 || s.length() == 4) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    sb.append(s.charAt(i)).append(s.charAt(i));
                }
                s = sb.toString();
            }
            if (s.length() == 6) {
                int rgb = (int) Long.parseLong(s, 16);
                return 0xFF000000 | rgb;
            }
            if (s.length() == 8) {
                long rgba = Long.parseLong(s, 16);
                int alpha = (int) (rgba & 0xFF);
                int rgb = (int) (rgba >>> 8);
                return (alpha << 24) | rgb;
            }
        } catch (NumberFormatException ignored) {
        }
        return fallback;
    }
}
