# AAB（Android App Bundle）生成ガイド

このドキュメントは、「一時保存メモ」アプリのAABファイルを生成する手順を説明します。

## 📦 AABとは？

**Android App Bundle (AAB)** は、Google Play Storeへアプリをアップロードするための新しい公式フォーマットです。

### APKとの違い

| 項目 | APK | AAB |
|------|-----|-----|
| 用途 | 直接インストール | Play Store配信専用 |
| サイズ | 大きい（全デバイス対応） | 小さい（デバイス別最適化） |
| 配信形式 | そのままインストール | Play Storeが最適化APKを生成 |
| 必須化 | いいえ | 2021年8月以降、新規アプリは必須 |

### AABのメリット

- **ダウンロードサイズ削減**: ユーザーのデバイスに必要なコードとリソースのみ配信
- **Dynamic Feature Modules**: オンデマンドで機能をダウンロード可能
- **自動最適化**: Google Playが各デバイス用に最適化されたAPKを生成

---

## 🔧 前提条件

### 必須ツール

1. **Android Studio** (推奨: 最新安定版)
   - ダウンロード: https://developer.android.com/studio

2. **Java Development Kit (JDK)** 17以上
   - Android Studioに同梱

3. **キーストアファイル**
   - `temporary-memo-release.keystore` が生成済みであること
   - `keystore.properties` が設定済みであること

### 確認項目

- [ ] `keystore.properties` に正しいパスワードとエイリアスが設定されている
- [ ] `app/build.gradle.kts` に署名設定が追加されている
- [ ] ProGuardルールが設定されている
- [ ] `gradle.properties` に最適化設定が追加されている

---

## 🚀 方法1: Android Studioを使用（推奨）

### ステップ1: プロジェクトを開く

1. Android Studioを起動
2. **File > Open** をクリック
3. `C:\Users\User\Desktop\APP\一時保存メモ` を選択
4. **OK** をクリック

### ステップ2: ビルドバリアントを選択

1. Android Studio下部の **Build Variants** タブをクリック
2. **release** を選択

### ステップ3: AABを生成

1. メニューから **Build > Generate Signed Bundle / APK** を選択
2. **Android App Bundle** を選択し、**Next** をクリック
3. キーストア情報を入力:
   - **Key store path**: `temporary-memo-release.keystore` のパスを選択
   - **Key store password**: `hy37748810`
   - **Key alias**: `temporary-memo`
   - **Key password**: `hy37748810`
4. **Next** をクリック
5. **Build Variants**: `release` を選択
6. **Signature Versions**: V1とV2の両方にチェック（推奨）
7. **Finish** をクリック

### ステップ4: 生成されたAABを確認

ビルド完了後、以下の場所にAABファイルが生成されます:

```
app/build/outputs/bundle/release/app-release.aab
```

成功メッセージが表示され、「Locate」リンクをクリックすると、ファイルが保存されたフォルダが開きます。

---

## 🖥️ 方法2: コマンドラインを使用

### Windowsの場合

#### ステップ1: プロジェクトディレクトリに移動

```cmd
cd C:\Users\User\Desktop\APP\一時保存メモ
```

#### ステップ2: Gradleラッパーを生成（初回のみ）

Android Studioでプロジェクトを一度開くと、自動的に `gradlew.bat` が生成されます。

または、手動で生成:

```cmd
gradle wrapper
```

#### ステップ3: AABをビルド

```cmd
gradlew.bat bundleRelease
```

### macOS/Linuxの場合

```bash
cd /path/to/一時保存メモ
./gradlew bundleRelease
```

### ビルドの確認

成功すると以下のメッセージが表示されます:

```
BUILD SUCCESSFUL in XXs
```

AABファイルの場所:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 🔍 AABファイルの検証

### サイズの確認

```cmd
dir app\build\outputs\bundle\release\app-release.aab
```

**期待されるサイズ**: 約4-8 MB

### bundletoolを使った検証（オプション）

bundletoolを使用すると、AABから実際のAPKサイズを確認できます。

#### bundletoolのダウンロード

```bash
# 最新版をダウンロード
curl -L -o bundletool.jar https://github.com/google/bundletool/releases/download/1.15.6/bundletool-all-1.15.6.jar
```

#### APKセットの生成

```bash
java -jar bundletool.jar build-apks ^
  --bundle=app\build\outputs\bundle\release\app-release.aab ^
  --output=app-release.apks ^
  --ks=temporary-memo-release.keystore ^
  --ks-pass=pass:hy37748810 ^
  --ks-key-alias=temporary-memo ^
  --key-pass=pass:hy37748810
```

