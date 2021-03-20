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
import net.toload.main.hd.data.Related;

public class ManageRelatedEditDialog extends DialogFragment {

	private Activity activity;
	private View view;

	private ManageRelatedHandler handler;

	private Related related;

	private Button btnManageRelatedCancel;
	private Button btnManageRelatedRemove;
	private Button btnManageRelatedUpdate;

	private Button btnManageMinusScore;
	private Button btnManageAddScore;

	private TextView edtManageRelatedScore;

	private EditText edtManageRelatedWord;

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public static ManageRelatedEditDialog newInstance() {
		ManageRelatedEditDialog btd = new ManageRelatedEditDialog();
						   btd.setCancelable(true);
		return btd;
	}
	
	public void setHandler(ManageRelatedHandler handler, Related related){
		this.related = related;
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

		getDialog().getWindow().setTitle(getResources().getString(R.string.manage_related_dialog_edit));

		activity = getActivity();
		view = inflater.inflate(R.layout.fragment_dialog_related_edit, container, false);

		btnManageRelatedCancel = (Button) view.findViewById(R.id.btnManageRelatedCancel);
		btnManageRelatedCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelDialog();
			}
		});

		btnManageRelatedRemove = (Button) view.findViewById(R.id.btnManageRelatedRemove);
		btnManageRelatedRemove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setTitle(activity.getResources().getString(R.string.manage_related_dialog_delete));
				alertDialog.setMessage(activity.getResources().getString(R.string.manage_related_dialog_delete_message));
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								handler.removeRelated(related.getId());
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

		btnManageRelatedUpdate = (Button) view.findViewById(R.id.btnManageRelatedUpdate);
		btnManageRelatedUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
				alertDialog.setTitle(activity.getResources().getString(R.string.manage_related_dialog_edit));
				alertDialog.setMessage(activity.getResources().getString(R.string.manage_related_dialog_message));
				//alertDialog.setIcon(R.drawable.);
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {

								String score = edtManageRelatedScore.getText().toString();
								String source = edtManageRelatedWord.getText().toString();
								String pword = "";
								String cword = "";

								if(!source.isEmpty() || source.length() > 1){
									source = source.trim();
									pword = source.substring(0,1);
									cword = source.substring(1);
									handler.updateRelated(related.getId(), pword, cword, Integer.parseInt(score));
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
					int value = Integer.parseInt(edtManageRelatedScore.getText().toString());
					if(value > 0){
						value = value -1 ;
						edtManageRelatedScore.setText(String.valueOf(value));
					}
				}catch(Exception e){}
			}
		});

		btnManageAddScore = (Button) view.findViewById(R.id.btnManageAddScore);
		btnManageAddScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					int value = Integer.parseInt(edtManageRelatedScore.getText().toString());
					value = value + 1 ;
					edtManageRelatedScore.setText(String.valueOf(value));
				}catch(Exception e){}
			}
		});

		edtManageRelatedWord = (EditText) view.findViewById(R.id.edtManageRelatedWord);

		edtManageRelatedScore = (TextView) view.findViewById(R.id.edtManageRelatedScore);

		edtManageRelatedWord.setText(related.getPword() + related.getCword());
		edtManageRelatedWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

			}
		});
		edtManageRelatedScore.setText(related.getBasescore() + "");
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
	}

}
