# Android開発ノウハウ集

**作成日**: 2026-01-17
**プロジェクト**: ポイメモ（期限付きメモアプリ）
**技術スタック**: Kotlin, Jetpack Compose, Room, MVVM, Glance

---

## 目次

### 基礎編
1. [環境構築](#1-環境構築)
2. [ビルド・署名](#2-ビルド署名)
3. [プロジェクト構成](#3-プロジェクト構成)

### 実装編
4. [Jetpack Compose基礎](#4-jetpack-compose基礎)
5. [MVVM + Room Database](#5-mvvm--room-database)
6. [ViewModel と State管理](#6-viewmodel-と-state管理)
7. [アニメーション実装](#7-アニメーション実装)
8. [ウィジェット（Glance）](#8-ウィジェットglance)

### 品質編
9. [パフォーマンス最適化](#9-パフォーマンス最適化)
10. [テストの書き方](#10-テストの書き方)
11. [セキュリティ](#11-セキュリティ)
12. [ProGuard設定](#12-proguard設定)

### リリース編
13. [リリース準備](#13-リリース準備)
14. [トラブルシューティング](#14-トラブルシューティング)

### 開発プロセス編
15. [Claude Codeを使った開発](#15-claude-codeを使った開発)
16. [UIデザインのコツ](#16-uiデザインのコツ)

---

## 1. 環境構築

### 1.1 必要なツール

| ツール | バージョン | 用途 |
|--------|-----------|------|
| Android Studio | Hedgehog以降 | IDE |
| JDK | 17以上 | ビルド |
| Gradle | 8.4以上 | ビルドツール |
| Kotlin | 1.9.20以上 | 言語 |

### 1.2 gradle.propertiesの重要設定

```properties
# 日本語パス対応（必須！Windowsで日本語ユーザー名の場合）
android.overridePathCheck=true

# JVMメモリ設定（大きいプロジェクトでは増やす）
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# ビルド高速化
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configuration-cache=true

# JDK自動ダウンロード無効（互換性問題回避）
org.gradle.java.installations.auto-download=false

# AndroidXを使用（新規プロジェクトはデフォルトでtrue）
android.useAndroidX=true
```

### 1.3 JAVA_HOME設定

Android StudioのJBR（JetBrains Runtime）を使うのが最も安定:

```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Windows コマンドプロンプト
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

# Mac/Linux
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

**keytoolの場所**:
```
Windows: C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe
Mac: /Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool
```

### 1.4 よくある初期設定ミス

| 問題 | 原因 | 解決策 |
|------|------|--------|
| Gradleビルドが遅い | キャッシュ未設定 | `org.gradle.caching=true`を追加 |
| メモリ不足エラー | JVM設定不足 | `-Xmx2048m`以上に設定 |
| 日本語パスエラー | パスチェック | `android.overridePathCheck=true` |

---

## 2. ビルド・署名

### 2.1 ビルドコマンド早見表

```bash
# クリーンビルド
./gradlew clean

# デバッグAPK
./gradlew assembleDebug

# リリースAPK
./gradlew assembleRelease

# リリースAAB（Play Store用）
./gradlew bundleRelease

# 依存関係ツリー表示
./gradlew app:dependencies --configuration releaseRuntimeClasspath

# ビルドキャッシュクリア
./gradlew cleanBuildCache
```

### 2.2 生成ファイルの場所

| ファイル | パス |
|----------|------|
| AAB | `app/build/outputs/bundle/release/app-release.aab` |
| APK | `app/build/outputs/apk/release/app-release.apk` |
| マッピング | `app/build/outputs/mapping/release/mapping.txt` |
| ログ | `app/build/outputs/logs/` |

### 2.3 キーストア生成（初回のみ）

```bash
# Windows
"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" ^
  -genkeypair -v ^
  -keystore myapp-release.keystore ^
  -alias myapp ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 10000 ^
  -storepass YourSecurePassword ^
  -keypass YourSecurePassword ^
  -dname "CN=Your Name, OU=Development, O=Personal, L=Tokyo, ST=Tokyo, C=JP"
```

**重要**:
- キーストアは**絶対に紛失しないこと**（更新版をPlay Storeに出せなくなる）
- パスワードは安全な場所に保管
- `.gitignore`に追加してリポジトリに含めない

### 2.4 keystore.properties

```properties
storeFile=myapp-release.keystore
storePassword=YourSecurePassword
keyAlias=myapp
keyPassword=YourSecurePassword
```

**必ず`.gitignore`に追加**:
```gitignore
keystore.properties
*.keystore
*.jks
```

### 2.5 build.gradle.kts署名設定

```kotlin
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)  // Room用
}

// キーストア読み込み
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.example.myapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file("../${keystoreProperties["storeFile"]}")
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"  // デバッグ版を別アプリとして扱う
        }
        release {
            isMinifyEnabled = true       // コード圧縮
            isShrinkResources = true     // 未使用リソース削除
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true  // BuildConfig生成
    }
}
```

---

## 3. プロジェクト構成

### 3.1 推奨ディレクトリ構造

```
app/src/main/java/com/example/myapp/
├── MainActivity.kt              # エントリーポイント
├── MyApplication.kt             # Application クラス
│
├── data/                        # データ層
│   ├── database/
│   │   ├── AppDatabase.kt       # Room Database
│   │   ├── entity/              # エンティティ（テーブル定義）
│   │   │   └── MemoEntity.kt
│   │   └── dao/                 # DAO（データアクセス）
│   │       └── MemoDao.kt
│   ├── repository/              # リポジトリ（データ取得の抽象化）
│   │   └── MemoRepository.kt
│   └── datastore/               # DataStore（設定保存）
│       └── SettingsDataStore.kt
│
├── domain/                      # ドメイン層（ビジネスロジック）
│   ├── model/                   # ドメインモデル
│   │   └── Memo.kt
│   └── usecase/                 # ユースケース（オプション）
│       └── GetMemosUseCase.kt
│
├── ui/                          # UI層
│   ├── theme/                   # テーマ
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── components/              # 共通コンポーネント
│   │   ├── AppButton.kt
│   │   └── AppCard.kt
│   ├── navigation/              # ナビゲーション
│   │   └── AppNavGraph.kt
│   └── screens/                 # 画面
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       └── settings/
│           ├── SettingsScreen.kt
│           └── SettingsViewModel.kt
│
├── widget/                      # ウィジェット（Glance）
│   ├── MyAppWidget.kt
│   └── MyAppWidgetReceiver.kt
│
└── util/                        # ユーティリティ
    ├── DateUtils.kt
    └── Extensions.kt
```

### 3.2 なぜこの構成にするのか

| 層 | 役割 | 理由 |
|---|------|------|
| **data** | データの取得・保存 | DBやAPIの変更がUIに影響しない |
| **domain** | ビジネスロジック | テストしやすい、再利用可能 |
| **ui** | 画面表示 | Composeに集中できる |

**シンプルなアプリの場合**:
domainレイヤーは省略してもOK。Repository → ViewModelの直接参照で十分。

---

## 4. Jetpack Compose基礎

### 4.1 基本的な画面構成

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),  // または viewModel()
    onNavigateToDetail: (Long) -> Unit
) {
    // StateをViewModelから取得
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ホーム") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addItem() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    ) { paddingValues ->
        // メインコンテンツ
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(uiState.error!!)
            uiState.items.isEmpty() -> EmptyState()
            else -> ItemList(
                items = uiState.items,
                onItemClick = onNavigateToDetail,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
```

### 4.2 角丸サイズの統一（ビジネスライク + 柔らかさ）

```kotlin
// Constants.kt または Theme.kt に定義
object AppShapes {
    val Small = 8.dp   // 小さいボタン、チップ
    val Medium = 12.dp // カード、テキストフィールド、ボタン
    val Large = 16.dp  // FAB、ダイアログ、ボトムシート
    val ExtraLarge = 24.dp // モーダル
}

// 使用例
Card(
    shape = RoundedCornerShape(AppShapes.Medium),
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
) {
    // ...
}
```

| コンポーネント | 角丸 | 理由 |
|---------------|------|------|
| カード | 12dp | 柔らかさと視認性のバランス |
| ボタン | 12dp | カードと統一感 |
| FAB | 16dp | アクセントとして少し大きめ |
| ダイアログ | 16dp | 重要なUIとして目立たせる |
| テキストフィールド | 12dp | 入力エリアの視認性 |

### 4.3 パディング・マージンの統一

```kotlin
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// 使用例
Column(
    modifier = Modifier.padding(horizontal = AppSpacing.lg),  // 16dp
    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm) // 8dp
) {
    // ...
}
```

| 用途 | サイズ |
|------|-------|
| 画面端 | 16dp〜20dp |
| カード内 | 16dp〜20dp |
| アイテム間 | 8dp〜12dp |
| セクション間 | 24dp〜32dp |
| アイコンとテキスト間 | 8dp |

### 4.4 Modifier拡張でコードを簡潔に

```kotlin
// Extensions.kt
fun Modifier.cardStyle() = this
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 8.dp)

fun Modifier.screenPadding() = this
    .fillMaxSize()
    .padding(horizontal = 16.dp)

// 使用例
Card(
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.cardStyle()
) {
    // ...
}
```

### 4.5 よく使うComposable早見表

```kotlin
// 縦スクロールリスト
LazyColumn(
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(items, key = { it.id }) { item ->
        ItemCard(item)
    }
}

// 横スクロールリスト
LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(items) { item -> Chip(item) }
}

// グリッド
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(items) { item -> GridItem(item) }
}

// 条件表示
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
) {
    Content()
}

// 遅延読み込み
var data by remember { mutableStateOf<Data?>(null) }
LaunchedEffect(Unit) {
    data = repository.loadData()
}
data?.let { Content(it) } ?: LoadingIndicator()
```

---

## 5. MVVM + Room Database

### 5.1 Entity（テーブル定義）

```kotlin
@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "priority")
    val priority: Int = 0  // 0: Low, 1: Medium, 2: High
)
```

**ポイント**:
- `@PrimaryKey(autoGenerate = true)`: IDを自動採番
- `@ColumnInfo`: カラム名を明示（リファクタリング時の安全性）
- デフォルト値を設定しておくと便利

### 5.2 DAO（データアクセス）

```kotlin
@Dao
interface MemoDao {
    // 全件取得（Flow でリアクティブに監視）
    @Query("SELECT * FROM memos ORDER BY created_at DESC")
    fun getAllMemos(): Flow<List<MemoEntity>>

    // 条件付き取得
    @Query("SELECT * FROM memos WHERE is_completed = :completed ORDER BY due_date ASC")
    fun getMemosByStatus(completed: Boolean): Flow<List<MemoEntity>>

    // 1件取得
    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): MemoEntity?

    // 検索
    @Query("SELECT * FROM memos WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchMemos(query: String): Flow<List<MemoEntity>>

    // 挿入（IDを返す）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity): Long

    // 更新
    @Update
    suspend fun update(memo: MemoEntity)

    // 削除
    @Delete
    suspend fun delete(memo: MemoEntity)

    // ID指定削除
    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 複数削除
    @Query("DELETE FROM memos WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    // 全削除
    @Query("DELETE FROM memos")
    suspend fun deleteAll()
}
```

**ポイント**:
- `Flow<List<T>>`: データ変更を自動で通知（LiveDataより推奨）
- `suspend`: コルーチンで非同期実行
- `OnConflictStrategy.REPLACE`: 同じIDなら上書き

### 5.3 Database

```kotlin
@Database(
    entities = [MemoEntity::class],
    version = 1,
    exportSchema = true  // マイグレーション用にスキーマ出力
)
@TypeConverters(Converters::class)  // カスタム型変換が必要な場合
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration()  // 開発中のみ使用
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}

// カスタム型変換（例: List<String> をJSONで保存）
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}
```

### 5.4 Repository

```kotlin
class MemoRepository(private val memoDao: MemoDao) {

    val allMemos: Flow<List<MemoEntity>> = memoDao.getAllMemos()

    fun getMemosByStatus(completed: Boolean): Flow<List<MemoEntity>> {
        return memoDao.getMemosByStatus(completed)
    }

    fun searchMemos(query: String): Flow<List<MemoEntity>> {
        return memoDao.searchMemos(query)
    }

    suspend fun getMemoById(id: Long): MemoEntity? {
        return memoDao.getMemoById(id)
    }

    suspend fun insert(memo: MemoEntity): Long {
        return memoDao.insert(memo)
    }

    suspend fun update(memo: MemoEntity) {
        memoDao.update(memo)
    }

    suspend fun delete(memo: MemoEntity) {
        memoDao.delete(memo)
    }

    suspend fun deleteById(id: Long) {
        memoDao.deleteById(id)
    }
}
```

### 5.5 マイグレーション（バージョンアップ時）

```kotlin
// バージョン1 → 2: カラム追加
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE memos ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
    }
}

// Database に適用
Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
    .addMigrations(MIGRATION_1_2)
    .build()
```

**注意**: 本番アプリでは `fallbackToDestructiveMigration()` を使わない（データが消える）

---

## 6. ViewModel と State管理

### 6.1 基本的なViewModel

```kotlin
class HomeViewModel(
    private val repository: MemoRepository
) : ViewModel() {

    // UI State（画面の状態を1つにまとめる）
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMemos()
    }

    private fun loadMemos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.allMemos.collect { memos ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            memos = memos,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "エラーが発生しました"
                    )
                }
            }
        }
    }

    fun addMemo(title: String, content: String) {
        viewModelScope.launch {
            repository.insert(
                MemoEntity(title = title, content = content)
            )
        }
    }

    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.delete(memo)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// UI State データクラス
data class HomeUiState(
    val isLoading: Boolean = false,
    val memos: List<MemoEntity> = emptyList(),
    val error: String? = null,
    val searchQuery: String = ""
)
```

### 6.2 ViewModelFactory（Hilt不使用の場合）

```kotlin
class HomeViewModelFactory(
    private val repository: MemoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Composableでの使用
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            MemoRepository(AppDatabase.getInstance(LocalContext.current).memoDao())
        )
    )
) {
    // ...
}
```

### 6.3 イベント処理パターン

```kotlin
// 一度だけ処理すべきイベント（Snackbar表示、画面遷移など）
sealed interface HomeEvent {
    data class ShowSnackbar(val message: String) : HomeEvent
    data class NavigateToDetail(val id: Long) : HomeEvent
    object NavigateBack : HomeEvent
}

class HomeViewModel(...) : ViewModel() {
    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onItemClick(id: Long) {
        viewModelScope.launch {
            _events.send(HomeEvent.NavigateToDetail(id))
        }
    }

    fun onDeleteSuccess() {
        viewModelScope.launch {
            _events.send(HomeEvent.ShowSnackbar("削除しました"))
        }
    }
}

// Composable での受信
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is HomeEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.id)
                }
                HomeEvent.NavigateBack -> { /* 戻る処理 */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        // ...
    }
}
```

### 6.4 検索機能の実装

```kotlin
class SearchViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 検索クエリが変わるたびに自動で結果を更新
    val searchResults: StateFlow<List<MemoEntity>> = _searchQuery
        .debounce(300)  // 300ms待ってから検索（連続入力対策）
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchMemos(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
```

### 6.5 StateとEventの使い分け

| 種類 | 用途 | 例 |
|------|------|-----|
| **State** | 画面に表示するデータ | リスト、ローディング状態、入力値 |
| **Event** | 一度だけ処理するアクション | Snackbar、画面遷移、ダイアログ表示 |

---

## 7. アニメーション実装

### 7.1 画面遷移アニメーション

```kotlin
NavHost(
    navController = navController,
    startDestination = "home",
    enterTransition = {
        fadeIn(animationSpec = tween(300)) +
        slideInHorizontally(
            initialOffsetX = { it / 4 },  // 右から25%スライド
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(300)) +
        slideOutHorizontally(
            targetOffsetX = { -it / 4 },  // 左へ25%スライド
            animationSpec = tween(300)
        )
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(300)) +
        slideInHorizontally(
            initialOffsetX = { -it / 4 },  // 左から25%スライド（戻る時）
            animationSpec = tween(300)
        )
    },
    popExitTransition = {
        fadeOut(animationSpec = tween(300)) +
        slideOutHorizontally(
            targetOffsetX = { it / 4 },  // 右へ25%スライド（戻る時）
            animationSpec = tween(300)
        )
    }
) {
    composable("home") { HomeScreen() }
    composable("detail/{id}") { DetailScreen() }
}
```

### 7.2 タップ時スケールアニメーション

```kotlin
@Composable
fun ScaleOnPressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        content()
    }
}
```

### 7.3 ふわふわ浮遊アニメーション

```kotlin
@Composable
fun FloatingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.offset(y = offsetY.dp)
    )
}
```

### 7.4 リストのStaggeredアニメーション

```kotlin
@Composable
fun AnimatedList(items: List<Item>) {
    LazyColumn {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 50L)  // 50msずつ遅延
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300)
                        )
            ) {
                ItemCard(item)
            }
        }
    }
}
```

### 7.5 シェイクアニメーション（エラー時）

```kotlin
@Composable
fun ShakeAnimation(
    trigger: Boolean,
    content: @Composable () -> Unit
) {
    val offsetX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = if (trigger) {
            keyframes {
                durationMillis = 400
                0f at 0
                (-10f) at 50
                10f at 100
                (-10f) at 150
                10f at 200
                (-5f) at 250
                5f at 300
                0f at 400
            }
        } else {
            tween(0)
        },
        label = "shake"
    )

    Box(modifier = Modifier.offset(x = offsetX.dp)) {
        content()
    }
}
```

### 7.6 アニメーション値の目安

| 用途 | 推奨値 | 理由 |
|------|--------|------|
| フェード時間 | 200〜300ms | 自然に見える最小時間 |
| スケール（タップ） | 0.92〜0.97 | 押した感覚を与える |
| スケール（FAB） | 0.85 | より強い押し込み感 |
| スライド移動量 | 画面幅の25% | 動きがわかりつつも邪魔にならない |
| 浮遊アニメーション | 8〜12dp | 柔らかい印象 |
| リスト遅延 | 30〜50ms | 順番に現れる効果 |

---

## 8. ウィジェット（Glance）

### 8.1 基本構成

```kotlin
// dependencies (build.gradle.kts)
dependencies {
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
}
```

### 8.2 Widget実装

```kotlin
class MyAppWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact  // または SizeMode.Responsive

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // データ取得（Room等から）
        val memos = getMemos(context)

        provideContent {
            GlanceTheme {
                WidgetContent(memos)
            }
        }
    }

    @Composable
    private fun WidgetContent(memos: List<Memo>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .cornerRadius(16.dp)
        ) {
            // ヘッダー
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "メモ",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // リスト
            LazyColumn {
                items(memos.take(5)) { memo ->
                    MemoItem(memo)
                }
            }

            // アプリ起動ボタン
            Button(
                text = "アプリを開く",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun MemoItem(memo: Memo) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(actionStartActivity<MainActivity>(
                    parameters = actionParametersOf(
                        ActionParameters.Key<Long>("memo_id") to memo.id
                    )
                ))
        ) {
            Text(
                text = memo.title,
                style = TextStyle(fontSize = 14.sp),
                maxLines = 1
            )
        }
    }
}
```

### 8.3 WidgetReceiver

```kotlin
class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyAppWidget()
}
```

### 8.4 AndroidManifest.xml

```xml
<receiver
    android:name=".widget.MyAppWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/my_app_widget_info" />
</receiver>
```

### 8.5 Widget Info (res/xml/my_app_widget_info.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:previewImage="@drawable/widget_preview"
    android:description="@string/widget_description"
    android:updatePeriodMillis="1800000" />
```

### 8.6 ウィジェット更新

```kotlin
// データ変更時にウィジェットを更新
suspend fun updateWidget(context: Context) {
    MyAppWidget().updateAll(context)
}

// 特定のウィジェットのみ更新
suspend fun updateWidget(context: Context, glanceId: GlanceId) {
    MyAppWidget().update(context, glanceId)
}

// ViewModelやRepositoryから呼び出し
viewModelScope.launch {
    repository.insert(memo)
    updateWidget(context)
}
```

### 8.7 Glanceの注意点

| 問題 | 解決策 |
|------|--------|
| 通常のComposeと違う | `GlanceModifier`、`GlanceTheme`を使う |
| Clickが効かない | `actionStartActivity`で明示的にアクション指定 |
| データ取得 | `provideGlance`内でsuspend関数使用可 |
| 更新されない | `updateAll()`を明示的に呼ぶ |
| プレビューがない | エミュレータで実機テスト必須 |

---

## 9. パフォーマンス最適化

### 9.1 Compose再コンポジション対策

```kotlin
// NG: 毎回再コンポーズされる
@Composable
fun BadExample(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            ItemCard(item)
        }
    }
}

// OK: keyを指定してアイテムを識別
@Composable
fun GoodExample(items: List<Item>) {
    LazyColumn {
        items(
            items = items,
            key = { it.id }  // ユニークなキーを指定
        ) { item ->
            ItemCard(item)
        }
    }
}
```

### 9.2 remember と derivedStateOf

```kotlin
// 重い計算は remember でキャッシュ
@Composable
fun ExpensiveCalculation(items: List<Item>) {
    val sortedItems = remember(items) {
        items.sortedByDescending { it.priority }
    }

    // derivedStateOf: 派生状態（頻繁に変わらないもの）
    val hasUrgentItems by remember {
        derivedStateOf { sortedItems.any { it.isUrgent } }
    }

    if (hasUrgentItems) {
        UrgentBanner()
    }

    ItemList(sortedItems)
}
```

### 9.3 LazyListState の保存

```kotlin
@Composable
fun ScrollableList(items: List<Item>) {
    val listState = rememberLazyListState()

    // スクロール位置を保存（画面回転等で維持）
    val savedPosition = rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { savedPosition.value = it }
    }

    LazyColumn(state = listState) {
        items(items, key = { it.id }) { item ->
            ItemCard(item)
        }
    }
}
```

### 9.4 Image の最適化

```kotlin
// Coilを使った画像読み込み
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .size(Size.ORIGINAL)  // または具体的なサイズ
        .build(),
    contentDescription = null,
    modifier = Modifier
        .size(100.dp)
        .clip(RoundedCornerShape(8.dp)),
    contentScale = ContentScale.Crop
)
```

### 9.5 重い処理の非同期化

```kotlin
@Composable
fun HeavyProcessingScreen(viewModel: MyViewModel) {
    // 状態
    var result by remember { mutableStateOf<Result?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 重い処理はLaunchedEffectで非同期実行
    LaunchedEffect(Unit) {
        isLoading = true
        result = withContext(Dispatchers.IO) {
            viewModel.heavyCalculation()
        }
        isLoading = false
    }

    when {
        isLoading -> CircularProgressIndicator()
        result != null -> ResultContent(result!!)
    }
}
```

### 9.6 パフォーマンス計測

```kotlin
// ビルド時間計測
./gradlew assembleDebug --profile

// APKサイズ分析
./gradlew assembleRelease
# Android Studio: Build > Analyze APK

// Compose再コンポジション確認
// Android Studio: Layout Inspector で Recomposition counts を表示
```

---

## 10. テストの書き方

### 10.1 依存関係

```kotlin
// build.gradle.kts
dependencies {
    // Unit Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")  // Flow テスト

    // Android Instrumented Test
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room Test
    testImplementation("androidx.room:room-testing:2.6.1")
}
```

### 10.2 ViewModelテスト

```kotlin
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: MemoRepository

    @Before
    fun setup() {
        repository = mockk()
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun `初期状態はローディング`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `メモ一覧を取得できる`() = runTest {
        val memos = listOf(
            MemoEntity(id = 1, title = "Test", content = "Content")
        )
        coEvery { repository.allMemos } returns flowOf(memos)

        viewModel.loadMemos()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.memos.size)
    }
}

// TestDispatcher設定
class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 10.3 Roomテスト

```kotlin
@RunWith(AndroidJUnit4::class)
class MemoDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var memoDao: MemoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        memoDao = database.memoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetMemo() = runTest {
        val memo = MemoEntity(title = "Test", content = "Content")
        val id = memoDao.insert(memo)

        val result = memoDao.getMemoById(id)
        assertNotNull(result)
        assertEquals("Test", result?.title)
    }

    @Test
    fun getAllMemosFlow() = runTest {
        memoDao.insert(MemoEntity(title = "Memo 1", content = ""))
        memoDao.insert(MemoEntity(title = "Memo 2", content = ""))

        memoDao.getAllMemos().test {
            val memos = awaitItem()
            assertEquals(2, memos.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 10.4 Composeテスト

```kotlin
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `空の状態で「メモがありません」が表示される`() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState(memos = emptyList())
            )
        }

        composeTestRule
            .onNodeWithText("メモがありません")
            .assertIsDisplayed()
    }

    @Test
    fun `FABをタップすると追加ダイアログが表示される`() {
        composeTestRule.setContent {
            HomeScreen(uiState = HomeUiState())
        }

        composeTestRule
            .onNodeWithContentDescription("追加")
            .performClick()

        composeTestRule
            .onNodeWithText("新規メモ")
            .assertIsDisplayed()
    }

    @Test
    fun `メモカードをタップすると詳細に遷移`() {
        var clickedId: Long? = null

        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState(
                    memos = listOf(MemoEntity(id = 1, title = "Test", content = ""))
                ),
                onMemoClick = { clickedId = it }
            )
        }

        composeTestRule
            .onNodeWithText("Test")
            .performClick()

        assertEquals(1L, clickedId)
    }
}
```

### 10.5 テストのベストプラクティス

| ポイント | 説明 |
|---------|------|
| AAA パターン | Arrange（準備）→ Act（実行）→ Assert（検証） |
| 1テスト1検証 | 1つのテストで1つのことだけ検証 |
| 意味のある名前 | `メモを追加すると一覧に表示される` |
| モックは最小限 | 本物を使えるなら本物を使う |
| エッジケース | 空、null、最大値などもテスト |

---

## 11. セキュリティ

### 11.1 FLAG_SECURE（スクショ防止）

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // スクリーンショット・画面録画を防止
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent { /* ... */ }
    }
}
```

**注意**: スクリーンショット撮影時は一時的にコメントアウトすること

### 11.2 生体認証（BiometricPrompt）

```kotlin
class BiometricHelper(private val activity: FragmentActivity) {

    private val executor = ContextCompat.getMainExecutor(activity)

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        title: String = "認証",
        subtitle: String = "続行するには認証してください",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onCancel()
                        else -> onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    // 認証失敗（指紋が一致しない等）- 再試行可能
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
```

### 11.3 DataStoreでの暗号化

```kotlin
// EncryptedSharedPreferences を使う場合
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 11.4 INTERNET権限を使わない（プライバシー重視）

```xml
<!-- AndroidManifest.xml -->
<!-- INTERNET権限を意図的に追加しない = 通信できない -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<!-- 必要な権限のみ追加 -->
```

これにより:
- ユーザーデータが外部に送信されないことを保証
- Play Storeの「データセーフティ」で「データ収集なし」と宣言可能

---

## 12. ProGuard設定

### 12.1 基本設定（proguard-rules.pro）

```proguard
# 行番号を保持（クラッシュログ解析用）
-keepattributes SourceFile,LineNumberTable

# クラス名を難読化（シンプルな名前に）
-renamesourcefileattribute SourceFile

# Kotlin関連
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Serialization（使用する場合）
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
```

### 12.2 Room用ルール

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
```

### 12.3 Glance（ウィジェット）用ルール

```proguard
# Glance
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
```

### 12.4 デバッグ用ルール

```proguard
# デバッグビルドでは難読化しない（オプション）
# -dontobfuscate

# 警告を無視（本番では推奨しない）
# -ignorewarnings

# 使用していないコードを削除しない（デバッグ用）
# -dontshrink
```

### 12.5 リリース前チェック

```bash
# ProGuard適用後のAPKをテスト
./gradlew assembleRelease

# マッピングファイルを保存（クラッシュログ解析に必要）
cp app/build/outputs/mapping/release/mapping.txt ./backup/
```

**重要**: マッピングファイルはリリースごとに保存すること

---

## 13. リリース準備

### 13.1 チェックリスト

```markdown
## リリース前チェックリスト

### コード
- [ ] 不要なログ出力を削除 (Log.d など)
- [ ] デバッグコードを削除
- [ ] FLAG_SECURE を有効化
- [ ] バージョンコード・バージョン名を更新
- [ ] ProGuardルールを確認

### ビルド
- [ ] クリーンビルド成功
- [ ] リリースビルド成功
- [ ] APKサイズを確認

### テスト
- [ ] 主要機能の動作確認
- [ ] 画面回転での状態保持
- [ ] 低スペック端末での動作
- [ ] Android 各バージョンでの動作

### Play Console
- [ ] デベロッパーアカウント作成（$25）
- [ ] アプリ名（30文字以内）
- [ ] 短い説明（80文字以内）
- [ ] 詳しい説明（4000文字以内）
- [ ] アイコン（512x512px）
- [ ] スクリーンショット（最低2枚、推奨5枚以上）
- [ ] フィーチャーグラフィック（1024x500px）
- [ ] プライバシーポリシーURL
- [ ] データセーフティ入力
- [ ] コンテンツレーティング
- [ ] ターゲットユーザー設定
```

### 13.2 バージョン管理

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        // versionCode: 整数、毎回増やす（1, 2, 3...）
        versionCode = 1

        // versionName: ユーザーに表示される（1.0.0, 1.0.1, 1.1.0...）
        versionName = "1.0.0"
    }
}
```

**セマンティックバージョニング**:
- `1.0.0` → `1.0.1`: バグ修正
- `1.0.0` → `1.1.0`: 機能追加
- `1.0.0` → `2.0.0`: 大きな変更

### 13.3 アイコン生成

```python
from PIL import Image
import os

