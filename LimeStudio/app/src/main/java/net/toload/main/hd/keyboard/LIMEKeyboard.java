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

import net.toload.main.hd.LIMEKeyboardSwitcher;
import net.toload.main.hd.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.ViewConfiguration;

/**
 * @author Art Hung
 */
public class LIMEKeyboard extends LIMEBaseKeyboard {

	static final boolean DEBUG = false;
	static final String TAG = "LIMEKeyboard";

	private Key mShiftKey;
    private Key mEnterKey;
	    
    private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    
    private int mShiftState = SHIFT_OFF;

    private static final float SPACEBAR_DRAG_THRESHOLD = 0.6f;

    private boolean mThemedIconLoaded = false;
    
    private static SlidingSpaceBarDrawable mSlidingSpaceBarIcon = null;

    private static boolean themedResourcesLoaded = false;
    /**
     * Drawable to override function key icons
     */
    private static Drawable mSpaceKeyIcon;
    private static Drawable mSpaceKeyPreviewIcon;
    private static Drawable mEnterKeyIcon;
    private static Drawable mDeleteKeyIcon;
    private static Drawable mShiftKeyIcon;
    private static Drawable mShiftKeyShiftedIcon;
    private static Drawable mDoneKeyIcon;

    private static Drawable mSearchKeyIcon;
    private static Drawable mSpaceKeySlidingLeftArrow;
    private static Drawable mSpaceKeySlidingRightArrow;
    private static int mSpaceKeySlidingTextSize;
    private static int mSpaceKeyTextColor;
    private static int mSpaceKeyVerticalCorrection;

    

    
    private boolean mCurrentlyInSpace;
    private static int mSpaceDragStartX;
    private static int mSpaceDragLastDiff;
    private Key mSpaceKey;
    
    // Minimum width of space key preview (proportional to keyboard width)
    private static final float SPACEBAR_POPUP_MIN_RATIO = 0.4f;
    // Height in space key the language name will be drawn. (proportional to space key height)
    private static final float SPACEBAR_IMNAME_BASELINE = 0.55f;
    
    private static final int OPACITY_FULLY_OPAQUE = 255;
    
    private Context mContext;
    private final Resources mRes;
    //private final int mMode;
    private LIMEKeyboardSwitcher mKeyboardSwitcher;
    
   // public LIMEKeyboard(Context context, int xmlLayoutResId) {
   // 	this(context, xmlLayoutResId, 0, 1, false);
   // }

    public LIMEKeyboard(Context context, int xmlLayoutResId, int mode, float keySizeScale, int showArrowKeys, int splitKeyboard ) {
        super(context, xmlLayoutResId, mode, keySizeScale, showArrowKeys, splitKeyboard);
        if(DEBUG)
            Log.i(TAG, "LIMEKeyboard()");
        mRes = context.getResources();
    }



