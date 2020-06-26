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

import net.toload.main.hd.R;
import net.toload.main.hd.keyboard.LIMEKeyboardBaseView.OnKeyboardActionListener;
import net.toload.main.hd.keyboard.LIMEKeyboardBaseView.UIHandler;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;

import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;

public class PointerTracker {
    private static final String TAG = "PointerTracker";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_MOVE = false;

    public interface UIProxy {
        public void invalidateKey(Key key);
        public void showPreview(int keyIndex, PointerTracker tracker);
        public boolean hasDistinctMultitouch();
    }

    public final int mPointerId;

    // Timing constants
    private final int mDelayBeforeKeyRepeatStart;
    private final int mLongPressKeyTimeout;
    private final int mMultiTapKeyTimeout;

    // Miscellaneous constants
    private static final int NOT_A_KEY = LIMEKeyboardBaseView.NOT_A_KEY;
    private static final int[] KEY_DELETE = { LIMEBaseKeyboard.KEYCODE_DELETE };

    private final UIProxy mProxy;
    private final UIHandler mHandler;
    private final KeyDetector mKeyDetector;
    private OnKeyboardActionListener mListener;
    //private final LIMEKeyboardSwitcher mKeyboardSwitcher;
    private final boolean mHasDistinctMultitouch;

    private Key[] mKeys;
    private int mKeyHysteresisDistanceSquared = -1;

    private final KeyState mKeyState;

    // true if keyboard layout has been changed.
    private boolean mKeyboardLayoutHasBeenChanged;

    // true if event is already translated to a key action (long press or mini-keyboard)
    private boolean mKeyAlreadyProcessed;

    // true if this pointer is repeatable key
    private boolean mIsRepeatableKey;

    // true if this pointer is in sliding key input
    private boolean mIsInSlidingKeyInput;

    // For multi-tap
    private int mLastSentIndex;
    private int mTapCount;
    private long mLastTapTime;
    private boolean mInMultiTap;
    private final StringBuilder mPreviewLabel = new StringBuilder(1);

    // pressed key
    private int mPreviousKey = NOT_A_KEY;

    // This class keeps track of a key index and a position where this pointer is.
    private static class KeyState {
        private final KeyDetector mKeyDetector;

        // The position and time at which first down event occurred.
        private int mStartX;
        private int mStartY;
        private long mDownTime;

        // The current key index where this pointer is.
        private int mKeyIndex = NOT_A_KEY;
        // The position where mKeyIndex was recognized for the first time.
        private int mKeyX;
        private int mKeyY;

        // Last pointer position.
        private int mLastX;
        private int mLastY;

        public KeyState(KeyDetector keyDetecor) {
            mKeyDetector = keyDetecor;
        }

        public int getKeyIndex() {
            return mKeyIndex;
        }

        public int getKeyX() {
            return mKeyX;
        }

        public int getKeyY() {
            return mKeyY;
        }

        public int getStartX() {
            return mStartX;
        }

        public int getStartY() {
            return mStartY;
        }

        public long getDownTime() {
            return mDownTime;
        }

        public int getLastX() {
            return mLastX;
        }

        public int getLastY() {
            return mLastY;
        }

        public int onDownKey(int x, int y, long eventTime) {
            mStartX = x;
            mStartY = y;
            mDownTime = eventTime;

            return onMoveToNewKey(onMoveKeyInternal(x, y), x, y);
        }

        private int onMoveKeyInternal(int x, int y) {
            mLastX = x;
            mLastY = y;
            return mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null);
        }

        public int onMoveKey(int x, int y) {
            return onMoveKeyInternal(x, y);
        }

        public int onMoveToNewKey(int keyIndex, int x, int y) {
            mKeyIndex = keyIndex;
            mKeyX = x;
            mKeyY = y;
            return keyIndex;
        }

