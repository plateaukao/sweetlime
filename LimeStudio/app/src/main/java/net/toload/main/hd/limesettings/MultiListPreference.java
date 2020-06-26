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

/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <kool.name at gmail.com> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.  Justin "kool.name" Lee
 * ----------------------------------------------------------------------------
 */

package net.toload.main.hd.limesettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class MultiListPreference extends DialogPreference {
	/**	Tag for logging! */
	public static final boolean DEBUG = false;
	public static final String TAG = "MultiListPreference";

	/**	How the "choices" will be delimeted. */
	public static final String CHOICE_DELIMITER = ";";
	/**	Truth regex thing. */
	public static final String TRUTH_REGEX = "^(?i:t(?:rue)?)";
	/**	"Using default." message incase of dialog error. */
	public static final String USING_DEFAULT = "Using default.";

	/**	Contains the boolean state of each entry. */
	private boolean[] state = null;
	/**	Contains textual representations of the entries. */
	private CharSequence[] entry = null;
	/**	Contains the default value, if any. */
	private String defaultValue = null;


	/**	Parse character sequence array into boolean array using regex.
		@param state Character sequence array to parse.
		@return Boolean array or null. */
	private static boolean[] cs2b(CharSequence[] state) {
		if(DEBUG)
			Log.d(TAG, "cs2b()");

		boolean[] out = null;
		
		if (state != null) {
			out = new boolean[state.length];

			for (int e = 0; e < state.length; e++) {
				out[e] = state[e].toString().matches(TRUTH_REGEX);
			}
		}

		return (out);
	}

	/**	Parse delimited numbers into a boolean array.
		@param state The states given here will be true.
		@param size The array size.
		@return Boolean array of given size or null. */
	private static boolean[] ds2b(CharSequence state, int size) {
		if(DEBUG)
			Log.d(TAG, "ds2b(): " + state + " (" + size + ")");

		boolean[] out = null;

		if (state != null) {
			out = new boolean[size];

			String[] s = state.toString().split(CHOICE_DELIMITER);
	
			for (int e = 0; e < s.length; e++) {
				int index = Integer.parseInt(s[e]);
	
				if (index < out.length) {
					out[index] = true;
				} else {
					out = null;
					break;
				}
			}
		}

		return (out);
	}

	/**	Turn boolean array into delimited numbers.
		@param state State array.
		@return Delimited number thing or null. */
	private static CharSequence b2ds(boolean[] state) {
		if(DEBUG)
			Log.d(TAG, "b2ds()");

		String out = null;

		if (state != null) {
			for (int e = 0; e < state.length; e++) {
				if (state[e]) {
					if (out == null) {
						out = "" + e;
					} else {
						out += CHOICE_DELIMITER + e;
					}
				}
			}

			if (out == null) {
				out = "";
			}
		}

		return (out);
	}


	/**	Set the entries to something new.
		@param entry New entry array.
		@return Success or failure flag. */
	public boolean setEntries(CharSequence[] entry) {
		if(DEBUG)
			Log.d(TAG, "setEntries()");

		boolean updated = false;

		if (this.entry == null) {
			if (this.state == null) {
				updated = true;
			} else if (entry.length == this.state.length) {
				updated = true;
			}
		} else if (entry.length == this.entry.length) {
			updated = true;
		}

		if (updated) {
			this.entry = entry;
		}

		return (updated);
	}

	/**	Fetch a copy of the current entry array.
		@return Current entry array clone. */
	public CharSequence[] getEntries() {
		if(DEBUG)
			Log.d(TAG, "getEntries()");

		return (this.entry.clone());
	}

	/**	Set the state to something new.
		@param state New state array.
		@return Success or failure flag. */
	public boolean setValue(boolean[] state) {
		if(DEBUG)
			Log.d(TAG, "setValue()");

		boolean updated = false;

		if (this.state == null) {
			if (this.entry == null) {
				updated = true;
			} else if (state.length == this.entry.length) {
				updated = true;
			}
		} else if (state.length == this.state.length) {
			updated = true;
		}

		if (updated) {
			this.state = state;
		}

		return (updated);
	}

	/**	Set the state to something new, state must be previously initialized.
		@param state New state, given by delimeted index.
		@return Success or failure flag. */
	public boolean setValue(CharSequence state) {
		if(DEBUG)
			Log.d(TAG, "setValue(): " + state);

		boolean updated = false;

		if (this.state != null) {
			boolean[] newState = ds2b(state, this.state.length);

			if (newState != null) {
				this.state = newState;
				updated = true;
			}
		}

		return (updated);
	}

	/**	Fetch a copy of the current state array.
		@return Current state array clone. */
	public boolean[] getValue() {
		if(DEBUG)
			Log.d(TAG, "getValue()");

		return (this.state.clone());
	}


	@SuppressWarnings("ResourceType")
	public MultiListPreference(Context context, AttributeSet attrs) {
		super(context, attrs); 
		if(DEBUG)
			Log.d(TAG, "MultiListPreference()");

		TypedArray in = context.obtainStyledAttributes(
			attrs,
			new int[]{
				// "android:entries", just like ListPreference
				android.R.attr.entries,
				// "android:entryValues", these are defaults ONLY
				android.R.attr.entryValues,
				// "android:defaultValue"
				android.R.attr.defaultValue
			}
		);

		this.entry = in.getTextArray(0);

		this.defaultValue = in.getString(2);

		if (this.entry == null) {
			if(DEBUG)
				Log.d(TAG, "MultiListPreference(): Could not restore entry.");
		} else {
			if(DEBUG)
				Log.d(TAG, "MultiListPreference(): Entry from defaults.");

			if (this.defaultValue != null) {
				this.state = ds2b(this.defaultValue, this.entry.length);
			}

			if (this.state == null) {
				this.state = cs2b(in.getTextArray(1));

				if (this.state == null) {
					if(DEBUG)
						Log.d(TAG, "MultiListPreference(): Could not restore state.");
				} else {
					if(DEBUG)
						Log.d(TAG, "MultiListPreference(): Restored state from entryValues.");
				}
			} else {
				if(DEBUG)
					Log.d(TAG, "MultiListPreference(): Restored state from defaultValue.");
			}
		}

		in.recycle();
	}


	@Override // here we set the multichoiceitem content
	public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		if(DEBUG)
			Log.d(TAG, "onPrepareDialogBuilder()");

		// try to restore state from persisted value
		if (this.entry != null || this.state != null) {
			int size = (
				(this.entry == null)
					? this.state.length
					: this.entry.length
			);
			
			boolean[] persistedState = ds2b(this.getPersistedString(null), size);

			if (persistedState != null) {
				this.state = persistedState;

				if(DEBUG)
					Log.d(TAG, "onPrepareDialogBuilder(): Persisted state restored.");
			}
		}


		// build the dialog
		builder
			.setCancelable(false)

			.setMultiChoiceItems(
				this.entry, // entries pulled from XML
				this.state,

				new DialogInterface.OnMultiChoiceClickListener() {
					// make a "convenience" pointer, cause I'm lazy
					private final MultiListPreference that = MultiListPreference.this;

					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						if(DEBUG)
							Log.d(TAG, "listItem_" + which + ".onClick(): " + isChecked);

						that.state[which] = isChecked;
					}
				}
			)
		;
	}


	@Override // called when OK (true) or Cancel (false) are pushed
	public void onDialogClosed(boolean positiveResult) {
		if(DEBUG)
			Log.d(TAG, "onDialogClosed(): " + positiveResult);

		if (positiveResult) {
			// only commit if my change listener says so 
			if (this.callChangeListener(this.state)) {
				String out = (String) b2ds(this.state);

				// check if the value is OK
				if (out == null || out == "") {
					Toast.makeText(
						this.getContext(),
						USING_DEFAULT,
						Toast.LENGTH_SHORT
					).show();
						
					out = (
						(this.defaultValue == null)
							? "0" // XXX: lol, hax
							: this.defaultValue
					);
				}
				
				if(DEBUG)
					Log.d(TAG, "onDialogClosed(): Saving: " + out);

				if (this.persistString(out)) {
					this.notifyChanged();
				} else {
					if(DEBUG)
						Log.d(TAG, "onDialogClosed(): Saving failed.");
				}
			}
		}
	}
}

