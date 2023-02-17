package fr.chatavion.client.ui.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chatavion.client.R
import fr.chatavion.client.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@Composable
fun UserParameter(
    currentPseudo: String,
    onClose: (String) -> Unit,
    community: String
) {
    var current by remember { mutableStateOf("") }
    val context = LocalContext.current
    val settingsRepository = SettingsRepository(context = context)

    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                    ) {
                        IconButton(
                            onClick = {
                                Log.i("userParameter", "User parameter")
                                onClose()
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.2f)
                                .align(Alignment.CenterVertically),
                            content = {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    "arrowBack",
                                    tint = MaterialTheme.colors.onBackground
                                )
                            }
                        )
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = "Pseudo",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            Divider(
                thickness = 2.dp,
                color = MaterialTheme.colors.onBackground
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight(2 / 4f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = "Pseudo actuel*"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colors.onBackground
                                ),
                                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .fillMaxWidth(0.8f),
                                value = if (current == "") currentPseudo else current.replace(
                                    "\n",
                                    ""
                                ),
                                onValueChange = {
                                    if (it.length <= 35) current = it
                                })
                            IconButton(
                                onClick = {
                                    CoroutineScope(IO).launch {
                                        settingsRepository.setPseudo(current)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically),
                                content = {
                                    Icon(
                                        Icons.Filled.Done,
                                        "Done",
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                }
                            )
                        }
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = stringResource(id = R.string.explication_pseudo_community)+" '$community'"
                        )
                    }
                }
            }
        }
    }
}