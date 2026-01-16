# アプリアイコン生成ガイド

Play Consoleに必要な512x512pxアイコンを生成する方法です。

## 📋 必要なアイコン

- **サイズ**: 512x512px
- **フォーマット**: 32bit PNG（透過あり）
- **内容**: アプリを象徴するシンプルなアイコン

---

## 🎨 方法1: Android Asset Studio（推奨・最も簡単）

### 手順

1. **Android Asset Studioにアクセス**
   - https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

2. **アイコンを作成**
   - **Foreground**: 以下のいずれかを選択
     - Image: 既存の画像をアップロード
     - Clipart: 内蔵アイコンから選択（メモのアイコンなど）
     - Text: テキストからアイコン生成

   推奨: Clipart > "note" または "description" を選択

3. **スタイル設定**
   - **Background Color**: アプリのテーマカラー（例: #2196F3 青色）
   - **Foreground Scaling**: 80-90%
   - **Shape**: Circle または Square（お好みで）

4. **ダウンロード**
   - 「Download」をクリック
   - ZIPファイルをダウンロード

5. **512x512pxを抽出**
   - ZIPを解凍
   - `res/mipmap-xxxhdpi/ic_launcher.png` を探す
   - または、別途512x512pxのPNGを生成

---

## 🖼️ 方法2: 既存のSVGから生成

現在のプロジェクトには `ic_launcher_foreground.xml` があります。

### 手順

1. **Android Studioを開く**

2. **Vector Assetを開く**
   - プロジェクト内の `res/drawable/ic_launcher_foreground.xml` を右クリック
   - または、File > New > Image Asset

3. **512x512pxのPNGを生成**
   - Asset Type: Image
   - Path: ic_launcher_foreground.xml を選択
   - Trim: Yes
   - Resize: 512x512px
   - Padding: 10%

4. **エクスポート**
   - Next > Finish
   - `res/mipmap-xxxhdpi/` に生成されたPNGを使用

---

## 🎯 方法3: オンラインツールで作成

### ツール1: Canva（簡単）

1. https://www.canva.com にアクセス
2. カスタムサイズで 512x512px を作成
3. シンプルなメモ帳のアイコンをデザイン
4. PNG形式でダウンロード

### ツール2: Figma（デザイナー向け）

1. https://www.figma.com にアクセス
2. 512x512pxのフレームを作成
3. アイコンをデザイン
4. Export > PNG > 2x でエクスポート

---

## 🖌️ 簡易アイコンのアイデア

以下のようなシンプルなデザインを推奨：

### デザイン案1: メモ帳 + 時計
- 背景: 青色（#2196F3）
- 前景: 白いメモ帳のアイコン + 小さい時計マーク
- 意味: 「時間制限付きメモ」を表現

### デザイン案2: 消えるメモ
- 背景: グラデーション（青→透明）
- 前景: メモのアイコンが薄くなっていく
- 意味: 「消える」を視覚的に表現

### デザイン案3: シンプルなメモ
- 背景: 白
- 前景: シンプルな青いメモ帳
- 境界: 丸角の四角形

---

## 📁 保存場所

生成したアイコンを以下に保存：

```
C:\Users\User\Desktop\APP\一時保存メモ\store_listing\app_icon_512.png
```

---

## ✅ アイコン確認チェックリスト

- [ ] サイズが512x512pxである
- [ ] PNG形式（32bit）である
- [ ] 背景が透過またはシンプルな単色である
- [ ] アプリの機能を表現している
- [ ] 小さく表示しても認識できる
- [ ] Google Playのポリシーに違反していない（商標など）

---

## 🚀 次のステップ

アイコンを生成したら、Play Consoleの以下の場所にアップロードします：

1. Play Console > ストアの設定 > メインのストアの掲載情報
2. グラフィック アセット > アプリアイコン
3. 512x512pxのPNGをアップロード

---

## 💡 ヒント

- **シンプルが一番**: 複雑なデザインは小さいサイズで見えにくい
- **カラー**: アプリのテーマカラーと統一
- **テスト**: 実際に小さいサイズ（48x48px）で表示して確認
- **一貫性**: ウィジェットアイコンとデザインを統一すると良い

---

最終更新: 2026-01-16
