# Android Studioインストールガイド

## 📥 ダウンロードとインストール

### 1. ダウンロード

1. **公式サイトにアクセス**
   ```
   https://developer.android.com/studio
   ```

2. **「Download Android Studio」をクリック**
   - Windows版が自動選択されます
   - ファイル名: `android-studio-XXXX.XX.XX-windows.exe` (約1GB)

3. **利用規約に同意**
   - チェックボックスにチェック
   - 「Download Android Studio」をクリック

---

### 2. インストール

1. **ダウンロードした.exeファイルを実行**
   - ダウンロードフォルダから実行
   - UACが表示されたら「はい」

2. **セットアップウィザード**

   **Welcome画面**:
   - 「Next」をクリック

   **Choose Components**:
   - ✅ Android Studio
   - ✅ Android Virtual Device
   - 両方チェック > 「Next」

   **Configuration Settings**:
   - インストール先: デフォルトのまま（通常 `C:\Program Files\Android\Android Studio`）
   - 「Next」

   **Choose Start Menu Folder**:
   - デフォルトのまま
   - 「Install」

3. **インストール待機**
   - 約5-10分かかります
   - コーヒーでも飲んで待ちましょう ☕

4. **完了**
   - 「Next」> 「Finish」

---

### 3. 初回起動と設定

1. **Android Studioを起動**
   - デスクトップまたはスタートメニューから起動

2. **Import Settings**
   - 「Do not import settings」を選択
   - 「OK」

3. **Data Sharing**
   - お好みで選択（「Don't send」でOK）

4. **Setup Wizard**

   **Install Type**:
   - 「Standard」を選択
   - 「Next」

   **Select UI Theme**:
   - お好みのテーマを選択（Light/Dark）
   - 「Next」

   **Verify Settings**:
   - そのまま「Next」

   **Downloading Components**:
   - Android SDKなどをダウンロード（約10-20分）
   - ☕ 再びコーヒータイム

5. **完了**
   - 「Finish」

---

## 🎯 プロジェクトを開く

### 1. プロジェクトを開く

1. **Welcome画面**
   - 「Open」をクリック

2. **フォルダを選択**
   ```
   C:\Users\User\Desktop\APP\一時保存メモ
   ```

3. **Trust Project**
   - 「Trust Project」をクリック

4. **Gradle Sync**
   - 自動的に開始されます（初回は5-10分）
   - 下部のステータスバーで進捗確認

---

## 🔑 キーストアとAABを生成

Gradle Syncが完了したら：

### ステップ1: Generate Signed Bundle

1. **メニュー**: Build > Generate Signed Bundle / APK

2. **Android App Bundle** を選択 > Next

3. **Create new...** をクリック

4. **キーストア情報を入力**:

   **Key store path**:
   ```
   C:\Users\User\Desktop\APP\一時保存メモ\temporary-memo-release.keystore
   ```

   **Password**: `hy37748810`
   **Confirm**: `hy37748810`

   **Alias**: `temporary-memo`
   **Password**: `hy37748810`
   **Confirm**: `hy37748810`

   **Validity (years)**: `25`

   **Certificate**:
   - First and Last Name: `NatuB`
   - Organizational Unit: `Development`
   - Organization: `Personal`
   - City or Locality: `Tokyo`
   - State or Province: `Tokyo`
   - Country Code: `JP`

5. **OK** をクリック

6. **Remember passwords** にチェック（任意）

7. **Next** をクリック

8. **Build Variants**: `release` を選択

9. **Signature Versions**: V1, V2 両方チェック

10. **Create** をクリック

---

## ✅ 完了確認

### 生成されたファイル

ビルドが完了すると、以下のファイルが生成されます：

1. **AABファイル**:
   ```
   app\build\outputs\bundle\release\app-release.aab
   ```
   → Play Consoleにアップロード

2. **キーストアファイル**:
   ```
   C:\Users\User\Desktop\APP\一時保存メモ\temporary-memo-release.keystore
   ```
   → **必ずバックアップ！**

3. **ProGuardマッピング**:
   ```
   app\build\outputs\mapping\release\mapping.txt
   ```
   → Play Consoleにアップロード

---

## 🚨 重要: キーストアのバックアップ

キーストアファイルを**必ず**バックアップしてください：

1. **USBメモリにコピー**
2. **クラウドストレージにアップロード**（Dropbox, Google Drive等）
3. **パスワードマネージャーに保存**

**紛失すると二度とアプリを更新できなくなります！**

---

## ⚠️ トラブルシューティング

### エラー1: Gradle Sync Failed

**対処法**:
1. File > Invalidate Caches > Invalidate and Restart
2. 再起動後、もう一度Gradle Syncを実行

### エラー2: SDK not found

**対処法**:
1. Tools > SDK Manager
2. Android SDK Location を確認
3. 最新のSDKをインストール

### エラー3: Build Failed

**対処法**:
1. Build > Clean Project
2. Build > Rebuild Project

---

## 🎯 次のステップ

AABファイルが生成できたら：

1. ✅ キーストアのバックアップ確認
2. ✅ AABファイルのサイズ確認（4-8 MB程度）
3. ✅ mapping.txt の存在確認
4. → Play Consoleでアカウント作成
5. → アプリ情報を入力
6. → AABとmapping.txtをアップロード
7. → 審査に提出

---

## 📞 サポート

- Android Studio公式ドキュメント: https://developer.android.com/studio/intro
- トラブルがあれば: yakabe@nodecast.jp

---

最終更新: 2026-01-16