def generate_icons(input_path, output_base):
    """各密度のアイコンを生成"""
    img = Image.open(input_path).convert("RGBA")

    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    for folder, size in sizes.items():
        output_dir = f"{output_base}/{folder}"
        os.makedirs(output_dir, exist_ok=True)

        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        resized.save(f"{output_dir}/ic_launcher.png", "PNG")
        print(f"Generated: {folder}/ic_launcher.png ({size}x{size})")

    # Play Store用
    os.makedirs(f"{output_base}/store", exist_ok=True)
    img.resize((512, 512), Image.Resampling.LANCZOS).save(
        f"{output_base}/store/app_icon_512.png", "PNG"
    )
    print("Generated: store/app_icon_512.png (512x512)")

# 使用例
generate_icons("icon_original.png", "app/src/main/res")
```

### 13.4 データセーフティの回答（データ収集なしの場合）

| 質問 | 回答 |
|------|------|
| ユーザーデータを収集しますか？ | いいえ |
| データを第三者と共有しますか？ | いいえ |
| データは送信中に暗号化されますか？ | 該当なし（通信しない） |
| ユーザーはデータ削除をリクエストできますか？ | はい（アプリ削除で全削除） |

### 13.5 コンテンツレーティング

「ユーティリティ」カテゴリの場合、質問に全て「いいえ」で回答すると **全年齢（3+）** になる。

---

## 14. トラブルシューティング

### 14.1 ビルドエラー

#### JAVA_HOME is not set

```
ERROR: JAVA_HOME is not set and no 'java' command could be found
```

**解決策**:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

# Mac/Linux
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

#### JDK Image Transform失敗

```
Failed to transform core-for-system-modules.jar
```

**解決策**:
1. `gradle.properties`に追加:
   ```properties
   org.gradle.java.installations.auto-download=false
   ```
2. Android Studio: File > Invalidate Caches > Invalidate and Restart
3. `~/.gradle/caches/transforms-*` を削除

#### 日本語パスのエラー

```
The path 'C:\Users\ユーザー\デスクトップ\...' seems to contain a non-ASCII character
```

**解決策**:
```properties
# gradle.propertiesに追加
android.overridePathCheck=true
```

#### Deprecated Gradle features

```
Deprecated Gradle features were used in this build
```

**対応**: 警告のみでビルドは成功する。詳細を見る場合:
```bash
./gradlew --warning-mode all
```

### 14.2 実行時エラー

#### Room: Cannot find implementation

```
Cannot find implementation for com.example.AppDatabase
```

**解決策**: KSPが正しく設定されているか確認
```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

