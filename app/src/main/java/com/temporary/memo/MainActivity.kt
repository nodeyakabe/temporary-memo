package com.temporary.memo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.temporary.memo.data.MemoDatabase
import com.temporary.memo.repository.MemoRepository
import com.temporary.memo.ui.navigation.Screen
import com.temporary.memo.ui.navigation.SetupNavGraph
import com.temporary.memo.ui.theme.TemporaryMemoTheme
import com.temporary.memo.viewmodel.BiometricViewModel
import com.temporary.memo.viewmodel.BiometricViewModelFactory
import com.temporary.memo.viewmodel.MemoViewModel
import com.temporary.memo.viewmodel.MemoViewModelFactory
import com.temporary.memo.viewmodel.ThemeViewModel
import com.temporary.memo.viewmodel.ThemeViewModelFactory

/**
 * MainActivity
 *
 * アプリのエントリーポイント。
 * FLAG_SECUREを設定し、スクリーンショット・画面録画を防止。
 * 生体認証のためFragmentActivityを継承。
 */
class MainActivity : FragmentActivity() {

    private var shouldRequireAuth = false
    private var pauseTime: Long = 0  // アプリがバックグラウンドに移行した時刻
    private var navController: NavHostController? = null

    // ViewModelFactoryをlazyで初期化（画面回転時も再利用）
    private val memoFactory by lazy {
        val database = MemoDatabase.getDatabase(applicationContext)
        val repository = MemoRepository(database.memoDao())
        MemoViewModelFactory(repository)
    }

    private val biometricFactory by lazy {
        BiometricViewModelFactory(applicationContext)
    }

    private val themeFactory by lazy {
        ThemeViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FLAG_SECURE設定（スクリーンショット・画面録画を防止）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        setContent {
            // ViewModelの取得（Factoryを再利用）
            val memoViewModel: MemoViewModel = viewModel(factory = memoFactory)
            val biometricViewModel: BiometricViewModel = viewModel(factory = biometricFactory)
            val themeViewModel: ThemeViewModel = viewModel(factory = themeFactory)

            // 生体認証設定の取得
            val biometricEnabled by biometricViewModel.biometricEnabled.collectAsState()

            // テーマ設定の取得
            val selectedTheme by themeViewModel.selectedTheme.collectAsState()

            // 開始画面を決定（初回のみ評価、再コンポーズで変更されない）
            val startDestination = remember {
                if (biometricEnabled) {
                    Screen.Lock.route
                } else {
                    Screen.MemoList.route
                }
            }

            // ナビゲーションコントローラー
            val navCtrl = rememberNavController()
            navController = navCtrl

            // onResumeでの認証チェック
            LaunchedEffect(shouldRequireAuth) {
                if (shouldRequireAuth && biometricEnabled) {
                    shouldRequireAuth = false
                    navCtrl.navigate(Screen.Lock.route) {
                        popUpTo(Screen.MemoList.route) { inclusive = false }
                    }
                }
            }

            TemporaryMemoTheme(themeType = selectedTheme) {
                SetupNavGraph(
                    navController = navCtrl,
                    memoViewModel = memoViewModel,
                    biometricViewModel = biometricViewModel,
                    themeViewModel = themeViewModel,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // バックグラウンドに移行した時刻を記録
        pauseTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // 3分以上経過した場合のみ再認証を要求
        val timeSincePause = System.currentTimeMillis() - pauseTime
        shouldRequireAuth = timeSincePause > 180_000  // 3分 = 180秒
        // Note: ViewModelへのアクセスはCompose内で行うため、ここでは削除処理を行わない
        // MemoListScreenのLaunchedEffectで期限切れメモの削除が行われる
    }
}
