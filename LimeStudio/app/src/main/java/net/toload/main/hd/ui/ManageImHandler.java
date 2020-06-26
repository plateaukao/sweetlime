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

import net.toload.main.hd.data.Word;

import java.util.List;


public class ManageImHandler extends Handler {

    private List<Word> wordlist;
    private ManageImFragment fragment = null;

    public ManageImHandler(ManageImFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        String action = msg.getData().getString("action");
        switch (action) {
            case "progress":
                fragment.showProgress();
                break;
            case "add": {
                String code = msg.getData().getString("code");
                int score = msg.getData().getInt("score");
                String word = msg.getData().getString("word");
                fragment.addWord(code, score, word);
                break;
            }
            case "update": {
                int id = msg.getData().getInt("id");
                String code = msg.getData().getString("code");
                int score = msg.getData().getInt("score");
                String word = msg.getData().getString("word");
                fragment.updateWord(id, code, score, word);
                break;
            }
            case "keyboard":
                String keyboard = msg.getData().getString("keyboard");
                fragment.updateKeyboard(keyboard);
                break;
            case "related": {
                String code = msg.getData().getString("code");
                //fragment.updateRelated(code);
                break;
            }
            case "remove": {
                int id = msg.getData().getInt("id");
                fragment.removeWord(id);
                break;
            }
            default:
                fragment.updateGridView(this.wordlist);
                break;
        }
    }

    public void showProgress() {
        Message m = new Message();
                m.getData().putString("action", "progress");
        this.sendMessageDelayed(m, 1);
    }

    public void updateGridView(List<Word> words) {
        this.wordlist = words;
        Message m = new Message();
                m.getData().putString("action", "display");
        this.sendMessageDelayed(m, 1);
    }

    public void removeWord(int id) {
        Message m = new Message();
        m.getData().putString("action", "remove");
        m.getData().putInt("id", id);
        this.sendMessageDelayed(m, 1);
    }

    public void updateWord(int id, String code, int score, String word) {
        Message m = new Message();
        m.getData().putString("action", "update");
        m.getData().putInt("id", id);
        m.getData().putString("code", code);
        m.getData().putInt("score", score);
        m.getData().putString("word", word);
        this.sendMessageDelayed(m, 1);
    }

    public void addWord(String code, int score, String word) {
        Message m = new Message();
        m.getData().putString("action", "add");
        m.getData().putString("code", code);
        m.getData().putInt("score", score);
        m.getData().putString("word", word);
        this.sendMessageDelayed(m, 1);
    }

    public void updateKeyboardButton(String keyboard) {
        Message m = new Message();
        m.getData().putString("action", "keyboard");
        m.getData().putString("keyboard", keyboard);
        this.sendMessageDelayed(m, 1);
    }


    public void updateRelated(String code) {
        Message m = new Message();
        m.getData().putString("action", "related");
        m.getData().putString("code", code);
        this.sendMessageDelayed(m, 1);
    }
}
