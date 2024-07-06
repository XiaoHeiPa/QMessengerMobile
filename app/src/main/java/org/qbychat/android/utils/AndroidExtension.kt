package org.qbychat.android.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

val ComponentActivity.vibrator: Vibrator
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

fun Int.translate(mContext: Context): String = mContext.getString(this)
fun Int.translate(application: Application): String = application.getString(this)

fun String.requestPermission(activity: Activity) {

    if (Build.VERSION.SDK_INT >= 33) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                this
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    this
                )
            ) {
                activity.launchSettings()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(this), 100)
            }
        }
    }
}

fun Context.launchSettings() {
    try {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, this.applicationInfo.uid)
        }
        intent.putExtra("app_package", this.packageName)
        intent.putExtra("app_uid", this.applicationInfo.uid)
        this.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.setData(uri)
        this.startActivity(intent)
    }
}