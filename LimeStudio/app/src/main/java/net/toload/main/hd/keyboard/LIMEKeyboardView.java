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

import info.plateaukao.sweetlime.R;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import net.toload.main.hd.global.LIMEPreferenceManager;

public class LIMEKeyboardView extends LIMEKeyboardBaseView {
	static final boolean DEBUG = true;
	static final String TAG = "LIMEKeyboardView";

	public static final int KEYCODE_OPTIONS = -100;
	public static final int KEYCODE_SPACE_LONGPRESS = -102;
    public static final int KEYCODE_NEXT_IM = -104;
    public static final int KEYCODE_PREV_IM = -105;
	public static final int KEYCODE_SYMBOL_KEYBOARD = -106;
	public static final int KEYCODE_CURSOR_LEFT = -107;
	public static final int KEYCODE_CURSOR_RIGHT = -108;

	private int mKeyHeight;

	private final LIMEPreferenceManager mLIMEPref;
	private final int mCursorStepPx;
	private boolean mSpaceCursorEnabled;
	private int mSpaceCursorSteps;
	private boolean mSpaceCursorMoved;

	public LIMEKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mKeyHeight = context.getResources().getDimensionPixelSize(R.dimen.key_height);
		mLIMEPref = new LIMEPreferenceManager(context.getApplicationContext());
		mCursorStepPx = Math.max(ViewConfiguration.get(context).getScaledTouchSlop(),
				(int) (16 * context.getResources().getDisplayMetrics().density));
	}

	public LIMEKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mKeyHeight = context.getResources().getDimensionPixelSize(R.dimen.key_height);
		mLIMEPref = new LIMEPreferenceManager(context.getApplicationContext());
		mCursorStepPx = Math.max(ViewConfiguration.get(context).getScaledTouchSlop(),
				(int) (16 * context.getResources().getDisplayMetrics().density));
	}

	@Override
	public void setKeyboard(LIMEBaseKeyboard keyboard) {
		super.setKeyboard(keyboard);
		mSpaceCursorEnabled = mLIMEPref.getSpaceCursorDrag();
	}

	@Override
	protected boolean onLongPress(Key key) {
		if(DEBUG)
			Log.i(TAG, "onLongPress, keycode = "+ key.codes[0] 
				+"; spaceDragDiff = " +((LIMEKeyboard) this.getKeyboard()).getSpaceDragDiff()
				+"; key_height = " + mKeyHeight
					);
		if (key.codes[0] == LIMEBaseKeyboard.KEYCODE_DONE) {
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showInputMethodPicker();
			return true;
		}else if (key.codes[0] == LIMEKeyboard.KEYCODE_SPACE
				&& !mSpaceCursorMoved
				&& Math.abs(((LIMEKeyboard) this.getKeyboard()).getSpaceDragDiff() ) < mKeyHeight/5) { //Jeremy '12,4,23 avoid small move blocking the long press.
			getOnKeyboardActionListener().onKey(KEYCODE_SPACE_LONGPRESS, null, 0, 0);
			return true;
		} else if (key.codes[0] ==  LIMEKeyboard.KEYCODE_MODE_CHANGE) {
			getOnKeyboardActionListener().onKey(KEYCODE_SYMBOL_KEYBOARD, null, 0, 0);
			return true;
		} else {
			return super.onLongPress(key);
		}
	}

	/*
	 *
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent me) {
		if(DEBUG) Log.i(TAG, "OnTouchEvent(), me.getAction() =" + me.getAction());
		LIMEKeyboard keyboard = (LIMEKeyboard) getKeyboard();
		final int action = me.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			if(DEBUG) Log.i(TAG, "OnTouchEvent(), ACTION_DOWN");
			keyboard.keyReleased();
			mSpaceCursorSteps = 0;
			mSpaceCursorMoved = false;
		}

		if (action == MotionEvent.ACTION_UP) {
			if (mSpaceCursorEnabled) {
				if (mSpaceCursorMoved) {
					// Cursor drag finished; swallow the space character.
					me.setAction(MotionEvent.ACTION_CANCEL);
					keyboard.keyReleased();
					return super.onTouchEvent(me);
				}
			} else {
				int spaceDragDirection = keyboard.getSpaceDragDirection();
				if(DEBUG) Log.i(TAG, "OnTouchEvent(), ACTION_UP, spaceDragDirection:" + spaceDragDirection);
				if (spaceDragDirection != 0) {
					getOnKeyboardActionListener().onKey(
							spaceDragDirection == 1 ? KEYCODE_NEXT_IM : KEYCODE_PREV_IM,
									null,0,0);
					me.setAction(MotionEvent.ACTION_CANCEL);
					keyboard.keyReleased();
					return super.onTouchEvent(me);
				}
			}
		}

		final boolean result = super.onTouchEvent(me);

		// Space drag state is updated inside super via PointerTracker/LIMEKeyboard.isInside().
		if (mSpaceCursorEnabled && action == MotionEvent.ACTION_MOVE) {
			final int targetSteps = keyboard.getSpaceDragDiff() / mCursorStepPx;
			while (mSpaceCursorSteps < targetSteps) {
				getOnKeyboardActionListener().onKey(KEYCODE_CURSOR_RIGHT, null, 0, 0);
				mSpaceCursorSteps++;
				mSpaceCursorMoved = true;
			}
			while (mSpaceCursorSteps > targetSteps) {
				getOnKeyboardActionListener().onKey(KEYCODE_CURSOR_LEFT, null, 0, 0);
				mSpaceCursorSteps--;
				mSpaceCursorMoved = true;
			}
		}
		return result;
	}
	

}
