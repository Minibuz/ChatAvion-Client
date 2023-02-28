package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
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
import fr.chatavion.client.connection.http.HttpResolver
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.db.entity.Message
import fr.chatavion.client.db.entity.MessageStatus
import fr.chatavion.client.ui.MESSAGE_SIZE
import fr.chatavion.client.ui.UiText
import fr.chatavion.client.ui.theme.White
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException

class TchatView {

    private val communityVM = communityViewModel

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor")
    fun TchatView(
        navController: NavController,
        communityName: String,
        communityAddress: String,
        communityId: Int,
        lastId: Int,
        openDrawer: () -> Unit
    ) {
        val context = LocalContext.current

        val community by communityVM.getById(communityId).observeAsState(Community(communityName,communityAddress,"", lastId, communityId))

        val dnsResolver = DnsResolver()
        val httpResolver = HttpResolver()
        val messages = remember { mutableStateListOf<Message>() }
        var msg by remember { mutableStateOf("") }
        var remainingCharacter by remember { mutableStateOf(MESSAGE_SIZE) }
        var enableSendingMessage by remember { mutableStateOf(true) }
        var displayBurgerMenu by remember { mutableStateOf(false) }
        var connectionIsDNS by remember { mutableStateOf(true) }

        // Popup
        var showCommunityDetails by remember { mutableStateOf(false) }
        if (showCommunityDetails) {
            AlertDialog(
                onDismissRequest = { showCommunityDetails = false },
                backgroundColor = MaterialTheme.colors.background,
                text = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(4 / 12f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                text = "Community details",
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "$communityName@$communityAddress",
                                color = MaterialTheme.colors.onBackground,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                },
                confirmButton = {

                })
        }

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
                            OutlinedButton(
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent),
                                border = BorderStroke(0.dp, Color.Transparent),
                                onClick = {
                                    Log.i("test","test")
                                    showCommunityDetails = true
                                },
                                modifier = Modifier
                                    .fillMaxSize(1f)
                            ) {
                                Text(
                                    text = communityName,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colors.onPrimary,
                                    modifier = Modifier
                                        .wrapContentSize(align = Alignment.Center)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("commName")
                                )
                            }
                        }
                        BurgerMenuCommunity(navController, communityId, displayBurgerMenu) {
                            displayBurgerMenu = !displayBurgerMenu
                        }
                            IconButton(
                                modifier = Modifier
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("commDropDown"),
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
                                .testTag("paramSwitch"),
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
                                if (connectionIsDNS) {
                                    connectionIsDNS = false
                                    Utils.showInfoToast(context.getString(R.string.connectionSwitchHTTP), context)
                                }
                                else{
                                    connectionIsDNS = true
                                    Utils.showInfoToast(context.getString(R.string.connectionSwitchDNS), context)
                                }
                            }) {
                            Column {
                                Icon(
                                    Icons.Filled.Wifi,
                                    "wifi",
                                modifier = Modifier.align(CenterHorizontally))
                                Text(text = if(connectionIsDNS) "DNS" else "HTTP")
                            }
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
                                msg = it.trim()
                                remainingCharacter =
                                    160 - msg.toByteArray(StandardCharsets.UTF_8).size
                            },
                            placeholder = { Text(text = stringResource(R.string.message_text)) },
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background)
                        )
                    }
                    Column {
                        Text(
                            text = "$remainingCharacter/${MESSAGE_SIZE}",
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
                                        Log.i("test", "${community.pseudo}:$msg")
                                        val ret = sendMessage(
                                            msg,
                                            community.pseudo,
                                            communityName,
                                            communityAddress,
                                            messages,
                                            dnsResolver,
                                            httpResolver,
                                            connectionIsDNS
                                        )

                                        if (ret) {
                                            msg = ""
                                            remainingCharacter = MESSAGE_SIZE
                                        } else {
                                            withContext(Main) {
                                                UiText.StringResource(R.string.messageTooLong).asString(context)
                                            }
                                        }
                                    }
                                    enableSendingMessage = true
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

        LaunchedEffect(true) {
            withContext(IO) {
                try {
                    dnsResolver.id = community.idLastMessage - 9
                    httpResolver.id = community.idLastMessage - 9

                    while (true) {
                        Log.i("History", "Retrieve the history")

                        if (connectionIsDNS) {
                            dnsHistoryRetrieval(
                                dnsResolver,
                                communityName,
                                communityAddress,
                                messages
                            )
                            httpResolver.id = dnsResolver.id
                        } else {
                            httpHistoryRetrieval(
                                httpResolver,
                                communityName,
                                communityAddress,
                                messages
                            )
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


    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun DisplayCenterText(message: Message) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        )
        {
            Text(
                text = message.pseudo.trim(),
                color = if (message.send) Color.Blue else MaterialTheme.colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("pseudoTag")
            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text = message.message.trim(),
                color = if (message.status == MessageStatus.SEND) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onPrimary,
                fontSize = 14.sp,
                softWrap = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("messageTag")
            )
        }
    }

    @Composable
    fun DrawerAppComponent(
        navController: NavController,
        communityName: String,
        communityAddress: String,
        communityId: Int,
        lastId: Int,
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContentComponent(
                    navController,
                    communityId
                )
            },
            content = {
                TchatView(
                    navController, communityName, communityAddress, communityId, lastId
                ) { coroutineScope.launch { drawerState.open() } }
            }
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun DrawerContentComponent(
        navController: NavController,
        communityId: Int
    ) {
        val community by communityVM.getById(communityId).observeAsState(Community("","","", -1))

        val context = LocalContext.current
        var pseudoCurrent by remember { mutableStateOf("") }
        var menu by remember { mutableStateOf(Parameters.Main) }
        val settingsRepository = SettingsRepository(context = context)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //Top Drawer
            when (menu) {
                Parameters.Main -> {
                    TopDrawer(
                        onClickedIcon = { Log.i("Top drawer", "Touched") },
                        icon = Icons.Filled.Menu,
                        resId = R.string.parameters
                    )
                }
                Parameters.Pseudo -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = Parameters.Main
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.pseudo
                    )
                }
                Parameters.Language -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = Parameters.Main
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.langue
                    )
                }
                Parameters.Theme -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = Parameters.Main
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.theme
                    )
                }
                Parameters.Messages -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = Parameters.Main
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.messages
                    )
                }
                Parameters.NetworkConnection -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = Parameters.Main
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.network_connection
                    )
                }
                else -> {}
            }
            Divider(
                thickness = 2.dp,
                color = MaterialTheme.colors.onBackground
            )
            //Middle Drawer
            Box(
                modifier = Modifier.weight(1f),
            ) {
                when(menu){
                    Parameters.Main -> {
                        ParametersColumn(
                            parametersSet = Parameters.values() as Array<Param>,
                            updateMenu =
                            {
                                when(it) {
                                    Parameters.Pseudo -> {
                                        Log.i("Parameters", "Pseudo touched")
                                        CoroutineScope(Dispatchers.Default).launch {
                                            settingsRepository.pseudo.collect { pseudo ->
                                                pseudoCurrent = pseudo
                                            }

                                        }
                                        menu = Parameters.Pseudo
                                    }
                                    Parameters.Theme -> {
                                        Log.i("Parameters", "Theme touched")
                                        menu = Parameters.Theme
                                    }
                                    Parameters.Language -> {
                                        Log.i("Parameters", "Language touched")
                                        menu = Parameters.Language
                                    }
                                    Parameters.Messages -> {
                                        Log.i("Parameters", "Messages touched")
                                        menu = Parameters.Messages
                                    }
                                    Parameters.NetworkConnection -> {
                                        Log.i("Parameters", "Messages touched")
                                        menu = Parameters.NetworkConnection
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }
                    Parameters.Language -> {
                        ParametersColumn(
                            parametersSet = Language.values() as Array<Param>,
                            updateMenu = {}
                        )
                    }
                    Parameters.Theme -> {
                        ParametersColumn(
                            parametersSet = Theme.values() as Array<Param>,
                            updateMenu = {}
                        )
                    }
                    Parameters.Messages -> {
                        ParametersColumn(
                            parametersSet = Messages.values() as Array<Param>,
                            updateMenu = {}
                        )
                    }
                    Parameters.NetworkConnection -> {
                        ParametersColumn(
                            parametersSet = NetworkConnection.values() as Array<Param>,
                            updateMenu = {}
                        )
                    }
                    Parameters.Pseudo -> {
                        UserParameter(
                            pseudo = community.pseudo,
                            communityId = community.communityId,
                            onClose = {
                                menu = Parameters.Main
                            },
                        )
                    }
                    else -> {}
                }
            }
            //Bottom Drawer
            if(menu == Parameters.Main) {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Divider(
                        thickness = 2.dp,
                        color = MaterialTheme.colors.onBackground
                    )
                    BottomDrawer(navController = navController)
                }
            }
        }
    }

    @Composable
    fun TopDrawer(
        onClickedIcon: () -> Unit,
        icon: ImageVector,
        @StringRes resId: Int
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
        ) {
            IconButton(
                onClick = onClickedIcon,
                modifier = Modifier
                    .fillMaxWidth(1 / 5f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    icon,
                    UiText.StringResource(resId).asString(),
                    tint = MaterialTheme.colors.onBackground
                )
            }
            Text(
                text = UiText.StringResource(resId).asString(),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ParametersColumn(
        parametersSet: Array<Param>,
        updateMenu: (Param) -> Unit
    ){
        LazyColumn() {
            items (parametersSet) { parameter ->
                if (parameter.getId() != R.string.parameters && parameter.getId() != R.string.notifications && parameter.getId() != R.string.advanced_parameters) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag(
                                UiText
                                    .StringResource(parameter.getId())
                                    .toString()
                            )
                            .background(MaterialTheme.colors.background),
                        content = {
                            TextButton(
                                content = {
                                    Text(
                                        color = MaterialTheme.colors.onBackground,
                                        text = UiText.StringResource(parameter.getId()).asString(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                                onClick = { updateMenu(parameter) },
                            )
                            Divider(
                                color = MaterialTheme.colors.onBackground,
                                thickness = 1.dp,
                                startIndent = (1 / 5f).dp
                            )

                        }
                    )
                }
            }
        }
    }

    @Composable
    fun BottomDrawer(
        navController: NavController
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("auth_page") }
                .padding(8.dp)
        ) {
            Icon(
                Icons.Filled.Home,
                "home",
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
            Text(
                text = stringResource(id = R.string.back_main_screen),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
            )
        }
    }

    interface Param {
        fun getId(): Int
    }

    enum class Parameters(@StringRes val resId: Int) : Param {
        Main(R.string.parameters),
        Pseudo(R.string.pseudo),
        Theme(R.string.theme),
        Language(R.string.langue),
        Notifications(R.string.notifications),
        Advanced(R.string.advanced_parameters),
        Messages(R.string.messages),
        NetworkConnection(R.string.network_connection);
        override fun getId(): Int {
            return this.resId
        }
    }

    enum class Language(@StringRes val resId: Int) : Param{
        French(R.string.french),
        English(R.string.english);

        override fun getId(): Int {
            return this.resId
        }
    }

    enum class Theme(@StringRes val resId: Int) : Param{
        French(R.string.light),
        English(R.string.dark);

        override fun getId(): Int {
            return this.resId
        }
    }

    enum class Messages(@StringRes val resId: Int) : Param{
        RefreshTime(R.string.refresh_time),
        LoadingHistory(R.string.loading_history),
        Encoding(R.string.encoding);

        override fun getId(): Int {
            return this.resId
        }
    }

    enum class NetworkConnection(@StringRes val resId: Int) : Param{
        TransactionTypeDNS(R.string.transaction_type_dns),
        ProtocolChoice(R.string.protocol_choice);

        override fun getId(): Int {
            return this.resId
        }
    }

    @Composable
    fun BurgerMenuCommunity(
        navController: NavController,
        communityId: Int,
        displayMenu: Boolean,
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current
        val communities by communityVM.getAll().observeAsState(listOf())

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        val lazyListState = rememberLazyListState()
        DropdownMenu(
            expanded = displayMenu,
            onDismissRequest = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
        ) {
            if (communities.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(width = screenWidth, height = screenHeight/4)
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colors.background)
                            .padding(PaddingValues(horizontal = 0.dp, vertical = 2.dp)),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        state = lazyListState
                    ) {
                        items(items = communities) { community ->
                            Log.i("test", "${community.communityId} + $communityId")
                            if(community.communityId != communityId) {
                                DropdownMenuItem(
                                    onClick = {

                                        Utils.showInfoToast(context.getString(R.string.commuSwitch) +" "+ community.name, context)


                                        val id = isCommunityStillAvailable(
                                            communityAddress = community.address,
                                            communityName = community.name
                                        )
                                        navController.navigate("tchat_page/${community.name}/${community.address}/${community.communityId}/${id}")
                                    },
                                ) {
                                    Row() {
                                        Text(
                                            text = "${community.name}@${community.address}",
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = {
                                                Log.i("Community", "Deleting community $community")
                                                communityViewModel.delete(community)
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                "close",
                                                tint = MaterialTheme.colors.onBackground,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
            returnVal = if (isDns) {
                senderDns.sendMessage(community, address, pseudo, message)
            } else {
                senderHttp.sendMessage(community, address, pseudo, message)
            }
        }
        if (returnVal) {
            messages.add(
                Message(
                    pseudo,
                    message,
                    MessageStatus.SEND,
                    true,
                    3,
                )
            )
            Log.i("Message", "Success")
        } else
            Log.i("Message", "Error")
        return returnVal
    }

    private fun isCommunityStillAvailable(
        communityName: String,
        communityAddress: String,
        ) : Int {
        Log.i("Address", communityAddress)
        Log.i("Community", communityName)
        var id = 0
        var isConnectionOk = false
        CoroutineScope(IO).launch {
            if (testHttp()) {
                val httpSender = HttpResolver()
                isConnectionOk = httpSender.communityChecker(communityAddress, communityName)
                id = httpSender.id
            } else {
                val dnsSender = DnsResolver()
                dnsSender.findType(communityAddress)
                isConnectionOk = dnsSender.communityDetection(communityAddress, communityName)
                id = dnsSender.id
            }

            if (isConnectionOk) {
                // TODO Toast if true
            } else {
                // TODO Toast if false
            }
        }
        return if (isConnectionOk) {
                id
            } else {
                -1
            }
    }
}