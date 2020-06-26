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

package net.toload.main.hd.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.toload.main.hd.R;
import net.toload.main.hd.data.Word;

import java.util.List;

/**
 * Created by Art Hung on 2015/4/26.
 */
public class ManageImAdapter extends BaseAdapter {

    private List<Word> wordlist;

    private Activity activity;
    private LayoutInflater mInflater;

    public ManageImAdapter(Activity activity,
                             List<Word> wordlist) {
        this.activity = activity;
        this.wordlist = wordlist;
        this.mInflater = LayoutInflater.from(this.activity);
    }

    @Override
    public int getCount() {
        return wordlist.size();
    }

    @Override
    public Object getItem(int position) {
        return wordlist.get(position);
    }

    public List<Word> getList(){
        return wordlist;
    }

    public void setList(List<Word> list){
        wordlist = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        int type = getItemViewType(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.word, null);
            holder = new ViewHolder();
            holder.txtWord = (TextView)convertView.findViewById(R.id.txtWord);
            holder.txtCode = (TextView)convertView.findViewById(R.id.txtCode);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        Word w = wordlist.get(position);
        if(w != null){
        	/*holder.chkItemDatetWorde.setText(hwresult.getGenerateDateTWorde());
        	holder.chkItemDatetWorde.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					hwlist.get(position).setCheck(arg1);
				}});*/
            String wordtext = w.getWord();
            if(wordtext.length() > 12){
                wordtext = wordtext.substring(0,10) + "...";
            }
            holder.txtCode.setText(w.getCode());
            holder.txtWord.setText(wordtext);
        }

        return convertView;

    }

    static class ViewHolder{
        TextView txtWord;
        TextView txtCode;
    }

    @Override
    public long getItemId(int position) {
        return wordlist.get(position).getId();
    }


}
