package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.navigation.NavController
import fr.chatavion.client.R
import fr.chatavion.client.connection.Message
import fr.chatavion.client.connection.MessageStatus
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.connection.http.HttpResolver
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.ui.theme.White
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException

class TchatView {

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor", "CoroutineCreationDuringComposition")
    fun TchatView(
        navController: NavController,
        community: String,
        address: String,
        openDrawer: () -> Unit
    ) {
        val context = LocalContext.current
        val settingsRepository = SettingsRepository(context = context)

        Log.i("Ici", "On est au debut")
        val dnsResolver = DnsResolver()
        val httpResolver = HttpResolver()
        val messages = remember { mutableStateListOf<Message>() }
        var msg by remember { mutableStateOf("") }
        var remainingCharacter by remember { mutableStateOf(35) }
        var enableSendingMessage by remember { mutableStateOf(true) }
        var displayBurgerMenu by remember { mutableStateOf(false) }
        var connectionIsDNS by remember { mutableStateOf(true) }
        var pseudo by remember{ mutableStateOf("") }

        BackHandler(enabled = true) {
            Log.i("Work", "Je marche")
            navController.navigate("auth_page")
        }

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
                            modifier = Modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("paramSwitch"),
                            onClick = {
                                Log.i("expandMore", "ExpandMore pushed")
                                displayBurgerMenu = !displayBurgerMenu
                            }) {
                            Icon(Icons.Filled.ExpandMore, "expandMore")
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("commDropDown"),
                            onClick = {
                                Log.i("menu", "Menu pushed")
                                openDrawer()
                            }) {
                            Icon(Icons.Filled.Menu, "menu")
                        }
                    }, actions = {
                        IconButton(
                            modifier = Modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("connectionSwitch"),
                            onClick = {
                                Log.i("wifi", "Wifi pushed")
                                if(connectionIsDNS){
                                    connectionIsDNS = false
                                    Utils.showInfoToast(R.string.connectionSwitchHTTP.toString(), context)
                                }
                                else{
                                    connectionIsDNS = true
                                    Utils.showInfoToast(R.string.connectionSwitchDNS.toString(), context)
                                }
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
                            modifier = Modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("msgEditField"),
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
                    Column {
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
                            enabled = enableSendingMessage,
                            modifier = Modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("sendBtn"),
                            onClick = {
                                CoroutineScope(IO).launch {
                                    enableSendingMessage = false

                                    if (msg != "") {
                                        Log.i("test", "$pseudo:$msg")
                                        val ret = sendMessage(
                                            msg,
                                            pseudo,
                                            community,
                                            address,
                                            messages,
                                            dnsResolver,
                                            httpResolver,
                                            connectionIsDNS
                                        )

                                        if( ret ) {
                                            msg = ""
                                            remainingCharacter = 35
                                        } else {
                                            withContext(Main) {
                                                Toast.makeText(context, "Test", LENGTH_SHORT).show()
                                            }
                                        }
                                        enableSendingMessage = true
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
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    state = LazyListState(firstVisibleItemIndex = messages.size)
                ) {
                    items(messages) { message ->
                        DisplayCenterText(message)
                    }
                }
            }
        }

        CoroutineScope(Main).launch {
            settingsRepository.pseudo.collect { value ->
                pseudo = value
            }
        }

        LaunchedEffect(true) {
            withContext(IO) {
                try {
                    while (true) {
                        Log.i("History", "Retrieve the history")

                        if (connectionIsDNS) {
                            dnsHistoryRetrieval(dnsResolver, community, address, messages)
                            httpResolver.id = dnsResolver.id
                        } else {
                            httpHistoryRetrieval(httpResolver, community, address, messages)
                            dnsResolver.id = httpResolver.id
                        }

                        delay(10_000L)
                    }
                } catch (e: CancellationException) {
                    e.message?.let { Log.i("History", it) }
                    Log.i("History", "Cancel history retrieve")
                }
            }
        }
    }


    @Composable
    fun DisplayCenterText(message: Message) {

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        )
        {
            Text(
                text = message.user.trim(),
                color = if(message.send) Color.Blue else MaterialTheme.colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text = message.message.trim(),
                color = if(message.status == MessageStatus.SEND) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onPrimary,
                fontSize = 14.sp,
                softWrap = true,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    fun DrawerAppComponent(
        navController: NavController,
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
                    navController,
                    community,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            },
            content = {
                TchatView(
                    navController, community, address
                ) { coroutineScope.launch { drawerState.open() } }
            }
        )
    }

    @Composable
    fun DrawerContentComponent(
        navController: NavController,
        community: String,
        closeDrawer: () -> Unit
    ) {
        val context = LocalContext.current
        val settingsRepository = SettingsRepository(context = context)

        var pseudoCurrent by remember { mutableStateOf("") }
        var showUser by remember { mutableStateOf(false) }
        if (showUser) {
            // Add pages here
            UserParameter(
                community = community,
                currentPseudo = pseudoCurrent,
                onClose = {
                    showUser = false
                }
            )
        }

        Surface(
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.15f)
                ) {
                    Surface(
                        color = MaterialTheme.colors.background,
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        Row {
                            Icon(
                                Icons.Filled.Menu,
                                "menu",
                                tint = MaterialTheme.colors.onBackground,
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .align(Alignment.CenterVertically)
                            )
                            Text(
                                text = "Paramètres",
                                color = MaterialTheme.colors.onBackground,
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
                ) {
                    Column {
                        for (index in Parameters.values().indices) {
                            val screen = getScreenBasedOnIndex(index).name
                            Column(
                                modifier = Modifier.clickable(onClick = {
                                    closeDrawer()
                                }), content = {
                                    Surface(
                                        color = MaterialTheme.colors.background,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        TextButton(
                                            content = {
                                                Text(
                                                    color = MaterialTheme.colors.onBackground,
                                                    text = screen,
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            modifier = Modifier.padding(8.dp),
                                            onClick = {
                                                Log.i("Parameters", "Parameters")
                                                CoroutineScope(Dispatchers.Default).launch {
                                                    settingsRepository.pseudo.collect { pseudo ->
                                                        pseudoCurrent = pseudo
                                                    }
                                                }
                                                showUser = true
                                            },
                                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                        )
                                        Divider(
                                            color = MaterialTheme.colors.onBackground,
                                            thickness = 1.dp,
                                            startIndent = (1 / 5f).dp
                                        )
                                    }
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colors.onBackground
                )
                IconButton(
                    onClick = {
                        navController.navigate("auth_page")
                    }
                ) {
                    Row() {
                        Icon(
                            Icons.Filled.Home,
                            "home",
                            tint = MaterialTheme.colors.onBackground,
                            modifier = Modifier
                                .fillMaxWidth(0.2f)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = "Retour à l'écran principal",
                            color = MaterialTheme.colors.onBackground,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterVertically)
                        )
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
                        Toast.makeText(context, "$i", LENGTH_SHORT).show()
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
        message: String,
        pseudo: String,
        community: String,
        address: String,
        messages: SnapshotStateList<Message>,
        senderDns: DnsResolver,
        senderHttp: HttpResolver,
        isDns: Boolean
    ): Boolean {
        var returnVal: Boolean
        withContext(IO) {
            if(isDns) {
                returnVal = senderDns.sendMessage(community, address, pseudo, message)
            } else {
                returnVal = senderHttp.sendMessage(community, address, pseudo, message)
            }
        }
        if (returnVal) {
            messages.add(Message(MessageStatus.SEND, pseudo, message, true))
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }
}