package com.temporary.memo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.temporary.memo.utils.TimeUtils
import com.temporary.memo.viewmodel.MemoViewModel
import kotlinx.coroutines.launch

/**
 * メモ編集画面
 *
 * 新規作成と編集を共通の画面で処理。
 * 期限設定UIとプレビュー機能を提供。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoEditScreen(
    memoViewModel: MemoViewModel,
    memoId: Long?,
    onNavigateBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf(24) }  // デフォルト24時間
    val scope = rememberCoroutineScope()
    val isNewMemo = memoId == null

    // 既存メモの場合、データを読み込む
    LaunchedEffect(memoId) {
        if (memoId != null) {
            val memo = memoViewModel.getMemoById(memoId)
            if (memo != null) {
                text = memo.text
                // 残り時間から時間数を計算
                val remaining = memo.deleteAt - System.currentTimeMillis()
                durationHours = (remaining / (1000 * 60 * 60)).toInt().coerceAtLeast(1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewMemo) "新規メモ" else "メモ編集") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (isNewMemo) {
                                    memoViewModel.createMemo(text, durationHours)
                                } else {
                                    memoViewModel.updateMemo(memoId!!, text, durationHours)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // メモ入力欄
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("メモ") },
                placeholder = {
                    Text(
                        text = "例: 今日の買い物リスト\n牛乳、卵、パン...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                maxLines = Int.MAX_VALUE
            )

            // 期限設定セクション
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "削除期限設定",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // スライダー
                    Text(
                        text = "${durationHours}時間後に自動削除",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = durationHours.toFloat(),
                        onValueChange = { durationHours = it.toInt() },
                        valueRange = 1f..168f,  // 1時間〜7日
                        steps = 167
                    )

                    // ヒントテキスト
                    Text(
                        text = "💡 ヒント: 期限が来ると自動的にメモが削除されます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // プリセットボタン
                    Text(
                        text = "プリセット:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            text = "1時間",
                            onClick = { durationHours = TimeUtils.Presets.ONE_HOUR },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "6時間",
                            onClick = { durationHours = TimeUtils.Presets.SIX_HOURS },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "24時間",
                            onClick = { durationHours = TimeUtils.Presets.ONE_DAY },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            text = "3日",
                            onClick = { durationHours = TimeUtils.Presets.THREE_DAYS },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "7日",
                            onClick = { durationHours = TimeUtils.Presets.ONE_WEEK },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 削除予定時刻プレビュー
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    val deleteAt = TimeUtils.getDeleteAtFromNow(durationHours)
                    val previewText = TimeUtils.calculateRemainingTime(deleteAt)

                    Text(
                        text = "削除予定: $previewText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * プリセットボタンコンポーネント
 */
@Composable
fun PresetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}
