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

import android.view.KeyEvent;
import android.text.method.MetaKeyKeyListener;

public abstract class LIMEMetaKeyKeyListener extends MetaKeyKeyListener{


    private static final int LOCKED_SHIFT = 8;
   
    public static final int META_CAP_LOCKED = KeyEvent.META_SHIFT_ON << LOCKED_SHIFT;
    public static final int META_ALT_LOCKED = KeyEvent.META_ALT_ON << LOCKED_SHIFT;
    public static final int META_SYM_LOCKED = KeyEvent.META_SYM_ON << LOCKED_SHIFT;

    
    public static final int META_SELECTING = 1 << 16;

    private static final int USED_SHIFT = 24;
   
    private static final long META_CAP_USED = ((long)KeyEvent.META_SHIFT_ON) << USED_SHIFT;
    private static final long META_ALT_USED = ((long)KeyEvent.META_ALT_ON) << USED_SHIFT;
    private static final long META_SYM_USED = ((long)KeyEvent.META_SYM_ON) << USED_SHIFT;

    private static final int PRESSED_SHIFT = 32;
   
    private static final long META_CAP_PRESSED = ((long)KeyEvent.META_SHIFT_ON) << PRESSED_SHIFT;
    private static final long META_ALT_PRESSED = ((long)KeyEvent.META_ALT_ON) << PRESSED_SHIFT;
    private static final long META_SYM_PRESSED = ((long)KeyEvent.META_SYM_ON) << PRESSED_SHIFT;

    //released shift can not be active if pressed shift is active
    private static final int RELEASED_SHIFT = 40;
   
    private static final long META_CAP_RELEASED = ((long)KeyEvent.META_SHIFT_ON) << RELEASED_SHIFT;
    private static final long META_ALT_RELEASED = ((long)KeyEvent.META_ALT_ON) << RELEASED_SHIFT;
    private static final long META_SYM_RELEASED = ((long)KeyEvent.META_SYM_ON) << RELEASED_SHIFT;

    private static final long META_SHIFT_MASK = META_SHIFT_ON
            | META_CAP_LOCKED | META_CAP_USED
            | META_CAP_PRESSED | META_CAP_RELEASED;
    private static final long META_ALT_MASK = META_ALT_ON
            | META_ALT_LOCKED | META_ALT_USED
            | META_ALT_PRESSED | META_ALT_RELEASED;
    private static final long META_SYM_MASK = META_SYM_ON
            | META_SYM_LOCKED | META_SYM_USED
            | META_SYM_PRESSED | META_SYM_RELEASED;
	//private static final Object CAP = new NoCopySpan.Concrete();
    //private static final Object ALT = new NoCopySpan.Concrete();
    //private static final Object SYM = new NoCopySpan.Concrete();
	//private static final Object SELECTING = new NoCopySpan.Concrete();
	


    /**
     * Call this method after you handle a keypress so that the meta
     * state will be reset to unshifted (if it is not still down)
     * or primed to be reset to unshifted (once it is released).  Takes
     * the current state, returns the new state.
     */
    public static long adjustMetaAfterKeypress(long state) {
    	if(android.os.Build.VERSION.SDK_INT > 10)
    		return MetaKeyKeyListener.adjustMetaAfterKeypress(state);
    	
        state = adjust(state, META_SHIFT_ON, META_SHIFT_MASK);
        state = adjust(state, META_ALT_ON, META_ALT_MASK);
        state = adjust(state, META_SYM_ON, META_SYM_MASK);
        return state;
    }

    private static long adjust(long state, int what, long mask) {
    
        if ((state&(((long)what)<<PRESSED_SHIFT)) != 0)
            return (state&~mask) | what | ((long)what)<<USED_SHIFT;
        else if ((state&(((long)what)<<RELEASED_SHIFT)) != 0)
            return state & ~mask;
        return state;
    }

    /**
     * Handles presses of the meta keys.
     */
    public static long handleKeyDown(long state, int keyCode, KeyEvent event) {
    	if(android.os.Build.VERSION.SDK_INT > 10)
    		return MetaKeyKeyListener.handleKeyDown(state, keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            return press(state, META_SHIFT_ON, META_SHIFT_MASK);
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT
                || keyCode == KeyEvent.KEYCODE_NUM) {
            return press(state, META_ALT_ON, META_ALT_MASK);
        }

        if (keyCode == KeyEvent.KEYCODE_SYM) {
            return press(state, META_SYM_ON, META_SYM_MASK);
        }

        return state;
    }

    private static long press(long state, int what, long mask) {
        if ((state&(((long)what)<<PRESSED_SHIFT)) != 0)
            ; // repeat before release
        else if ((state&(((long)what)<<RELEASED_SHIFT)) != 0)
            state = (state&~mask) | what | (((long)what) << LOCKED_SHIFT);
        else if ((state&(((long)what)<<USED_SHIFT)) != 0)
            ; // repeat after use
        else if ((state&(((long)what)<<LOCKED_SHIFT)) != 0)
            state = state&~mask;
        else
        	//Buggy original:
        	//state = (state | what | (((long)what)<<PRESSED_SHIFT));
            state = (state | what | (((long)what)<<PRESSED_SHIFT)) & (~(((long)what)<<RELEASED_SHIFT));
        return state;
    }


    /**
     * Handles release of the meta keys.
     */
    public static long handleKeyUp(long state, int keyCode, KeyEvent event) {
    	if(android.os.Build.VERSION.SDK_INT > 10)
    		return MetaKeyKeyListener.handleKeyUp(state, keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            return release(state, META_SHIFT_ON, META_SHIFT_MASK);
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT
                || keyCode == KeyEvent.KEYCODE_NUM) {
            return release(state, META_ALT_ON, META_ALT_MASK);
        }

        if (keyCode == KeyEvent.KEYCODE_SYM) {
            return release(state, META_SYM_ON, META_SYM_MASK);
        }

        return state;
    }

    private static long release(long state, int what, long mask) {
        if ((state&(((long)what)<<USED_SHIFT)) != 0)
            state = state&~mask;
        else if ((state&(((long)what)<<PRESSED_SHIFT)) != 0)
                //released can not be with pressed
        	//Buggy original:
        	//state = (state | what | (((long)what)<<RELEASED_SHIFT));
            state = (state | what | (((long)what)<<RELEASED_SHIFT)) & (~(((long)what)<<PRESSED_SHIFT));
        return state;
    }


   
}
