package com.temporary.memo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.temporary.memo.ui.screens.LockScreen
import com.temporary.memo.ui.screens.MemoEditScreen
import com.temporary.memo.ui.screens.MemoListScreen
import com.temporary.memo.ui.screens.SettingsScreen
import com.temporary.memo.viewmodel.BiometricViewModel
import com.temporary.memo.viewmodel.MemoViewModel

/**
 * ナビゲーショングラフ
 *
 * アプリ内の画面遷移を定義。
 */

sealed class Screen(val route: String) {
    object Lock : Screen("lock")
    object MemoList : Screen("memo_list")
    object MemoEdit : Screen("memo_edit/{memoId}") {
        fun createRoute(memoId: Long?) = if (memoId != null) "memo_edit/$memoId" else "memo_edit/new"
    }
    object Settings : Screen("settings")
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    memoViewModel: MemoViewModel,
    biometricViewModel: BiometricViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ロック画面
        composable(route = Screen.Lock.route) {
            LockScreen(
                biometricViewModel = biometricViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.MemoList.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        // メモ一覧画面
        composable(route = Screen.MemoList.route) {
            MemoListScreen(
                memoViewModel = memoViewModel,
                onNavigateToEdit = { memoId ->
                    navController.navigate(Screen.MemoEdit.createRoute(memoId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // メモ編集画面
        composable(
            route = Screen.MemoEdit.route,
            arguments = listOf(
                navArgument("memoId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val memoIdString = backStackEntry.arguments?.getString("memoId")
            val memoId = if (memoIdString == "new") null else memoIdString?.toLongOrNull()

            MemoEditScreen(
                memoViewModel = memoViewModel,
                memoId = memoId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 設定画面
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                biometricViewModel = biometricViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
