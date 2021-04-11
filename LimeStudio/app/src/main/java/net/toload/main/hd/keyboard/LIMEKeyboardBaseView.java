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

/*
 * Jeremy '11,8,8
 * Derive from gingerbread Latin IME LatinKeyboardBaseView, 
 * modified to compatible with pre 2.2 devices, and disable
 * fling selection of popup minikeybaord on large screen.
 * 
 */

package net.toload.main.hd.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import net.toload.main.hd.R;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

@SuppressLint("UseSparseArrays")
public class LIMEKeyboardBaseView extends View implements PointerTracker.UIProxy {
    private static final String TAG = "LIMEKeyboardBaseView";
    private static final boolean DEBUG = false;

    public static final int NOT_A_TOUCH_COORDINATE = -1;

    public interface OnKeyboardActionListener {

        /**
         * Called when the user presses a key. This is sent before the
         * {@link #onKey} is called. For keys that repeat, this is only
         * called once.
         *
         * @param primaryCode the unicode of the key being pressed. If the touch is
         *                    not on a valid key, the value will be zero.
         */
        void onPress(int primaryCode);

        /**
         * Called when the user releases a key. This is sent after the
         * {@link #onKey} is called. For keys that repeat, this is only
         * called once.
         *
         * @param primaryCode the code of the key that was released
         */
        void onRelease(int primaryCode);

        /**
         * Send a key press to the listener.
         *
         * @param primaryCode this is the key that was pressed
         * @param keyCodes    the codes for all the possible alternative keys with
         *                    the primary code being the first. If the primary key
         *                    code is a single character such as an alphabet or
         *                    number or symbol, the alternatives will include other
         *                    characters that may be on the same key or adjacent
         *                    keys. These codes are useful to correct for
         *                    accidental presses of a key adjacent to the intended
         *                    key.
         * @param x           x-coordinate pixel of touched event. If onKey is not called by onTouchEvent,
         *                    the value should be NOT_A_TOUCH_COORDINATE.
         * @param y           y-coordinate pixel of touched event. If onKey is not called by onTouchEvent,
         *                    the value should be NOT_A_TOUCH_COORDINATE.
         */
        void onKey(int primaryCode, int[] keyCodes, int x, int y);

        /**
         * Sends a sequence of characters to the listener.
         *
         * @param text the sequence of characters to be displayed.
         */
        void onText(CharSequence text);

        /**
         * Called when user released a finger outside any key.
         */
        void onCancel();

        /**
         * Called when the user quickly moves the finger from right to
         * left.
         */
        void swipeLeft();

        /**
         * Called when the user quickly moves the finger from left to
         * right.
         */
        void swipeRight();

        /**
         * Called when the user quickly moves the finger from up to down.
         */
        void swipeDown();

        /**
         * Called when the user quickly moves the finger from down to up.
         */
        void swipeUp();
    }

    //themed context
    Context mContext;

    // Timing constants
    private final int mKeyRepeatInterval;

    // Miscellaneous constants
    /* package */ static final int NOT_A_KEY = -1;
    private static final int[] LONG_PRESSABLE_STATE_SET = {android.R.attr.state_long_pressable};
    private static final int NUMBER_HINT_VERTICAL_ADJUSTMENT_PIXEL = -1;

    // XML attribute
    private int mKeyTextSize;
    private int mKeyTextColorNormal;
    private int mKeyTextColorPressed; //Jeremy '15,5,13
    private int mFunctionKeyTextColorNormal;
    private int mFunctionKeyTextColorPressed; //Jeremy '15,5,13
    private int mKeySubLabelTextColorNormal; //Jeremy '12,4,29
    private int mKeySubLabelTextColorPressed; //Jeremy '15,5,13
    private Typeface mKeyTextStyle = Typeface.DEFAULT;
    private int mLabelTextSize;
    private int mSmallLabelTextSize;
    private int mSubLabelTextSize;
    private int mSymbolColorScheme = 0;
    private int mShadowColor;
    private float mShadowRadius;
    private Drawable mKeyBackground;
    private float mBackgroundDimAmount;
    private float mKeyHysteresisDistance;
    private float mVerticalCorrection;
    private int mPreviewOffset;
    private int mPreviewHeight;
    private int mPopupLayout;
    private int mSpacePreviewTopPadding;
    private int mPreviewTopPadding;

    private boolean mtHardwareAcceleratedDrawingEnabled = false;


    // Main keyboard
    private LIMEBaseKeyboard mKeyboard;
    private Key[] mKeys;
    // TODO this attribute should be gotten from Keyboard.
    private int mKeyboardVerticalGap;

    // Key preview popup
    private PopupWindow mPreviewPopup;
    private int mPreviewTextSizeLarge;
    private int[] mOffsetInWindow;
    private int mOldPreviewKeyIndex = NOT_A_KEY;
    private boolean mShowPreview = true;
    private boolean mShowTouchPoints = true;
    private int mPopupPreviewOffsetX;
    private int mPopupPreviewOffsetY;
    private int mWindowY;
    private int mPopupPreviewDisplayedY;
    private final int mDelayBeforePreview;
    private final int mDelayAfterPreview;

    // Popup mini keyboard
    private PopupWindow mMiniKeyboardPopup;
    private LIMEKeyboardBaseView mMiniKeyboard;
    private View mMiniKeyboardParent;
    private final WeakHashMap<Key, View> mMiniKeyboardCache = new WeakHashMap<>();
    private int mMiniKeyboardOriginX;
    private int mMiniKeyboardOriginY;
    private long mMiniKeyboardPopupTime;
    private int[] mWindowOffset;
    private final float mMiniKeyboardSlideAllowance;
    private int mMiniKeyboardTrackerId;

    //key preview animation
    private Animation mKeyPreviewFadeInAnimator;
    private Animation mKeyPreviewFadeOutAnimator;

    /**
     * Listener for {@link OnKeyboardActionListener}.
     */
    private OnKeyboardActionListener mKeyboardActionListener;

    private final ArrayList<PointerTracker> mPointerTrackers = new ArrayList<>();

    // TODO: Let the PointerTracker class manage this pointer queue
    private final PointerQueue mPointerQueue = new PointerQueue();

    private final boolean mHasDistinctMultitouch;
    private int mOldPointerCount = 1;

    protected KeyDetector mKeyDetector = new ProximityKeyDetector();

    // Swipe gesture detector
    private GestureDetector mGestureDetector;
    private final SwipeTracker mSwipeTracker = new SwipeTracker();
    private final int mSwipeThreshold;
    private final boolean mDisambiguateSwipe;

