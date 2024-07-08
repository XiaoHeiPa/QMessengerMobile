package org.qbychat.android.service

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class FCMNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        Log.v(TAG, token)
//        sendRegistrationToServer(token)
    }
}