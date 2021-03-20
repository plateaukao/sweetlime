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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.toload.main.hd.R;
import net.toload.main.hd.data.Word;

public class ManageImEditDialog extends DialogFragment {

	private Activity activity;
	private View view;

	private String imtype;

	//Button btnQuizExitConfirm;
	//Button btnQuizExitCancel;

	private ManageImHandler handler;

	private Word word;

	private Button btnManageImWordCancel;
	private Button btnManageImWordRemove;
	private Button btnManageImWordUpdate;

	private Button btnManageMinusScore;
	private Button btnManageAddScore;

	private TextView edtManageImWordScore;

	private EditText edtManageImWordCode;
	private EditText edtManageImWordWord;

	public ManageImEditDialog() {}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public static ManageImEditDialog newInstance(String imtype) {
		ManageImEditDialog btd = new ManageImEditDialog();
						   btd.setCancelable(true);
		Bundle bundle = new Bundle(1);
		bundle.putString("imtype", imtype);
		btd.setArguments(bundle);
		return btd;
	}
	
	public void setHandler(ManageImHandler handler, Word word){
		this.word = word;
		this.handler = handler;
	}

	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
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
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
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

		getDialog().getWindow().setTitle(getResources().getString(R.string.manage_word_dialog_edit));

		imtype = getArguments().getString("imtype");

		activity = getActivity();
		view = inflater.inflate(R.layout.fragment_dialog_edit, container, false);

		btnManageImWordCancel = (Button) view.findViewById(R.id.btnManageImWordCancel);
		btnManageImWordCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelDialog();
			}
		});

		btnManageImWordRemove = (Button) view.findViewById(R.id.btnManageImWordRemove);
		btnManageImWordRemove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setTitle(activity.getResources().getString(R.string.manage_word_dialog_delete));
				alertDialog.setMessage(activity.getResources().getString(R.string.manage_word_dialog_delete_message));
				//alertDialog.setIcon(R.drawable.);
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								handler.removeWord(word.getId());
								dialog.dismiss();
								cancelDialog();
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
		});

		btnManageImWordUpdate = (Button) view.findViewById(R.id.btnManageImWordUpdate);
		btnManageImWordUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setTitle(activity.getResources().getString(R.string.manage_word_dialog_edit));
				alertDialog.setMessage(activity.getResources().getString(R.string.manage_word_dialog_message));
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String code = edtManageImWordCode.getText().toString();
								String text = edtManageImWordWord.getText().toString();
								if(!code.isEmpty() && !text.isEmpty()) {

									int value = Integer.parseInt(edtManageImWordScore.getText().toString());
									handler.updateWord(word.getId(), code, value, text);
									handler.updateRelated(code);
									dialog.dismiss();
									cancelDialog();
								}else{
									Toast.makeText(activity, R.string.update_error, Toast.LENGTH_SHORT).show();
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
		});

		btnManageMinusScore = (Button) view.findViewById(R.id.btnManageMinusScore);
		btnManageMinusScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					int value = Integer.parseInt(edtManageImWordScore.getText().toString());
					if(value > 0){
						value = value -1 ;
						edtManageImWordScore.setText(String.valueOf(value));
					}
				}catch(Exception e){}
			}
		});

		btnManageAddScore = (Button) view.findViewById(R.id.btnManageAddScore);
		btnManageAddScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					int value = Integer.parseInt(edtManageImWordScore.getText().toString());
					value = value + 1 ;
					edtManageImWordScore.setText(String.valueOf(value));
				}catch(Exception e){}
			}
		});

		edtManageImWordScore = (TextView) view.findViewById(R.id.edtManageImWordScore);


		edtManageImWordCode = (EditText) view.findViewById(R.id.edtManageImWordCode);
		edtManageImWordWord = (EditText) view.findViewById(R.id.edtManageImWordWord);

		edtManageImWordCode.setText(word.getCode());
		edtManageImWordScore.setText(String.valueOf(word.getScore()));
		edtManageImWordWord.setText(word.getWord());

		/*txtManageImWordCode3r = (TextView) view.findViewById(R.id.txtManageImWordCode3r);

		if(!imtype.equals(Lime.DB_TABLE_DAYI)){
			edtManageImWordCode3r.setVisibility(View.GONE);
			txtManageImWordCode3r.setVisibility(View.GONE);
		}*/
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
	}

}
