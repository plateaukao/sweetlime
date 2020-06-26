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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.toload.main.hd.DBServer;
import net.toload.main.hd.Lime;
import net.toload.main.hd.R;
import net.toload.main.hd.data.Im;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.global.LIMEUtilities;
import net.toload.main.hd.limedb.LimeDB;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
/**
 * A placeholder fragment containing a simple rootView.
 */
public class SetupImFragment extends Fragment {

    // IM Log Tag
    private final String TAG = "SetupImFragment";

    // Debug Flag
    private final boolean DEBUG = false;

    // Basic
    private SetupImHandler handler;
    private Thread backupthread;
    private Thread restorethread;

    private ProgressDialog progress;

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    //Activate LIME IM
    Button btnSetupImSystemSettings;
    Button btnSetupImSystemIMPicker;
    Button btnSetupImGrantPermission;

    // Custom Import
    Button btnSetupImImportStandard;
    Button btnSetupImImportRelated;

    // Default IME
    Button btnSetupImPhonetic;
    Button btnSetupImCj;
    Button btnSetupImCj5;
    Button btnSetupImScj;
    Button btnSetupImEcj;
    Button btnSetupImDayi;
    Button btnSetupImEz;
    Button btnSetupImArray;
    Button btnSetupImArray10;
    Button btnSetupImHs;
    Button btnSetupImWb;
    Button btnSetupImPinyin;

    // Backup Restore
    Button btnSetupImBackupLocal;
    Button btnSetupImRestoreLocal;

    private ConnectivityManager connManager;

    private View rootView;
    private LimeDB datasource;
    private DBServer DBSrv = null;
    private Activity activity;
    private LIMEPreferenceManager mLIMEPref;

    List<Im> imlist;

    TextView txtVersion;

    // Vpon
    //private RelativeLayout adBannerLayout;
    // private VpadnBanner vpadnBanner = null;


