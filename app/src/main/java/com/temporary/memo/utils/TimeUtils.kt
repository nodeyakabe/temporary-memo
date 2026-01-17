package com.temporary.memo.utils

import androidx.compose.ui.graphics.Color
import java.util.concurrent.TimeUnit

/**
 * 時間計算ユーティリティ
 *
 * 残り時間の計算と表示形式変換を提供。
 */
object TimeUtils {

    /**
     * 残り時間を人間が読みやすい形式で取得
     *
     * @param deleteAt 削除予定時刻（Unix timestamp）
     * @return 残り時間の文字列（例: "あと3時間12分", "2日後"）
     */
    fun calculateRemainingTime(deleteAt: Long): String {
        val now = System.currentTimeMillis()
        val remaining = deleteAt - now

        if (remaining <= 0) return "期限切れ"

        val days = TimeUnit.MILLISECONDS.toDays(remaining)
        val hours = TimeUnit.MILLISECONDS.toHours(remaining) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60

        return when {
            days > 0 -> {
                if (hours > 0) {
                    "${days}日${hours}時間後"
                } else {
                    "${days}日後"
                }
            }
            hours > 0 -> {
                if (minutes > 0) {
                    "あと${hours}時間${minutes}分"
                } else {
                    "あと${hours}時間"
                }
            }
            minutes > 0 -> "あと${minutes}分"
            else -> "まもなく削除"  // 1分未満の場合
        }
    }

    /**
     * 残り時間に応じた色を取得
     *
     * 仕様:
     * - 緑: 24時間以上残り
     * - 黄色: 3時間〜24時間
     * - オレンジ: 1時間〜3時間
     * - 赤: 1時間未満
     *
     * @param deleteAt 削除予定時刻（Unix timestamp）
     * @return 残り時間を表す色
     */
    fun getRemainingTimeColor(deleteAt: Long): Color {
        val now = System.currentTimeMillis()
        val remaining = deleteAt - now
        val hours = TimeUnit.MILLISECONDS.toHours(remaining)

        return when {
            hours >= 24 -> Color(0xFF4CAF50)      // 緑
            hours >= 3 -> Color(0xFFFFB300)       // 黄色
            hours >= 1 -> Color(0xFFFF6F00)       // オレンジ
            else -> Color(0xFFF44336)             // 赤
        }
    }

    /**
     * 時間単位を時間数に変換
     *
     * @param hours 時間数
     * @return ミリ秒
     */
    fun hoursToMillis(hours: Int): Long {
        return TimeUnit.HOURS.toMillis(hours.toLong())
    }

    /**
     * 現在時刻から指定時間後のタイムスタンプを取得
     *
     * @param hours 何時間後
     * @return Unix timestamp
     */
    fun getDeleteAtFromNow(hours: Int): Long {
        return System.currentTimeMillis() + hoursToMillis(hours)
    }

    /**
     * 期限設定用のプリセット値
     */
    object Presets {
        const val ONE_HOUR = 1
        const val THREE_HOURS = 3
        const val SIX_HOURS = 6
        const val TWELVE_HOURS = 12
        const val ONE_DAY = 24
        const val THREE_DAYS = 72
        const val ONE_WEEK = 168
    }
}
