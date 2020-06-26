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

package net.toload.main.hd.limesettings;

import java.io.File;
import java.util.List;

import net.toload.main.hd.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LIMESelectFileAdapter extends BaseAdapter {

	//private File currentdir;
	private List<File> list;
	private LayoutInflater mInflater;
	//private Context mContext;
	//private BaseAdapter adapter;
	
	public LIMESelectFileAdapter(Context context, List<File> ls) {
		this.list = ls;
		//this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		//this.adapter = this;
	}
	

	public int getCount() {		
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.imgstring, null);
			holder = new ViewHolder();
			holder.image = (ImageView)convertView.findViewById(R.id.img_function_icon);
			holder.filename = (TextView)convertView.findViewById(R.id.txt_function_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(list.get(position).isDirectory()){
			holder.image.setImageResource(R.drawable.folder);
		}else{
			holder.image.setImageResource(R.drawable.scolling_holder);
		}
		holder.filename.setText(list.get(position).getName());
		
		return convertView;
	}
	
	static class ViewHolder{
		ImageView image;
		TextView filename;
		TextView detail;
	}

	

}
