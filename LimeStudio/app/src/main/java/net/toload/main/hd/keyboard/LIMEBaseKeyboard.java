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

/**
 * Derived from Gingerbread inputmethodservice Keyboard.java.
 * Add mKeySizeScale to scale keyboard in vertical direction (height and gap).
 * Jeremy '11,9,4
 */


package net.toload.main.hd.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;

import net.toload.main.hd.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Loads an XML description of a keyboard and stores the attributes of the keys. A keyboard
 * consists of rows of keys.
 * <p>The layout file for a keyboard contains XML that looks like the following snippet:</p>
 * <pre>
 * &lt;Keyboard
 *         android:keyWidth="%10p"
 *         android:keyHeight="50px"
 *         android:horizontalGap="2px"
 *         android:verticalGap="2px" &gt;
 *     &lt;Row android:keyWidth="32px" &gt;
 *         &lt;Key android:keyLabel="A" /&gt;
 *         ...
 *     &lt;/Row&gt;
 *     ...
 * &lt;/Keyboard&gt;
 * </pre>
 *
 * @attr ref R.styleable#Keyboard_keyWidth
 * @attr ref R.styleable#Keyboard_keyHeight
 * @attr ref R.styleable#Keyboard_horizontalGap
 * @attr ref R.styleable#Keyboard_verticalGap
 */
public class LIMEBaseKeyboard {

    static final String TAG = "LIMEBaseKeyboard";
    private static final boolean DEBUG = false;

    // Keyboard XML Tags

    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";

    public static final int EDGE_LEFT = 0x01;
    public static final int EDGE_RIGHT = 0x02;
    public static final int EDGE_TOP = 0x04;
    public static final int EDGE_BOTTOM = 0x08;

    public static final int KEYCODE_SHIFT = -1;
    public static final int KEYCODE_MODE_CHANGE = -2;
    public static final int KEYCODE_DONE = -3;

    public static final int KEYCODE_DELETE = -5;
    public static final int KEYCODE_ALT = -6;
    public static final int KEYCODE_UP = -11;
    public static final int KEYCODE_DOWN = -12;
    public static final int KEYCODE_LEFT = -13;
    public static final int KEYCODE_RIGHT = -14;
    //Jeremy '12,5,26 moved from LIMEKeyboard
    public static final int KEYCODE_ENTER = '\n';
    public static final int KEYCODE_SPACE = ' ';
    //Jeremy '12,6,19
    public static final int SPLIT_KEYBOARD_NEVER = 0;
    public static final int SPLIT_KEYBOARD_ALWAYS = 1;
    public static final int SPLIT_KEYBOARD_LANDSCAPD_ONLY = 2;

    /**
     * Drawable for arrow keys
     */

    private static Drawable mDrawableArrowUp;
    private static Drawable mDrawableArrowDown;
    private static Drawable mDrawableArrowRight;
    private static Drawable mDrawableArrowLeft;




    /**
     * orientation of the screen
     */
    private boolean mLandScape;
    /** Keyboard label **/
    //private CharSequence mLabel;

    /**
     * Horizontal gap default for all rows
     */
    private int mDefaultHorizontalGap;

    /**
     * Default key width
     */
    private int mDefaultWidth;

    /**
     * Default key height
     */
    private int mDefaultHeight;

    /**
     * KeySizeScale Jeremy '11,9,3
     */
    private static float mKeySizeScale;


    /**
     * Default gap between rows
     */
    private int mDefaultVerticalGap;

    /**
     * Is the keyboard in the shifted state
     */
    private boolean mShifted;

    /**
     * Key instance for the shift key, if present
     */
    private Key mShiftKey;

    /**
     * Key index for the shift key, if present
     */
    private int mShiftKeyIndex = -1;

    /** Current key width, while loading the keyboard */
    //private int mKeyWidth;

    /** Current key height, while loading the keyboard */
    //private int mKeyHeight;

    /**
     * Total height of the keyboard, including the padding and keys
     */
    private int mTotalHeight;

    /**
     * Total width of the keyboard, including left side gaps and keys, but not any gaps on the
     * right side.
     */
    private int mTotalWidth;

    /**
     * List of keys in this keyboard
     */
    private List<Key> mKeys;

    /**
     * List of modifier keys such as Shift & Alt, if any
     */
    private List<Key> mModifierKeys;

    /**
     * Width of the screen available to fit the keyboard
     */
    private int mDisplayWidth;

    /**
     * Height of the screen
     */
    private int mDisplayHeight;

    /**
     * Keyboard mode, or zero, if none.
     */
    private int mKeyboardMode;

    /**
     * Show arrow keys on keyboard or not.
     */ //Add by Jeremy '12,5,21
    protected int mShowArrowKeys;

    /**
     * Show separated keyboard with arrow keys in the middle.
     */ //Add by Jeremy '12,5,26
    protected static boolean mSplitKeyboard;

    /**
     * Reserved space in the middle in unit of columns for separated keyboard in landscape mode.
     */
    protected static int mReservedColumnsForSplitedKeyboard = 2;

    /**
     * Key width reduction scale for separated keyboard in landscape mode.
     */
    protected static float mSplitedKeyWidthScale = 1f;

    /**
     * Key width for separated keyboard in landscape mode.
     */
    protected int mSplitKeyWidth = mDefaultWidth;
    /**
     * Default key number in a row .
     */
    int mKeysInRow = 10;

