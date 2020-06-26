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

package net.toload.main.hd.data;

import java.util.LinkedList;
import java.util.List;

public class ChineseSymbol {
	public final static String chineseSymbols = "，|。|、|？|！|：|；|（|）|「|」|『|』|【|】|" +
			"／|＼|－|＿|＊|＆|︿|％|＄|＃|＠|～|｛|｝|［|］|＜|＞|＋|｜|‵|＂";	
	
	
	private static List<Mapping> mChineseSymbolMapping = new LinkedList<>();
	public static String getSymbol(char symbol){
	
		switch(symbol){
		case '.': return "。";	
		case ',': return "，";	
		case '/': return "／";	
		case '\\': return "＼";	
		case '=': return "＝";	
		case '-': return "－";	
		case '_': return "＿";	
		case '*': return "＊";	
		case '&': return "＆";	
		case '^': return "︿";	
		case '%': return "％";	
		case '$': return "＄";	
		case '#': return "＃";	
		case '@': return "＠";	
		case '~': return "～";	
		case '`': return "‵";	
		case '"': return "＂";	
		case '\'': return "’";	
		case '?': return "？";	
		case '}': return "｝";	
		case '{': return "｛";	
		case ']': return "］";	
		case '[': return "［";	
		case '<': return "＜";	
		case '>': return "＞";	
		case '+': return "＋";	
		case '(': return "（";	
		case ')': return "）";	
		case '|': return "｜";	
		case ':': return "：";	
		case ';': return "；";	
		case '1': return "１";	
		case '2': return "２";	
		case '3': return "３";	
		case '4': return "４";	
		case '5': return "５";	
		case '6': return "６";	
		case '7': return "７";	
		case '8': return "８";	
		case '9': return "９";	
		case '0': return "０";
		case '!': return "！";
		}  
		return null;
	}
	
	public static List<Mapping> getChineseSymoblList(){

		if(mChineseSymbolMapping.size()==0){
			String [] symArray =  chineseSymbols.split("\\|");
			
			for(String sym: symArray){
				Mapping mapping = new Mapping();
				mapping.setCode("");
				mapping.setWord(sym);
				mapping.setChinesePunctuationSymbolRecord();
				mChineseSymbolMapping.add(mapping);
				
			}
		}
		//Log.i("getChineseSymoblList()", "mChineseSymbolMapping.size()=" + mChineseSymbolMapping.size());
		return mChineseSymbolMapping;
		
	}
	
}
