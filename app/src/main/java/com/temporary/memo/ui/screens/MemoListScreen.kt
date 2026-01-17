package com.temporary.memo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.temporary.memo.data.MemoEntity
import com.temporary.memo.utils.TimeUtils
import com.temporary.memo.viewmodel.MemoViewModel
import com.temporary.memo.widget.WidgetUpdateHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Undo用に削除したメモを一時保存
    var recentlyDeletedMemo by remember { mutableStateOf<MemoEntity?>(null) }

    // 画面表示時に期限切れメモを削除
    LaunchedEffect(Unit) {
        memoViewModel.deleteExpiredMemos()
    }

    // 定期的に期限切れメモを削除（1分ごと）
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                delay(60_000) // 1分
                memoViewModel.deleteExpiredMemos()
            } catch (e: CancellationException) {
                // キャンセル時は再スロー
                throw e
            } catch (e: Exception) {
                // その他のエラーはログに記録して続行
                android.util.Log.e("MemoListScreen", "Failed to delete expired memos", e)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ポイメモ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        floatingActionButton = {
            // FABタップ時のバウンスアニメーション
            var isPressed by remember { mutableStateOf(false) }
            val fabScale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fabScale"
            )

            FloatingActionButton(
                onClick = { onNavigateToEdit(null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .scale(fabScale)
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
                Icon(Icons.Default.Add, contentDescription = "新規作成")
            }
        }
    ) { paddingValues ->
        if (memoList.isEmpty()) {
            // 空状態 - ふわふわ浮くアニメーション付き
            val infiniteTransition = rememberInfiniteTransition(label = "emptyStateFloat")
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "floatOffset"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer {
                                translationY = -floatOffset
                            },
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "メモはまだありません",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "右下の + ボタンから\n新しいメモを作成できます",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
                itemsIndexed(
                    items = memoList,
                    key = { _, memo -> memo.id }
                ) { index, memo ->
                    // Staggered animation: 各アイテムが順番にふわっと表示
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(memo.id) {
                        delay(index * 50L) // 50msずつ遅延
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + expandVertically(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ),
                        exit = fadeOut(
                            animationSpec = tween(200)
                        ) + shrinkVertically(
                            animationSpec = tween(200)
                        )
                    ) {
                        MemoCard(
                            memo = memo,
                            onClick = { onNavigateToEdit(memo.id) },
                            onLongClick = { showDeleteDialog = memo }
                        )
                    }
                }
            }
        }
    }

    // 削除確認ダイアログ - Undo機能付き
    showDeleteDialog?.let { memo ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(16.dp),
            title = { Text("メモの削除") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "このメモを削除してもよろしいですか？",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "削除後、数秒以内であれば「元に戻す」で復元できます。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 削除前にメモを保存（Undo用）
                        recentlyDeletedMemo = memo
                        memoViewModel.deleteMemo(memo)
                        // ウィジェットを更新
                        WidgetUpdateHelper.updateWidgets(context)
                        showDeleteDialog = null

                        // Snackbarで通知とUndo機能
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "メモを削除しました",
                                actionLabel = "元に戻す",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Undo: メモを復元
                                recentlyDeletedMemo?.let { deletedMemo ->
                                    memoViewModel.restoreMemo(deletedMemo)
                                    WidgetUpdateHelper.updateWidgets(context)
                                }
                            }
                            recentlyDeletedMemo = null
                        }
                    }
                ) {
                    Text("削除する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("やめる")
                }
            }
        )
    }
}

/**
 * メモカードコンポーネント
 * タップ時のスケールアニメーション付き
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

    // タップ時のスケールアニメーション
    var isPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // メモ本文（冒頭3行）
            Text(
                text = memo.getPreviewText(maxLines = 3),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 残り時間
            Text(
                text = remainingTime,
                style = MaterialTheme.typography.labelMedium,
                color = timeColor
            )
        }
    }
}
