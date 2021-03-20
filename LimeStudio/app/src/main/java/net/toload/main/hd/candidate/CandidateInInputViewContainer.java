

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
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.toload.main.hd.R;

public class CandidateInInputViewContainer extends LinearLayout  implements View.OnClickListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "CandiInputViewContainer";
    private ImageButton mRightButton;
    private View mButtonRightExpand;
    private CandidateView mCandidateView;

    Context ctx;

    public CandidateInInputViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG)
            Log.i(TAG, "CandidateInInputViewContainer() constructor");

        ctx = context;

    }

    public void initViews() {
        if (DEBUG)
            Log.i(TAG, "initViews()");
        if (mCandidateView == null) {
            mButtonRightExpand = findViewById(R.id.candidate_right_parent);
            mRightButton = (ImageButton) findViewById(R.id.candidate_right);

            if (mRightButton != null) {
                mRightButton.setOnClickListener(this);
            }
            mCandidateView = (CandidateView) findViewById(R.id.candidatesView);

            mCandidateView.setBackgroundColor(mCandidateView.mColorBackground);
            mRightButton.setBackgroundColor(mCandidateView.mColorBackground);
            this.setBackgroundColor(mCandidateView.mColorBackground);
        }
    }

    @Override
    public void requestLayout() {
        if (DEBUG)
            Log.i(TAG, "requestLayout()");

        if (mCandidateView != null) {
            int availableWidth = mCandidateView.getWidth();
            int neededWidth = mCandidateView.computeHorizontalScrollRange();

            if (DEBUG)
                Log.i(TAG, "requestLayout() availableWidth:" + availableWidth + " neededWidth:" + neededWidth);


            boolean showExpandButton = availableWidth < neededWidth;
            boolean showVoiceInputButton = mCandidateView.isEmpty();
            if (mCandidateView.isCandidateExpanded())
                showExpandButton = true;

            if (mRightButton != null) {
                mRightButton.setImageDrawable(showVoiceInputButton ? mCandidateView.mDrawableVoiceInput : mCandidateView.mDrawableExpandButton);
            }

            if (mButtonRightExpand != null) {
                mButtonRightExpand.setVisibility((showVoiceInputButton || showExpandButton) ? VISIBLE : GONE);
            }
        }
        super.requestLayout();
    }

    @Override
    public void onClick(View v) {

        if (mCandidateView.isEmpty())
            mCandidateView.startVoiceInput();
        else
            mCandidateView.showCandidatePopup();


    }
}
