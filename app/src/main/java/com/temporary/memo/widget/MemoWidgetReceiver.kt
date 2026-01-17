package com.temporary.memo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.temporary.memo.MainActivity
import com.temporary.memo.R
import com.temporary.memo.data.MemoDatabase
import com.temporary.memo.repository.MemoRepository
import com.temporary.memo.utils.TimeUtils
import kotlinx.coroutines.withTimeout
import android.util.Log

/**
 * メモウィジェットReceiver
 *
 * ホーム画面ウィジェットを管理。
 * 有効なメモ上位3件を表示。
 */
class MemoWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("MemoWidgetReceiver", "onUpdate called for ${appWidgetIds.size} widgets")

        // まず即座にローディング状態を表示（ユーザーフィードバック）
        for (appWidgetId in appWidgetIds) {
            showLoadingWidget(context, appWidgetManager, appWidgetId)
        }

        // goAsync()を正しく使用: コルーチンの完了を待つ
        val pendingResult = goAsync()

        // ApplicationScopeを使用してライフサイクルに依存しないCoroutineを起動
        val applicationContext = context.applicationContext

        // 別スレッドで実行し、pendingResult.finish()を必ず呼ぶ
        Thread {
            try {
                // ブロッキング呼び出しで同期的にコルーチンを実行
                kotlinx.coroutines.runBlocking {
                    // 9秒のタイムアウトを設定（10秒制限より余裕を持たせる）
                    withTimeout(9_000) {
                        Log.d("MemoWidgetReceiver", "Starting data fetch")

                        // データベースとメモを取得
                        val database = try {
                            MemoDatabase.getDatabase(applicationContext)
                        } catch (e: Exception) {
                            Log.e("MemoWidgetReceiver", "Database initialization failed", e)
                            throw e
                        }

                        val repository = MemoRepository(database.memoDao())
                        val memos = try {
                            repository.getValidMemosForWidget()
                        } catch (e: Exception) {
                            Log.e("MemoWidgetReceiver", "Failed to fetch memos", e)
                            throw e
                        }
                        val totalMemoCount = try {
                            repository.getValidMemoCount()
                        } catch (e: Exception) {
                            Log.e("MemoWidgetReceiver", "Failed to get memo count", e)
                            memos.size
                        }

                        Log.d("MemoWidgetReceiver", "Found $totalMemoCount memos total for all widgets")

                        // ウィジェット更新
                        for (appWidgetId in appWidgetIds) {
                            try {
                                Log.d("MemoWidgetReceiver", "Updating widget $appWidgetId")
                                updateAppWidget(applicationContext, appWidgetManager, appWidgetId, memos, totalMemoCount)
                            } catch (e: Exception) {
                                Log.e("MemoWidgetReceiver", "Failed to update widget $appWidgetId", e)
                                showErrorWidget(applicationContext, appWidgetManager, appWidgetId, e)
                            }
                        }
                    }
                }
                Log.d("MemoWidgetReceiver", "All widgets updated successfully")
            } catch (e: Exception) {
                Log.e("MemoWidgetReceiver", "Widget update failed", e)
                // エラー時にも全てのウィジェットにエラーメッセージを表示
                try {
                    for (appWidgetId in appWidgetIds) {
                        showErrorWidget(applicationContext, appWidgetManager, appWidgetId, e)
                    }
                } catch (innerException: Exception) {
                    Log.e("MemoWidgetReceiver", "Failed to show error widget", innerException)
                }
            } finally {
                // 処理完了を必ず通知
                try {
                    pendingResult.finish()
                    Log.d("MemoWidgetReceiver", "pendingResult.finish() called")
                } catch (e: Exception) {
                    Log.e("MemoWidgetReceiver", "Failed to finish pendingResult", e)
                }
            }
        }.start()
    }

    private fun showLoadingWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        try {
            val views = RemoteViews(context.packageName, R.layout.memo_widget)
            views.setTextViewText(R.id.widget_title, "ポイメモ")
            views.setTextViewText(R.id.memo1_text, "読み込み中...")
            views.setTextViewText(R.id.memo1_time, "")
            views.setTextViewText(R.id.memo2_text, "")
            views.setTextViewText(R.id.memo2_time, "")
            views.setTextViewText(R.id.memo3_text, "")
            views.setTextViewText(R.id.memo3_time, "")

            // ウィジェットタップでアプリ起動
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("MemoWidgetReceiver", "Loading widget displayed for $appWidgetId")
        } catch (e: Exception) {
            Log.e("MemoWidgetReceiver", "Failed to create loading widget view", e)
        }
    }

    private fun showErrorWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        error: Exception
    ) {
        try {
            val views = RemoteViews(context.packageName, R.layout.memo_widget)
            views.setTextViewText(R.id.widget_title, "ポイメモ")
            views.setTextViewText(R.id.memo1_text, "ウィジェットの読み込みに失敗しました")

            // デバッグ用にエラー詳細を表示
            val errorMessage = when {
                error.message?.contains("timeout", ignoreCase = true) == true ->
                    "タイムアウト: 時間内に処理が完了しませんでした"
                error.message?.contains("database", ignoreCase = true) == true ->
                    "データベースエラー: ${error.message}"
                else ->
                    "エラー: ${error.javaClass.simpleName}"
            }

            views.setTextViewText(R.id.memo1_time, errorMessage)
            views.setTextViewText(R.id.memo2_text, "タップしてアプリを開く")
            views.setTextViewText(R.id.memo2_time, "")
            views.setTextViewText(R.id.memo3_text, "")
            views.setTextViewText(R.id.memo3_time, "")

            // ウィジェットタップでアプリ起動
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("MemoWidgetReceiver", "Error widget displayed for $appWidgetId: ${error.message}")
        } catch (e: Exception) {
            Log.e("MemoWidgetReceiver", "Failed to create error widget view", e)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        memos: List<com.temporary.memo.data.MemoEntity>,
        totalMemoCount: Int
    ) {
        try {
            Log.d("MemoWidgetReceiver", "Creating RemoteViews for widget $appWidgetId")
            val views = RemoteViews(context.packageName, R.layout.memo_widget)

            // タイトル設定
            views.setTextViewText(R.id.widget_title, "ポイメモ")

            // セキュリティ設定を確認
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val isBiometricEnabled = prefs.getBoolean("biometric_enabled", false)

            // セキュリティがオンの場合は内容を非表示
            if (isBiometricEnabled) {
                Log.d("MemoWidgetReceiver", "Security enabled, hiding memo contents")
                views.setTextViewText(R.id.memo1_text, "🔒 認証が必要です")
                views.setTextViewText(R.id.memo1_time, "")
                views.setTextViewText(R.id.memo2_text, "メモ: ${totalMemoCount}件")
                // 最初のメモの残り時間を表示
                if (memos.isNotEmpty()) {
                    views.setTextViewText(R.id.memo2_time, "⏰ " + TimeUtils.calculateRemainingTime(memos[0].deleteAt))
                } else {
                    views.setTextViewText(R.id.memo2_time, "")
                }
                views.setTextViewText(R.id.memo3_text, "")
                views.setTextViewText(R.id.memo3_time, "タップしてアプリを開く")
            } else {
                // メモ表示（最大3件）
                if (memos.isEmpty()) {
                    Log.d("MemoWidgetReceiver", "No memos found, showing empty message")
                    views.setTextViewText(R.id.memo1_text, "メモはまだありません")
                    views.setTextViewText(R.id.memo1_time, "")
                    views.setTextViewText(R.id.memo2_text, "")
                    views.setTextViewText(R.id.memo2_time, "")
                    views.setTextViewText(R.id.memo3_text, "")
                    views.setTextViewText(R.id.memo3_time, "")
                } else {
                    // メモ1
                    val memo1 = memos[0]
                    views.setTextViewText(R.id.memo1_text, memo1.getPreviewText(1))
                    views.setTextViewText(R.id.memo1_time, "⏰ " + TimeUtils.calculateRemainingTime(memo1.deleteAt))

                    // メモ2
                    if (memos.size > 1) {
                        val memo2 = memos[1]
                        views.setTextViewText(R.id.memo2_text, memo2.getPreviewText(1))
                        views.setTextViewText(R.id.memo2_time, "⏰ " + TimeUtils.calculateRemainingTime(memo2.deleteAt))
                    } else {
                        views.setTextViewText(R.id.memo2_text, "")
                        views.setTextViewText(R.id.memo2_time, "")
                    }

                    // メモ3
                    if (memos.size > 2) {
                        val memo3 = memos[2]
                        views.setTextViewText(R.id.memo3_text, memo3.getPreviewText(1))
                        views.setTextViewText(R.id.memo3_time, "⏰ " + TimeUtils.calculateRemainingTime(memo3.deleteAt))
                    } else {
                        views.setTextViewText(R.id.memo3_text, "")
                        views.setTextViewText(R.id.memo3_time, "")
                    }
                }
            }

            // ウィジェットタップでアプリ起動
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,  // appWidgetIdを使用して複数ウィジェット対応
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            Log.d("MemoWidgetReceiver", "Updating widget $appWidgetId with AppWidgetManager")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("MemoWidgetReceiver", "Widget $appWidgetId updated successfully")
        } catch (e: Exception) {
            Log.e("MemoWidgetReceiver", "Failed to update widget $appWidgetId", e)
            // エラー時にも基本的なビューを表示
            try {
                showErrorWidget(context, appWidgetManager, appWidgetId, e)
            } catch (innerException: Exception) {
                Log.e("MemoWidgetReceiver", "Failed to show error in updateAppWidget", innerException)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // 最初のウィジェットが作成されたときに呼ばれる
        Log.d("MemoWidgetReceiver", "Widget enabled")
    }

    override fun onDisabled(context: Context) {
        // 最後のウィジェットが削除されたときに呼ばれる
        Log.d("MemoWidgetReceiver", "Widget disabled")
    }
}
