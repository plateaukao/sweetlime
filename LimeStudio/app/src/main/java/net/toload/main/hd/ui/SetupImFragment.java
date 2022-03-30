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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.util.Log;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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

    //Activate LIME IM
    Button btnSetupImSystemSettings;
    Button btnSetupImSystemIMPicker;

    // Custom Import
    Button btnSetupImImportStandard;
    Button btnSetupImImportRelated;

    // Default IME
    Button btnSetupImPhonetic;

    // Backup Restore
    Button btnSetupImBackupLocal;
    Button btnSetupImRestoreLocal;

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

        rootView = inflater.inflate(R.layout.fragment_setup_im, container, false);

        btnSetupImSystemSettings = (Button) rootView.findViewById(R.id.btnSetupImSystemSetting);
        btnSetupImSystemIMPicker = (Button) rootView.findViewById(R.id.btnSetupImSystemIMPicker);
        btnSetupImImportStandard = (Button) rootView.findViewById(R.id.btnSetupImImportStandard);
        btnSetupImImportRelated = (Button) rootView.findViewById(R.id.btnSetupImImportRelated);
        btnSetupImPhonetic = (Button) rootView.findViewById(R.id.btnSetupImPhonetic);

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
                    if(LIMEUtilities.isLIMEActive(getActivity().getApplicationContext())) {  //LIME is activated, also the active Keyboard, and write storage permission is grated
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

                        btnSetupImBackupLocal.setEnabled(true);
                        btnSetupImRestoreLocal.setEnabled(true);
                        btnSetupImImportStandard.setEnabled(true);
                        btnSetupImImportRelated.setEnabled(true);
                    }
                }else {
                    btnSetupImSystemSettings.setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.setup_im_system_settings_description).setVisibility(View.VISIBLE);
                    btnSetupImSystemIMPicker.setVisibility(View.GONE);
                    rootView.findViewById(R.id.setup_im_system_impicker_description).setVisibility(View.GONE);
                    rootView.findViewById(R.id.SetupImList).setVisibility(View.GONE);
                }

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
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == RESTORE_FILE_REQUEST_CODE) {
            Uri uri = data.getData();
            Log.i(TAG, "Uri: " + uri.toString());
            restorethread = new Thread(new SetupImRestoreRunnable(this, handler, getFilePathFromUri(uri)));
            restorethread.start();
        } else if (requestCode == BACKUP_FILE_REQUEST_CODE) {
            Uri treeUri = data.getData();
            if (data != null) {
                backupthread = new Thread(new SetupImBackupRunnable(this, handler, treeUri));
                backupthread.start();
            }
        }

    }

    public String getFilePathFromUri(Uri uri) {
        String fileName = Lime.DATABASE_BACKUP_NAME;
        File file = new File(this.getContext().getExternalCacheDir(), fileName);
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = getContext().getContentResolver().openInputStream(uri))
        {
            file.createNewFile();
            copyFile(inputStream, outputStream); //Simply reads input to output stream
            outputStream.flush();
        } catch (Exception exception) {
            // TODO
        }

        return file.getAbsolutePath();
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    private String getFilePath(Uri uri) {
        String filePath;
        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = this.getContext().getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = uri.getPath();
        }
        return filePath;
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
            //backupthread = new Thread(new SetupImBackupRunnable(this, handler, type));
            //backupthread.start();
            launchBackupFilePicker();
        }else if(action.equals(Lime.RESTORE)){
            if(restorethread != null && restorethread.isAlive()){
                handler.removeCallbacks(restorethread);
            }
            launchRestoreFilePicker();
        }
    }

    final static int BACKUP_FILE_REQUEST_CODE = 10421;
    private void launchBackupFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, BACKUP_FILE_REQUEST_CODE);
    }

    final static int RESTORE_FILE_REQUEST_CODE = 0421;
    private void launchRestoreFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        startActivityForResult(intent, RESTORE_FILE_REQUEST_CODE);
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
    }
}