#### Compose: Recomposition loop

画面がフリーズ、または無限ループ

**原因と解決策**:
```kotlin
// NG: 毎回新しいオブジェクトを作成
@Composable
fun BadExample() {
    val items = listOf("A", "B", "C")  // 毎回再生成される
    ItemList(items)
}

// OK: remember でキャッシュ
@Composable
fun GoodExample() {
    val items = remember { listOf("A", "B", "C") }
    ItemList(items)
}
```

### 14.3 リソースエラー

#### ic_launcher not found

```
ERROR: resource mipmap/ic_launcher not found
```

**解決策**:
- `app/src/main/res/mipmap-*` フォルダに `ic_launcher.png` を配置
- Android Studio: New > Image Asset でアイコン生成

### 14.4 デバッグ

#### スクリーンショットが撮れない

**原因**: FLAG_SECURE が有効

**解決策**:
```kotlin
// MainActivity.kt で一時的にコメントアウト
// window.setFlags(
//     WindowManager.LayoutParams.FLAG_SECURE,
//     WindowManager.LayoutParams.FLAG_SECURE
// )
```
**撮影後は必ず元に戻す**

#### Logcatでログが見えない

**解決策**:
1. フィルタを確認（パッケージ名で絞り込み）
2. ログレベルを確認（Verbose以上）
3. 実機の場合、開発者オプションを有効化

