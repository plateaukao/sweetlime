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
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.toload.main.hd.Lime;
import net.toload.main.hd.R;
import net.toload.main.hd.data.Im;
import net.toload.main.hd.data.Related;
import net.toload.main.hd.data.Word;
import net.toload.main.hd.limedb.LimeDB;

import java.util.HashMap;
import java.util.List;

public class ImportDialog extends DialogFragment {

	LimeDB datasource;
	Activity activity;
	View view;

	Button btnImportCancel;

	Button btnImportCustom;
	Button btnImportArray;
	Button btnImportArray10;
	Button btnImportCj;
	Button btnImportCj5;
	Button btnImportDayi;
	Button btnImportEcj;
	Button btnImportEz;
	Button btnImportPhonetic;
	Button btnImportPinyin;
	Button btnImportScj;
	Button btnImportWb;
	Button btnImportHs;

	Button btnImportRelated;

	ImportDialog importdialog;

	String importtext;

	public static ImportDialog newInstance(String importtext) {
		ImportDialog btd = new ImportDialog();
		Bundle args = new Bundle();
			   args.putString(Lime.IMPORT_TEXT, importtext);
			   btd.setArguments(args);
			   btd.setCancelable(true);
		return btd;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		importtext = getArguments().getString(Lime.IMPORT_TEXT);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.setCancelable(false);
	}


