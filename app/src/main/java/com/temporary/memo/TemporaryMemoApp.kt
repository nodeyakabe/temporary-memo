package com.temporary.memo

import android.app.Application
import com.temporary.memo.data.MemoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Applicationクラス
 *
 * アプリケーション起動時の初期化処理。
 */
class TemporaryMemoApp : Application() {

    // アプリケーションスコープのCoroutine
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // データベースを事前初期化（ウィジェット初回読み込みの高速化）
        applicationScope.launch {
            try {
                MemoDatabase.getDatabase(applicationContext)
                android.util.Log.d("TemporaryMemoApp", "Database pre-initialized successfully")
            } catch (e: Exception) {
                android.util.Log.e("TemporaryMemoApp", "Database pre-initialization failed", e)
            }
        }
    }
}
