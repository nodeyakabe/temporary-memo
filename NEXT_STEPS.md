# 🎯 次のステップ - Google Play公開までのロードマップ

## ✅ 完了済みの作業

1. ✅ UI/文言の改善（7項目）
2. ✅ メールアドレス追記（yakabe@nodecast.jp）
3. ✅ プライバシーポリシーURL追記（4ファイル）
4. ✅ GitHubリポジトリ作成とプッシュ
5. ✅ GitHub Pages有効化
6. ✅ ドキュメント作成（ガイド類）

**プライバシーポリシー公開URL**:
```
https://nodeyakabe.github.io/temporary-memo/privacy_policy.html
```

---

## 📋 残りの作業（優先順位順）

### 🔴 最優先（今すぐできる）

#### 1. プライバシーポリシーURLの確認
**所要時間**: 2分

1. ブラウザで以下のURLを開く:
   ```
   https://nodeyakabe.github.io/temporary-memo/privacy_policy.html
   ```

2. 正しく表示されることを確認:
   - [ ] ページが読み込まれる
   - [ ] 日本語で表示されている
   - [ ] メールアドレス `yakabe@nodecast.jp` が表示されている

---

#### 2. アプリアイコン生成（512x512px）
**所要時間**: 15分
**参考**: `store_listing/ICON_GUIDE.md`

**推奨方法: Android Asset Studio**

1. https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html にアクセス

2. アイコンを作成:
   - **Foreground**: Clipart > "note" または "description" を選択
   - **Background Color**: `#2196F3`（青色）
   - **Shape**: Circle
   - **Foreground Scaling**: 80%

3. ダウンロード:
   - 「Download」をクリック
   - ZIPファイルを解凍

4. 512x512pxのPNGを保存:
   - `store_listing/app_icon_512.png` として保存

---

### 🟠 優先度高（Android Studioが必要）

#### 3. キーストアファイル生成
**所要時間**: 10分
**参考**: `KEYSTORE_SETUP.md`

**コマンド**:
```cmd
cd "C:\Users\User\Desktop\APP\一時保存メモ"

keytool -genkeypair -v -keystore temporary-memo-release.keystore -alias temporary-memo -keyalg RSA -keysize 2048 -validity 10000 -storepass hy37748810 -keypass hy37748810 -dname "CN=NatuB, OU=Development, O=Personal, L=Tokyo, ST=Tokyo, C=JP"
```

**確認**:
- [ ] `temporary-memo-release.keystore` ファイルが生成された
- [ ] キーストアのバックアップを作成（USBメモリ、クラウド等）

---

#### 4. スクリーンショット撮影
**所要時間**: 1-2時間
**参考**: `store_listing/SCREENSHOT_GUIDE.md`

**必要な枚数**: 最低2枚、推奨4-6枚

**撮影する画面**:
1. ✅ メモ一覧画面（色分けが見える状態）
2. ✅ メモ編集画面（期限設定スライダー）
3. ✅ 設定画面（生体認証スイッチと免責文言）
4. ✅ ロック画面（生体認証）

**手順**:
1. Android Studioでエミュレータを起動（Pixel 6, 1080x2400推奨）
2. デバッグビルドでアプリをインストール
3. サンプルメモを作成:
   - メモ1: 「明日の打ち合わせ資料」（期限3日）→ 緑
   - メモ2: 「スーパーで買うもの」（期限12時間）→ 黄色
   - メモ3: 「Wi-Fiパスワード」（期限2時間）→ オレンジ
4. スクリーンショットを撮影（エミュレータのカメラアイコン）
5. `store_listing/screenshots/` に保存

**注意**: FLAG_SECUREで撮影できない場合、一時的にコメントアウト（撮影後必ず戻す）

---

#### 5. AABファイルのビルド
**所要時間**: 30分
**参考**: `AAB_GENERATION.md`

**手順（Android Studio）**:
1. Android Studioでプロジェクトを開く
2. Build > Generate Signed Bundle / APK
3. Android App Bundle を選択 > Next
4. キーストア情報を入力:
   - Key store path: `temporary-memo-release.keystore`
   - Key store password: `hy37748810`
   - Key alias: `temporary-memo`
   - Key password: `hy37748810`
5. Next > release を選択 > Finish

**生成場所**:
```
app\build\outputs\bundle\release\app-release.aab
```

**同時に生成されるファイル**:
```
app\build\outputs\mapping\release\mapping.txt
```
（ProGuardマッピングファイル - Play Consoleにアップロード）

---

### 🟢 最終段階（Play Console）

#### 6. Play Consoleアカウント作成
**所要時間**: 30分
**費用**: $25（初回のみ、一生有効）

