package org.qbychat.android

import java.io.Serializable

data class Channel(
    val id: Int,
    val shownName: String,
    val name: String,
    val directMessage: Boolean,
    val preview: String
): Serializable