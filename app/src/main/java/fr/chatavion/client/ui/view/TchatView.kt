package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.chatavion.client.R
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

class TchatView {

    // Never stops so can be a problem if we swap community
    // TODO : make a singleton
    var historySender by mutableStateOf(true)
    var retrieve by mutableStateOf(true)

    @Composable
    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition")
    fun TchatView(
        navController: NavController,
//        sender: DnsResolver,
        pseudo: String,
        community: String,
        address: String,
        openDrawer: () -> Unit
    ) {
        val sender = DnsResolver()
        val messages = remember { mutableStateListOf<String>() }
        var msg by remember { mutableStateOf("") }
        var remainingCharacter by remember { mutableStateOf(35) }

        if (historySender) {
            historySender = false
            CoroutineScope(IO).launch {
                while (retrieve) {
                    messages.addAll(
                        sender.requestHistorique(
                            community,
                            address,
                            10
                        )
                    )
                    delay(10000L)
                }
            }
        }

        var displayBurgerMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    title = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize(4 / 5f)
                        ) {
                            Text(
                                text = "$community",
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .wrapContentHeight()
                            )
                        }
                        burgerMenuCommunity(displayBurgerMenu) {
                            displayBurgerMenu = !displayBurgerMenu
                        }
                        IconButton(
                            onClick = {
                                Log.i("expandMore", "ExpandMore pushed")
                                displayBurgerMenu = !displayBurgerMenu
                            }) {
                            Icon(Icons.Filled.ExpandMore, "expandMore")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            Log.i("menu", "Menu pushed")
                            openDrawer()
                        }) {
                            Icon(Icons.Filled.Menu, "menu")
                        }
                    }, actions = {
                        IconButton(
                            onClick = {
                                Log.i("wifi", "Wifi pushed")
                            }) {
                            Icon(Icons.Filled.Wifi, "wifi")
                        }
                    }
                )
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
                                onValueChange = {
                                    msg = it
                                    remainingCharacter =
                                        35 - msg.toByteArray(StandardCharsets.UTF_8).size
                                },
                                label = { Text(text = R.string.message_text.toString())) },
                                textStyle = TextStyle(fontSize = 16.sp),
                                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                                modifier = Modifier.fillMaxSize(0.8f)
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
                                CoroutineScope(IO).launch {
                                    sendMessage(msg, pseudo, community, address, messages, sender)
                                    msg = ""
                            
                        }
                        Column(
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$remainingCharacter/35",
                                    color = if (remainingCharacter < 0) MaterialTheme.colors.error else MaterialTheme.colors.primaryVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.wrapContentSize()
                                )
                            }
                            Row() {
                                Button(
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp,
                                        disabledElevation = 0.dp
                                    ),
                                    onClick = {
                                        if (msg != "") {
                                            CoroutineScope(IO).launch {
                                                sendMessage(
                                                    msg,
                                                    pseudo,
                                                    community,
                                                    address,
                                                    messages,
                                                    sender
                                                )
                                                msg = ""
                                            }
                                        }
                                    }) {
                                    Icon(Icons.Filled.Send, "send")
                                }
                            }
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
                        DisplayCenterText(message)
                    }
                }
            }
        }
    }


    @Composable
    fun DisplayCenterText(text: String) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        )
        {
            val parts: List<String> = text.split(":")
            Text(
                text = parts[0].trim(),
                color = MaterialTheme.colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text = parts[1].trim(),
                color = MaterialTheme.colors.onPrimary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    fun DrawerAppComponent(
        navController: NavController,
//        sender: DnsResolver,
        pseudo: String,
        community: String,
        address: String
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContentComponent(
                    closeDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        ) {
            TchatView(
                navController, pseudo, community, address
            ) { coroutineScope.launch { drawerState.open() } }
        }
    }

    @Composable
    fun DrawerContentComponent(
        closeDrawer: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (index in Parameters.values().indices) {
                val screen = getScreenBasedOnIndex(index)
                Column(Modifier.clickable(onClick = {
                    closeDrawer()
                }), content = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colors.background
                    ) {
                        Text(text = "param", modifier = Modifier.padding(16.dp))
                    }
                })
            }
        }
    }

    /**
     * Returns the corresponding DrawerAppScreen based on the index passed to it.
     */
    private fun getScreenBasedOnIndex(index: Int) = when (index) {
        0 -> Parameters.Param1
        1 -> Parameters.Param2
        2 -> Parameters.Param3
        else -> Parameters.Param1
    }

    enum class Parameters {
        Param1,
        Param2,
        Param3
    }

    @Composable
    fun burgerMenuCommunity(
        displayMenu: Boolean,
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current

        DropdownMenu(
            expanded = displayMenu,
            onDismissRequest = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth(3 / 5f)
                .background(MaterialTheme.colors.background)
        ) {
            for (i in 1..3) {
                DropdownMenuItem(
                    onClick = {
                        Toast.makeText(context, "$i", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = "Communauté $i")
                }
            }
        }
    }

    private fun sendHistorique(
        sender: DnsResolver,
        community: String,
        address: String,
        nbToRetrieve: Int
    ) {
        CoroutineScope(IO).launch {
            sender.requestHistorique(community, address, nbToRetrieve)
        }
    }

    private suspend fun sendMessage(
        text: String,
        pseudo: String,
        community: String,
        address: String,
        messages: SnapshotStateList<String>,
        sender: DnsResolver
    ): Boolean {
        var returnVal: Boolean
        withContext(IO) {
            returnVal = sender.sendMessage(community, address, pseudo, text)
        }
        if (returnVal) {
            messages.add("$pseudo : $text")
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }
}