### 14.5 依存関係の問題

#### バージョン競合

```
Duplicate class found in modules...
```

**解決策**:
```kotlin
// 依存関係ツリーを確認
./gradlew app:dependencies --configuration releaseRuntimeClasspath

// 競合を解決
implementation("com.example:library:1.0.0") {
    exclude(group = "com.conflict", module = "module")
}
```

#### Could not resolve

```
Could not resolve com.example:library:1.0.0
```

**解決策**:
1. `settings.gradle.kts`でリポジトリを確認:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```
2. オフラインモードを無効化（Android Studio: File > Settings > Build > Gradle）

---

## 15. Claude Codeを使った開発

### 15.1 効果的な依頼の仕方

#### 良い依頼の例

```
【良い例1: 具体的なゴールを示す】
「HomeScreenにプルダウンで更新できる機能を追加してください。
SwipeRefreshを使って、更新中はインジケータを表示してください。」

【良い例2: コンテキストを共有】
「現在のMemoRepositoryを見て、検索機能を追加してください。
SQLiteのLIKE句を使って、タイトルと内容から検索できるようにしてください。」

【良い例3: 参考を示す】
「添付したスクリーンショットのようなカードデザインにしてください。
角丸12dp、影はelevation 4dpでお願いします。」
```

#### 避けるべき依頼の例

```
【悪い例1: 曖昧すぎる】
「いい感じにしてください」

