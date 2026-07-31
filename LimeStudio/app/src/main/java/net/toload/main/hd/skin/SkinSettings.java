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

import java.util.HashMap;
import java.util.Map;

/**
 * Parsed contents of a .cskin skin file (the settings.json inside the zip)
 * reduced to what Sweet LIME applies: light/dark styles, toolbar button
 * configuration, swipe/long-press gesture flags and the per-key swipe maps
 * from lib/swipeData.libsonnet.
 */
public class SkinSettings {

    /** One per-key swipe action from the skin's swipeData map. */
    public static class KeySwipe {
        /** Feed the value through the IM like a keypress (enters composing). */
        public static final int TYPE_CHARACTER = 0;
        /** Commit the value directly to the editor. */
        public static final int TYPE_TEXT = 1;
        /** Named action: cut/copy/paste/selectText/行首/行尾/tab. */
        public static final int TYPE_SHORTCUT = 2;

        public int type;
        public String value;
        /** Short hint drawn on the key cap. */
        public String label;

        public KeySwipe(int type, String value, String label) {
            this.type = type;
            this.value = value;
            this.label = label;
        }
    }

    /** Per-key swipe actions keyed by lowercase letter. */
    public Map<Character, KeySwipe> swipeUp = new HashMap<>();
    public Map<Character, KeySwipe> swipeDown = new HashMap<>();

    /** Per-row gesture switches. Rows are 1-4 top to bottom (index 0-3). */
    public static class RowGesture {
        public boolean swipeUp = true;
        public boolean swipeDown = true;
        public boolean longPress = true;
        public boolean showSwipeUpText = true;
        public boolean showSwipeDownText = true;

        public RowGesture copy() {
            RowGesture r = new RowGesture();
            r.swipeUp = swipeUp;
            r.swipeDown = swipeDown;
            r.longPress = longPress;
            r.showSwipeUpText = showSwipeUpText;
            r.showSwipeDownText = showSwipeDownText;
            return r;
        }
    }

    // Toolbar function ids used in toolbarButtons (from the skin designer).
    public static final int TB_SPACER = 0;
    public static final int TB_SETTINGS = 1;
    public static final int TB_COLLAPSE = 2;
    public static final int TB_CHI_ENG = 3;
    public static final int TB_SIMP_TRAD = 4;
    public static final int TB_PHRASES = 5;
    public static final int TB_CLIPBOARD = 6;
    public static final int TB_SYMBOL = 7;
    public static final int TB_EMOJI = 8;
    public static final int TB_NUMBER = 9;
    public static final int TB_SELECT_ALL = 10;
    public static final int TB_COPY = 11;
    public static final int TB_CUT = 12;
    public static final int TB_PASTE = 13;
    public static final int TB_UNDO = 14;
    public static final int TB_REDO = 15;
    public static final int TB_CURSOR_LEFT = 16;
    public static final int TB_CURSOR_RIGHT = 17;

    public String name = "";
    public String author = "";

    public boolean enableCustomColors = true;

    public SkinStyle light;
    public SkinStyle dark;

    /** 10 slots of toolbar function ids; unsupported ids degrade to spacers. */
    public int[] toolbarButtons = new int[]{1, 0, 0, 3, 4, 5, 6, 7, 8, 2};

    /** Effective per-row gesture flags (global flags merged with advanced row control). */
    public RowGesture[] rows = new RowGesture[]{
            new RowGesture(), new RowGesture(), new RowGesture(), new RowGesture()
    };

    public SkinStyle forNight(boolean night) {
        return night ? dark : light;
    }

    /** Gesture flags for a keyboard row (0-based); defaults for out-of-range rows. */
    public RowGesture rowGesture(int row) {
        if (row < 0 || row >= rows.length) return rows.length > 0 ? rows[rows.length - 1] : new RowGesture();
        return rows[row];
    }

    /** Whole-keyboard swipe-up gestures stay enabled while any row allows them. */
    public boolean anySwipeUp() {
        for (RowGesture r : rows) if (r.swipeUp) return true;
        return false;
    }

    /** Whole-keyboard swipe-down gestures stay enabled while any row allows them. */
    public boolean anySwipeDown() {
        for (RowGesture r : rows) if (r.swipeDown) return true;
        return false;
    }
}
