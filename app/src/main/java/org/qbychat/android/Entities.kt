package org.qbychat.android

import kotlinx.serialization.Serializable


@Serializable
data class Channel(
    val id: Int,
    val shownName: String,
    val name: String,
    val directMessage: Boolean,
    val preview: String
) : java.io.Serializable

@Serializable
enum class Role {
    USER,
    ADMIN
}

@Serializable
data class Account(
    val id: Int,
    val username: String,
    val email: String,
    val role: Role,
    val registerTime: String,
    val nickname: String,
)

@Serializable
data class Authorize(
    var username: String,
    var role: String,
    var token: String,
    var email: String,
    var expire: Int,

    var password: String? = null // for renew token
)

@Serializable
data class RestBean<T>(val code: Int, val data: T, val message: String?)
