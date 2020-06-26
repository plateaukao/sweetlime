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

package net.toload.main.hd.keyboard;


import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;

import java.util.Arrays;
import java.util.List;



abstract class KeyDetector {
    protected LIMEBaseKeyboard mKeyboard;

    private Key[] mKeys;

    protected int mCorrectionX;

    protected int mCorrectionY;

    protected boolean mProximityCorrectOn;

    protected int mProximityThresholdSquare;

    public Key[] setKeyboard(LIMEBaseKeyboard keyboard, float correctionX, float correctionY) {
        if (keyboard == null)
            throw new NullPointerException();
        mCorrectionX = (int)correctionX;
        mCorrectionY = (int)correctionY;
        mKeyboard = keyboard;
        List<Key> keys = mKeyboard.getKeys();
        Key[] array = keys.toArray(new Key[keys.size()]);
        mKeys = array;
        return array;
    }

    protected int getTouchX(int x) {
        return x + mCorrectionX;
    }

    protected int getTouchY(int y) {
        return y + mCorrectionY;
    }

    protected Key[] getKeys() {
        if (mKeys == null)
            throw new IllegalStateException("keyboard isn't set");
        // mKeyboard is guaranteed not to be null at setKeybaord() method if mKeys is not null
        return mKeys;
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        mProximityCorrectOn = enabled;
    }

    public boolean isProximityCorrectionEnabled() {
        return mProximityCorrectOn;
    }

    public void setProximityThreshold(int threshold) {
        mProximityThresholdSquare = threshold * threshold;
    }

    /**
     * Allocates array that can hold all key indices returned by {@link #getKeyIndexAndNearbyCodes}
     * method. The maximum size of the array should be computed by {@link #getMaxNearbyKeys}.
     *
     * @return Allocates and returns an array that can hold all key indices returned by
     *         {@link #getKeyIndexAndNearbyCodes} method. All elements in the returned array are
     *         initialized by {@link com.android.inputmethod.latin.LatinKeyboardView.NOT_A_KEY}
     *         value.
     */
    public int[] newCodeArray() {
        int[] codes = new int[getMaxNearbyKeys()];
        Arrays.fill(codes, LIMEKeyboardBaseView.NOT_A_KEY);
        return codes;
    }

    /**
     * Computes maximum size of the array that can contain all nearby key indices returned by
     * {@link #getKeyIndexAndNearbyCodes}.
     *
     * @return Returns maximum size of the array that can contain all nearby key indices returned
     *         by {@link #getKeyIndexAndNearbyCodes}.
     */
    abstract protected int getMaxNearbyKeys();

    /**
     * Finds all possible nearby key indices around a touch event point and returns the nearest key
     * index. The algorithm to determine the nearby keys depends on the threshold set by
     * {@link #setProximityThreshold(int)} and the mode set by
     * {@link #setProximityCorrectionEnabled(boolean)}.
     *
     * @param x The x-coordinate of a touch point
     * @param y The y-coordinate of a touch point
     * @param allKeys All nearby key indices are returned in this array
     * @return The nearest key index
     */
    abstract public int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys);
}
