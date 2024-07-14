package org.qbychat.android

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.qbychat.android.utils.JSON


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
data class Invitation(
    val code: String,
    val adminId: Int,
    val expire: Long,
)

@Serializable
data class Account(
    val id: Int,
    val username: String,
    val email: String,
    val bio: String = "-",
    val role: Role = Role.USER,
    val registerTime: Long,
    val nickname: String,
): java.io.Serializable {
    companion object {
        val UNKNOWN = Account(-1, "user_unknown", "unknown@lunarclient.top", registerTime = -1, nickname = "Unknown")
    }
}

@Serializable
data class Authorize(
    var username: String,
    var role: Role,
    var token: String,
    var email: String,
    var expire: Long,

    var password: String? = null // for renew token
): java.io.Serializable

@Serializable
data class Friend(
    val id: Int,
    val username: String,
    val nickname: String
)

@Serializable
data class MessengerRequest<T>(
    val method: String,
    val data: T?
) {
    fun json(dataSerial: KSerializer<T>): String =
        JSON.encodeToString(serializer(dataSerial), this)

    @Serializable
    data class FetchLatestMessages(
        val channel: Int,
        val directMessage: Boolean
    )
}

@Serializable
data class MessengerResponse<T>(
    val method: String,
    val data: T? = null,
    val hasError: Boolean = false
) {
    companion object {
        const val HAS_FRIEND = "has-friend"
        const val FRIEND_REQUEST = "friend-request"
        const val FRIEND_REQUEST_SENT = "friend-request-sent"
        const val CHAT_MESSAGE = "chat-message"
        const val USER_INFO = "user-info"
    }
}

@Serializable
data class Message(
    val id: Int? = null, // auto generated
    val sender: Int? = null, // auto generated
    val to: Int,
    val type: MessageType = MessageType.TEXT,
    val directMessage: Boolean,
    val timestamp: Long = 0, // auto generated
    val content: MessageContent,
    val senderInfo: Account = Account.UNKNOWN
): java.io.Serializable {


    enum class MessageType {
        TEXT,
        IMAGE
    }

    @Serializable
    data class MessageContent(
        val text: String,
        val description: String? = "Message content",
        val replyTo: Int? = null
    ): java.io.Serializable
}

@Serializable
data class Group(
    val id: Int,
    val owner: Int,
    val name: String,
    val shownName: String,
    val description: String = "Default description",
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
        const val FETCH_LATEST_MESSAGES: String = "fetch-latest-messages"
    }
}
