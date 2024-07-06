@file:OptIn(ExperimentalMaterial3Api::class)

package org.qbychat.android

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import org.qbychat.android.ui.theme.QMessengerMobileTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val channel =
            intent.getBundleExtra("channel")!!.getSerializable("object") as Channel

        setContent {
            QMessengerMobileTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                        ChatBox()
                    }

                ) { innerPadding ->

                }
            }
        }
    }
}

@Composable
fun ChatBox(modifier: Modifier = Modifier) {
    var chatBoxValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    Row {
        TextField(
            value = chatBoxValue,
            onValueChange = { newValue ->
                chatBoxValue = newValue // update text
            },
            placeholder = {
                Text(text = "QwQ")
            }
        )
        IconButton(onClick = { /*TODO: SendMessage*/ }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    QMessengerMobileTheme {
    }
}