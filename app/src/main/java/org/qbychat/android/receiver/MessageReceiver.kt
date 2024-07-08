package org.qbychat.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.qbychat.android.Message
import org.qbychat.android.MessengerResponse
import org.qbychat.android.utils.account

class MessageReceiver : BroadcastReceiver() {
    override fun onReceive(mContext: Context, intent: Intent) {
        if (intent.action == MessengerResponse.CHAT_MESSAGE) {
            @Suppress("DEPRECATION")
            val message = intent.getBundleExtra("message")!!.getSerializable("object") as Message
            val account = message.sender!!.account!!
            Toast.makeText(mContext, "${account.nickname}: ${message.content.text}", Toast.LENGTH_LONG).show()
        }
    }
}