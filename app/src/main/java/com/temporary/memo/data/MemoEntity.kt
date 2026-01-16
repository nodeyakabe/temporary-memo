package com.temporary.memo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * メモエンティティ
 *
 * 期限付きメモのデータモデル。
 * 期限到達時に自動削除される設計。
 */
@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,  // Unix timestamp (milliseconds)

    @ColumnInfo(name = "delete_at")
    val deleteAt: Long    // Unix timestamp (milliseconds)
) {
    /**
     * メモが期限切れかどうかを判定
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= deleteAt
    }

    /**
     * メモの冒頭テキストを取得（最大3行）
     */
    fun getPreviewText(maxLines: Int = 3): String {
        val lines = text.lines()
        return lines.take(maxLines).joinToString("\n")
    }
}
