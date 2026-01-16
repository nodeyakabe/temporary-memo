package com.temporary.memo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.temporary.memo.utils.BiometricHelper
import com.temporary.memo.viewmodel.BiometricViewModel

/**
 * 設定画面
 *
 * 生体認証のON/OFF切替、アプリ説明、免責文言を表示。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    biometricViewModel: BiometricViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val biometricEnabled by biometricViewModel.biometricEnabled.collectAsState()
    val canUseBiometric = BiometricHelper.canAuthenticate(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "生体認証",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "起動時に生体認証を要求",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!canUseBiometric) {
                                Text(
                                    text = BiometricHelper.getBiometricStatus(context),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
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

            // アプリ説明
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "アプリについて",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "一時保存メモは、期限付きでメモを保存できるシンプルなアプリです。",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = """
                            主な機能:
                            • 期限付きメモ（1時間〜7日）
                            • 期限到達で自動削除
                            • 生体認証でロック
                            • ホーム画面ウィジェット
                            • オフライン動作（通信なし）
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 免責文言
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ 重要な注意事項",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "このアプリの限界について",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = """
                            本アプリは端末内にのみ保存されるローカルメモアプリです。

                            本アプリは簡易的なメモ管理を目的としており、完全なセキュリティを保証するものではありません。

                            • データは暗号化されていません
                            • 機種変更時の引き継ぎはできません
                            • 削除されたメモは復元できません
                            • バックアップは作成されません

                            重要なデータの保存には使用しないでください。
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // バージョン情報
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "バージョン情報",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "バージョン: 1.0.0",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
