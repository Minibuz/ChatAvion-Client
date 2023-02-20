package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import fr.chatavion.client.communityViewModel
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.db.entity.Message
import fr.chatavion.client.messageViewModel
import fr.chatavion.client.ui.theme.White
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.nio.charset.StandardCharsets

class TchatView {
    private val communityVM = communityViewModel
    private val messageVM = messageViewModel

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor")
    fun TchatView(
        navController: NavController,
        communityName: String,
        communityAddress: String,
        communityId: Int,
        openDrawer: () -> Unit
    ) {
        val com = communityViewModel.getAll()
        Log.i("COM", com.toString())
        Log.i("SIZE", com.value?.size.toString())
        com.value?.forEach { communityWithMessages ->
            Log.i(
                "AAAH",
                communityWithMessages.community.name
            )
        }

        val context = LocalContext.current

        val community = communityId.let {
            communityVM.getById(it).observeAsState().value
        }

        Log.i("CommunityName", community?.community?.name.toString())

        val sender = DnsResolver()
        val messages = remember { mutableStateListOf<Message>() }
        var msg by remember { mutableStateOf("") }
        var remainingCharacter by remember { mutableStateOf(35) }
        var enableSendingMessage by remember { mutableStateOf(true) }
        var displayBurgerMenu by remember { mutableStateOf(false) }

        BackHandler(enabled = true) {
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
                                text = communityName,
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
                                    val settingsRepository = SettingsRepository(context = context)
                                    settingsRepository.pseudo.collect { pseudo ->
                                        if (msg != "") {
                                            Log.i("test", "$pseudo:$msg")
                                            sendMessage(
                                                msg,
                                                pseudo,
                                                communityName,
                                                communityAddress,
                                                messages,
                                                sender,
                                                communityId
                                            )
                                            msg = ""
                                            remainingCharacter = 35
                                            enableSendingMessage = true
                                        }
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
                    if (community != null) {
                        items(
                            community.messages
                        ) { message ->
                            DisplayCenterText(message.message)
                        }
                    }
                }
            }
        }

        LaunchedEffect(true) {
            withContext(IO) {
                try {
                    while (true) {
                        Log.i("History", "Retrieve the history")
                        val msg = sender.requestHistorique(
                            communityName,
                            communityAddress,
                            10
                        )
                        // TODO :
                        // msg = List<String>
                        // Add in messageVM.insertAll() List<Message>
                        // So for each String in List<String>,
                        // split pseudo and message and add to
                        // list while creating Message each time

//                        val parts: List<String> = msg.split(":::")
//
//                        msg.stream().map { m -> Message(m) }
//
//                        messageVM.insertAll(msg)
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
        navController: NavController,
        communityName: String,
        communityAddress: String,
        communityId: Int
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContentComponent(
                    communityName,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            },
            content = {
                TchatView(
                    navController, communityName, communityAddress, communityId
                ) { coroutineScope.launch { drawerState.open() } }
            }
        )
    }

    @Composable
    fun DrawerContentComponent(
        communityName: String,
        closeDrawer: () -> Unit
    ) {
        val context = LocalContext.current
        val settingsRepository = SettingsRepository(context = context)

        var pseudoCurrent by remember { mutableStateOf("") }
        var showUser by remember { mutableStateOf(false) }
        if (showUser) {
            // Add pages here
            UserParameter(
                community = communityName,
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
                                                CoroutineScope(IO).launch {
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
        communityName: String,
        communityAddress: String,
        messages: SnapshotStateList<Message>,
        sender: DnsResolver,
        id: Int
    ): Boolean {
        var returnVal: Boolean
        withContext(IO) {
            returnVal = sender.sendMessage(communityName, communityAddress, pseudo, text)
        }
        if (returnVal) {
            messages.add(
                Message(
                    pseudo,
                    text,
                    id
                )
            )
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }
}