    // Drawing
    /**
     * Whether the keyboard bitmap needs to be redrawn before it's blitted. *
     */
    private boolean mDrawPending;
    /**
     * The dirty region in the keyboard bitmap
     */
    private final Rect mDirtyRect = new Rect();
    /**
     * The keyboard bitmap for faster updates
     */
    private Bitmap mBuffer;
    /**
     * Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.
     */
    private boolean mKeyboardChanged;
    private Key mInvalidatedKey;
    /**
     * The canvas for the above mutable keyboard bitmap
     */
    private Canvas mCanvas;
    private final Paint mPaint;
    private final Rect mPadding;
    private final Rect mClipRegion = new Rect(0, 0, 0, 0);
    // This map caches key label text height in pixel as value and key label text size as map key.
    private final HashMap<Integer, Integer> mTextHeightCache = new HashMap<>();
    private final HashMap<Integer, Integer> mTextWidthCache = new HashMap<>();

    private Drawable mPopupHint;//Jeremy /11,8,11

    private boolean isLargeScreen; // Jeremy //11,8,8 used for disable fling selection on minipopup keyboard for larger screen

    private final UIHandler mHandler = new UIHandler(this);

    //private LIMEPreferenceManager mLIMEPref;

    static class UIHandler extends Handler {
        private static final int MSG_POPUP_PREVIEW = 1;
        private static final int MSG_DISMISS_PREVIEW = 2;
        private static final int MSG_REPEAT_KEY = 3;
        private static final int MSG_LONGPRESS_KEY = 4;
        private static final int MSG_SHOW_PREVIEW = 5;

        private boolean mInKeyRepeat;

        private final WeakReference<LIMEKeyboardBaseView> mLIMEKeyboardBaseViewWeakReference;

        public UIHandler(LIMEKeyboardBaseView keyboardBaseView){
            mLIMEKeyboardBaseViewWeakReference = new WeakReference<LIMEKeyboardBaseView>(keyboardBaseView);
        }

        @Override
        public void handleMessage(Message msg) {
            LIMEKeyboardBaseView mLIMEKeyboardBaseView = mLIMEKeyboardBaseViewWeakReference.get();
            if(mLIMEKeyboardBaseView == null) return;
            switch (msg.what) {
                case MSG_REPEAT_KEY: {
                    if(DEBUG) Log.i(TAG, "handleMessage()  MSG_REPEAT_KEY");
                    final PointerTracker tracker = (PointerTracker) msg.obj;
                    tracker.repeatKey(msg.arg1);
                    startKeyRepeatTimer(mLIMEKeyboardBaseView.mKeyRepeatInterval, msg.arg1, tracker);
                    break;
                }
                case MSG_LONGPRESS_KEY: {
                    if(DEBUG) Log.i(TAG, "handleMessage()  MSG_LONGPRESS_KEY");
                    final PointerTracker tracker = (PointerTracker) msg.obj;
                    mLIMEKeyboardBaseView.openPopupIfRequired(msg.arg1, tracker);
                    break;
                }
            }
        }

        public void startKeyRepeatTimer(long delay, int keyIndex, PointerTracker tracker) {
            if(DEBUG)
                Log.i(TAG, "UIHandler.startKeyRepeatTimer() delay=" + delay + "keyIndex= " + keyIndex);
            mInKeyRepeat = true;
            sendMessageDelayed(obtainMessage(MSG_REPEAT_KEY, keyIndex, 0, tracker), delay);
        }

        public void cancelKeyRepeatTimer() {
            if(DEBUG)
                Log.i(TAG, "UIHandler.cancelKeyRepeatTimer()");
            mInKeyRepeat = false;
            removeMessages(MSG_REPEAT_KEY);
        }

        public boolean isInKeyRepeat() {
            if(DEBUG)
                Log.i(TAG, "UIHandler.isInKeyRepeat(): " + mInKeyRepeat);
            return mInKeyRepeat;
        }

        public void startLongPressTimer(long delay, int keyIndex, PointerTracker tracker) {
            if(DEBUG)
                Log.i(TAG, "UIHandler.startLongPressTimer() delay=" + delay + "keyIndex= " + keyIndex);
            removeMessages(MSG_LONGPRESS_KEY);
            sendMessageDelayed(obtainMessage(MSG_LONGPRESS_KEY, keyIndex, 0, tracker), delay);
        }

        public void cancelLongPressTimer() {
            if(DEBUG)
                Log.i(TAG, "UIHandler.cancelLongPressTimer()");
            removeMessages(MSG_LONGPRESS_KEY);
        }

        public void cancelKeyTimers() {
            cancelKeyRepeatTimer();
            cancelLongPressTimer();
        }

        public void cancelAllMessages() {
            cancelKeyTimers();
        }
    }

    static class PointerQueue {
        private LinkedList<PointerTracker> mQueue = new LinkedList<>();

        public void add(PointerTracker tracker) {
            mQueue.add(tracker);
        }

        public int lastIndexOf(PointerTracker tracker) {
            LinkedList<PointerTracker> queue = mQueue;
            for (int index = queue.size() - 1; index >= 0; index--) {
                PointerTracker t = queue.get(index);
                if (t == tracker)
                    return index;
            }
            return -1;
        }

        public void releaseAllPointersOlderThan(PointerTracker tracker, long eventTime) {
            LinkedList<PointerTracker> queue = mQueue;
            int oldestPos = 0;
            for (PointerTracker t = queue.get(oldestPos); t != tracker; t = queue.get(oldestPos)) {
                if (t.isModifier()) {
                    oldestPos++;
                } else {
                    t.onUpEvent(t.getLastX(), t.getLastY(), eventTime);
                    t.setAlreadyProcessed();
                    queue.remove(oldestPos);
                }
            }
        }

        public void releaseAllPointersExcept(PointerTracker tracker, long eventTime) {
            for (PointerTracker t : mQueue) {
                if (t == tracker)
                    continue;
                t.onUpEvent(t.getLastX(), t.getLastY(), eventTime);
                t.setAlreadyProcessed();
            }
            mQueue.clear();
            if (tracker != null)
                mQueue.add(tracker);
        }

        public void remove(PointerTracker tracker) {
            mQueue.remove(tracker);
        }

        public boolean isInSlidingKeyInput() {
            for (final PointerTracker tracker : mQueue) {
                if (tracker.isInSlidingKeyInput())
                    return true;
            }
            return false;
        }
    }
    private void startKeyPreviewFadeInAnimation()
    {
        /* Daniel
        mKeyPreviewFadeInAnimator.reset();
        mPreviewText.clearAnimation();
        mPreviewText.startAnimation(mKeyPreviewFadeInAnimator);
         */
    }
    private void startKeyPreviewFadeOutAnimation()
    {
        /* Daniel
        mKeyPreviewFadeOutAnimator.reset();
        mPreviewText.clearAnimation();
        mPreviewText.startAnimation(mKeyPreviewFadeOutAnimator);
         */
    }

