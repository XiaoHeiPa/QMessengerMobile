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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import org.qbychat.android.service.MessagingService
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.JSON
import org.qbychat.android.utils.POST_NOTIFICATIONS
import org.qbychat.android.utils.account
import org.qbychat.android.utils.bundle
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
    lateinit var authorize: Authorize
    companion object {
        var messagingService: MessagingService? = null
        var isServiceBound = false



        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MessagingService.MessagingBinder
                messagingService = binder.getService()
                isServiceBound = true
                Log.d(TAG, "Service connected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isServiceBound = false
                Log.d(TAG, "Service disconnected")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceBound && ::authorize.isInitialized) {
            Log.v(TAG, "Service not running... Starting service...")
            baseContext.bindService(
                Intent(
                    baseContext,
                    MessagingService::class.java
                ).apply { putExtra("token", authorize.token) },
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(R.string.notification_channel_messages.translate(application))
        POST_NOTIFICATIONS.requestPermission(this)


        setContent {
            QMessengerMobileTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val channels = remember {
                    mutableStateListOf<Channel>()
                }
                // check login
                val accountJson = filesDir.resolve("account.json")
                if (!accountJson.exists()) {
                    doLogin()
                    return@QMessengerMobileTheme
                }
                authorize = JSON.decodeFromString<Authorize>(accountJson.readText())
                // check token expire date
                val accountInfoJson = cacheDir.resolve("account-info.json")
                var account by remember {
                    mutableStateOf(
                        Account(
                            -1,
                            "null",
                            "null@lunarclient.top",
                            registerTime = 0,
                            nickname = R.string.unknown_user.translate(application)
                        )
                    )
                }
                if (accountInfoJson.exists()) {
                    account = JSON.decodeFromString(accountInfoJson.readText())
                }
                if (Date().time >= authorize.expire) {
                    Toast.makeText(
                        baseContext,
                        stringResource(R.string.refresh_token),
                        Toast.LENGTH_LONG
                    ).show()
                    Thread {
                        val authorize1 = login(authorize.username, authorize.password!!)
                        if (authorize1 == null) {
                            // password changed
                            doLogin()
                            return@Thread
                        }
                        authorize1.password = authorize.password
                        authorize = authorize1
                        // save
                        saveAuthorize(baseContext, authorize1)
                    }.apply {
                        start()
                        join()
                    }
                }

                Thread {
                    account = authorize.token.account()!!
                    if (!isServiceBound) bindService(
                        Intent(
                            baseContext,
                            MessagingService::class.java
                        ).apply { putExtra("token", authorize.token) },
                        connection,
                        Context.BIND_AUTO_CREATE
                    )


                    authorize.token.getGroups()?.forEach { group ->
                        channels.add(
                            Channel(
                                group.id,
                                group.shownName,
                                group.name,
                                false
                            )
                        )
                    }

                    authorize.token.getFriends()?.forEach { friend ->
                        channels.add(
                            Channel(
                                friend.id,
                                friend.nickname,
                                friend.username,
                                true
                            )
                        )
                    }

                }.apply {
                    start()
                }

                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Row(modifier = Modifier
                                .padding(5.dp)
                                .clickable {
                                    startActivity(Intent(baseContext, ProfileActivity::class.java)
                                        .apply {
                                            putExtra("authorize", authorize.bundle())
                                            putExtra("info", account.bundle())
                                        })
                                }) {
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
                                    Text(
                                        text = account.email,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            HorizontalDivider()
                            if (account.role == Role.ADMIN) {
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.admin_panel_settings),
                                            contentDescription = "Admin Only"
                                        )
                                    },
                                    label = { Text(text = stringResource(R.string.admin)) },
                                    selected = false,
                                    onClick = {
                                        startActivity(Intent(baseContext, AdminActivity::class.java).apply {
                                            putExtra("token", authorize.token)
                                            putExtra("account", account.bundle())
                                        })
                                    }
                                )
                            }
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text(text = stringResource(R.string.settings)) },
                                selected = false,
                                onClick = {
                                    // TODO
                                }
                            )
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                        contentDescription = "Logout"
                                    )
                                },
                                label = { Text(text = stringResource(R.string.logout)) },
                                selected = false,
                                onClick = {
                                    accountJson.delete()
                                    accountInfoJson.delete()
                                    unbindService(connection)
                                    doLogin()
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
                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxWidth()
                            ) {
                                items(
                                    items = channels,
                                    key = { channel ->
                                        channel.name
                                    }
                                ) { channel ->
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth()
                                            .clickable {
                                                val p0 = Intent(baseContext, ChatActivity::class.java)
                                                p0.putExtra("channel", channel.bundle())
                                                p0.putExtra("account", account.bundle())
                                                p0.putExtra("authorize", authorize.bundle())
                                                startActivity(p0)
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
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Column(
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
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
                                            if (!channel.directMessage) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.groups),
                                                    contentDescription = "icon of group",
                                                    modifier = Modifier.align(Alignment.TopEnd)
                                                )
                                            }
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

    private fun doLogin() {
        startActivity(Intent(baseContext, LoginActivity::class.java))
        finish() // kill current activity
    }


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