1. https://play.google.com/console/ にアクセス
2. Googleアカウントでログイン
3. デベロッパー登録
4. $25を支払い（クレジットカードまたはPayPal）

---

#### 7. アプリ作成とストア情報設定
**所要時間**: 2-3時間
**参考**: `FINAL_GUIDE.md`、`PLAY_CONSOLE_DATA_SAFETY.md`

**7-1. アプリ作成**
- アプリ名: 一時保存メモ
- デフォルト言語: 日本語（日本）
- 無料

**7-2. ストアの設定**
- 簡単な説明: `store_listing/short_description_ja.txt` をコピー
- 詳しい説明: `store_listing/description_ja.txt` をコピー
- アプリアイコン: 512x512pxをアップロード
- スクリーンショット: 撮影した画像をアップロード（最低2枚）

**7-3. プライバシーポリシー**
```
https://nodeyakabe.github.io/temporary-memo/privacy_policy.html
```

**7-4. データセーフティ**
`store_listing/PLAY_CONSOLE_DATA_SAFETY.md` を参照してコピペ

**7-5. コンテンツレーティング**
- カテゴリ: ユーティリティ
- すべて「いいえ」で回答
- 結果: 全年齢（3+）

**7-6. 内部テストトラック**
- AABファイルをアップロード
- mapping.txt をアップロード
- リリースノート: `store_listing/release_notes_ja.txt` をコピー
- テスター: 自分のメールアドレスを追加

**7-7. テスト実施**（1-2日）
- [ ] メモの作成・編集・削除
- [ ] 期限設定（1時間、24時間、7日）
- [ ] 生体認証ロック
- [ ] ウィジェット
- [ ] 期限到達時の自動削除

**7-8. 本番リリース**
- 配信国を選択（日本、アメリカなど）
- 同じAABとリリースノートを使用
- 審査に提出

---

## 📅 推奨スケジュール

### 今日（1-2時間）
1. ✅ プライバシーポリシーURL確認（2分）
2. ✅ アプリアイコン生成（15分）
3. ✅ キーストア生成（10分）

### 明日（2-3時間）
4. ✅ スクリーンショット撮影（1-2時間）
5. ✅ AABビルド（30分）

### 3日目（3-4時間）
6. ✅ Play Consoleアカウント作成（30分）
7. ✅ アプリ作成とストア情報設定（2-3時間）
8. ✅ 内部テストトラックにアップロード（30分）

### 4-5日目（テスト期間）
- 内部テストで動作確認

### 6日目
- 本番リリースに提出

### 7-10日目
- 審査待ち（通常1-3日）

---

## ✅ 各ステップのチェックリスト

### ステップ1: プライバシーポリシー
- [x] HTMLファイル作成
- [x] GitHubにプッシュ
- [x] GitHub Pages有効化
- [ ] URLが正しく表示されることを確認

### ステップ2: アプリアイコン
- [ ] 512x512pxのPNG生成
- [ ] `store_listing/app_icon_512.png` に保存

### ステップ3: キーストア
- [ ] keytoolコマンド実行
- [ ] .keystoreファイル生成確認
- [ ] バックアップ作成

### ステップ4: スクリーンショット
- [ ] エミュレータ起動
- [ ] サンプルメモ作成
- [ ] 4-6枚撮影
- [ ] `store_listing/screenshots/` に保存

### ステップ5: AABビルド
- [ ] Android Studioでビルド
- [ ] app-release.aab 生成
- [ ] mapping.txt 生成
- [ ] ファイルサイズ確認（4-8 MB）

### ステップ6: Play Console
- [ ] アカウント作成
- [ ] $25支払い

### ステップ7: ストア設定
- [ ] アプリ作成
- [ ] ストア情報入力
- [ ] プライバシーポリシーURL入力
- [ ] データセーフティ記入
- [ ] コンテンツレーティング取得
- [ ] 内部テストトラック設定
- [ ] AABアップロード
- [ ] テスター追加

### ステップ8: テスト
- [ ] 全機能動作確認
- [ ] 問題なし

### ステップ9: 本番提出
- [ ] 配信国選択
- [ ] 審査提出

---

## 🎯 現在地

**今ここ** → ステップ1完了、ステップ2に進む

次にやること:
1. プライバシーポリシーURLをブラウザで確認
2. アプリアイコンを生成（15分）
3. キーストアを生成（10分）

---

## 📞 サポート

質問やトラブルがあれば:
- FINAL_GUIDE.md - 完全ガイド
- 各種*_GUIDE.md - 詳細手順
- メール: yakabe@nodecast.jp

---

最終更新: 2026-01-16
作成者: NatuB

**頑張ってください！あと少しでリリースです！** 🎉
