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
        // BIOMETRIC_WEAK を追加して顔認証にも対応
        // 指紋、顔認証、PINなど幅広い認証方法に対応
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
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

                    // エラーコードに応じた親切なメッセージを表示
                    val message = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            "認証をキャンセルしました"
                        BiometricPrompt.ERROR_LOCKOUT ->
                            "認証の試行回数が上限に達しました。少し時間をおいてから、もう一度お試しください。"
                        BiometricPrompt.ERROR_TIMEOUT ->
                            "認証がタイムアウトしました。下のボタンからもう一度お試しください。"
                        BiometricPrompt.ERROR_NO_BIOMETRICS ->
                            "端末に認証方法が登録されていません。\n設定 → セキュリティ → 画面ロック から登録してください。\n登録後、この機能が利用できます。"
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                            "現在、認証センサーをご利用いただけません。"
                        else ->
                            "認証できませんでした。もう一度お試しください。"
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
            .setTitle("ポイメモのロック解除")
            .setSubtitle("端末に登録された認証方法でロック解除")
            .setDescription("指紋認証、顔認証、画面ロック(PIN/パターン/パスワード)が利用できます")
            .setConfirmationRequired(false)  // 顔認証を自動承認
            // BIOMETRIC_WEAKを追加して顔認証にも対応
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
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
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                "認証機能をご利用いただけます"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "この端末には認証センサーがありません"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "認証センサーが一時的にご利用いただけません"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "端末の設定 → セキュリティ → 画面ロック から認証方法を登録してください"
            else -> "認証機能をご利用いただけません"
        }
    }
}