【悪い例2: 一度に多すぎる】
「ログイン画面、ホーム画面、設定画面、全部作ってください」

【悪い例3: 背景情報なし】
「エラーが出ます」（エラー内容を共有しない）
```

### 15.2 開発フロー

```
1. 要件を整理して依頼
   └→ 「〇〇画面で△△できるようにしたい」

2. 生成されたコードを確認
   └→ 意図と違う場合は具体的にフィードバック

3. 動作確認
   └→ エミュレータ or 実機でテスト

4. 微調整を依頼
   └→ 「ここの色をもう少し薄く」など

5. コードレビュー
   └→ 最終的にコードを理解してからコミット
```

### 15.3 効率的なやり取りのコツ

| コツ | 説明 |
|------|------|
| 小さく依頼 | 1回の依頼は1機能 |
| スクショ共有 | UIは言葉より画像 |
| エラー全文共有 | ログは省略せずに |
| ファイルパス指定 | どのファイルを編集するか明示 |
| 既存コード参照 | 「〇〇と同じスタイルで」 |

### 15.4 よく使う依頼テンプレート

```
【新機能追加】
「[画面名]に[機能]を追加してください。
- [具体的な動作1]
- [具体的な動作2]
参考: [類似の既存コード or 画像]」

【バグ修正】
「[画面名]で[問題の現象]が起きています。
期待する動作: [正しい動作]
エラーログ: [ログ全文]」