    @Override
    public void onPause() {
        super.onPause();

        // Update IM pick up list items
        if(imlist != null && imlist.size() > 0){
            mLIMEPref.syncIMActivatedState(imlist);
        }

    }

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SetupImFragment newInstance(int sectionNumber) {
        SetupImFragment frg = new SetupImFragment();
        Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        frg.setArguments(args);
        return frg;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if(vpadnBanner != null){
            vpadnBanner.destroy();
            vpadnBanner = null;
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        initialbutton();
    }

    public void showProgress(boolean spinnerStyle, String message) {


        if (progress.isShowing()) progress.dismiss();

        progress = new ProgressDialog(activity);
        progress.setCancelable(false);
        progress.setProgressStyle(spinnerStyle ? ProgressDialog.STYLE_SPINNER : ProgressDialog.STYLE_HORIZONTAL);
        if(message!=null) progress.setMessage(message);
        if(!spinnerStyle) progress.setProgress(0);

        progress.show();

    }

    public void cancelProgress(){
        if(progress.isShowing()){
            progress.dismiss();
            handler.initialImButtons();
        }
    }

    public void setProgressIndeterminate(boolean flag){
        progress.setIndeterminate(flag);
    }

    public void updateProgress(int value){
          progress.setProgress(value);
    }

    public void updateProgress(String value){
        if(progress != null){
            progress.setMessage(value);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        datasource = new LimeDB(this.getActivity());

        handler = new SetupImHandler(this);

        activity = getActivity();

        progress = new ProgressDialog(activity);
        progress.setMax(100);
        progress.setCancelable(false);

        DBSrv = new DBServer(activity);
        mLIMEPref = new LIMEPreferenceManager(activity);

        connManager = (ConnectivityManager) SetupImFragment.this.activity.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_setup_im, container, false);

        btnSetupImSystemSettings = (Button) rootView.findViewById(R.id.btnSetupImSystemSetting);
        btnSetupImSystemIMPicker = (Button) rootView.findViewById(R.id.btnSetupImSystemIMPicker);
        btnSetupImGrantPermission = (Button) rootView.findViewById(R.id.btnSetupImGrantPermission);
        btnSetupImImportStandard = (Button) rootView.findViewById(R.id.btnSetupImImportStandard);
        btnSetupImImportRelated = (Button) rootView.findViewById(R.id.btnSetupImImportRelated);
        btnSetupImPhonetic = (Button) rootView.findViewById(R.id.btnSetupImPhonetic);
        btnSetupImCj = (Button) rootView.findViewById(R.id.btnSetupImCj);
        btnSetupImCj5 = (Button) rootView.findViewById(R.id.btnSetupImCj5);
        btnSetupImScj = (Button) rootView.findViewById(R.id.btnSetupImScj);
        btnSetupImEcj = (Button) rootView.findViewById(R.id.btnSetupImEcj);
        btnSetupImDayi = (Button) rootView.findViewById(R.id.btnSetupImDayi);
        btnSetupImEz = (Button) rootView.findViewById(R.id.btnSetupImEz);
        btnSetupImArray = (Button) rootView.findViewById(R.id.btnSetupImArray);
        btnSetupImArray10 = (Button) rootView.findViewById(R.id.btnSetupImArray10);
        btnSetupImHs = (Button) rootView.findViewById(R.id.btnSetupImHs);
        btnSetupImWb = (Button) rootView.findViewById(R.id.btnSetupImWb);
        btnSetupImPinyin = (Button) rootView.findViewById(R.id.btnSetupImPinyin);

        // Backup and Restore Setting
        btnSetupImBackupLocal = (Button) rootView.findViewById(R.id.btnSetupImBackupLocal);
        btnSetupImRestoreLocal = (Button) rootView.findViewById(R.id.btnSetupImRestoreLocal);

        btnSetupImBackupLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(Lime.BACKUP, Lime.LOCAL, getResources().getString(R.string.l3_initial_backup_confirm));
            }
        });

        btnSetupImRestoreLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(Lime.RESTORE, Lime.LOCAL, getResources().getString(R.string.l3_initial_restore_confirm));
            }
        });

        PackageInfo pInfo;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionstr = "v"+ pInfo.versionName + " - " + pInfo.versionCode;
            txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);
            txtVersion.setText(versionstr);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    public void initialbutton(){

        HashMap<String, String> check = new HashMap<>();

        // Load Menu Item
        //if(!mLIMEPref.getDatabaseOnHold()){
        if(!DBSrv.isDatabseOnHold()){
            try {
                //datasource.open();
                imlist = datasource.getIm(null, Lime.IM_TYPE_NAME);
                for(int i = 0; i < imlist.size() ; i++){
                    check.put(imlist.get(i).getCode(), imlist.get(i).getDesc());
                }

                // Update IM pick up list items
                mLIMEPref.syncIMActivatedState(imlist);

                Context ctx = getActivity().getApplicationContext();
                if(LIMEUtilities.isLIMEEnabled(getActivity().getApplicationContext())){  //LIME is activated in system
                    btnSetupImSystemSettings.setVisibility(View.GONE);
                    rootView.findViewById(R.id.setup_im_system_settings_description).setVisibility(View.GONE);
                    rootView.findViewById(R.id.SetupImList).setVisibility(View.VISIBLE);
                    if(LIMEUtilities.isLIMEActive(getActivity().getApplicationContext()) &&
                            ContextCompat.checkSelfPermission(this.getActivity(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {  //LIME is activated, also the active Keyboard, and write storage permission is grated
                        btnSetupImSystemIMPicker.setVisibility(View.GONE);
                        rootView.findViewById(R.id.Setup_Wizard).setVisibility(View.GONE);
                        btnSetupImBackupLocal.setEnabled(true);
                        btnSetupImRestoreLocal.setEnabled(true);
                        btnSetupImImportStandard.setEnabled(true);
                        btnSetupImImportRelated.setEnabled(true);
                    }
                    else  //LIME is activated, but not active keyboard
                    {
                        if(LIMEUtilities.isLIMEActive(getActivity().getApplicationContext()))
                        {
                            btnSetupImSystemIMPicker.setVisibility(View.GONE);
                            rootView.findViewById(R.id.setup_im_system_impicker_description).setVisibility(View.GONE);
                        }
                        else
                        {
                            btnSetupImSystemIMPicker.setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.setup_im_system_impicker_description).setVisibility(View.VISIBLE);
                        }
                        //Check permission for > API 23
                        if (ContextCompat.checkSelfPermission(this.getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        {
                            rootView.findViewById(R.id.setup_im_grant_permission).setVisibility((View.GONE));
                            btnSetupImGrantPermission.setVisibility(View.GONE);
                            btnSetupImBackupLocal.setEnabled(true);
                            btnSetupImRestoreLocal.setEnabled(true);
                            btnSetupImImportStandard.setEnabled(true);
                            btnSetupImImportRelated.setEnabled(true);
                        }
                        else
                        {
                            rootView.findViewById(R.id.setup_im_grant_permission).setVisibility((View.VISIBLE));
                            btnSetupImGrantPermission.setVisibility(View.VISIBLE);
                            btnSetupImBackupLocal.setEnabled(false);
                            btnSetupImRestoreLocal.setEnabled(false);
                            btnSetupImImportStandard.setEnabled(false);
                            btnSetupImImportRelated.setEnabled(false);
                        }

                    }
                }else {
                    btnSetupImSystemSettings.setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.setup_im_system_settings_description).setVisibility(View.VISIBLE);
                    btnSetupImSystemIMPicker.setVisibility(View.GONE);
                    rootView.findViewById(R.id.setup_im_grant_permission).setVisibility((View.GONE));
                    btnSetupImGrantPermission.setVisibility(View.GONE);
                    rootView.findViewById(R.id.setup_im_system_impicker_description).setVisibility(View.GONE);
                    rootView.findViewById(R.id.SetupImList).setVisibility(View.GONE);
                }



                btnSetupImGrantPermission.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
                btnSetupImSystemSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LIMEUtilities.showInputMethodSettingsPage(getActivity().getApplicationContext());
                    }
                });

                btnSetupImSystemIMPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LIMEUtilities.showInputMethodPicker(getActivity().getApplicationContext());
                        rootView.invalidate();
                    }
                });

                if(check.get(Lime.DB_TABLE_CUSTOM) != null){
                    btnSetupImImportStandard.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImImportStandard.setText(check.get(Lime.DB_TABLE_CUSTOM));
                    btnSetupImImportStandard.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImImportStandard.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImImportStandard.setTypeface(null, Typeface.BOLD);
                }



                btnSetupImImportStandard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_CUSTOM, handler);
                        dialog.show(ft, "loadimdialog");

                    }
                });

                // User can always load new related table ...
                btnSetupImImportRelated.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_RELATED, handler);
                        dialog.show(ft, "loadimdialog");

                    }
                });

                if(check.get(Lime.DB_TABLE_PHONETIC) != null){
                    btnSetupImPhonetic.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImPhonetic.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImPhonetic.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImPhonetic.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImPhonetic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_PHONETIC, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });


                if(check.get(Lime.DB_TABLE_CJ) != null){
                    btnSetupImCj.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImCj.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImCj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImCj.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImCj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_CJ, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });



                if(check.get(Lime.DB_TABLE_CJ5) != null){
                    btnSetupImCj5.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImCj5.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImCj5.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImCj5.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImCj5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_CJ5, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_SCJ) != null){
                    btnSetupImScj.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImScj.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImScj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImScj.setTypeface(null, Typeface.BOLD);
                }
                btnSetupImScj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_SCJ, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_ECJ) != null){
                    btnSetupImEcj.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImEcj.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImEcj.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImEcj.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImEcj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_ECJ, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_DAYI) != null){
                    btnSetupImDayi.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImDayi.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImDayi.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImDayi.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImDayi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_DAYI, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_EZ) != null){
                    btnSetupImEz.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImEz.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImEz.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImEz.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImEz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_EZ, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_ARRAY) != null){
                    btnSetupImArray.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImArray.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImArray.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImArray.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImArray.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_ARRAY, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_ARRAY10) != null){
                    btnSetupImArray10.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImArray10.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImArray10.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImArray10.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImArray10.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_ARRAY10, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });


                if(check.get(Lime.DB_TABLE_HS) != null){
                    btnSetupImHs.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImHs.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImHs.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImHs.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImHs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_HS, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_WB) != null){
                    btnSetupImWb.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImWb.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImWb.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImWb.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImWb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_WB, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });

                if(check.get(Lime.DB_TABLE_PINYIN) != null){
                    btnSetupImPinyin.setAlpha(Lime.HALF_ALPHA_VALUE);
                    btnSetupImPinyin.setTypeface(null, Typeface.ITALIC);
                }else {
                    btnSetupImPinyin.setAlpha(Lime.NORMAL_ALPHA_VALUE);
                    btnSetupImPinyin.setTypeface(null, Typeface.BOLD);
                }

                btnSetupImPinyin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        SetupImLoadDialog dialog = SetupImLoadDialog.newInstance(Lime.DB_TABLE_PINYIN, handler);
                        dialog.show(ft, "loadimdialog");
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    public void showAlertDialog(final String action, final String type, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(action != null){
                            if(action.equalsIgnoreCase(Lime.BACKUP)) {

                                if(type.equalsIgnoreCase(Lime.LOCAL)){
                                    backupLocalDrive();
                                }

                            }else if(action.equalsIgnoreCase(Lime.RESTORE)){

                                if(type.equalsIgnoreCase(Lime.LOCAL)){
                                    restoreLocalDrive();
                                }
                            }
                        }
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == Lime.PAYMENT_REQUEST_CODE) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            //int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            //String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            this.getActivity();
            if (resultCode == Activity.RESULT_OK) {
                mLIMEPref.setParameter(Lime.PAYMENT_FLAG, true);
                showToastMessage(getResources().getString(R.string.payment_service_success), Toast.LENGTH_LONG);
                //Log.i("LIME", "purchasing complete " + new Date() + " / " + purchaseData);
            }
        }

    }


    public void backupLocalDrive(){
        initialThreadTask(Lime.BACKUP, Lime.LOCAL);
    }

    public void restoreLocalDrive(){
        initialThreadTask(Lime.RESTORE, Lime.LOCAL);
    }

    public void initialThreadTask(String action, String type) {

        // Default Setting
        mLIMEPref.setParameter("dbtarget", Lime.DEVICE);

        if (action.equals(Lime.BACKUP)) {
            if(backupthread != null && backupthread.isAlive()){
                handler.removeCallbacks(backupthread);
            }
            backupthread = new Thread(new SetupImBackupRunnable(this, handler, type));
            backupthread.start();
        }else if(action.equals(Lime.RESTORE)){
            if(restorethread != null && restorethread.isAlive()){
                handler.removeCallbacks(restorethread);
            }
            restorethread = new Thread(new SetupImRestoreRunnable(this, handler, type));
            restorethread.start();
        }
    }

    public void showToastMessage(String msg, int length) {
        Toast toast = Toast.makeText(activity, msg, length);
        toast.show();
    }

    public void updateCustomButton() {
        btnSetupImImportStandard.setText(getResources().getString(R.string.setup_im_load_standard));
    }

    public void resetImTable(String imtable, boolean backuplearning){
        try {
            if(backuplearning){
                datasource.backupUserRecords(imtable);
            }
            DBSrv.resetMapping(imtable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void finishProgress(final String imtype) {

        cancelProgress();

        /*boolean check = datasource.checkBackuptable(imtype);
        if(check){

            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle(activity.getResources().getString(R.string.setup_im_restore_learning_data));
            alertDialog.setMessage(activity.getResources().getString(R.string.setup_im_restore_learning_data_message));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getResources().getString(R.string.dialog_confirm),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            showProgress(true, activity.getResources().getString(R.string.setup_im_restore_learning_data));

                            new Thread() {

                                @Override
                                public void run() {
                                    datasource.restoreUserRecords(imtype);
                                    cancelProgress();
                                }
                            }.start();
                        }
                    });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getResources().getString(R.string.dialog_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else{
            handler.cancelProgress();
        }*/
    }
}