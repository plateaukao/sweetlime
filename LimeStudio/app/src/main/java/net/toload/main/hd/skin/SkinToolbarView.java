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
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.toload.main.hd.global.LIMEPreferenceManager;

import info.plateaukao.sweetlime.R;

/**
 * Toolbar shown in the candidate bar slot while nothing is being composed,
 * built from an imported skin's toolbarButtons configuration. Buttons use
 * icon drawables tinted with the skin's toolbar color where an equivalent
 * exists; skin function ids without a Sweet LIME equivalent render as blank
 * spacers.
 */
public class SkinToolbarView extends LinearLayout {

    public interface OnActionListener {
        void onSkinToolbarAction(int function);
    }

    private final int mHeight;

    public SkinToolbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        Resources r = context.getResources();
        LIMEPreferenceManager pref = new LIMEPreferenceManager(context.getApplicationContext());
        // Mirror CandidateView's height so the bar keeps its size when the
        // candidate view is swapped out for the toolbar.
        mHeight = (int) (r.getDimensionPixelSize(R.dimen.candidate_stripe_height) * pref.getFontSize())
                + (int) (r.getDimensionPixelSize(R.dimen.candidate_vertical_padding) * pref.getFontSize());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    private static int iconFor(int function) {
        switch (function) {
            case SkinSettings.TB_SETTINGS:
                return R.drawable.skin_ic_settings;
            case SkinSettings.TB_COLLAPSE:
                return R.drawable.skin_ic_collapse;
            case SkinSettings.TB_CHI_ENG:
                return R.drawable.skin_ic_lang;
            case SkinSettings.TB_SIMP_TRAD:
                return R.drawable.skin_ic_translate;
            case SkinSettings.TB_SYMBOL:
                return R.drawable.skin_ic_keyboard;
            case SkinSettings.TB_NUMBER:
                return R.drawable.skin_ic_dialpad;
            case SkinSettings.TB_SELECT_ALL:
                return R.drawable.skin_ic_select_all;
            case SkinSettings.TB_COPY:
                return R.drawable.skin_ic_copy;
            case SkinSettings.TB_CUT:
                return R.drawable.skin_ic_cut;
            case SkinSettings.TB_PASTE:
                return R.drawable.skin_ic_paste;
            case SkinSettings.TB_UNDO:
                return R.drawable.skin_ic_undo;
            case SkinSettings.TB_REDO:
                return R.drawable.skin_ic_redo;
            case SkinSettings.TB_CURSOR_LEFT:
                return R.drawable.skin_ic_left;
            case SkinSettings.TB_CURSOR_RIGHT:
                return R.drawable.skin_ic_right;
            default:
                return 0; // spacer / unsupported function
        }
    }

    /** (Re)builds the toolbar buttons from the skin configuration. */
    public void setup(SkinSettings skin, SkinStyle style, boolean night,
                      final OnActionListener listener) {
        removeAllViews();
        if (skin == null || style == null) return;
        setBackgroundColor(SkinDrawables.opaqueBackground(style.toolbarBackground, night));

        int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                style.toolbarSize, getContext().getResources().getDisplayMetrics());
        LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        for (final int function : skin.toolbarButtons) {
            int iconRes = iconFor(function);
            View button;
            if (iconRes != 0) {
                ImageView icon = new ImageView(getContext());
                icon.setImageResource(iconRes);
                icon.setColorFilter(style.toolbarColor, PorterDuff.Mode.SRC_IN);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int pad = Math.max(0, (mHeight - iconSize) / 2);
                icon.setPadding(0, pad, 0, pad);
                button = icon;
            } else {
                // Spacer keeps the slot so the layout matches the design.
                button = new TextView(getContext());
                ((TextView) button).setGravity(Gravity.CENTER);
            }
            if (iconRes != 0) {
                button.setBackgroundDrawable(
                        SkinDrawables.toolbarButtonBackground(getContext(), style));
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onSkinToolbarAction(function);
                    }
                });
            }
            addView(button, lp);
        }
    }
}
