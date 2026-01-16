# 一時保存メモ (Temporary Memo)

## 概要

期限付きメモアプリ。設定した期限に達すると自動的にメモが削除されます。

## 主な機能

- **期限付きメモ**: 1時間〜7日の期限を設定可能
- **自動削除**: 期限到達で完全削除（復元不可）
- **生体認証**: アプリ起動時に生体認証でロック
- **スクリーンショット防止**: FLAG_SECUREによる完全防止
- **ホーム画面ウィジェット**: 有効なメモ上位3件を表示
- **オフライン動作**: 通信なし・広告なし

## 技術スタック

- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose
- **データベース**: Room (SQLite)
- **アーキテクチャ**: MVVM + Repository パターン
- **最低APIレベル**: API 28 (Android 9+)

## プロジェクト構成

```
app/
├── src/main/
│   ├── java/com/temporary/memo/
│   │   ├── MainActivity.kt
│   │   ├── TemporaryMemoApp.kt
│   │   ├── data/              # データベース層
│   │   ├── repository/         # リポジトリ層
│   │   ├── viewmodel/          # ViewModel
│   │   ├── ui/                 # UI層
│   │   │   ├── screens/        # 画面
│   │   │   ├── theme/          # テーマ
│   │   │   └── navigation/     # ナビゲーション
│   │   ├── utils/              # ユーティリティ
│   │   └── widget/             # ウィジェット
│   ├── res/                    # リソース
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## ビルド方法

### 前提条件

- Android Studio Arctic Fox以降
- JDK 17
- Android SDK API 34

### ビルド手順

1. プロジェクトをAndroid Studioで開く
2. `Build` > `Make Project`
3. エミュレータまたは実機で実行

または、コマンドラインから:

```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease
```

## 重要な注意事項

### セキュリティについて

本アプリは**完全なセキュリティを保証するものではありません**。

- データは暗号化されていません
- 生体認証は端末標準の機能に依存します
- FLAG_SECUREによるスクリーンショット防止は完全ではありません

### データ保護について

- **機種変更時の引き継ぎ不可**: データは端末内のみに保存
- **削除されたメモは復元不可**: ゴミ箱機能なし
- **バックアップなし**: `android:allowBackup="false"`設定

## 免責事項

本アプリは端末内にのみ保存されるローカルメモアプリです。
本アプリは簡易的なメモ管理を目的としており、完全なセキュリティを保証するものではありません。

重要なデータの保存には使用しないでください。

## ライセンス

このプロジェクトは個人使用を目的としています。

## バージョン

- **現在のバージョン**: 1.0.0
- **最低Androidバージョン**: Android 9 (API 28)
- **対象Androidバージョン**: Android 14 (API 34)
