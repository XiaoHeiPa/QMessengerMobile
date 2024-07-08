@file:OptIn(ExperimentalMaterial3Api::class)

package org.qbychat.android

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.qbychat.android.service.MessagingService
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.POST_NOTIFICATIONS
import org.qbychat.android.utils.account
import org.qbychat.android.utils.createNotificationChannel
import org.qbychat.android.utils.getFriends
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
    companion object {
        var messagingService: MessagingService? = null
        var isServiceBound = false
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MessagingService.MessagingBinder
            messagingService = binder.getService()
            isServiceBound = true
            Log.d(TAG, "Service connected")

            messagingService!!.connectWS()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(R.string.notification_channel_messages.translate(application))
        POST_NOTIFICATIONS.requestPermission(this)
        setContent {
            QMessengerMobileTheme {
                val mContext = LocalContext.current
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

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
                val accountInfoJson = cacheDir.resolve("account-info.json")
                val channelsCache = cacheDir.resolve("groups.json")
                lateinit var account: Account
                if (accountInfoJson.exists()) {
                    account = JSON.decodeFromString(accountInfoJson.readText())
                }
                if (channelsCache.exists()) {
                    channels.addAll(JSON.decodeFromString(channelsCache.readText()))
                }
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
                    val channels1 = mutableListOf<Channel>()
                    authorize.token.getGroups()?.forEach { group ->
                        channels1.add(Channel(group.id, group.shownName, group.name, false))
                    }

                    authorize.token.getFriends()?.forEach { friend ->
                        channels1.add(Channel(friend.id, friend.nickname, friend.username, true))
                    }
                    channels.clear()
                    channels.addAll(channels1)
                    val json = JSON.encodeToString(channels1)
                    if (json != channelsCache.let { if (it.exists()) it.readText() else "" }) channelsCache.writeText(json)
                }.start()
                Thread {
                    account = authorize.token.account()!!
                }.apply {
                    this.start()
                    if (!accountInfoJson.exists()) {
                        this.join()
                        accountInfoJson.writeText(
                            JSON.encodeToString(
                                Account.serializer(),
                                account
                            )
                        )
                    }
                }
                bindService(Intent(mContext, MessagingService::class.java).apply { putExtra("token", authorize.token) }, connection, Context.BIND_AUTO_CREATE)
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Row(modifier = Modifier.padding(5.dp)) {
                                SubcomposeAsyncImage(
                                    model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${account.id}&isUser=1",
                                    loading = {
                                        CircularProgressIndicator()
                                    },
                                    contentDescription = "avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = account.nickname)
//                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = account.email,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                        contentDescription = "Logout"
                                    )
                                },
                                label = { Text(text = R.string.logout.translate(mContext)) },
                                selected = false,
                                onClick = {
                                    accountJson.delete()
                                    accountInfoJson.delete()
                                    channelsCache.delete()
                                    doLogin(mContext)
                                }
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
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = channel.shownName
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
}

private fun Channel.bundle(name: String = "object"): Bundle {
    val bundle = Bundle()
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