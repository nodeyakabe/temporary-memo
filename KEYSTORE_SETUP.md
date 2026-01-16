# キーストア生成手順

## ⚠️ 重要な注意事項

**このキーストアファイルとパスワードは絶対に紛失しないでください！**
- 紛失すると、アプリの更新ができなくなります
- 再発行は不可能です
- 安全な場所（パスワードマネージャー、暗号化USBなど）に保管してください

## 1. キーストア生成コマンド

以下のコマンドを実行してください：

```bash
cd "C:\Users\User\Desktop\APP\一時保存メモ"

keytool -genkeypair -v ^
  -storetype PKCS12 ^
  -keystore temporary-memo-release.keystore ^
  -alias temporary-memo ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 10000
```

## 2. 入力項目

コマンド実行時に以下の情報を入力してください：

| 項目 | 入力値 |
|------|--------|
| キーストアのパスワード | `hy37748810` |
| パスワードの再入力 | `hy37748810` |
| 名前と姓 | `NatuB` |
| 組織単位 | `(Enter)` ※スキップ可 |
| 組織名 | `(Enter)` ※スキップ可 |
| 都市名 | `(Enter)` ※スキップ可 |
| 都道府県名 | `(Enter)` ※スキップ可 |
| 国コード | `JP` |
| 正しいか確認 | `yes` |
| キーのパスワード | `(Enter)` ※キーストアと同じパスワードを使用 |

## 3. 生成確認

コマンド実行後、以下のファイルが作成されていることを確認：

- ✅ `C:\Users\User\Desktop\APP\一時保存メモ\temporary-memo-release.keystore`

## 4. バックアップ

**必須**: キーストアファイルをバックアップしてください

推奨バックアップ先：
- クラウドストレージ（Google Drive、Dropbox等）の非公開フォルダ
- 暗号化USBメモリ
- パスワードマネージャーの添付ファイル機能

## 5. パスワード管理

以下の情報を安全な場所に記録してください：

```
アプリ名: 一時保存メモ
開発者名: NatuB
キーストアパスワード: hy37748810
キーエイリアス: temporary-memo
キーエイリアスパスワード: hy37748810
キーストアファイル: temporary-memo-release.keystore
作成日: 2026-01-16
```

## 6. セキュリティ確認

以下を確認してください：

- ✅ `keystore.properties` が `.gitignore` に含まれている
- ✅ `*.keystore` が `.gitignore` に含まれている
- ✅ キーストアファイルがGitにコミットされていない

## 7. 次のステップ

キーストア生成後、以下のコマンドでリリースビルドをテスト：

```bash
cd "C:\Users\User\Desktop\APP\一時保存メモ"
gradlew assembleRelease
```

成功すると、以下にAPKが生成されます：
`app\build\outputs\apk\release\app-release.apk`

---

## トラブルシューティング

### エラー: keytool がコマンドとして認識されない

**解決方法**: Java JDKのbinディレクトリにパスを通す

```bash
# Java JDKの場所を確認
where java

# パスの例
set PATH=%PATH%;C:\Program Files\Java\jdk-17\bin
```

### エラー: キーストアファイルが見つからない

**解決方法**: `keystore.properties` のパスを確認

```properties
# 相対パスの場合
storeFile=temporary-memo-release.keystore

# 絶対パスの場合
storeFile=C:/Users/User/Desktop/APP/一時保存メモ/temporary-memo-release.keystore
```
