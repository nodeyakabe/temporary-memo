package com.temporary.memo.repository

import com.temporary.memo.data.MemoDao
import com.temporary.memo.data.MemoEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicReference

/**
 * メモリポジトリ
 *
 * データアクセスの抽象化層。
 * DAOへのアクセスを一元管理し、自動削除ロジックを実装。
 */
class MemoRepository(private val memoDao: MemoDao) {

    /**
     * 編集中のメモID（自動削除から保護）
     * スレッドセーフな実装
     */
    private val editingMemoId = AtomicReference<Long?>(null)

    /**
     * 全メモをFlowで監視
     */
    val allMemos: Flow<List<MemoEntity>> = memoDao.getAllMemos()

    /**
     * メモを挿入
     */
    suspend fun insert(memo: MemoEntity): Long {
        return memoDao.insert(memo)
    }

    /**
     * メモを更新
     */
    suspend fun update(memo: MemoEntity) {
        memoDao.update(memo)
    }

    /**
     * メモを直接更新（競合状態を回避）
     *
     * @return 更新に成功した場合true
     */
    suspend fun updateMemoDirect(id: Long, text: String, deleteAt: Long): Boolean {
        return memoDao.updateMemoDirectly(id, text, deleteAt) > 0
    }

    /**
     * メモを削除
     */
    suspend fun delete(memo: MemoEntity) {
        memoDao.delete(memo)
    }

    /**
     * IDでメモを削除
     */
    suspend fun deleteById(memoId: Long) {
        memoDao.deleteById(memoId)
    }

    /**
     * IDでメモを取得
     */
    suspend fun getMemoById(memoId: Long): MemoEntity? {
        return memoDao.getMemoById(memoId)
    }

    /**
     * 編集中メモIDを設定（自動削除から保護）
     */
    fun setEditingMemoId(memoId: Long?) {
        editingMemoId.set(memoId)
    }

    /**
     * 期限切れメモを自動削除
     *
     * アプリ起動時および定期的に実行される。
     * 冪等性を保証（複数回実行しても問題なし）。
     * 編集中のメモは削除しない。
     * 最適化: 一括削除クエリを使用
     */
    suspend fun deleteExpiredMemos() {
        try {
            val currentTime = System.currentTimeMillis()
            val currentEditingId = editingMemoId.get()

            val deletedCount = if (currentEditingId != null) {
                // 編集中メモを除外して削除
                memoDao.deleteExpiredMemosExcept(currentTime, currentEditingId)
            } else {
                // すべての期限切れメモを削除
                memoDao.deleteExpiredMemos(currentTime)
            }

            if (deletedCount > 0) {
                android.util.Log.d("MemoRepository", "Deleted $deletedCount expired memo(s)")
            }
        } catch (e: Exception) {
            // エラーをログに記録
            android.util.Log.e("MemoRepository", "Failed to delete expired memos", e)
        }
    }

    /**
     * ウィジェット用に有効なメモ（最大3件）を取得
     */
    suspend fun getValidMemosForWidget(): List<MemoEntity> {
        val currentTime = System.currentTimeMillis()
        return memoDao.getValidMemosForWidget(currentTime)
    }

    /**
     * 有効なメモの総件数を取得（ウィジェット用）
     */
    suspend fun getValidMemoCount(): Int {
        val currentTime = System.currentTimeMillis()
        return memoDao.getValidMemoCount(currentTime)
    }

    /**
     * 全メモを削除（デバッグ用）
     */
    suspend fun deleteAll() {
        memoDao.deleteAll()
    }
}
