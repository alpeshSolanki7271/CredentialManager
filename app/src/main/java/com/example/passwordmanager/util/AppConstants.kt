package com.example.passwordmanager.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.example.passwordmanager.R
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AppConstants {

    companion object {

        const val TAG = "ModalBottomSheetDialog"

        fun calculatePasswordStrength(password: String): PasswordStrength {
            // Basic strength calculation logic
            val length = password.length
            val containsLowerCase = password.any { it.isLowerCase() }
            val containsUpperCase = password.any { it.isUpperCase() }
            val containsDigit = password.any { it.isDigit() }
            val containsSpecialChar = password.any { it.isLetterOrDigit().not() }

            var strength = 0

            if (length >= 8) strength++
            if (containsLowerCase) strength++
            if (containsUpperCase) strength++
            if (containsDigit) strength++
            if (containsSpecialChar) strength++

            return when (strength) {
                in 0..1 -> PasswordStrength.WEAK
                in 2..3 -> PasswordStrength.MODERATE
                else -> PasswordStrength.STRONG
            }
        }

        fun displayPasswordStrength(
            strength: PasswordStrength, strengthTv: AppCompatTextView, context: Context
        ) {
            when (strength) {
                PasswordStrength.WEAK -> {
                    strengthTv.text = "Weak"
                    strengthTv.setTextColor(ContextCompat.getColor(context, R.color.red))
                }

                PasswordStrength.MODERATE -> {
                    strengthTv.text = "Moderate"
                    strengthTv.setTextColor(ContextCompat.getColor(context, R.color.blue))
                }

                PasswordStrength.STRONG -> {
                    strengthTv.text = "Strong"
                    strengthTv.setTextColor(ContextCompat.getColor(context, R.color.green))
                }
            }
        }

        enum class PasswordStrength {
            WEAK, MODERATE, STRONG
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun encrypt(text: String, secretKey: String): String {
            val cipher = Cipher.getInstance("AES")
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(text.toByteArray())
            return Base64.getEncoder().encodeToString(encryptedBytes)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun decrypt(encryptedText: String, secretKey: String): String {
            val cipher = Cipher.getInstance("AES")
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val encryptedBytes = Base64.getDecoder().decode(encryptedText)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes)
        }


        fun parseDateString(dateString: String): Calendar {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            return Calendar.getInstance().apply {
                if (date != null) {
                    time = date
                }
            }
        }




    }
}