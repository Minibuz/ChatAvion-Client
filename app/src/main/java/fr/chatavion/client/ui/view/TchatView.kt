package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TchatView {

    @Composable
    @SuppressLint("NotConstructor")
    fun TchatView(
        navController: NavController,
//        sender: DnsResolver,
        pseudo: String,
        community: String,
        address: String
    ) {
        val sender = DnsResolver()
        val messages = remember { mutableStateListOf<String>() }
        var msg by remember { mutableStateOf("") }
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .weight(1f / 3f)
                            .background(MaterialTheme.colors.background)
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            ),
                            onClick = {
                                Log.i("menu", "Menu pushed")
                            }) {
                            Icon(Icons.Filled.Menu, "menu")
                        }
                    }
                    Column(Modifier.weight(1f / 3f)) {
                        Text(
                            text = "Communauté",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                    Row(Modifier.weight(1f / 3f)) {
                        Column(Modifier.weight(1f / 2f)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                ),
                                onClick = {
                                    Log.i("expandMore", "ExpandMore pushed")
                                }) {
                                Icon(Icons.Filled.ExpandMore, "expandMore")
                            }
                        }
                        Column(Modifier.weight(1f / 2f)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                ),
                                onClick = {
                                    Log.i("wifi", "Wifi pushed")
                                    CoroutineScope(IO).launch {
                                        messages.addAll(
                                            sender.requestHistorique(
                                                community,
                                                address,
                                                1
                                            )
                                        )
                                    }
                                }) {
                                Icon(Icons.Filled.Wifi, "wifi")
                            }
                        }
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                    backgroundColor = MaterialTheme.colors.background
                ) {
                    Box(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .fillMaxWidth(0.8f)
                    ) {
                        TextField(
                            value = msg,
                            onValueChange = { msg = it },
                            label = {},
                            textStyle = TextStyle(fontSize = 16.sp),
                            placeholder = { Text(text = "Message text...") },
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                        )
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        onClick = {
                            if (msg != "") {
                                // TODO - send message to
                                CoroutineScope(IO).launch {
                                    sender.sendMessage(community, address, pseudo, msg)
                                    messages.add(msg)
                                    Log.i("Send", "Msg sent : $msg")
                                    msg = ""
                                }
                            }
                        }) {
                        Icon(Icons.Filled.Send, "send")
                    }
                }
            }
        ) { innerTag ->
            Divider(color = White, thickness = 1.dp)
            Column(Modifier.padding(innerTag))
            {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.background)
                ) {
                    items(messages) { message ->
                        DisplayCenterText(message, pseudo)
                    }
                }
            }
        }
    }

    @Composable
    fun DisplayCenterText(text: String, pseudo: String) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp, 5.dp)
                .height(35.dp)
        )
        {
            Text(
//                text = "$pseudo: $text",
                text = text,
                color = MaterialTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .height(20.dp)
            )
        }
    }

    private fun sendHistorique(
        sender: DnsResolver,
        community: String,
        address: String,
        nbToRetrieve: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            sender.requestHistorique(community, address, nbToRetrieve)
        }
    }


    private suspend fun sendMessage(
        text: String,
        pseudo: String,
        community: String,
        address: String,
        words: SnapshotStateList<String>,
        sender: DnsResolver
    ): Boolean {
        var returnVal: Boolean
        withContext(Dispatchers.IO) {
            returnVal = sender.sendMessage(community, address, pseudo, text)
        }
        if (returnVal) {
            words.add(text)
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }
}
