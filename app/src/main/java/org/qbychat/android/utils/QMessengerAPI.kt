package org.qbychat.android.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val httpClient = OkHttpClient.Builder()
    .build()
const val HTTP_PROTOCOL = "https://"
const val WS_PROTOCOL = "wss://"
const val BACKEND = "backend.lunarclient.top"

fun login(username: String, password: String): String? {
    val body = "username=$username&password=$password".toRequestBody("application/x-www-form-urlencoded".toMediaType())
    val request = Request.Builder()
        .url("$HTTP_PROTOCOL$BACKEND/user/login")
        .post(body)
        .build()
    with(httpClient.newCall(request).execute()) {
        if (this.body == null) {
            return null
        }
        return this.body!!.string() // todo json
    }
}