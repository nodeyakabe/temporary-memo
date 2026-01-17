package com.temporary.memo.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // ロックアイコンのふわふわアニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "lockIconFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // アイコンの脈動アニメーション
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // ロックアイコン（ふわふわ浮くアニメーション付き）
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "ロック",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        translationY = -floatOffset
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                tint = MaterialTheme.colorScheme.primary
            )

            // タイトルとサブタイトル
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ポイメモ",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "認証してメモを表示します",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 認証ボタン（タップ時のスケールアニメーション付き）
            var isPressed by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "authButtonScale"
            )

            Button(
                onClick = {
                    errorMessage = null
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        BiometricHelper.authenticate(
                            activity = activity,
                            onSuccess = { biometricViewModel.onAuthSuccess() },
                            onError = { message -> biometricViewModel.onAuthError(message) }
                        )
                    } else {
                        biometricViewModel.onAuthError("認証機能をご利用いただけません")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .scale(buttonScale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    }
            ) {
                Text("認証してロック解除")
            }

            // エラーメッセージ
            errorMessage?.let { message ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
