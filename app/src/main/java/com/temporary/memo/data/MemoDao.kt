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
     * 全メモを期限が近い順で取得（Flow）
     */
    @Query("SELECT * FROM memos ORDER BY delete_at ASC")
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
     * 有効なメモ（期限切れでない）を期限が近い順で最大3件取得（ウィジェット用）
     * アプリのメモリストと同じ並び順で統一
     */
    @Query("SELECT * FROM memos WHERE delete_at > :currentTime ORDER BY delete_at ASC LIMIT 3")
    suspend fun getValidMemosForWidget(currentTime: Long): List<MemoEntity>

    /**
     * 有効なメモの件数を取得（ウィジェット用）
     */
    @Query("SELECT COUNT(*) FROM memos WHERE delete_at > :currentTime")
    suspend fun getValidMemoCount(currentTime: Long): Int

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
     * メモを直接更新（トランザクションで安全）
     *
     * @return 更新された行数
     */
    @Query("UPDATE memos SET text = :text, delete_at = :deleteAt WHERE id = :id")
    suspend fun updateMemoDirectly(id: Long, text: String, deleteAt: Long): Int

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
     * 期限切れメモを一括削除（最適化版）
     *
     * @param currentTime 現在時刻
     * @param excludeId 除外するメモID（編集中メモ）
     * @return 削除された行数
     */
    @Query("DELETE FROM memos WHERE delete_at <= :currentTime AND id != :excludeId")
    suspend fun deleteExpiredMemosExcept(currentTime: Long, excludeId: Long): Int

    /**
     * 期限切れメモを一括削除（除外なし）
     *
     * @param currentTime 現在時刻
     * @return 削除された行数
     */
    @Query("DELETE FROM memos WHERE delete_at <= :currentTime")
    suspend fun deleteExpiredMemos(currentTime: Long): Int

    /**
     * 全メモを削除（デバッグ用）
     */
    @Query("DELETE FROM memos")
    suspend fun deleteAll()
}
