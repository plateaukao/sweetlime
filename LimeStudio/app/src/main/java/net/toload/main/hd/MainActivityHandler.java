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

package net.toload.main.hd;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Art Hung on 2015/4/26.
 */
public class MainActivityHandler extends Handler {

    private MainActivity activity = null;

    public MainActivityHandler(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {

        String action = msg.getData().getString("action");
        String type = msg.getData().getString("type");

        if(action != null && action.equalsIgnoreCase("progress")){
            if(type != null){
                if(type.equalsIgnoreCase("show")){
                    activity.showProgress();;
                }else if(type.equalsIgnoreCase("cancel")){
                    activity.cancelProgress();
                }else if(type.equalsIgnoreCase("update")){
                    int value= msg.getData().getInt("value");
                    activity.updateProgress(value);
                }else if(type.equalsIgnoreCase("message")){
                    String message = msg.getData().getString("message");
                    activity.updateProgress(message);
                }
            }
        }else if(action != null && action.equalsIgnoreCase("toast")){
            String message = msg.getData().getString("message");
            int length = msg.getData().getInt("length");

            if(message != null){
                activity.showToastMessage(message, length);
            }else{
                activity.showToastMessage("Error", length);
            }

        }else if(action != null && action.equalsIgnoreCase("sharezip")){
            String filepath = msg.getData().getString("filepath");
            activity.shareTo(filepath, Lime.SHARE_TYPE_ZIP);
        }else if(action != null && action.equalsIgnoreCase("sharetxt")){
            String filepath = msg.getData().getString("filepath");
            activity.shareTo(filepath, Lime.SHARE_TYPE_TXT);
        }else if(action != null && action.equalsIgnoreCase("initialpreference")){
            activity.initialDefaultPreference();
        }
    }

    public void cancelProgress() {
        Message m = new Message();
        m.getData().putString("action", "progress");
        m.getData().putString("type", "cancel");
        this.sendMessageDelayed(m, 1);
    }

    public void showProgress() {
        Message m = new Message();
        m.getData().putString("action", "progress");
        m.getData().putString("type", "show");
        this.sendMessageDelayed(m, 1);
    }

    public void updateProgress(int value) {
        Message m = new Message();
        m.getData().putString("action", "progress");
        m.getData().putString("type", "update");
        m.getData().putInt("value", value);
        this.sendMessageDelayed(m, 1);
    }

    public void updateProgress(String message) {
        Message m = new Message();
        m.getData().putString("action", "progress");
        m.getData().putString("type", "message");
        m.getData().putString("message", message);
        this.sendMessageDelayed(m, 1);
    }

    public void showToastMessage(String message, int length){
        Message m = new Message();
                m.getData().putString("action", "toast");
                m.getData().putString("message", message);
                m.getData().putInt("length", length);
        this.sendMessageDelayed(m, 1);
    }

    public void shareTxtTo(String filepath){
        Message m = new Message();
        m.getData().putString("action", "sharetxt");
        m.getData().putString("filepath", filepath);
        this.sendMessageDelayed(m, 1);
    }

    public void shareZipTo(String filepath){
        Message m = new Message();
        m.getData().putString("action", "sharezip");
        m.getData().putString("filepath", filepath);
        this.sendMessageDelayed(m, 1);
    }

    public void initialDefaultPreference(){
        Message m = new Message();
        m.getData().putString("action", "initialpreference");
        this.sendMessageDelayed(m, 1000);
    }

    public void showMessageBoard() {
        Message m = new Message();
        m.getData().putString("action", "showmessageboard");
        this.sendMessageDelayed(m, 1);
    }
}
