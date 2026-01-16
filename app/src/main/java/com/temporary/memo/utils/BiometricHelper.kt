package com.temporary.memo.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 生体認証ヘルパー
 *
 * Android BiometricPrompt APIのラッパー。
 * 生体認証の実行と結果処理を簡潔に行う。
 */
object BiometricHelper {

    /**
     * 端末が生体認証に対応しているかチェック
     *
     * @param context コンテキスト
     * @return true: 対応している、false: 対応していない
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * 生体認証を実行
     *
     * @param activity FragmentActivity
     * @param onSuccess 認証成功時のコールバック
     * @param onError 認証失敗時のコールバック
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    // エラーコードに応じたメッセージを表示
                    val message = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            "認証がキャンセルされました"
                        BiometricPrompt.ERROR_LOCKOUT ->
                            "試行回数が上限に達しました。しばらく待ってから再度お試しください。"
                        BiometricPrompt.ERROR_TIMEOUT ->
                            "認証がタイムアウトしました。もう一度お試しください。"
                        BiometricPrompt.ERROR_NO_BIOMETRICS ->
                            "生体認証が登録されていません。端末の設定から登録してください。"
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                            "生体認証センサーが利用できません。"
                        else ->
                            "認証に失敗しました: ${errString}"
                    }
                    onError(message)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // 認証失敗（指紋が一致しない等）
                    // エラーコールバックは呼ばず、再試行を促す
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("メモのロックを解除")
            .setSubtitle("生体認証でアプリを開きます")
            .setNegativeButtonText("キャンセル")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * 生体認証が利用可能かどうかを詳細に確認
     *
     * @param context コンテキスト
     * @return 状態メッセージ
     */
    fun getBiometricStatus(context: Context): String {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                "生体認証が利用可能です"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "この端末には生体認証センサーがありません"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "生体認証センサーが現在利用できません"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "生体認証が登録されていません"
            else -> "生体認証が利用できません"
        }
    }
}
