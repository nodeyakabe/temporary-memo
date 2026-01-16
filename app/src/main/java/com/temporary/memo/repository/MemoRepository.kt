package com.temporary.memo.repository

import com.temporary.memo.data.MemoDao
import com.temporary.memo.data.MemoEntity
import kotlinx.coroutines.flow.Flow

/**
 * メモリポジトリ
 *
 * データアクセスの抽象化層。
 * DAOへのアクセスを一元管理し、自動削除ロジックを実装。
 */
class MemoRepository(private val memoDao: MemoDao) {

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
     * 期限切れメモを自動削除
     *
     * アプリ起動時および定期的に実行される。
     * 冪等性を保証（複数回実行しても問題なし）。
     */
    suspend fun deleteExpiredMemos() {
        val currentTime = System.currentTimeMillis()
        val expiredMemos = memoDao.getMemosExpiredBefore(currentTime)

        // 期限切れメモを1件ずつ削除
        expiredMemos.forEach { memo ->
            memoDao.delete(memo)
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
     * 全メモを削除（デバッグ用）
     */
    suspend fun deleteAll() {
        memoDao.deleteAll()
    }
}
