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

import net.toload.main.hd.data.Related;
import net.toload.main.hd.limedb.LimeDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Art Hung on 2015/4/26.
 */
public class ManageRelatedRunnable implements Runnable{


    private ManageRelatedHandler handler;
    private Activity activity;
    private LimeDB datasource;
    private String query;
    private int maximum;
    private int offset;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public ManageRelatedRunnable(ManageRelatedHandler handler, Activity activity, String query, int maximum, int offset) {
        this.handler = handler;
        this.activity = activity;
        this.query = query;
        this.maximum = maximum;
        this.offset = offset;

        datasource = new LimeDB(this.activity);
    }

    public void run() {
        handler.showProgress();

        handler.updateGridView(loadRelated(query, maximum, offset));

        /*if(maximum > 0){
            handler.updateGridViewInitial(loadRelated(getMappingByCode, maximum));
        }else{
            handler.updateGridView(loadRelated(getMappingByCode, maximum));
        }*/
    }

    private List<Related> loadRelated(String pword, int maximum, int offset){
        List<Related> results = new ArrayList<>();

        results = datasource.loadRelated(pword, maximum, offset);
        /*try {
            datasource.open();
            datasource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        return results;
    }

}
