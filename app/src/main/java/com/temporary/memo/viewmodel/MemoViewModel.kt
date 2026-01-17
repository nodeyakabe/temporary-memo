package com.temporary.memo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.temporary.memo.data.MemoEntity
import com.temporary.memo.repository.MemoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * メモViewModel
 *
 * UIとRepositoryの橋渡し。
 * メモのCRUD操作と自動削除ロジックを管理。
 */
class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    /**
     * 全メモのリスト（StateFlow）
     */
    val memoList: StateFlow<List<MemoEntity>> = repository.allMemos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // 初期化時に期限切れメモを削除
        deleteExpiredMemos()
    }

    /**
     * 期限切れメモを削除
     */
    fun deleteExpiredMemos() {
        viewModelScope.launch {
            repository.deleteExpiredMemos()
        }
    }

    /**
     * 新規メモを作成
     *
     * @param text メモ本文
     * @param durationHours 期限（時間単位）
     * @return 作成成功時true
     */
    suspend fun createMemo(text: String, durationHours: Int): Boolean {
        if (text.isBlank()) return false

        return try {
            val now = System.currentTimeMillis()
            val deleteAt = now + TimeUnit.HOURS.toMillis(durationHours.toLong())

            val memo = MemoEntity(
                text = text.trim(),
                createdAt = now,
                deleteAt = deleteAt
            )

            repository.insert(memo)
            true
        } catch (e: Exception) {
            android.util.Log.e("MemoViewModel", "Failed to create memo", e)
            false
        }
    }

    /**
     * メモを更新
     *
     * @param memoId メモID
     * @param newText 新しい本文
     * @param newDurationHours 新しい期限（時間単位）
     * @return 更新成功時true
     */
    suspend fun updateMemo(memoId: Long, newText: String, newDurationHours: Int): Boolean {
        if (newText.isBlank()) return false

        return try {
            val now = System.currentTimeMillis()
            val newDeleteAt = now + TimeUnit.HOURS.toMillis(newDurationHours.toLong())

            // 競合状態を回避するため、直接更新クエリを使用
            val success = repository.updateMemoDirect(memoId, newText.trim(), newDeleteAt)
            if (!success) {
                android.util.Log.w("MemoViewModel", "Failed to update memo $memoId - may have been deleted")
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MemoViewModel", "Failed to update memo", e)
            false
        }
    }

    /**
     * メモを削除
     */
    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.delete(memo)
        }
    }

    /**
     * IDでメモを削除
     */
    fun deleteMemoById(memoId: Long) {
        viewModelScope.launch {
            repository.deleteById(memoId)
        }
    }

    /**
     * IDでメモを取得
     */
    suspend fun getMemoById(memoId: Long): MemoEntity? {
        return repository.getMemoById(memoId)
    }

    /**
     * 編集中メモIDを設定（自動削除から保護）
     */
    fun setEditingMemoId(memoId: Long?) {
        repository.setEditingMemoId(memoId)
    }

    /**
     * 削除したメモを復元（Undo用）
     */
    fun restoreMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.insert(memo)
        }
    }
}

/**
 * ViewModelFactory
 */
class MemoViewModelFactory(
    private val repository: MemoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoViewModel::class.java)) {
            return MemoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
