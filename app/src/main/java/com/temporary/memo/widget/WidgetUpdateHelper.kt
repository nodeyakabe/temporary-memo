package com.temporary.memo.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * ウィジェット更新ヘルパー
 *
 * メモの作成・更新・削除時にウィジェットを更新するためのユーティリティ
 */
object WidgetUpdateHelper {

    /**
     * 全てのウィジェットを更新
     *
     * メモの変更時に呼び出して、ウィジェットに最新情報を反映させる
     */
    fun updateWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MemoWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                // ウィジェット更新をトリガー
                val intent = Intent(context, MemoWidgetReceiver::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("WidgetUpdateHelper", "Failed to update widgets", e)
        }
    }
}
