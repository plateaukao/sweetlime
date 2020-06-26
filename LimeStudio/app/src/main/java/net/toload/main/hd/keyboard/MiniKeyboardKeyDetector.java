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

class MiniKeyboardKeyDetector extends KeyDetector {
    private static final int MAX_NEARBY_KEYS = 1;

    private final int mSlideAllowanceSquare;
    private final int mSlideAllowanceSquareTop;

    public MiniKeyboardKeyDetector(float slideAllowance) {
        super();
        mSlideAllowanceSquare = (int)(slideAllowance * slideAllowance);
        // Top slide allowance is slightly longer (sqrt(2) times) than other edges.
        mSlideAllowanceSquareTop = mSlideAllowanceSquare * 2;
    }

    @Override
    protected int getMaxNearbyKeys() {
        return MAX_NEARBY_KEYS;
    }

    @Override
    public int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys) {
        final Key[] keys = getKeys();
        final int touchX = getTouchX(x);
        final int touchY = getTouchY(y);
        int closestKeyIndex = LIMEKeyboardBaseView.NOT_A_KEY;
        int closestKeyDist = (y < 0) ? mSlideAllowanceSquareTop : mSlideAllowanceSquare;
        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[i];
            int dist = key.squaredDistanceFrom(touchX, touchY);
            if (dist < closestKeyDist) {
                closestKeyIndex = i;
                closestKeyDist = dist;
            }
        }
        if (allKeys != null && closestKeyIndex != LIMEKeyboardBaseView.NOT_A_KEY)
            allKeys[0] = keys[closestKeyIndex].codes[0];
        return closestKeyIndex;
    }
}
