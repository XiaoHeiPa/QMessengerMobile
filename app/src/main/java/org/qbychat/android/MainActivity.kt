@file:OptIn(ExperimentalMaterial3Api::class)

package org.qbychat.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.qbychat.android.service.MessagingService
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.POST_NOTIFICATIONS
import org.qbychat.android.utils.createNotificationChannel
import org.qbychat.android.utils.getGroups
import org.qbychat.android.utils.login
import org.qbychat.android.utils.requestPermission
import org.qbychat.android.utils.saveAuthorize
import org.qbychat.android.utils.translate
import java.util.Date


const val CHANNEL_ID = "qmessenger"

@SuppressLint(
    "UnusedMaterial3ScaffoldPaddingParameter"
)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startService()
        createNotificationChannel(R.string.notification_channel_messages.translate(application))
        POST_NOTIFICATIONS.requestPermission(this)
        setContent {
            QMessengerMobileTheme {
                val mContext = LocalContext.current
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val filesDir = mContext.filesDir

                val channels = remember {
                    mutableStateListOf<Channel>()
                }
                // check login
                val accountJson = filesDir.resolve("account.json")
                if (!accountJson.exists()) {
                    doLogin(mContext = mContext)
                    return@QMessengerMobileTheme
                }
                var authorize = JSON.decodeFromString<Authorize>(accountJson.readText())
                // check token expire date
                if (Date().time >= authorize.expire) {
                    Toast.makeText(
                        mContext,
                        R.string.reflesh_token.translate(application),
                        Toast.LENGTH_LONG
                    ).show()
                    val runnable = Runnable {
                        val authorize1 = login(authorize.username, authorize.password!!)
                        if (authorize1 == null) {
                            // password changed
                            doLogin(mContext = mContext)
                            return@Runnable
                        }
                        authorize1.password = authorize.password
                        authorize = authorize1
                        // save
                        saveAuthorize(mContext, authorize1)
                    }
                    val thread = Thread(runnable)
                    thread.start()
                    thread.join()
                }
                Thread {
                    authorize.token.getGroups()!!.forEach { group ->
                        channels.add(Channel(group.id, group.shownName, group.name, false, ""))
                    }
                }.start()
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = authorize.username)
                                Text(
                                    text = authorize.email,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "About"
                                    )
                                },
                                label = { Text(text = R.string.about.translate(mContext)) },
                                selected = false,
                                onClick = { /*TODO*/ }
                            )
                        }
                    },
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxWidth(),
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                ),
                                title = {
                                    Text(
                                        "QMessenger"
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Menu"
                                        )
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            AddContactButton()
                        },
                        content = { innerPadding ->
                            val scrollState = rememberScrollState()

                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                            ) {
                                items(channels) { channel ->
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .clickable {
                                                val p0 = Intent(mContext, ChatActivity::class.java)
                                                p0.putExtra("channel", channel.bundle())
                                                mContext.startActivity(p0)
                                            }
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = "avatar",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.Gray, CircleShape)
                                        )
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = channel.shownName,
                                            )
                                            Text(
                                                text = channel.preview,
                                                color = MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun doLogin(mContext: Context) {
        startActivity(Intent(mContext, LoginActivity::class.java))
        finish() // kill current activity
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    private fun startService() {
        startService(Intent(baseContext, MessagingService::class.java))
    }

    private fun stopService() {
        stopService(Intent(baseContext, MessagingService::class.java))
    }
}

private fun Channel.bundle(name: String = "object"): Bundle {
    val bundle = Bundle();
    bundle.putSerializable(name, this)
    return bundle
}

@Composable
fun AddContactButton(modifier: Modifier = Modifier) {
    FloatingActionButton(
        shape = CircleShape,
        onClick = {
        }
    ) {
        Box {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QMessengerMobileTheme {
        FloatingActionButton(
            onClick = { },
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}