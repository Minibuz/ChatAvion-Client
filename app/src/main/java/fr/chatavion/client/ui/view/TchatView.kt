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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
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

    @OptIn(ExperimentalComposeUiApi::class)
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
                        .semantics {testTagsAsResourceId = true}
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
                                disabledElevation = 0.dp,
                            ),
                            modifier = Modifier.testTag("paramSwitch"),
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
                                modifier = Modifier.testTag("commDropDown"),
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
                                modifier = Modifier.testTag("connectionSwitch"),
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
                            value = msg.replace("\n", ""),
                            onValueChange = { msg = it },
                            label = {Text(text = "Message text...")},
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                            modifier = Modifier.semantics{
                                testTagsAsResourceId = true
                                }.testTag("msgEditField")
                        )
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        modifier = Modifier.semantics{
                            testTagsAsResourceId = true}
                            .testTag("sendBtn"),
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
                        .padding(PaddingValues(horizontal = 25.dp, vertical = 15.dp)),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
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
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        )
        {
            Text(
                text = "$pseudo",
                color = MaterialTheme.colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()

            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text ="$text",
                color = MaterialTheme.colors.onPrimary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
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
