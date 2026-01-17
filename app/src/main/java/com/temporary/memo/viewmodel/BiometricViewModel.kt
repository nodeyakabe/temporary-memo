package com.temporary.memo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.temporary.memo.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 生体認証ViewModel
 *
 * 生体認証の状態管理とSharedPreferencesへの設定保存。
 */
class BiometricViewModel(private val context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val DEFAULT_BIOMETRIC_ENABLED = false
    }

    private val _biometricEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_BIOMETRIC_ENABLED, DEFAULT_BIOMETRIC_ENABLED)
    )
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * 生体認証の有効/無効を切り替え
     */
    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        // ウィジェットを更新してセキュリティ設定を反映
        WidgetUpdateHelper.updateWidgets(context)
    }

    /**
     * 認証成功
     */
    fun onAuthSuccess() {
        _authState.value = AuthState.Success
    }

    /**
     * 認証失敗
     */
    fun onAuthError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    /**
     * 認証状態をリセット
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

/**
 * 認証状態
 */
sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
