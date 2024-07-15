package org.qbychat.android

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.BACKEND
import org.qbychat.android.utils.HTTP_PROTOCOL
import org.qbychat.android.utils.changeBio
import org.qbychat.android.utils.changeNickname
import kotlin.properties.Delegates

class EditProfileActivity : ComponentActivity() {
    private lateinit var target: Account
    private lateinit var authorize: Authorize
    private var useAdminAPI by Delegates.notNull<Boolean>()

    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("DEPRECATION")
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
                    val scrollState = rememberScrollState()
                    var currentNickname by remember {
                        mutableStateOf(target.nickname)
                    }
                    var newNickname by remember {
                        mutableStateOf(target.nickname)
                    }
                    var newBio by remember {
                        mutableStateOf(target.bio)
                    }
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(5.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.align(Alignment.Center)) {
                                SubcomposeAsyncImage(
                                    model = "$HTTP_PROTOCOL$BACKEND/avatar/query?id=${target.id}&isUser=1",
                                    loading = {
                                        CircularProgressIndicator()
                                    },
                                    contentDescription = "avatar",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            // TODO Change avatar
                                        }
                                )
                                Text(
                                    text = currentNickname,
                                    style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)
                                )
                            }
                        }
                        TextField(
                            value = newNickname,
                            onValueChange = { newValue ->
                                newNickname = newValue
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newNickname.isEmpty()) return@KeyboardActions // 不推荐使用空昵称, 服务端暂时没有判断, 去除这行可使用空昵称. 后续可能添加限制
                                    authorize.token.changeNickname(newNickname) {
                                        currentNickname = newNickname
                                    }
                                }
                            ),
                            label = {
                                Row {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "icon"
                                    )
                                    Text(text = stringResource(id = R.string.nickname))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = newBio,
                            onValueChange = { newValue ->
                                newBio = newValue
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    authorize.token.changeBio(newBio) {
                                    }
                                }
                            ),
                            label = {
                                Row {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "icon"
                                    )
                                    Text(text = stringResource(id = R.string.bio))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun Preview7() {
        QMessengerMobileTheme {
        }
    }
}

