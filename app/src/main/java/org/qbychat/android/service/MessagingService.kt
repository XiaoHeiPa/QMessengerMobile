package org.qbychat.android.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import okhttp3.WebSocket
import org.qbychat.android.utils.connect

class MessagingService : Service() {
    private val mBinder = MessagingBinder()

    inner class MessagingBinder : Binder() {
        fun getService(): MessagingService = this@MessagingService
    }
    var mAllowRebind: Boolean = false
    var mStartMode: Int = 0

    private lateinit var token: String
    var websocket: WebSocket? = null

    override fun onBind(p0: Intent): IBinder {
        token = p0.getStringExtra("token")!!
        return mBinder
    }

    fun connect() {
        websocket?.close(200, null)
        websocket = token.connect()
    }
}