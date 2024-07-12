package org.qbychat.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.account
import org.qbychat.android.utils.translate

class UserDetailsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bundleExtra = intent.getBundleExtra("info")
        val user = if (bundleExtra == null) {
            Account.UNKNOWN
        } else {
            (bundleExtra.getSerializable("object")) as Account
        }
        setContent {
            QMessengerMobileTheme {
                var account by remember {
                    mutableStateOf(user)
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ), title = {
                            Text(text = account.nickname)
                        }, navigationIcon = {
                            IconButton(onClick = { this@UserDetailsActivity.finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        })
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        UserDetails(account)
                    }
                }
                if (user == Account.UNKNOWN) {
                    val targetId = intent.getIntExtra("id", -1)
                    val token = intent.getStringExtra("token")!!
                    targetId.account(token) { account1 ->
                       account = account1
                    }
                }
            }
        }
    }


    @Composable
    fun UserDetails(account: Account) {
        Row(modifier = Modifier.padding(10.dp)) {
            SubcomposeAsyncImage(
                model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${account.id}&isUser=1",
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
                    text = account.nickname,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = account.bio, style = MaterialTheme.typography.bodyMedium)
                if (account.role == Role.ADMIN) {
                    AssistChip(
                        label = {
                            Text(text = R.string.admin_user.translate(application), color = Color.Red)
                        },
                        onClick = {},
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "admin",
                                Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Preview3() {
    QMessengerMobileTheme {}
}