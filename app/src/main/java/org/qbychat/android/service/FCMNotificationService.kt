package org.qbychat.android.service

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.qbychat.android.Authorize
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.refresh
import org.qbychat.android.utils.updateFCMToken
import java.util.Date

class FCMNotificationService : FirebaseMessagingService() {
    private lateinit var authorize: Authorize
    override fun onCreate() {
        super.onCreate()
        val accountJson = filesDir.resolve("account.json")
        if (accountJson.exists()) {
            authorize = JSON.decodeFromString<Authorize>(accountJson.readText())
        }
        if (Date().time >= authorize.expire) {
            Thread {
                authorize.refresh(baseContext)
            }.apply {
                start()
            }
        }
    }

    override fun onNewToken(fcmToken: String) {
        Log.d(TAG, "Refreshed token: $fcmToken")

        Log.v(TAG, fcmToken)
        authorize.token.updateFCMToken(fcmToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.body?.let { Log.v(TAG, it) }
    }
}