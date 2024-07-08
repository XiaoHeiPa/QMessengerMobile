@file:OptIn(ExperimentalMaterial3Api::class)

package org.qbychat.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.qbychat.android.RequestType.Companion.SEND_MESSAGE
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.account

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val channel =
            intent.getBundleExtra("channel")!!.getSerializable("object") as Channel
        @Suppress("DEPRECATION")
        val currentUser =
            intent.getBundleExtra("account")!!.getSerializable("object") as Account

        setContent {
            QMessengerMobileTheme {
                val mContext = LocalContext.current
                val messages = remember {
                    mutableStateListOf<Message>()
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            ),
                            title = {
                                Text(text = channel.shownName)
                            },
                            navigationIcon = {
                                IconButton(onClick = { this@ChatActivity.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        ChatBox({ messageContent ->
                            if (!MainActivity.isServiceBound) {
                                Toast.makeText(
                                    mContext,
                                    R.string.service_disabled,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@ChatBox
                            }
                            val websocket = MainActivity.messagingService!!.websocket
                            val request = MessengerRequest(
                                SEND_MESSAGE,
                                Message(
                                    to = channel.id,
                                    directMessage = channel.directMessage,
                                    content = Message.MessageContent(text = messageContent)
                                )
                            )
                            websocket?.send(
                                JSON.encodeToString(
                                    MessengerRequest.serializer(Message.serializer()),
                                    request
                                )
                            )
                        })
                    }

                ) { innerPadding ->
                    repeat(30) { i ->
                        messages.add(
                            Message(
                            id = i,
                            sender = if (i % 2 == 0) currentUser.id else -1,
                            to = 3,
                            directMessage = true,
                            content = Message.MessageContent(
                                text = "MSG x$i"
                            )
                        ))
                    }
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(messages) { message ->
                            ChatMessage(
                                message = message, isFromMe = currentUser.id == message.sender
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessage(message: Message, isFromMe: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
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
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            if (!message.directMessage && !isFromMe) {
                val account = message.sender!!.account!!
                AsyncImage(
                    model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${account.id}&isUser=1",
                    contentDescription = "${account.username}'s avatar",
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(5.dp)
                        .size(25.dp)
                        .clickable { /*TODO*/ }
                )
            }
            Text(text = message.content.text, color = MaterialTheme.colorScheme.onPrimary)
        }

    }
}

@Composable
fun ChatBox(onSendMessageClicked: (String) -> Unit, modifier: Modifier = Modifier) {
    var chatBoxValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    Row(modifier = modifier.padding(16.dp)) {
        TextField(
            value = chatBoxValue,
            onValueChange = { newValue ->
                chatBoxValue = newValue // update text
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
                onSendMessageClicked(chatBoxValue.text)
                chatBoxValue = TextFieldValue("") // clear text field
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onPrimary)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
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