    // Variables for pre-computing nearest keys.

    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 5;
    private static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;
    private int mCellWidth;
    private int mCellHeight;
    private int[][] mGridNeighbors;
    private int mProximityThreshold;
    /**
     * Number of key widths from current touch point to search for nearest keys.
     */
    private static float SEARCH_DISTANCE = 1.8f;



    /**
     * Container for keys in the keyboard. All keys in a row are at the same Y-coordinate.
     * Some of the key size defaults can be overridden per row from what the {@link LIMEBaseKeyboard}
     * defines.
     *
     * @attr ref R.styleable#Keyboard_keyWidth
     * @attr ref R.styleable#Keyboard_keyHeight
     * @attr ref R.styleable#Keyboard_horizontalGap
     * @attr ref R.styleable#Keyboard_verticalGap
     * @attr ref R.styleable#Keyboard_Row_rowEdgeFlags
     * @attr ref R.styleable#Keyboard_Row_keyboardMode
     */
    public static class Row {
        /**
         * Default width of a key in this row.
         */
        public int defaultWidth;
        /**
         * Default height of a key in this row.
         */
        public int defaultHeight;
        /**
         * Default horizontal gap between keys in this row.
         */
        public int defaultHorizontalGap;
        /**
         * Vertical gap following this row.
         */
        public int verticalGap;
        /**
         * Edge flags for this row of keys. Possible values that can be assigned are
         * {@link LIMEBaseKeyboard#EDGE_TOP EDGE_TOP} and {@link LIMEBaseKeyboard#EDGE_BOTTOM EDGE_BOTTOM}
         */
        public int rowEdgeFlags;

        /**
         * The keyboard mode for this row
         */
        public int mode;

        private LIMEBaseKeyboard parent;

        public Row(LIMEBaseKeyboard parent) {
            this.parent = parent;
        }

