package org.qbychat.android

import kotlinx.serialization.Serializable


@Serializable
data class Channel(
    val id: Int,
    val shownName: String,
    val name: String,
    val directMessage: Boolean,
    val preview: String = ""
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
    val role: Role = Role.USER,
    val registerTime: Long,
    val nickname: String,
)

@Serializable
data class Authorize(
    var username: String,
    var role: String,
    var token: String,
    var email: String,
    var expire: Long,

    var password: String? = null // for renew token
)

@Serializable
data class Friend(
    val id: Int,
    val username: String,
    val nickname: String
)

@Serializable
data class Request<T>(
    val method: String,
    val data: T
)

@Serializable
data class Message(
    val id: Int? = null, // auto generated
    val sender: Int? = null, // auto generated
    val to: Int,
    val type: MessageType = MessageType.TEXT,
    val directMessage: Boolean,
    val timestamp: Long = 0, // auto generated
    val content: MessageContent
) {
    enum class MessageType {
        TEXT,
        IMAGE
    }

    @Serializable
    data class MessageContent(
        val text: String,
        val description: String = "Message content",
        val replyTo: Int? = null
    )
}

@Serializable
data class Group(
    val id: Int,
    val owner: Int,
    val name: String,
    val shownName: String,
    val description: String?,
    val createTime: Long,
    val members: Set<Int>,
)

@Serializable
data class RestBean<T>(val code: Int, val data: T, val message: String?)


interface RequestType {
    companion object {
        // Messenger
        const val SEND_MESSAGE: String = "send-message"
        const val ADD_FRIEND: String = "add-friend"
        const val ACCEPT_FRIEND_REQUEST: String = "accept-friend-request"
    }
}