【リファクタリング】
「[ファイル名]を[改善内容]してください。
現在の問題: [問題点]
期待する状態: [改善後の状態]」

【UI調整】
「[コンポーネント名]の見た目を調整してください。
- 角丸: 〇dp
- 色: #XXXXXX
- パディング: 〇dp
参考画像: [添付]」
```

---

## 16. UIデザインのコツ

### 16.1 ビジネスライク + 柔らかさのバランス

**基本方針**:
- 信頼感のあるシンプルなデザイン
- 角丸で柔らかさを演出
- 過度な装飾は避ける

#### カラー設計

```kotlin
// Theme.kt
val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),         // 信頼感のあるブルー
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD), // 淡いブルー
    secondary = Color(0xFF26A69A),        // アクセントのティール
    surface = Color.White,
    background = Color(0xFFFAFAFA),       // ほんのりグレーの背景
    error = Color(0xFFD32F2F)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFF80CBC4),
    surface = Color(0xFF121212),
    background = Color(0xFF1E1E1E),
    error = Color(0xFFEF5350)
)
```

#### タイポグラフィ

```kotlin
val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,  // Boldより柔らかい
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp  // 読みやすい行間
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)
```

### 16.2 アニメーションの適切な使い方

#### 使うべき場面

| 場面 | アニメーション | 理由 |
|------|---------------|------|
| 画面遷移 | フェード + スライド | 自然な流れを演出 |
| ボタンタップ | スケール（0.95） | フィードバック |
| リスト表示 | Stagger | 順番に現れる楽しさ |
| ローディング | スピナー or シマー | 待機感の軽減 |
| 削除 | スワイプ + フェード | 操作の結果を可視化 |

#### 使いすぎ注意

```kotlin
// NG: 常に動き続けるアニメーション（うるさい）
val infiniteRotation by rememberInfiniteTransition()
    .animateFloat(0f, 360f, infiniteRepeatable(...))

