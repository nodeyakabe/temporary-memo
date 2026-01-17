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
     * 空行のみの場合は適切に処理する
     */
    fun getPreviewText(maxLines: Int = 3): String {
        if (text.isBlank()) return "(空のメモ)"

        val lines = text.lines().filter { it.isNotBlank() }
        return if (lines.isEmpty()) {
            "(空のメモ)"
        } else {
            lines.take(maxLines).joinToString("\n")
        }
    }
}
