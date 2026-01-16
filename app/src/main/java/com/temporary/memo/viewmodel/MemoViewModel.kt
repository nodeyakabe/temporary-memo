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
     */
    fun createMemo(text: String, durationHours: Int) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val deleteAt = now + TimeUnit.HOURS.toMillis(durationHours.toLong())

            val memo = MemoEntity(
                text = text.trim(),
                createdAt = now,
                deleteAt = deleteAt
            )

            repository.insert(memo)
        }
    }

    /**
     * メモを更新
     *
     * @param memoId メモID
     * @param newText 新しい本文
     * @param newDurationHours 新しい期限（時間単位）
     */
    fun updateMemo(memoId: Long, newText: String, newDurationHours: Int) {
        if (newText.isBlank()) return

        viewModelScope.launch {
            val existingMemo = repository.getMemoById(memoId)
            if (existingMemo != null) {
                val now = System.currentTimeMillis()
                val newDeleteAt = now + TimeUnit.HOURS.toMillis(newDurationHours.toLong())

                val updatedMemo = existingMemo.copy(
                    text = newText.trim(),
                    deleteAt = newDeleteAt
                )

                repository.update(updatedMemo)
            }
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