#### デバイス別APKサイズの確認

```bash
java -jar bundletool.jar get-size total ^
  --apks=app-release.apks
```

---

## 📋 生成されるファイル一覧

AABビルド後、以下のファイルが生成されます:

### 1. AABファイル（本体）
```
app/build/outputs/bundle/release/app-release.aab
```
→ これをPlay Consoleにアップロード

### 2. ProGuardマッピングファイル
```
app/build/outputs/mapping/release/mapping.txt
```
→ クラッシュレポート解読用（Play Consoleにアップロード推奨）

### 3. リソースファイル
```
app/build/outputs/mapping/release/resources.txt
```
→ 最適化されたリソース一覧

### 4. ビルドログ
```
app/build/outputs/logs/manifest-merger-release-report.txt
```
→ マニフェストのマージ結果

---

## ⚠️ トラブルシューティング

### エラー1: キーストアが見つからない

```
Keystore file 'C:\Users\User\Desktop\APP\一時保存メモ\temporary-memo-release.keystore' not found
```

**解決策**: KEYSTORE_SETUP.mdの手順に従い、keytoolコマンドでキーストアを生成してください。

### エラー2: パスワードが間違っている

```
Failed to read key temporary-memo from store: Keystore was tampered with, or password was incorrect
```

**解決策**: `keystore.properties` のパスワードが正しいか確認してください。

### エラー3: ビルド失敗（一般）

```
FAILURE: Build failed with an exception.
```

**解決策**:
1. Android Studioで **Build > Clean Project** を実行
2. **Build > Rebuild Project** を実行
3. エラーメッセージを確認して修正

### エラー4: gradlewが見つからない

```
'gradlew' は、内部コマンドまたは外部コマンド、
操作可能なプログラムまたはバッチ ファイルとして認識されていません。
```

**解決策**: Android Studioでプロジェクトを開き、Gradleの同期を実行してください。これにより `gradlew.bat` が自動生成されます。

---

## 🎯 Play Consoleへのアップロード手順

### ステップ1: Play Consoleにログイン

https://play.google.com/console/

### ステップ2: アプリを作成（初回のみ）

1. **すべてのアプリ** > **アプリを作成** をクリック
2. アプリ名: **一時保存メモ**
3. デフォルトの言語: **日本語**
4. アプリまたはゲーム: **アプリ**
5. 無料または有料: **無料**

### ステップ3: 内部テストトラックを作成

1. 左メニューから **テスト > 内部テスト** を選択
2. **新しいリリースを作成** をクリック
3. **App Bundle をアップロード** をクリック
4. `app-release.aab` を選択
5. リリースノートを入力（`store_listing/release_notes_ja.txt` の内容）
6. **保存** をクリック

### ステップ4: ProGuardマッピングファイルをアップロード

1. アップロードしたAABの横にある **ProGuardマッピングファイル** をクリック
2. `app/build/outputs/mapping/release/mapping.txt` を選択
3. アップロード

### ステップ5: テスターを追加

1. **テスター** タブを選択
2. メールアドレスを追加（自分のメールアドレスでOK）
3. **保存** をクリック

### ステップ6: 内部テストを開始

1. **公開を開始** をクリック
2. 数分待つと、テスターにメールが送信されます

---

## 📊 AABサイズの目安

| コンポーネント | サイズ（概算） |
|----------------|----------------|
| Jetpack Compose | 2-3 MB |
| Room Database | 500 KB |
| Biometric API | 100 KB |
| Navigation | 200 KB |
| アプリコード | 500 KB |
| リソース | 500 KB |
| **合計** | **約4-5 MB** |

*R8による最適化で、最終的には3-4 MBまで削減される可能性があります。*

---

## 🚀 次のステップ

1. **AABファイルの生成** ← 今ココ
2. **Play Consoleへアップロード**
3. **内部テストで動作確認**
4. **ストアリストの作成**（説明文、スクリーンショット、アイコン）
5. **データセーフティフォームの記入**
6. **審査に提出**

---

## 📚 参考リンク

- [Android公式: App Bundleについて](https://developer.android.com/guide/app-bundle)
- [Play Console: AABのアップロード](https://support.google.com/googleplay/android-developer/answer/9859152)
- [bundletoolの使い方](https://developer.android.com/studio/command-line/bundletool)

---

最終更新: 2026-01-16
