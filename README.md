# 往後不再維護！ 請前往安裝 [fcitx5-android](https://github.com/plateaukao/fcitx5-android/releases) 或是原始版本的 [fcitx5-android/fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)
# 並參考網路上的教學安裝所需的字碼表 (搜尋關鍵字: 無蝦米 fcitx)

* fcitx5-android 持續有在開發中，可以自訂輸入法及鍵盤的外觀，設定上也很容易。最重要的是：**在新版的 Android 中，外接硬體鍵盤時，可以正確地顯示輸入中的候選字視窗。**

## Sweet LIME 

(Lightweight Input Method Editor) 程式碼採用開源GPL 的方式授權，原始專案網站分別放置在 Github 及 中研院 OpenFoundary 上對外開放。

Github Project 網址 http://github.com/lime-ime/    http://www.limeime.org/

**目前的 LIME HD 已經很久沒有更新了。所以我把它 fork 一份來改成自己想要的型式。**

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-zh-hant.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/net.toload.main.hd)

## 主要更動
1. 將 Github 上的 repo (原本有 900 多 MB)，重新建立。不然只是要改個 code，得要下載接近 1GB 的 repo 到自己電腦上，會讓人卻步。
2. 拿掉廣告。
3. 拿掉 Google Drive 和 Dropbox 的整合。雖然這功能很方便，但我希望輸入法不要跟網路有任何連結。
4. 拿掉了一堆不知道當初為什麼要 import 的 local libraries。拿掉後似乎還是可以動，而且 apk size 從原本的 10MB 瘦身到 3.9MB！
5. **改善 UX** 原先長按左下角的鍵盤鈕，會跳出功能選單；如果想要切換系統鍵盤的話，需要先長按鍵盤鈕，然後再點選系統鍵盤選項。這功能我常常需要使用到，所以希望可以一鍵就可以完成。所以我把長按鍵盤鈕直接對應到開啟系統鍵盤選單。至於原本的功能選單，則改成長按空白鍵。
6. 更改鍵盤介面，讓它的淡色主題更適合電子書閱讀器使用。


<img width="350" alt="image" src="https://user-images.githubusercontent.com/4084738/119271272-75c30080-bc33-11eb-8a9c-bf6a40912844.png">
