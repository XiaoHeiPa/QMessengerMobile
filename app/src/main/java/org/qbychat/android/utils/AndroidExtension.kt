package org.qbychat.android.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity

val ComponentActivity.vibrator: Vibrator
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }