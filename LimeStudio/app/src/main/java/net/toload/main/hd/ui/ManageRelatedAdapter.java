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
import net.toload.main.hd.data.Related;

import java.util.List;

/**
 * Created by Art Hung on 2015/4/26.
 */
public class ManageRelatedAdapter extends BaseAdapter {

    private  List<Related> relatedlist;

    private Activity activity;
    private LayoutInflater mInflater;

    public ManageRelatedAdapter(Activity activity,
                                List<Related> relatedlist) {
        this.activity = activity;
        this.relatedlist = relatedlist;
        this.mInflater = LayoutInflater.from(this.activity);
    }

    @Override
    public int getCount() {
        return relatedlist.size();
    }

    @Override
    public Object getItem(int position) {
        return relatedlist.get(position);
    }

    public List<Related> getList(){
        return relatedlist;
    }

    public void setList(List<Related> list){
        relatedlist = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        int type = getItemViewType(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.related, null);
            holder = new ViewHolder();
            holder.txtWord = (TextView)convertView.findViewById(R.id.txtWord);
            holder.txtFreq = (TextView)convertView.findViewById(R.id.txtFreq);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        Related r = relatedlist.get(position);
        if(r != null){
        	/*holder.chkItemDatetWorde.setText(hwresult.getGenerateDateTWorde());
        	holder.chkItemDatetWorde.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					hwlist.get(position).setCheck(arg1);
				}});*/
            String pword = r.getPword();
            String cword = r.getCword();
            String text = pword + cword;

            int freq = r.getBasescore();

            if(text.length() > 12){
                text = text.substring(0,10) + "...";
            }

            holder.txtWord.setText(text);
            holder.txtFreq.setText(freq+"");
        }

        return convertView;

    }

    static class ViewHolder{
        TextView txtWord;
        TextView txtFreq;
    }

    @Override
    public long getItemId(int position) {
        return relatedlist.get(position).getId();
    }


}
