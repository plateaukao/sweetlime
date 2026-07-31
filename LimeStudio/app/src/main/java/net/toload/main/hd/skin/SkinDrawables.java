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

package net.toload.main.hd.skin;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

/**
 * Builds runtime drawables that mirror the static theme drawables
 * (btn_keyboard_key_* : flat rounded rects with optional stroke) using the
 * colors from an imported skin.
 */
public class SkinDrawables {

    private SkinDrawables() {
    }

    private static int dp(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    private static Drawable keyFace(Context context, SkinStyle style, int fillColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(context, 3));
        shape.setColor(fillColor);
        if (style.borderSize > 0)
            shape.setStroke(dp(context, style.borderSize), style.borderColor);
        int inset = dp(context, 2);
        return new InsetDrawable(shape, inset, inset, inset, inset);
    }

    /**
     * State list equivalent to btn_keyboard_key_*.xml: state_single marks
     * function keys, state_pressed the pressed face.
     */
    public static StateListDrawable keyBackground(Context context, SkinStyle style) {
        StateListDrawable d = new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_single, android.R.attr.state_pressed},
                keyFace(context, style, style.keySystemPressed));
        d.addState(new int[]{android.R.attr.state_single},
                keyFace(context, style, style.keySystem));
        d.addState(new int[]{android.R.attr.state_pressed},
                keyFace(context, style, style.keyNormalPressed));
        d.addState(new int[]{},
                keyFace(context, style, style.keyNormal));
        return d;
    }

    /**
     * Skins made for iOS often use near-transparent keyboard backgrounds
     * (layered over a system blur there). Android has no such backdrop, so
     * translucent backgrounds are composited over an opaque base color.
     */
    public static int opaqueBackground(int color, boolean night) {
        int base = night ? 0xFF1C1C1E : 0xFFD0D3DA;
        return compositeOver(color, base);
    }

    static int compositeOver(int color, int opaqueBase) {
        int a = (color >>> 24);
        if (a == 0xFF) return color;
        int r = blend(color >>> 16 & 0xFF, opaqueBase >>> 16 & 0xFF, a);
        int g = blend(color >>> 8 & 0xFF, opaqueBase >>> 8 & 0xFF, a);
        int b = blend(color & 0xFF, opaqueBase & 0xFF, a);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int blend(int src, int dst, int alpha) {
        return (src * alpha + dst * (255 - alpha)) / 255;
    }

    public static Drawable keyboardBackground(SkinStyle style, boolean night) {
        return new ColorDrawable(opaqueBackground(style.keyboardBackground, night));
    }

    public static Drawable toolbarBackground(SkinStyle style, boolean night) {
        return new ColorDrawable(opaqueBackground(style.toolbarBackground, night));
    }

    /** Pressed-feedback background for toolbar buttons. */
    public static StateListDrawable toolbarButtonBackground(Context context, SkinStyle style) {
        StateListDrawable d = new StateListDrawable();
        GradientDrawable pressed = new GradientDrawable();
        pressed.setShape(GradientDrawable.RECTANGLE);
        pressed.setCornerRadius(dp(context, 3));
        pressed.setColor(style.keySystemPressed);
        d.addState(new int[]{android.R.attr.state_pressed}, pressed);
        d.addState(new int[]{}, new ColorDrawable(0));
        return d;
    }
}
