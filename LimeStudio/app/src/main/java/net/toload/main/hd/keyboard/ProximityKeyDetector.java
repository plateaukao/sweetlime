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



class ProximityKeyDetector extends KeyDetector {
    private static final int MAX_NEARBY_KEYS = 12;

    // working area
    private int[] mDistances = new int[MAX_NEARBY_KEYS];

    @Override
    protected int getMaxNearbyKeys() {
        return MAX_NEARBY_KEYS;
    }

    @Override
    public int getKeyIndexAndNearbyCodes(int x, int y, int[] allKeys) {
        final Key[] keys = getKeys();
        final int touchX = getTouchX(x);
        final int touchY = getTouchY(y);
        int primaryIndex = LIMEKeyboardBaseView.NOT_A_KEY;
        int closestKey = LIMEKeyboardBaseView.NOT_A_KEY;
        int closestKeyDist = mProximityThresholdSquare + 1;
        int[] distances = mDistances;
        Arrays.fill(distances, Integer.MAX_VALUE);
        int [] nearestKeyIndices = mKeyboard.getNearestKeys(touchX, touchY);
        final int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[nearestKeyIndices[i]];
            int dist = 0;
            boolean isInside = key.isInside(touchX, touchY);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }

            if (((mProximityCorrectOn
                    && (dist = key.squaredDistanceFrom(touchX, touchY)) < mProximityThresholdSquare)
                    || isInside)
                    && key.codes[0] > 32) {
                // Find insertion point
                final int nCodes = key.codes.length;
                if (dist < closestKeyDist) {
                    closestKeyDist = dist;
                    closestKey = nearestKeyIndices[i];
                }

                if (allKeys == null) continue;

                for (int j = 0; j < distances.length; j++) {
                    if (distances[j] > dist) {
                        // Make space for nCodes codes
                        System.arraycopy(distances, j, distances, j + nCodes,
                                distances.length - j - nCodes);
                        System.arraycopy(allKeys, j, allKeys, j + nCodes,
                                allKeys.length - j - nCodes);
                        System.arraycopy(key.codes, 0, allKeys, j, nCodes);
                        Arrays.fill(distances, j, j + nCodes, dist);
                        break;
                    }
                }
            }
        }
        if (primaryIndex == LIMEKeyboardBaseView.NOT_A_KEY) {
            primaryIndex = closestKey;
        }
        return primaryIndex;
    }
}
