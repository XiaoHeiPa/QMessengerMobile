package org.qbychat.android.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.serialization.json.JsonObject
import okhttp3.WebSocket
import org.qbychat.android.MainActivity
import org.qbychat.android.Message
import org.qbychat.android.MessengerResponse
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.bundle
import org.qbychat.android.utils.connect
import org.qbychat.android.utils.isAppInForeground
import org.qbychat.android.utils.updateFCMToken


const val RECEIVED_MESSAGE = "org.qbychat.android.RECEIVED_MESSAGE"
private lateinit var localToken: String

class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isAppInForeground(context) && intent.action == Intent.ACTION_USER_PRESENT && !MainActivity.isServiceBound && ::localToken.isInitialized) {
            Log.v(TAG, "Starting service...")
            context.bindService(
                Intent(
                    context,
                    MessagingService::class.java
                ).apply { putExtra("token", localToken) },
                MainActivity.connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }
}


class MessagingService : Service() {
    private val mBinder = MessagingBinder()

    inner class MessagingBinder : Binder() {
        fun getService(): MessagingService = this@MessagingService
    }

    private lateinit var token: String

    companion object {
        var websocket: WebSocket? = null
    }

    override fun onBind(p0: Intent): IBinder {
        token = p0.getStringExtra("token")!!
        localToken = token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val fcmToken = task.result

            // Log
            Log.d(TAG, "FCM token: $fcmToken")
            token.updateFCMToken(fcmToken)
        })

        websocket?.close(200, null)
        connectWS()
        return mBinder
    }

    private fun connectWS() {
        websocket = token.connect({
            Log.i(TAG, "WS disconnected.")
            stopSelf()
        }) { _, responseJson ->
            val response = JSON.decodeFromString<MessengerResponse<JsonObject>>(responseJson)
            if (response.hasError) {
                return@connect // do nothing
            }
            if (response.method == MessengerResponse.CHAT_MESSAGE) {
                val message = JSON.decodeFromJsonElement(Message.serializer(), response.data!!)
                sendBroadcast(Intent(RECEIVED_MESSAGE).apply {
                    putExtra("message", message.bundle())
                })
            }
        }
    }


    override fun onDestroy() {
        websocket?.close(200, null)
        websocket = null
        super.onDestroy()
    }
}
