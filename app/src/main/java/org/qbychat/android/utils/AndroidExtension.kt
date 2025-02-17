package org.qbychat.android.utils

import android.app.Activity
import android.app.Activity.NOTIFICATION_SERVICE
import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import kotlinx.serialization.json.Json
import org.qbychat.android.CHANNEL_ID
import java.io.Serializable

const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }

val Activity.vibrator: Vibrator
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

const val PICK_FILE_REQUEST_CODE = 1

fun Activity.openFilePicker(type: String = "*/*") {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = type
    startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
}

fun Int.translate(mContext: Context): String = mContext.getString(this)
fun Int.translate(application: Application): String = application.getString(this)

//fun String.requestPermission(activity: Activity) {
//    if (Build.VERSION.SDK_INT >= 33) {
//        if (ActivityCompat.checkSelfPermission(
//                activity,
//                this
//            ) == PackageManager.PERMISSION_DENIED
//        ) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    this
//                )
//            ) {
//                activity.launchSettings()
//            } else {
//                ActivityCompat.requestPermissions(activity, arrayOf(this), 100)
//            }
//        }
//    }
//}

fun String.requestPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT >= 33) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                this
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    this
                )
            ) {
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

fun Activity.createNotificationChannel(channelName: String, description: String? = null, importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel.
        val mChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
        mChannel.description = description
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}

fun Serializable.bundle(name: String = "object"): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(name, this)
    return bundle
}

fun isAppInForeground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val packageName = context.packageName

    val runningAppProcesses = activityManager.runningAppProcesses ?: return false

    for (processInfo in runningAppProcesses) {
        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            && processInfo.processName == packageName) {
            return true
        }
    }
    return false
}