        public int onUpKey(int x, int y) {
            return onMoveKeyInternal(x, y);
        }
    }

    public PointerTracker(int id, UIHandler handler, KeyDetector keyDetector, UIProxy proxy,
            Resources res) {
        if (proxy == null || handler == null || keyDetector == null)
            throw new NullPointerException();
        mPointerId = id;
        mProxy = proxy;
        mHandler = handler;
        mKeyDetector = keyDetector;
       // mKeyboardSwitcher = null;//LIMEKeyboardSwitcher.getInstance();
        mKeyState = new KeyState(keyDetector);
        mHasDistinctMultitouch = proxy.hasDistinctMultitouch();
        mDelayBeforeKeyRepeatStart = res.getInteger(R.integer.config_delay_before_key_repeat_start);
        mLongPressKeyTimeout = res.getInteger(R.integer.config_long_press_key_timeout);
        mMultiTapKeyTimeout = res.getInteger(R.integer.config_multi_tap_key_timeout);
        resetMultiTap();
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mListener = listener;
    }

    public void setKeyboard(Key[] keys, float keyHysteresisDistance) {
        if (keys == null || keyHysteresisDistance < 0)
            throw new IllegalArgumentException();
        mKeys = keys;
        mKeyHysteresisDistanceSquared = (int)(keyHysteresisDistance * keyHysteresisDistance);
        // Mark that keyboard layout has been changed.
        mKeyboardLayoutHasBeenChanged = true;
    }

    public boolean isInSlidingKeyInput() {
        return mIsInSlidingKeyInput;
    }

    private boolean isValidKeyIndex(int keyIndex) {
        return keyIndex >= 0 && keyIndex < mKeys.length;
    }

    public Key getKey(int keyIndex) {
        return isValidKeyIndex(keyIndex) ? mKeys[keyIndex] : null;
    }

    private boolean isModifierInternal(int keyIndex) {
        Key key = getKey(keyIndex);
        if (key == null)
            return false;
        int primaryCode = key.codes[0];
        return primaryCode == LIMEBaseKeyboard.KEYCODE_SHIFT
                || primaryCode == LIMEBaseKeyboard.KEYCODE_MODE_CHANGE;
    }

    public boolean isModifier() {
        return isModifierInternal(mKeyState.getKeyIndex());
    }

    public boolean isFunctionalKey(){
        int keyIndex = mKeyState.getKeyIndex();
        Key key = getKey( keyIndex);
        if(key==null) return false;
        else
            return key.isFunctionalKey() ;
    }

    public boolean isOnModifierKey(int x, int y) {
        return isModifierInternal(mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null));
    }

    public boolean isSpaceKey(int keyIndex) {
        Key key = getKey(keyIndex);
        return key != null && key.codes[0] == ' ';
    }

    public void updateKey(int keyIndex) {
        if (mKeyAlreadyProcessed)
            return;
        int oldKeyIndex = mPreviousKey;
        mPreviousKey = keyIndex;
        if (keyIndex != oldKeyIndex) {
            if (isValidKeyIndex(oldKeyIndex)) {
                // if new key index is not a key, old key was just released inside of the key.
                final boolean inside = (keyIndex == NOT_A_KEY);
                mKeys[oldKeyIndex].onReleased(inside);
                mProxy.invalidateKey(mKeys[oldKeyIndex]);
            }
            if (isValidKeyIndex(keyIndex)) {
                mKeys[keyIndex].onPressed();
                mProxy.invalidateKey(mKeys[keyIndex]);
            }
        }
    }

    public void setAlreadyProcessed() {
        mKeyAlreadyProcessed = true;
    }

    public void onTouchEvent(int action, int x, int y, long eventTime) {
        switch (action) {
        case MotionEvent.ACTION_MOVE:
            onMoveEvent(x, y, eventTime);
            break;
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            onDownEvent(x, y, eventTime);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            onUpEvent(x, y, eventTime);
            break;
        case MotionEvent.ACTION_CANCEL:
            onCancelEvent(x, y, eventTime);
            break;
        }
    }

    public void onDownEvent(int x, int y, long eventTime) {
        if (DEBUG)
            Log.i(TAG,"onDownEvent(): x = "+ x + ", y="+ y);
        if (DEBUG_MOVE)
            debugLog("onDownEvent:", x, y);
        int keyIndex = mKeyState.onDownKey(x, y, eventTime);
        mKeyboardLayoutHasBeenChanged = false;
        mKeyAlreadyProcessed = false;
        mIsRepeatableKey = false;
        mIsInSlidingKeyInput = false;
        checkMultiTap(eventTime, keyIndex);
        if (mListener != null) {
            if (isValidKeyIndex(keyIndex)) {
                mListener.onPress(mKeys[keyIndex].codes[0]);
                // This onPress call may have changed keyboard layout. Those cases are detected at
                // {@link #setKeyboard}. In those cases, we should update keyIndex according to the
                // new keyboard layout.
                if (mKeyboardLayoutHasBeenChanged) {
                    mKeyboardLayoutHasBeenChanged = false;
                    keyIndex = mKeyState.onDownKey(x, y, eventTime);
                }
            }
        }
        if (isValidKeyIndex(keyIndex)) {
            if (mKeys[keyIndex].repeatable && mKeys[keyIndex].codes[0]!= LIMEKeyboard.KEYCODE_SPACE) {
                repeatKey(keyIndex);
                mHandler.startKeyRepeatTimer(mDelayBeforeKeyRepeatStart, keyIndex, this);
                mIsRepeatableKey = true;
            }
            startLongPressTimer(keyIndex);
        }
        showKeyPreviewAndUpdateKey(keyIndex);
    }

    public void onMoveEvent(int x, int y, long eventTime) {
        if (DEBUG)
            Log.i(TAG,"onMoveEvent(): x = "+ x + ", y="+ y);
        if (DEBUG_MOVE)
            debugLog("onMoveEvent:", x, y);
        if (mKeyAlreadyProcessed)
            return;
        final KeyState keyState = mKeyState;
        int keyIndex = keyState.onMoveKey(x, y);
        final Key oldKey = getKey(keyState.getKeyIndex());
        if (isValidKeyIndex(keyIndex)) {
            if (oldKey == null) {
                // The pointer has been slid in to the new key, but the finger was not on any keys.
                // In this case, we must call onPress() to notify that the new key is being pressed.
                if (mListener != null) {
                    mListener.onPress(getKey(keyIndex).codes[0]);
                    // This onPress call may have changed keyboard layout. Those cases are detected
                    // at {@link #setKeyboard}. In those cases, we should update keyIndex according
                    // to the new keyboard layout.
                    if (mKeyboardLayoutHasBeenChanged) {
                        mKeyboardLayoutHasBeenChanged = false;
                        keyIndex = keyState.onMoveKey(x, y);
                    }
                }
                keyState.onMoveToNewKey(keyIndex, x, y);
                startLongPressTimer(keyIndex);
            } else if (!isMinorMoveBounce(x, y, keyIndex)) {
                // The pointer has been slid in to the new key from the previous key, we must call
                // onRelease() first to notify that the previous key has been released, then call
                // onPress() to notify that the new key is being pressed.
                mIsInSlidingKeyInput = true;
                if (mListener != null)
                    mListener.onRelease(oldKey.codes[0]);
                resetMultiTap();
                if (mListener != null) {
                    mListener.onPress(getKey(keyIndex).codes[0]);
                    // This onPress call may have changed keyboard layout. Those cases are detected
                    // at {@link #setKeyboard}. In those cases, we should update keyIndex according
                    // to the new keyboard layout.
                    if (mKeyboardLayoutHasBeenChanged) {
                        mKeyboardLayoutHasBeenChanged = false;
                        keyIndex = keyState.onMoveKey(x, y);
                    }
                }
                keyState.onMoveToNewKey(keyIndex, x, y);
                startLongPressTimer(keyIndex);
            }
        } else {
            if (oldKey != null && !isMinorMoveBounce(x, y, keyIndex)) {
                // The pointer has been slid out from the previous key, we must call onRelease() to
                // notify that the previous key has been released.
                mIsInSlidingKeyInput = true;
                if (mListener != null)
                    mListener.onRelease(oldKey.codes[0]);
                resetMultiTap();
                keyState.onMoveToNewKey(keyIndex, x ,y);
                mHandler.cancelLongPressTimer();
            }
        }
        showKeyPreviewAndUpdateKey(keyState.getKeyIndex());
    }

    public void onUpEvent(int x, int y, long eventTime) {
        if (DEBUG)
            Log.i(TAG,"onUpEvent(): x = "+ x + ", y="+ y);
        if (DEBUG_MOVE)
            debugLog("onUpEvent  :", x, y);
        mHandler.cancelKeyTimers();
        //mHandler.cancelPopupPreview();
        showKeyPreviewAndUpdateKey(NOT_A_KEY);
        mIsInSlidingKeyInput = false;
        if (mKeyAlreadyProcessed)
            return;
        int keyIndex = mKeyState.onUpKey(x, y);
        if (isMinorMoveBounce(x, y, keyIndex)) {
            // Use previous fixed key index and coordinates.
            keyIndex = mKeyState.getKeyIndex();
            x = mKeyState.getKeyX();
            y = mKeyState.getKeyY();
        }
        if (!mIsRepeatableKey) {
            detectAndSendKey(keyIndex, x, y, eventTime);
        }

        if (isValidKeyIndex(keyIndex))
            mProxy.invalidateKey(mKeys[keyIndex]);
    }

    public void onCancelEvent(int x, int y, long eventTime) {
        if (DEBUG)
            Log.i(TAG,"onCancelEvent(): x = "+ x + ", y="+ y);
        if (DEBUG_MOVE)
            debugLog("onCancelEvent(): ", x, y);
        mHandler.cancelKeyTimers();
        mHandler.cancelPopupPreview();
        showKeyPreviewAndUpdateKey(NOT_A_KEY);
        mIsInSlidingKeyInput = false;
        int keyIndex = mKeyState.getKeyIndex();
        if (isValidKeyIndex(keyIndex))
           mProxy.invalidateKey(mKeys[keyIndex]);
    }

    public void repeatKey(int keyIndex) {
        Key key = getKey(keyIndex);
        if (key != null) {
            // While key is repeating, because there is no need to handle multi-tap key, we can
            // pass -1 as eventTime argument.
            detectAndSendKey(keyIndex, key.x, key.y, -1);
        }
    }

    public int getLastX() {
        return mKeyState.getLastX();
    }

    public int getLastY() {
        return mKeyState.getLastY();
    }

    public long getDownTime() {
        return mKeyState.getDownTime();
    }

    // These package scope methods are only for debugging purpose.
    /* package */ int getStartX() {
        return mKeyState.getStartX();
    }

    /* package */ int getStartY() {
        return mKeyState.getStartY();
    }

    private boolean isMinorMoveBounce(int x, int y, int newKey) {
        if (mKeys == null || mKeyHysteresisDistanceSquared < 0)
            throw new IllegalStateException("keyboard and/or hysteresis not set");
        int curKey = mKeyState.getKeyIndex();
        if (newKey == curKey) {
            return true;
        } else if (isValidKeyIndex(curKey)) {
            return getSquareDistanceToKeyEdge(x, y, mKeys[curKey]) < mKeyHysteresisDistanceSquared;
        } else {
            return false;
        }
    }

    private static int getSquareDistanceToKeyEdge(int x, int y, Key key) {
        final int left = key.x;
        final int right = key.x + key.width;
        final int top = key.y;
        final int bottom = key.y + key.height;
        final int edgeX = x < left ? left : (x > right ? right : x);
        final int edgeY = y < top ? top : (y > bottom ? bottom : y);
        final int dx = x - edgeX;
        final int dy = y - edgeY;
        return dx * dx + dy * dy;
    }

    private void showKeyPreviewAndUpdateKey(int keyIndex) {
        if(DEBUG)
            Log.i(TAG, "showKeyPreviewAndUpdateKey() keyIndex=" + keyIndex + ", isModifier() = "+ isModifier() );
        updateKey(keyIndex);
        // The modifier key, such as shift key, should not be shown as preview when multi-touch is
        // supported. On the other hand, if multi-touch is not supported, the modifier key should
        // be shown as preview.
        //  Jeremy '15,6,5 do not show preview on all functional keys.
        // '16,7,13 exclude space key in isFunctionalKey() for sliding IM preview.
        if (mHasDistinctMultitouch && isFunctionalKey() && !isSpaceKey(keyIndex)){
            mProxy.showPreview(NOT_A_KEY, this);
        } else {
            mProxy.showPreview(keyIndex, this);
       }
    }

    private void startLongPressTimer(int keyIndex) {
        //if (mKeyboardSwitcher.isInMomentaryAutoModeSwitchState()) {
            // We use longer timeout for sliding finger input started from the symbols mode key.
         //   mHandler.startLongPressTimer(mLongPressKeyTimeout * 3, keyIndex, this);
        //} else {
            mHandler.startLongPressTimer(mLongPressKeyTimeout, keyIndex, this);
        //}
    }

    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        final OnKeyboardActionListener listener = mListener;
        final Key key = getKey(index);

        if (key == null) {
            if (listener != null)
                listener.onCancel();
        } else {
            if (key.text != null) {
                if (listener != null) {
                    listener.onText(key.text);
                    listener.onRelease(0); // dummy key code
                }
            } else {
                int code = key.codes[0];
                int[] codes = mKeyDetector.newCodeArray();
                mKeyDetector.getKeyIndexAndNearbyCodes(x, y, codes);
                // Multi-tap
                if (mInMultiTap) {
                    if (mTapCount != -1) {
                        mListener.onKey(LIMEBaseKeyboard.KEYCODE_DELETE, KEY_DELETE, x, y);
                    } else {
                        mTapCount = 0;
                    }
                    code = key.codes[mTapCount];
                }
                /*
                 * Swap the first and second values in the codes array if the primary code is not
                 * the first value but the second value in the array. This happens when key
                 * debouncing is in effect.
                 */
                if (codes.length >= 2 && codes[0] != code && codes[1] == code) {
                    codes[1] = codes[0];
                    codes[0] = code;
                }
                if (listener != null) {
                    listener.onKey(code, codes, x, y);
                    listener.onRelease(code);
                }
            }
            mLastSentIndex = index;
            mLastTapTime = eventTime;
        }
    }

    /**
     * Handle multi-tap keys by producing the key label for the current multi-tap state.
     */
    public CharSequence getPreviewText(Key key) {
        if (mInMultiTap) {
            // Multi-tap
            mPreviewLabel.setLength(0);
            mPreviewLabel.append((char) key.codes[mTapCount < 0 ? 0 : mTapCount]);
            return mPreviewLabel;
        } else {
            return key.label;
        }
    }

    private void resetMultiTap() {
        mLastSentIndex = NOT_A_KEY;
        mTapCount = 0;
        mLastTapTime = -1;
        mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        Key key = getKey(keyIndex);
        if (key == null)
            return;

        final boolean isMultiTap =
                (eventTime < mLastTapTime + mMultiTapKeyTimeout && keyIndex == mLastSentIndex);
        if (key.codes.length > 1) {
            mInMultiTap = true;
            if (isMultiTap) {
                mTapCount = (mTapCount + 1) % key.codes.length;
                return;
            } else {
                mTapCount = -1;
                return;
            }
        }
        if (!isMultiTap) {
            resetMultiTap();
        }
    }

    private void debugLog(String title, int x, int y) {
        int keyIndex = mKeyDetector.getKeyIndexAndNearbyCodes(x, y, null);
        Key key = getKey(keyIndex);
        final String code;
        if (key == null) {
            code = "----";
        } else {
            int primaryCode = key.codes[0];
            code = String.format((primaryCode < 0) ? "%4d" : "0x%02x", primaryCode);
        }
        Log.d(TAG, String.format("%s%s[%d] %3d,%3d %3d(%s) %s", title,
                (mKeyAlreadyProcessed ? "-" : " "), mPointerId, x, y, keyIndex, code,
                (isModifier() ? "modifier" : "")));
    }
}
