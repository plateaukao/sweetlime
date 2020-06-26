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
import net.toload.main.hd.Lime;
import net.toload.main.hd.R;

public class SetupImBackupRunnable implements Runnable {

    // Global
    private String mType;
    private SetupImFragment mFragment;

    private SetupImHandler mHandler;

    public SetupImBackupRunnable(SetupImFragment fragment, SetupImHandler handler, String type) {
        this.mHandler = handler;
        this.mType = type;
        this.mFragment = fragment;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void run() {

        mHandler.showProgress(true, this.mFragment.getResources().getString(R.string.setup_im_backup_message));
        // Preparing the file to be backup
        if (mType.equals(Lime.LOCAL) || mType.equals(Lime.GOOGLE) || mType.equals(Lime.DROPBOX)) {
            try {
                DBServer.backupDatabase();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        switch (mType) {
            default:
                DBServer.showNotificationMessage(mFragment.getResources().getString(R.string.l3_initial_backup_end));
                mHandler.cancelProgress();
                break;
        }
    }
}