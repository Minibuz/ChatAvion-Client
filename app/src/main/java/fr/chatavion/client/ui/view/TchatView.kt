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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    private var historySender by mutableStateOf(true)
    private var retrieve by mutableStateOf(true)

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition")
    fun TchatView(
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
                                text = community,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .wrapContentHeight()
                            )
                        }
                        BurgerMenuCommunity(displayBurgerMenu) {
                            displayBurgerMenu = !displayBurgerMenu
                        }
                        IconButton(
                            modifier = Modifier.semantics{
                                testTagsAsResourceId = true
                            }.testTag("paramSwitch"),
                            onClick = {
                                Log.i("expandMore", "ExpandMore pushed")
                                displayBurgerMenu = !displayBurgerMenu
                            }) {
                            Icon(Icons.Filled.ExpandMore, "expandMore")
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.semantics{
                                testTagsAsResourceId = true
                            }.testTag("commDropDown"),
                            onClick = {
                            Log.i("menu", "Menu pushed")
                            openDrawer()
                        }) {
                            Icon(Icons.Filled.Menu, "menu")
                        }
                    }, actions = {
                        IconButton(
                            modifier = Modifier.semantics{
                                testTagsAsResourceId = true
                            }.testTag("connectionSwitch"),
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
                    Column(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .fillMaxWidth(0.8f)
                            .fillMaxHeight()
                    ) {
                        TextField(
                            value = msg.replace("\n", ""),
                            modifier = Modifier.semantics{
                                testTagsAsResourceId = true
                            }.testTag("msgEditField"),
                            onValueChange = {
                                msg = it
                                remainingCharacter =
                                    35 - msg.toByteArray(StandardCharsets.UTF_8).size
                            },
                            placeholder = { Text(text = stringResource(R.string.message_text)) },
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
                        )
                    }
                    Column(
                    ) {
                        Text(
                            text = "$remainingCharacter/35",
                            color = if (remainingCharacter < 0) MaterialTheme.colors.error else MaterialTheme.colors.primaryVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .wrapContentSize()
                                .fillMaxHeight(0.3f)
                                .fillMaxWidth()
                        )
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
                                        remainingCharacter = 35
                                    }
                                }
                            }) {
                            Icon(Icons.Filled.Send, "send")
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
            val parts: List<String> = text.split(":::")
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
                softWrap = true,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    fun DrawerAppComponent(
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
                pseudo, community, address
            ) { coroutineScope.launch { drawerState.open() } }
        }
    }

    @Composable
    fun DrawerContentComponent(
        closeDrawer: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
            ) {
                Surface(
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row() {
                        Icon(
                            Icons.Filled.Menu,
                            "menu",
                            tint = MaterialTheme.colors.background,
                            modifier = Modifier
                                .fillMaxWidth(0.2f)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = "Paramètres",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterVertically),
                            color = MaterialTheme.colors.background
                        )
                    }
                }
            }
            Divider(
                color = MaterialTheme.colors.background,
                thickness = 2.dp,
            )
            Box(
                modifier = Modifier.fillMaxHeight(2/4f)
            ) {
                Column {
                    for (index in Parameters.values().indices) {
                        val screen = getScreenBasedOnIndex(index).name
                        Column(
                            modifier = Modifier.clickable(onClick = {
                                closeDrawer()
                            }), content = {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    color = MaterialTheme.colors.onBackground
                                ) {
                                    Button(
                                        content = {
                                            Text(
                                                text = screen,
                                                color = MaterialTheme.colors.background,
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        modifier = Modifier.padding(8.dp),
                                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.onBackground),
                                        onClick = {
                                            Log.i("Parameters", "Parameters")
                                        }
                                    )
                                    Divider(
                                        color = MaterialTheme.colors.background,
                                        thickness = 1.dp,
                                        startIndent = (1/5f).dp
                                    )
                                }
                            })
                    }
                }
            }
        }
    }

    /**
     * Returns the corresponding DrawerAppScreen based on the index passed to it.
     */
    private fun getScreenBasedOnIndex(index: Int) = when (index) {
        0 -> Parameters.Pseudo
        1 -> Parameters.Theme
        2 -> Parameters.Langue
        3 -> Parameters.Notifications
        else -> Parameters.Pseudo
    }

    enum class Parameters {
        Pseudo,
        Theme,
        Langue,
        Notifications
    }

    enum class AdvanceParameters {
        Messages,
        Connexion
    }

    @Composable
    fun BurgerMenuCommunity(
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
            messages.add("$pseudo:::$text")
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }
}