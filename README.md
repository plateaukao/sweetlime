## Sweet LIME 

(Lightweight Input Method Editor) 程式碼採用開源GPL 的方式授權，原始專案網站分別放置在 Github 及 中研院 OpenFoundary 上對外開放。

Github Project 網址 http://github.com/lime-ime/    http://www.limeime.org/

**目前的 LIME HD 已經很久沒有更新了。所以我把它 fork 一份來改成自己想要的型式。**

## 主要更動
1. 將 github 上的 repo (原本有 900 多 MB)，重新建立。不然只是要改個 code，得要下載接近 1GB 的 repo 到自己電腦上，會讓人卻步。
2. 拿掉廣告。雖然我知道支持原始開發者很重要，但既然我自己要貢獻，而且也有開放原始碼，所以我應該可以把廣告都拿掉吧。
3. 拿掉 Google Drive 和 Dropbox 的整合。雖然這功能很方便，但我就是不想因此整合了一堆 library。
4. 拿掉了一堆不知道當初為什麼要 import 的 local libraries。拿掉後似乎還是可以動，而且 apk size 從原本的 10MB 瘦身到 3.9MB！開心！
5. **改善 UX** 原先長按左下角的鍵盤鈕，會跳出功能選單；如果想要切換系統鍵盤的話，需要先長按鍵盤鈕，然後再點選系統鍵盤選項。這功能我常常需要使用到，所以希望可以一鍵就可以完成。所以我把長按鍵盤鈕直接對應到開啟系統鍵盤選單。至於原本的功能選單，則改成長按空白鍵。