        public Row(Resources res, LIMEBaseKeyboard parent, XmlResourceParser parser) {
            this.parent = parent;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.LIMEBaseKeyboard);
            defaultWidth = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_keyWidth,
                    parent.mDisplayWidth, parent.mDefaultWidth);
            defaultHeight = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_keyHeight, //Jeremy '11,9,4
                    parent.mDisplayHeight, parent.mDefaultHeight, mKeySizeScale);
            defaultHorizontalGap = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_horizontalGap,
                    parent.mDisplayWidth, parent.mDefaultHorizontalGap);
            verticalGap = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_verticalGap, //Jeremy '11,9,4
                    parent.mDisplayHeight, parent.mDefaultVerticalGap, mKeySizeScale);
            a.recycle();
            a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.LIMEBaseKeyboard_Row);
            rowEdgeFlags = a.getInt(R.styleable.LIMEBaseKeyboard_Row_rowEdgeFlags, 0);
            mode = a.getResourceId(R.styleable.LIMEBaseKeyboard_Row_keyboardMode, 0);

        }
    }

    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     *
     * @attr ref R.styleable#Keyboard_keyWidth
     * @attr ref R.styleable#Keyboard_keyHeight
     * @attr ref R.styleable#Keyboard_horizontalGap
     * @attr ref R.styleable#Keyboard_Key_codes
     * @attr ref R.styleable#Keyboard_Key_keyIcon
     * @attr ref R.styleable#Keyboard_Key_keyLabel
     * @attr ref R.styleable#Keyboard_Key_iconPreview
     * @attr ref R.styleable#Keyboard_Key_isSticky
     * @attr ref R.styleable#Keyboard_Key_isRepeatable
     * @attr ref R.styleable#Keyboard_Key_isModifier
     * @attr ref R.styleable#Keyboard_Key_popupKeyboard
     * @attr ref R.styleable#Keyboard_Key_popupCharacters
     * @attr ref R.styleable#Keyboard_Key_keyOutputText
     * @attr ref R.styleable#Keyboard_Key_keyEdgeFlags
     */
    public static class Key {
        /**
         * All the key codes (unicode or custom code) that this key could generate, zero'th
         * being the most important.
         */
        public int[] codes;

        /**
         * Label to display
         */
        public CharSequence label;

        /**
         * Icon to display instead of a label. Icon takes precedence over a label
         */
        public Drawable icon;
        /**
         * Preview version of the icon, for the preview popup
         */
        public Drawable iconPreview;
        /**
         * Width of the key, not including the gap
         */
        public int width;
        /**
         * Height of the key, not including the gap
         */
        public int height;
        /**
         * The horizontal gap before this key
         */
        public int gap;
        /**
         * Whether this key is sticky, i.e., a toggle key
         */
        public boolean sticky;
        /**
         * X coordinate of the key in the keyboard layout
         */
        public int x;
        /**
         * Y coordinate of the key in the keyboard layout
         */
        public int y;
        /**
         * The current pressed state of this key
         */
        public boolean pressed;
        /**
         * If this is a sticky key, is it on?
         */
        public boolean on;
        /**
         * Text to output when pressed. This can be multiple characters, like ".com"
         */
        public CharSequence text;
        /**
         * Popup characters
         */
        public CharSequence popupCharacters;

        /**
         * LabelSizeScale Jeremy '12,6,7
         */
        private static float mLabelSizeScale = 0f;

        public float getLabelSizeScale() {
            if (DEBUG)
                Log.i(TAG, "getLabelSizeScale() "
                        + ", key height = " + height
                        + ", key width = " + width
                        + ", mSplitedKeyWidthScale = " + mSplitedKeyWidthScale
                        + ", keyboard.getKeyHeight = " + keyboard.getKeyHeight()
                        + ", keyboard.getKeyWidth() = " + keyboard.getKeyWidth());
            if (mLabelSizeScale > 0) return mLabelSizeScale;

            //Jeremy '12,6, 7 move from LIMEkeyboardbaseview
            mLabelSizeScale = 1;


            if (width < keyboard.getKeyWidth())  //Jeremy '12,5,26 scaled the label size if the key width is smaller than default key width
                mLabelSizeScale = mSplitKeyboard ? 1f : mSplitedKeyWidthScale;
            //*=  (float)(width) / (float)(keyboard.getKeyWidth());

            return mLabelSizeScale;
        }

        /**
         * Flags that specify the anchoring to edges of the keyboard for detecting touch events
         * that are just out of the boundary of the key. This is a bit mask of
         * {@link LIMEBaseKeyboard#EDGE_LEFT}, {@link LIMEBaseKeyboard#EDGE_RIGHT}, {@link LIMEBaseKeyboard#EDGE_TOP} and
         * {@link LIMEBaseKeyboard#EDGE_BOTTOM}.
         */
        public int edgeFlags;
        /**
         * Whether this is a modifier key, such as Shift or Alt
         */
        public boolean modifier;
        /**
         * The keyboard that this key belongs to
         */
        private LIMEBaseKeyboard keyboard;
        /**
         * If this key pops up a mini keyboard, this is the resource id for the XML layout for that
         * keyboard.
         */
        public int popupResId;
        /**
         * Whether this key repeats itself when held down
         */
        public boolean repeatable;


        private final static int[] KEY_STATE_NORMAL_ON = {
                android.R.attr.state_single,
                android.R.attr.state_checkable,
                android.R.attr.state_checked
        };

        private final static int[] KEY_STATE_PRESSED_ON = {
                android.R.attr.state_single,
                android.R.attr.state_pressed,
                android.R.attr.state_checkable,
                android.R.attr.state_checked
        };

        private final static int[] KEY_STATE_NORMAL_OFF = {
                android.R.attr.state_single,
                android.R.attr.state_checkable
        };

        private final static int[] KEY_STATE_PRESSED_OFF = {
                android.R.attr.state_single,
                android.R.attr.state_pressed,
                android.R.attr.state_checkable
        };

        private final static int[] KEY_STATE_NORMAL = {
        };

        private final static int[] KEY_STATE_PRESSED = {
                android.R.attr.state_pressed
        };
        // moved from LIMEKeybard by Jeremy '12,5,22
        private final int[] KEY_STATE_FUNCTIONAL_NORMAL = {
                android.R.attr.state_single
        };

        // functional pressed state (with properties)
        private final int[] KEY_STATE_FUNCTIONAL_PRESSED = {
                android.R.attr.state_single,
                android.R.attr.state_pressed
        };


        /**
         * Create an empty key with no attributes.
         */
        public Key(Row parent) {
            keyboard = parent.parent;
        }

        /**
         * Clone a key with same attributes
         */
        public Key(Row parent, Key key) {
            keyboard = parent.parent;

            x = key.x;
            y = key.y;

            width = key.width;
            height = key.height;
            gap = key.gap;
            codes = key.codes;
            iconPreview = key.iconPreview;
            popupCharacters = key.popupCharacters;
            popupResId = key.popupResId;
            repeatable = key.repeatable;
            modifier = key.modifier;
            sticky = key.sticky;
            edgeFlags = key.edgeFlags;
            icon = key.icon;
            label = key.label;
            text = key.text;
            if (iconPreview != null) {
                iconPreview.setBounds(0, 0, iconPreview.getIntrinsicWidth(),
                        iconPreview.getIntrinsicHeight());
            }
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            if (codes == null && !TextUtils.isEmpty(label)) {
                codes = new int[]{label.charAt(0)};
            }
        }

        /**
         * Create a key with the given top-left coordinate and extract its attributes from
         * the XML parser.
         *
         * @param res    resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to
         *               a {@link LIMEBaseKeyboard}.
         * @param x      the x coordinate of the top-left
         * @param y      the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
        public Key(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            this(parent);

            this.x = x;
            this.y = y;

            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.LIMEBaseKeyboard);

            float keyWidthScale = 1f;
            if (mSplitKeyboard)
                keyWidthScale = mSplitedKeyWidthScale;
            if (DEBUG)
                Log.i(TAG, "Key(): key.mSeperatedKeyboard = " + mSplitKeyboard + ". keyWidthScale = " + keyWidthScale);


            width = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_keyWidth,
                    keyboard.mDisplayWidth, Math.round((float) (parent.defaultWidth * keyWidthScale))
                    , keyWidthScale); //Jeremy '12,5,26

            height = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_keyHeight,
                    keyboard.mDisplayHeight, parent.defaultHeight, mKeySizeScale); //Jeremy '11,9,3


            gap = getDimensionOrFraction(a,
                    R.styleable.LIMEBaseKeyboard_horizontalGap,
                    keyboard.mDisplayWidth, parent.defaultHorizontalGap);
            a.recycle();
            a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.LIMEBaseKeyboard_Key);
            this.x += gap;
            TypedValue codesValue = new TypedValue();
            a.getValue(R.styleable.LIMEBaseKeyboard_Key_codes,
                    codesValue);
            if (codesValue.type == TypedValue.TYPE_INT_DEC
                    || codesValue.type == TypedValue.TYPE_INT_HEX) {
                codes = new int[]{codesValue.data};
            } else if (codesValue.type == TypedValue.TYPE_STRING) {
                codes = parseCSV(codesValue.string.toString());
            }

            iconPreview = a.getDrawable(R.styleable.LIMEBaseKeyboard_Key_iconPreview);
            if (iconPreview != null) {
                iconPreview.setBounds(0, 0, iconPreview.getIntrinsicWidth(),
                        iconPreview.getIntrinsicHeight());
            }
            popupCharacters = a.getText(
                    R.styleable.LIMEBaseKeyboard_Key_popupCharacters);
            popupResId = a.getResourceId(
                    R.styleable.LIMEBaseKeyboard_Key_popupKeyboard, 0);
            repeatable = a.getBoolean(
                    R.styleable.LIMEBaseKeyboard_Key_isRepeatable, false);
            modifier = a.getBoolean(
                    R.styleable.LIMEBaseKeyboard_Key_isModifier, false);
            sticky = a.getBoolean(
                    R.styleable.LIMEBaseKeyboard_Key_isSticky, false);
            edgeFlags = a.getInt(R.styleable.LIMEBaseKeyboard_Key_keyEdgeFlags, 0);
            edgeFlags |= parent.rowEdgeFlags;

            icon = a.getDrawable(
                    R.styleable.LIMEBaseKeyboard_Key_keyIcon);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            label = a.getText(R.styleable.LIMEBaseKeyboard_Key_keyLabel);
            text = a.getText(R.styleable.LIMEBaseKeyboard_Key_keyOutputText);

            if (codes == null && !TextUtils.isEmpty(label)) {
                codes = new int[]{label.charAt(0)};
            }
            a.recycle();
        }

        /**
         * Informs the key that it has been pressed, in case it needs to change its appearance or
         * state.
         *
         * @see #onReleased(boolean)
         */
        public void onPressed() {
            pressed = !pressed;
        }

        /**
         * Changes the pressed state of the key. If it is a sticky key, it will also change the
         * toggled state of the key if the finger was release inside.
         *
         * @param inside whether the finger was released inside the key
         * @see #onPressed()
         */
        public void onReleased(boolean inside) {
            pressed = !pressed;
            if (sticky) {
                on = !on;
            }
        }

        protected boolean isFunctionalKey() {
            return modifier;
        }

        int[] parseCSV(String value) {
            int count = 0;
            int lastIndex = 0;
            if (value.length() > 0) {
                count++;
                while ((lastIndex = value.indexOf(",", lastIndex + 1)) > 0) {
                    count++;
                }
            }
            int[] values = new int[count];
            count = 0;
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
                try {
                    values[count++] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Error parsing keycodes " + value);
                }
            }
            return values;
        }

        /**
         * Detects if a point falls inside this key.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key. If the key is attached to an edge,
         * it will assume that all points between the key and the edge are considered to be inside
         * the key.
         */
        public boolean isInside(int x, int y) {
            boolean leftEdge = (edgeFlags & EDGE_LEFT) > 0;
            boolean rightEdge = (edgeFlags & EDGE_RIGHT) > 0;
            boolean topEdge = (edgeFlags & EDGE_TOP) > 0;
            boolean bottomEdge = (edgeFlags & EDGE_BOTTOM) > 0;
            if ((x >= this.x || (leftEdge && x <= this.x + this.width))
                    && (x < this.x + this.width || (rightEdge && x >= this.x))
                    && (y >= this.y || (topEdge && y <= this.y + this.height))
                    && (y < this.y + this.height || (bottomEdge && y >= this.y))) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the square of the distance between the center of the key and the given point.
         *
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return the square of the distance of the point from the center of the key
         */
        public int squaredDistanceFrom(int x, int y) {
            int xDist = this.x + width / 2 - x;
            int yDist = this.y + height / 2 - y;
            return xDist * xDist + yDist * yDist;
        }

        /**
         * Returns the drawable state for the key, based on the current state and type of the key.
         *
         * @return the drawable state of the key.
         * @see android.graphics.drawable.StateListDrawable#setState(int[])
         */
        public int[] getCurrentDrawableState() {
            int[] states = KEY_STATE_NORMAL;
            if (sticky) {
                if (on) {
                    if (pressed) {
                        states = KEY_STATE_PRESSED_ON;
                    } else {
                        states = KEY_STATE_NORMAL_ON;
                    }
                } else {
                    if (pressed) {
                        states = KEY_STATE_PRESSED_OFF;
                    } else {
                        states = KEY_STATE_NORMAL_OFF;
                    }
                }


            } else if (isFunctionalKey()) {
                if (pressed) {
                    states = KEY_STATE_FUNCTIONAL_PRESSED;
                } else {
                    states = KEY_STATE_FUNCTIONAL_NORMAL;
                }
            } else {
                if (pressed) {
                    states = KEY_STATE_PRESSED;
                } else {
                    states = KEY_STATE_NORMAL;
                }
            }
            return states;
        }
    }

    /**
     * Creates a keyboard from the given xml key layout file.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     */
    public LIMEBaseKeyboard(Context context, int xmlLayoutResId, float keySizeScale, int showArrowKeys, int splitKeyboard) {
        this(context, xmlLayoutResId, 0, keySizeScale, showArrowKeys, splitKeyboard);
    }

    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param modeId         keyboard mode identifier
     */
    public LIMEBaseKeyboard(Context context, int xmlLayoutResId, int modeId, float keySizeScale, int showArrowKeys, int splitKeyboard) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;

        if (DEBUG)
            Log.i(TAG, "LIMEBaseKeyboard() mDisplayWidth = " + mDisplayWidth + ". mDisplayHeight" + mDisplayHeight);


        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = mDefaultWidth;
        mKeys = new ArrayList<Key>();
        mModifierKeys = new ArrayList<Key>();
        mKeyboardMode = modeId;
        mKeySizeScale = keySizeScale;
        mShowArrowKeys = showArrowKeys;

        mLandScape = mDisplayWidth > mDisplayHeight;

        TypedArray a = context.getTheme().obtainStyledAttributes(//R.style.LIMEBaseKeyboardLight, R.styleable.LIMEBaseKeyboard);
                null, R.styleable.LIMEBaseKeyboard, R.attr.LIMEBaseKeyboardStyle, R.style.LIMEBaseKeyboard);

        mDrawableArrowUp = a.getDrawable(R.styleable.LIMEBaseKeyboard_drawableArrowUp);
        mDrawableArrowDown = a.getDrawable(R.styleable.LIMEBaseKeyboard_drawableArrowDown);
        mDrawableArrowLeft = a.getDrawable(R.styleable.LIMEBaseKeyboard_drawableArrowLeft);
        mDrawableArrowRight = a.getDrawable((R.styleable.LIMEBaseKeyboard_drawableArrowRight));


        //Jeremy '12,5,26 reserve  columns in the middle for arrow keys in landscape mode.
        //Jeremy '12,5,27 read splitkeyboard setting from preference. 
        //Jeremy '12,6,19  add orientation consideration on split keyboard
        mSplitKeyboard = (mLandScape && mShowArrowKeys != 0)
                || (mLandScape && splitKeyboard == SPLIT_KEYBOARD_LANDSCAPD_ONLY)
                || splitKeyboard == SPLIT_KEYBOARD_ALWAYS;

        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }

    /**
     * <p>Creates a blank keyboard from the given resource file and populates it with the specified
     * characters in left-to-right, top-to-bottom fashion, using the specified number of columns.
     * </p>
     * <p>If the specified number of columns is -1, then the keyboard will fit as many keys as
     * possible in each row.</p>
     *
     * @param context             the application or service context
     * @param layoutTemplateResId the layout template file, containing no keys.
     * @param characters          the list of characters to display on the keyboard. One key will be created
     *                            for each character.
     * @param columns             the number of columns of keys to display. If this number is greater than the
     *                            number of keys that can fit in a row, it will be ignored. If this number is -1, the
     *                            keyboard will fit as many keys as possible in each row.
     */
    public LIMEBaseKeyboard(Context context, int layoutTemplateResId,
                            CharSequence characters, int columns, int horizontalPadding, float keySizeScale) {
        this(context, layoutTemplateResId, keySizeScale, 0, 0); //Jeremy '12,5,21 never show arrow keys in popup keyboard
        int x = 0;
        int y = 0;
        int column = 0;
        mTotalWidth = 0;


        Row row = new Row(this);
        row.defaultHeight = (int) (mDefaultHeight * mKeySizeScale);
        row.defaultWidth = mDefaultWidth;
        row.defaultHorizontalGap = mDefaultHorizontalGap;
        row.verticalGap = (int) (mDefaultVerticalGap * mKeySizeScale);
        ;
        row.rowEdgeFlags = EDGE_TOP | EDGE_BOTTOM;
        final int maxColumns = columns == -1 ? Integer.MAX_VALUE : columns;

        CharSequence labels = null;
        if (characters.toString().contains("\n")) {
            String[] charactersAndLabel = characters.toString().split("\n");
            characters = new String(charactersAndLabel[0]);
            labels = new String(charactersAndLabel[1]);
        }

        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);
            if (column >= maxColumns
                    || x + mDefaultWidth + horizontalPadding > mDisplayWidth) {
                x = 0;
                y += mDefaultVerticalGap + mDefaultHeight;
                column = 0;
            }
            final Key key = new Key(row);
            key.x = x;
            key.y = y;
            key.width = mDefaultWidth;
            key.height = (int) (mDefaultHeight * mKeySizeScale);
            key.gap = mDefaultHorizontalGap;
            if (labels == null) //Jeremy '12,5,21 add keylabels in popupcharacters seperated as \n. The format is "123\nABC" ABC are keylabels for 123.
                key.label = String.valueOf(c);
            else
                key.label = String.valueOf(c) + "\n" + String.valueOf(labels.charAt(i));
            key.codes = new int[]{c};
            column++;
            x += key.width + key.gap;
            mKeys.add(key);
            if (x > mTotalWidth) {
                mTotalWidth = x;
            }
        }
        mTotalHeight = y + row.defaultHeight;//mDefaultHeight;



    }

    public List<Key> getKeys() {
        return mKeys;
    }

    public List<Key> getModifierKeys() {
        return mModifierKeys;
    }

    protected int getHorizontalGap() {
        return mDefaultHorizontalGap;
    }

    protected void setHorizontalGap(int gap) {
        mDefaultHorizontalGap = gap;
    }

    protected int getVerticalGap() {
        return mDefaultVerticalGap;
    }

    public float getKeySizeScale() {
        return mKeySizeScale;
    }

    public void setKeySizeScale(float mKeySizeScale) {
        LIMEBaseKeyboard.mKeySizeScale = mKeySizeScale;
    }

    protected void setVerticalGap(int gap) {
        mDefaultVerticalGap = gap;
    }

    protected int getKeyHeight() {
        return mDefaultHeight;
    }

    protected void setKeyHeight(int height) {
        mDefaultHeight = height;
    }

    protected int getKeyWidth() {
        return mDefaultWidth;
    }

    protected void setKeyWidth(int width) {
        mDefaultWidth = width;
    }

    /**
     * Returns the total height of the keyboard
     *
     * @return the total height of the keyboard
     */
    public int getHeight() {
        return mTotalHeight;
    }

    public int getMinWidth() {
        return mTotalWidth;
    }

    public boolean setShifted(boolean shiftState) {
        if (mShiftKey != null) {
            mShiftKey.on = shiftState;
        }
        if (mShifted != shiftState) {
            mShifted = shiftState;
            return true;
        }
        return false;
    }

    public boolean isShifted() {
        return mShifted;
    }

    public int getShiftKeyIndex() {
        return mShiftKeyIndex;
    }

    private void computeNearestNeighbors() {
        // Round-up so we don't have any pixels outside the grid
        mCellWidth = (getMinWidth() + GRID_WIDTH - 1) / GRID_WIDTH;
        mCellHeight = (getHeight() + GRID_HEIGHT - 1) / GRID_HEIGHT;
        mGridNeighbors = new int[GRID_SIZE][];
        int[] indices = new int[mKeys.size()];
        final int gridWidth = GRID_WIDTH * mCellWidth;
        final int gridHeight = GRID_HEIGHT * mCellHeight;
        for (int x = 0; x < gridWidth; x += mCellWidth) {
            for (int y = 0; y < gridHeight; y += mCellHeight) {
                int count = 0;
                for (int i = 0; i < mKeys.size(); i++) {
                    final Key key = mKeys.get(i);
                    if (key.squaredDistanceFrom(x, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y) < mProximityThreshold ||
                            key.squaredDistanceFrom(x + mCellWidth - 1, y + mCellHeight - 1)
                                    < mProximityThreshold ||
                            key.squaredDistanceFrom(x, y + mCellHeight - 1) < mProximityThreshold) {
                        indices[count++] = i;
                    }
                }
                int[] cell = new int[count];
                System.arraycopy(indices, 0, cell, 0, count);
                mGridNeighbors[(y / mCellHeight) * GRID_WIDTH + (x / mCellWidth)] = cell;
            }
        }
    }

    /**
     * Returns the indices of the keys that are closest to the given point.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the array of integer indices for the nearest keys to the given point. If the given
     * point is out of range, then an array of size zero is returned.
     */
    public int[] getNearestKeys(int x, int y) {
        if (mGridNeighbors == null) computeNearestNeighbors();
        if (x >= 0 && x < getMinWidth() && y >= 0 && y < getHeight()) {
            int index = (y / mCellHeight) * GRID_WIDTH + (x / mCellWidth);
            if (index < GRID_SIZE) {
                return mGridNeighbors[index];
            }
        }
        return new int[0];
    }

    protected Row createRowFromXml(Resources res, XmlResourceParser parser) {
        return new Row(res, this, parser);
    }

    protected Key createKeyFromXml(Context context, Row parent, int x, int y,
                                   XmlResourceParser parser) {
       return  new Key(context.getResources(), parent, x, y, parser);

    }

    /**
     * createArrowKeysRow() returns the total height of the row.
     */
    final float ARROW_KEY_HEIGHT_FRACTION = 0.8f;

    protected int createArrowKeys(Resources res, int x, int y, boolean verticalLayout) {
        if (DEBUG)
            Log.i(TAG, "createArrowKeys(): mDisplayWidth = " + mDisplayWidth);

        Row row = new Row(this);

        row.verticalGap = (int) (mDefaultVerticalGap * mKeySizeScale);
        ;
        row.defaultHorizontalGap = mDefaultHorizontalGap;
        if (verticalLayout) {
            row.defaultHeight = (int) (mTotalHeight - 3 * row.verticalGap) / 4;
            row.defaultWidth = mSplitKeyWidth;
        } else {
            row.defaultHeight = (int) (mDefaultHeight * mKeySizeScale * ARROW_KEY_HEIGHT_FRACTION);
            row.defaultWidth = Math.round((mDisplayWidth - 3 * mDefaultHorizontalGap) / 4);
            row.rowEdgeFlags = EDGE_TOP | EDGE_BOTTOM;
        }


        // Many special symbols : http://star.gg/special-symbols

        for (int i = 0; i < 4; i++) {

            final Key key = new Key(row);
            key.x = x;
            key.y = y;
            key.width = row.defaultWidth;
            key.height = row.defaultHeight;
            key.gap = row.defaultHorizontalGap;
            key.modifier = true;


            // Cross shape arrow keys layout if center reserved space is larger than 2 , Jeremy '12,5,28
            if (verticalLayout && mReservedColumnsForSplitedKeyboard > 2) {
                switch (i) {
                    case 0:

                        key.icon = mDrawableArrowUp;
                        key.codes = new int[]{KEYCODE_UP};
                        y += key.height + row.verticalGap;
                        x -= key.width / 2 + key.gap;
                        break;
                    case 1:

                        key.icon = mDrawableArrowLeft;
                        key.codes = new int[]{KEYCODE_LEFT};
                        x += key.width + key.gap * 2;
                        break;
                    case 2:

                        key.icon = mDrawableArrowRight;
                        key.codes = new int[]{KEYCODE_RIGHT};
                        x -= key.width / 2 + key.gap;
                        y += key.height + row.verticalGap;
                        break;
                    case 3:

                        key.icon = mDrawableArrowDown;
                        key.codes = new int[]{KEYCODE_DOWN};
                        y += key.height + row.verticalGap;
                        break;
                }

            } else {
                switch (i) {
                    case 0:

                        key.icon = mDrawableArrowUp;
                        key.codes = new int[]{KEYCODE_UP};
                        break;
                    case 1:

                        key.icon = mDrawableArrowDown;
                        key.codes = new int[]{KEYCODE_DOWN};
                        break;
                    case 2:

                        key.icon = mDrawableArrowLeft;
                        key.codes = new int[]{KEYCODE_LEFT};
                        break;
                    case 3:

                        key.icon = mDrawableArrowRight;
                        key.codes = new int[]{KEYCODE_RIGHT};
                        break;
                }
                if (verticalLayout)
                    y += key.height + row.verticalGap;
                else
                    x += key.width + key.gap;
            }


            if (DEBUG)
                Log.i(TAG, "createArrowKeysRow(): key[" + i + "]" + "; x = " + x);

            mKeys.add(key);
            //mModifierKeys.add(key);
            if (x > mTotalWidth) {
                mTotalWidth = x;
            }
        }


        return (row.defaultHeight + row.verticalGap);  //return the row total height

    }


    private void loadKeyboard(Context context, XmlResourceParser parser) {
        boolean inKey = false;
        boolean inRow = false;
        //boolean leftMostKey = false;

        //int row = 0;
        int x = 0;
        int y = 0;
        Key key = null;
        Row currentRow = null;
        Resources res = context.getResources();
        boolean skipRow = false;

        /** Show arrow keys on top of the soft keyboard in portrait mode.*/
        boolean showArrowKeysOnTop = (mShowArrowKeys == 1) && (mDisplayWidth < mDisplayHeight);
        /** Show arrow keys on bottom of the soft keyboard in portrait mode.*/
        boolean showArrowKeysOnBottom = (mShowArrowKeys == 2) && (mDisplayWidth < mDisplayHeight);
        /** The left bound of the center blank area on split keyboard. */
        int leftSplitBorder = 0;
        /** The distance to be shifted for right side keyboard */
        int splitDistance = 0;
        /** The centerLine of current screen in horizontal direction. */
        int centerLine = mDisplayWidth / 2;
        /** Reserved center space for arrow keys on right or left of the center line. */
        int reservedCenterSpace = 0;

        try {
            int event;


            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
                        inRow = true;
                        x = 0;

                        currentRow = createRowFromXml(res, parser);
                        skipRow = currentRow.mode != 0 && currentRow.mode != mKeyboardMode;
                        if (skipRow) {
                            skipToEndOfRow(parser);
                            inRow = false;
                        }
                    } else if (TAG_KEY.equals(tag)) {

                        inKey = true;
                        key = createKeyFromXml(context, currentRow, x, y, parser);
                        mKeys.add(key);

                        //Jeremy '12,5,26 shift the keys after separated threshold and 
                        // repeat space keys or keys longer then the distance between space key and half of screen  on right keyboard.
                        if (DEBUG)
                            Log.i(TAG, "loadkeyboard():"
                                            + ". key.x = " + key.x
                                            + ". key.width = " + key.width
                            );


                        if (mSplitKeyboard && leftSplitBorder > 0
                                && ((key.x >= leftSplitBorder && key.x < centerLine) //key left bound in between split border and centerline
                                || (key.x < leftSplitBorder  //the key right bound is too closed to centerline so as no enough clearance for center arrow keys
                                && key.x + key.width >= centerLine - reservedCenterSpace
                                && key.x + splitDistance > centerLine + reservedCenterSpace
                        ))) {
                            key.x += splitDistance;
                            x += splitDistance;
                            if (DEBUG)
                                Log.i(TAG, "loadkeyboard(): shitfing"
                                        + ". key.x = " + key.x
                                        + ". key.width = " + key.width
                                        + ". x = " + x
                                        + ". y = " + y);


                        } else if (mSplitKeyboard && leftSplitBorder > 0
                                && ((key.codes[0] == KEYCODE_SPACE && key.x < leftSplitBorder)
                                || (key.x < leftSplitBorder && key.x + key.width > centerLine))

                                ) {
                            int keyRightBound = key.x + key.width + splitDistance;

                            if (DEBUG)
                                Log.i(TAG, "loadkeyboard() split keys,  keyRightBound = " + keyRightBound
                                        + ". key.x = " + key.x
                                        + ". key.width = " + key.width
                                        + ". x = " + x
                                        + ". y = " + y);

                            // add space key in right side split keyboard Jeremy '12,5,26
                            if (keyRightBound > centerLine + key.gap + mSplitKeyWidth + reservedCenterSpace) {
                                key.width = centerLine - key.x - key.gap - reservedCenterSpace;
                                final Key rightKey = new Key(currentRow, key); //clone the space key for the space key on right keyboard.
                                rightKey.x = centerLine + key.gap + reservedCenterSpace;
                                rightKey.width = keyRightBound - rightKey.x;
                                mKeys.add(rightKey);
                                x += rightKey.gap * 2 + rightKey.width + reservedCenterSpace * 2; //shift x for the distance on center reserved space + right key width
                            }

                        }


                        if (key.codes[0] == KEYCODE_SHIFT) {
                            mShiftKey = key;
                            mShiftKeyIndex = mKeys.size() - 1;
                            mModifierKeys.add(key);
                        } else if (key.codes[0] == KEYCODE_ALT) {
                            mModifierKeys.add(key);
                        }
                    } else if (TAG_KEYBOARD.equals(tag)) {
                        parseKeyboardAttributes(res, parser);

                        if (mSplitKeyboard) {
                            leftSplitBorder = (mKeysInRow / 2 - 1) * mDefaultHorizontalGap + (mKeysInRow / 2) * mSplitKeyWidth;
                            splitDistance = mDisplayWidth - mKeysInRow * mSplitKeyWidth;
                            if (mReservedColumnsForSplitedKeyboard > 2)
                                reservedCenterSpace = mSplitKeyWidth; //reserved 2 columns in the center
                            else
                                reservedCenterSpace = mSplitKeyWidth / 2;  //reserved 1 columns in the center
                            if (DEBUG)
                                Log.i(TAG, "loadkeyboard() keyboard attributed parsed, leftSplitBorder = " + leftSplitBorder
                                                + ". keysInRow = " + mKeysInRow
                                                + ". mSeparatedKeyWidth = " + mSplitKeyWidth
                                                + ". splitDistance = " + splitDistance
                                                + ". centerLine = " + centerLine
                                );

                        }


                        if (showArrowKeysOnTop)    //Jeremy '12,5,24 create arrow keys before reading further rows.
                            y += createArrowKeys(res, 0, 0, false);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {

                        inKey = false;
                        x += key.gap + key.width;

                        if (DEBUG)
                            Log.i(TAG, "loadKeyboard() inkey: x = " + x
                                    + ". kye.gap = " + key.gap
                                    + ". key.width = " + key.width
                                    + ". splitDistance = " + splitDistance);


                        if (x > mTotalWidth) {
                            mTotalWidth = x;
                        }
                    } else if (inRow) {
                        inRow = false;
                        y += currentRow.verticalGap;
                        y += currentRow.defaultHeight;
                        //row++;
                    } else {
                        // TODO: error or extend?
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        /** Add arrow keys row if mShowArrowKeys is on */  //Add by Jeremy '12,5,21
        if (showArrowKeysOnBottom)
            y += createArrowKeys(res, 0, y, false);

        mTotalHeight = y - mDefaultVerticalGap;

        if (mSplitKeyboard && mShowArrowKeys != 0 && mDisplayWidth > mDisplayHeight)
            createArrowKeys(res, (mDisplayWidth - mSplitKeyWidth) / 2, 0, true);


        if (DEBUG) Log.i(TAG, "loadKeyboard():mTotalHeight" + mTotalHeight);
    }

    private void skipToEndOfRow(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int event;
        while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.END_TAG
                    && parser.getName().equals(TAG_ROW)) {
                break;
            }
        }
    }

    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {


        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                R.styleable.LIMEBaseKeyboard);

        mDefaultWidth = getDimensionOrFraction(a,
                R.styleable.LIMEBaseKeyboard_keyWidth,
                mDisplayWidth, mDisplayWidth / 10);
        mDefaultHeight = getDimensionOrFraction(a,
                R.styleable.LIMEBaseKeyboard_keyHeight, //Jeremy '11,9,4
                mDisplayHeight, 50, mKeySizeScale);
        mDefaultHorizontalGap = getDimensionOrFraction(a,
                R.styleable.LIMEBaseKeyboard_horizontalGap,
                mDisplayWidth, 0);
        mDefaultVerticalGap = getDimensionOrFraction(a,
                R.styleable.LIMEBaseKeyboard_verticalGap, //Jeremy '11,9,4
                mDisplayHeight, 0, mKeySizeScale);
        mProximityThreshold = (int) (mDefaultWidth * SEARCH_DISTANCE);
        mProximityThreshold = mProximityThreshold * mProximityThreshold; // Square it for comparison

        //Jeremy '12,5,26 for seperated keyboard in landscape with arrow keys
        mReservedColumnsForSplitedKeyboard = (int) (res.getInteger(R.integer.reserved_columns_for_seperated_keyboard));

        mKeysInRow = Math.round(mDisplayWidth / mDefaultWidth);
        mSplitKeyWidth = Math.round(mDisplayWidth / (mKeysInRow + mReservedColumnsForSplitedKeyboard));
        mSplitedKeyWidthScale = (float) (mSplitKeyWidth) / (float) (mDefaultWidth);
        if (DEBUG)
            Log.i(TAG, "mKeysInRow = " + mKeysInRow
                            + ". mDisplayWidth = " + mDisplayWidth
                            + ". mDefaultWidth = " + mDefaultWidth
                            + ". mSeparatedKeyWidth = " + mSplitKeyWidth
                            + ". mSeperatedKeyWidthScale = " + mSplitedKeyWidthScale
            );

        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        return getDimensionOrFraction(a, index, base, defValue, 1);
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue, float scale) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            //Log.i(TAG, "getDimensionOrFraction() got dimension value, defvalue = " + defValue + ". scale = " +scale);
            return (int) (a.getDimensionPixelOffset(index, defValue) * scale);  //Jeremy '11,9,4
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            //Log.i(TAG, "getDimensionOrFraction() got fraction value, base = " + base + ". defvalue = " + defValue + ". scale = " +scale);
            return (int) (a.getFraction(index, base, base, defValue) * scale); //Jeremy '12,5,26 add scale. '12,5,27 use (int) instead of round to avoid rouding error
        }
        return defValue;
    }
}
