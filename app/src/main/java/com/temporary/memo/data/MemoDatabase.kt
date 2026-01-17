package com.temporary.memo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Roomデータベース
 *
 * メモデータをローカルに保存。
 * 暗号化は意図的に実装しない（仕様通り）。
 */
@Database(
    entities = [MemoEntity::class],
    version = 1,
    exportSchema = true  // スキーマエクスポートを有効化（将来のマイグレーション用）
)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        /**
         * データベースインスタンスを取得（シングルトン）
         */
        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                )
                    // 暗号化なし（意図的）
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
