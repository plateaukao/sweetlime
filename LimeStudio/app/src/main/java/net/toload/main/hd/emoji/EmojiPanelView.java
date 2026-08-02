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

package net.toload.main.hd.emoji;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.limedb.EmojiConverter;
import net.toload.main.hd.skin.SkinDrawables;
import net.toload.main.hd.skin.SkinStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.plateaukao.sweetlime.R;

/**
 * Emoji picker panel shown in place of the keyboard, like the symbol or
 * numeric modes: category tabs on the left, a scrollable emoji grid, and a
 * bottom row with back/space/delete/enter. Emoji come from emoji.db whose
 * en table follows Unicode block order, so categories are id ranges.
 */
public class EmojiPanelView extends LinearLayout {

    public interface Listener {
        void onEmojiPicked(String emoji);

        /** Back to the soft keyboard. */
        void onEmojiBack();

        void onEmojiSpace();

        void onEmojiDelete();

        void onEmojiEnter();
    }

    private static final String PREF_RECENT_EMOJI = "emoji_recents";
    private static final int MAX_RECENTS = 40;

    private static final int DELETE_REPEAT_START_MS = 400;
    private static final int DELETE_REPEAT_INTERVAL_MS = 60;

    /** First keyword-row id of each category run in the en table. */
    private static final int[] CATEGORY_STARTS = {
            0,      // smileys   😀 ..
            492,    // people    👦 .. (incl. hearts and clothing)
            1241,   // animals   🐒 ..
            1542,   // plants    💐 ..
            1633,   // food      🍇 ..
            2066,   // travel    🌍 .. (places, transport, time, weather)
            3192,   // activity  🎃 .. (events, sport, games)
            3583,   // objects   🔇 ..
            4454,   // symbols   🏧 ..
    };

    private static final int[] CATEGORY_LABELS = {
            R.string.emoji_tab_smileys,
            R.string.emoji_tab_people,
            R.string.emoji_tab_animals,
            R.string.emoji_tab_plants,
            R.string.emoji_tab_food,
            R.string.emoji_tab_travel,
            R.string.emoji_tab_activity,
            R.string.emoji_tab_objects,
            R.string.emoji_tab_symbols,
    };

    /** Tab 0 is recents; category i maps to tab i+1. */
    private static final int TAB_RECENT = 0;

    private final Listener mListener;
    private final LIMEPreferenceManager mPref;

    private final List<List<String>> mCategories = new ArrayList<>();
    private final List<String> mRecents = new ArrayList<>();
    private boolean mRecentsDirty = false;

    private final List<TextView> mTabViews = new ArrayList<>();
    private int mSelectedTab = -1;
    private final EmojiGridAdapter mAdapter;
    private final GridView mGrid;

    private final int mTextColor;
    private final int mTabBackground;
    private final int mTabSelectedBackground;

    private final Handler mDeleteRepeatHandler = new Handler(Looper.getMainLooper());
    private final Runnable mDeleteRepeat = new Runnable() {
        @Override
        public void run() {
            mListener.onEmojiDelete();
            mDeleteRepeatHandler.postDelayed(this, DELETE_REPEAT_INTERVAL_MS);
        }
    };

