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

package net.toload.main.hd.candidate;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import net.toload.main.hd.LIMEService;
import net.toload.main.hd.R;
import net.toload.main.hd.data.Mapping;
import net.toload.main.hd.global.LIMEPreferenceManager;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;


public class CandidateView extends View implements View.OnClickListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "CandidateView";

    protected static final int OUT_OF_BOUNDS = -1;

    protected LIMEService mService;
    protected List<Mapping> mSuggestions;
    protected CandidateView mCandidateView;
    private TextView embeddedComposing;

    protected int mSelectedIndex;
    protected int mTouchX = OUT_OF_BOUNDS;

    //private boolean mTypedWordValid;
    private boolean mShowNumber; //Jeremy '11,5,25 for showing physical keyboard number or not.

    protected Rect mBgPadding;


    private static final int MAX_SUGGESTIONS = 500;
    private static final int SCROLL_PIXELS = 20;

    // Add by Jeremy '10, 3, 29.
    // Suggestions size. Set to MAX_GUGGESTIONS if larger then it.
    protected int mCount = 0;
    //Composing view
    private TextView mComposingTextView;
    private static TextView mComposingPopupTextView;
    private static PopupWindow mComposingTextPopup;

    //private String mComposingText = "";

    protected int[] mWordWidth = new int[MAX_SUGGESTIONS];
    protected int[] mWordX = new int[MAX_SUGGESTIONS];

    protected static int X_GAP = 12;

    private static final List<Mapping> EMPTY_LIST = new LinkedList<>();


    protected int mHeight;
    private int configHeight;
    private int currentX;

    protected int mColorBackground;
    protected int mColorNormalText;
    protected int mColorNormalTextHighlight;
    protected int mColorInvertedTextTransparent;

    protected int mColorComposingText;
    protected int mColorComposingBackground;


    protected int mColorComposingCodeHighlight;
    protected int mColorComposingCode;

    protected int mColorSpacer;

    protected Drawable mDrawableSuggestHighlight;
    protected Drawable mDrawableVoiceInput;
    protected Drawable mDrawableExpandButton;
    protected Drawable mDrawableCloseButton;

    protected int mColorSelKey;
    protected int mColorSelKeyShifted;
    
    protected int mVerticalPadding;
    protected int mExpandButtonWidth;

    protected Paint mCandidatePaint;
    protected Paint mSelKeyPaint;



    private boolean mScrolled;
    protected int mTargetScrollX;
    private String mDisplaySelkey = "1234567890";

    protected int mTotalWidth;

    private boolean goLeft = false;
    private boolean goRight = false;
    private boolean hasSlide = false;
    //private int bgcolor = 0;

    private View mCandidatePopupContainer;
    private PopupWindow mCandidatePopupWindow;

    protected int mScreenWidth;
    protected int mScreenHeight;

    protected GestureDetector mGestureDetector;
    protected final Context mContext;

    protected LIMEPreferenceManager mLIMEPref;

    private CandidateExpandedView mPopupCandidateView;
    private int mCloseButtonHeight;
    private ScrollView mPopupScrollView;
    private boolean candidateExpanded = false;

    private boolean waitingForMoreRecords = false;

    private boolean mTransparentCandidateView = false;

    //private Rect padding = null;

    /**
     * Construct a CandidateView for showing suggested words for completion.
     */

    public CandidateView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.LIMECandidateView);
    }
    public CandidateView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        mContext = context;

        mCandidateView = this;
        embeddedComposing = null;  // Jeremy '15,6,4 for embedded composing view in candidateView when floating candidateView (not fixed)

        mLIMEPref = new LIMEPreferenceManager(context);

        //Jeremy '16,7,24 get themed objects
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.LIMECandidateView, defStyle, R.style.LIMECandidateView);

        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.LIMECandidateView_suggestHighlight:
                    mDrawableSuggestHighlight = a.getDrawable(attr);
                    break;
                case R.styleable.LIMECandidateView_voiceInputIcon:
                    mDrawableVoiceInput = a.getDrawable(attr);
                    break;
                case R.styleable.LIMECandidateView_ExpandButtonIcon:
                    mDrawableExpandButton = a.getDrawable(attr);
                    break;
                case R.styleable.LIMECandidateView_closeButtonIcon:
                    mDrawableCloseButton = a.getDrawable(attr);
                    break;
                case R.styleable.LIMECandidateView_candidateBackground:
                    mColorBackground = a.getColor(attr,  ContextCompat.getColor(context, R.color.third_background_light));
                    break;
                case R.styleable.LIMECandidateView_composingTextColor:
                    mColorComposingText = a.getColor(attr,  ContextCompat.getColor(context, R.color.second_foreground_light));
                    break;
                case R.styleable.LIMECandidateView_composingBackgroundColor:
                    mColorComposingBackground = a.getColor(attr,  ContextCompat.getColor(context, R.color.composing_background_light));
                    break;
                case R.styleable.LIMECandidateView_candidateNormalTextColor:
                    mColorNormalText = a.getColor(attr,  ContextCompat.getColor(context, R.color.foreground_light));
                    break;
                case R.styleable.LIMECandidateView_candidateNormalTextHighlightColor:
                    mColorNormalTextHighlight = a.getColor(attr,  ContextCompat.getColor(context, R.color.foreground_light));
                    break;
                case R.styleable.LIMECandidateView_composingCodeColor:
                    mColorComposingCode = a.getColor(attr,  ContextCompat.getColor(context, R.color.color_common_green_hl));
                    break;
                case R.styleable.LIMECandidateView_composingCodeHighlightColor:
                    mColorComposingCodeHighlight = a.getColor(attr,  ContextCompat.getColor(context, R.color.third_background_light));
                    break;
                case R.styleable.LIMECandidateView_spacerColor:
                    mColorSpacer = a.getColor(attr,  ContextCompat.getColor(context, R.color.candidate_spacer));
                    break;
                case R.styleable.LIMECandidateView_selKeyColor:
                    mColorSelKey = a.getColor(attr,  ContextCompat.getColor(context, R.color.candidate_selection_keys));
                    break;
                case R.styleable.LIMECandidateView_selKeyShiftedColor:
                    mColorSelKeyShifted = a.getColor(attr,  ContextCompat.getColor(context, R.color.color_common_green_hl));
                    break;
            }
        }

        a.recycle();

       final Resources r = context.getResources();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mScreenWidth = screenSize.x;
        mScreenHeight = screenSize.y;

        mVerticalPadding = (int) (r.getDimensionPixelSize(R.dimen.candidate_vertical_padding) * mLIMEPref.getFontSize());
        configHeight = (int) (r.getDimensionPixelSize(R.dimen.candidate_stripe_height) * mLIMEPref.getFontSize());
        mHeight = configHeight + mVerticalPadding;
        mExpandButtonWidth = r.getDimensionPixelSize(R.dimen.candidate_expand_button_width);// *mLIMEPref.getFontSize());

        mCandidatePaint = new Paint();
        mCandidatePaint.setColor(mColorNormalText);
        mCandidatePaint.setAntiAlias(true);
        mCandidatePaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_size) * mLIMEPref.getFontSize());
        mCandidatePaint.setStrokeWidth(0);


        mSelKeyPaint = new Paint();
        mSelKeyPaint.setColor(mColorSelKey);
        mSelKeyPaint.setAntiAlias(true);
        mSelKeyPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_number_font_size) * mLIMEPref.getFontSize());
        mSelKeyPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        //final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //Jeremy '12,4,23 add mContext parameter.  The constructor without context is deprecated
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {

                if (DEBUG)
                    Log.i(TAG, "onScroll(): distanceX = " + distanceX + "; distanceY = " + distanceY);


                //Jeremy '12,4,8 filter out small scroll which is actually candidate selection.
                if (Math.abs(distanceX) < mHeight / 5 && Math.abs(distanceY) < mHeight / 5)
                    return true;

                mScrolled = true;

                // Update full candidate list before scroll
                checkHasMoreRecords();


                int sx = getScrollX();
                sx += distanceX;
                if (sx < 0) {
                    sx = 0;
                }
                if (sx + getWidth() > mTotalWidth) {
                    sx -= distanceX;
                }

                if (mLIMEPref.getParameterBoolean("candidate_switch", false)) {
                    hasSlide = true;
                    mTargetScrollX = sx;
                    scrollTo(sx, getScrollY());
                    currentX = getScrollX(); //Jeremy '12,7,6 set currentX to the left edge of current scrollview after scrolled
                } else {
                    hasSlide = false;
                    if (distanceX < 0) {
                        goLeft = true;
                        goRight = false;
                    } else if (distanceX > 0) {
                        goLeft = false;
                        goRight = true;
                    } else {
                        mTargetScrollX = sx;
                    }
                }

                return true;
            }
        });

    }

    /*
    * New embedded composing view inside candidate container for floating candidate mode. Jeremy '15,6,14
    * (android 5.1 does not allow popup composing go over candidate area).
     */
    public void setEmbeddedComposingView(TextView composingView) {
        if (DEBUG)
            Log.i(TAG, "setEmbeddedComposingView()");
        embeddedComposing = composingView;
    }

    private UIHandler mHandler = new UIHandler(this);

    private static class UIHandler extends Handler {

        private final WeakReference<CandidateView> mCandidateViewWeakReference;


        private static final int MSG_UPDATE_UI = 1;
        private static final int MSG_UPDATE_COMPOSING = 2;
        private static final int MSG_HIDE_COMPOSING = 3;
        private static final int MSG_SHOW_CANDIDATE_POPUP = 4;
        private static final int MSG_HIDE_CANDIDATE_POPUP = 5;
        private static final int MSG_SET_COMPOSING = 6;

        public UIHandler(CandidateView candiInstance) {
            mCandidateViewWeakReference = new WeakReference<>(candiInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) Log.i(TAG, "UIHandler.handlMessage(): message:" + msg.what);

            CandidateView mCandiInstance = mCandidateViewWeakReference.get();
            if (mCandiInstance == null) return;

            switch (msg.what) {
                case MSG_UPDATE_UI:
                    mCandiInstance.doUpdateUI();
                    break;
                case MSG_UPDATE_COMPOSING:
                    mCandiInstance.doUpdateComposing();
                    break;
                case MSG_HIDE_COMPOSING: {
                    mCandiInstance.doHideComposing();
                    break;
                }
                case MSG_SHOW_CANDIDATE_POPUP: {
                    mCandiInstance.doUpdateCandidatePopup();
                    break;
                }
                case MSG_HIDE_CANDIDATE_POPUP: {
                    mCandiInstance.doHideCandidatePopup();
                    break;
                }
                case MSG_SET_COMPOSING: {
                    String composingText = (String) msg.obj;
                    if (DEBUG)
                        Log.i(TAG, "UIHandler.handleMessage(): composingText" + composingText);
                    mCandiInstance.doSetComposing(composingText);
                    break;
                }
            }
        }

        public void updateUI(int delay) {

            sendMessageDelayed(obtainMessage(MSG_UPDATE_UI, 0, 0, null), delay);

        }

        public void setComposing(String text, int delay) {
            sendMessageDelayed(obtainMessage(MSG_SET_COMPOSING, 0, 0, text), delay);
        }

        public void updateComposing(int delay) {
            sendMessageDelayed(obtainMessage(MSG_UPDATE_COMPOSING, 0, 0, null), delay);
        }

        public void dismissComposing(int delay) {
            sendMessageDelayed(obtainMessage(MSG_HIDE_COMPOSING, 0, 0, null), delay);
        }

        public void showCandidatePopup(int delay) {
            sendMessageDelayed(obtainMessage(MSG_SHOW_CANDIDATE_POPUP, 0, 0, null), delay);
        }

        public void dismissCandidatePopup(int delay) {
            sendMessageDelayed(obtainMessage(MSG_HIDE_CANDIDATE_POPUP, 0, 0, null), delay);
        }


    }


    public void doUpdateUI() {

        if (DEBUG)
            Log.i(TAG, "doUpdateUI()");

        if ((mSuggestions == null || mSuggestions.isEmpty())
                && (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing())) {
            doHideCandidatePopup();
            return;
        }


        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing()) {
            doUpdateCandidatePopup();
        } else {
            if (!waitingForMoreRecords) {  // New suggestion list, reset scroll to (0,0);
                scrollTo(0, 0);
                mTargetScrollX = 0;
            }
            resetWidth();  // update layout width of this view
            invalidate();  // caused onDraw and update mTotoalX

        }
        waitingForMoreRecords = false;

    }

    protected void updateFontSize() {
        Resources r = mContext.getResources();
        float scaling = mLIMEPref.getFontSize();
        mVerticalPadding = (int) (r.getDimensionPixelSize(R.dimen.candidate_vertical_padding) * scaling);
        mCandidatePaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_size) * scaling);
        mSelKeyPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_number_font_size) * scaling);
        configHeight = (int) (r.getDimensionPixelSize(R.dimen.candidate_stripe_height) * scaling);
        if (DEBUG)
            Log.i(TAG, "updateFontSize(), scaling=" + scaling + ", mVerticalPadding=" + mVerticalPadding);
    }

    private void doHideCandidatePopup() {
        if (DEBUG)
            Log.i(TAG, "doHideCandidatePopup()");

        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing()) {
            mCandidatePopupWindow.dismiss();
            //resetWidth();
        }
        candidateExpanded = false;

        doUpdateUI();
    }

    /*
    * Contains requestLayout() which can only call from UI thread
    */
    private void resetWidth() {
        if (DEBUG)
            Log.i(TAG, "resetWidth() mHeight:" + mHeight);
        int candiWidth = mScreenWidth;
        if (mTotalWidth > mScreenWidth || isEmpty()) candiWidth -= mExpandButtonWidth;
        if (DEBUG)
            Log.i(TAG, "resetWidth() candiWidth:" + candiWidth);
        this.setLayoutParams(new LinearLayout.LayoutParams(candiWidth, mHeight));
        requestLayout();
    }


    public void doUpdateCandidatePopup() {
        if (DEBUG)
            Log.i(TAG, "doUpdateCandidatePopup(), mHeight:" + mHeight);

        //Jeremy '11,8.27 do vibrate and sound on candidateview expand button pressed.
        if (!candidateExpanded)
            mService.doVibrateSound(0);

        candidateExpanded = true;
        requestLayout();

        checkHasMoreRecords();

        if (mCandidatePopupWindow == null) {

            mCandidatePopupWindow = new PopupWindow(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mCandidatePopupContainer = inflater.inflate(R.layout.candidatepopup, (ViewGroup) this.getRootView(), false);

            mCandidatePopupContainer.setBackgroundColor(mColorBackground);

            mCandidatePopupWindow.setContentView(mCandidatePopupContainer);

            ImageButton btnClose = (ImageButton) mCandidatePopupContainer.findViewById(R.id.closeButton);
            if (btnClose != null) {
                btnClose.setOnClickListener(this);
                btnClose.setImageDrawable(mDrawableCloseButton);
                btnClose.setBackgroundColor(mColorBackground);
                btnClose.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                mCloseButtonHeight = btnClose.getMeasuredHeight();
            }

            mPopupScrollView = (ScrollView) mCandidatePopupContainer.findViewById(R.id.sv);

            CandidateExpandedView popupCandidate =
                    (CandidateExpandedView) mCandidatePopupContainer.findViewById(R.id.candidatePopup);
            popupCandidate.setParentCandidateView(this);
            popupCandidate.setParentScrollView(mPopupScrollView);
            popupCandidate.setService(mService);

            mPopupCandidateView = popupCandidate;


        }

        if (mSuggestions.size() == 0) return;


        mCandidatePopupWindow.setContentView(mCandidatePopupContainer);
        int[] offsetOnScreen = new int[2];
        this.getLocationOnScreen(offsetOnScreen);

        mPopupCandidateView.setSuggestions(mSuggestions);
        mPopupCandidateView.prepareLayout();

        mPopupCandidateView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        int popHeight = mScreenHeight - offsetOnScreen[1];
        if (mPopupCandidateView.getMeasuredHeight() + mCloseButtonHeight < popHeight)
            popHeight = mPopupCandidateView.getMeasuredHeight() + mCloseButtonHeight;

        if (!hasRoomForExpanding()) {
            popHeight = 3 * (configHeight + mVerticalPadding) + mCloseButtonHeight;

            if (DEBUG)
                Log.i(TAG, "doUpdateCandidatePopup(), " +
                        "no enough room for expanded view, expand self first. newHeight:" + popHeight);

            if (mPopupCandidateView.getMeasuredHeight() + mCloseButtonHeight < popHeight)
                popHeight = mPopupCandidateView.getMeasuredHeight() + mCloseButtonHeight;
            this.setLayoutParams(
                    new LinearLayout.LayoutParams(mScreenWidth - mExpandButtonWidth, popHeight));
        }

        if (DEBUG)
            Log.i(TAG, "doUpdateCandidatePopup(), mHeight=" + mHeight
                            + ", getHeight() = " + getHeight()
                            + ", mPopupCandidateView.getHeight() = " + mPopupCandidateView.getHeight()
                            + ", mPopupScrollView.getHeight() = " + mPopupScrollView.getHeight()
                            + ", offsetOnScreen[1] = " + offsetOnScreen[1]
                            + ", popHeight = " + popHeight
                            + ", CandidateExpandedView.measureHeight = " + mPopupCandidateView.getMeasuredHeight()
                            + ", btnClose.getMeasuredHeight() = " + mCloseButtonHeight
            );


        if (mCandidatePopupWindow.isShowing()) {
            if (DEBUG)
                Log.i(TAG, "doUpdateCandidatePopup(),mCandidatePopup.isShowing ");
            mCandidatePopupWindow.update(mScreenWidth, popHeight);
        } else {
            mCandidatePopupWindow.setWidth(mScreenWidth);
            mCandidatePopupWindow.setHeight(popHeight);
            mCandidatePopupWindow.showAsDropDown(this, 0, -getHeight());
            mPopupScrollView.scrollTo(0, 0);
        }

        //Jeremy '12,5,31 do update layoutparams after popupWindow update or creation.
        mPopupCandidateView.setLayoutParams(
                new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT
                        , popHeight - mCloseButtonHeight));

        mPopupScrollView.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                        , popHeight - mCloseButtonHeight));


    }

    public void setComposingText(String composingText) {
        if (DEBUG)
            Log.i(TAG, "setComposingText():composingText:" + composingText);
        if (!composingText.trim().equals("")) {
            mHandler.setComposing(composingText, 0);
            showComposing();
        } else {
            hideComposing();
        }


    }

    public void doHideComposing() {

        if (DEBUG)
            Log.i(TAG, "doHideComposing()");

        if (mComposingTextView == null) return;

        if (embeddedComposing != null || // for embedded composing in floating candidateView
                (mComposingTextPopup != null  // for fixed candidate View
                        && (mComposingTextPopup.isShowing()) || mComposingTextView.getVisibility() == VISIBLE)) {

            mComposingTextView.setVisibility(INVISIBLE);
        }
    }

    /**
     * Jeremy '12,6,2 separated from doupdateComposing
     */

    public void doSetComposing(String composingText) {
        if (DEBUG)
            Log.i(TAG, "doSetComposing():" + composingText + "; this.isShown()" + this.isShown() +
                    "(mComposingTextView == null):" + (mComposingTextView == null) +
                    ";(embeddedComposing == null):" + (embeddedComposing != null));

        // Initialize mComposingTextView as embedding composing or popup window for fixed candidate mode. Jeremy '15,6,4

        if (embeddedComposing != null) {
            if( mComposingTextView != embeddedComposing ) {
                mComposingTextView = embeddedComposing;
                mComposingTextView.setBackgroundColor(mColorComposingBackground);
                mComposingTextView.setTextColor(mColorComposingText);
            }

        } else {
            if (mComposingPopupTextView == null) {
                LayoutInflater inflater
                        = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mComposingPopupTextView = (TextView) inflater.inflate(R.layout.composingtext, (ViewGroup) getRootView(), false);

                if (mComposingTextPopup == null) {
                    mComposingTextPopup = new PopupWindow(mContext);
                    mComposingTextPopup.setTouchable(false);
                }
                //mComposingTextPopup.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  //Deprecated in API 23. Jeremy '16,7,16
                mComposingTextPopup.setWidth(LayoutParams.WRAP_CONTENT);
                mComposingTextPopup.setHeight(LayoutParams.WRAP_CONTENT);
                mComposingTextPopup.setContentView(mComposingPopupTextView);
                mComposingTextPopup.setBackgroundDrawable(null);
            }
            if (mComposingTextView != mComposingPopupTextView) {
                mComposingTextView = mComposingPopupTextView;
                mComposingTextView.setBackgroundColor(mColorComposingBackground);
                mComposingTextView.setTextColor(mColorComposingText);

            }
        }


        if (composingText != null) {
            mComposingTextView.setText(composingText);
            //The textsize got is coverted into PX already. Thus force setup the setTextSize in unit of PX.
            float scaledTextSize =
                    mContext.getResources().getDimensionPixelSize(R.dimen.composing_text_size) * mLIMEPref.getFontSize();
            mComposingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize);

        } else
            return;

        mComposingTextView.invalidate();  //Jeremy '12,6,2 invalidate and measure so as to get correct height and width later. 
        mComposingTextView.setVisibility(VISIBLE);

        //Jeremy '15,6, 4 bypass updating popup when composing view is embedded in candidate container

        if (embeddedComposing == null)
            doUpdateComposing();

    }

    /**
     * Update composing to correct location with a delay after setComposing.
     */

    public void doUpdateComposing() {
        if (DEBUG)
            Log.i(TAG, "doUpdateComposing(): this.isShown()" + this.isShown() +
                    "; embeddedComposing is null:" + (embeddedComposing == null));


        if (embeddedComposing != null)
            return; //Jeremy '15,6, 4 bypass updating popup when composing view is embedded in candidate container

        //mComposingTextView.measure(
                //MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                //MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        //final int popupWidth = mComposingTextView.getMeasuredWidth();  //Jeremy '12,6,2 use getWidth and getHeight instead
        //final int popupHeight = mComposingTextView.getMeasuredHeight();
        // getMeasuredWidth cannot get correct width of textVIEW in android 6 Jeremy '16,7,16
        String composingText =  String.valueOf(mComposingTextView.getText());
        if(composingText == null) return;  // avoid measureText on null object.  Jeremy '16/7/26

        Paint paint = mComposingTextView.getPaint();
        Paint.FontMetrics metrics = paint.getFontMetrics();
        final int popupWidth = (int) paint.measureText(composingText);
        final int popupHeight = (int)(metrics.bottom - metrics.top);


        int[] offsetInWindow = new int[2];
        this.getLocationInWindow(offsetInWindow);
        int mPopupComposingY = offsetInWindow[1];
        int mPopupComposingX = 0;

        mPopupComposingY -= popupHeight;


        if (DEBUG)
            Log.i(TAG, "doUpdateComposing():mPopupComposingX:" + mPopupComposingX
                    + ". mPopupComposingY:" + mPopupComposingY
                    + ". popupWidth = " + popupWidth
                    + ". popupHeight = " + popupHeight
                    + ". mComposingTextPopup.isShowing()=" + mComposingTextPopup.isShowing());


        try {
            if (mComposingTextPopup.isShowing()) {
                mComposingTextPopup.update(mPopupComposingX, mPopupComposingY,
                        popupWidth, popupHeight);
            } else {
                mComposingTextPopup.setWidth(popupWidth);
                mComposingTextPopup.setHeight(popupHeight);
                mComposingTextPopup.showAtLocation(this, Gravity.NO_GRAVITY, mPopupComposingX,
                        mPopupComposingY);
            }
        }catch(Exception e){
            // ignore candidate construct error
            e.printStackTrace();
        }

    }

    public void showComposing() {
        if (DEBUG)
            Log.i(TAG, "showComposing()");
        //jeremy '12,6,3 moved the creation of mComposingTextPopup and mComposingTextView from doUpdateComposing
        //Jeremy '12,4,8 to avoid fc when hard keyboard is engaged and candidateview is not shown
        if (!this.isShown()) return;

        mHandler.updateComposing(50);

    }

    public void hideComposing() {
        if (DEBUG)
            Log.i(TAG, "hideComposing()");
        mHandler.dismissComposing(100); //Jeremy '12,6,3 the same delay as showComposing to avoid showed after hided

    }

    public void showCandidatePopup() {
        if (DEBUG)
            Log.i(TAG, "showCandidatePopup()");

        mHandler.showCandidatePopup(0);


    }

    public void hideCandidatePopup() {
        if (DEBUG)
            Log.i(TAG, "hideCandidatePopup()");

        mHandler.dismissCandidatePopup(0);

    }


    public boolean isCandidateExpanded() {
        return candidateExpanded;
    }

    private boolean mHasRoomForExpanding = true;

    public boolean hasRoomForExpanding() {
        if (!mCandidatePopupWindow.isShowing()) {
            int[] offsetOnScreen = new int[2];
            this.getLocationOnScreen(offsetOnScreen);
            mHasRoomForExpanding = (mScreenHeight - offsetOnScreen[1]) > 2 * mHeight;
        }
        return mHasRoomForExpanding;
    }

    public void setTransparentCandidateView(boolean transparent){
        mTransparentCandidateView = transparent;
    }

    /**
     * A connection back to the service to communicate with the text field
     */
    public void setService(LIMEService listener) {
        mService = listener;
    }

    @Override
    public int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (DEBUG)
            Log.i(TAG, "onMeasure()");
        int measuredWidth = resolveSize(mTotalWidth, widthMeasureSpec);

        final int desiredHeight = mHeight;

        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
    }


    /**
     * If the canvas is null, then only touch calculations are performed to pick the target
     * candidate.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        doDraw(canvas);
    }

    private void prepareLayout() {
        doDraw(null);
    }

    private void doDraw(Canvas canvas) {


        if (mSuggestions == null) return;
        if (DEBUG)
            Log.i(TAG, "CandidateView:doDraw():Suggestion mCount:" + mCount + " mSuggestions.size:" + mSuggestions.size());
        mTotalWidth = 0;

        updateFontSize();

        if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
        }

        final int height = mHeight;
        final Rect bgPadding = mBgPadding;
        final Paint candidatePaint = mCandidatePaint;
        final Paint candidateEmojiPaint = mCandidatePaint;
                    candidateEmojiPaint.setTextSize((float) (candidateEmojiPaint.getTextSize() * 0.9));

        final Paint selKeyPaint = mSelKeyPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;

        final int textBaseLine = (int) (((height - mCandidatePaint.getTextSize()) / 2) - mCandidatePaint.ascent());

        // Modified by jeremy '10, 3, 29.  Update mselectedindex if touched and build wordX[i] and wordwidth[i]
        int x = 0;
        final int count = mCount; //Cache count here '11,8,18
        for (int i = 0; i < count; i++) {
            if (count != mCount || mSuggestions == null || count != mSuggestions.size()
                    || mSuggestions.size() == 0 || i >= mSuggestions.size())
                return;  // mSuggestion is updated, force abort

            String suggestion = mSuggestions.get(i).getWord();
            if (i == 0 && mSuggestions.size() > 1 && mSuggestions.get(1).isRuntimeBuiltPhraseRecord() && suggestion.length() > 8) {
                suggestion = suggestion.substring(0, 2) + "..";
            }
            float base = (suggestion == null)? 0: candidatePaint.measureText("。");
            float textWidth = (suggestion == null)? 0: candidatePaint.measureText(suggestion);

            if( textWidth < base){
                textWidth = base;
            }


            final int wordWidth = (int) textWidth + X_GAP * 2;

            mWordX[i] = x;

            mWordWidth[i] = wordWidth;


            if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth && !scrolled) {
                mSelectedIndex = i;
            }
            x += wordWidth;
        }

        mTotalWidth = x;

        if (DEBUG)
            Log.i(TAG, "CandidateView:doDraw():mTotalWidth :" + mTotalWidth + "  this.getWidth():" + this.getWidth());

        //Jeremy '11,8,11. If the candidate list is within 1 page and has more records, get full records first.
        if (mTotalWidth < this.getWidth())
            checkHasMoreRecords();




        // Paint all the suggestions and lines.
        if (canvas != null) {

            // Moved from above by jeremy '10 3, 29. Paint mSelectedindex in highlight here
            if ( count > 0 && mSelectedIndex >= 0) {
                //    candidatePaint.setColor(mColorComposingCode);
                //    canvas.drawRect(mWordX[mSelectedIndex],bgPadding.top, mWordWidth[mSelectedIndex] , height, candidatePaint);

                canvas.translate(mWordX[mSelectedIndex], 0);
                mDrawableSuggestHighlight.setBounds(0, bgPadding.top, mWordWidth[mSelectedIndex], height);
                mDrawableSuggestHighlight.draw(canvas);
                canvas.translate(-mWordX[mSelectedIndex], 0);

            }
            if(mTransparentCandidateView){
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                Paint backgroundPaint = new Paint();
                backgroundPaint.setColor(ContextCompat.getColor(mContext, R.color.third_background_light));
                backgroundPaint.setAlpha(33);
                backgroundPaint.setStyle(Paint.Style.FILL);

                canvas.drawRect(0.5f, bgPadding.top, mScreenWidth, height, backgroundPaint);
            }


            for (int i = 0; i < count; i++) {

                if (count != mCount || mSuggestions == null || count != mSuggestions.size()
                        || mSuggestions.size() == 0 || i >= mSuggestions.size()) break;

                boolean isEmoji = mSuggestions.get(i).isEmojiRecord();
                String suggestion = mSuggestions.get(i).getWord();
                if (i == 0 && mSuggestions.size() > 1 && mSuggestions.get(1).isRuntimeBuiltPhraseRecord() && suggestion.length() > 8) {
                    suggestion = suggestion.substring(0, 2) + "..";
                }

                int c = i + 1;
                switch (mSuggestions.get(i).getRecordType()) {
                    case Mapping.RECORD_COMPOSING_CODE:
                       if (mSelectedIndex == 0) {

                           if(mTransparentCandidateView){
                               candidatePaint.setColor(mColorInvertedTextTransparent);
                           }else{
                               candidatePaint.setColor(mColorComposingCodeHighlight);
                           }
                       }
                       else candidatePaint.setColor(mColorComposingCode);
                        break;
                    case Mapping.RECORD_CHINESE_PUNCTUATION_SYMBOL:
                    case Mapping.RECORD_RELATED_PHRASE:
                        selKeyPaint.setColor(mColorSelKeyShifted);
                        if(i == mSelectedIndex)
                            candidatePaint.setColor(mColorNormalTextHighlight);
                        else
                            candidatePaint.setColor(mColorNormalText);
                        break;
                    case Mapping.RECORD_EXACT_MATCH_TO_CODE:
                    case Mapping.RECORD_PARTIAL_MATCH_TO_CODE:
                    case Mapping.RECORD_RUNTIME_BUILT_PHRASE:
                    case Mapping.RECORD_ENGLISH_SUGGESTION:
                    default:
                        selKeyPaint.setColor(mColorSelKey);
                        if(i == mSelectedIndex)
                            candidatePaint.setColor(mColorNormalTextHighlight);
                        else
                            candidatePaint.setColor(mColorNormalText);
                        break;

                }

                if(isEmoji){
                    canvas.drawText(suggestion, mWordX[i] + X_GAP, Math.round(textBaseLine*0.95), candidateEmojiPaint);
                }else{
                    canvas.drawText(suggestion, mWordX[i] + X_GAP, textBaseLine, candidatePaint);
                }
                if (mShowNumber) {
                    //Jeremy '11,6,17 changed from <=10 to mDisplaySelkey length. The length maybe 11 or 12 if shifted with space.
                    if (c <= mDisplaySelkey.length()) {
                        //Jeremy '11,6,11 Drawing text using relative font dimensions.
                        canvas.drawText(mDisplaySelkey.substring(c - 1, c),
                                mWordX[i] + mWordWidth[i] - height * 0.3f, height * 0.4f, selKeyPaint);
                    }
                }
                //Draw spacer
                candidatePaint.setColor(mColorSpacer);
                canvas.drawLine(mWordX[i] + mWordWidth[i] + 0.5f, bgPadding.top + (mVerticalPadding/2), mWordX[i] + mWordWidth[i] + 0.5f, height - (mVerticalPadding/2), candidatePaint);
                candidatePaint.setFakeBoldText(false);

            }

            if (mTargetScrollX != getScrollX()) {
                if (DEBUG)
                    Log.i(TAG, "CandidateView:doDraw():mTargetScrollX :" + mTargetScrollX + "  getScrollX():" + getScrollX());
                scrollToTarget();
            }


        }


    }


    private boolean checkHasMoreRecords() {
        if (DEBUG)
            Log.i(TAG, "checkHasMoreRecords(), waitingForMoreRecords = " + waitingForMoreRecords);

        if (waitingForMoreRecords)
            return false; //Jeremy '12,7,6 avoid repeated calls of requestFullrecords().
        if (mSuggestions != null && mSuggestions.size() > 0 &&
                mSuggestions.get(mSuggestions.size() - 1).getCode() != null &&
                mSuggestions.get(mSuggestions.size() - 1).isHasMoreRecordsMarkRecord()) {  //getCode().equals("has_more_records")) {
            waitingForMoreRecords = true;
            Thread updatingThread = new Thread() {

                public void run() {
                    mService.requestFullRecords(mSuggestions.get(0).isRelatedPhraseRecord());
                }
            };
            updatingThread.start();
            return true;
        }
        return false;
    }

    private void scrollToTarget() {
        int sx = getScrollX();
        if (mTargetScrollX > sx) {
            sx += SCROLL_PIXELS;
            if (sx >= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        } else {
            sx -= SCROLL_PIXELS;
            if (sx <= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        }
        scrollTo(sx, getScrollY());

        invalidate();
    }


    public void setSuggestions(List<Mapping> suggestions, boolean showNumber, String displaySelkey) {
        mDisplaySelkey = displaySelkey;
        setSuggestions(suggestions, showNumber);

    }

    public synchronized void setSuggestions(List<Mapping> suggestions, boolean showNumber) {
        //clear();
        //Jeremy '11,8,14 moved from clear();
        if (DEBUG)
            Log.i(TAG, "setSuggestions()");

        Resources res = mContext.getResources();

        configHeight = (int) (res.getDimensionPixelSize(
                R.dimen.candidate_stripe_height) * mLIMEPref.getFontSize());
        mVerticalPadding = (int) (res.getDimensionPixelSize(R.dimen.candidate_vertical_padding) * mLIMEPref.getFontSize());
        mHeight = configHeight + mVerticalPadding;

        currentX = 0;
        mTouchX = OUT_OF_BOUNDS;
        mCount = 0;
        mSelectedIndex = -1;

        if (mLIMEPref.getDisablePhysicalSelKeyOption()) {
            showNumber = true;
        }

        mShowNumber = showNumber;

        if (mShowNumber)
            X_GAP = (int) (res.getDimensionPixelSize(R.dimen.candidate_font_size) * 0.35f);//13;
        else
            X_GAP = (int) (res.getDimensionPixelSize(R.dimen.candidate_font_size) * 0.25f);


        if (suggestions != null) {
            mSuggestions = new LinkedList<>(suggestions);

            if (mSuggestions.size() > 0) {
                // Add by Jeremy '10, 3, 29
                mCount = mSuggestions.size();
                if (mCount > MAX_SUGGESTIONS) mCount = MAX_SUGGESTIONS;

                if (DEBUG)
                    Log.i(TAG, "setSuggestions():mSuggestions.size():" + mSuggestions.size()
                            + " mCount=" + mCount);

                if (mCount > 1 && mSuggestions.get(1).isExactMatchToCodeRecord()) {
                    mSelectedIndex = 1;
                } else if (mCount > 0 && (mSuggestions.get(0).isComposingCodeRecord() || mSuggestions.get(0).isRuntimeBuiltPhraseRecord())) {
/*
                    int seloption = mLIMEPref.getSelkeyOption();
                    if(seloption > 0 && suggestions.size() > seloption){
                        mSelectedIndex = seloption;
                    }else{*/
                    mSelectedIndex = 0;
                    //}

                } else {
                    // no default selection for related phrase, chinese punctuation symbols1 and English suggestions  Jeremy '15,6,4
                    mSelectedIndex = -1;
                }
            } else {
                if (DEBUG)
                    Log.i(TAG, "setSuggestions():mSuggestions=null");
            }
        } else {
            mSuggestions = new LinkedList<>();
            hideCandidatePopup();
        }

        prepareLayout();


        mHandler.updateUI(0);


    }


    public void clear() {
        if (DEBUG) Log.i(TAG, "clear()");
        //mHeight =0; //Jeremy '12,5,6 hide candidate bar when candidateview is fixed.
        if (mSuggestions != null) mSuggestions.clear();
        mCount = 0;
        // Jeremy 11,8,14 close all popup on clear
        setComposingText("");
        mTargetScrollX = 0;
        mTotalWidth = 0;
        hideComposing();


        prepareLayout();
        mHandler.updateUI(0);

        Resources r = mContext.getResources();
        configHeight = (int) (r.getDimensionPixelSize(
                R.dimen.candidate_stripe_height) * mLIMEPref.getFontSize());
        mVerticalPadding = (int) (r.getDimensionPixelSize(R.dimen.candidate_vertical_padding) * mLIMEPref.getFontSize());
        configHeight = (int) (r.getDimensionPixelSize(R.dimen.candidate_stripe_height) * mLIMEPref.getFontSize());
        mHeight = configHeight + mVerticalPadding;

    }
    public boolean isEmpty(){
        return mCount ==0;
    }

    public void startVoiceInput(){
        if(mService!=null)
            mService.startVoiceInput();
    }

    //Jeremy '12,5,6 hide candidate bar when candidateView is fixed.
    public void forceHide() {
        if (DEBUG)
            Log.i(TAG, "forceHide()");
        mHeight = 0;
        //clear();
        //resetWidth();// will cause wrong thread exception. clear() will call updateUI() and will do resetWidth
        mSuggestions = EMPTY_LIST;
        // Jeremy 11,8,14 close all popup on clear
        setComposingText("");
        mTargetScrollX = 0;
        mTotalWidth = 0;
        mHandler.dismissComposing(0);
        mHandler.updateUI(0);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        if (DEBUG)
            Log.i(TAG, "OnTouchEvent() action = " + me.getAction());
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(me)) {
            if (DEBUG)
                Log.i(TAG, "OnTouchEvent() event processed by mGestureDetector");
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchX = x;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrolled = false;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (y <= 0) {
                    // Fling up!?
                    if (mSelectedIndex >= 0) {
                        takeSelectedSuggestion(true);
                        mSelectedIndex = -1;
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (DEBUG)
                    Log.i(TAG, "OnTouchEvent():MotionEvent.ACTION_UP, mScrolled=" + mScrolled + "; mSelectedIndex = " + mSelectedIndex);
                if (!mScrolled) {
                    if (mSelectedIndex >= 0) {
                        takeSelectedSuggestion(true);
                    }
                }
                mSelectedIndex = -1;
                removeHighlight();
                requestLayout();

                if (!hasSlide) {
                    if (goLeft) {
                        scrollPrev();
                    }
                    if (goRight) {
                        scrollNext();
                    }
                }
                performClick();
                break;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();
        return true;
    }

    public void scrollPrev() {
        int i = 0;
        //final int mCount = mSuggestions.size();
        int firstItem = 0; // Actually just before the first item, if at the boundary
        while (i < mCount) {
            if (mWordX[i] < currentX
                    && mWordX[i] + mWordWidth[i] >= currentX - 1) {
                firstItem = i;
                break;
            }
            i++;
        }
        int leftEdge = mWordX[firstItem] + mWordWidth[firstItem] - getWidth();
        if (leftEdge < 0) {
            leftEdge = 0;
            currentX = leftEdge;
        } else {
            currentX = leftEdge;
        }
        updateScrollPosition(leftEdge);
    }


    public void scrollNext() {
        if (DEBUG)
            Log.i(TAG, "scrollNext(), currentX = " + currentX + ", mSelectedIndex = " + mSelectedIndex);
        checkHasMoreRecords(); //Jeremy '12,7,6 check if has more records before scroll
        int i = 0;
        int targetX = currentX;
        //final int mCount = mSuggestions.size();
        int rightEdge = currentX + getWidth();
        while (i < mCount) {
            if (mWordX[i] <= rightEdge &&
                    mWordX[i] + mWordWidth[i] >= rightEdge) {
                targetX = Math.min(mWordX[i], mTotalWidth - getWidth());
                currentX = mWordX[i];
                break;
            }
            i++;
        }
        if (DEBUG)
            Log.i(TAG, "scrollNext(), new currentX = " + currentX + ", new mSelectedIndex = " + mSelectedIndex);
        updateScrollPosition(targetX);
    }

    private void updateScrollPosition(int targetX) {
        if (targetX != mTouchX) {
            mTargetScrollX = targetX;
            requestLayout();
            invalidate();
            mScrolled = true;
        }
    }

    //Add by Jeremy '10, 3, 29 for DPAD (physical keyboard) selection.
    public void selectNext() {
        if (DEBUG)
            Log.i(TAG, "selectNext(), currentX = " + currentX + ", mSelectedIndex = " + mSelectedIndex);
        if (mSuggestions == null) return;
        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing()) {
            mPopupCandidateView.selectNext();
        } else {
            if (mSelectedIndex < mCount - 1) {
                mSelectedIndex++;
                if (mWordX[mSelectedIndex] + mWordWidth[mSelectedIndex] > currentX + getWidth())
                    scrollNext();
                //Jeremy '12,7,6 if the selected index is not in current visible area, set the selected index to the fist item visible
                int rightEdge = currentX + getWidth();
                if (mWordX[mSelectedIndex] < currentX ||
                        mWordX[mSelectedIndex] + mWordWidth[mSelectedIndex] > rightEdge) {
                    for (int i = 0; i < mCount - 1; i++)
                        if (mWordX[i] >= currentX) {
                            mSelectedIndex = i;
                            break;
                        }
                }
            }
            invalidate();
        }
    }

    public void selectPrev() {
        if (DEBUG)
            Log.i(TAG, "selectPrev(), currentX = " + currentX + ", mSelectedIndex = " + mSelectedIndex);
        if (mSuggestions == null) return;
        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing()) {
            mPopupCandidateView.selectPrev();
        } else {
            if (mSelectedIndex > 0) {
                mSelectedIndex--;
                if (mWordX[mSelectedIndex] < currentX) scrollPrev();

            }
            //Jeremy '12,7,6 if the selected index is not in current visible area, set the selected index to the last item visible
            int rightEdge = currentX + getWidth();
            if (mSelectedIndex == -1 ||
                    mWordX[mSelectedIndex] < currentX ||
                    mWordX[mSelectedIndex] + mWordWidth[mSelectedIndex] > rightEdge) {
                for (int i = mCount - 2; i < mCount - 1; i--)
                    if (mWordX[i] + mWordWidth[i] <= rightEdge) {
                        mSelectedIndex = i;
                        break;
                    }
            }
            invalidate();
        }
    }

    //Jeremy '11,8,28
    public void selectNextRow() {
        if (mSuggestions == null) return;
        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing())
            mPopupCandidateView.selectNextRow();
        else if (mScreenWidth < mTotalWidth)
            showCandidatePopup();

    }

    public void selectPrevRow() {
        if (mSuggestions == null) return;
        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing())
            mPopupCandidateView.selectPrevRow();

    }

    public boolean takeSuggstionAtIndex(int index) {
        if (DEBUG) {
            Log.i(TAG, "takeSuggestion():mSelectedIndex:" + mSelectedIndex);
        }


        if (mSuggestions != null && index >= 0 && index <= mSuggestions.size()) {
            mService.pickCandidateManually(index);
            return true;  // Selection picked
        } else
            return false;
    }

    public boolean takeSelectedSuggestion() {
        return this.takeSelectedSuggestion(false);
    }

    public boolean takeSelectedSuggestion(boolean vibrateSound) {
        if (DEBUG) {
            Log.i(TAG, "takeSelectedSuggestion():mSelectedIndex:" + mSelectedIndex);
        }
        //Jeremy '11,9,1 do vibrate and sound on suggestion picked from candidateview
        if (vibrateSound) mService.doVibrateSound(0);
        hideComposing(); //Jeremy '12,5,6
        if (mCandidatePopupWindow != null && mCandidatePopupWindow.isShowing()) {
            hideCandidatePopup();
            return takeSuggstionAtIndex(mPopupCandidateView.mSelectedIndex);
        } else
            return takeSuggstionAtIndex(mSelectedIndex);

    }

    /**
     * For flick through from keyboard, call this method with the x coordinate of the flick
     * gesture.
     */
    /*public void takeSuggestionAt(float x) {

        mTouchX = (int) x;
        // To detect candidate
        onDraw(null);
        takeSelectedSuggestion();
        invalidate();
    }*/
    private void removeHighlight() {
        mTouchX = OUT_OF_BOUNDS;
        invalidate();
    }

    @Override
    public void onDetachedFromWindow() {
        if (DEBUG) Log.i(TAG, "onDetachedFromWindow() ");
        super.onDetachedFromWindow();
        hideComposing();
        hideCandidatePopup();
    }

    @Override
    public void onClick(View v) {
        //Jeremy '11,8.27 do vibrate and sound on candidateexpandedview close button pressed.
        mService.doVibrateSound(0);

        hideCandidatePopup();
    }


}
