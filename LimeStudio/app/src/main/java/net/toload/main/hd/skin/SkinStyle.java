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

/**
 * Resolved style values for one appearance mode (light or dark) of an
 * imported .cskin skin. All colors are Android ARGB ints; sizes are the
 * designer's point values, treated as dp/sp on Android.
 */
public class SkinStyle {

    // Backgrounds
    public int keyboardBackground;
    public int toolbarBackground;

    // Key faces: normal (letter) keys, system/function keys, enter key
    public int keyNormal;
    public int keyNormalPressed;
    public int keySystem;
    public int keySystemPressed;
    public int keyEnter;
    public int keyEnterPressed;

    // Text
    public int textMain;
    public int textSub;
    /** Function-key label color (v2 skins); falls back to textMain. */
    public int textSystem;
    public int toolbarColor;

    // Candidate bar
    public int candidateSelectedText;
    public int candidateUnselectedText;
    public int candidateSelectedBackground;

    // Key preview bubble
    public int bubbleTextSelected;
    public int bubbleTextUnselected;

    // Effects
    public int shadow;
    public int borderColor;
    public float borderSize;
    /** Function-key border (v2 skins); falls back to borderColor/borderSize. */
    public int systemBorderColor;
    public float systemBorderSize;

    // Font sizes (designer points; use as sp)
    public float alphabetSize;
    public float lowercaseSize;
    public float systemSize;
    public float spaceSize;
    public float numberSize;
    public float toolbarSize;
    public float swipeSize;
}
