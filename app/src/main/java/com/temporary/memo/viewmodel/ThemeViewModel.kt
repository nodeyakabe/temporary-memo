package com.temporary.memo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * テーマViewModel
 *
 * テーマの選択状態管理とSharedPreferencesへの設定保存。
 */
class ThemeViewModel(context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_SELECTION = "theme_selection"
        private const val DEFAULT_THEME = "light"
    }

    private val _selectedTheme = MutableStateFlow(
        prefs.getString(KEY_THEME_SELECTION, DEFAULT_THEME) ?: DEFAULT_THEME
    )
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    /**
     * テーマを設定
     * @param theme "dark", "light", "orange", "blue"
     */
    fun setTheme(theme: String) {
        _selectedTheme.value = theme
        prefs.edit().putString(KEY_THEME_SELECTION, theme).apply()
    }
}

/**
 * テーマの種類
 */
object ThemeType {
    const val DARK = "dark"
    const val LIGHT = "light"
    const val ORANGE = "orange"
    const val BLUE = "blue"
}
