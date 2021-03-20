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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import net.toload.main.hd.Lime;
import net.toload.main.hd.MainActivity;
import net.toload.main.hd.R;
import net.toload.main.hd.SearchServer;
import net.toload.main.hd.data.Related;
import net.toload.main.hd.global.LIMEPreferenceManager;
import net.toload.main.hd.limedb.LimeDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class ManageRelatedFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private SearchServer SearchSrv = null;
    private GridView gridManageRelated;

    private Button btnManageRelatedAdd;
    private Button btnManageRelatedSearch;
    private Button btnManageRelatedPrevious;
    private Button btnManageRelatedNext;

    private EditText edtManageRelatedSearch;
    private TextView txtNavigationInfo;

    private List<Related> relatedlist;

    private int page = 0;
    private int total = 0;
    private boolean searchreset = false;

    private String prequery = "";

    private Activity activity;
    private ManageRelatedHandler handler;
    private ManageRelatedAdapter adapter;

    private Thread ManageRelatedthread;

    private LimeDB datasource;

    private ProgressDialog progress;
    private LIMEPreferenceManager mLIMEPref;

    // AD
    //private RelativeLayout adBannerLayout;
    //private VpadnBanner vpadnBanner = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ManageRelatedFragment newInstance(int sectionNumber) {
        ManageRelatedFragment fragment = new ManageRelatedFragment();
        Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ManageRelatedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_related, container, false);

        this.activity = this.getActivity();
        this.datasource = new LimeDB(this.activity);
        this.SearchSrv = new SearchServer(this.activity);

        this.handler = new ManageRelatedHandler(this);

        this.progress = new ProgressDialog(this.activity);
        this.progress.setCancelable(false);
        this.progress.setMessage(getResources().getString(R.string.manage_related_loading));
        mLIMEPref = new LIMEPreferenceManager(activity);

        this.gridManageRelated = (GridView) rootView.findViewById(R.id.gridManageRelated);
        this.gridManageRelated.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               // try {
                    //datasource.open();
                    Related w = datasource.getRelated(id);
                    //datasource.close();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    // Create and show the dialog.
                    ManageRelatedEditDialog dialog = ManageRelatedEditDialog.newInstance();
                    dialog.setHandler(handler, w);
                    dialog.show(ft, "editdialog");
                /*} catch (SQLException e) {
                    e.printStackTrace();
                }*/
            }
        });

        this.btnManageRelatedAdd = (Button) rootView.findViewById(R.id.btnManageRelatedAdd);
        this.btnManageRelatedAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ManageRelatedAddDialog dialog = ManageRelatedAddDialog.newInstance();
                dialog.setHandler(handler);
                dialog.show(ft, "adddialog");
            }
        });


        this.btnManageRelatedNext = (Button) rootView.findViewById(R.id.btnManageRelatedNext);
        this.btnManageRelatedNext.setEnabled(false);
        this.btnManageRelatedNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkrecord = Lime.IM_MANAGE_DISPLAY_AMOUNT * (page + 1);
                if (checkrecord < total) {
                    page++;
                }
                searchrelated();
                //updateGridView(relatedlist);
            }
        });
        this.btnManageRelatedPrevious = (Button) rootView.findViewById(R.id.btnManageRelatedPrevious);
        this.btnManageRelatedPrevious.setEnabled(false);
        this.btnManageRelatedPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page > 0) {
                    page--;
                }
                searchrelated();
                //updateGridView(relatedlist);
            }
        });

        this.edtManageRelatedSearch = (EditText) rootView.findViewById(R.id.edtManageRelatedSearch);
        this.edtManageRelatedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchreset = false;
                btnManageRelatedSearch.setText(getResources().getText(R.string.manage_related_search));
            }
        });
        this.edtManageRelatedSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtManageRelatedSearch.getWindowToken(), 0);
                }
            }
        });

        this.btnManageRelatedSearch = (Button) rootView.findViewById(R.id.btnManageRelatedSearch);
        this.btnManageRelatedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchreset) {
                    String query = edtManageRelatedSearch.getText().toString();
                    // hide the soft keyboard before search Jeremy 15,6,4
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtManageRelatedSearch.getWindowToken(), 0);
                    if (query != null && query.length() > 0 &&
                            (prequery == null || !prequery.equals(query) || !searchreset)) {
                        query = query.trim();
                        searchrelated(query);
                    }
                    searchreset = true;
                    btnManageRelatedSearch.setText(getResources().getText(R.string.manage_related_reset));
                } else {
                    total = 0;
                    searchrelated(null);
                    edtManageRelatedSearch.setText("");
                    searchreset = false;
                    btnManageRelatedSearch.setText(getResources().getText(R.string.manage_related_search));
                }
            }
        });

        this.txtNavigationInfo = (TextView) rootView.findViewById(R.id.txtNavigationInfo);

        searchrelated(null);

        return rootView;
    }

    public void searchrelated(){
        searchrelated(prequery);
    }

    public void searchrelated(String curquery){

        int offset = Lime.IM_MANAGE_DISPLAY_AMOUNT * page;

        if((curquery == null && total == 0) || curquery != prequery ){
            total = datasource.getRelatedSize(curquery);
            page = 0;
        }
        if(ManageRelatedthread != null && ManageRelatedthread.isAlive()){
            handler.removeCallbacks(ManageRelatedthread);
        }
        ManageRelatedthread = new Thread(new ManageRelatedRunnable(handler, activity, curquery,
                                                                Lime.IM_MANAGE_DISPLAY_AMOUNT, offset));
        ManageRelatedthread.start();
        prequery = curquery;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.ManageRelatedthread != null){
            this.handler.removeCallbacks(ManageRelatedthread);
        }
        if(this.progress.isShowing()){
            this.progress.cancel();
        }
        this.relatedlist = null;
        this.SearchSrv.initialCache();
    }

    public void showProgress(){
        if(!this.progress.isShowing()){
            this.progress.show();
        }
    }

    public void cancelProgress(){
        if(this.progress.isShowing()){
            this.progress.cancel();
        }
    }

    public void updateGridView(List<Related> relatedlist){

        this.relatedlist = relatedlist;

        int startrecord = Lime.IM_MANAGE_DISPLAY_AMOUNT * page;
        int endrecord = Lime.IM_MANAGE_DISPLAY_AMOUNT * (page + 1);

        if(page > 0){
            this.btnManageRelatedPrevious.setEnabled(true);
        }else{
            this.btnManageRelatedPrevious.setEnabled(false);
        }

        if(endrecord <= total){
            this.btnManageRelatedNext.setEnabled(true);
        }else{
            this.btnManageRelatedNext.setEnabled(false);
            endrecord = total;
        }

        if(total > 0){
            if(this.adapter == null){
                this.adapter = new ManageRelatedAdapter(this.activity, relatedlist);
                this.gridManageRelated.setAdapter(this.adapter);
            }else{
                this.adapter.setList(relatedlist);
                this.adapter.notifyDataSetChanged();
                this.gridManageRelated.setSelection(0);
            }
        }else{
            this.adapter.setList(new ArrayList());
            this.adapter.notifyDataSetChanged();
            this.gridManageRelated.setSelection(0);
            Toast.makeText(activity, R.string.no_search_result, Toast.LENGTH_SHORT).show();
        }

        String nav = "0";

        if(total > 0){
            nav = Lime.format(startrecord + 1) + "-" + Lime.format(endrecord);
            nav += " of " + Lime.format(total);
        }

        this.txtNavigationInfo.setText(nav);
        cancelProgress();

    }

    public void removeRelated(int id){

        // Remove from the temp list
        for(int i = 0 ; i < total ; i++){
           if(id== this.relatedlist.get(i).getId()){
               this.relatedlist.remove(i);
               break;
           }
        }

        // Remove from the database
        String removesql = "DELETE FROM " + Lime.DB_RELATED + " WHERE " + Lime.DB_COLUMN_ID + " = '" + id + "'";

        datasource.remove(removesql);
        /*try {
            datasource.open();
            datasource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        total--;
        searchrelated();
        //updateGridView(this.relatedlist);
    }

    public void addRelated(String pword, String cword, int score) {

        int hasRelatedCheck = datasource.hasRelated(pword, cword);
        if(hasRelatedCheck == 0){
            // Add to database
            Related obj = new Related();
            obj.setPword(pword);
            obj.setCword(cword);
            obj.setBasescore(score);

            String insertsql = Related.getInsertQuery(obj);

            datasource.insert(insertsql);

            total++;
            searchrelated();

        }else{
            if(hasRelatedCheck == 9999999){
                Toast.makeText(activity, R.string.manage_related_format_error, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, R.string.manage_related_duplicated, Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void updateRelated(int id, String pword, String cword, int score) {

            // remove from temp list
            for(int i = 0 ; i < total ; i++){
                if(id== this.relatedlist.get(i).getId()){
                    Related check = this.relatedlist.get(i);
                    check.setPword(pword);
                    check.setCword(cword);
                    check.setBasescore(score);
                    this.relatedlist.remove(i);
                    this.relatedlist.add(i, check);
                    break;
                }
            }

            // Update record in the database
            String updatesql = "UPDATE " + Lime.DB_RELATED + " SET ";
            updatesql += Lime.DB_RELATED_COLUMN_PWORD + " = \"" + Lime.formatSqlValue(pword) + "\", ";
            updatesql += Lime.DB_RELATED_COLUMN_CWORD + " = \"" + Lime.formatSqlValue(cword) + "\", ";
            updatesql += Lime.DB_RELATED_COLUMN_BASESCORE + " = \"" + score + "\" ";
            updatesql += " WHERE " + Lime.DB_RELATED_COLUMN_ID + " = \"" + id + "\"";

            datasource.update(updatesql);

            searchrelated();

    }

}