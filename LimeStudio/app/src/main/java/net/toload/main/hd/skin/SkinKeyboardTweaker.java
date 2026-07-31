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

import net.toload.main.hd.keyboard.LIMEBaseKeyboard;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes bottom-row keys that the skin toolbar already covers (EN/中
 * switch, 123 symbol keyboard) and hands their width to the space bar,
 * like the 元書 layouts do. Applied to freshly built keyboards while the
 * custom skin is active; only main text keyboards (those with letter
 * keys) are touched, so symbol/number layouts keep their return keys.
 */
public class SkinKeyboardTweaker {

    private static final int KEYCODE_SPACE = 32;
    private static final int KEYCODE_LETTER_Q = 113;
    private static final int KEYCODE_SWITCH_TO_SYMBOL_MODE = -2;
    private static final int KEYCODE_SWITCH_TO_ENGLISH_MODE = -9;
    private static final int KEYCODE_SWITCH_TO_IM_MODE = -10;

    private SkinKeyboardTweaker() {
    }

    public static void apply(LIMEBaseKeyboard keyboard, Context context) {
        SkinSettings skin = SkinManager.getInstance().getActiveSkin(context);
        if (skin == null) return;

        boolean toolbarChiEng = hasToolbarFunction(skin, SkinSettings.TB_CHI_ENG);
        boolean toolbarSymbols = hasToolbarFunction(skin, SkinSettings.TB_SYMBOL)
                || hasToolbarFunction(skin, SkinSettings.TB_NUMBER);
        if (!toolbarChiEng && !toolbarSymbols) return;

        List<Key> keys = keyboard.getKeys();
        if (!hasKeyCode(keys, KEYCODE_LETTER_Q)) return; // not a main text keyboard

        List<Key> toRemove = new ArrayList<>();
        for (Key key : keys) {
            int code = primaryCode(key);
            if (toolbarChiEng && (code == KEYCODE_SWITCH_TO_ENGLISH_MODE
                    || code == KEYCODE_SWITCH_TO_IM_MODE))
                toRemove.add(key);
            else if (toolbarSymbols && code == KEYCODE_SWITCH_TO_SYMBOL_MODE)
                toRemove.add(key);
        }
        for (Key key : toRemove)
            removeKeyAndWidenSpace(keys, key);
    }

    private static boolean hasToolbarFunction(SkinSettings skin, int function) {
        for (int f : skin.toolbarButtons)
            if (f == function) return true;
        return false;
    }

    private static boolean hasKeyCode(List<Key> keys, int code) {
        for (Key key : keys)
            if (primaryCode(key) == code) return true;
        return false;
    }

    private static int primaryCode(Key key) {
        return (key.codes == null || key.codes.length == 0)
                ? Integer.MIN_VALUE : key.codes[0];
    }

    /**
     * Drops the key from the list and gives its footprint (gap + width) to
     * the space key of the same row, shifting the keys in between so the
     * row stays flush.
     */
    private static void removeKeyAndWidenSpace(List<Key> keys, Key removed) {
        int d = removed.gap + removed.width;
        Key space = null;
        for (Key key : keys) {
            if (key.y == removed.y && primaryCode(key) == KEYCODE_SPACE) {
                space = key;
                break;
            }
        }
        if (space != null) {
            if (removed.x < space.x) {
                for (Key key : keys) {
                    if (key.y == removed.y && key.x > removed.x && key.x < space.x)
                        key.x -= d;
                }
                space.x -= d;
                space.width += d;
            } else {
                for (Key key : keys) {
                    if (key.y == removed.y && key.x > space.x && key.x < removed.x)
                        key.x += d;
                }
                space.width += d;
            }
        } else {
            for (Key key : keys) {
                if (key.y == removed.y && key.x > removed.x)
                    key.x -= d;
            }
        }
        keys.remove(removed);
    }
}
