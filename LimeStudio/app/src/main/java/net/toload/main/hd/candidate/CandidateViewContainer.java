

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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.toload.main.hd.R;

public class CandidateViewContainer extends LinearLayout implements OnTouchListener {

    private ImageButton mButtonExpand;
    private View mButtonExpandLayout;
    private CandidateView mCandidateView;


    public CandidateViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
       
        
    }

    public void initViews() {
        if (mCandidateView == null) {
            mButtonExpandLayout = findViewById(R.id.candidate_right_parent);
            mButtonExpand = (ImageButton) findViewById(R.id.candidate_right);
            if (mButtonExpand != null) {
                mButtonExpand.setOnTouchListener(this);
            }
            mCandidateView = (CandidateView) findViewById(R.id.candidates);
            TextView mEmbeddedTextView = (TextView) findViewById(R.id.embeddedComposing);

            mCandidateView.setEmbeddedComposingView(mEmbeddedTextView);
            mCandidateView.setBackgroundColor(mCandidateView.mColorBackground);
            mButtonExpand.setBackgroundColor(mCandidateView.mColorBackground);
            mButtonExpand.setImageDrawable(mCandidateView.mDrawableExpandButton);
        }
    }

    @Override
    public void requestLayout() {
        if (mCandidateView != null) {
            int availableWidth = mCandidateView.getWidth();
            int neededWidth = mCandidateView.computeHorizontalScrollRange();
         
            boolean rightVisible =  availableWidth < neededWidth;
            if(mCandidateView.isCandidateExpanded())
            	rightVisible = true;
            
            if (mButtonExpandLayout != null) {
                mButtonExpandLayout.setVisibility(rightVisible ? VISIBLE : GONE);
            }
        }
        super.requestLayout();
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v == mButtonExpand) {
            	
            	mCandidateView.showCandidatePopup();
            	
            }
        }
        return false;
    }

    
}