    public EmojiPanelView(Context context, List<EmojiConverter.EmojiItem> emoji,
                          LIMEPreferenceManager pref, SkinStyle style, boolean night,
                          Listener listener) {
        super(context);
        mListener = listener;
        mPref = pref;

        for (int i = 0; i < CATEGORY_STARTS.length; i++)
            mCategories.add(new ArrayList<String>());
        if (emoji != null) {
            for (EmojiConverter.EmojiItem item : emoji)
                mCategories.get(categoryOf(item.firstId)).add(item.value);
        }
        loadRecents();

        int panelBackground;
        int bottomBackground;
        if (style != null) {
            panelBackground = SkinDrawables.opaqueBackground(style.keyboardBackground, night);
            mTabBackground = SkinDrawables.opaqueBackground(style.toolbarBackground, night);
            mTabSelectedBackground = SkinDrawables.opaqueBackground(style.keyNormalPressed, night);
            bottomBackground = mTabBackground;
            mTextColor = style.textMain;
        } else {
            panelBackground = Color.WHITE;
            mTabBackground = 0xFFEEEEEE;
            mTabSelectedBackground = 0xFFC8C8C8;
            bottomBackground = 0xFFEEEEEE;
            mTextColor = 0xFF212121;
        }

        setOrientation(VERTICAL);
        setBackgroundColor(panelBackground);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(HORIZONTAL);

        ScrollView tabScroll = new ScrollView(context);
        tabScroll.setVerticalScrollBarEnabled(false);
        tabScroll.setBackgroundColor(mTabBackground);
        LinearLayout tabColumn = new LinearLayout(context);
        tabColumn.setOrientation(VERTICAL);
        for (int tab = 0; tab <= CATEGORY_LABELS.length; tab++) {
            TextView tabView = new TextView(context);
            tabView.setText(tab == TAB_RECENT
                    ? R.string.emoji_tab_recent : CATEGORY_LABELS[tab - 1]);
            tabView.setTextColor(mTextColor);
            tabView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tabView.setGravity(Gravity.CENTER);
            final int tabIndex = tab;
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTab(tabIndex);
                }
            });
            tabColumn.addView(tabView, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
            mTabViews.add(tabView);
        }
        tabScroll.addView(tabColumn);
        content.addView(tabScroll, new LinearLayout.LayoutParams(
                dp(64), ViewGroup.LayoutParams.MATCH_PARENT));

        mAdapter = new EmojiGridAdapter();
        mGrid = new GridView(context);
        mGrid.setNumColumns(GridView.AUTO_FIT);
        mGrid.setColumnWidth(dp(48));
        mGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mGrid.setVerticalScrollBarEnabled(false);
        mGrid.setCacheColorHint(Color.TRANSPARENT);
        mGrid.setAdapter(mAdapter);
        content.addView(mGrid, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

        addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        addView(buildBottomRow(context, bottomBackground), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
    }

    private View buildBottomRow(Context context, int background) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setBackgroundColor(background);

        row.addView(bottomKey(context, R.string.emoji_key_back, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEmojiBack();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

        row.addView(bottomKey(context, R.string.emoji_key_space, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEmojiSpace();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2.0f));

        ImageView delete = new ImageView(context);
        delete.setImageResource(R.drawable.key_ic_backspace);
        delete.setColorFilter(mTextColor, PorterDuff.Mode.SRC_IN);
        delete.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = dp(12);
        delete.setPadding(pad, pad, pad, pad);
        delete.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mListener.onEmojiDelete();
                        mDeleteRepeatHandler.postDelayed(mDeleteRepeat, DELETE_REPEAT_START_MS);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mDeleteRepeatHandler.removeCallbacks(mDeleteRepeat);
                        return true;
                }
                return false;
            }
        });
        row.addView(delete, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

        row.addView(bottomKey(context, R.string.emoji_key_enter, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEmojiEnter();
            }
        }), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

        return row;
    }

    private TextView bottomKey(Context context, int labelRes, OnClickListener onClick) {
        TextView key = new TextView(context);
        key.setText(labelRes);
        key.setTextColor(mTextColor);
        key.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        key.setGravity(Gravity.CENTER);
        key.setOnClickListener(onClick);
        return key;
    }

    private static int categoryOf(int firstId) {
        for (int i = CATEGORY_STARTS.length - 1; i > 0; i--) {
            if (firstId >= CATEGORY_STARTS[i]) return i;
        }
        return 0;
    }

    /** Recents when there are any, the smileys category otherwise. */
    public void showDefaultCategory() {
        selectTab(mRecents.isEmpty() ? TAB_RECENT + 1 : TAB_RECENT);
    }

    private void selectTab(int tab) {
        if (tab == mSelectedTab && !(tab == TAB_RECENT && mRecentsDirty)) return;
        if (mSelectedTab >= 0)
            mTabViews.get(mSelectedTab).setBackgroundColor(mTabBackground);
        mTabViews.get(tab).setBackgroundColor(mTabSelectedBackground);
        mSelectedTab = tab;
        // Recents are snapshotted on selection so picking from the recents tab
        // never reorders the grid under the user's finger.
        mAdapter.setEmoji(tab == TAB_RECENT
                ? new ArrayList<>(mRecents) : mCategories.get(tab - 1));
        mRecentsDirty = false;
        mGrid.setSelection(0);
    }

    private void pickEmoji(String emoji) {
        mListener.onEmojiPicked(emoji);
        mRecents.remove(emoji);
        mRecents.add(0, emoji);
        while (mRecents.size() > MAX_RECENTS)
            mRecents.remove(mRecents.size() - 1);
        mRecentsDirty = true;
    }

    private void loadRecents() {
        String stored = mPref.getParameterString(PREF_RECENT_EMOJI, "");
        if (!stored.isEmpty())
            mRecents.addAll(Arrays.asList(stored.split(",")));
    }

    /** Persists the recents MRU; called when the panel is hidden, not per tap. */
    public void saveRecents() {
        mPref.setParameter(PREF_RECENT_EMOJI, TextUtils.join(",", mRecents));
    }

    @Override
    protected void onDetachedFromWindow() {
        mDeleteRepeatHandler.removeCallbacks(mDeleteRepeat);
        saveRecents();
        super.onDetachedFromWindow();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics());
    }

    private class EmojiGridAdapter extends BaseAdapter {

        private List<String> mEmoji = new ArrayList<>();

        void setEmoji(List<String> emoji) {
            mEmoji = emoji;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mEmoji.size();
        }

        @Override
        public Object getItem(int position) {
            return mEmoji.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView cell;
            if (convertView instanceof TextView) {
                cell = (TextView) convertView;
            } else {
                cell = new TextView(parent.getContext());
                // Fully opaque text color: the theme default carries alpha,
                // which washes out color emoji glyphs.
                cell.setTextColor(Color.BLACK);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                cell.setGravity(Gravity.CENTER);
                cell.setIncludeFontPadding(false);
                cell.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
                cell.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickEmoji((String) v.getTag());
                    }
                });
            }
            String emoji = mEmoji.get(position);
            cell.setTag(emoji);
            cell.setText(emoji);
            return cell;
        }
    }
}
