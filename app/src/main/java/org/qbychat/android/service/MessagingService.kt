package org.qbychat.android.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MessagingService : Service() {
    private val mBinder: IBinder? = null
    var mAllowRebind: Boolean = false
    var mStartMode: Int = 0

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }
}