// OK: 必要な時だけアニメーション
AnimatedVisibility(
    visible = isLoading,
    enter = fadeIn(),
    exit = fadeOut()
) {
    CircularProgressIndicator()
}
```

### 16.3 空の状態（Empty State）デザイン

```kotlin
@Composable
fun EmptyState(
    icon: ImageVector = Icons.Outlined.Description,
    title: String = "メモがありません",
    description: String = "右下の＋ボタンから\n新しいメモを追加できます",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ふわふわアイコン
        FloatingIcon(
            icon = icon,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
```

### 16.4 ローディング表示

```kotlin
// シンプルなスピナー
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

// スケルトン（シマー）表示
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )
    }
}
```

---

## 付録

### A. よく使うGradleコマンド

```bash
# ビルド
./gradlew clean                    # クリーン
./gradlew assembleDebug            # デバッグAPK
./gradlew assembleRelease          # リリースAPK
./gradlew bundleRelease            # AAB（Play Store用）

# 確認
./gradlew tasks                    # タスク一覧
./gradlew dependencies             # 依存関係
./gradlew signingReport            # 署名情報

# テスト
./gradlew test                     # ユニットテスト
./gradlew connectedAndroidTest     # 端末テスト

# その他
./gradlew --stop                   # デーモン停止
./gradlew --warning-mode all       # 警告詳細表示
```

### B. よく使うADBコマンド

```bash
# デバイス確認
adb devices

# アプリインストール
adb install app-debug.apk

# アプリアンインストール
adb uninstall com.example.myapp

# ログ表示
adb logcat | grep "MyApp"

# スクリーンショット
adb exec-out screencap -p > screenshot.png

# 画面録画
adb shell screenrecord /sdcard/video.mp4

# ファイル転送
adb push local.txt /sdcard/
adb pull /sdcard/file.txt ./
```

### C. ショートカットキー（Android Studio）

| 操作 | Windows | Mac |
|------|---------|-----|
| どこでも検索 | Shift×2 | Shift×2 |
| ファイル検索 | Ctrl+Shift+N | Cmd+Shift+O |
| クラス検索 | Ctrl+N | Cmd+O |
| テキスト検索 | Ctrl+Shift+F | Cmd+Shift+F |
| リファクタリング | Ctrl+Alt+Shift+T | Ctrl+T |
| コード整形 | Ctrl+Alt+L | Cmd+Option+L |
| インポート最適化 | Ctrl+Alt+O | Ctrl+Option+O |
| 定義に移動 | Ctrl+B | Cmd+B |
| 実装に移動 | Ctrl+Alt+B | Cmd+Option+B |
| ビルド | Ctrl+F9 | Cmd+F9 |
| 実行 | Shift+F10 | Ctrl+R |

---

## 17. 実装時に遭遇した問題と解決策

### 17.1 ウィジェット実装の問題

#### 問題1: RemoteViewsでViewクラスが使えない

**症状**:
```
Binary XML file line #51: Class not allowed to be inflated android.view.View
```

ウィジェットレイアウトで区切り線として `<View>` を使用していたが、RemoteViewsでは `android.view.View` クラスは使用できない。

**原因**:
RemoteViewsはセキュリティ上の理由から、使用できるViewクラスが制限されている。抽象クラスの `View` は使用不可。

**解決策**:
区切り線を `<View>` から `<ImageView>` に変更。

```xml
<!-- Before (NG) -->
<View
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:background="@color/widget_divider" />

