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

import android.os.Handler;
import android.os.Message;

import net.toload.main.hd.data.Related;

import java.util.List;

public class ManageRelatedHandler extends Handler {

    private List<Related> relatedlist;
    private ManageRelatedFragment mFragment = null;

    public ManageRelatedHandler(ManageRelatedFragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        String action = msg.getData().getString("action");
        switch (action) {
            case "progress":
                mFragment.showProgress();
                break;
            case "add": {
                String pword = msg.getData().getString("pword");
                String cword = msg.getData().getString("cword");
                int score = msg.getData().getInt("score");
                mFragment.addRelated(pword, cword, score);
                break;
            }
            case "update": {
                int id = msg.getData().getInt("id");
                String pword = msg.getData().getString("pword");
                String cword = msg.getData().getString("cword");
                int score = msg.getData().getInt("score");
                mFragment.updateRelated(id, pword, cword, score);
                break;
            }
            case "remove": {
                int id = msg.getData().getInt("id");
                mFragment.removeRelated(id);
                break;
            }
            default:
                mFragment.updateGridView(this.relatedlist);
                break;
        }
    }

    public void showProgress() {
        Message m = new Message();
                m.getData().putString("action", "progress");
        this.sendMessageDelayed(m, 1);
    }

    public void updateGridView(List<Related> related) {
        this.relatedlist = related;
        Message m = new Message();
                m.getData().putString("action", "display");
        this.sendMessageDelayed(m, 1);
    }

    public void removeRelated(int id) {
        Message m = new Message();
        m.getData().putString("action", "remove");
        m.getData().putInt("id", id);
        this.sendMessageDelayed(m, 1);
    }

    public void updateRelated(int id, String pword, String cword, int score) {
        Message m = new Message();
        m.getData().putString("action", "update");
        m.getData().putInt("id", id);
        m.getData().putString("pword", pword);
        m.getData().putString("cword", cword);
        m.getData().putInt("score", score);
        this.sendMessageDelayed(m, 1);
    }

    public void addRelated(String pword, String cword, int score) {
        Message m = new Message();
        m.getData().putString("action", "add");
        m.getData().putString("pword", pword);
        m.getData().putString("cword", cword);
        m.getData().putInt("score", score);
        this.sendMessageDelayed(m, 1);
    }

}
