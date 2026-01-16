package com.temporary.memo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.temporary.memo.utils.BiometricHelper
import com.temporary.memo.viewmodel.AuthState
import com.temporary.memo.viewmodel.BiometricViewModel

/**
 * ロック画面
 *
 * 生体認証を実行し、成功時にメモ一覧画面へ遷移。
 */
@Composable
fun LockScreen(
    biometricViewModel: BiometricViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by biometricViewModel.authState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 認証成功時の処理
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                biometricViewModel.resetAuthState()
                onAuthSuccess()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    // 画面表示時に自動的に生体認証を開始
    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { biometricViewModel.onAuthSuccess() },
                onError = { message -> biometricViewModel.onAuthError(message) }
            )
        } else {
            // 生体認証が使用できない場合は自動的に通過
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ロックアイコン
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "ロック",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // タイトル
            Text(
                text = "一時保存メモ",
                style = MaterialTheme.typography.headlineMedium
            )

            // 認証ボタン
            Button(
                onClick = {
                    errorMessage = null
                    BiometricHelper.authenticate(
                        activity = context as FragmentActivity,
                        onSuccess = { biometricViewModel.onAuthSuccess() },
                        onError = { message -> biometricViewModel.onAuthError(message) }
                    )
                }
            ) {
                Text("認証する")
            }

            // エラーメッセージ
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
