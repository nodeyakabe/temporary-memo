package com.temporary.memo.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.temporary.memo.utils.BiometricHelper
import com.temporary.memo.viewmodel.BiometricViewModel
import com.temporary.memo.viewmodel.ThemeViewModel
import com.temporary.memo.viewmodel.ThemeType

/**
 * 設定画面
 *
 * 生体認証のON/OFF切替、アプリ説明、免責文言を表示。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    biometricViewModel: BiometricViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val biometricEnabled by biometricViewModel.biometricEnabled.collectAsState()
    val canUseBiometric = BiometricHelper.canAuthenticate(context)
    val selectedTheme by themeViewModel.selectedTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 生体認証設定
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "セキュリティ",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "起動時に認証を求める",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "端末に登録済みの認証方法(指紋、顔、画面ロック)でアプリを保護します。\n\nまだ設定していない場合:\n端末の設定 → セキュリティ → 画面ロック から登録してください。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!canUseBiometric) {
                                Text(
                                    text = BiometricHelper.getBiometricStatus(context),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Switch(
                            checked = biometricEnabled && canUseBiometric,
                            onCheckedChange = { biometricViewModel.setBiometricEnabled(it) },
                            enabled = canUseBiometric
                        )
                    }
                }
            }

            // テーマ設定
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "テーマ設定",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "アプリの表示テーマを選択してください",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // テーマ選択ボタン
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeSelectionButton(
                            text = "ホワイト",
                            isSelected = selectedTheme == ThemeType.LIGHT,
                            onClick = { themeViewModel.setTheme(ThemeType.LIGHT) }
                        )
                        ThemeSelectionButton(
                            text = "オレンジ",
                            isSelected = selectedTheme == ThemeType.ORANGE,
                            onClick = { themeViewModel.setTheme(ThemeType.ORANGE) }
                        )
                        ThemeSelectionButton(
                            text = "ライトブルー",
                            isSelected = selectedTheme == ThemeType.BLUE,
                            onClick = { themeViewModel.setTheme(ThemeType.BLUE) }
                        )
                        ThemeSelectionButton(
                            text = "ダーク",
                            isSelected = selectedTheme == ThemeType.DARK,
                            onClick = { themeViewModel.setTheme(ThemeType.DARK) }
                        )
                    }
                }
            }

            // アプリ説明
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ℹ️ ポイメモについて",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "ポイメモは、期限付きでメモを保存できるシンプルなアプリです。設定した時間が経過すると、メモは自動的に削除されます。",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = """
主な機能:
・期限付きメモ（1時間から7日まで）
・設定した時間で自動削除
・指紋認証などでアプリをロック
・ホーム画面にウィジェットを配置
・インターネット接続は不要です
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 免責文言
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️ ご利用にあたって",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "このアプリは、一時的なメモの管理を目的としています。以下の点をご理解の上、ご利用ください。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Text(
                        text = """
・メモはこの端末内にのみ保存されます
・データの暗号化は行っていません
・機種変更時にデータを引き継ぐことはできません
・削除されたメモを復元することはできません
・バックアップ機能はありません

大切な情報の保存にはご注意ください。
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // バージョン情報
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "バージョン情報",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "バージョン 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * テーマ選択ボタンコンポーネント
 * タップ時のスケールアニメーション付き
 */
@Composable
fun ThemeSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "themeButtonScale"
    )

    // 選択状態の変化をアニメーション
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "themeButtonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(200),
        label = "themeButtonContentColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(buttonScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
