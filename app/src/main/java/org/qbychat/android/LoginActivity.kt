@file:SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

package org.qbychat.android

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.qbychat.android.ui.theme.QMessengerMobileTheme
import org.qbychat.android.utils.login
import org.qbychat.android.utils.saveAuthorize


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QMessengerMobileTheme {
                val scope = rememberCoroutineScope()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(10.dp)
                            .fillMaxSize(1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .imePadding()
                        ) {
                            Login(modifier = Modifier.padding(16.dp))
                        }

                        Text(
                            text = stringResource(R.string.register_tip),
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.BottomEnd)
                                .clickable {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://chat.lunarclient.top/web/register")
                                    )
                                    startActivity(browserIntent)
                                }
                        )
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    fun Login(modifier: Modifier = Modifier) {
        val mContext = LocalContext.current
        var username by remember {
            mutableStateOf(TextFieldValue(""))
        }
        var password by remember {
            mutableStateOf(TextFieldValue(""))
        }
        Text(
            text = stringResource(R.string.login_title),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column {
            OutlinedTextField(
                value = username,
                onValueChange = { newValue ->
                    username = newValue
                },
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 10.dp),
                label = {
                    Text(text = stringResource(R.string.username))
                }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    password = newValue
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 10.dp),
                label = {
                    Text(text = stringResource(R.string.login_password))
                }
            )
        }
        var isAuthorizing by remember {
            mutableStateOf(false)
        }

        Button(
            onClick = {
                if (isAuthorizing) return@Button
                val runnable = Runnable {
                    isAuthorizing = true
                    try {
                        val authorize = login(username.text, password.text)
                        if (authorize != null) {
                            authorize.password = password.text
                            saveAuthorize(mContext, authorize)
                            startActivity(Intent(mContext, MainActivity::class.java))
                        } else {
                            Looper.prepare()
                            Toast.makeText(mContext, R.string.login_failed, Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.stackTraceToString())
                    } finally {
                        isAuthorizing = false
                    }
                }
                Thread(runnable).start()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = stringResource(R.string.login_button))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    QMessengerMobileTheme {
    }
}