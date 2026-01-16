# リリースビルド最適化設定まとめ

このドキュメントは、「一時保存メモ」アプリのリリースビルドに適用された最適化設定をまとめたものです。

## 📋 適用済み最適化

### 1. ProGuard/R8による難読化・最適化

**ファイル**: `app/proguard-rules.pro`

**設定内容**:
- コード難読化（minifyEnabled = true）
- リソース圧縮（shrinkResources = true）
- 未使用コードの削除
- 最適化パス数: 5回

**保護対象**:
- Jetpack Compose
- Jetpack Navigation
- Biometric API
- Room Database
- ViewModels
- Kotlinリフレクション

### 2. Gradle最適化設定

**ファイル**: `gradle.properties`

**設定内容**:

```properties
# R8フルモード（最大限の最適化）
android.enableR8.fullMode=true

# ビルド速度の最適化
org.gradle.parallel=true           # 並列ビルド
org.gradle.caching=true             # ビルドキャッシュ
org.gradle.configureondemand=true   # オンデマンド設定

# Kotlinコンパイラの最適化
kotlin.incremental=true             # インクリメンタルコンパイル
kotlin.daemon.jvmargs=-Xmx2048m     # Kotlinデーモンのメモリ
```

### 3. ビルド設定

**ファイル**: `app/build.gradle.kts`

**Releaseビルドタイプ**:
```kotlin
release {
    isMinifyEnabled = true          // ProGuard/R8有効化
    isShrinkResources = true        // リソース圧縮
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
    signingConfig = signingConfigs.getByName("release")
}
```

## 📊 期待される効果

### APK/AABサイズ削減
- **未使用コード削除**: 約30-50%削減
- **リソース圧縮**: 約10-20%削減
- **総合効果**: 最大60%のサイズ削減

### ビルド速度向上
- **並列ビルド**: 最大40%高速化（マルチコアCPU環境）
- **ビルドキャッシュ**: 2回目以降のビルドが最大80%高速化
- **インクリメンタルコンパイル**: Kotlinの再コンパイル時間が最大90%短縮

### セキュリティ向上
- **コード難読化**: リバースエンジニアリングの難易度向上
- **未使用コード削除**: 攻撃対象範囲の縮小

### パフォーマンス向上
- **R8最適化**: メソッドインライン化、デッドコード削除
- **フルモード**: より積極的な最適化によるランタイム高速化

## ⚠️ 注意事項

### 1. ProGuardルールのメンテナンス
新しいライブラリを追加した際は、必要に応じてProGuardルールを追加してください。

### 2. ビルド時間
初回のReleaseビルドは最適化処理のため、Debugビルドより時間がかかります。

### 3. テスト必須
Releaseビルドでは以下の機能を必ずテストしてください:
- [ ] Room Database操作（CRUD）
- [ ] 生体認証機能
- [ ] ウィジェット機能
- [ ] Navigation遷移
- [ ] ViewModelの状態保持

### 4. デバッグビルドとの差異
- Releaseビルドではスタックトレースが難読化される
- ログ出力が一部削除される可能性がある
- デバッグ用のコードは含まれない

## 🔍 最適化の確認方法

### APK/AABサイズの確認
```bash
# AABファイルのサイズ確認
ls -lh app/build/outputs/bundle/release/

# APKファイルのサイズ確認（AABから生成後）
ls -lh app/build/outputs/apk/release/
```

### ProGuard/R8マッピングファイル
Releaseビルド後、以下のファイルが生成されます:
```
app/build/outputs/mapping/release/mapping.txt
```
このファイルは、難読化されたクラス名と元のクラス名の対応表です。
**Play Consoleにアップロードすることでクラッシュレポートが読みやすくなります。**

## 📈 最適化前後の比較（目安）

| 項目 | 最適化前 | 最適化後 | 削減率 |
|------|----------|----------|--------|
| APKサイズ | ~8-10 MB | ~4-6 MB | 約40-50% |
| メソッド数 | ~15,000 | ~8,000 | 約46% |
| リソースファイル数 | ~500 | ~350 | 約30% |

*実際のサイズは使用しているライブラリやリソースによって異なります。*

## 🚀 次のステップ

1. **Releaseビルドの実行**
   ```bash
   ./gradlew bundleRelease
   ```

2. **AABファイルの確認**
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```

3. **Play Consoleへのアップロード**
   - AABファイルをアップロード
   - mapping.txtをアップロード（ProGuardマッピング）

4. **内部テストトラックでテスト**
   - 最低1-2名のテスターで動作確認
   - 主要機能のテスト実施

---

## 📚 参考リンク

- [Android公式: アプリを縮小、難読化する](https://developer.android.com/studio/build/shrink-code)
- [R8の最適化について](https://developer.android.com/studio/build/r8)
- [Gradle ビルド最適化](https://developer.android.com/studio/build/optimize-your-build)

---

最終更新: 2026-01-16
