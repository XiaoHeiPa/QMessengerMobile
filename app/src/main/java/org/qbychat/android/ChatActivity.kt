@file:OptIn(ExperimentalMaterial3Api::class)

package org.qbychat.android

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import org.qbychat.android.RequestType.Companion.SEND_MESSAGE
import org.qbychat.android.service.MessagingService
import org.qbychat.android.service.RECEIVED_MESSAGE
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.bundle


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class ChatActivity : ComponentActivity() {
    class MessageReceiver : BroadcastReceiver() {
        private lateinit var messages: SnapshotStateList<Message>

        override fun onReceive(mContext: Context, intent: Intent) {
            @Suppress("DEPRECATION")
            val message =
                intent.getBundleExtra("message")!!.getSerializable("object") as Message
            if (!messages.any { it.id == message.id }) messages.add(message)
            if (messages.size > 100) messages.removeRange(0, 50)
            messages.sortBy { message1 -> message1.id }
        }

        fun setList(messages: SnapshotStateList<Message>) {
            this.messages = messages
        }
    }

    private val messageReceiver = MessageReceiver()
    private lateinit var authorize: Authorize
    private lateinit var channel: Channel

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        channel =
            intent.getBundleExtra("channel")!!.getSerializable("object") as Channel

        val currentUser =
            intent.getBundleExtra("account")!!.getSerializable("object") as Account
        authorize = intent.getBundleExtra("authorize")!!.getSerializable("object")!! as Authorize

        val intentFilter = IntentFilter()
        intentFilter.addAction(RECEIVED_MESSAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(messageReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(messageReceiver, intentFilter)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val clipboard =
            baseContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        setContent {
            QMessengerMobileTheme {
                val messages = remember {
                    mutableStateListOf<Message>()
                }
                messageReceiver.setList(messages)

                Thread {
                    channel.updateMessage()
                }.start()
                val selectedMessages = remember {
                    mutableStateListOf<Int>()
                }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            ),
                            title = {
                                Row {
                                    SubcomposeAsyncImage(
                                        model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${channel.id}&isUser=${if (channel.directMessage) 1 else 0}",
                                        loading = {
                                            CircularProgressIndicator()
                                        },
                                        contentDescription = "avatar",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                    )

                                    Text(
                                        text = channel.title,
                                        modifier = Modifier.clickable {
                                            if (channel.directMessage) {
                                                startActivity(
                                                    Intent(
                                                        baseContext,
                                                        ProfileActivity::class.java
                                                    ).apply {
                                                        putExtra(
                                                            "id",
                                                            channel.id
                                                        )
                                                        putExtra("authorize", authorize.bundle())
                                                    }
                                                )
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        baseContext,
                                                        GroupDetailsActivity::class.java
                                                    ).apply {
                                                        putExtra(
                                                            "id",
                                                            channel.id
                                                        )
                                                        putExtra("authorize", authorize.bundle())
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { this@ChatActivity.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                if (selectedMessages.size == 1) {
                                    IconButton(
                                        onClick = {
                                            selectedMessages.forEach { msgId ->
                                                val msg = messages.find { it.id == msgId }?.let {
                                                    if (it.type == Message.MessageType.TEXT) {
                                                        clipboard.setPrimaryClip(
                                                            ClipData.newPlainText(
                                                                "MESSAGE",
                                                                it.content.text
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            selectedMessages.clear()
                                            Toast.makeText(baseContext, R.string.copy_successfully, Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(painter = painterResource(id = R.drawable.file_copy), contentDescription = "Copy")
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        ChatBox(onMenuClicked = {

                        }, onSendMessageClicked = { messageContent ->
                            val websocket = MessagingService.websocket!!
                            val request = MessengerRequest(
                                SEND_MESSAGE,
                                Message(
                                    to = channel.id,
                                    directMessage = channel.directMessage,
                                    content = Message.MessageContent(text = messageContent)
                                )
                            )
                            websocket.send(
                                JSON.encodeToString(
                                    MessengerRequest.serializer(Message.serializer()),
                                    request
                                )
                            )
                        })
                    }

                ) { innerPadding ->
                    var lastMessage: Message? = null
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        contentPadding = innerPadding,
                        modifier = Modifier
                            .consumeWindowInsets(innerPadding)
                    ) {
                        items(
                            items = messages,
                            key = { message1 ->
                                message1.id!!
                            }
                        ) { message ->
                            ChatMessage(
                                message = message,
                                isFromMe = currentUser.id == message.sender,
                                selectedMessages = selectedMessages,
                                lastMessage = lastMessage
                            )
                            lastMessage = message
                        }
                    }

                    LaunchedEffect(messages.size) {
                        if (messages.size == 0) return@LaunchedEffect
                        listState.scrollToItem(messages.size - 1)
                    }
                }
            }
        }
    }

    private fun Channel.updateMessage() {
        MessagingService.websocket?.send(
            MessengerRequest(
                RequestType.FETCH_LATEST_MESSAGES,
                MessengerRequest.FetchLatestMessages(this.id, this.directMessage)
            ).json(MessengerRequest.FetchLatestMessages.serializer())
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !MainActivity.isServiceBound) {
            bindService(
                Intent(
                    baseContext,
                    MessagingService::class.java
                ).apply { putExtra("token", authorize.token) },
                MainActivity.connection,
                Context.BIND_AUTO_CREATE
            )
            Thread {
                channel.updateMessage()
            }.start()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(messageReceiver)
        super.onDestroy()
    }

    @Composable
    fun ChatMessage(
        message: Message,
        isFromMe: Boolean,
        selectedMessages: SnapshotStateList<Int>,
        lastMessage: Message? = null
    ) {
        var isSelected = selectedMessages.contains(message.id)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row {
                if (!message.directMessage && !isFromMe && lastMessage?.sender != message.sender) {
                    AsyncImage(
                        model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${message.sender}&isUser=1",
                        contentDescription = "avatar",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(30.dp)
                            .clickable {
                                startActivity(
                                    Intent(
                                        baseContext,
                                        ProfileActivity::class.java
                                    ).apply {
                                        putExtra("authorize", authorize.bundle())
                                        putExtra("info", message.senderInfo.bundle())
                                    })
                            }
                    )
                    Text(text = message.senderInfo.nickname)
                }
            }
            Box(
                modifier = Modifier
                    .align(if (isFromMe) Alignment.End else Alignment.Start)
                    .clip(
                        RoundedCornerShape(
                            topStart = 48f,
                            topEnd = 48f,
                            bottomStart = if (isFromMe) 48f else 0f,
                            bottomEnd = if (isFromMe) 0f else 48f
                        )
                    )
                    .background(if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                isSelected = true
                                selectedMessages.add(message.id!!)
                            },
                            onPress = {
                                if (selectedMessages.size != 0 && !selectedMessages.contains(
                                        message.id!!
                                    )
                                ) {
                                    isSelected = true
                                    selectedMessages.add(message.id)
                                } else if (isSelected) {
                                    isSelected = false
                                    selectedMessages.remove(message.id)
                                }
                            }
                        )
                    }
            ) {
                Text(
                    text = message.content.text,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


@Composable
fun ChatBox(
    modifier: Modifier = Modifier,
    onMenuClicked: () -> Unit,
    onSendMessageClicked: (String) -> Unit
) {
    var chatBoxValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var btnMethodIsSend by remember {
        mutableStateOf(false)
    }
    Row(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = chatBoxValue,
            onValueChange = { newValue ->
                chatBoxValue = newValue // update text
                btnMethodIsSend = if (newValue.text.isEmpty()) false else true
            },
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(text = "QwQ")
            }
        )
        IconButton(
            onClick = {
                val msg = chatBoxValue.text
                if (msg.isBlank()) return@IconButton
                if (btnMethodIsSend) {
                    onSendMessageClicked(chatBoxValue.text)
                } else {
                    onMenuClicked()
                }
                chatBoxValue = TextFieldValue("") // clear text field
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onPrimary)
                .align(Alignment.CenterVertically)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onMenuClicked()
                        }
                    )
                }
        ) {
            Icon(
                imageVector = if (btnMethodIsSend) Icons.AutoMirrored.Filled.Send else Icons.Filled.Add,
                contentDescription = "Function btn",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Preview2() {
    QMessengerMobileTheme {
    }
}