    private void loadThemedIcons(Context context){

        if(DEBUG)
            Log.i(TAG, "loadThemedIcons()");

        mContext = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(//R.style.LIMEKeyboardLight, R.styleable.LIMEKeyboard);
                null, R.styleable.LIMEKeyboard, R.attr.LIMEKeyboardStyle, R.style.LIMEKeyboard);

        mSpaceKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_spaceKeyIcon);
        mSpaceKeyPreviewIcon = a.getDrawable(R.styleable.LIMEKeyboard_spaceKeyPreviewIcon);
        mEnterKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_enterKeyIcon);
        mSearchKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_searchKeyIcon);
        mDoneKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_doneKeyIcon);
        mDeleteKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_deleteKeyIcon);
        mShiftKeyIcon = a.getDrawable(R.styleable.LIMEKeyboard_shiftKeyIcon);
        mShiftKeyShiftedIcon = a.getDrawable(R.styleable.LIMEKeyboard_shiftKeyShiftedIcon);

        // for sliding space bar
        mSpaceKeyTextColor = a.getColor(R.styleable.LIMEKeyboard_spaceKeyTextColor,0xFF000000);
        mSpaceKeyVerticalCorrection = a.getDimensionPixelSize(R.styleable.LIMEKeyboard_spaceKeyVerticalCorrection, 0);
        mSpaceKeySlidingTextSize = a.getDimensionPixelSize(R.styleable.LIMEKeyboard_spaceKeySlidingTextSize,25);
        mSpaceKeySlidingLeftArrow = a.getDrawable(R.styleable.LIMEKeyboard_spaceKeySlidingLeftArrow);
        mSpaceKeySlidingRightArrow = a.getDrawable(R.styleable.LIMEKeyboard_spaceKeySlidingRightArrow);

        a.recycle();
    }

	@Override
    protected Key createKeyFromXml(Context context, Row parent, int x, int y, XmlResourceParser parser) {
        if(DEBUG)
            Log.i(TAG, "createKeyFromXml() mThemedIconLoaded = " + mThemedIconLoaded);

        if(!mThemedIconLoaded) {
            // createKeyFromXml called from constructor of super and will be earlier of anything in constructor of this..  Jeremy '16,7,31
            loadThemedIcons(context);
            mThemedIconLoaded = true;
        }

        Key key = new LIMEKey(context.getResources(), parent, x, y, parser);
        //Override function key icons from theme
        switch (key.codes[0]) {
            case KEYCODE_ENTER:
                mEnterKey = key;
                if(mEnterKeyIcon!=null)
                    key.icon = mEnterKeyIcon;
                break;
            case KEYCODE_SPACE:
                mSpaceKey = key;
                if(mSpaceKeyIcon!=null)
                    key.icon = mSpaceKeyIcon;
                if(mSpaceKeyPreviewIcon!=null) {
                    key.iconPreview = mSpaceKeyPreviewIcon;
                    int width = key.width;
                    int height = key.height;
                    mSlidingSpaceBarIcon = new SlidingSpaceBarDrawable(mSpaceKeyPreviewIcon, mSpaceKeySlidingLeftArrow,
                            mSpaceKeySlidingRightArrow, mSpaceKeySlidingTextSize, width, height);
                    mSlidingSpaceBarIcon.setBounds(0, 0, width, height /2);
                }
                break;
            case KEYCODE_DELETE:
                if (mDeleteKeyIcon != null)
                    key.icon = mDeleteKeyIcon;
                break;
            case KEYCODE_DONE:
                if (mDoneKeyIcon != null)
                    key.icon = mDoneKeyIcon;
                break;
            case KEYCODE_SHIFT:
                if (mShiftKeyIcon != null) {
                    key.icon = (isShifted())?mShiftKeyShiftedIcon:mShiftKeyIcon;
                    mShiftKey = key;
                }
                break;
        }

        return key;
    }
    
    public void enableShiftLock() {
        //int index = getShiftKeyIndex();
        //if (index >= 0) {
            //mShiftKey = getKeys().get(index);
        if (mShiftKey instanceof LIMEKey) {
            ((LIMEKey) mShiftKey).enableShiftLock();
        }

        //}
    }

    public void setShiftLocked(boolean shiftLocked) {
    	if(DEBUG)
            Log.i("LIMEKeyboard", "setShiftLocked: "+ shiftLocked);
        if (mShiftKey != null) {
            if (shiftLocked) {
                mShiftKey.on = true;
                mShiftState = SHIFT_LOCKED;

            } else {
                mShiftKey.on = false;
                mShiftState = SHIFT_ON;
            }
        }
    }

    public boolean isShiftLocked() {
        return mShiftState == SHIFT_LOCKED;
    }
    
    @Override
    public boolean setShifted(boolean shiftState) {
    	if(DEBUG)
            Log.i("LIMEKeyboard", "setShifted: "+ shiftState);
        boolean shiftChanged = false;
        if (mShiftKey != null) {
            if (!shiftState) {
                shiftChanged = mShiftState != SHIFT_OFF;
                mShiftState = SHIFT_OFF;
                mShiftKey.on = false;
                mShiftKey.icon = mShiftKeyIcon;
            } else {
                if (mShiftState == SHIFT_OFF) {
                    shiftChanged = true;
                    mShiftState = SHIFT_ON;
                }
                mShiftKey.icon = mShiftKeyShiftedIcon;
            }
            mShiftKey.icon.invalidateSelf();
        } else {
            return super.setShifted(shiftState);
        }
        return shiftChanged;
    }
    
    @Override
    public boolean isShifted() {
        if (mShiftKey != null) {
            return mShiftState != SHIFT_OFF;
        } else {
            return super.isShifted();
        }
    }
    
    void setImeOptions(Resources res, int options) {
    	setImeOptions(res, LIMEKeyboardSwitcher.MODE_TEXT, options);
    }
    
    public void setImeOptions(Resources res, int mode, int options) {
        if (mEnterKey != null) {
            // Reset some of the rarely used attributes.
            mEnterKey.popupCharacters = null;
            mEnterKey.popupResId = 0;
            mEnterKey.text = null;
            switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
                case EditorInfo.IME_ACTION_GO:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_go_key);
                    break;
                case EditorInfo.IME_ACTION_NEXT:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    //int c[] = {-99};
                    //mEnterKey.codes = c;
                    mEnterKey.label = res.getText(R.string.label_next_key);
                    break;
                case EditorInfo.IME_ACTION_DONE:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_done_key);
                    break;
                case EditorInfo.IME_ACTION_SEARCH:
                    mEnterKey.icon = mSearchKeyIcon;
                    mEnterKey.label = null;
                    break;
                case EditorInfo.IME_ACTION_SEND:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_send_key);
                    break;
                default:
                	
                    if (mode == LIMEKeyboardSwitcher.MODE_IM) {
                        mEnterKey.icon = null;
                        mEnterKey.label = ":-)";
                        //mEnterKey.text = ":-) ";
                        mEnterKey.popupResId = R.xml.popup_smileys;
                    } else {

                        mEnterKey.icon = mEnterKeyIcon;
                        mEnterKey.label = null;
                    }
                    break;
            }
            // Set the initial size of the preview icon
            if (mEnterKey.iconPreview != null) {
                mEnterKey.iconPreview.setBounds(0, 0,
                        mEnterKey.height *
                                mEnterKey.iconPreview.getIntrinsicWidth()/ mEnterKey.iconPreview.getIntrinsicHeight(),
                        mEnterKey.height);
            }
        }
        
    }
    
    /**
     * Does the magic of locking the touch gesture into the spacebar when
     * switching input languages.
     */
    boolean isInside(LIMEKey key, int x, int y) {
        if (DEBUG) Log.i(TAG, "isInside(), keycode = " + key.codes[0] + ". x=" + x + ". y=" + y +
                ". mSpaceDragStartX=" + mSpaceDragStartX +
                ". mSpaceDragLastDiff=" + mSpaceDragLastDiff);
        final int code = key.codes[0];
        if (code == KEYCODE_SHIFT ||
                code == KEYCODE_DELETE) {
            y -= key.height / 10;
            if (code == KEYCODE_SHIFT) x += key.width / 6;
            if (code == KEYCODE_DELETE) x -= key.width / 6;
        } else if (code == KEYCODE_SPACE) {
            y += LIMEKeyboard.mSpaceKeyVerticalCorrection;

            if (mCurrentlyInSpace) {
                int diff = x - mSpaceDragStartX;
                if (Math.abs(diff - mSpaceDragLastDiff) > 0) {
                    updateSpacebarDrag(diff);
                }
                mSpaceDragLastDiff = diff;
                return true;
            } else {
                boolean insideSpace = key.isInsideSuper(x, y);
                if (insideSpace) {
                    mCurrentlyInSpace = true;
                    mSpaceDragStartX = x;
                    updateSpacebarDrag(0);
                }
                return insideSpace;
            }


        }

        // Lock into the spacebar
        return !mCurrentlyInSpace && key.isInsideSuper(x, y);

    }
    
    void keyReleased() {
        mCurrentlyInSpace = false;
        mSpaceDragLastDiff = 0;

       if (mSpaceKey != null) {
    	   updateSpacebarDrag(Integer.MAX_VALUE);
        }
    }
    
    
    public int getSpaceDragDiff() {
     	return mSpaceDragLastDiff;
        }
    public int getSpaceDragDirection() {
    	if(DEBUG) Log.i(TAG, "getSpaceDragDirection(): mSpaceDragLastDiff= " + 
    			mSpaceDragLastDiff + ". mSpaceKey.width=" + mSpaceKey.width);
        if (mSpaceKey == null 
                || Math.abs(mSpaceDragLastDiff) < mSpaceKey.width * SPACEBAR_DRAG_THRESHOLD ) {
            return 0; // No change
        }
        return mSpaceDragLastDiff > 0 ? 1 : -1;
    }
   /* private int getTextSizeFromTheme(int style, int defValue) {
        TypedArray array = mContext.getTheme().obtainStyledAttributes(
                style, new int[] { android.R.attr.textSize });
        int textSize = array.getDimensionPixelSize(array.getResourceId(0, 0), defValue);
        return textSize;
    }*/
    private void setDefaultBounds(Drawable drawable) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }
    
    private void updateSpacebarDrag(int diff) {
    	if(DEBUG) Log.i(TAG, "updateSpacebarDrag(), deff=" + diff);
        if (mSlidingSpaceBarIcon == null) {
            final int width = Math.max(mSpaceKey.width,
                    (int)(getMinWidth() * SPACEBAR_POPUP_MIN_RATIO));
            final int height = mSpaceKey.height;// mSpacePreviewIcon.getIntrinsicHeight();

            mSlidingSpaceBarIcon = new SlidingSpaceBarDrawable(mSpaceKeyPreviewIcon, mSpaceKeySlidingLeftArrow, mSpaceKeySlidingRightArrow, mSpaceKeySlidingTextSize, width, height);
            mSlidingSpaceBarIcon.setBounds(0, 0, width, height /2);
            mSpaceKey.iconPreview = mSlidingSpaceBarIcon;
        }
        mSlidingSpaceBarIcon.setDiff(diff);
        if (Math.abs(diff) == Integer.MAX_VALUE) {
           mSpaceKey.iconPreview = mSpaceKeyPreviewIcon;
        } else {
            mSpaceKey.iconPreview = mSlidingSpaceBarIcon;
        }
        mSpaceKey.iconPreview.invalidateSelf();
    }

    class LIMEKey extends LIMEBaseKeyboard.Key {
       
    	private boolean mShiftLockEnabled;


        public LIMEKey(Resources res, LIMEBaseKeyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if(DEBUG) Log.i(TAG,"LIMEKey():"+this.codes[0]);
            if (popupCharacters != null && popupCharacters.length() == 0) {
                // If there is a keyboard with no keys specified in popupCharacters
                popupResId = 0;
            }

        }
        
        @Override
        public void onReleased(boolean inside) {
            if (!mShiftLockEnabled) {
                super.onReleased(inside);
            } else {
                pressed = !pressed;
            }
        }

        void enableShiftLock() {
            mShiftLockEnabled = true;
        }
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
        @Override
        public boolean isInside(int x, int y) {
//        	 final int code = codes[0];
//             if (code == KEYCODE_SHIFT ||
//                     code == KEYCODE_DELETE) {
//                 y -= height / 10;
//                 if (code == KEYCODE_SHIFT) x += width / 6;
//                 if (code == KEYCODE_DELETE) x -= width / 6;
//             }
//              if (code == KEYCODE_DONE) y  -= 10;
              
           
            return	LIMEKeyboard.this.isInside(this, x, y);
            
            //return super.isInside(x,  y);
        }
        boolean isInsideSuper(int x, int y) {
            return super.isInside(x, y);
        }

    }
    /**
     * 
     * Jeremy '11,8,5 make a link back channel to LIMEKeyboardSwitcher
     */
    public void setKeyboardSwitcher(LIMEKeyboardSwitcher keyboardswitcher){
    	mKeyboardSwitcher = keyboardswitcher;
    }
    

    /**
     * Animation to be displayed on the spacebar preview popup when switching 
     * IM by swiping the spacebar. It draws the current, previous and
     * next languages and moves them by the delta of touch movement on the spacebar.
     */
    class SlidingSpaceBarDrawable extends Drawable {

        private final int mWidth;
        private final int mHeight;
        private final Drawable mBackground;
        private final TextPaint mTextPaint;
        private final int mMiddleX;
        private final Drawable mLeftDrawable;
        private final Drawable mRightDrawable;
        private final int mThreshold;
        private int mDiff;
        private boolean mHitThreshold;

        private String mCurrentKeyboard;
        private String mNextKeyboard;
        private String mPrevKeyboard;
        private int mSlidingTextSize;

        public SlidingSpaceBarDrawable(Drawable background, Drawable leftArrow, Drawable rightArrow, int slidingTextSize, int width, int height) {
            mBackground = background;
            setDefaultBounds(mBackground);
            mLeftDrawable = leftArrow;
            mRightDrawable = rightArrow;
            mSlidingTextSize = slidingTextSize;

            mWidth = width;
            mHeight = height;

            mTextPaint = new TextPaint();
            mTextPaint.setTextSize(mSlidingTextSize);

            mTextPaint.setColor(mSpaceKeyTextColor);
            mTextPaint.setTextAlign(Align.CENTER);
            mTextPaint.setAlpha(OPACITY_FULLY_OPAQUE);
            mTextPaint.setAntiAlias(true);
            mMiddleX = (mWidth - mBackground.getIntrinsicWidth()) / 2;

            mThreshold = ViewConfiguration.get(mContext).getScaledTouchSlop();
        }

        private void setDiff(int diff) {
        	if(DEBUG) Log.i(TAG, "setDiff()");
            if (diff == Integer.MAX_VALUE) {
                mCurrentKeyboard = null;
                mHitThreshold = false;
                return;
            }
            mDiff = diff;

            if (mDiff > mWidth) mDiff = mWidth;
            if (mDiff < -mWidth) mDiff = -mWidth;
            if (Math.abs(mDiff) > mThreshold) mHitThreshold = true;
            invalidateSelf();
        }

       

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            if (mHitThreshold) {
                Paint paint = mTextPaint;
                final int width = mWidth;
                final int height = mHeight;
                final int diff = mDiff;
                final Drawable lArrow = mLeftDrawable;
                final Drawable rArrow = mRightDrawable;
                canvas.clipRect(0, 0, width, height);
                if (mCurrentKeyboard == null) {
                    mCurrentKeyboard = mKeyboardSwitcher.getActiveIMShortname();
                    mNextKeyboard = mKeyboardSwitcher.getNextActivatedIMShortname();
                    mPrevKeyboard = mKeyboardSwitcher.getPrevActivatedIMShortname();
                    if (DEBUG)
                        Log.i(TAG, "SlidingSpaceBarDrawable:draw(), current=" + mCurrentKeyboard +
                                ". next = " + mNextKeyboard + ". prev = " + mPrevKeyboard);
                }


                // Draw language text with shadow
                final float baseline = mHeight * SPACEBAR_IMNAME_BASELINE - paint.descent();
                paint.setColor(mSpaceKeyTextColor);
                paint.setTextSize(mSlidingTextSize);
                canvas.drawText(mCurrentKeyboard, width / 2 + diff, baseline, paint);
                canvas.drawText(mNextKeyboard, diff - width / 5, baseline, paint);
                canvas.drawText(mPrevKeyboard, diff + width + width / 5, baseline, paint);

                setDefaultBounds(lArrow);
                rArrow.setBounds(width - rArrow.getIntrinsicWidth(), 0, width,
                        rArrow.getIntrinsicHeight());
                lArrow.draw(canvas);
                rArrow.draw(canvas);
            }
            if (mBackground != null) {
                canvas.translate(mMiddleX, 0);
                mBackground.draw(canvas);
            }
            canvas.restore();
        }
        
        
        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            // Ignore
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            // Ignore
        }

        @Override
        public int getIntrinsicWidth() {
            return mWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return mHeight;
        }
    }

}
