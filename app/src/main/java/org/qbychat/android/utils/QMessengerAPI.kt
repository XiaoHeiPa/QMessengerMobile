package org.qbychat.android.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.qbychat.android.Account
import org.qbychat.android.Authorize
import org.qbychat.android.Friend
import org.qbychat.android.Group
import org.qbychat.android.Invitation
import org.qbychat.android.RestBean
import java.io.IOException


private val httpClient = OkHttpClient.Builder()
    .build()
const val HTTP_PROTOCOL = "https://"
const val WS_PROTOCOL = "wss://"
const val BACKEND = "backend.lunarclient.top"

val X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded".toMediaType()

fun login(username: String, password: String): Authorize? {
    val body =
        "username=$username&password=$password".toRequestBody("application/x-www-form-urlencoded".toMediaType())
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND/user/login")
        .post(body)
        .build()
    with(httpClient.newCall(request).execute()) {
        if (this.body == null) {
            return null
        }
        val response = JSON.decodeFromString<RestBean<Authorize?>>(this.body!!.string())
        if (response.code != 200) return null
        return response.data
    }
}

private inline fun <reified T> String.getAPI(
    api: String,
    crossinline onSuccess: (call: Call, response: RestBean<T>) -> Unit
) {
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND$api")
        .get()
        .header("Authorization", "Bearer $this")
        .build()
    httpClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, e.stackTraceToString())
        }

        override fun onResponse(call: Call, response: Response) {
            val response1 = JSON.decodeFromString<RestBean<T>>(response.body!!.string())
            onSuccess(call, response1)
        }

    })
}

private inline fun <reified T> String.getAPI(api: String): T? {
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND$api")
        .get()
        .header("Authorization", "Bearer $this")
        .build()
    with(httpClient.newCall(request).execute()) {
        if (this.body == null) return null // unreachable
        val response = JSON.decodeFromString<RestBean<T>>(this.body!!.string())
        return response.data
    }
}

private inline fun <reified T> String.postAPI(
    api: String, body: RequestBody,
    crossinline onSuccess: (call: Call, response: RestBean<T>) -> Unit
) {
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND$api")
        .post(body)
        .apply {
            if (this@postAPI.isNotEmpty())
                this.header("Authorization", "Bearer $this")
        }
        .build()
    httpClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, e.stackTraceToString())
        }

        override fun onResponse(call: Call, response: Response) {
            val response1 = JSON.decodeFromString<RestBean<T>>(response.body!!.string())
            onSuccess(call, response1)
        }

    })
}

// String: token
fun String.getGroups(onSuccess: (List<Group>) -> Unit) =
    this.getAPI("/user/groups/list") { _, response ->
        onSuccess(response.data!!)
    }

fun String.getFriends(onSuccess: (List<Friend>) -> Unit) {
    this.getAPI("/user/friends/list") { _, response ->
        onSuccess(response.data)
    }
}

fun String.account(onSuccess: (Account) -> Unit) {
    this.getAPI("/user/account") { _, response ->
        onSuccess(response.data)
    }
}

fun String.getGroups(): List<Group>? = this.getAPI("/user/groups/list")

fun String.getFriends(): List<Friend>? = this.getAPI("/user/friends/list")

fun String.account(): Account? = this.getAPI("/user/account")

fun String.changeUsername(newName: String, onSuccess: (Account) -> Unit) {
    this.postAPI("/user/account/username", "value=$newName".toRequestBody(X_WWW_FORM_URLENCODED)) { _, response ->
        onSuccess(response.data)
    }
}

fun String.changePassword(newPassword: String, onSuccess: (Account) -> Unit) {
    this.postAPI("/user/account/password", "value=$newPassword".toRequestBody(X_WWW_FORM_URLENCODED)) { _, response ->
        onSuccess(response.data)
    }
}

fun String.changeBio(newBio: String, onSuccess: (Account) -> Unit) {
    this.postAPI("/user/account/bio", "value=$newBio".toRequestBody(X_WWW_FORM_URLENCODED)) { _, response ->
        onSuccess(response.data)
    }
}

fun String.generateInviteCode(onSuccess: (Invitation) -> Unit) {
    this.getAPI("/admin/register/invite") { _, response ->
        onSuccess(response.data)
    }
}

fun String.updateFCMToken(fcmToken: String) {

    val body = "newToken=$fcmToken".toRequestBody(X_WWW_FORM_URLENCODED)
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND/user/fcm/token")
        .header("Authorization", this)
        .post(body)
        .build()
    httpClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, e.stackTraceToString())
        }

        override fun onResponse(call: Call, response: Response) {

        }
    })
}

fun saveAuthorize(mContext: Context, authorize: Authorize) {
    mContext.filesDir.resolve("account.json").writeText(
        JSON.encodeToString(Authorize.serializer(), authorize)
    )
}

fun Authorize.refresh(baseContext: Context, passwordChanged: () -> Unit = {}) {
    val authorize = login(this.username, this.password!!)
    if (authorize == null) {
        // password changed
        passwordChanged()
        return
    }
    authorize.password = this.password
    // save
    saveAuthorize(baseContext, authorize)
    this.username = authorize.username
    this.role = authorize.role
    this.email = authorize.email
    this.token = authorize.token
    this.expire = authorize.expire
}

// Int: userId
private val accountMap = mutableMapOf<Int, Account>()

fun Int.account(): Account? {
    if (accountMap.containsKey(this)) return accountMap[this]

    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND$/user/query?id=$this")
        .get()
        .build()
    var account: Account?
    with(httpClient.newCall(request).execute()) {
        if (this.body == null) account = null // unreachable
        val response = JSON.decodeFromString<RestBean<Account>>(this.body!!.string())
        account = response.data
        accountMap[this@account] = account!!
    }
    return account
}

fun Int.account(token: String = "", onSuccess: (Account) -> Unit) {
    token.getAPI("/user/query/id/$this") { _, response ->
        onSuccess(response.data)
    }
}

fun Int.group(token: String = "", onSuccess: (Group) -> Unit) {
    token.getAPI("/group/query/id/$this") { _, response ->
        onSuccess(response.data)
    }
}

// WS
fun String.connect(
    onWSClosed: () -> Unit = {},
    onMessageReceived: (websocket: WebSocket, response: String) -> Unit
): WebSocket {
    val request: Request = Request.Builder()
        .url("$WS_PROTOCOL$BACKEND/ws/messenger")
        .header("Authorization", "Bearer $this")
        .build()

    val webSocket: WebSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.i("Websocket", "WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.i("Websocket", "Received message: $text")
            onMessageReceived(webSocket, text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            t.printStackTrace()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            onWSClosed()
        }
    })
    return webSocket
}


