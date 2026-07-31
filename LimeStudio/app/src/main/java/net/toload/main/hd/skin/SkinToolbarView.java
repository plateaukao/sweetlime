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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.toload.main.hd.global.LIMEPreferenceManager;

import info.plateaukao.sweetlime.R;

/**
 * Toolbar shown in the candidate bar slot while nothing is being composed,
 * built from an imported skin's toolbarButtons configuration. Skin function
 * ids without a Sweet LIME equivalent render as blank spacers.
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

    private static String labelFor(int function) {
        switch (function) {
            case SkinSettings.TB_SETTINGS:
                return "設定";
            case SkinSettings.TB_COLLAPSE:
                return "▽";
            case SkinSettings.TB_CHI_ENG:
                return "中英";
            case SkinSettings.TB_SIMP_TRAD:
                return "簡繁";
            case SkinSettings.TB_SYMBOL:
                return "符號";
            case SkinSettings.TB_NUMBER:
                return "123";
            case SkinSettings.TB_SELECT_ALL:
                return "全選";
            case SkinSettings.TB_COPY:
                return "複製";
            case SkinSettings.TB_CUT:
                return "剪下";
            case SkinSettings.TB_PASTE:
                return "貼上";
            case SkinSettings.TB_UNDO:
                return "復原";
            case SkinSettings.TB_REDO:
                return "重做";
            case SkinSettings.TB_CURSOR_LEFT:
                return "◁";
            case SkinSettings.TB_CURSOR_RIGHT:
                return "▷";
            default:
                return null; // spacer / unsupported function
        }
    }

    /** (Re)builds the toolbar buttons from the skin configuration. */
    public void setup(SkinSettings skin, SkinStyle style, boolean night,
                      final OnActionListener listener) {
        removeAllViews();
        if (skin == null || style == null) return;
        setBackgroundColor(SkinDrawables.opaqueBackground(style.toolbarBackground, night));

        for (final int function : skin.toolbarButtons) {
            String label = labelFor(function);
            TextView button = new TextView(getContext());
            button.setGravity(Gravity.CENTER);
            button.setTextColor(style.toolbarColor);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.toolbarSize);
            if (label != null) {
                button.setText(label);
                button.setBackgroundDrawable(
                        SkinDrawables.toolbarButtonBackground(getContext(), style));
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onSkinToolbarAction(function);
                    }
                });
            }
            LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
            addView(button, lp);
        }
    }
}
