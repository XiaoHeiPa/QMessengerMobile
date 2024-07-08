package org.qbychat.android.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import kotlinx.serialization.json.JsonObject
import okhttp3.WebSocket
import org.qbychat.android.Message
import org.qbychat.android.MessengerResponse
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.bundle
import org.qbychat.android.utils.connect

const val RECEIVED_MESSAGE = "org.qbychat.android.RECEIVED_MESSAGE"

class MessagingService : JobService() {
    private lateinit var token: String
    companion object {
        var websocket: WebSocket? = null
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        websocket?.close(200, null)
        websocket = token.connect { _, responseJson ->
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
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }
}