<!-- After (OK) -->
<ImageView
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:background="@color/widget_divider" />
```

**RemoteViewsで使用可能な主要クラス**:
- `TextView`, `ImageView`, `Button`
- `LinearLayout`, `RelativeLayout`, `FrameLayout`
- `ListView`, `GridView`, `StackView`
- `ProgressBar`, `Chronometer`

#### 問題2: AppWidgetProviderでのgoAsync()とCoroutineの組み合わせ

**症状**:
ウィジェット追加時に「ウィジェットの読み込みに失敗しました」エラーが表示される。ログでは "Widget updated successfully" と出ているのにウィジェットが表示されない。

**原因**:
```kotlin
// NG: goAsync() + CoroutineScope.launch() の組み合わせ
val pendingResult = goAsync()
CoroutineScope(Dispatchers.IO).launch {
    // データベースアクセス
    updateWidget()
    pendingResult.finish()  // launch()は非同期なので、ここに到達する前にonUpdate()が終了する
}
// onUpdate()がここで終了 → pendingResultが無効化される
```

`CoroutineScope.launch()` は非同期実行のため、`onUpdate()` メソッドが即座に終了してしまい、`pendingResult` が無効化される。その後に `pendingResult.finish()` を呼んでも手遅れ。

**解決策**:
`Thread + runBlocking` パターンで同期的に実行する。

```kotlin
// OK: Thread + runBlocking で同期実行
override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
) {
    // まず即座にローディング状態を表示
    for (appWidgetId in appWidgetIds) {
        showLoadingWidget(context, appWidgetManager, appWidgetId)
    }

    val pendingResult = goAsync()
    val applicationContext = context.applicationContext

    // Threadを使って同期的に処理
    Thread {
        try {
            kotlinx.coroutines.runBlocking {
                withTimeout(9_000) {
                    val database = MemoDatabase.getDatabase(applicationContext)
                    val repository = MemoRepository(database.memoDao())
                    val memos = repository.getValidMemosForWidget()

                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId, memos)
                    }
                }
            }
        } catch (e: Exception) {
            for (appWidgetId in appWidgetIds) {
                showErrorWidget(context, appWidgetManager, appWidgetId, e)
            }
        } finally {
            // Threadなので必ずここに到達する
            pendingResult.finish()
        }
    }.start()
}
```

**ポイント**:
- `Thread.start()` でスレッドを起動し、その中で `runBlocking` を使用
- これにより `pendingResult.finish()` が確実に処理完了後に呼ばれる
- `withTimeout(9_000)` で9秒のタイムアウトを設定（10秒制限を考慮）
- エラー時も必ず `finally` で `finish()` を呼ぶ

### 17.2 生体認証（BiometricPrompt）の問題

#### 問題1: BIOMETRIC_WEAKが非推奨

**症状**:
```kotlin
// Deprecated in API 30
BiometricManager.Authenticators.BIOMETRIC_WEAK
```

**解決策**:
`BIOMETRIC_STRONG` と `DEVICE_CREDENTIAL` の組み合わせを使用。

```kotlin
val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                     BiometricManager.Authenticators.DEVICE_CREDENTIAL

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("認証")
    .setSubtitle("続行するには認証してください")
    .setAllowedAuthenticators(authenticators)
    .build()  // setNegativeButtonText() は不要（DEVICE_CREDENTIAL使用時）
```

#### 問題2: アプリ切り替え時の頻繁な再認証

**症状**:
アプリを一瞬バックグラウンドにしただけで、戻ると毎回認証が求められる。

**解決策**:
30秒の猶予期間を設ける。

```kotlin
class MainActivity : ComponentActivity() {
    private var pauseTime: Long = 0
    private var shouldRequireAuth = true

    override fun onPause() {
        super.onPause()
        pauseTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val timeSincePause = System.currentTimeMillis() - pauseTime

        // 30秒以上経過している場合のみ認証を要求
        if (timeSincePause > 30_000 && shouldRequireAuth) {
            performBiometricAuth()
        }
    }
}
```

### 17.3 UI/UX改善

#### 問題: プリセット期間ボタンの選択状態が不明瞭

**症状**:
よく使う期間ボタン（1時間、3時間など）を押しても、どれが選択されているか分からない。

**解決策**:
選択状態を追跡し、ボタンの色を変更。スライダーで変更した場合は選択を解除。

```kotlin
var selectedPresetHours by remember { mutableStateOf<Int?>(null) }

// プリセットボタン
Row {
    listOf(1, 3, 6, 12, 24).forEach { hours ->
        val isSelected = selectedPresetHours == hours

        Button(
            onClick = {
                selectedPresetHours = hours
                durationHours = hours
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("${hours}時間")
        }
    }
}

// スライダーで変更時は選択解除
Slider(
    value = durationHours.toFloat(),
    onValueChange = {
        durationHours = it.toInt()
        selectedPresetHours = null  // スライダー操作時は選択解除
    }
)
```

#### 新規メモ作成時の自動フォーカス

**要件**:
新規メモ画面を開いたとき、すぐに入力できるように自動でカーソルをテキストフィールドに合わせる。

**実装**:
```kotlin
val focusRequester = remember { FocusRequester() }

// 新規メモの場合のみ自動フォーカス
LaunchedEffect(Unit) {
    if (isNewMemo) {
        delay(100)  // キーボード表示のタイミングを考慮
        focusRequester.requestFocus()
    }
}

OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    modifier = Modifier.focusRequester(focusRequester)
)
```

### 17.4 ビルドとデプロイの問題

#### 問題: Gradleキャッシュの破損

**症状**:
```
Could not read workspace metadata from C:\Users\...\kotlin-dsl\accessors\...\metadata.bin
```

**解決策**:
```bash
# 1. Javaプロセスを全停止
taskkill /F /IM java.exe

# 2. kotlin-dslキャッシュを削除
rm -rf C:/Users/seizo/.gradle/caches/8.10.2/kotlin-dsl

# 3. Gradleデーモン停止
./gradlew --stop

# 4. クリーンビルド
./gradlew clean assembleRelease
```

#### HTTPサーバーによるファイルロック

**症状**:
Pythonの `http.server` がAPKファイルをロックして、ビルドが失敗する。

**解決策**:
```bash
# ビルド前にサーバープロセスを停止
netstat -ano | findstr :8000
taskkill /F /PID [プロセスID]

# またはcleanせずにビルド
./gradlew assembleRelease  # cleanなし
```

### 17.5 デバッグとテストの工夫

#### エミュレーターでのウィジェットテスト自動化

```bash
# ログのクリアと監視
adb logcat -c
adb logcat -s MemoWidgetReceiver:* AndroidRuntime:E *:F

# スクリーンショット取得
adb exec-out screencap -p > screenshot.png

# ウィジェット更新をトリガー
adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE
```

#### 実機テスト用簡易配布

```bash
# リリースAPKをビルド
./gradlew assembleRelease

# HTTPサーバー起動
cd app/build/outputs/apk/release
python -m http.server 8000

# ローカルIPを確認
ipconfig | findstr IPv4
# → http://192.168.x.x:8000/app-release.apk
```

実機から上記URLにアクセスしてAPKをダウンロード・インストール。

## 更新履歴

| 日付 | 内容 |
|------|------|
| 2026-01-17 | 初版作成（ポイメモ開発時のノウハウ） |
| 2026-01-17 | 大幅拡充: MVVM+Room、ViewModel、Glance、テスト、ProGuard、Claude Code開発フロー追加 |
| 2026-01-17 | セクション17追加: ウィジェット実装、生体認証、UI/UX改善、ビルド・デプロイ問題の解決策 |
