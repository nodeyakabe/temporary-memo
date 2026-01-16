package com.temporary.memo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.temporary.memo.data.MemoEntity
import com.temporary.memo.utils.TimeUtils
import com.temporary.memo.viewmodel.MemoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * メモ一覧画面
 *
 * 全メモをリスト表示し、新規作成・編集・削除操作を提供。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MemoListScreen(
    memoViewModel: MemoViewModel,
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val memoList by memoViewModel.memoList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<MemoEntity?>(null) }

    // 画面表示時に期限切れメモを削除
    LaunchedEffect(Unit) {
        memoViewModel.deleteExpiredMemos()
    }

    // 定期的に期限切れメモを削除（1分ごと）
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000) // 1分
            memoViewModel.deleteExpiredMemos()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("一時保存メモ") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = "新規作成")
            }
        }
    ) { paddingValues ->
        if (memoList.isEmpty()) {
            // 空状態
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "メモがまだありません",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "右下の + ボタンをタップして\n最初のメモを作成しましょう",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = memoList,
                    key = { it.id }
                ) { memo ->
                    MemoCard(
                        memo = memo,
                        onClick = { onNavigateToEdit(memo.id) },
                        onLongClick = { showDeleteDialog = memo }
                    )
                }
            }
        }
    }

    // 削除確認ダイアログ
    showDeleteDialog?.let { memo ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("メモを削除") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("このメモを削除しますか？")
                    Text(
                        text = "⚠️ この操作は取り消せません。\n削除されたメモは復元できません。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        memoViewModel.deleteMemo(memo)
                        showDeleteDialog = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

/**
 * メモカードコンポーネント
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MemoCard(
    memo: MemoEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val remainingTime = TimeUtils.calculateRemainingTime(memo.deleteAt)
    val timeColor = TimeUtils.getRemainingTimeColor(memo.deleteAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // メモ本文（冒頭3行）
            Text(
                text = memo.getPreviewText(maxLines = 3),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 残り時間
            Text(
                text = remainingTime,
                style = MaterialTheme.typography.labelMedium,
                color = timeColor
            )
        }
    }
}
