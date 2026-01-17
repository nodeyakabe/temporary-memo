package com.temporary.memo.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.temporary.memo.utils.TimeUtils
import com.temporary.memo.viewmodel.MemoViewModel
import com.temporary.memo.widget.WidgetUpdateHelper
import kotlinx.coroutines.delay
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
    var text by rememberSaveable { mutableStateOf("") }
    val maxCharacters = 3000  // 最大文字数
    var durationHours by rememberSaveable { mutableStateOf(24) }  // デフォルト24時間
    var selectedPresetHours by rememberSaveable { mutableStateOf<Int?>(24) }  // 選択中のプリセット（デフォルトは24時間）
    var isSaving by remember { mutableStateOf(false) }  // 保存中フラグ（画面回転で保存中状態はリセット）
    var showDeleteDialog by remember { mutableStateOf(false) }  // 削除確認ダイアログ
    val scope = rememberCoroutineScope()
    val isNewMemo = memoId == null
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // 新規メモの場合、自動的に入力欄にフォーカスを当てる
    LaunchedEffect(Unit) {
        if (isNewMemo) {
            delay(100)  // キーボード表示のため少し待つ
            focusRequester.requestFocus()
        }
    }

    // 既存メモの場合、データを読み込む
    LaunchedEffect(memoId) {
        if (memoId != null) {
            // 編集中メモとして登録（自動削除から保護）
            memoViewModel.setEditingMemoId(memoId)

            try {
                val memo = memoViewModel.getMemoById(memoId)
                if (memo != null) {
                    text = memo.text
                    // 残り時間から時間数を計算
                    val remaining = memo.deleteAt - System.currentTimeMillis()
                    durationHours = (remaining / (1000 * 60 * 60)).toInt().coerceAtLeast(1)

                    // プリセット値と一致する場合は選択状態にする
                    selectedPresetHours = when (durationHours) {
                        TimeUtils.Presets.ONE_HOUR -> TimeUtils.Presets.ONE_HOUR
                        TimeUtils.Presets.SIX_HOURS -> TimeUtils.Presets.SIX_HOURS
                        TimeUtils.Presets.ONE_DAY -> TimeUtils.Presets.ONE_DAY
                        TimeUtils.Presets.THREE_DAYS -> TimeUtils.Presets.THREE_DAYS
                        TimeUtils.Presets.ONE_WEEK -> TimeUtils.Presets.ONE_WEEK
                        else -> null  // プリセット以外の値の場合は選択なし
                    }
                } else {
                    // メモが見つからない場合
                    snackbarHostState.showSnackbar(
                        message = "このメモは見つかりませんでした。すでに削除された可能性があります。",
                        duration = SnackbarDuration.Long
                    )
                }
            } catch (e: Exception) {
                // メモの読み込みに失敗した場合
                android.util.Log.e("MemoEditScreen", "Failed to load memo", e)
                snackbarHostState.showSnackbar(
                    message = "メモを読み込めませんでした。もう一度お試しください。",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // 画面離脱時に編集中メモIDをクリア
    DisposableEffect(Unit) {
        onDispose {
            memoViewModel.setEditingMemoId(null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isNewMemo) "新規メモ" else "メモ編集") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    // 削除ボタン（編集時のみ表示）
                    if (!isNewMemo) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !isSaving
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "削除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // 保存ボタン
                    TextButton(
                        onClick = {
                            if (isSaving) return@TextButton  // 保存中は何もしない
                            isSaving = true
                            scope.launch {
                                try {
                                    val success = if (isNewMemo) {
                                        memoViewModel.createMemo(text, durationHours)
                                    } else {
                                        memoId?.let {
                                            memoViewModel.updateMemo(it, text, durationHours)
                                        } ?: false
                                    }

                                    if (success) {
                                        // 保存成功時、編集中フラグをクリアしてから画面遷移
                                        memoViewModel.setEditingMemoId(null)
                                        // ウィジェットを更新
                                        WidgetUpdateHelper.updateWidgets(context)
                                        onNavigateBack()
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = "保存できませんでした。メモがすでに削除されている可能性があります。",
                                            duration = SnackbarDuration.Long
                                        )
                                        isSaving = false
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MemoEditScreen", "Failed to save memo", e)
                                    snackbarHostState.showSnackbar(
                                        message = "予期しないエラーが発生しました。もう一度お試しください。",
                                        duration = SnackbarDuration.Long
                                    )
                                    isSaving = false
                                }
                            }
                        },
                        enabled = text.isNotBlank() && text.length <= maxCharacters && !isSaving
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // メモ入力欄
            val isOverLimit = text.length > maxCharacters
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    // 文字数制限を超えても入力は許可（警告表示のみ）
                    text = newText
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                label = { Text("メモの内容") },
                placeholder = {
                    Text(
                        text = "例: 今日の買い物リスト\n- 牛乳\n- 卵\n- パン",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isOverLimit) "文字数が上限を超えています" else "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${text.length} / $maxCharacters",
                            color = if (isOverLimit) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                isError = isOverLimit,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None
                ),
                maxLines = Int.MAX_VALUE
            )

            // 期限設定セクション
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "保存期間の設定",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // ヒントテキスト
                    Text(
                        text = "ℹ️ 設定した時間が経過すると、メモは自動的に削除されます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // プリセットボタン
                    Text(
                        text = "よく使う期間:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            text = "1時間",
                            isSelected = selectedPresetHours == TimeUtils.Presets.ONE_HOUR,
                            onClick = {
                                durationHours = TimeUtils.Presets.ONE_HOUR
                                selectedPresetHours = TimeUtils.Presets.ONE_HOUR
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "6時間",
                            isSelected = selectedPresetHours == TimeUtils.Presets.SIX_HOURS,
                            onClick = {
                                durationHours = TimeUtils.Presets.SIX_HOURS
                                selectedPresetHours = TimeUtils.Presets.SIX_HOURS
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "24時間",
                            isSelected = selectedPresetHours == TimeUtils.Presets.ONE_DAY,
                            onClick = {
                                durationHours = TimeUtils.Presets.ONE_DAY
                                selectedPresetHours = TimeUtils.Presets.ONE_DAY
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            text = "3日",
                            isSelected = selectedPresetHours == TimeUtils.Presets.THREE_DAYS,
                            onClick = {
                                durationHours = TimeUtils.Presets.THREE_DAYS
                                selectedPresetHours = TimeUtils.Presets.THREE_DAYS
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            text = "7日",
                            isSelected = selectedPresetHours == TimeUtils.Presets.ONE_WEEK,
                            onClick = {
                                durationHours = TimeUtils.Presets.ONE_WEEK
                                selectedPresetHours = TimeUtils.Presets.ONE_WEEK
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // スライダー（プリセットボタンの下に移動）
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "${durationHours}時間後に自動で削除されます",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = durationHours.toFloat(),
                        onValueChange = {
                            durationHours = it.toInt()
                            selectedPresetHours = null  // スライダー操作時は選択解除
                        },
                        valueRange = 1f..168f,  // 1時間〜7日
                        steps = 167
                    )

                    // 削除予定時刻プレビュー
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

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

        // 削除確認ダイアログ
        if (showDeleteDialog && !isNewMemo) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                shape = RoundedCornerShape(16.dp),
                title = { Text("メモの削除") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "このメモを削除してもよろしいですか？",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "削除すると元に戻せませんので、ご注意ください。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    memoId?.let { id ->
                                        // ViewModelから削除メソッドを呼び出す
                                        val memo = memoViewModel.getMemoById(id)
                                        if (memo != null) {
                                            memoViewModel.deleteMemo(memo)
                                            memoViewModel.setEditingMemoId(null)
                                            // ウィジェットを更新
                                            WidgetUpdateHelper.updateWidgets(context)
                                            showDeleteDialog = false
                                            onNavigateBack()
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "メモが見つかりません。すでに削除された可能性があります。",
                                                duration = SnackbarDuration.Long
                                            )
                                            showDeleteDialog = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MemoEditScreen", "Failed to delete memo", e)
                                    snackbarHostState.showSnackbar(
                                        message = "削除に失敗しました。もう一度お試しください。",
                                        duration = SnackbarDuration.Long
                                    )
                                    showDeleteDialog = false
                                }
                            }
                        }
                    ) {
                        Text("削除する", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("やめる")
                    }
                }
            )
        }
    }
}

/**
 * プリセットボタンコンポーネント
 * タップ時のスケールアニメーション付き
 * 選択状態でボタンの色が変わる
 */
@Composable
fun PresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "presetButtonScale"
    )

    // 選択状態によってボタンのスタイルを変更
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
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
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}