    public LIMEKeyboardBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.LIMEKeyboardBaseView);
        mContext = context;
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mtHardwareAcceleratedDrawingEnabled = true;
    }


    public LIMEKeyboardBaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        setLayerType(LAYER_TYPE_HARDWARE, null);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.LIMEKeyboardBaseView, defStyle, R.style.LIMEBaseKeyboard);
        LayoutInflater inflate =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int previewLayout = 0;
        int keyTextSize = 0;


        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.LIMEKeyboardBaseView_keyBackground:
                    mKeyBackground = a.getDrawable(attr);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyHysteresisDistance:
                    mKeyHysteresisDistance = a.getDimensionPixelOffset(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_verticalCorrection:
                    mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyPreviewLayout:
                    previewLayout = a.getResourceId(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyPreviewOffset:
                    mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyPreviewHeight:
                    mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyTextSize:
                    mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                    break;
                case R.styleable.LIMEKeyboardBaseView_functionKeyTextColorNormal:
                    mFunctionKeyTextColorNormal = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_functionKeyTextColorPressed:
                    mFunctionKeyTextColorPressed = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyTextColorNormal:
                    mKeyTextColorNormal = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keyTextColorPressed:
                    mKeyTextColorPressed = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keySubLabelTextColorNormal:
                    mKeySubLabelTextColorNormal = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_keySubLabelTextColorPressed:
                    mKeySubLabelTextColorPressed = a.getColor(attr, 0xFF000000);
                    break;
                case R.styleable.LIMEKeyboardBaseView_labelTextSize:
                    mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                //Jeremy '11,8,11, Extended for sub-label display
                case R.styleable.LIMEKeyboardBaseView_smallLabelTextSize:
                    mSmallLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                //Jeremy '11,8,11, Extended for sub-label display
                case R.styleable.LIMEKeyboardBaseView_subLabelTextSize:
                    mSubLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                case R.styleable.LIMEKeyboardBaseView_popupLayout:
                    mPopupLayout = a.getResourceId(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_popupHint:
                    mPopupHint = a.getDrawable(attr);
                    break;
                case R.styleable.LIMEKeyboardBaseView_shadowColor:
                    mShadowColor = a.getColor(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_shadowRadius:
                    mShadowRadius = a.getFloat(attr, 0f);
                    break;
                case R.styleable.LIMEKeyboardBaseView_spacePreviewTopPadding:  //Jeremy 15,7,13
                    mSpacePreviewTopPadding = a.getDimensionPixelSize(attr, 10);
                    break;
                case R.styleable.LIMEKeyboardBaseView_previewTopPadding:  //Jeremy 15,7,13
                    mPreviewTopPadding = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.LIMEKeyboardBaseView_backgroundDimAmount:
                    mBackgroundDimAmount = a.getFloat(attr, 0.5f);
                    break;
                //case android.R.styleable.
                case R.styleable.LIMEKeyboardBaseView_keyTextStyle:
                    int textStyle = a.getInt(attr, 0);
                    switch (textStyle) {
                        case 0:
                            mKeyTextStyle = Typeface.DEFAULT;
                            break;
                        case 1:
                            mKeyTextStyle = Typeface.DEFAULT_BOLD;
                            break;
                        default:
                            mKeyTextStyle = Typeface.defaultFromStyle(textStyle);
                            break;
                    }
                    break;
                case R.styleable.LIMEKeyboardBaseView_symbolColorScheme:
                    mSymbolColorScheme = a.getInt(attr, 0);
                    break;
            }
        }

        final Resources res = getResources();

        isLargeScreen = true; //large || xlarge;  //Force turn off fling selection now.

        mPreviewPopup = new PopupWindow(context);
        if (previewLayout != 0) {
            mKeyPreviewFadeInAnimator = AnimationUtils.loadAnimation(mContext,R.anim.key_preview_fadein);
            mKeyPreviewFadeOutAnimator = AnimationUtils.loadAnimation(mContext,R.anim.key_preview_fadeout);

            mPreviewTextSizeLarge = (int) res.getDimension(R.dimen.key_preview_text_size_large);
        } else {
            mShowPreview = false;
        }
        mPreviewPopup.setTouchable(false);
        mDelayBeforePreview = res.getInteger(R.integer.config_delay_before_preview);
        mDelayAfterPreview = res.getInteger(R.integer.config_delay_after_preview);

        mMiniKeyboardParent = this;
        mMiniKeyboardPopup = new PopupWindow(context);
        mMiniKeyboardPopup.setBackgroundDrawable(null);
        mMiniKeyboardPopup.setAnimationStyle(R.style.MiniKeyboardAnimation);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(keyTextSize);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAlpha(255);

        mPadding = new Rect(0, 0, 0, 0);
        mKeyBackground.getPadding(mPadding);

        mSwipeThreshold = (int) (500 * res.getDisplayMetrics().density);
        // TODO: Refer frameworks/base/core/res/res/values/config.xml
        mDisambiguateSwipe = res.getBoolean(R.bool.config_swipeDisambiguation);
        mMiniKeyboardSlideAllowance = res.getDimension(R.dimen.mini_keyboard_slide_allowance);

        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX,
                                           float velocityY) {
                        final float absX = Math.abs(velocityX);
                        final float absY = Math.abs(velocityY);
                        float deltaX = me2.getX() - me1.getX();
                        float deltaY = me2.getY() - me1.getY();
                        int travelX = getWidth() / 2; // Half the keyboard width
                        int travelY = getHeight() / 2; // Half the keyboard height
                        mSwipeTracker.computeCurrentVelocity(1000);
                        final float endingVelocityX = mSwipeTracker.getXVelocity();
                        final float endingVelocityY = mSwipeTracker.getYVelocity();
                        if (velocityX > mSwipeThreshold && absY < absX && deltaX > travelX) {
                            if (mDisambiguateSwipe && endingVelocityX >= velocityX / 4) {
                                swipeRight();
                                return true;
                            }
                        } else if (velocityX < -mSwipeThreshold && absY < absX && deltaX < -travelX) {
                            if (mDisambiguateSwipe && endingVelocityX <= velocityX / 4) {
                                swipeLeft();
                                return true;
                            }
                        } else if (velocityY < -mSwipeThreshold && absX < absY && deltaY < -travelY) {
                            if (mDisambiguateSwipe && endingVelocityY <= velocityY / 4) {
                                swipeUp();
                                return true;
                            }
                        } else if (velocityY > mSwipeThreshold && absX < absY / 2 && deltaY > travelY) {
                            if (mDisambiguateSwipe && endingVelocityY >= velocityY / 4) {
                                swipeDown();
                                return true;
                            }
                        }
                        return false;
                    }
                };

        final boolean ignoreMultitouch = true;
        mGestureDetector = new GestureDetector(getContext(), listener, null, ignoreMultitouch);

        mGestureDetector.setIsLongpressEnabled(false);

        mHasDistinctMultitouch = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
        mKeyRepeatInterval = res.getInteger(R.integer.config_key_repeat_interval);
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mKeyboardActionListener = listener;
        for (PointerTracker tracker : mPointerTrackers) {
            tracker.setOnKeyboardActionListener(listener);
        }
    }

    /**
     * Returns the {@link OnKeyboardActionListener} object.
     *
     * @return the listener attached to this keyboard
     */
    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return mKeyboardActionListener;
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     * //* @see Keyboard
     *
     * @param keyboard the keyboard to display in this view
     * @see #getKeyboard()
     */
    public void setKeyboard(LIMEBaseKeyboard keyboard) {
        // Remove any pending messages, except dismissing preview
        mHandler.cancelKeyTimers();
        mKeyboard = keyboard;
        //LatinImeLogger.onSetKeyboard(keyboard);
        mKeys = mKeyDetector.setKeyboard(keyboard, -getPaddingLeft(),
                -getPaddingTop() + mVerticalCorrection);
        mKeyboardVerticalGap = (int) getResources().getDimension(R.dimen.key_bottom_gap);
        for (PointerTracker tracker : mPointerTrackers) {
            tracker.setKeyboard(mKeys, mKeyHysteresisDistance);
        }
        requestLayout();
        // Hint to reallocate the buffer if the size changed
        mOffsetInWindow = null;  //reset offset window.  keyboard changed.
        mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        mMiniKeyboardCache.clear();
    }

    /**
     * Returns the current keyboard being displayed by this view.
     *
     * @return the currently attached keyboard
     * //* @see #setKeyboard(Keyboard)
     */
    public LIMEBaseKeyboard getKeyboard() {
        return mKeyboard;
    }

    /**
     * Return whether the device has distinct multi-touch panel.
     *
     * @return true if the device has distinct multi-touch panel.
     */
    public boolean hasDistinctMultitouch() {
        return mHasDistinctMultitouch;
    }

    /**
     * Sets the state of the shift key of the keyboard, if any.
     *
     * @param shifted whether or not to enable the state of the shift key
     * @return true if the shift key state changed, false if there was no change
     */
    public boolean setShifted(boolean shifted) {
        if (mKeyboard != null) {
            if (mKeyboard.setShifted(shifted)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the state of the shift key of the keyboard, if any.
     *
     * @return true if the shift is in a pressed state, false otherwise. If there is
     * no shift key on the keyboard or there is no keyboard attached, it returns false.
     */
    public boolean isShifted() {
        return mKeyboard != null && mKeyboard.isShifted();
    }

    /**
     * Enables or disables the key feedback popup. This is a popup that shows a magnified
     * version of the depressed key. By default the preview is enabled.
     *
     * @param previewEnabled whether or not to enable the key feedback popup
     * @see #isPreviewEnabled()
     */
    public void setPreviewEnabled(boolean previewEnabled) {
        mShowPreview = previewEnabled;
    }

    /**
     * Returns the enabled state of the key feedback popup.
     *
     * @return whether or not the key feedback popup is enabled
     * @see #setPreviewEnabled(boolean)
     */
    public boolean isPreviewEnabled() {
        return mShowPreview;
    }

    public int getSymbolColorScheme() {
        return mSymbolColorScheme;
    }

    public void setPopupParent(View v) {
        mMiniKeyboardParent = v;
    }

    public void setPopupOffset(int x, int y) {
        mPopupPreviewOffsetX = x;
        mPopupPreviewOffsetY = y;
        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
    }

    /**
     * When enabled, calls to {@link OnKeyboardActionListener#onKey} will include key
     * codes for adjacent keys.  When disabled, only the primary key code will be
     * reported.
     *
     * @param enabled whether or not the proximity correction is enabled
     */
    public void setProximityCorrectionEnabled(boolean enabled) {
        mKeyDetector.setProximityCorrectionEnabled(enabled);
    }

    /**
     * Returns true if proximity correction is enabled.
     */
    public boolean isProximityCorrectionEnabled() {
        return mKeyDetector.isProximityCorrectionEnabled();
    }

    protected CharSequence adjustCase(CharSequence label) {
        if (mKeyboard.isShifted() && label != null && label.length() <= 3
                && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Round up a little
        if (mKeyboard == null) {
            setMeasuredDimension(
                    getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        } else {
            int width = mKeyboard.getMinWidth() + getPaddingLeft() + getPaddingRight();
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            setMeasuredDimension(
                    width, mKeyboard.getHeight() + getPaddingTop() + getPaddingBottom());
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically)
     * and square it to get the proximity threshold. We use a square here and in computing
     * the touch distance from a key's center to avoid taking a square root.
     *
     */
    private void computeProximityThreshold(LIMEBaseKeyboard keyboard) {
        if (keyboard == null) return;
        final Key[] keys = mKeys;
        if (keys == null) return;
        int length = keys.length;
        int dimensionSum = 0;
        for (Key key : keys) {
            dimensionSum += Math.min(key.width, key.height + mKeyboardVerticalGap) + key.gap;
        }
        if (dimensionSum < 0 || length == 0) return;
        mKeyDetector.setProximityThreshold((int) (dimensionSum * 1.4f / length));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Release the buffer, if any and it will be reallocated on the next draw
        mBuffer = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
    }

    private void onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }
        final Canvas canvas = mCanvas;
        //Daniel: this is old usage. will cause crash
        //canvas.clipRect(mDirtyRect, Op.REPLACE);

        if (mKeyboard == null) return;

        final Paint paint = mPaint;
        final Drawable keyBackground = mKeyBackground;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        final Key[] keys = mKeys;
        final Key invalidKey = mInvalidatedKey;


        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // TODO we should use Rect.inset and Rect.contains here.
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                    invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                    invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                    invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        //final int keyCount = keys.length;
        for (final Key key : keys) {
            if (drawSingleKey && invalidKey != key) {
                continue;
            }
            int[] drawableState = key.getCurrentDrawableState();
            keyBackground.setState(drawableState);


            // Switch the character to uppercase if shift is pressed
            String label = key.label == null ? null : adjustCase(key.label).toString();

            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right || key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            boolean shouldDrawIcon = true;
            if (label != null) {
                // For characters, use large font. For labels like "Done", use small font.
                final int labelSize;

                /*
                if (DEBUG)
                    Log.i(TAG, "onBufferDraw():" + label
                            + " keySizeScale = " + mKeyboard.getKeySizeScale() + " "
                            + " labelSizeScale = " + key.getLabelSizeScale());
                 */

                float keySizeScale = mKeyboard.getKeySizeScale();
                float labelSizeScale = key.getLabelSizeScale();

                boolean hasSubLabel = label.contains("\n");
                boolean hasSecondSubLabel = false;
                String subLabel = "", secondSubLabel = "";
                if (hasSubLabel) {
                    String labelA[] = label.split("\n");
                    if (labelA.length > 0) label = labelA[1];
                    subLabel = labelA[0];

                    hasSecondSubLabel = subLabel.contains("\t");
                    if (hasSecondSubLabel) {
                        String subLabelA[] = subLabel.split("\t");
                        if (subLabelA.length > 0) subLabel = subLabelA[0];
                        secondSubLabel = subLabelA[1];
                    }
                }
                if (hasSubLabel) {
                    if (label.length() > 1) { //Jeremy '12,6,6 shrink the font size for more characters on label
                        labelSize = (int) (mSmallLabelTextSize * keySizeScale * labelSizeScale * 0.8f);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                    } else {
                        labelSize = (int) (mSmallLabelTextSize * keySizeScale * labelSizeScale);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                } else if (label.length() > 1 && key.codes.length < 2) {
                    labelSize = (int) (mLabelTextSize * keySizeScale * labelSizeScale);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    labelSize = (int) (mKeyTextSize * keySizeScale * labelSizeScale);
                    paint.setTypeface(mKeyTextStyle);
                }
                paint.setTextSize(labelSize);


                final int labelHeight;
                final int labelWidth;
                String KEY_LABEL_HEIGHT_REFERENCE_CHAR = "W";
                if (mTextHeightCache.get(labelSize) != null) {
                    labelHeight = mTextHeightCache.get(labelSize);
                    labelWidth = mTextWidthCache.get(labelSize);
                } else {
                    Rect textBounds = new Rect();
                    paint.getTextBounds(KEY_LABEL_HEIGHT_REFERENCE_CHAR, 0, 1, textBounds);
                    labelHeight = textBounds.height();
                    labelWidth = textBounds.width();
                    mTextHeightCache.put(labelSize, labelHeight);
                    mTextWidthCache.put(labelSize, labelWidth);
                }

                // Draw a drop shadow for the text
                if (mShadowRadius > 0) paint.setShadowLayer(mShadowRadius, 0, 0, mShadowColor);
                final int centerX = (key.width + padding.left - padding.right) / 2;
                final int centerY = (key.height + padding.top - padding.bottom) / 2;
                final int keyColor = key.isFunctionalKey()
                        ? (key.pressed ? mFunctionKeyTextColorPressed : mFunctionKeyTextColorNormal)
                        : (key.pressed ? mKeyTextColorPressed : mKeyTextColorNormal);
                final int subKeyColor = key.isFunctionalKey()
                        ? (key.pressed ? mFunctionKeyTextColorPressed : mFunctionKeyTextColorNormal)
                        :(key.pressed ? mKeySubLabelTextColorPressed : mKeySubLabelTextColorNormal);

                float KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR = 0.55f;
                float baseline = centerY
                        + labelHeight * KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR;
                if (hasSubLabel) {
                    final int subLabelSize = (int) (mSubLabelTextSize * keySizeScale * labelSizeScale);
                    final int subLabelHeight;
                    final int subLabelWidth;
                    paint.setTypeface(Typeface.DEFAULT_BOLD);

                    paint.setTextSize(subLabelSize);
                    if (mTextHeightCache.get(subLabelSize) != null) {
                        subLabelHeight = mTextHeightCache.get(subLabelSize);
                        subLabelWidth = mTextWidthCache.get(subLabelSize);
                    } else {

                        Rect textBounds = new Rect();
                        paint.getTextBounds(KEY_LABEL_HEIGHT_REFERENCE_CHAR, 0, 1, textBounds);
                        subLabelHeight = textBounds.height();
                        subLabelWidth = textBounds.width();
                        mTextHeightCache.put(subLabelSize, subLabelHeight);
                        mTextWidthCache.put(subLabelSize, subLabelWidth);
                    }

                    //portrait keyboard
                    if (key.height > key.width || subLabel.length() >2 || hasSecondSubLabel) {
                        baseline = (key.height + padding.top - padding.bottom) * 2 / 3
                                + labelHeight * KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR;
                        float subBaseline = (key.height + padding.top - padding.bottom) / 4
                                + subLabelHeight * KEY_LABEL_VERTICAL_ADJUSTMENT_FACTOR;
                        paint.setColor(subKeyColor);

                        if (hasSecondSubLabel) {
                            canvas.drawText(subLabel, centerX / 2, subBaseline, paint);

                            paint.setColor(keyColor);
                            canvas.drawText(secondSubLabel, centerX / 2 * 3, subBaseline, paint);
                        } else
                            canvas.drawText(subLabel, centerX, subBaseline, paint);

                        paint.setTextSize(labelSize);
                        paint.setTypeface(mKeyTextStyle);
                        paint.setColor(keyColor);
                        canvas.drawText(label, centerX, baseline, paint);

                    } else {    //landscape keyboard
                        paint.setColor(subKeyColor);
                        //if (subLabel.length() > 2)  // draw sub keys as portrait keys in two rows.
                        //    paint.setTextSize(subLabelSize * 2 / 3);  //123 EN  in landscape is usually to wide.
                        /*if (hasSecondSubLabel) {
                                                    canvas.drawText(subLabel, centerX - subLabelWidth * 2, baseline, paint);
                                                    paint.setColor(keyColor);
                                                    canvas.drawText(secondSubLabel, centerX - subLabelWidth, baseline, paint);
                                                } else*/
                        canvas.drawText(subLabel, centerX - subLabelWidth, baseline, paint);

                        paint.setTextSize(labelSize);
                        paint.setTypeface(mKeyTextStyle);
                        paint.setColor(keyColor);
                        canvas.drawText(label, centerX + labelWidth/2, baseline, paint);

                    }

                } else {
                    paint.setColor(keyColor);
                    canvas.drawText(label, centerX, baseline, paint);
                }
                // Turn off drop shadow
                if (mShadowRadius > 0) paint.setShadowLayer(0, 0, 0, 0);

                // Usually don't draw icon if label is not null, but we draw icon for the number
                // hint and popup hint.
                shouldDrawIcon = shouldDrawLabelAndIcon(key);
            }
            if (shouldDrawIcon) {
                Drawable icon = key.icon;
                if (icon == null)
                    icon = mPopupHint;
                else {
                    icon.setState(drawableState);
                }


                // Special handing for the upper-right number hint icons
                final int drawableWidth;
                final int drawableHeight;
                final int drawableX;
                final int drawableY;
                if (shouldDrawIconFully(key)) {
                    drawableWidth = key.width;
                    drawableHeight = key.height;
                    drawableX = 0;
                    drawableY = NUMBER_HINT_VERTICAL_ADJUSTMENT_PIXEL;
                } else {

                    drawableHeight = key.height; // icon.getIntrinsicHeight();
                    drawableWidth = icon.getIntrinsicWidth() * drawableHeight / icon.getIntrinsicHeight()  ;
                    drawableX = (key.width + padding.left - padding.right - drawableWidth) / 2;
                    drawableY = (key.height + padding.top - padding.bottom - drawableHeight) / 2;
                }
                canvas.translate(drawableX, drawableY);
                icon.setBounds(0, 0, drawableWidth, drawableHeight);
                icon.draw(canvas);
                canvas.translate(-drawableX, -drawableY);
            }
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
        }
        mInvalidatedKey = null;
        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboard != null) {
            paint.setColor((int) (mBackgroundDimAmount * 0xFF) << 24);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }

        if (DEBUG) {
            if (mShowTouchPoints) {
                for (PointerTracker tracker : mPointerTrackers) {
                    int startX = tracker.getStartX();
                    int startY = tracker.getStartY();
                    int lastX = tracker.getLastX();
                    int lastY = tracker.getLastY();
                    paint.setAlpha(128);
                    paint.setColor(0xFFFF0000);
                    canvas.drawCircle(startX, startY, 3, paint);
                    canvas.drawLine(startX, startY, lastX, lastY, paint);
                    paint.setColor(0xFF0000FF);
                    canvas.drawCircle(lastX, lastY, 3, paint);
                    paint.setColor(0xFF00FF00);
                    canvas.drawCircle((startX + lastX) / 2, (startY + lastY) / 2, 2, paint);
                }
            }
        }

        mDrawPending = false;
        mDirtyRect.setEmpty();
    }

    /**
     * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     *
     * @see #invalidateKey(Key)
     */
    public void invalidateAllKeys() {
        mDirtyRect.union(0, 0, getWidth(), getHeight());
        mDrawPending = true;
        invalidate();
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     * //* @param key key in the attached {@link //Keyboard}.
     *
     * @see #invalidateAllKeys
     */
    public void invalidateKey(Key key) {
        if (key == null)
            return;
        mInvalidatedKey = key;
        // TODO we should clean up this and record key's region to use in onBufferDraw.
        mDirtyRect.union(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
        onBufferDraw();
        invalidate(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
    }

    @Override
    public void showPreview(int keyIndex, PointerTracker tracker) {

    }

    private boolean openPopupIfRequired(int keyIndex, PointerTracker tracker) {
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false;
        }

        Key popupKey = tracker.getKey(keyIndex);
        if (popupKey == null)
            return false;
        boolean result = onLongPress(popupKey);
        if (result) {
            mMiniKeyboardTrackerId = tracker.mPointerId;
            // Mark this tracker "already processed" and remove it from the pointer queue
            tracker.setAlreadyProcessed();
            mPointerQueue.remove(tracker);
        }
        return result;
    }

    private View inflateMiniKeyboardContainer(Key popupKey) {
        int popupKeyboardId = popupKey.popupResId;
        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View container = LayoutInflater.from(mContext).inflate(mPopupLayout, null);
        if (container == null)
            throw new NullPointerException();

        LIMEKeyboardBaseView miniKeyboard =
                (LIMEKeyboardBaseView) container.findViewById(R.id.LIMEPopupKeyboard);
        miniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
            public void onKey(int primaryCode, int[] keyCodes, int x, int y) {
                mKeyboardActionListener.onKey(primaryCode, keyCodes, x, y);
                dismissPopupKeyboard();
            }

            public void onText(CharSequence text) {
                mKeyboardActionListener.onText(text);
                dismissPopupKeyboard();
            }

            public void onCancel() {
                mKeyboardActionListener.onCancel();
                dismissPopupKeyboard();
            }

            public void swipeLeft() {
            }

            public void swipeRight() {
            }

            public void swipeUp() {
            }

            public void swipeDown() {
            }

            public void onPress(int primaryCode) {
                mKeyboardActionListener.onPress(primaryCode);
            }

            public void onRelease(int primaryCode) {
                mKeyboardActionListener.onRelease(primaryCode);
            }
        });
        // Override default ProximityKeyDetector.
        miniKeyboard.mKeyDetector = new MiniKeyboardKeyDetector(mMiniKeyboardSlideAllowance);
        // Remove gesture detector on mini-keyboarda
        miniKeyboard.mGestureDetector = null;

        LIMEBaseKeyboard keyboard;
        if (popupKey.popupCharacters != null) {
            keyboard = new LIMEBaseKeyboard(mContext, popupKeyboardId, popupKey.popupCharacters,
                    -1, getPaddingLeft() + getPaddingRight(),
                    LIMEKeyboardBaseView.this.mKeyboard.getKeySizeScale());
        } else {
            keyboard = new LIMEBaseKeyboard(mContext, popupKeyboardId
                    , LIMEKeyboardBaseView.this.mKeyboard.getKeySizeScale(), 0, 0); //Jeremy '12,5,21 never show arrow keys in popup keyboard
        }
        //mini keyboard in fling mode override with fling correction. Jeremy '12,5,27
        if (!isLargeScreen || keyboard.getKeys().size() == 1)
            miniKeyboard.mVerticalCorrection =
                    getResources().getDimension(R.dimen.mini_keyboard_fling_vertical_correction);
        miniKeyboard.setKeyboard(keyboard);
        miniKeyboard.setPopupParent(this);

        container.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

        return container;
    }

    private static boolean isOneRowKeys(List<Key> keys) {
        if (keys.size() == 0) return false;
        final int edgeFlags = keys.get(0).edgeFlags;
        // HACK: The first key of mini keyboard which was inflated from xml and has multiple rows,
        // does not have both top and bottom edge flags on at the same time.  On the other hand,
        // the first key of mini keyboard that was created with popupCharacters must have both top
        // and bottom edge flags on.
        // When you want to use one row mini-keyboard from xml file, make sure that the row has
        // both top and bottom edge flags set.
        return (edgeFlags & LIMEBaseKeyboard.EDGE_TOP) != 0 && (edgeFlags & LIMEBaseKeyboard.EDGE_BOTTOM) != 0;
    }

    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated
     * with this key through the attributes popupLayout and popupCharacters.
     *
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the
     * method on the base class if the subclass doesn't wish to handle the call.
     */
    protected boolean onLongPress(Key popupKey) {
        // TODO if popupKey.popupCharacters has only one letter, send it as key without opening
        // mini keyboard.

        if (popupKey.popupResId == 0)
            return false;

        View container = mMiniKeyboardCache.get(popupKey);
        if (container == null) {
            container = inflateMiniKeyboardContainer(popupKey);
            mMiniKeyboardCache.put(popupKey, container);
        }
        mMiniKeyboard = (LIMEKeyboardBaseView) container.findViewById(R.id.LIMEPopupKeyboard);
        if (mWindowOffset == null) {
            mWindowOffset = new int[2];
            getLocationInWindow(mWindowOffset);
        }

        // Get width of a key in the mini popup keyboard = "miniKeyWidth".
        // On the other hand, "popupKey.width" is width of the pressed key on the main keyboard.
        // We adjust the position of mini popup keyboard with the edge key in it:
        //  a) When we have the leftmost key in popup keyboard directly above the pressed key
        //     Right edges of both keys should be aligned for consistent default selection
        //  b) When we have the rightmost key in popup keyboard directly above the pressed key
        //     Left edges of both keys should be aligned for consistent default selection
        final List<Key> miniKeys = mMiniKeyboard.getKeyboard().getKeys();
        final int miniKeyWidth = miniKeys.size() > 0 ? miniKeys.get(0).width : 0;

        // HACK: Have the leftmost number in the popup characters right above the key
        boolean isNumberAtLeftmost =
                hasMultiplePopupChars(popupKey) && isNumberAtLeftmostPopupChar(popupKey);
        int popupX = popupKey.x + mWindowOffset[0];
        popupX += getPaddingLeft();
        if (isNumberAtLeftmost) {
            popupX += popupKey.width - miniKeyWidth;  // adjustment for a) described above
            popupX -= container.getPaddingLeft();
        } else {
            popupX += miniKeyWidth;  // adjustment for b) described above
            popupX -= container.getMeasuredWidth();
            popupX += container.getPaddingRight();
        }
        int popupY = popupKey.y + mWindowOffset[1];
        popupY += getPaddingTop();
        popupY -= container.getMeasuredHeight();
        popupY += container.getPaddingBottom();
        final int x = popupX;
        final int y = mShowPreview && isOneRowKeys(miniKeys) ? mPopupPreviewDisplayedY : popupY;

        int adjustedX = x;
        if (x < 0) {
            adjustedX = 0;
        } else if (x > (getMeasuredWidth() - container.getMeasuredWidth())) {
            adjustedX = getMeasuredWidth() - container.getMeasuredWidth();
        }
        mMiniKeyboardOriginX = adjustedX + container.getPaddingLeft() - mWindowOffset[0];
        mMiniKeyboardOriginY = y + container.getPaddingTop() - mWindowOffset[1];
        mMiniKeyboard.setPopupOffset(adjustedX, y);
        mMiniKeyboard.setShifted(isShifted());
        // Mini keyboard needs no pop-up key preview displayed.
        mMiniKeyboard.setPreviewEnabled(isLargeScreen && miniKeys.size() > 1);  // no fling on large screen
        mMiniKeyboardPopup.setContentView(container);
        mMiniKeyboardPopup.setWidth(container.getMeasuredWidth());
        mMiniKeyboardPopup.setHeight(container.getMeasuredHeight());
        mMiniKeyboardPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);

        // Inject down event on the key to mini keyboard.
        long eventTime = SystemClock.uptimeMillis();
        mMiniKeyboardPopupTime = eventTime;
        if (!isLargeScreen || miniKeys.size() == 1) {   // disable fling on large screen; //Jeremy enable fling when popup keyboard only has 1 key '12,5,20
            MotionEvent downEvent = generateMiniKeyboardMotionEvent(MotionEvent.ACTION_DOWN, popupKey.x
                    + popupKey.width / 2, popupKey.y + popupKey.height / 2, eventTime);
            mMiniKeyboard.onTouchEvent(downEvent);
            downEvent.recycle();
        }

        invalidateAllKeys();
        return true;
    }

    private static boolean hasMultiplePopupChars(Key key) {
        return key.popupCharacters != null && key.popupCharacters.length() > 1;
    }

    private boolean shouldDrawIconFully(Key key) {
        return (hasPopupKeyboard(key));
        //return isNumberAtEdgeOfPopupChars(key) || isLatinF1Key(key);
        //|| LIMEKeyboard.hasPuncOrSmileysPopup(key);

    }

    private boolean shouldDrawLabelAndIcon(Key key) {
        return hasPopupKeyboard(key) ||  key.icon != null;

    }

    private boolean hasPopupKeyboard(Key key) {
        return key.popupResId != 0;
    }

    private static boolean isNumberAtLeftmostPopupChar(Key key) {
        return key.popupCharacters != null && key.popupCharacters.length() > 0
                && isAsciiDigit(key.popupCharacters.charAt(0));
    }

   private static boolean isAsciiDigit(char c) {
        return (c < 0x80) && Character.isDigit(c);
    }

    private MotionEvent generateMiniKeyboardMotionEvent(int action, int x, int y, long eventTime) {
        return MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, action,
                x - mMiniKeyboardOriginX, y - mMiniKeyboardOriginY, 0);
    }

    private PointerTracker getPointerTracker(final int id) {
        final ArrayList<PointerTracker> pointers = mPointerTrackers;
        final Key[] keys = mKeys;
        final OnKeyboardActionListener listener = mKeyboardActionListener;

        // Create pointer trackers until we can get 'id+1'-th tracker, if needed.
        for (int i = pointers.size(); i <= id; i++) {
            final PointerTracker tracker =
                    new PointerTracker(i, mHandler, mKeyDetector, this, getResources());
            if (keys != null)
                tracker.setKeyboard(keys, mKeyHysteresisDistance);
            if (listener != null)
                tracker.setOnKeyboardActionListener(listener);
            pointers.add(tracker);
        }

        return pointers.get(id);
    }

    private void onDownEvent(PointerTracker tracker, int x, int y, long eventTime) {
        if(DEBUG)
            Log.i(TAG,"onDownEvent() eventTime = " + eventTime);
        if (tracker.isOnModifierKey(x, y)) {
            // Before processing a down event of modifier key, all pointers already being tracked
            // should be released.
            mPointerQueue.releaseAllPointersExcept(null, eventTime);
        }
        tracker.onDownEvent(x, y, eventTime);
        mPointerQueue.add(tracker);
    }

    private void onUpEvent(PointerTracker tracker, int x, int y, long eventTime) {
        if(DEBUG)
            Log.i(TAG,"onUpEvent() eventTime = " + eventTime);

        if (tracker.isModifier()) {
            // Before processing an up event of modifier key, all pointers already being tracked
            // should be released.
            mPointerQueue.releaseAllPointersExcept(tracker, eventTime);
        } else {
            int index = mPointerQueue.lastIndexOf(tracker);
            if (index >= 0) {
                mPointerQueue.releaseAllPointersOlderThan(tracker, eventTime);
            } else {
                Log.w(TAG, "onUpEvent: corresponding down event not found for pointer "
                        + tracker.mPointerId);
            }
        }
        tracker.onUpEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        if(DEBUG)
            Log.i(TAG,"onTouchEvent()");
        final int action = me.getActionMasked();
        final int pointerCount = me.getPointerCount();
        final int oldPointerCount = mOldPointerCount;
        mOldPointerCount = pointerCount;

        if(DEBUG)
            Log.i(TAG,"onTouchEvent() pointerCount = "+ pointerCount + ", oldPointerCount" + oldPointerCount);

        // TODO: cleanup this code into a multi-touch to single-touch event converter class?
        // If the device does not have distinct multi-touch support panel, ignore all multi-touch
        // events except a transition from/to single-touch.
        if (!mHasDistinctMultitouch && pointerCount > 1 && oldPointerCount > 1) {
            return true;
        }

        // Track the last few movements to look for spurious swipes.
        mSwipeTracker.addMovement(me);

        // Gesture detector must be enabled only when mini-keyboard is not on the screen.
        if (mMiniKeyboard == null
                && mGestureDetector != null && mGestureDetector.onTouchEvent(me)) {
            mHandler.cancelKeyTimers();
            return true;
        }

        final long eventTime = me.getEventTime();
        final int index = me.getActionIndex();
        final int id = me.getPointerId(index);
        final int x = (int) me.getX(index);
        final int y = (int) me.getY(index);

        // Needs to be called after the gesture detector gets a turn, as it may have
        // displayed the mini keyboard
        if (mMiniKeyboard != null && (!isLargeScreen || mMiniKeyboard.getKeyboard().getKeys().size() == 1)) {  //Jeremy enable fling when popup keyboard only has 1 key '12,5,20
            final int miniKeyboardPointerIndex = me.findPointerIndex(mMiniKeyboardTrackerId);
            if (miniKeyboardPointerIndex >= 0 && miniKeyboardPointerIndex < pointerCount) {
                final int miniKeyboardX = (int) me.getX(miniKeyboardPointerIndex);
                final int miniKeyboardY = (int) me.getY(miniKeyboardPointerIndex);
                MotionEvent translated = generateMiniKeyboardMotionEvent(action,
                        miniKeyboardX, miniKeyboardY, eventTime);
                mMiniKeyboard.onTouchEvent(translated);
                translated.recycle();
            }
            return true;
        }

        if (mHandler.isInKeyRepeat()) {
            // It will keep being in the key repeating mode while the key is being pressed.
            if (action == MotionEvent.ACTION_MOVE) {
                return true;
            }
            final PointerTracker tracker = getPointerTracker(id);
            // Key repeating timer will be canceled if 2 or more keys are in action, and current
            // event (UP or DOWN) is non-modifier key.
            if (pointerCount > 1 && !tracker.isModifier()) {
                mHandler.cancelKeyRepeatTimer();
            }
            // Up event will pass through.
        }

        // TODO: cleanup this code into a multi-touch to single-touch event converter class?
        // Translate mutli-touch event to single-touch events on the device that has no distinct
        // multi-touch panel.
        if (!mHasDistinctMultitouch) {
            // Use only main (id=0) pointer tracker.
            PointerTracker tracker = getPointerTracker(0);
            if (pointerCount == 1 && oldPointerCount == 2) {
                // Multi-touch to single touch transition.
                // Send a down event for the latest pointer.
                tracker.onDownEvent(x, y, eventTime);
            } else if (pointerCount == 2 && oldPointerCount == 1) {
                // Single-touch to multi-touch transition.
                // Send an up event for the last pointer.
                tracker.onUpEvent(tracker.getLastX(), tracker.getLastY(), eventTime);
            } else if (pointerCount == 1 && oldPointerCount == 1) {
                tracker.onTouchEvent(action, x, y, eventTime);
            } else {
                Log.w(TAG, "Unknown touch panel behavior: pointer count is " + pointerCount
                        + " (old " + oldPointerCount + ")");
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < pointerCount; i++) {
                PointerTracker tracker = getPointerTracker(me.getPointerId(i));
                tracker.onMoveEvent((int) me.getX(i), (int) me.getY(i), eventTime);
            }
        } else {
            PointerTracker tracker = getPointerTracker(id);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    onDownEvent(tracker, x, y, eventTime);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    onUpEvent(tracker, x, y, eventTime);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    onCancelEvent(tracker, x, y, eventTime);
                    break;
            }
        }

        return true;
    }

    private void onCancelEvent(PointerTracker tracker, int x, int y, long eventTime) {
        if(DEBUG)
            Log.i(TAG,"onCancelEvent() eventTime = " + eventTime);

        tracker.onCancelEvent(x, y, eventTime);
        mPointerQueue.remove(tracker);
    }

    protected void swipeRight() {
        mKeyboardActionListener.swipeRight();
    }

    protected void swipeLeft() {
        mKeyboardActionListener.swipeLeft();
    }

    protected void swipeUp() {
        mKeyboardActionListener.swipeUp();
    }

    protected void swipeDown() {
        mKeyboardActionListener.swipeDown();
    }

    public void closing() {
        if(DEBUG)
            Log.i(TAG,"closing()");

        mHandler.cancelAllMessages();
        dismissPopupKeyboard();
        mBuffer = null;
        mCanvas = null;
        mMiniKeyboardCache.clear();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    private void dismissPopupKeyboard() {
        if (mMiniKeyboardPopup.isShowing()) {
            mMiniKeyboardPopup.dismiss();
            mMiniKeyboard = null;
            mMiniKeyboardOriginX = 0;
            mMiniKeyboardOriginY = 0;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (mMiniKeyboardPopup.isShowing()) {
            dismissPopupKeyboard();
            return true;
        }
        return false;
    }
}
