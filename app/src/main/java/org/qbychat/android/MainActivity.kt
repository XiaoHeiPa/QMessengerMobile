package org.qbychat.android

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.vibrator

@SuppressLint(
    "UnusedMaterial3ScaffoldPaddingParameter"
)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QMessengerMobileTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "qby")
                                Text(
                                    text = "rentler@lunarclient.top",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Home,
                                        contentDescription = "Homepage"
                                    )
                                },
                                label = { Text(text = "主页") },
                                selected = true,
                                onClick = {}
                            )
                            HorizontalDivider()
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "About"
                                    )
                                },
                                label = { Text(text = "关于") },
                                selected = false,
                                onClick = { /*TODO*/ }
                            )
                        }
                    },
                    gesturesEnabled = true
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                            AddContactButton(vibrator = this.vibrator)
                        },
                        content = { innerPadding ->
                            val channels = arrayListOf<Channel>()
                            channels.add(
                                Channel(
                                    0,
                                    "Serren * Banka",
                                    "example",
                                    false,
                                    "Yoshino: Ciallo～(∠・ω< )"
                                )
                            )
                            repeat(30) { exampleCount ->
                                channels.add(
                                    Channel(
                                        exampleCount + 1,
                                        "Example Group",
                                        "example",
                                        false,
                                        "nkwjg: hi!"
                                    )
                                )
                            }
                            val scrollState = rememberScrollState()

                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .horizontalScroll(scrollState)
                            ) {
                                items(channels) { channel ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                            .clickable { }
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = "avatar",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)                       // clip to the circle shape
                                                .border(2.dp, Color.Gray, CircleShape)
                                        )
                                        Column {
                                            Text(
                                                text = channel.shownName,
                                                style = MaterialTheme.typography.headlineSmall,
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
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AddContactButton(modifier: Modifier = Modifier, vibrator: Vibrator) {
    FloatingActionButton(
        onClick = {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
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