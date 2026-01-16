package com.temporary.memo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.temporary.memo.data.MemoDatabase
import com.temporary.memo.repository.MemoRepository
import com.temporary.memo.ui.navigation.Screen
import com.temporary.memo.ui.navigation.SetupNavGraph
import com.temporary.memo.ui.theme.TemporaryMemoTheme
import com.temporary.memo.viewmodel.BiometricViewModel
import com.temporary.memo.viewmodel.MemoViewModel
import com.temporary.memo.viewmodel.MemoViewModelFactory

/**
 * MainActivity
 *
 * アプリのエントリーポイント。
 * FLAG_SECUREを設定し、スクリーンショット・画面録画を防止。
 */
class MainActivity : ComponentActivity() {

    private lateinit var memoViewModel: MemoViewModel
    private lateinit var biometricViewModel: BiometricViewModel
    private var shouldRequireAuth = false
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FLAG_SECURE設定（スクリーンショット・画面録画を防止）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // ViewModelの初期化
        val database = MemoDatabase.getDatabase(applicationContext)
        val repository = MemoRepository(database.memoDao())
        val factory = MemoViewModelFactory(repository)

        enableEdgeToEdge()

        setContent {
            // ViewModelの取得
            memoViewModel = viewModel(factory = factory)
            biometricViewModel = BiometricViewModel(applicationContext)

            // 生体認証設定の取得
            val biometricEnabled by biometricViewModel.biometricEnabled.collectAsState()

            // ナビゲーションコントローラー
            val navCtrl = rememberNavController()
            navController = navCtrl

            // 開始画面を決定（生体認証が有効ならロック画面、無効ならメモ一覧）
            val startDestination = if (biometricEnabled) {
                Screen.Lock.route
            } else {
                Screen.MemoList.route
            }

            TemporaryMemoTheme {
                SetupNavGraph(
                    navController = navCtrl,
                    memoViewModel = memoViewModel,
                    biometricViewModel = biometricViewModel,
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // バックグラウンドに移行する際、生体認証が有効なら再認証フラグを立てる
        if (::biometricViewModel.isInitialized) {
            val biometricEnabled = biometricViewModel.biometricEnabled.value
            if (biometricEnabled) {
                shouldRequireAuth = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // アプリが前面に戻った際に期限切れメモを削除
        if (::memoViewModel.isInitialized) {
            memoViewModel.deleteExpiredMemos()
        }

        // 生体認証が必要な場合はロック画面に遷移
        if (shouldRequireAuth && ::biometricViewModel.isInitialized) {
            shouldRequireAuth = false
            navController?.navigate(Screen.Lock.route) {
                popUpTo(Screen.MemoList.route) { inclusive = false }
            }
        }
    }
}
