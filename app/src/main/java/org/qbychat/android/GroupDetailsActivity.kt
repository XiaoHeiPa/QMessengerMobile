package org.qbychat.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.account
import org.qbychat.android.utils.bundle
import org.qbychat.android.utils.group
import org.qbychat.android.utils.translate

class GroupDetailsActivity : ComponentActivity() {
    private lateinit var token: String

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        token = intent.getStringExtra("token")!!
        val targetId = intent.getIntExtra("id", -1)
        setContent {
            QMessengerMobileTheme {
                var name by remember {
                    mutableStateOf("group_unknown")
                }
                var title by remember {
                    mutableStateOf(R.string.unknown_group.translate(application))
                }
                var description by remember {
                    mutableStateOf("Unknown Description")
                }
                var owner by remember {
                    mutableIntStateOf(-1)
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ), title = {
                            Text(text = title)
                        }, navigationIcon = {
                            IconButton(onClick = { this@GroupDetailsActivity.finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        })
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        GroupDetails(targetId, title, description, owner)
                    }
                }


                targetId.group(token) { group ->
                    name = group.name
                    title = group.shownName
                    description = group.description
                    owner = group.owner
                }
            }
        }
    }

    @Composable
    fun GroupDetails(id: Int, title: String, description: String, ownerId: Int) {
        var owner by remember {
            mutableStateOf(Account.UNKNOWN)
        }
        Row(modifier = Modifier.padding(10.dp)) {
            SubcomposeAsyncImage(
                model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${id}&isUser=0",
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = "avatar",
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.padding(horizontal = 5.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                OwnerDetails(owner)
            }
        }
        if (ownerId == -1) return
        ownerId.account(token) { account ->
            owner = account
        }
    }

    @Composable
    fun OwnerDetails(owner: Account) {
        Row(modifier = Modifier.clickable {
            startActivity(Intent(baseContext, UserDetailsActivity::class.java).apply {
                putExtra("info", owner.bundle())
                putExtra("token", token)
            })
        }) {
            SubcomposeAsyncImage(
                model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${owner.id}&isUser=1",
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = "avatar",
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
            )
            Text(text = owner.nickname)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview4() {
    QMessengerMobileTheme {
    }
}