	@Override
	public void onResume() {
		super.onResume();

		getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(android.content.DialogInterface dialog,
								 int keyCode, android.view.KeyEvent event) {
				if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
					// To dismiss the fragment when the back-button is pressed.
					dismiss();
					return true;
				}
				// Otherwise, do nothing else
				else return false;
			}
		});
	}

	public void cancelDialog(){
		this.dismiss();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

		getDialog().getWindow().setTitle(getResources().getString(R.string.import_dialog_title));
		datasource = new LimeDB(getActivity());
		importdialog = this;

		activity = getActivity();
		view = inflater.inflate(R.layout.fragment_dialog_import, container, false);

		btnImportCustom = (Button) view.findViewById(R.id.btnImportCustom);
		btnImportArray = (Button) view.findViewById(R.id.btnImportArray);
		btnImportArray10 = (Button) view.findViewById(R.id.btnImportArray10);
		btnImportCj = (Button) view.findViewById(R.id.btnImportCj);
		btnImportCj5 = (Button) view.findViewById(R.id.btnImportCj5);
		btnImportDayi = (Button) view.findViewById(R.id.btnImportDayi);
		btnImportEcj = (Button) view.findViewById(R.id.btnImportEcj);
		btnImportEz = (Button) view.findViewById(R.id.btnImportEz);
		btnImportPhonetic = (Button) view.findViewById(R.id.btnImportPhonetic);
		btnImportPinyin = (Button) view.findViewById(R.id.btnImportPinyin);
		btnImportScj = (Button) view.findViewById(R.id.btnImportScj);
		btnImportWb = (Button) view.findViewById(R.id.btnImportWb);
		btnImportHs = (Button) view.findViewById(R.id.btnImportHs);

		btnImportRelated = (Button) view.findViewById(R.id.btnImportRelated);
		
		btnImportCancel = (Button) view.findViewById(R.id.btnImportCancel);
		btnImportCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		HashMap<String, String> check = new HashMap<String, String>();

		List<Im> imlist = datasource.getIm(null, Lime.IM_TYPE_NAME);
		for(int i = 0; i < imlist.size() ; i++){
			check.put(imlist.get(i).getCode(), imlist.get(i).getDesc());
		}

		if(check.get(Lime.DB_TABLE_CUSTOM) == null){
			btnImportCustom.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportCustom.setTypeface(null, Typeface.ITALIC);
			btnImportCustom.setEnabled(false);
		}else {
			btnImportCustom.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportCustom.setTypeface(null, Typeface.BOLD);

			btnImportCustom.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_CUSTOM);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_PHONETIC) == null){
			btnImportPhonetic.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportPhonetic.setTypeface(null, Typeface.ITALIC);
			btnImportPhonetic.setEnabled(false);
		}else {
			btnImportPhonetic.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportPhonetic.setTypeface(null, Typeface.BOLD);

			btnImportPhonetic.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_PHONETIC);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_CJ) == null){
			btnImportCj.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportCj.setTypeface(null, Typeface.ITALIC);
			btnImportCj.setEnabled(false);
		}else {
			btnImportCj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportCj.setTypeface(null, Typeface.BOLD);

			btnImportCj.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_CJ);
				}
			});
		}



		if(check.get(Lime.DB_TABLE_CJ5) == null){
			btnImportCj5.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportCj5.setTypeface(null, Typeface.ITALIC);
			btnImportCj5.setEnabled(false);
		}else {
			btnImportCj5.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportCj5.setTypeface(null, Typeface.BOLD);

			btnImportCj5.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_CJ5);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_SCJ) == null){
			btnImportScj.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportScj.setTypeface(null, Typeface.ITALIC);
			btnImportScj.setEnabled(false);
		}else {
			btnImportScj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportScj.setTypeface(null, Typeface.BOLD);
			btnImportScj.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_SCJ);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_ECJ) == null){
			btnImportEcj.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportEcj.setTypeface(null, Typeface.ITALIC);
			btnImportEcj.setEnabled(false);
		}else {
			btnImportEcj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportEcj.setTypeface(null, Typeface.BOLD);

			btnImportEcj.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_ECJ);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_DAYI) == null){
			btnImportDayi.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportDayi.setTypeface(null, Typeface.ITALIC);
			btnImportDayi.setEnabled(false);
		}else {
			btnImportDayi.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportDayi.setTypeface(null, Typeface.BOLD);

			btnImportDayi.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_DAYI);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_EZ) == null){
			btnImportEz.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportEz.setTypeface(null, Typeface.ITALIC);
			btnImportEz.setEnabled(false);
		}else {
			btnImportEz.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportEz.setTypeface(null, Typeface.BOLD);

			btnImportEz.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_EZ);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_ARRAY) == null){
			btnImportArray.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportArray.setTypeface(null, Typeface.ITALIC);
			btnImportArray.setEnabled(false);
		}else {
			btnImportArray.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportArray.setTypeface(null, Typeface.BOLD);

			btnImportArray.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_ARRAY);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_ARRAY10) == null){
			btnImportArray10.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportArray10.setTypeface(null, Typeface.ITALIC);
			btnImportArray10.setEnabled(false);
		}else {
			btnImportArray10.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportArray10.setTypeface(null, Typeface.BOLD);

			btnImportArray10.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_ARRAY10);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_HS) == null){
			btnImportHs.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportHs.setTypeface(null, Typeface.ITALIC);
			btnImportHs.setEnabled(false);
		}else {
			btnImportHs.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportHs.setTypeface(null, Typeface.BOLD);

			btnImportHs.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_HS);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_WB) == null){
			btnImportWb.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportWb.setTypeface(null, Typeface.ITALIC);
			btnImportWb.setEnabled(false);
		}else {
			btnImportWb.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportWb.setTypeface(null, Typeface.BOLD);

			btnImportWb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_WB);
				}
			});
		}

		if(check.get(Lime.DB_TABLE_PINYIN) == null){
			btnImportPinyin.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportPinyin.setTypeface(null, Typeface.ITALIC);
			btnImportPinyin.setEnabled(false);
		}else {
			btnImportPinyin.setAlpha(Lime.NORMAL_ALPHA_VALUE);
			btnImportPinyin.setTypeface(null, Typeface.BOLD);

			btnImportPinyin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.IM_PINYIN);
				}
			});
		}

		if(importtext.length() > 1) {
			btnImportRelated.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					confirmimportdialog(Lime.DB_RELATED);
				}
			});
		}else{
			btnImportRelated.setAlpha(Lime.HALF_ALPHA_VALUE);
			btnImportRelated.setTypeface(null, Typeface.ITALIC);
			btnImportRelated.setEnabled(false);
		}

		return view;
	}

	public void confirmimportdialog(final String imtype){

		AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

		final EditText input = new EditText(activity);

		if(imtype.equalsIgnoreCase(Lime.DB_RELATED)) {
			alertDialog.setTitle(activity.getResources().getString(R.string.import_dialog_related_title));
			alertDialog.setMessage(importtext);
		}else{
			alertDialog.setTitle(activity.getResources().getString(R.string.import_dialog_title));
			alertDialog.setMessage(importtext + getResources().getString(R.string.import_code_hint));

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			input.setLayoutParams(lp);
			alertDialog.setView(input);
		}

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						if(imtype.equals(Lime.DB_RELATED)){
							importToRelatedTable();
							dismiss();
							importdialog.dismiss();
						}else{
							if(input.getText() != null && !input.getText().toString().isEmpty()){
								importToImTable(imtype, input.getText().toString());
								dismiss();
								importdialog.dismiss();
							}else{
								Toast.makeText(activity, getResources().getString(R.string.import_code_empty), Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getResources().getString(R.string.dialog_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
	}

	private void importToRelatedTable(){

		String pword = importtext.substring(0, 1);
		String cword = importtext.substring(1);

		Related obj = new Related();
				obj.setPword(pword);
				obj.setCword(cword);
				obj.setBasescore(0);
				obj.setUserscore(1);

		datasource.add(Related.getInsertQuery(obj));
		Toast.makeText(activity, getResources().getString(R.string.import_related_success), Toast.LENGTH_SHORT).show();

	}

	private void importToImTable(String imtype, String addcode){
		Word obj = new Word();
			 obj.setCode(addcode);
		obj.setWord(importtext);
			 obj.setScore(1);
			 obj.setBasescore(0);
		datasource.add(Word.getInsertQuery(imtype, obj));

		Toast.makeText(activity, getResources().getString(R.string.import_word_success), Toast.LENGTH_SHORT).show();

	}

}
