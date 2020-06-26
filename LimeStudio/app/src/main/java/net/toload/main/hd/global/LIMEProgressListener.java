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

package net.toload.main.hd.global;

/**
 * Created by Jeremy on 2015/5/23.
 */
public abstract class LIMEProgressListener {
    public LIMEProgressListener() {    }

    public abstract void onProgress(long var1, long var2, String status);

    public long progressInterval() {
        return 500L;
    }

    public void onError(int code, String source){
        return;
    }
    public void onPreExecute(){
        return;
    }
    public void onPostExecute(boolean success, String status, int code){
        return;
    }
    public void onStatusUpdate(String status){
        return;
    }
}
