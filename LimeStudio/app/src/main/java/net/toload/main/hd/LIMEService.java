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

package net.toload.main.hd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import net.toload.main.hd.candidate.CandidateInInputViewContainer;
import net.toload.main.hd.candidate.CandidateView;
import net.toload.main.hd.candidate.CandidateViewContainer;
import net.toload.main.hd.data.ChineseSymbol;
import net.toload.main.hd.data.Keyboard;
import net.toload.main.hd.data.Mapping;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.global.LIMEUtilities;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard;
import net.toload.main.hd.keyboard.LIMEKeyboard;
import net.toload.main.hd.keyboard.LIMEKeyboardBaseView;
import net.toload.main.hd.keyboard.LIMEKeyboardView;
import net.toload.main.hd.keyboard.LIMEMetaKeyKeyListener;
import net.toload.main.hd.limesettings.LIMEPreferenceHC;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class LIMEService extends InputMethodService implements
        LIMEKeyboardBaseView.OnKeyboardActionListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "LIMEService";

    private static Thread queryThread; // queryThread for no-blocking I/O  Jeremy '15,6,1

    static final int KEYCODE_SWITCH_TO_SYMBOL_MODE = -2;
    static final int KEYCODE_SWITCH_TO_ENGLISH_MODE = -9;
    static final int KEYCODE_SWITCH_TO_IM_MODE = -10;
    static final int KEYCODE_SWITCH_SYMBOL_KEYBOARD = -15;

    //Jeremy '16,7,22 To control delayed hiding candidate view and avoid hide and show candidate view in short time.
    private static final int DELAY_BEFORE_HIDE_CANDIDATE_VIEW = 200;

    private LIMEKeyboardView mInputView = null;
    private CandidateInInputViewContainer mCandidateInInputView = null;//Jeremy'12,5,3
    private boolean mFixedCandidateViewOn; //Jeremy'12,5,3
    private CandidateView mCandidateView = null;
    private CandidateView mCandidateViewInInputView = null;
    private CandidateView mCandidateViewStandAlone = null;
    private CandidateViewContainer mCandidateViewContainer = null;
    private CompletionInfo[] mCompletions;
    private TextView candidateHintView = null;

    private StringBuilder mComposing = new StringBuilder();

    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private boolean mCapsLock;
    private boolean mAutoCap;
    private boolean mHasShift;

    private boolean mEnglishOnly;
    private boolean mEnglishFlagShift;
    private boolean mPersistentLanguageMode;  //Jeremy '12,5,1
    private int mShowArrowKeys; //Jeremy '12,5,22 force recreate keyboard if show arrow keys mode changes.
    private int mSplitKeyboard; //Jeremy '12,5,26 force recreate keyboard if split keyboard settings changes; 6/19 changed to int

    public boolean hasMappingList = false;

    private long mMetaState;
    private int mImeOptions;

    LIMEKeyboardSwitcher mKeyboardSwitcher;

    private int mOrientation;
    private int mHardkeyboardHidden;
    private boolean mPredicting;

    private Context mThemeContext;

    private Mapping selectedCandidate; //Jeremy '12,5,7 renamed from firstMacthed
    //private int selectedIndex; //Jeremy '12,5,7 the index in resultList of selectedCandidate
    private Mapping committedCandidate; //Jeremy '12,5,7 renamed from tempMatched

    private StringBuffer tempEnglishWord;
    private List<Mapping> tempEnglishList;

    private boolean hasPhysicalKeyPressed;

    //private String mWordSeparators;
    //private String misMatched;  //Removed by Jeremy '13,1,10

    private LinkedList<Mapping> mCandidateList; //Jeremy '12,5,7 renamed from templist

    private Vibrator mVibrator;
    private AudioManager mAudioManager;


    private boolean hasVibration = false;
    private boolean hasSound = false;
    private boolean hasNumberMapping = false;
    private boolean hasSymbolMapping = false;
    private boolean hasQuickSwitch = false;

    // Hard Keyboad Shift + Space Status
    private boolean hasShiftPress = false;
    private boolean onlyShiftPress = false;  //Jeremy '15,5,30 shift only to switch between chi/eng

    private boolean hasCtrlPress = false; // Jeremy '11,5,13
    private boolean lastKeyCtrl = false;  // Jeremy '15,5,30 for process physical keyboard ctrl-space with missing space down event
    private boolean spaceKeyPress = false; // Jeremy '15,5,30 for process physical keyboard ctrl-space with missing space down event
    private boolean hasWinPress = false; // Jeremy '12,4,29 windows start key on standard windows keyboard
    //private boolean hasCtrlProcessed = false; // Jeremy '11,6.18
    private boolean hasDistinctMultitouch;// Jeremy '11,8,3 
    private boolean hasShiftCombineKeyPressed = false; //Jeremy ,11,8, 3
    private boolean hasMenuPress = false; // Jeremy '11,5,29
    private boolean hasMenuProcessed = false; // Jeremy '11,5,29
    //private boolean hasSearchPress = false; // Jeremy '11,5,29
    //private boolean hasSearchProcessed = false; // Jeremy '11,5,29

    private boolean hasEnterProcessed = false; // Jeremy '11,6.18
    private boolean hasSpaceProcessed = false;
    private boolean hasKeyProcessed = false; // Jeremy '11,8,15 for long pressed key
    private int mLongPressKeyTimeout; //Jeremy '11,8, 15 read long press timeout from config

    private boolean mIsHardwareAcceleratedDrawingEnabled = false;

    private boolean hasSymbolEntered = false; //Jeremy '11,5,24 

    // private boolean hasSpacePress = false;

    // Hard Keyboad Shift + Space Status
    //private boolean hasAltPress = false;

    private String mIMActivatedState = ""; // Jeremy '12,5,3, renamed from keyboardSelectedState
    public String activeIM;  //Jeremy '12,4,30 renamed from keyboardSelection
    private List<String> activatedIMNameList; //Jeremy '12,4,30 renamed from keyboardList
    private List<String> activatedIMShortNameList; //Jeremy '12,4,30 renamed from keyboardShortname
    private List<String> activatedIMList; //jerem '12,4,30 reanmed from keybaordCodeList
    private String currentSoftKeyboard = "";  //Jeremy '12,4,30 reanmed from keybaord_xml;

    // To keep key press time
    //private long keyPressTime = 0;

    // Keep keydown event
    KeyEvent mKeydownEvent = null;

    //private int previousKeyCode = 0;
    //private final float moveLength = 15;
    //private ISearchService SearchSrv = null;
    private SearchServer SearchSrv = null;

    // Auto Commmit Value
    private int auto_commit = 0;


    // Disable physical keyboard candidate words selection
    private boolean disable_physical_selection = false;

    // Replace Keycode.KEYCODE_CTRL_LEFT/RIGHT, ESC on android 3.x
    // for backward compatibility of 2.x
    static final int MY_KEYCODE_ESC = 111;
    static final int MY_KEYCODE_CTRL_LEFT = 113;
    static final int MY_KEYCODE_CTRL_RIGHT = 114;
    static final int MY_KEYCODE_ENTER = 10;
    static final int MY_KEYCODE_SPACE = 32;
    static final int MY_KEYCODE_SWITCH_CHARSET = 95;
    static final int MY_KEYCODE_WINDOWS_START = 117; //Jeremy '12,4,29 windows start key

    private String LDComposingBuffer = ""; //Jeremy '11,7,30 for learning continuous typing phrases

    private LIMEPreferenceManager mLIMEPref;

    private boolean hasChineseSymbolCandidatesShown = false;
    private boolean hasCandidatesShown = false;


    public LIMEService(){
        mIsHardwareAcceleratedDrawingEnabled = true;// this.enableHardwareAcceleration();
    }

    /**
     * Main initialization of the input method component. Be sure to call to
     * super class.
     */
    @Override
    public void onCreate() {

        if (DEBUG) Log.i(TAG, "OnCreate()");

        super.onCreate();

        SearchSrv = new SearchServer(this);
        mEnglishOnly = false;
        mEnglishFlagShift = false;


        // Construct Preference Access Tool
        mLIMEPref = new LIMEPreferenceManager(this);

        mFixedCandidateViewOn = mLIMEPref.getFixedCandidateViewDisplay();

        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mLongPressKeyTimeout = getResources().getInteger(R.integer.config_long_press_key_timeout); // Jeremy '11,8,15 read longpress timeout from config resources.


        // initial keyboard list
        activatedIMNameList = new ArrayList<>();
        activatedIMList = new ArrayList<>();
        activatedIMShortNameList = new ArrayList<>();
        activeIM = mLIMEPref.getActiveIM();
        buildActivatedIMList();


    }


    /**
     * This is the point where you can do all of your UI initialization. It is
     * called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {

        if (DEBUG)
            Log.i(TAG, "onInitializeInterface()");

        initialViewAndSwitcher(false);
        initCandidateView(); //Force the oncreatedcandidate to be called
        mKeyboardSwitcher.resetKeyboards(true);
        super.onInitializeInterface();

    }

    /**
     * Called by the system when the device configuration changes while your activity is running.
     */
    @Override
    public void onConfigurationChanged(Configuration conf) {

        if (DEBUG)
            Log.i(TAG, "LIMEService:OnConfigurationChanged()");


        //Jeremy '12,4,7 add hardkeyboard hidden configuration changed event and clear composing to avoid fc.
        if (conf.orientation != mOrientation || conf.hardKeyboardHidden != mHardkeyboardHidden) {
            //Jeremy '12,4,21 force clear the composing buffer
            clearComposing(true);


            mOrientation = conf.orientation;
            mHardkeyboardHidden = conf.hardKeyboardHidden;
        }
        initialViewAndSwitcher(true);
        mKeyboardSwitcher.resetKeyboards(true);
        super.onConfigurationChanged(conf);

    }

    /**
     * Called by the framework when your view for creating input needs to be
     * generated. This will be called the first time your input method is
     * displayed, and every time it needs to be re-created such as due to a
     * configuration change.
     */
    @Override
    public View onCreateInputView() {
        if (DEBUG)
            Log.i(TAG, "OnCreateInputView()");


        if(mInputView !=null) mInputView =null;

        initialViewAndSwitcher(true);  //Jeremy '12,4,29.  will do buildactivekeyboardlist in init startInput

        if (mFixedCandidateViewOn) {
            if (DEBUG)
                Log.i(TAG, "Fixed candiateView in on, return nInputViewContainer ");
            return mCandidateInInputView;
        } else
            return mInputView;

    }

    /**
     * Create and return the view hierarchy used to show candidates.
     * This will be called once, when the candidates are first displayed.
     * You can return null to have no candidates view; the default implementation returns null.
     */

    @Override
    public View onCreateCandidatesView() {
        if (DEBUG) Log.i(TAG, "onCreateCandidatesView()");

        @SuppressLint("InflateParams")
        CandidateViewContainer candidateViewContainer = (CandidateViewContainer) getLayoutInflater().inflate(R.layout.candidates, null);
        candidateViewContainer.initViews();
        mCandidateViewContainer = candidateViewContainer;

        mCandidateViewStandAlone = (CandidateView) mCandidateViewContainer.findViewById(R.id.candidates);
        mCandidateViewStandAlone.setService(this);


        if (!mFixedCandidateViewOn)
            mCandidateView = mCandidateViewStandAlone;

        return mCandidateViewContainer;
    }

    /**
     * Override this to control when the input method should run in fullscreen mode.
     * Jeremy '11,5,31
     * Override fullscreen editing mode settings for larger screen  (>1.4in)
     */

    @Override
    public boolean onEvaluateFullscreenMode() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float displayHeight = dm.heightPixels;
        // If the display is more than X inches high, don't go to fullscreen mode
        float max = getResources().getDimension(R.dimen.max_height_for_fullscreen);
        if (DEBUG)
            Log.i(TAG, "onEvaluateFullScreenMode() DisplayHeight:" + displayHeight + " limit:" + max
                    + "super.onEvaluateFullscreenMode():" + super.onEvaluateFullscreenMode());
        //Jeremy '12,4,30 Turn off evaluation only for tablet and xhdpi phones (required horizontal >900pts)
        return !(displayHeight > max && this.getMaxWidth() > 900) && super.onEvaluateFullscreenMode();
    }

    /**
     * This is called when the user is done editing a field. We can use this to
     * reset our state.
     */

    @Override
    public void onFinishInput() {

        if (DEBUG) {
            Log.i(TAG, "onFinishInput()");
        }
        super.onFinishInput();

        if (mInputView != null) {
            mInputView.closing();
        }
        try {
            if (LDComposingBuffer.length() > 0) { // Force interrupt the LD process
                LDComposingBuffer = "";
                SearchSrv.addLDPhrase(null, true);
            }
            // Jeremy '11,8,1 do postfinishinput in searchSrv (learn userdic and LDPhrase). 
            SearchSrv.postFinishInput();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Clear current composing text and candidates.
        //Jeremy '12,5,21 
        finishComposing();

        // -> 26.May.2011 by Art : Update keyboard list when user click the keyboard.
        try {
            mKeyboardSwitcher.setKeyboardList(SearchSrv.getKeyboardList());
            mKeyboardSwitcher.setImList(SearchSrv.getImList());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * add by Jeremy '12,4,21
     * Send ic.finishComposingText upon composing is about to end
     */
    private void finishComposing() {
        if (DEBUG)
            Log.i(TAG, "finishComposing()");
        //Jeremy '11,8,14
        if (mComposing != null && mComposing.length() > 0)
            mComposing.setLength(0);

        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.finishComposingText();

        selectedCandidate = null;
        //selectedIndex = 0;

        if (mCandidateList != null)
            mCandidateList.clear();
        if (mCandidateView != null)
            mCandidateView.clear();
    }

    /**
     * add by Jeremy '12,4,21
     * clearComposing buffer upon composing is about to end
     * add forceClearComposing parameter to control forced clear the system composing buffer
     */
    private void clearComposing(boolean forceClearComposing) {
        if (DEBUG)
            Log.i(TAG, "clearComposing()");

        //Log.i(TAG, "===========> clear composing");

        try {
            //Jeremy '11,8,14
            if (mComposing != null && mComposing.length() > 0)
                mComposing.setLength(0);
            if (mCandidateList != null)
                mCandidateList.clear();

            if (forceClearComposing) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) ic.commitText("", 0);
            }

            selectedCandidate = null;
            //selectedIndex = 0;

            clearSuggestions();
        }catch(Exception e){
            e.printStackTrace();
            // ignore candidate clear error
        }
    }

    /**
     * Clear suggestions or candidates in candidate view.
     */
    private synchronized void clearSuggestions() {
        if (mCandidateView != null) {
            if (DEBUG)
                Log.i(TAG, "clearSuggestions(): "
                        + ", hasCandidatesShown:" + hasCandidatesShown);

            if (!mEnglishOnly && mLIMEPref.getAutoChineseSymbol() //Jeremy '12,4,29 use mEnglishOnly instead of onIM 
                    && (hasCandidatesShown || mFixedCandidateViewOn)) {   // Change isCandiateShown() to hasCandiatesShown
                mCandidateView.clear();
                if (hasCandidatesShown)
                    updateChineseSymbol(); // Jeremy '12.5,23 do not show chinesesymbol when init for fixed candidate view.
            } else {
                mCandidateView.clear();
                hideCandidateView();
            }

        }
    }
    /**
     * Jeremy '15,7,8 to avoid candidateView shift up and down when it's not fixed.
     */
    @Override public void onComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if(mCandidateView == null || mCandidateView == mCandidateViewInInputView ) return;

        outInsets.contentTopInsets = mCandidateViewContainer.getHeight() - mCandidateViewStandAlone.getHeight();
        outInsets.visibleTopInsets = mCandidateViewContainer.getHeight();

        if(mCandidateViewStandAlone.isShown()) {
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT;
        }else{
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE;
        }
    }
    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application. At this point we have been bound to
     * the client, and are now receiving all of the detailed information about
     * the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        if (DEBUG)
            Log.i(TAG, "onStartInput()");
        super.onStartInputView(attribute, restarting);
        initOnStartInput(attribute);
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (DEBUG)
            Log.i(TAG, "onStartInputView()");
        super.onStartInputView(attribute, restarting);
        initOnStartInput(attribute);
    }

    /**
     * Initialization for IM and softkeybaords, and also choose wring lanaguage mode
     * according the input attrubute in editorInfo
     */
    private void initOnStartInput(EditorInfo attribute) {


        if (DEBUG)
            Log.i(TAG, "initOnStartInput(): attribute.inputType & EditorInfo.TYPE_MASK_CLASS: "
                    + (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) + "; attribute.inputType & EditorInfo.TYPE_MASK_VARIATION: "
                    + (attribute.inputType & EditorInfo.TYPE_MASK_VARIATION));

        if (mInputView == null) {
            return;
        }

        //Jeremy '12,5,29 override the fixCanddiateMode setting in Landscape mode (in landscape mode the candidate bar is always not fixed).
        boolean fixedCandidateMode = mLIMEPref.getFixedCandidateViewDisplay();

        //Jeremy '12,5,6 recreate inputView if fixedCandidateView setting is altered
        //Jeremy '15,7,15 recreate inputView if keyboard theme changed
        if (mFixedCandidateViewOn != fixedCandidateMode
                || mKeyboardThemeIndex != mLIMEPref.getKeyboardTheme()) {
            requestHideSelf(0);
            mInputView.closing();
            mFixedCandidateViewOn = fixedCandidateMode;

            initialViewAndSwitcher(true);

            if (mFixedCandidateViewOn) {
                if (DEBUG)
                    Log.i(TAG, "Fixed candidateView in on, return nInputViewContainer ");
                setInputView(mCandidateInInputView);
            } else {
                setInputView(mInputView);
                if (DEBUG)
                    Log.i(TAG, "Fixed candidateView in off, return mInputView ");
            }

        }

        hasPhysicalKeyPressed = false;  //Jeremy '11,9,6 reset phsycalkeyflag
        hasCandidatesShown = false;

        // Reset the IM softkeyboard settings. Jeremy '11,6,19
        try {
            mKeyboardSwitcher.setImList(SearchSrv.getImList());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        mKeyboardSwitcher.resetKeyboards(
                mShowArrowKeys != mLIMEPref.getShowArrowKeys() //Jeremy '12,5,22 recreate keyboard if the setting altered.
                        || mSplitKeyboard != mLIMEPref.getSplitKeyboard()); //Jeremy '12,5,26 recreate keyboard if the setting altered.



        loadSettings();
        mImeOptions = attribute.imeOptions;

        buildActivatedIMList();  //Jeremy '12,4,29 only this is required here, instead of fully initialKeybaord
        mPredictionOn = true;
        mCompletionOn = false;
        mCompletions = null;
        mCapsLock = false;
        mHasShift = false;


        tempEnglishWord = new StringBuffer();
        tempEnglishList = new LinkedList<>();


        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:  //0x02
            case EditorInfo.TYPE_CLASS_DATETIME: //0x04
                mEnglishOnly = true;
                mKeyboardSwitcher.setKeyboardMode(activeIM, LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, false, true, false);
                break;
            case EditorInfo.TYPE_CLASS_PHONE: //0x03
                mEnglishOnly = true;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_PHONE, mImeOptions, false, false, false);
                break;
            case EditorInfo.TYPE_CLASS_TEXT: //0x01

                // Make sure that passwords are not displayed in candidate view
                int variation = attribute.inputType
                        & EditorInfo.TYPE_MASK_VARIATION;
            /*
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME) {
                    //mAutoSpace = false;
                } else {
                    //mAutoSpace = true;
                }
                */
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    mPredictionOn = true;
                }
                // If NO_SUGGESTIONS is set, don't do prediction.
                if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
                    mPredictionOn = true;
                }
                if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    mPredictionOn = true;
                    mCompletionOn = isFullscreenMode();
                }

                // Switch keyboard here.
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    mPredictionOn = false;
                    //isModePassword = true;
                    mEnglishOnly = true;
                    //onIM = false;//Jeremy '12,4,29 use mEnglishOnly instead of onIM
                    mKeyboardSwitcher.setKeyboardMode(activeIM, LIMEKeyboardSwitcher.MODE_EMAIL,
                            mImeOptions, false, false, false);
                    break;
                } else if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
                    mEnglishOnly = true;
                    mPredictionOn = true;
                    mKeyboardSwitcher.setKeyboardMode(activeIM,
                            LIMEKeyboardSwitcher.MODE_EMAIL, mImeOptions, false, false, false);
                    break;
                } else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                    mPredictionOn = true;
                    mEnglishOnly = true;
                    //onIM = false; //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                    //isModeURL = true;
                    mKeyboardSwitcher.setKeyboardMode(activeIM,
                            LIMEKeyboardSwitcher.MODE_URL, mImeOptions, false, false, false);
                    break;
                } else if (variation == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                    mEnglishOnly = false;
                    mKeyboardSwitcher.setKeyboardMode(activeIM, LIMEKeyboardSwitcher.MODE_IM, mImeOptions, true, false, false);
                    break;
                }
            default:
                if (mPersistentLanguageMode)
                    mEnglishOnly = mLIMEPref.getLanguageMode(); //Jeremy '12,4,30 restore lanaguage mode from preference.

                if (mPersistentLanguageMode && mEnglishOnly) {
                    mPredictionOn = true;
                    mEnglishOnly = true;
                    //onIM = false; //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                    mKeyboardSwitcher.setKeyboardMode(activeIM, LIMEKeyboardSwitcher.MODE_TEXT,
                            mImeOptions, false, false, false);

                } else {
                    mEnglishOnly = false;
                    initialIMKeyboard();  //'12,4,29 intial chinese IM keybaord
                }
        }


        if (mEnglishOnly && !mPredictionOn) //Jeremy '12,5,20 Only hide candidateview when prediction mode is not on. 
            //Jeremy '12,5,6 clear internal composing buffer in forceHideCandiateView 
            forceHideCandidateView();  //Jeremy '12,5,6 zero the canidateView height to force hide it for eng/numeric keyboard
        else {
            clearComposing(false);//Jeremy '12,5,24 clear the suggesions and also restore the height of fixed candaiteview if it's hide before
            //clearSuggestions();  // do this in clearcomposing already.
        }

        mPredicting = false;
        updateShiftKeyState(getCurrentInputEditorInfo());


        //initCandidateView(); //Force the oncreatedcandidate to be called   
        //clearComposing(false);

    }

    private void loadSettings() {

        hasVibration = mLIMEPref.getVibrateOnKeyPressed();
        hasSound = mLIMEPref.getSoundOnKeyPressed();
        mPersistentLanguageMode = mLIMEPref.getPersistentLanguageMode();
        activeIM = mLIMEPref.getActiveIM();
        hasQuickSwitch = mLIMEPref.getSwitchEnglishModeHotKey();
        mAutoCap = true;

        mPersistentLanguageMode = mLIMEPref.getPersistentLanguageMode();
        mShowArrowKeys = mLIMEPref.getShowArrowKeys();
        mSplitKeyboard = mLIMEPref.getSplitKeyboard();

        disable_physical_selection = mLIMEPref.getDisablePhysicalSelkey();

        auto_commit = mLIMEPref.getAutoCommitValue();
        currentSoftKeyboard = mKeyboardSwitcher.getImKeyboard(activeIM);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        if (DEBUG)
            Log.i(TAG, "onUpdateSelection():oldSelStart" + oldSelStart
                    + " oldSelEnd:" + oldSelEnd
                    + " newSelStart:" + newSelStart + " newSelEnd:" + newSelEnd
                    + " candidatesStart:" + candidatesStart + " candidatesEnd:" + candidatesEnd);

        InputConnection ic = getCurrentInputConnection();

        if (mComposing.length() > 0
                && !(candidatesEnd == candidatesStart) //Jeremy '12,7,2 bug fixed on composition being clear after second word in chrome 
                && candidatesStart >= 0 && candidatesEnd > 0 // in composing  
                ) {
            if (newSelStart < candidatesStart || newSelStart > candidatesEnd) { // cursor is moved before or after composing area

                if (mCandidateList != null) mCandidateList.clear();
                hideCandidateView();

                if (mComposing != null && mComposing.length() > 0) {

                    mComposing.setLength(0);


                    if (ic != null)
                        ic.finishComposingText();
                }
            }
            // Jeremy '13,8,25 setSelection cause inputbox in Chorme failed to input
            // Jeremy '12,5,23 Select the composing text and forbidded moving cursor within the composing text.
            //if (ic != null)	ic.setSelection(candidatesStart, candidatesEnd);


        }


    }

    /**
     * This tells us about completions that the editor has determined based on
     * the current text in it. We want to use this in fullscreen mode to show
     * the completions ourself, since the editor can not be seen in that
     * situation.
     */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (DEBUG)
            Log.i(TAG, "onDisplayCompletions()");
        if (mCompletionOn) {
            mCompletions = completions;
            if (!mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                if (mComposing.length() == 0) updateRelatedPhrase(false);
            }
            if (mEnglishOnly && !mPredictionOn) {
                setSuggestions(buildCompletionList(), false, "");
            }

        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection. It is only needed when using the PROCESS_HARD_KEYS
     * option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        hasPhysicalKeyPressed = true;

        // If user use the physical keyboard then not fixed the candidate view also use the tranparent background
        mFixedCandidateViewOn = false;
        mCandidateView.setTransparentCandidateView(false);

        if (DEBUG)
            Log.i(TAG, "translateKeyDown() LIMEMetaKeyKeyListener.getMetaState(mMetaState) = "
                    + Integer.toHexString(LIMEMetaKeyKeyListener.getMetaState(mMetaState))
                    + ", event.getMetaState()" + Integer.toHexString(event.getMetaState()));

        int metaState;
        if (mLIMEPref.getPhysicalKeyboardType().equals("standard"))
            metaState = event.getMetaState();
        else
            metaState = LIMEMetaKeyKeyListener.getMetaState(mMetaState);


        int c = event.getUnicodeChar(metaState);


        InputConnection ic = getCurrentInputConnection();

        /// Jeremy '12,4,1 XPERIA Pro force translating special keys 
        if (mLIMEPref.getPhysicalKeyboardType().equals("xperiapro")) {
            boolean isShift = LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                    LIMEMetaKeyKeyListener.META_SHIFT_ON) > 0;
            switch (keyCode) {
                case KeyEvent.KEYCODE_AT:
                    if (isShift) c = '/';
                    else c = '!';
                    break;
                case KeyEvent.KEYCODE_APOSTROPHE:
                    if (isShift) c = '"';
                    else c = '\'';
                    break;
                case KeyEvent.KEYCODE_GRAVE:
                    if (isShift) c = '~';
                    else c = '`';
                    break;
                case KeyEvent.KEYCODE_COMMA:
                    if (isShift) c = '?';
                    else c = '.';
                    break;
                case KeyEvent.KEYCODE_PERIOD:
                    if (isShift) c = '>';
                    else c = '@';
                    break;

            }
        }

        if (c == 0 || ic == null) {
            return false;
        }

        // Compact code by Jeremy '10, 3, 27
        if (keyCode == 59) { // Translate shift as -1
            c = -1;
        }
        if (c != -1 && (c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        onKey(c, null);
        return true;
    }


    /**
     * Physical KeyBoard Event Handler Use this to monitor key events being
     * delivered to the application. We get first crack at them, and can either
     * resume them or let them continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // Clean code by jeremy '11,8,22
        if (DEBUG)
            Log.i(TAG, "OnKeyDown():keyCode:" + keyCode
                    + ", mComposing = " + mComposing
                    + ", hasMenuPress = " + hasMenuPress
                    + ", hasCtrlPress = " + hasCtrlPress
                    + ", isCtrlPressed = " + event.isCtrlPressed()
                    + ", hasShiftPress = " + hasShiftPress
                    + ", onlyShiftPress = " + onlyShiftPress
                    + ", hasWinPress = " + hasWinPress
                    + ", event.getEventTime() -  event.getDownTime()" + (event.getEventTime() - event.getDownTime())
                    + ", event.getRepeatCount()" + event.getRepeatCount()
                    + ", event.getMetaState()" + Integer.toHexString(event.getMetaState()));


        mKeydownEvent = new KeyEvent(event);
        // Record key pressed time and set key processed flags(key down, for physical keys)
        //Jeremy '11,8,22 using getRepeatCount from event to set processed flags
        if (event.getRepeatCount() == 0) {//!keydown) {
            //keyPressTime = System.currentTimeMillis();
            //keydown = true;
            hasKeyProcessed = false;
            hasMenuProcessed = false; // only do this on first keydown event
            hasEnterProcessed = false;
            hasSpaceProcessed = false;
            hasSymbolEntered = false;
            //Jeremy '15,5,30 for physical keyboard
            onlyShiftPress = false;
            lastKeyCtrl = false;
            spaceKeyPress = false;
        }


        switch (keyCode) {
            // Jeremy '11,5,29 Bypass search and menu combination keys.
            case KeyEvent.KEYCODE_MENU:

                hasMenuPress = true;
                break;
            // Add by Jeremy '10, 3, 29. DPAD selection on candidate view
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Log.i("ART","select:"+1);
                if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    mCandidateView.selectNext();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                // Log.i("ART","select:"+2);
                if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    mCandidateView.selectPrev();
                    return true;
                }
                break;
            //Jeremy '11,8,28 for expanded canddiateviewi
            case KeyEvent.KEYCODE_DPAD_UP:
                // Log.i("ART","select:"+2);
                if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    mCandidateView.selectPrevRow();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Log.i("ART","select:"+2);
                if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    mCandidateView.selectNextRow();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // Log.i("ART","select:"+3);
                if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    pickHighlightedCandidate();
                    return true;
                }
                break;
            // Add by Jeremy '10,3,26, process metakey with
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                hasShiftPress = true;
                onlyShiftPress = true;
                mMetaState = LIMEMetaKeyKeyListener.handleKeyDown(mMetaState, keyCode, event);
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                mMetaState = LIMEMetaKeyKeyListener.handleKeyDown(mMetaState, keyCode, event);
                break;
            case MY_KEYCODE_CTRL_LEFT:
            case MY_KEYCODE_CTRL_RIGHT:
                hasCtrlPress = true;
                lastKeyCtrl = true;
                break;
            case MY_KEYCODE_WINDOWS_START:
                hasWinPress = true;
                break;
            case MY_KEYCODE_ESC:
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.

                if (event.getRepeatCount() == 0) {
                    if (mInputView != null && mInputView.handleBack()) {
                        Log.i(TAG, "KEYCODE_BACK mInputView handled the backed key");
                        return true;
                    }
                    //Jeremy '12,4,8 rewrite the logic here
                    //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    //TODO: need to recheck here.
                    else if (!mEnglishOnly
                            && hasCandidatesShown
                            && (mComposing.length() > 0
                            || (selectedCandidate != null && !selectedCandidate.isComposingCodeRecord()
                            && !hasChineseSymbolCandidatesShown))) {
                        if (DEBUG)
                            Log.i(TAG, "KEYCODE_BACK clearcomposing only.");
                        clearComposing(false);
                        return true;
                    } else if (!mEnglishOnly && hasCandidatesShown) { //Jeremy '12,6,13
                        hideCandidateView();
                        return true;
                    }

                }
                if (DEBUG)
                    Log.i(TAG, "KEYCODE_BACK return to super.");

                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                hasPhysicalKeyPressed = true;
                onKey(LIMEBaseKeyboard.KEYCODE_DELETE, null);
                return true;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these, if return
                // false from takeSelectedSuggestion().
                // Process enter for candidate view selection in OnKeyUp() to block
                // the real enter afterward.
                // return false;
                // Log.i("ART", "physical keyboard:"+ keyCode);
                mMetaState = LIMEMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                if (!mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                    if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                        // To block a real enter after suggestion selection. We have to
                        // return true in OnKeyUp();					
                        if (pickHighlightedCandidate()) {
                            hasEnterProcessed = true;
                            return true;
                        } else {
                            hideCandidateView();
                            break;
                        }
                    }
                } else if (//mLIMEPref.getEnglishPrediction() && 
                        mPredictionOn && mLIMEPref.getEnglishPredictionOnPhysicalKeyboard()) {
                    resetTempEnglishWord();
                    this.updateEnglishPrediction();
                    break;
                } else  //Jeremy '12',7,1 bug fixed on english mode enter not functioning in chrome
                    break;

/*		case MY_KEYCODE_ESC:
        //Jeremy '11,9,7 treat esc as back key
			//Jeremy '11,8,14
			clearComposing();
			InputConnection ic=getCurrentInputConnection();
			if(ic!=null) ic.commitText("", 0);
			return true;*/

            case KeyEvent.KEYCODE_SPACE:
                spaceKeyPress = true;
                hasQuickSwitch = mLIMEPref.getSwitchEnglishModeHotKey();
                // If user enable Quick Switch Mode control then check if has
                // 	Shift+Space combination
                // '11,5,13 Jeremy added Ctrl-space switch chi/eng
                // '11,6,18 Jeremy moved from on_KEY_UP
                // '12,4,29 Jeremy add hasWinPress + space to switch chi/eng (earth key on zippy keyboard)
                // '12,5,8  Jeremy add send the space key to onKey with translatekeydown for candidate processing if it's not switching chi/eng 
                if ((hasQuickSwitch && hasShiftPress) || hasCtrlPress || hasMenuPress || hasWinPress || event.isCtrlPressed() ) {
                    if (!hasWinPress)
                        this.switchChiEng();  //Jeremy '12,5,20 move hasWinPress to winstartkey in onkeyUp()
                    if (hasMenuPress) hasMenuProcessed = true;
                    hasSpaceProcessed = true;
                    return true;
                } else
                    return translateKeyDown(keyCode, event);

            case MY_KEYCODE_SWITCH_CHARSET: // experia pro earth key
            case 1000: // milestone chi/eng key
                switchChiEng();
                break;
            case KeyEvent.KEYCODE_SYM:
            case KeyEvent.KEYCODE_AT:
                //Jeremy '11,8,22 use begintime and eventtime in event to see if long-pressed or not.
                if (!hasKeyProcessed
                        && event.getRepeatCount() > 0
                        && event.getEventTime() - event.getDownTime() > mLongPressKeyTimeout) {
                    //&& System.currentTimeMillis() - keyPressTime > mLongPressKeyTimeout){
                    switchChiEng();
                    hasKeyProcessed = true;
                }
                return true;
            case KeyEvent.KEYCODE_TAB: // Jeremy '12.6,22 Force bypassing tab processing to super if not on milestone 2 with alt on (alt+tab = ~ on milestone2)
                if (!(LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                        LIMEMetaKeyKeyListener.META_ALT_ON) > 0
                        && mLIMEPref.getPhysicalKeyboardType().equals("milestone2")))
                    break;
            default:
                if (!(hasCtrlPress ||  event.isCtrlPressed()  || hasMenuPress)) {
                    if (translateKeyDown(keyCode, event)) {
                        if (DEBUG) Log.i(TAG, "Onkeydown():tranlatekeydown:true");
                        return true;
                    }
                }

        }


        if ((hasCtrlPress || hasMenuPress) && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
            int primaryKey = event.getUnicodeChar(LIMEMetaKeyKeyListener.getMetaState(mMetaState));
            char t = (char) primaryKey;


            if (hasCtrlPress &&  //Only working with ctrl Jeremy '11,8,22
                    mCandidateList != null && mCandidateList.size() > 0
                    && mCandidateView != null && hasCandidatesShown) {
                switch (keyCode) {
                    case 8:
                        this.pickCandidateManually(0);
                        return true;
                    case 9:
                        this.pickCandidateManually(1);
                        return true;
                    case 10:
                        this.pickCandidateManually(2);
                        return true;
                    case 11:
                        this.pickCandidateManually(3);
                        return true;
                    case 12:
                        this.pickCandidateManually(4);
                        return true;
                    case 13:
                        this.pickCandidateManually(5);
                        return true;
                    case 14:
                        this.pickCandidateManually(6);
                        return true;
                    case 15:
                        this.pickCandidateManually(7);
                        return true;
                    case 16:
                        this.pickCandidateManually(8);
                        return true;
                    case 7:
                        this.pickCandidateManually(9);
                        return true;
                }
            }
            if ((mComposing == null || mComposing.length() == 0)) {
                // Jeremy '11,8,21.  Ctrl-/ to fetch full-shaped chinese symbols1 in candidateview.
                if (t == '/') {
                    if (hasMenuPress) hasMenuProcessed = true;
                    updateChineseSymbol();
                    return true;
                }
                // 27.May.2011 Art : when user click Ctrl + Symbol or number then send Chinese Symobl Characters
                String s = ChineseSymbol.getSymbol(t);
                if (s != null) {
                    clearSuggestions();
                    getCurrentInputConnection().commitText(s, 0);
                    hasSymbolEntered = true;
                    if (hasMenuPress) hasMenuProcessed = true;
                    return true;

                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void resetTempEnglishWord() {
        tempEnglishWord.delete(0, tempEnglishWord.length());
        tempEnglishList.clear();
    }

    private void setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            int clearStatesFlags = 0;
            if (LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                    LIMEMetaKeyKeyListener.META_ALT_ON) == 0)
                clearStatesFlags += KeyEvent.META_ALT_ON;
            if (LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                    LIMEMetaKeyKeyListener.META_SHIFT_ON) == 0)
                clearStatesFlags += KeyEvent.META_SHIFT_ON;
            if (LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                    LIMEMetaKeyKeyListener.META_SYM_ON) == 0)
                clearStatesFlags += KeyEvent.META_SYM_ON;
            ic.clearMetaKeyStates(clearStatesFlags);
        }
    }

    /**
     * Use this to monitor key events being delivered to the application. We get
     * first crack at them, and can either resume them or let them continue to
     * the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (DEBUG)
            Log.i(TAG, "OnKeyUp():keyCode:" + keyCode
                            + ", mComposing = " + mComposing
                            + ", hasCtrlPress:" + hasCtrlPress
                            + ", hasWinPress:" + hasWinPress
                            + ", hasShiftPress = " + hasShiftPress
                            + ", event.getEventTime() -  event.getDownTime()" + (event.getEventTime() - event.getDownTime())

            );


        switch (keyCode) {
            //Jeremy '11,5,29 Bypass search and menu keys.
//		case KeyEvent.KEYCODE_SEARCH:
//			hasSearchPress = false;
//			if(hasSearchProcessed) return true;
//			break;
            case KeyEvent.KEYCODE_CAPS_LOCK:
                // Modified by Art 20130607
                // to switch the cap lock mode
                toggleCapsLock();
            case KeyEvent.KEYCODE_MENU:
                hasMenuPress = false;
                if (hasMenuProcessed) return true;
                break;
            // */------------------------------------------------------------------------
            // Modified by Jeremy '10, 3,12
            // keep track of alt state with mHasAlt.
            // Modified '10, 3, 24 for bug fix and alt-lock implementation
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                hasShiftPress = false;
                mMetaState = LIMEMetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event);
                // '11,8,28 Jeremy popup keyboard picker instead of nextIM when onIM
                // '11,5,14 Jeremy ctrl-shift switch to next available keyboard; 
                // '11,5,24 blocking switching if full-shape symbol 
                if (!hasSymbolEntered && !mEnglishOnly && (hasMenuPress || hasCtrlPress)) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM  
                    //nextActiveKeyboard(true);
                    showIMPicker(); //Jeremy '11,8,28
                    if (hasMenuPress) {
                        hasMenuProcessed = true;
                        hasMenuPress = false;
                    }
                    mMetaState = LIMEMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                    setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                    return true;
                } else if (mLIMEPref.getShiftSwitchEnglishMode() && onlyShiftPress) {
                    this.switchChiEng();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                mMetaState = LIMEMetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event);
                break;
            case MY_KEYCODE_CTRL_LEFT:
            case MY_KEYCODE_CTRL_RIGHT:
                hasCtrlPress = false;
                break;
            case MY_KEYCODE_WINDOWS_START:
                if (hasSpaceProcessed) //Jeremy '12,5,20 long press to show IM picker, switch chi/eng otherwise for the win+space or earth key on zippy
                    if (event.getEventTime() - event.getDownTime() > mLongPressKeyTimeout)
                        showIMPicker();
                    else
                        switchChiEng();
                hasWinPress = false;
                break;
            case KeyEvent.KEYCODE_ENTER:
                // Add by Jeremy '10, 3 ,29. Pick selected selection if candidates
                // shown.
                // Does not block real enter after select the suggestion. !! need
                // fix here!!
                // Let the underlying text editor always handle these, if return
                // false from takeSelectedSuggestion().

                if (hasEnterProcessed) {
                    return true;
                }
                // Jeremy '10, 4, 12 bug fix on repeated enter.
                break;

            case KeyEvent.KEYCODE_SYM:
            case KeyEvent.KEYCODE_AT:
                if (hasKeyProcessed) {  //(keyPressTime != 0
                    //&& System.currentTimeMillis() - keyPressTime > 700) {
                    //switchChiEng(); // Jeremy '11,8,15 moved to onKeyDown()
                    return true;
                } else if (LIMEMetaKeyKeyListener.getMetaState(mMetaState,
                        LIMEMetaKeyKeyListener.META_SHIFT_ON) > 0 && !mEnglishOnly //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                        && !mLIMEPref.getPhysicalKeyboardType().equals("xperiapro")) {  // '12,4,1 Jeremy XPERIA Pro does not use this key as @
                    // alt-@ is conflict with symbol input thus altered to shift-@ Jeremy '11,8,15
                    // alt-@ switch to next active keyboard.
                    //nextActiveKeyboard(true);
                    showIMPicker(); //Jeremy '11,8,28
                    mMetaState = LIMEMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                    setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                    return true;
                    // Long press physical @ key to swtich chn/eng 
                } else if ((!mEnglishOnly || mPredictionOn)
                        && translateKeyDown(keyCode, event)) {
                    return true;
                } else {
                    translateKeyDown(keyCode, event);
                    super.onKeyDown(keyCode, mKeydownEvent);
                }
                break;

            case KeyEvent.KEYCODE_SPACE:
                //Jeremy move the chi/eng switching to on_KEY_UP '11,6,18

                if (!spaceKeyPress && lastKeyCtrl) { //missing space down event when ctrl-space is pressed
                    this.switchChiEng();
                    return true;
                }

                if (hasSpaceProcessed)
                    return true;
            default:

        }
        // Update metakeystate of IC maintained by MetaKeyKeyListerner
        //setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState(); moved to OnKey by jeremy '12,6,13

        if (DEBUG)
            Log.i(TAG, "OnKeyUp():keyCode:" + keyCode
                            + ";hasCtrlPress:" + hasCtrlPress
                            + ";hasWinPress:" + hasWinPress
                            + ", event.getEventTime() -  event.getDownTime()" + (event.getEventTime() - event.getDownTime())
                            + " call super.onKeyUp()"
            );


        return super.onKeyUp(keyCode, event);
    }


    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection ic) {
        if (DEBUG)
            Log.i(TAG, "commitTyped()");
        if(selectedCandidate==null)     return;
        try {
            if (   (mComposing.length() > 0   //denotes composing just finished
                    ||  !selectedCandidate.isComposingCodeRecord() ) // commit selected candidate if it is not the composing text. '15,6,4 Jeremy  (like related phrase or English suggestions)
                    &&!LIMEUtilities.isUnicodeSurrogate(selectedCandidate.getWord())  ) {   //check if it's surrogate characters (emoji) '15,7,19 Jeremy

                if (!mEnglishOnly
                        || !selectedCandidate.isComposingCodeRecord()
                        || !selectedCandidate.isEnglishSuggestionRecord() ) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                    if (selectedCandidate != null && selectedCandidate.getWord() != null
                            && !selectedCandidate.getWord().equals("")) {

                        int firstMatchedLength = 1;

                        if (selectedCandidate.getCode() == null
                                || selectedCandidate.getCode().equals("")) {
                            firstMatchedLength = 1;
                        }

                        String wordToCommit = selectedCandidate.getWord();

                        if (selectedCandidate != null
                                && selectedCandidate.getCode() != null
                                && selectedCandidate.getWord() != null) {
                            if (selectedCandidate
                                    .getCode()
                                    .toLowerCase(Locale.US)
                                    .equals(selectedCandidate.getWord()
                                            .toLowerCase(Locale.US))) {
                                firstMatchedLength = 1;


                            }
                        }

                        if (DEBUG)
                            Log.i(TAG, "commitTyped() committed Length="
                                    + firstMatchedLength);

                        // Do hanConvert before commit
                        // '10, 4, 17 Jeremy
                        if(mLIMEPref.getHanCovertOption() == 0){
                            if (ic != null) ic.commitText(wordToCommit, firstMatchedLength);
                            if (mLIMEPref.shouldShowTypedWord()) {
                                candidateHintAddWord(wordToCommit);
                            }
                        }else{
                            if(mLIMEPref.getHanConvertNotify()){

                                Calendar now = Calendar.getInstance();

                                long nowvalue = now.getTimeInMillis();
                                long storevalue = mLIMEPref.getParameterLong("han_notify_interval", 0);

                                // 1 minute idle time
                                if(nowvalue - storevalue > 60000){
                                    if(mLIMEPref.getHanCovertOption() == 1){
                                        Toast.makeText(this, R.string.han_convert_ts, Toast.LENGTH_SHORT).show();
                                    }else if(mLIMEPref.getHanCovertOption() == 2){
                                        Toast.makeText(this, R.string.han_convert_st, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                mLIMEPref.setParameter("han_notify_interval", now.getTimeInMillis());
                            }
                            if (ic != null) ic.commitText(SearchSrv.hanConvert(wordToCommit), firstMatchedLength);
                        }

                        // Art '30,Sep,2011 when show related then clear composing
                        if (currentSoftKeyboard.contains("wb") || selectedCandidate.isEmojiRecord() || selectedCandidate.isChinesePunctuationSymbolRecord()) {
                            clearComposing(true);
                        }


                        // Jeremy '11,7,28 for continuous typing (LD) 
                        // Jeremy '12,6,2 get real committed code length from searchserver
                        boolean composingNotFinish = false;
                        //Jeremy '15,6,2 retrieve real code length with selectedCandidate using exact code match stack in search server
                        int committedCodeLength = SearchSrv.getRealCodeLength(selectedCandidate, mComposing.toString());

                        if (DEBUG)
                            Log.i(TAG, "commitTyped(): committedCodeLength = " + committedCodeLength);

                        if (mComposing.length() > selectedCandidate.getCode().length()) {
                            composingNotFinish = true;
                        }

                        boolean shouldUpdateCandidates = false;
                        if (composingNotFinish) {
                            if (LDComposingBuffer.length() == 0) {
                                //starting LD process
                                LDComposingBuffer = mComposing.toString();
                                if (DEBUG)
                                    Log.i(TAG, "commitTyped():starting LD process, LDBuffer=" + LDComposingBuffer +
                                            ". just committed code= '" + selectedCandidate.getCode() + "'");
                                SearchSrv.addLDPhrase(selectedCandidate, false);
                            } else {
                                //Continuous LD process
                                if (DEBUG)
                                    Log.i(TAG, "commitTyped():Continuous LD process, LDBuffer='" + LDComposingBuffer +
                                            "'. just committed code=" + selectedCandidate.getCode());
                                SearchSrv.addLDPhrase(selectedCandidate, false);
                            }
                            mComposing = mComposing.delete(0, committedCodeLength);
                            if (DEBUG)
                                Log.i(TAG, "commitTyped(): trimmed mComposing = '" + mComposing + "', " +
                                        "+ mComposing.length = " + mComposing.length());

                            if (!mComposing.toString().equals(" ")) {
                                if (mComposing.toString().startsWith(" "))
                                    mComposing = mComposing.deleteCharAt(0);
                                if (DEBUG)
                                    Log.i(TAG, "commitTyped(): new mComposing:'" + mComposing + "'");
                                if (mComposing.length() > 0) { //Jeremy '12,7,11 only fetch remaining composing when length >0
                                    if (ic != null && mPredictionOn) ic.setComposingText(mComposing, 1);
                                    shouldUpdateCandidates = true;
                                }
                            }
                        } else {

                            if (LDComposingBuffer.length() > 0) {// && LDComposingBuffer.contains(mComposing.toString())){
                                //Ending continuous LD process (last of LD process)
                                if (DEBUG)
                                    Log.i(TAG, "commitTyped():Ending LD process, LDBuffer=" + LDComposingBuffer +
                                            ". just committed code=" + selectedCandidate.getCode());
                                LDComposingBuffer = "";
                                SearchSrv.addLDPhrase(selectedCandidate, true);
                            } else if (LDComposingBuffer.length() > 0) {
                                //LD process interrupted.
                                if (DEBUG)
                                    Log.i(TAG, "commitTyped():LD process interrupted, LDBuffer=" + LDComposingBuffer +
                                            ". just committed code=" + selectedCandidate.getCode());
                                LDComposingBuffer = "";
                                SearchSrv.addLDPhrase(null, true);
                            }


                        }

                        //Jeremy '13,1,10 do update score and reverse lookup after updateRelatedPhrase to shorten the time user see related candidates after select a candidate.
                        if (shouldUpdateCandidates) {
                            updateCandidates();
                        } else {
                            committedCandidate = new Mapping(selectedCandidate);
                            selectedCandidate = null;
                            clearComposing(false);
                            updateRelatedPhrase(false);

                            if(committedCandidate != null && committedCandidate.getWord() != null){
                                SearchSrv.learnRelatedPhraseAndUpdateScore(committedCandidate);

                                //do reverse lookup and display notification if required.
                                SearchSrv.getCodeListStringFromWord(committedCandidate.getWord());
                            }
                        }

                    } else {
                        if (ic != null) ic.commitText(mComposing,
                                mComposing.length());

                    }
                } else {  //English mode or composing code or English run-time suggestion
                    if (ic != null) {
                        ic.commitText(mComposing, mComposing.length());
                        if(!mEnglishOnly) clearComposing(false);
                    }

                }


            }else if(LIMEUtilities.isUnicodeSurrogate(selectedCandidate.getWord())){ //Jeremy '15,7,16
                ic.commitText(selectedCandidate.getWord(), 1);
                clearComposing(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Candidate Hint Handlings -- Start
     */
    private static final int HINT_COUNT = 5;
    private static final int HIDE_HINT_INTERVAL = 1000 * 5;
    private Timer timer = null;
    private String hint = "";
    private void candidateHintAddWord(String word) {
        candidateHintView.setVisibility(View.VISIBLE);
        if (mCandidateView != null) {
            candidateHintView.setTextColor(mCandidateView.mColorNormalText);
        }

        String newHint = hint + word;
        if (newHint.length() > HINT_COUNT) {
            candidateHintView.setText(newHint.substring(newHint.length()-HINT_COUNT));
        } else {
            candidateHintView.setText(newHint);
        }
        hint = candidateHintView.getText().toString();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearCandidateHint();
                timer = null;
            }

        }, HIDE_HINT_INTERVAL);
    }

    private void candidateHintAddComposing(String composingText) {
        if (mCandidateView != null) {
            candidateHintView.setTextColor(mCandidateView.mColorNormalText);
        }
        candidateHintView.post(() -> {
            candidateHintView.setVisibility(View.VISIBLE);
            candidateHintView.setText(hint + composingText);
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    clearCandidateHint();
                    timer = null;
                }

            }, HIDE_HINT_INTERVAL);
        });
    }

    private void clearCandidateHint(){
        candidateHintView.post(() -> {
            candidateHintView.setText("");
            hint = "";
        });
    }

    private void candidateHintDeleteWord() {
        String currentHint = candidateHintView.getText().toString();
        if (currentHint.length() > 0) {
            candidateHintView.setText(currentHint.substring(0, currentHint.length() - 1));
        }
    }

    /**
     * Candidate Hint Handlings -- End
     */

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    public void updateShiftKeyState(EditorInfo attr) {
        if (DEBUG) Log.i(TAG, "updateShiftKeyState() ");
        InputConnection ic = getCurrentInputConnection();
        if (attr != null && mInputView != null
                && mKeyboardSwitcher.isAlphabetMode() && ic != null) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (mAutoCap && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = ic.getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        } else {
            if (!mCapsLock && mHasShift) {
                mKeyboardSwitcher.toggleShift();
                mHasShift = false;
            }
        }

    }

    private boolean isValidLetter(int code) {
        return Character.isLetter(code);
    }

    private boolean isValidDigit(int code) {
        return Character.isDigit(code);
    }

    private boolean isValidSymbol(int code) {
        String checkCode = String.valueOf((char) code);
        // code has to < 256, a ascii character
        return code < 256 && checkCode.matches(".*?[^A-Z]")
                && checkCode.matches(".*?[^a-z]")
                && checkCode.matches(".*?[^0-9]") && code != 32;
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode, boolean sendToSelf) {
        InputConnection ic = getCurrentInputConnection();

        long eventTime = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
        KeyEvent upEvent = new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, keyEventCode, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
        if (sendToSelf) {  //Jeremy '12,5,23 send to this.onKeyDown and onKeyUp if sendToSelf is true.
            if (!this.onKeyDown(keyEventCode, downEvent) && ic != null)
                ic.sendKeyEvent(downEvent);
            if (!this.onKeyUp(keyEventCode, upEvent) && ic != null)
                ic.sendKeyEvent(upEvent);

        } else if (ic != null) {
            ic.sendKeyEvent(downEvent);
            ic.sendKeyEvent(upEvent);
        }


    }


    public void onKey(int primaryCode, int[] keyCodes) {
        onKey(primaryCode, keyCodes, 0, 0);
    }

    public void onKey(int primaryCode, int[] keyCodes, int x, int y) {
        if (DEBUG)
            Log.i(TAG, "OnKey(): primaryCode:" + primaryCode
                    + " hasShiftPress:" + hasShiftPress);

        // Modified by Art
        // This is to fixed the CapsLock issue on Physical keyboard
        if (mCapsLock) {
            if (primaryCode >= 97 && primaryCode <= 122) {
                primaryCode -= 32;
            }
        }
        // Adjust metakeystate on printed key pressed.
        if (hasPhysicalKeyPressed) {  //Jeremy '12,6,11 moved from handleCharacter()
            mMetaState = LIMEMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
            setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState(); //Jeremy '12,6,13 moved from OnkeyUP by Jeremy '12,6,13
            if (DEBUG)
                Log.i(TAG, "onKey(): adjustMetaAfterKeypress()");

        }


        if (mLIMEPref.getEnglishPrediction()
                && primaryCode != LIMEBaseKeyboard.KEYCODE_DELETE) {

            // Check if input character not valid English Character then reset
            // temp english string
            if (!Character.isLetter(primaryCode) && mEnglishOnly) {

                //Jeremy '11,6,10. Select english suggestion with shift+123457890
                if (hasPhysicalKeyPressed && (mCandidateView != null && hasCandidatesShown)) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                    if (handleSelkey(primaryCode)) {
                        return;
                    }
                    resetTempEnglishWord();
                    if (!hasCtrlPress)
                        clearSuggestions(); //Jeremy '12,4,29 moved from resetcandidateBar
                }

            }
        }

        // Handle English/Lime Keyboard switch
        if (!mEnglishFlagShift
                && (primaryCode == LIMEBaseKeyboard.KEYCODE_SHIFT)) {
            mEnglishFlagShift = true;
        }
        if (primaryCode == LIMEBaseKeyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_SHIFT) {
            if (DEBUG) Log.i(TAG, "OnKey():KEYCODE_SHIFT");
            if (!(!hasPhysicalKeyPressed && hasDistinctMultitouch))
                handleShift();
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_DONE) {// long press on options and shift
            handleClose();
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_UP) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_UP, hasCandidatesShown);
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_DOWN) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN, hasCandidatesShown);
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_RIGHT) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT, hasCandidatesShown);
        } else if (primaryCode == LIMEBaseKeyboard.KEYCODE_LEFT) {
            keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT, hasCandidatesShown);
        } else if (primaryCode == LIMEKeyboardView.KEYCODE_OPTIONS) {
            handleOptions();
        } else if (primaryCode == LIMEKeyboardView.KEYCODE_SPACE_LONGPRESS) {
            handleOptions();
        } else if (primaryCode == LIMEKeyboardView.KEYCODE_SYMBOL_KEYBOARD) {
            mEnglishOnly = true;
            mKeyboardSwitcher.setKeyboardMode(activeIM, LIMEKeyboardSwitcher.MODE_PHONE, mImeOptions, false, false, false);
        } else if (primaryCode == KEYCODE_SWITCH_TO_SYMBOL_MODE && mInputView != null) { //->symbol keyboard
            switchKeyboard(primaryCode);
            // here
        } else if (primaryCode == KEYCODE_SWITCH_SYMBOL_KEYBOARD && mInputView != null) { //->switch symbols1 keyboards
            switchKeyboard(primaryCode);
        } else if (primaryCode == LIMEKeyboardView.KEYCODE_NEXT_IM) {
            switchToNextActivatedIM(true);
        } else if (primaryCode == LIMEKeyboardView.KEYCODE_PREV_IM) {
            switchToNextActivatedIM(false);
        } else if (primaryCode == KEYCODE_SWITCH_TO_ENGLISH_MODE && mInputView != null) { //chi->eng
            switchKeyboard(primaryCode);
            // Jeremy '11,5,31 Rewrite softkeybaord enter/space and english separator processing.
        } else if (primaryCode == KEYCODE_SWITCH_TO_IM_MODE && mInputView != null) { //eng -> chi
            switchKeyboard(primaryCode);
        } else if ( //Jeremy '12,7,1 bug fixed on enter not functioning in english mode
                ((primaryCode == MY_KEYCODE_SPACE && !mEnglishOnly && !activeIM.equals("phonetic"))
                        //||(primaryCode== MY_KEYCODE_SPACE &&  !mEnglishOnly &&
                        //		activeIM.equals("phonetic") //&& !mLIMEPref.getParameterBoolean("doLDPhonetic", true) 	
                        || (primaryCode == MY_KEYCODE_SPACE && !mEnglishOnly &&
                        activeIM.equals("phonetic") && (mComposing.toString().endsWith(" ") || mComposing.length() == 0))
                        || primaryCode == MY_KEYCODE_ENTER)) {

            if (hasCandidatesShown) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
                if (!pickHighlightedCandidate()) {//Jeremy '12,5,11 fixed for not sedning related.
                    if (mComposing.length() == 0)
                        hideCandidateView();
                    sendKeyChar((char) primaryCode);

                }

            } else {
                sendKeyChar((char) primaryCode);
            }

        } else {

            handleCharacter(primaryCode);

            // Art 11, 9, 26 Check if need to auto commit composing
            if (auto_commit > 0 && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                if (mComposing != null && mComposing.length() == auto_commit &&
                        currentSoftKeyboard != null && currentSoftKeyboard.contains("phone")) {
                    InputConnection ic = getCurrentInputConnection();
                    commitTyped(ic);

                }
            }
        }
    }


    private AlertDialog mOptionsDialog;
    // Contextual menu positions

    private static final int POS_SETTINGS = 0;
    private static final int POS_HANCONVERT = 1;  //Jeremy '11,9,17
    private static final int POS_KEYBOARD = 2;
    private static final int POS_METHOD = 3;
    private static final int POS_SPLIT_KEYBOARD = 4;
    private static final int POS_ADD_WORD = 5;


    /**
     * Add by Jeremy '10, 3, 24 for options menu in soft keyboard
     */

    private void handleOptions() {
        if (DEBUG)
            Log.i(TAG, "handleOptions()");
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);


        builder.setCancelable(true);
        builder.setIcon(R.drawable.sym_keyboard_done_dark);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setTitle(getResources().getString(R.string.ime_name));

        CharSequence itemSettings = getString(R.string.lime_setting_preference);
        CharSequence hanConvert = getString(R.string.han_convert_option_list);

        CharSequence itemSwitchIM = getString(R.string.keyboard_list);
        CharSequence itemSwitchSytemIM = getString(R.string.input_method);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int displayWidth = dm.widthPixels;
        int displayHeight = dm.heightPixels;
        final boolean isLandScape = displayWidth > displayHeight;

        CharSequence itemSplitKeyboard = getString(R.string.split_keyboard);
        if ((mSplitKeyboard == LIMEKeyboard.SPLIT_KEYBOARD_LANDSCAPD_ONLY && isLandScape)
                || mSplitKeyboard == LIMEKeyboard.SPLIT_KEYBOARD_ALWAYS)
            itemSplitKeyboard = getString(R.string.merge_keyboard);


        CharSequence[] options;
        CharSequence itemAddWord = "新增常用字";


        final boolean hasSplitOption;

        //Jeremy '12,5,27 do not show split/merge keyboard option if in landscape mode and show arrow keys is on
        if (isLandScape && mShowArrowKeys > 0) {
            hasSplitOption = false;
            options = new CharSequence[]
                    {itemSettings, hanConvert, itemSwitchIM, itemSwitchSytemIM, itemAddWord};
        } else {
            hasSplitOption = true;
            options = new CharSequence[]
                    {itemSettings, hanConvert, itemSwitchIM, itemSwitchSytemIM, itemSplitKeyboard, itemAddWord};

        }


        builder.setItems(options, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int position) {
                di.dismiss();
                switch (position) {

                    case POS_SETTINGS:
                        launchSettings();
                        break;
                    case POS_HANCONVERT:  //Jeremy '11,9,17
                        showHanConvertPicker();
                        break;
                    case POS_KEYBOARD:
                        showIMPicker();
                        break;
                    case POS_METHOD:
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
                        break;
                    case POS_SPLIT_KEYBOARD: //Jeremy '12,5,27 new option to split keyboard; '12,6,9 add orientation consideration on split keyboard
                        if (hasSplitOption) {
                            if (mSplitKeyboard == LIMEKeyboard.SPLIT_KEYBOARD_NEVER) {
                                if (isLandScape)
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_LANDSCAPD_ONLY);
                                else
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_ALWAYS);
                            } else if (mSplitKeyboard == LIMEKeyboard.SPLIT_KEYBOARD_ALWAYS) {
                                if (isLandScape)
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_NEVER);
                                else
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_LANDSCAPD_ONLY);
                            } else {// LIMEKeyboard.SPLIT_KEYBOARD_LANDSCAPD_ONLY
                                if (isLandScape)
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_NEVER);
                                else
                                    mLIMEPref.setSplitKeyboard(LIMEKeyboard.SPLIT_KEYBOARD_ALWAYS);
                            }

                            handleClose();
                            mKeyboardSwitcher.resetKeyboards(true);
                            break;
                        }
                    case POS_ADD_WORD:
                        showIMEAddWordPage();
                        break;

                }
            }
        });

        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    private void showIMEAddWordPage() {
        LIMEUtilities.showIMEAddWordPage(this, activeIM);
    }

    private void launchSettings() {
        handleClose();
        Intent intent = new Intent();
        intent.setClass(LIMEService.this, LIMEPreferenceHC.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void switchToNextActivatedIM(boolean forward) { // forward: true, next IM; false prev. IM
        if (DEBUG) Log.i(TAG, "switchToNextActivatedIM()");
        buildActivatedIMList();
        int i;
        CharSequence activeIMName = "";
        for (i = 0; i < activatedIMList.size(); i++) {
            if (activeIM.equals(activatedIMList.get(i))) {
                if (i == activatedIMList.size() - 1 && forward) {
                    activeIM = activatedIMList.get(0);
                    activeIMName = activatedIMNameList.get(0);
                } else if (i == 0 && !forward) {
                    activeIM = activatedIMList.get(activatedIMList.size() - 1);
                    activeIMName = activatedIMNameList.get(activatedIMList.size() - 1);
                } else {
                    activeIM = activatedIMList.get(i + ((forward) ? 1 : -1));
                    activeIMName = activatedIMNameList.get(i + ((forward) ? 1 : -1));
                }
                break;
            }
        }
        mLIMEPref.setActiveIM(activeIM);
        //Jeremy '12,4,21 force clear when switch to next keybaord
        clearComposing(false);
        // cancel candidate view if it's shown
        mEnglishOnly = false;
        mLIMEPref.setLanguageMode(false);
        //initialKeyboard();
        initialIMKeyboard();
        Toast.makeText(this, activeIMName, Toast.LENGTH_SHORT).show();
        try {
            mKeyboardSwitcher.setKeyboardList(SearchSrv.getKeyboardList());
            mKeyboardSwitcher.setImList(SearchSrv.getImList());
            //mKeyboardSwitcher.clearKeyboards();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Update keyboard xml information
        currentSoftKeyboard = mKeyboardSwitcher.getImKeyboard(activeIM);
    }

    private void buildActivatedIMList() {

        CharSequence[] items = getResources().getStringArray(R.array.keyboard);
        CharSequence[] shortNames = getResources().getStringArray(R.array.keyboardShortname);
        CharSequence[] codes = getResources().getStringArray(
                R.array.keyboard_codes);

        String pIMActiveState = mLIMEPref.getIMActivatedState();

        if (pIMActiveState.trim().isEmpty()) {

            activatedIMNameList.clear();
            activatedIMList.clear();
            activatedIMShortNameList.clear();
            return;
        }

        if (!(mIMActivatedState.length() > 0 && mIMActivatedState.equals(pIMActiveState))) {

            mIMActivatedState = pIMActiveState;

            String[] s = pIMActiveState.split(";");

            activatedIMNameList.clear();
            activatedIMList.clear();
            activatedIMShortNameList.clear();

            for (String value : s) {
                if (value.isEmpty()) continue;
                int index = Integer.parseInt(value);

                if (index < items.length) {
                    activatedIMNameList.add(items[index].toString());
                    activatedIMShortNameList.add(shortNames[index].toString());
                    activatedIMList.add(codes[index].toString());
                    if (DEBUG)
                        Log.i(TAG, "buildActivatedIMList()(): buildActivatedIMList()[" + index + "] = "
                                + codes[index].toString() + " ;" + shortNames[index].toString());
                } else {
                    break;
                }
            }
        }
        if (DEBUG) Log.i(TAG, "current active IM:" + activeIM);
        // check if the selected keybaord is in active keybaord list.
        boolean matched = false;
        for (int i = 0; i < activatedIMList.size(); i++) {
            if (activeIM.equals(activatedIMList.get(i))) {
                if (DEBUG)
                    Log.i(TAG, "buildActivatedIMList(): activatedIM[" + i + "] matches current active IM: " + activeIM);
                matched = true;
                break;
            }
        }
        if (!matched && SearchSrv != null) {
            // if the selected keyboard is not in the active keyboard list.
            // set the keyboard to the first active keyboard
            //if(DEBUG) Log.i(TAG, "current keyboard is not in active list, reset to :" +  keyboardListCodes.get(0));

            try {
                activeIM = activatedIMList.get(0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                //Toast.makeText(this, getResources().getString(R.string.error_set_active_im), Toast.LENGTH_LONG).show();
            }
            //initializeIMKeyboard();

        }

    }

    /**
     * Add by Jeremy '11,9,17 for han convert (tranditional <-> simplifed) options
     */
    private void showHanConvertPicker() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setCancelable(true);
        builder.setIcon(R.drawable.sym_keyboard_done_light);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setTitle(getResources().getString(R.string.han_convert_option_list));
        CharSequence[] items = getResources().getStringArray(R.array.han_convert_options);
        builder.setSingleChoiceItems(items, mLIMEPref.getHanCovertOption(),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        handleHanConvertSelection(position);
                    }
                });

        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        if (!(window == null)) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.token = mCandidateViewStandAlone.getWindowToken();  //Jeremy 12,5,4 it's always there 
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        }
        mOptionsDialog.show();
    }

    private void handleHanConvertSelection(int position) {
        mLIMEPref.setHanCovertOption(position);

    }

    /**
     * Add by Jeremy '10, 3, 24 for IM picker menu in options menu
     * renamed to showIMPicker from showKeybaordPicer to avoid confusion '12,3,40
     */
    private void showIMPicker() {
        if (DEBUG)
            Log.i(TAG, "showIMPicker()");
        buildActivatedIMList();

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setCancelable(true);
        builder.setIcon(R.drawable.sym_keyboard_done_light);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setTitle(getResources().getString(R.string.keyboard_list));

        CharSequence[] items = new CharSequence[activatedIMNameList.size()];// =
        // getResources().getStringArray(R.array.keyboard);
        int curKB = 0;
        for (int i = 0; i < activatedIMNameList.size(); i++) {
            items[i] = activatedIMNameList.get(i);
            if (activeIM.equals(activatedIMList.get(i)))
                curKB = i;
        }

        builder.setSingleChoiceItems(items, curKB,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        handleIMSelection(position);
                    }
                });

        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        // Jeremy '10, 4, 12
        // The IM is not initialialized. do nothing here if window=null.
        if (!(window == null)) {
            WindowManager.LayoutParams lp = window.getAttributes();
            // Jeremy '11,8,28 Use candidate instead of mInputview because mInputView may not present when using physical keyboard
            lp.token = mCandidateViewStandAlone.getWindowToken();  //always there Jeremy '12,5,4 
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        }
        mOptionsDialog.show();

    }

    private void handleIMSelection(int position) {
        if (DEBUG) Log.i(TAG, "handleIMSelection() position = " + position);

        activeIM = activatedIMList.get(position);

        mLIMEPref.setActiveIM(activeIM);
        //spe.putString("keyboard_list", keyboardSelection);
        //spe.commit();


        //Jeremy '12,4,21 foce clear when switch to selected keybaord
        if (!mEnglishOnly) clearComposing(true);

        mEnglishOnly = false;//Jeremy '12,5,24 force to switch to Chinese mode if it's choosing in english mode.
        initialIMKeyboard();

        try {
            mKeyboardSwitcher.setKeyboardList(SearchSrv.getKeyboardList());
            mKeyboardSwitcher.setImList(SearchSrv.getImList());
            //mKeyboardSwitcher.clearKeyboards();

            // Update soft keybaord information
            currentSoftKeyboard = mKeyboardSwitcher.getImKeyboard(activeIM);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void onText(CharSequence text) {
        if (DEBUG)
            Log.i(TAG, "OnText()");
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        ic.beginBatchEdit();

        if (mPredicting) {
            commitTyped(ic);
            //mJustRevertedSeparator = null;
        } else if (!mEnglishOnly && mComposing.length() > 0) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
            pickHighlightedCandidate();
            //	commitTyped(ic);
        }
        ic.commitText(text, 1);
        //ic.commitText(text, 0);

        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void updateCandidates() {
        this.updateCandidates(false);
    }


    private void updateChineseSymbol() {
        //ChineseSymbol chineseSym = new ChineseSymbol();
        hasChineseSymbolCandidatesShown = true;
        List<Mapping> list = ChineseSymbol.getChineseSymoblList();
        if (list.size() > 0) {

            // Setup sel key display if 
            String selkey = "1234567890";
            if (disable_physical_selection && hasPhysicalKeyPressed) {
                selkey = "";
            }

            setSuggestions(list, hasPhysicalKeyPressed, selkey);

            if (DEBUG) Log.i(TAG, "updateChineseSymbol():"
                    + "mCandidateList.size:" + mCandidateList.size());
        }

    }


    /**
     * Update the list of available candidates from the current composing text.
     * This will need to be filled in by however you are determining candidates.
     */
    public void updateCandidates(final boolean getAllRecords) {

        if (DEBUG) Log.i(TAG, "updateCandidate():Update Candidate mComposing:" + mComposing);

        hasChineseSymbolCandidatesShown = false;

        if (mComposing.length() > 0) {

            final LinkedList<Mapping> list = new LinkedList<>();

            String keyString = mComposing.toString();

            //Art '30,Sep,2011 restrict the length of composing text for Stroke5
            if (currentSoftKeyboard.contains("wb")) {
                if (keyString.length() > 5) {
                    keyString = keyString.substring(0, 5);
                    mComposing = new StringBuilder();
                    mComposing.append(keyString);
                    InputConnection ic = getCurrentInputConnection();
                    if(ic!=null && mPredictionOn) ic.setComposingText(keyString, 1);
                }
            }

            final String finalKeyString = keyString;
            final boolean finalHasPhysicalKeyPressed = hasPhysicalKeyPressed;
            if (queryThread != null && queryThread.isAlive()) queryThread.interrupt();
            queryThread = new Thread() {

                public void run() {

                    try {
                        list.addAll(SearchSrv.getMappingByCode(finalKeyString, !finalHasPhysicalKeyPressed, getAllRecords));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
//                    try {
//                        sleep(0);
//                    } catch (InterruptedException ignored) {
//                        ignored.printStackTrace();
//                        return;   // terminate thread here, since it is interrupted and more recent getMappingByCode will update the suggestions.
//                    }
                    //Jeremy '11,6,19 EZ and ETEN use "`" as IM Keys, and also custom may use "`".
                    if (list.size() > 0) {
                        // Setup sel key display if
                        String selkey = null;
                        if (disable_physical_selection && finalHasPhysicalKeyPressed) {
                            selkey = "";
                        } else {
                            try {
                                selkey = SearchSrv.getSelkey();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            String mixedModeSelkey = "`";
                            if (hasSymbolMapping && !activeIM.equals("dayi")
                                    && !(activeIM.equals("phonetic")
                                    && mLIMEPref.getPhoneticKeyboardType().equals("standard"))) {
                                mixedModeSelkey = " ";
                            }


                            int selkeyOption = mLIMEPref.getSelkeyOption();
                            if (selkeyOption == 1) selkey = mixedModeSelkey + selkey;
                            else if (selkeyOption == 2) selkey = mixedModeSelkey + " " + selkey;
                        }

//                        try {
//                            sleep(0);
//                        } catch (InterruptedException ignored) {
//                            ignored.printStackTrace();
//                            return;   // terminate thread here, since it is interrupted and more recent getMappingByCode will update the suggestions.
//                        }


                        // Emoji Control
                        // Check the Emoji parameter setting and load icons into the suggestions list
                        if(mLIMEPref.getEmojiMode() && !finalHasPhysicalKeyPressed){
                            HashMap<String, String> emojiCheck = new HashMap<>();
                            List<Mapping> emojiList = new LinkedList<>();

                            if(list.size() > 0){

                                List<Mapping> item1 = null, item2, item3;

                                int insertPosition = mLIMEPref.getEmojiDisplayPosition();
                                if(list.size() <= insertPosition){
                                    insertPosition = list.size();
                                }

                                if( list.get(0).getWord().matches("[A-Za-z]+") ) {

                                    item1 = SearchSrv.emojiConvert(list.get(0).getWord(), Lime.EMOJI_EN);
                                    if (item1.size() > 0) {
                                        for (Mapping m : item1) {
                                            if (emojiCheck.get(m.getWord()) == null) {
                                                emojiList.add(m);
                                                emojiCheck.put(m.getWord(), m.getWord());
                                            }
                                        }
                                    }

                                }

                                if(item1 == null || item1.size() == 0){

                                    //Log.i("EMOJI Check:", ""+list.get(1).getWord().getBytes().length);
                                    if (list.size() > 1 && list.get(1) != null && list.get(1).getWord() != null &&
                                            list.get(1).getWord().getBytes().length > 1 &&
                                            list.get(1).getWord().length() < 4
                                    ) {
                                        item2 = SearchSrv.emojiConvert(list.get(1).getWord(), Lime.EMOJI_TW);
                                        if (item2.size() > 0) {
                                            for (Mapping m : item2) {
                                                if (emojiCheck.get(m.getWord()) == null) {
                                                    emojiList.add(m);
                                                    emojiCheck.put(m.getWord(), m.getWord());
                                                }
                                            }
                                        }
                                        if (item2.size() == 0) {
                                            item3 = SearchSrv.emojiConvert(list.get(1).getWord(), Lime.EMOJI_CN);
                                            if (item3.size() > 0) {
                                                for (Mapping m : item3) {
                                                    if (emojiCheck.get(m.getWord()) == null) {
                                                        emojiList.add(m);
                                                        emojiCheck.put(m.getWord(), m.getWord());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if(emojiList.size() > 0){
                                    list.addAll(insertPosition, emojiList);
                                }
                            }
                        }

                        setSuggestions(list, finalHasPhysicalKeyPressed, selkey);

                        if (DEBUG) Log.i(TAG, "updateCandidates(): display selkey:" + selkey
                                + ", list.size:" + list.size()
                                + ", mComposing = " + mComposing);
                    } else {
                        //Jeremy '11,8,14
                        clearSuggestions();
                    }

                    // Show composing window if keyToKeyname got different string. Revised by Jeremy '11,6,4
                    if (SearchSrv.getTablename() != null) {
                        String keynameString = SearchSrv.keyToKeyname(finalKeyString); //.toLowerCase(Locale.US)); moved to LimeDB
                        if (mLIMEPref.shouldShowTypedWord()) {
                            candidateHintAddComposing(keynameString);
                        }
                        if (mCandidateView != null
                                && !keynameString.toUpperCase(Locale.US).equals(finalKeyString.toUpperCase(Locale.US))
                                && !keynameString.trim().equals("")
                        ) {
                            try {
                                sleep(0);
                            } catch (InterruptedException ignored) {
                                ignored.printStackTrace();
                                return;   // terminate thread here, since it is interrupted and more recent getMappingByCode will update the suggestions.
                            }
                            mCandidateView.setComposingText(keynameString);
                        }
                    }
                }
            };
            queryThread.start();
        } else
            //Jermy '11,8,14
            clearSuggestions();
    }

    /*
	 * Update English suggestions view
	 */
    private void updateEnglishPrediction() {

        hasChineseSymbolCandidatesShown = false;
        if (mPredictionOn && mLIMEPref.getEnglishPrediction()) {

            try {

                final LinkedList<Mapping> list = new LinkedList<>();

                if (tempEnglishWord == null || tempEnglishWord.length() == 0) {
                    //Jeremy '11,8,14
                    clearSuggestions();
                } else {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic == null) return;
                    boolean after = false;
                    try {
                        if( ic.getTextAfterCursor(1, 1).length() > 0){
                            char c = ic.getTextAfterCursor(1, 1).charAt(0);
                            if (!Character.isLetterOrDigit(c)) {
                                after = true;
                            }
                        }else{
                            after = true;
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        after = true;
                    }

                    boolean matchedtemp = false;

                    if (tempEnglishWord.length() > 0) {
                        try {
                            if (tempEnglishWord.toString()
                                    .equalsIgnoreCase(
                                            ic.getTextBeforeCursor(
                                                    tempEnglishWord.toString()
                                                            .length(), 1)
                                                    .toString())) {
                                matchedtemp = true;
                            }
                        } catch (StringIndexOutOfBoundsException ignored) {
                            ignored.printStackTrace();
                        }
                    }

                    if (after || matchedtemp) {

                        tempEnglishList.clear();

                        final boolean finalHasPhysicalKeyPressed = hasPhysicalKeyPressed;
                        if (queryThread != null && queryThread.isAlive()) queryThread.interrupt();
                        queryThread = new Thread() {
                            public void run() {
                                final Mapping self = new Mapping();
                                self.setWord(tempEnglishWord.toString());
                                self.setComposingCodeRecord();

                                List<Mapping> suggestions = null;
                                try {
                                    suggestions = SearchSrv.getEnglishSuggestions(tempEnglishWord.toString());
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    sleep(0);
                                } catch (InterruptedException ignored) {
                                    ignored.printStackTrace();
                                    return;   // terminate thread here, since it is interrupted and more recent getMappingByCode will update the suggestions.
                                }

                                if ((suggestions != null ? suggestions.size() : 0) > 0) {
                                    list.add(self);
                                    assert suggestions != null;
                                    list.addAll(suggestions);

                                    // Setup sel key display if
                                    String selkey = "1234567890";
                                    if (disable_physical_selection && finalHasPhysicalKeyPressed) {
                                        selkey = "";
                                    }
                                    try {
                                        sleep(0);
                                    } catch (InterruptedException ignored) {
                                        ignored.printStackTrace();
                                        return;   // terminate thread here, since it is interrupted and more recent getMappingByCode will update the suggestions.
                                    }


                                    // Emoji Control
                                    // Check the Emoji parameter setting and load icons into the suggestions list
                                    if(mLIMEPref.getEmojiMode()){
                                        HashMap<String, String> emojiCheck = new HashMap<>();
                                        List<Mapping> emojiList = new LinkedList<>();

                                        if(list.size() > 0){

                                            List<Mapping> item1;
                                            int insertPosition = mLIMEPref.getEmojiDisplayPosition();
                                            if(list.size() <= insertPosition){
                                                insertPosition = list.size();
                                            }

                                            item1 = SearchSrv.emojiConvert(list.get(0).getWord(), Lime.EMOJI_EN);
                                            if(item1.size() > 0){
                                                for(Mapping m: item1){
                                                    if(emojiCheck.get(m.getWord()) == null){
                                                        emojiList.add(m);
                                                        emojiCheck.put(m.getWord(), m.getWord());
                                                    }
                                                }
                                            }

                                            if(emojiList.size() > 0){
                                                list.addAll(insertPosition, emojiList);
                                            }
                                        }
                                    }


                                    //Log.i("EMOJIbefore:", tempEnglishList.size() + "");
                                    tempEnglishList.addAll(list);
                                    setSuggestions(list, finalHasPhysicalKeyPressed, selkey);

                                    //Log.i("EMOJIafter:", tempEnglishList.size() + "");

                                } else {
                                    //Jermy '11,8,14
                                    clearSuggestions();
                                }
                            }
                        };
                        queryThread.start();
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("ART", "Error to update English predication");
            }
        }
    }

    /*
	 * Update dictionary view
	 */
    private void updateRelatedPhrase(final boolean getAllRecords) {
        if (DEBUG)
            Log.i(TAG, "updateRelatedPhrase()");
        hasChineseSymbolCandidatesShown = false;
        // Also use this to control whether need to display the english
        // suggestions words.

        // If there is no Temp Matched word exist then not to display dictionary
        // Modified by Jeremy '10, 4,1. getCode -> getWord
        // if( tempMatched != null && tempMatched.getCode() != null &&
        // !tempMatched.getCode().equals("")){
        if (committedCandidate != null && committedCandidate.getWord() != null
                && !committedCandidate.getWord().equals("")) {

            final boolean finalHasPhysicalKeyPressed = hasPhysicalKeyPressed;
            if (queryThread != null && queryThread.isAlive()) queryThread.interrupt();
            queryThread = new Thread() {
                public void run() {

                    LinkedList<Mapping> list = new LinkedList<>();
                    //Jeremy '11,8,9 Insert completion suggestions from application
                    //in front of related dictionary list in full-screen mode
                    if (mCompletionOn) {
                        list.addAll(buildCompletionList());
                    }


                    if (committedCandidate != null && hasMappingList) {
                        if (queryThread != null && queryThread.isAlive()) queryThread.interrupt();
                        if(!committedCandidate.isEmojiRecord() && !committedCandidate.isChinesePunctuationSymbolRecord()){
                            list.addAll(SearchSrv.getRelatedPhrase(committedCandidate.getWord(), getAllRecords));
                        }

                        if (list.size() > 0) {


                            // Setup sel key display if
                            String selkey = "1234567890";
                            if (disable_physical_selection && finalHasPhysicalKeyPressed) {
                                selkey = "";
                            }

                            setSuggestions(list, finalHasPhysicalKeyPressed && !isFullscreenMode(), selkey);
                        } else {
                            committedCandidate = null;
                            //Jermy '11,8,14
                            clearSuggestions();
                        }
                    }
                }
            };
            queryThread.start();
        }

    }

    private List<Mapping> buildCompletionList() {
        LinkedList<Mapping> list = new LinkedList<>();
        for (int i = 0; i < (mCompletions != null ? mCompletions.length : 0); i++) {
            CompletionInfo ci = mCompletions[i];
            if (ci != null) {
                Mapping temp = new Mapping();
                temp.setWord(ci.getText().toString());
                temp.setCode("");
                temp.setCompletionSuggestionRecord();
                list.add(temp);
            }
        }
        return list;
    }


    private void initCandidateView() {
        if (DEBUG) Log.i(TAG, "initCandidateView()");

        mCandidateViewHandler.showCandidateView();
        mCandidateViewHandler.hideCandidateView();
    }

    private void showCandidateView() {
        if (DEBUG) Log.i(TAG, "showCandidateView()");
        mCandidateViewHandler.showCandidateView();
    }

    private void hideCandidateView() {
        if (DEBUG) Log.i(TAG, "hideCandidateView()");
        if (mCandidateView != null) mCandidateView.clear();
        hasCandidatesShown = false;
        hasChineseSymbolCandidatesShown = false;
        if (mCandidateViewStandAlone == null || (!mCandidateViewStandAlone.isShown()))
            return;  // escape if mCandidateViewStandAlone is not created or it's not shown '12,5,6, Jeremy 

        mCandidateViewHandler.hideCandidateViewDelayed(DELAY_BEFORE_HIDE_CANDIDATE_VIEW);
        clearCandidateHint();
    }

    private void forceHideCandidateView() {
        if (DEBUG) Log.i(TAG, "forceHideCandidateView()");

        if (mComposing != null && mComposing.length() > 0)
            mComposing.setLength(0);

        selectedCandidate = null;

        if (mCandidateList != null)
            mCandidateList.clear();

        if (mFixedCandidateViewOn) {
            //mCandidateViewInInputView.forceHide();
        } else {
            //hideCandidateView();
        }
    }


    final CandidateViewHandler mCandidateViewHandler = new CandidateViewHandler(this);


    private static class CandidateViewHandler extends Handler {

        private final WeakReference<LIMEService> mLIMEService;
        private final int MSG_SHOW_CANDIDATE_VIEW = 1;
        private final int MSG_HIDE_CANDIDATE_VIEW = 2;

        CandidateViewHandler(LIMEService im){
            mLIMEService = new WeakReference<>(im);
        }
        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) Log.i(TAG, "CandidateViewHandler.handleMessage(): message:" + msg.what);
            LIMEService mLIMEInstance = mLIMEService.get();
            if(mLIMEInstance == null) return;
            switch (msg.what) {
                case MSG_SHOW_CANDIDATE_VIEW:
                    mLIMEInstance.setCandidatesViewShown(true);
                    break;
                case MSG_HIDE_CANDIDATE_VIEW:
                    mLIMEInstance.setCandidatesViewShown(false);
                    break;
            }
        }
        void showCandidateView()
        {
            removeMessages(MSG_HIDE_CANDIDATE_VIEW);  //cancel previous hide messages if any
            sendMessage(obtainMessage(MSG_SHOW_CANDIDATE_VIEW));
        }
        void hideCandidateView()
        {
            sendMessage(obtainMessage(MSG_HIDE_CANDIDATE_VIEW));
        }

        void hideCandidateViewDelayed(int delay)
        {
            sendMessageDelayed(obtainMessage(MSG_HIDE_CANDIDATE_VIEW),delay);
        }
    }

    public synchronized void setSuggestions(List<Mapping> suggestions, boolean showNumber, String diplaySelkey) {
        if (suggestions != null && suggestions.size() > 0) {

            if (DEBUG)
                Log.i(TAG, "setSuggestion():suggestions.size=" + suggestions.size()
                            + ", mComposing = " + mComposing
                            + ", mFixedCandidateViewOn:" + mFixedCandidateViewOn
                            + ", hasPhysicalKeyPressed:" + hasPhysicalKeyPressed
            );

            if ((!mFixedCandidateViewOn || hasPhysicalKeyPressed)
                    && mCandidateView != mCandidateViewStandAlone) {
                mCandidateViewInInputView.clear();

                mCandidateView = mCandidateViewStandAlone; //Jeremy '12,5,4 use standalone candidateView for physical keyboard (no soft keyboard shown)
                //forceHideCandidateView(); //Jeremy '16,7,19 caused the first composing character missing typed with physical keyboard.
                if (hasPhysicalKeyPressed) {
                    // cancel the current composing first before closing soft keyboard and switched to physical keyboarding typing.
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null && mPredictionOn) ic.setComposingText("", 0);
                    mInputView.closing();
                    requestHideSelf(0);
                    // preserved the last character typed with physical keyboard in composing
                    if(mComposing.length() > 1)
                        mComposing.delete(0, mComposing.length()-1);
                    updateCandidates();
                }
            } else if((mFixedCandidateViewOn || !hasPhysicalKeyPressed ) &&
                    mCandidateView != mCandidateViewInInputView) {
                mCandidateViewStandAlone.clear();
                hideCandidateView();
                mCandidateView = mCandidateViewInInputView;
                if(mCandidateViewStandAlone!=null) mCandidateViewStandAlone.setEmbeddedComposingView(null);
            }
            if (!mFixedCandidateViewOn || (hasPhysicalKeyPressed))
                showCandidateView();

            hasCandidatesShown = true; //Jeremy '15,6,1 move after hideCandidateView if candidateView is fixed.
            hasMappingList = true;

            if (mCandidateView != null) {
                mCandidateList = (LinkedList<Mapping>) suggestions;
                try {

                    if (suggestions.size() > 1 && suggestions.get(1).isExactMatchToCodeRecord()) {
                        selectedCandidate = suggestions.get(1);
                        //selectedIndex = 1;
                        // this is for no exact match condition with code.  //do not set default suggestion for other record type like chinese punctuation symbols1 or related phrases. Jeremy '15,6,4
                    } else if (suggestions.size() > 0) {
                            selectedCandidate = suggestions.get(0);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mCandidateView.setSuggestions(suggestions, showNumber, diplaySelkey);

                if (DEBUG)
                    Log.i(TAG, "setSuggestion(): mCandidateList.size: " + mCandidateList.size()
                            + ", mComposing = " + mComposing);
            }
        } else {
            if (DEBUG) Log.i(TAG, "setSuggestion() with list=null");
            hasMappingList = false;
            //Jeremy '11,8,15
            clearSuggestions();
        }

    }


    private void handleBackspace() {
        if (DEBUG)
            Log.i(TAG, "handleBackspace()");
        final int length = mComposing.length();
        InputConnection ic = getCurrentInputConnection();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length == 1) {
            //Jeremy '12,4, 21 force clear the last characacter in composing
            clearComposing(true);
            //Jeremy '12,4,29 use mEnglishOnly instead of onIM
        } else if (!mEnglishOnly  // composing length == 0 after here
                && (hasCandidatesShown)// repalce isCandaiteShwon() with hasCandidatesShwn by Jeremy '12,5,6
                //&& mLIMEPref.getAutoChineseSymbol()
                && !hasChineseSymbolCandidatesShown) {
            clearComposing(false);  //Jeremy '12,4,21 composing length 0, no need to force commit again. 
        } else if (!mEnglishOnly
                //&& mCandidateView !=null && isCandidateShown() 
                && hasCandidatesShown //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
            //&& !mFixedCandidateViewOn //Jeremy '12,5,23 clear the chinese symbol list for arrow keys to do navigation inside document
                ) {
            hideCandidateView();  //Jeremy '11,9,8
        } else {
            try {
                if (mEnglishOnly && mLIMEPref.getEnglishPrediction() && mPredictionOn
                        && (!hasPhysicalKeyPressed || mLIMEPref.getEnglishPredictionOnPhysicalKeyboard())//mPredictionOnPhysicalKeyboard)
                        ) {
                    if (tempEnglishWord != null && tempEnglishWord.length() > 0) {
                        tempEnglishWord.deleteCharAt(tempEnglishWord.length() - 1);
                        updateEnglishPrediction();
                    }

                }
                keyDownUp(KeyEvent.KEYCODE_DEL, false);
                candidateHintDeleteWord();

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "->" + e);
            }
        }

    }

    public void   setCandidatesViewShown(boolean shown) {

        if (DEBUG)
            Log.i(TAG, "setCandidateViewShown():" + shown);
        if (shown)
            super.setCandidatesViewShown(true);
        else
            super.setCandidatesViewShown(false);

        if (DEBUG)
            Log.i(TAG, "isCandidateViewShown:" + mCandidateViewStandAlone.isShown());

    }


    private void handleShift() {
        if (DEBUG) Log.i(TAG, "handleShift()");
        if (mInputView == null) {
            return;
        }

        if (mKeyboardSwitcher.isAlphabetMode()) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
            mHasShift = mCapsLock || !mInputView.isShifted();
            if (mHasShift) {
                mKeyboardSwitcher.toggleShift();
            }
        } else {
            if (mCapsLock) {
                toggleCapsLock();
                mHasShift = false;
            } else if (mHasShift) {
                toggleCapsLock();
                mHasShift = true;
            } else {
                mKeyboardSwitcher.toggleShift();
                mHasShift = mKeyboardSwitcher.isShifted();
            }
        }
    }


    /**
     * Integrated all soft keyboards switching in this function.
     */
    private void switchKeyboard(int primaryCode) {
        if (DEBUG) Log.i(TAG, "switchKeyboard() primaryCode = " + primaryCode);

        if (mCapsLock) toggleCapsLock();

        // Auto commit the text when user switch the keyboard from chi -> eng
        try {
            if (mComposing != null && mComposing.length() > 0) {
                getCurrentInputConnection().commitText(mComposing, 1);
                finishComposing();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        clearComposing(false);
        hideCandidateView();

        if (primaryCode == KEYCODE_SWITCH_TO_SYMBOL_MODE) { //Symbol keyboard
            mEnglishOnly = true;
            mKeyboardSwitcher.toggleSymbols();
            if (mFixedCandidateViewOn) {
                forceHideCandidateView();
            }
        }
        else if (primaryCode == KEYCODE_SWITCH_SYMBOL_KEYBOARD) { //Symbol keyboard
                mEnglishOnly = true;
                mKeyboardSwitcher.switchSymbols();
                if(mFixedCandidateViewOn) {
                    forceHideCandidateView();
                }
        } else if (primaryCode == KEYCODE_SWITCH_TO_ENGLISH_MODE) { //Chi --> Eng
            mEnglishOnly = true;
            mLIMEPref.setLanguageMode(true);
            mKeyboardSwitcher.toggleChinese();
            if(mFixedCandidateViewOn) {
                if (!mPredictionOn) {
                    //forceHideCandidateView();
                } else {
                    mCandidateViewInInputView.setSuggestions(null, false);  // reset the candidate view if it's force hided before
                }
            }
        } else if (primaryCode == KEYCODE_SWITCH_TO_IM_MODE) { //Eng --> Chi moved from SwitchKeyboardIM by Jeremy '12,4,29
            mEnglishOnly = false;
            mLIMEPref.setLanguageMode(false);
            initialIMKeyboard();
            if (mFixedCandidateViewOn) {
                mCandidateViewInInputView.setSuggestions(null,false);  // reset the candiate view if it's force hided before
            }
        }

        mHasShift = false;
        updateShiftKeyState(getCurrentInputEditorInfo());

        // Update keyboard xml information
        currentSoftKeyboard = mKeyboardSwitcher.getImKeyboard(activeIM);

    }


    /**
     * For physical keybaord to switch between chinese and english mode.
     */
    private void switchChiEng() {
        if (DEBUG) Log.i(TAG, "switchChiEng(): mEnglishOnly:" + mEnglishOnly);

        //Jeremy '12,4,21 force clear before switching chi/eng
        clearComposing(false);

        mKeyboardSwitcher.toggleChinese();
        mEnglishOnly = !mKeyboardSwitcher.isChinese();
        mLIMEPref.setLanguageMode(mEnglishOnly);

        if (DEBUG)
            Log.i(TAG, "switchChiEng(): mEnglishOnly updated as " + mEnglishOnly);


        if (mEnglishOnly) {
            Toast.makeText(this, R.string.typing_mode_english,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.typing_mode_mixed,
                    Toast.LENGTH_SHORT).show();
        }
        clearSuggestions(); //Jeremy '11,9,5
    }


    @SuppressLint("InflateParams")
    private void initialViewAndSwitcher(boolean forceRecreate) {
        if (DEBUG)
            Log.i(TAG, "initialViewAndSwitcher() mKeyboardThemeIndex = " + mKeyboardThemeIndex + ", mLIMEPref.getKeyboardTheme() = " + mLIMEPref.getKeyboardTheme());

        boolean mForceRecreate = forceRecreate;
        if(mKeyboardThemeIndex != mLIMEPref.getKeyboardTheme()) {
            mKeyboardThemeIndex = mLIMEPref.getKeyboardTheme();
            mForceRecreate=true;
            mThemeContext = null;
            if(mKeyboardSwitcher!=null) mKeyboardSwitcher.resetKeyboards(true);
        }

        if(mThemeContext==null ) {
            mThemeContext = new ContextThemeWrapper(this, getKeyboardTheme());
            if(mKeyboardSwitcher!=null) mKeyboardSwitcher.setThemedContext(mThemeContext);

        }

        if (mFixedCandidateViewOn) { //Have candidateview in InputView
            //Create inputView if it's null 
            if (mCandidateInInputView == null || mForceRecreate) {

                mCandidateInInputView = (CandidateInInputViewContainer) LayoutInflater.from(mThemeContext).inflate(
                        R.layout.inputcandidate, null);
                mInputView = mCandidateInInputView.findViewById(R.id.keyboard);
                mInputView.setOnKeyboardActionListener(this);
                hasDistinctMultitouch = mInputView.hasDistinctMultitouch();
                mInputView.setHardwareAcceleratedDrawingEnabled(mIsHardwareAcceleratedDrawingEnabled);
                mCandidateInInputView.initViews();
                mCandidateViewInInputView = mCandidateInInputView.findViewById(R.id.candidatesView);
                mCandidateViewInInputView.setService(this);

                candidateHintView = mCandidateInInputView.findViewById(R.id.candidate_hint);
            }
            if (mCandidateView != mCandidateViewInInputView)
                mCandidateView = mCandidateViewInInputView;

        } else {
            if (mInputView == null || forceRecreate) {
                mInputView = (LIMEKeyboardView) LayoutInflater.from(mThemeContext).inflate(R.layout.input, null);
                mInputView.setOnKeyboardActionListener(this);
                mInputView.setHardwareAcceleratedDrawingEnabled(mIsHardwareAcceleratedDrawingEnabled);

            }
            mCandidateView = mCandidateViewStandAlone;

        }

        // Check if mKeyboardSwitcher == null
        if (mKeyboardSwitcher == null) {
            mKeyboardSwitcher = new LIMEKeyboardSwitcher(this, mThemeContext);
        }
        mKeyboardSwitcher.setInputView(mInputView);
        buildActivatedIMList();
        mKeyboardSwitcher.setActivatedIMList(activatedIMList, activatedIMNameList, activatedIMShortNameList);

        if (mKeyboardSwitcher.getKeyboardSize() == 0 && SearchSrv != null) {
            try {
                mKeyboardSwitcher.setKeyboardList(SearchSrv.getKeyboardList());
                mKeyboardSwitcher.setImList(SearchSrv.getImList());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        adjustHintPosition();
    }

    private void adjustHintPosition() {
        if (mKeyboardSwitcher.getImKeyboard(activeIM).equals("lime")) {
            candidateHintView.setTranslationY(pxToDp(-20, this));
        }
    }


    private int pxToDp(int px, LIMEService limeService) {
        return (int) (limeService.getResources().getDisplayMetrics().density * px);
    }

    /**
     * For initializing Chinese IM and corresponding soft keyboards.
     */
    private void initialIMKeyboard() {
        if (DEBUG)
            Log.i(TAG, "initalizeIMKeyboard(): keyboardSelection:" + activeIM);
        //mEnglishOnly = false;
        //super.setCandidatesViewShown(false);

        switch (activeIM) {
            case "custom":
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);

                hasNumberMapping = mLIMEPref.getAllowNumberMapping();
                hasSymbolMapping = mLIMEPref.getAllowSymoblMapping();
                break;
            case "cj":
            case "scj":
            case "cj5":
            case "ecj":
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                hasNumberMapping = false;
                hasSymbolMapping = false;
                break;
            case "phonetic":
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                //Jeremy '11,6,18 ETEN 26 has no number mapping
                boolean standardPhonetic = !(mLIMEPref.getPhoneticKeyboardType().equals("eten26")
                        || mLIMEPref.getPhoneticKeyboardType().equals("hsu"));
                hasNumberMapping = standardPhonetic;
                hasSymbolMapping = standardPhonetic;
                break;
            case "ez":
            case "dayi":
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                hasNumberMapping = true;
                hasSymbolMapping = true;
                break;
            case "array10":
                hasNumberMapping = true;
                hasSymbolMapping = false;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
            case "array":
                hasNumberMapping = true; //Jeremy '12,4,28 array 30 actually use number combination keys to enter symbols1

                hasSymbolMapping = true;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
            case "wb":
                hasNumberMapping = false;
                hasSymbolMapping = true;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
            case "hs":
                hasNumberMapping = true;
                hasSymbolMapping = true;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
            case "pinyin":
                hasNumberMapping = true;
                hasSymbolMapping = false;
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
            default:
                mKeyboardSwitcher.setKeyboardMode(activeIM,
                        LIMEKeyboardSwitcher.MODE_TEXT, mImeOptions, true, false, false);
                break;
        }
        //Jeremy '11,9,3 for phone numeric key direct input on chacha
        if (mLIMEPref.getPhysicalKeyboardType().equals("chacha")) hasNumberMapping = false;
        String tablename = activeIM;
        if (tablename.equals("custom") || tablename.equals("phone")) {
            tablename = "custom";
        }
        //Jeremy '11,6,10 pass hasnumbermapping and hassymbolmapping to searchservice for selkey validation.
        if (DEBUG)
            Log.i(TAG, "switchKeyboard() current keyboard:" +
                    tablename + " hasnumbermapping:" + hasNumberMapping + " hasSymbolMapping:" + hasSymbolMapping);
        SearchSrv.setTablename(tablename, hasNumberMapping, hasSymbolMapping);
    }

    private boolean handleSelkey(int primaryCode) {
        if (DEBUG)
            Log.i(TAG, "handleSelKey()");
        // Jeremy '12,4,1 only do selkey on starndard keyboard

        // Check if disable physical key option is open
        if ((disable_physical_selection && hasPhysicalKeyPressed)
                || !mLIMEPref.getPhysicalKeyboardType().equals("normal_keyboard")) {
            return false;
        }

        if (DEBUG) Log.i(TAG, "handleSelkey():primarycode:" + primaryCode);

        int i = -1;
        if (mComposing.length() > 0 && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
            String selkey = "";

            // Jeremy '12,7,5 rewrite the selkey processing
            if (!(disable_physical_selection && hasPhysicalKeyPressed)) {
                try {
                    selkey = SearchSrv.getSelkey();
                } catch (RemoteException ignored) {
                    ignored.printStackTrace();
                }

                String mixedModeSelkey = "`";
                if (hasSymbolMapping && !activeIM.equals("dayi")
                        && !(activeIM.equals("phonetic")
                        && mLIMEPref.getPhoneticKeyboardType().equals("standard"))) {
                    mixedModeSelkey = " ";
                }


                int selkeyOption = mLIMEPref.getSelkeyOption();
                if (selkeyOption == 1) selkey = mixedModeSelkey + selkey;
                else if (selkeyOption == 2) selkey = mixedModeSelkey + " " + selkey;


                i = selkey.indexOf((char) primaryCode);

                //Jeremy '12,7,11 bypass space as first tone for phonetic 
                if (i >= 0 && selkey.substring(i, i + 1).equals(" ")
                        && primaryCode == MY_KEYCODE_SPACE && activeIM.equals("phonetic")
                        //&& mLIMEPref.getParameterBoolean("doLDPhonetic", true) 
                        && !(mComposing.toString().endsWith(" ") || mComposing.length() == 0)) {
                    return false;
                }


            }

            //Jeremy '12,4,29 use mEnglishOnly instead of onIM
        } else if (mEnglishOnly || (mComposing.length() == 0)) {
            // related candidates view
            String relatedSelkey = "!@#$%^&*()";
            i = relatedSelkey.indexOf(primaryCode);
        }


        if (i < 0 || i >= mCandidateList.size()) {
            return false;
        } else {
            pickCandidateManually(i);
            return true;
        }

    }

    /**
     * This method construct candidate view and add key code to composing object
     */
    private void handleCharacter(int primaryCode) {
        //Jeremy '11,6,9 Cleaned code!!
        if (DEBUG)
            Log.i(TAG, "handleCharacter():primaryCode:" + primaryCode
                    + ", metaState = " + mMetaState
                    + ", hasPhysicalKeyPressed = " + hasPhysicalKeyPressed
                    + ", currentSoftKeyboard=" + currentSoftKeyboard);


        //Jeremy '11,6,6 processing physical keyboard selkeys.
        //Move here '11,6,9 to have lower priority than hasnumbermapping
        if (hasPhysicalKeyPressed && (mCandidateView != null && hasCandidatesShown)) { //Replace isCandidateShown() with hasCandidatesShown by Jeremy '12,5,6
            if (handleSelkey(primaryCode)) {
                updateShiftKeyState(getCurrentInputEditorInfo());
                if (DEBUG)
                    Log.i(TAG, "handleCharacter() sel key found return now");
                return;
            }
        }


        if (!mEnglishOnly) {

            InputConnection ic = getCurrentInputConnection();

            if (DEBUG)
                Log.i(TAG, "HandleCharacter():"
                        + " ic != null:" + (ic != null)
                        + " isValidLetter:" + isValidLetter(primaryCode)
                        + " isValidDigit:" + isValidDigit(primaryCode)
                        + " isValidSymbol:" + isValidSymbol(primaryCode)
                        + " hasSymbolMapping:" + hasSymbolMapping
                        + " hasNumberMapping:" + hasNumberMapping
                        + " (primaryCode== MY_KEYCODE_SPACE && keyboardSelection.equals(phonetic):" + (primaryCode == MY_KEYCODE_SPACE && activeIM.equals("phonetic"))
                        + " mEnglishOnly:" + mEnglishOnly);


            if ((!hasSymbolMapping) && (primaryCode == ',' || primaryCode == '.')) { // Chinese , and . processing //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();
            } else if (!hasSymbolMapping && !hasNumberMapping  //Jeremy '11,10.19 fixed to bypass number key in et26 and hsu
                    && (isValidLetter(primaryCode)
                    || (primaryCode == MY_KEYCODE_SPACE && activeIM.equals("phonetic"))) //Jeremy '11,9,6 for et26 and hsu
                    && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                //Log.i(TAG,"handlecharacter(), onIM and no number and no symbol mapping");
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();
            } else if (!hasSymbolMapping
                    && hasNumberMapping
                    && (isValidLetter(primaryCode) || isValidDigit(primaryCode))
                    && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();
            } else if (hasSymbolMapping
                    && !hasNumberMapping
                    && (isValidLetter(primaryCode) || isValidSymbol(primaryCode)
                    || (primaryCode == MY_KEYCODE_SPACE && activeIM.equals("phonetic"))) //Jeremy '11,9,6 for chacha
                    && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();
            } else if (hasSymbolMapping && !hasNumberMapping && activeIM.equals("array")
                    && mComposing != null && mComposing.length() >= 1
                    && getCurrentInputConnection().getTextBeforeCursor(1, 1).charAt(0) == 'w'
                    && Character.isDigit((char) primaryCode)
                    && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                // 27.May.2011 Art : This is the method to check user input type
                // if first previous character is w and second char is number then enable im mode.
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();
            } else if (hasSymbolMapping
                    && hasNumberMapping
                    && (isValidSymbol(primaryCode)
                    || (primaryCode == MY_KEYCODE_SPACE && activeIM.equals("phonetic"))
                    || isValidLetter(primaryCode) || isValidDigit(primaryCode)) && !mEnglishOnly) { //Jeremy '12,4,29 use mEnglishOnly instead of onIM
                mComposing.append((char) primaryCode);
                //InputConnection ic=getCurrentInputConnection();
                if(ic!=null && mPredictionOn)  ic.setComposingText(mComposing, 1);
                updateCandidates();
                //misMatched = mComposing.toString();

            } else {


                pickHighlightedCandidate();  // check here.

                if (ic != null) ic.commitText(String.valueOf((char) primaryCode), 1);
                //Jeremy '12,4,21
                finishComposing();
            }

        } else {
			/*
			 * Handle when user input English Characters
			 */
            if (DEBUG)
                Log.i(TAG, "handleCharacter() english only mode without prediction, committext = "
                        + String.valueOf((char) primaryCode));
            if (isInputViewShown()) {
                if (mInputView.isShifted()) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }
            }

            if (mLIMEPref.getEnglishPrediction() && mPredictionOn && !mKeyboardSwitcher.isSymbols()
                    && (!hasPhysicalKeyPressed || mLIMEPref.getEnglishPredictionOnPhysicalKeyboard())
                    ) {
                if (Character.isLetter((char) primaryCode)) {
                    this.tempEnglishWord.append((char) primaryCode);
                    this.updateEnglishPrediction();
                } else {
                    resetTempEnglishWord();
                    this.updateEnglishPrediction();
                }

            }

            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }

        if (!(!hasPhysicalKeyPressed && hasDistinctMultitouch))
            updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleClose() {
        if (DEBUG) Log.i(TAG, "handleClose()");
        // cancel candidate view if it's shown

        //Jeremy '12,4,23 need to check here.
        finishComposing();

        requestHideSelf(0);
        mInputView.closing();
    }

    private void checkToggleCapsLock() {

        if (mInputView.getKeyboard().isShifted()) {
            toggleCapsLock();
        }

    }

    private void toggleCapsLock() {
        mCapsLock = !mCapsLock;
        if (mKeyboardSwitcher.isAlphabetMode()) {
            ((LIMEKeyboard) mInputView.getKeyboard()).setShiftLocked(mCapsLock);
        } else {
            if (mCapsLock) {
                if (DEBUG) {
                    Log.i(TAG, "toggleCapsLock():mCapsLock:true");
                }
                if (!mKeyboardSwitcher.isShifted())
                    mKeyboardSwitcher.toggleShift();
                ((LIMEKeyboard) mInputView.getKeyboard()).setShiftLocked(true);
            } else {
                if (DEBUG) {
                    Log.i(TAG, "toggleCapsLock():mCapsLock:false");
                }
                ((LIMEKeyboard) mInputView.getKeyboard()).setShiftLocked(false);
                if (mKeyboardSwitcher.isShifted())
                    mKeyboardSwitcher.toggleShift();


            }
        }
    }

    /*
        public boolean isWordSeparator(int code) {
            //Jeremy '11,5,31
            String separators = getResources().getString(R.string.word_separators);
            return separators.contains(String.valueOf((char) code));

        }
    */
    //Jeremy '12,5,11 add return value from mCandidate.takeselectedsuggestion()
    public boolean pickHighlightedCandidate() {
        return mCandidateView != null && mCandidateView.takeSelectedSuggestion();
    }

    public void requestFullRecords(boolean isRelatedPhrase) {
        if (DEBUG)
            Log.i(TAG, "requestFullRecords()");

        if (isRelatedPhrase)
            this.updateRelatedPhrase(true);
        else
            this.updateCandidates(true);

    }

    public void pickCandidateManually(int index) {
        if (DEBUG)
            Log.i(TAG, "pickCandidateManually():"
                    + "Pick up candidate at index : " + index);

        // This is to prevent if user select the index more than the list
        if (mCandidateList != null && index >= mCandidateList.size()) {
            return;
        }


        if (mCandidateList != null && mCandidateList.size() > 0) {
            selectedCandidate = mCandidateList.get(index);
            //selectedIndex = index;
        }

        InputConnection ic = getCurrentInputConnection();

        if (mCompletionOn && mCompletions != null && index >= 0
                && selectedCandidate.isPartialMatchToCodeRecord()
                && index < mCompletions.length) {  // user picked the completion suggestion item.
            CompletionInfo ci = mCompletions[index];
            if (ic != null) ic.commitCompletion(ci);
            if (DEBUG)
                Log.i(TAG, "pickSuggestionManually():mCompletionOn:" + mCompletionOn);

        } else if ((mComposing.length() > 0 || (selectedCandidate != null && !selectedCandidate.isComposingCodeRecord()))
                && !mEnglishOnly) {  // user picked candidates from composing candidate or related phrase candidates
            //Jeremy '12,4,29 use mEnglishOnly instead of onIM
            commitTyped(ic);
        } else if (mLIMEPref.getEnglishPrediction() && tempEnglishList != null
                && tempEnglishList.size() > 0) {  // user picked English prediction suggestions


            //Log.i("EMOJI-commit-index:", index + "");
            //Log.i("EMOJI-commit:", tempEnglishList.size() + "");

            if(this.tempEnglishList.get(index).isEmojiRecord()){
                if (ic != null) ic.commitText(
                        this.tempEnglishList.get(index).getWord() + " ", 0);
            }else{
                if (ic != null) ic.commitText(
                        this.tempEnglishList.get(index).getWord()
                                .substring(tempEnglishWord.length())
                                + " ", 0);
            }

            resetTempEnglishWord();

            clearSuggestions();

        }

        if (currentSoftKeyboard.contains("wb")) {
            if(ic!=null && mPredictionOn)   ic.setComposingText("", 0);
        }

    }


    public void swipeRight() {
        pickHighlightedCandidate();
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
        handleOptions();
    }

    /**
     * First method to call after key press
     */
    public void onPress(int primaryCode) {
        if (DEBUG) Log.i(TAG, "onPress(): code = " + primaryCode);

        hasPhysicalKeyPressed = false;

        if (hasDistinctMultitouch && primaryCode == LIMEBaseKeyboard.KEYCODE_SHIFT) {
            hasShiftPress = true;
            hasShiftCombineKeyPressed = false;
            handleShift();
        } else if (hasDistinctMultitouch && hasShiftPress) {
            hasShiftCombineKeyPressed = true;
        }
        doVibrateSound(primaryCode);
    }

    public void doVibrateSound(int primaryCode) {
        if (DEBUG) Log.i(TAG, "doVibrateSound()");
        if (hasVibration) {
            mVibrator.vibrate(mLIMEPref.getVibrateLevel());
        }
        if (hasSound) {
            int sound = AudioManager.FX_KEYPRESS_STANDARD;
            switch (primaryCode) {
                case LIMEBaseKeyboard.KEYCODE_DELETE:
                    sound = AudioManager.FX_KEYPRESS_DELETE;
                    break;
                case MY_KEYCODE_ENTER:
                    sound = AudioManager.FX_KEYPRESS_RETURN;
                    break;
                case MY_KEYCODE_SPACE:
                    sound = AudioManager.FX_KEYPRESS_SPACEBAR;
                    break;
            }
            float FX_VOLUME = 1.0f;
            mAudioManager.playSoundEffect(sound, FX_VOLUME);
        }
    }

    /**
     * Last method to execute when key release
     */
    public void onRelease(int primaryCode) {
        if (DEBUG)
            Log.i(TAG, "onRelease(): code = " + primaryCode);
        if (hasDistinctMultitouch && primaryCode == LIMEBaseKeyboard.KEYCODE_SHIFT) {
            hasShiftPress = false;
            if (hasShiftCombineKeyPressed) {
                hasShiftCombineKeyPressed = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }
        } else if (hasDistinctMultitouch && !hasShiftPress) {
            updateShiftKeyState(getCurrentInputEditorInfo());

        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.i(TAG, "onDestroy()");

        //jeremy 12,4,21 need to check again---
        //clearComposing(true); see no need to do this '12,4,21
        super.onDestroy();

    }

    @Override
    public void onCancel() {
        if (DEBUG)
            Log.i(TAG, "onCancel()");
        //clearComposing();  Jeremy '12,4,10 avoid clearcomposing when user slide outside the candidate area

    }

    //jeremy '11,9, 5 hideCanddiate when inputView is closed
    @Override
    public void updateInputViewShown() {
        if (mInputView == null) return;
        if (DEBUG)
            Log.i(TAG, "updateInputViewShown(): mInputView.isShown(): " + mInputView.isShown());
        super.updateInputViewShown();
        if (!mInputView.isShown() && !hasPhysicalKeyPressed)
            hideCandidateView();
    }


    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (DEBUG)
            Log.i(TAG, "onFinishInputView()");
        super.onFinishInputView(finishingInput);
        hideCandidateView(); //Jeremy '12,5,7 hideCandiate when inputview is closed but not yet leave the original field (onfinishinput() will not called). 
    }

    /**
     *  start voice input
     */
    public void startVoiceInput() {
        if (DEBUG)
            Log.i(TAG, "startVoiceInput()");

        String voiceID = LIMEUtilities.isVoiceSearchServiceExist(getBaseContext());
        if (voiceID != null)
            this.switchInputMethod(voiceID);


    }

    private static class KeyboardTheme {
        final String mName;
        final int mThemeId;
        final int mStyleId;
        KeyboardTheme(String name, int themeId, int styleId) {
            mName = name;
            mThemeId = themeId;
            mStyleId = styleId;
        }
    }
    private static final KeyboardTheme[] KEYBOARD_THEMES = {
            new KeyboardTheme("Light",  0, R.style.LIMETheme_Light),
            new KeyboardTheme("Dark",   1, R.style.LIMETheme_Dark),
            new KeyboardTheme("Pink",   2, R.style.LIMETheme_Pink),
            new KeyboardTheme("TechBlue",   3, R.style.LIMETheme_TechBlue),
            new KeyboardTheme("FashionPurple",   4, R.style.LIMETheme_FashionPurple),
            new KeyboardTheme("RelaxGreen",   5, R.style.LIMETheme_RelaxGreen),
    };

    private int mKeyboardThemeIndex = -1;

    private int getKeyboardTheme(){
        return KEYBOARD_THEMES[mKeyboardThemeIndex].mStyleId;
    }

}
