# GitHub Pages公開ガイド

プライバシーポリシーをGitHub Pagesで公開する手順です。

---

## 📋 前提条件

- GitHubアカウント: `nodeyakabe`
- メールアドレス: `yakabe@nodecast.jp`
- リポジトリ名: `temporary-memo` （推奨）

---

## 🚀 ステップ1: GitHubリポジトリ作成

### 1-1. GitHubにログイン

https://github.com にアクセスし、ログイン

### 1-2. 新しいリポジトリを作成

1. 右上の「+」> 「New repository」をクリック

2. リポジトリ設定:
   - **Repository name**: `temporary-memo`
   - **Description** (任意): `一時保存メモ - 期限付きメモアプリのプライバシーポリシー`
   - **Public** を選択（GitHub Pagesに必須）
   - **Initialize this repository with** は全てチェックなし

3. 「Create repository」をクリック

---

## 🖥️ ステップ2: ローカルからGitHubにプッシュ

### 2-1. コマンドプロンプトを開く

Windowsキー + R > `cmd` と入力 > Enter

### 2-2. プロジェクトディレクトリに移動

```cmd
cd "C:\Users\User\Desktop\APP\一時保存メモ"
```

### 2-3. Git初期化とコミット

以下のコマンドを**1行ずつ**実行してください：

```cmd
git init
```

```cmd
git config user.name "NatuB"
```

```cmd
git config user.email "yakabe@nodecast.jp"
```

```cmd
git add docs/privacy_policy.html PRIVACY_POLICY.md README.md
```

```cmd
git commit -m "Add privacy policy for Temporary Memo app"
```

```cmd
git branch -M main
```

```cmd
git remote add origin https://github.com/nodeyakabe/temporary-memo.git
```

```cmd
git push -u origin main
```

**注意**: 初回pushでは、GitHubのユーザー名とパスワード（またはPersonal Access Token）が必要です。

---

## 🔑 GitHubの認証（トークン方式）

パスワード認証が使えない場合、Personal Access Tokenを使用します。

### トークン作成手順

1. GitHub > 右上のプロフィール > Settings
2. 左メニュー最下部 > Developer settings
3. Personal access tokens > Tokens (classic)
4. 「Generate new token」> 「Generate new token (classic)」
5. 設定:
   - **Note**: `Temporary Memo Push Token`
   - **Expiration**: 90 days
   - **Select scopes**: `repo` にチェック
6. 「Generate token」をクリック
7. **トークンをコピー**（再表示されないので注意）

### pushコマンドでの使用

```cmd
git push -u origin main
```

- **Username**: `nodeyakabe`
- **Password**: コピーしたPersonal Access Token を貼り付け

---

## 🌐 ステップ3: GitHub Pagesを有効化

### 3-1. GitHubリポジトリページを開く

https://github.com/nodeyakabe/temporary-memo

### 3-2. Pagesを設定

1. 「Settings」タブをクリック

2. 左メニューの「Pages」をクリック

3. Build and deployment:
   - **Source**: Deploy from a branch
   - **Branch**: `main`
   - **Folder**: `/docs`

4. 「Save」をクリック

### 3-3. 公開URLを確認

数分後、以下のURLでアクセス可能になります：

```
https://nodeyakabe.github.io/temporary-memo/privacy_policy.html
```

確認方法:
1. Settings > Pages に戻る
2. 「Your site is live at ...」と表示される
3. リンクをクリックして確認

---

## ✅ 公開確認チェックリスト

- [ ] GitHubリポジトリが作成されている
- [ ] privacy_policy.html がプッシュされている
- [ ] GitHub Pagesが有効化されている
- [ ] URLでアクセスして正しく表示される
- [ ] Play ConsoleにプライバシーポリシーURLを入力

---

## 📝 プライバシーポリシーURL

公開URL:
```
https://nodeyakabe.github.io/temporary-memo/privacy_policy.html
```

このURLを以下の場所に入力してください：

1. ✅ `store_listing/description_ja.txt` - 既に追記済み
2. ✅ `store_listing/description_en.txt` - 既に追記済み
3. ✅ `docs/privacy_policy.html` - 既に追記済み
4. ✅ `PRIVACY_POLICY.md` - 既に追記済み
5. ⏳ Play Console > ポリシー > プライバシーポリシー

---

## 🔄 プライバシーポリシーを更新する場合

### 更新手順

1. `docs/privacy_policy.html` または `PRIVACY_POLICY.md` を編集

2. コマンドプロンプトで:

```cmd
cd "C:\Users\User\Desktop\APP\一時保存メモ"
git add docs/privacy_policy.html PRIVACY_POLICY.md
git commit -m "Update privacy policy"
git push
```

3. 数分後、GitHub Pagesに反映される

---

## 🎯 次のステップ

プライバシーポリシーが公開できたら：

1. ✅ Play ConsoleにURLを入力
2. ✅ データセーフティフォームを記入（PLAY_CONSOLE_DATA_SAFETY.md 参照）
3. ✅ スクリーンショットを撮影（SCREENSHOT_GUIDE.md 参照）
4. ✅ キーストアを生成（KEYSTORE_SETUP.md 参照）
5. ✅ AABファイルをビルド（AAB_GENERATION.md 参照）

---

## ⚠️ トラブルシューティング

### エラー1: "repository not found"

**原因**: リポジトリ名またはURLが間違っている

**解決策**:
```cmd
git remote -v
```
で現在の設定を確認し、間違っている場合:
```cmd
git remote remove origin
git remote add origin https://github.com/nodeyakabe/temporary-memo.git
```

---

### エラー2: "failed to push some refs"

**原因**: ローカルとリモートの履歴が異なる

**解決策**:
```cmd
git pull origin main --allow-unrelated-histories
git push -u origin main
```

---

### エラー3: GitHub Pagesが404エラー

**原因**: ファイルパスが間違っている、またはPages設定が未完了

**解決策**:
1. Settings > Pages で設定を確認
2. フォルダが `/docs` になっているか確認
3. `docs/privacy_policy.html` が存在するか確認
4. 5-10分待ってから再度アクセス

---

## 📞 サポート

質問がある場合:
- GitHub Docs: https://docs.github.com/pages
- メール: yakabe@nodecast.jp

---

最終更新: 2026-01-16
作成者: NatuB
