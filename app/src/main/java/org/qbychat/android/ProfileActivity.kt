package org.qbychat.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.translate

class ProfileActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val account = intent.getBundleExtra("account")!!.getSerializable("object")!! as Account
        val authorize =
            intent.getBundleExtra("authorize")!!.getSerializable("object")!! as Authorize
        setContent {
            QMessengerMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        title = {
                            Text(text = R.string.profile.translate(application))
                        },
                        navigationIcon = {
                            IconButton(onClick = { this@ProfileActivity.finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Profile(account = account)
                        Card(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .clickable {

                                }
                        ) {
                            Text(text = account.bio, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = R.string.change_bio.translate(application),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            // todo change bio
                        }

                    }
                }
            }
        }
    }

    @Composable
    fun Profile(account: Account, modifier: Modifier = Modifier) {
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
                    .clickable {

                    }
            )
            Column(modifier = Modifier.padding(horizontal = 5.dp)) {
                Text(
                    text = account.nickname,
                    style = MaterialTheme.typography.titleLarge
                )
                if (account.role == Role.ADMIN) {
                    Text(text = R.string.admin_user.translate(application), color = Color.Red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview5() {
    QMessengerMobileTheme {}
}