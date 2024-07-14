package org.qbychat.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.generateInviteCode
import org.qbychat.android.utils.translate

class AdminActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val token = intent.getStringExtra("token")!!
        val account = intent.getBundleExtra("account")!!.getSerializable("object")!! as Account
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
                                Text(text = stringResource(R.string.admin))
                            },
                            navigationIcon = {
                                IconButton(onClick = { this@AdminActivity.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    val inviteCodeDialogState = remember {
                        mutableStateOf(false)
                    }
                    var rememberInviteCode by remember {
                        mutableStateOf("UNKNOWN")
                    }
                    when {
                        inviteCodeDialogState.value -> {
                            InviteCodeDialog(rememberInviteCode) {
                                inviteCodeDialogState.value = false
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(innerPadding)) {
                        if (account.role != Role.ADMIN) {
                            Text(text = stringResource(R.string.non_admin_warning))
                        }
                        Card(
                            modifier = Modifier
                                .padding(10.dp)
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.admin_registation),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(5.dp)
                            )
                            Button(onClick = {
                                token.generateInviteCode { invitation ->
                                    rememberInviteCode = invitation.code
                                    inviteCodeDialogState.value = true
                                }
                            }) {
                                Text(text = stringResource(R.string.gerenate_invite_code))
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun InviteCodeDialog(inviteCode: String, onBtnClick: () -> Unit) {
    val mContext = LocalContext.current
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.invitation))
        },
        text = {
            Text(text = stringResource(R.string.templete_invite_code, inviteCode))
        },
        onDismissRequest = {
            onBtnClick()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clipboard =
                        mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("INVITE_CODE", inviteCode))
                    onBtnClick()
                }
            ) {
                Text(stringResource(R.string.copy))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onBtnClick()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun Preview6() {
    QMessengerMobileTheme {
    }
}