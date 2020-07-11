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

import android.os.RemoteException;

import net.toload.main.hd.DBServer;
import net.toload.main.hd.R;
import net.toload.main.hd.SearchServer;

/**
 * Created by Art Hung on 2015/4/26.
 */
public class SetupImRestoreRunnable implements Runnable {

    // Global
    private SetupImFragment mFragment;
    private SearchServer SearchSrv;
    private String filePath;

    private SetupImHandler mHandler;

    public SetupImRestoreRunnable(SetupImFragment fragment, SetupImHandler handler, String filePath) {
        this.mHandler = handler;
        this.mFragment = fragment;
        this.SearchSrv = new SearchServer(this.mFragment.getActivity());
        this.filePath= filePath;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void run() {
        // Clean the cache before restore the data
        this.SearchSrv.initialCache();

        mHandler.showProgress(true, this.mFragment.getResources().getString(R.string.setup_im_restore_message));
        try {
            DBServer.restoreDatabase(filePath);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mHandler.cancelProgress();
    }
}

