package com.temporary.memo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * メモDAO
 *
 * データベース操作のインターフェース。
 * 期限切れメモの抽出と削除を効率的に行う。
 */
@Dao
interface MemoDao {
    /**
     * 全メモを作成日時の降順で取得（Flow）
     */
    @Query("SELECT * FROM memos ORDER BY created_at DESC")
    fun getAllMemos(): Flow<List<MemoEntity>>

    /**
     * 期限切れメモを取得
     *
     * @param currentTime 現在時刻（Unix timestamp）
     * @return 期限切れメモのリスト
     */
    @Query("SELECT * FROM memos WHERE delete_at <= :currentTime")
    suspend fun getMemosExpiredBefore(currentTime: Long): List<MemoEntity>

    /**
     * 有効なメモ（期限切れでない）を最大3件取得（ウィジェット用）
     */
    @Query("SELECT * FROM memos WHERE delete_at > :currentTime ORDER BY delete_at ASC LIMIT 3")
    suspend fun getValidMemosForWidget(currentTime: Long): List<MemoEntity>

    /**
     * IDでメモを取得
     */
    @Query("SELECT * FROM memos WHERE id = :memoId")
    suspend fun getMemoById(memoId: Long): MemoEntity?

    /**
     * メモを挿入
     *
     * @return 挿入されたメモのID
     */
    @Insert
    suspend fun insert(memo: MemoEntity): Long

    /**
     * メモを更新
     */
    @Update
    suspend fun update(memo: MemoEntity)

    /**
     * メモを削除
     */
    @Delete
    suspend fun delete(memo: MemoEntity)

    /**
     * IDでメモを削除
     */
    @Query("DELETE FROM memos WHERE id = :memoId")
    suspend fun deleteById(memoId: Long)

    /**
     * 全メモを削除（デバッグ用）
     */
    @Query("DELETE FROM memos")
    suspend fun deleteAll()
}
