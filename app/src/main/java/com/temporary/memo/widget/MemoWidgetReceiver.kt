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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = MemoDatabase.getDatabase(context)
            val repository = MemoRepository(database.memoDao())
            val memos = repository.getValidMemosForWidget()

            kotlinx.coroutines.withContext(Dispatchers.Main) {
                val views = RemoteViews(context.packageName, R.layout.memo_widget)

                // タイトル設定
                views.setTextViewText(R.id.widget_title, "一時保存メモ")

                // メモ表示（最大3件）
                if (memos.isEmpty()) {
                    views.setTextViewText(R.id.memo1_text, "メモがありません")
                    views.setTextViewText(R.id.memo1_time, "")
                    views.setTextViewText(R.id.memo2_text, "")
                    views.setTextViewText(R.id.memo2_time, "")
                    views.setTextViewText(R.id.memo3_text, "")
                    views.setTextViewText(R.id.memo3_time, "")
                } else {
                    // メモ1
                    if (memos.isNotEmpty()) {
                        val memo1 = memos[0]
                        views.setTextViewText(R.id.memo1_text, memo1.getPreviewText(2))
                        views.setTextViewText(R.id.memo1_time, TimeUtils.calculateRemainingTime(memo1.deleteAt))
                    }

                    // メモ2
                    if (memos.size > 1) {
                        val memo2 = memos[1]
                        views.setTextViewText(R.id.memo2_text, memo2.getPreviewText(2))
                        views.setTextViewText(R.id.memo2_time, TimeUtils.calculateRemainingTime(memo2.deleteAt))
                    } else {
                        views.setTextViewText(R.id.memo2_text, "")
                        views.setTextViewText(R.id.memo2_time, "")
                    }

                    // メモ3
                    if (memos.size > 2) {
                        val memo3 = memos[2]
                        views.setTextViewText(R.id.memo3_text, memo3.getPreviewText(2))
                        views.setTextViewText(R.id.memo3_time, TimeUtils.calculateRemainingTime(memo3.deleteAt))
                    } else {
                        views.setTextViewText(R.id.memo3_text, "")
                        views.setTextViewText(R.id.memo3_time, "")
                    }
                }

                // ウィジェットタップでアプリ起動
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // 最初のウィジェットが作成されたときに呼ばれる
    }

    override fun onDisabled(context: Context) {
        // 最後のウィジェットが削除されたときに呼ばれる
    }
}
