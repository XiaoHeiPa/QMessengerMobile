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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import kotlin.properties.Delegates

class EditProfileActivity : ComponentActivity() {
    private lateinit var target: Account
    private lateinit var authorize: Authorize
    private var useAdminAPI by Delegates.notNull<Boolean>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authorize = intent.getBundleExtra("authorize")!!.getSerializable("object") as Authorize
        target = intent.getBundleExtra("target")!!.getSerializable("object") as Account
        useAdminAPI = intent.getBooleanExtra("useAdminAPI", false)
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
                                Text(text = stringResource(R.string.edit_profile))
                            },
                            navigationIcon = {
                                IconButton(onClick = { this@EditProfileActivity.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        LabelUsername(target.username)
                    }
                }
            }
        }
    }

    @Composable
    fun LabelUsername(username: String) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row {
                Icon(imageVector = Icons.Outlined.Person, contentDescription = "name")
                Text(text = stringResource(R.string.username))
            }


            Text(
                text = username,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview7() {
        QMessengerMobileTheme {
            LabelUsername("Example")
        }
    }
}



