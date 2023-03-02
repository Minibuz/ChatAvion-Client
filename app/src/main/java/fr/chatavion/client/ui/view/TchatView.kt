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
import fr.chatavion.client.ui.theme.Blue
import fr.chatavion.client.ui.theme.White
import fr.chatavion.client.util.LocaleHelper
import fr.chatavion.client.util.ThemeHelper
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException

class TchatView {

    private val communityVM = communityViewModel

    /**
     * Composable function that displays the TchatView UI.
     * @param navController Navigation controller for navigating between destinations.
     * @param communityName Name of the community.
     * @param communityAddress Address of the community.
     * @param communityId ID of the community.
     * @param lastId Last message ID.
     * @param openDrawer Function that opens the navigation drawer.
     */
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
        val settingsRepository = SettingsRepository(context = context)
        val refreshTime by settingsRepository.refreshTime.collectAsState(initial = 0L)

        val community by communityVM.getById(communityId)
            .observeAsState(Community(communityName, communityAddress, "", lastId, communityId))

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
                                    Log.i("CommunityDetails", "Show community details")
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
                                Log.i("ExpandCommunity", "Show community registered")
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
                                Log.i("ParametersMenu", "Parameters menu pushed")
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
                                Log.i("Wifi", "Wifi pushed")
                                if (connectionIsDNS && testHttp()) {
                                    connectionIsDNS = false
                                    Utils.showInfoToast(
                                        UiText.StringResource(R.string.connectionSwitchHTTP).asString(context),
                                        context
                                    )
                                } else {
                                    connectionIsDNS = true
                                    Utils.showInfoToast(
                                        UiText.StringResource(R.string.connectionSwitchDNS).asString(context),
                                        context
                                    )
                                }
                            }) {
                            Column {
                                Icon(
                                    Icons.Filled.Wifi,
                                    "wifi",
                                    modifier = Modifier.align(CenterHorizontally)
                                )
                                Text(text = UiText.StringResource(if (connectionIsDNS) R.string.DNS else R.string.HTTP).asString(context))
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
                                msg = it
                                remainingCharacter =
                                    160 - msg.toByteArray(StandardCharsets.UTF_8).size
                            },
                            placeholder = { Text(text = UiText.StringResource(R.string.message_text).asString(context)) },
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

                                    msg = msg.trim()
                                    if (msg != "") {
                                        Log.i("MessageSender", "${community.pseudo}:$msg")
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
                                            CoroutineScope(Main).launch {
                                                Utils.showErrorToast(UiText.StringResource(R.string.messageTooLong).asString(context), context)
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
                        delay(refreshTime * 1000)
                    }
                } catch (e: CancellationException) {
                    e.message?.let { Log.i("History", it) }
                    Log.i("History", "Cancel history retrieve")
                }
            }
        }
    }

    /**
     * Composable function that displays a message with a centered text layout.
     * @param message The message to be displayed.
     * @return A Column composable with centered text layout.
     */
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
                color = if (message.send) Blue else MaterialTheme.colors.onPrimary,
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

    /**
     * Composable function that displays a drawer component and a TchatView composable content.
     * @param navController The NavController used for navigation.
     * @param communityName The name of the community displayed in the TchatView composable content.
     * @param communityAddress The address of the community displayed in the TchatView composable content.
     * @param communityId The ID of the community used for the DrawerContentComponent composable content.
     * @param lastId The last message ID displayed in the TchatView composable content.
     * @return A ModalDrawer composable with drawer and TchatView composable content.
     */
    @Composable
    fun DrawerAppComponent(
        navController: NavController,
        communityName: String,
        communityAddress: String,
        communityId: Int,
        lastId: Int,
        enableDarkTheme: (Boolean) -> Unit,
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        ModalDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContentComponent(
                    navController,
                    communityId,
                    enableDarkTheme
                )
            },
            content = {
                TchatView(
                    navController, communityName, communityAddress, communityId, lastId
                ) { coroutineScope.launch { drawerState.open() } }
            }
        )
    }

    /**
     * This composable function is used to display the content of the drawer.
     *
     * @param navController The navController used to navigate between the different screens
     * @param communityId The id of the community currently selected
     */
    @Composable
    fun DrawerContentComponent(
        navController: NavController,
        communityId: Int,
        enableDarkTheme: (Boolean) -> Unit
    ) {
        val community by communityVM.getById(communityId).observeAsState(Community("", "", "", -1))

        val context = LocalContext.current
        var pseudoCurrent by remember { mutableStateOf("") }
        var menu by remember { mutableStateOf(R.string.parameters) }
        val settingsRepository = SettingsRepository(context = context)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //Top Drawer
            when (menu) {
                R.string.parameters -> {
                    TopDrawer(
                        onClickedIcon = { Log.i("Top drawer", "Touched") },
                        icon = Icons.Filled.Menu,
                        resId = R.string.parameters
                    )
                }
                R.string.pseudo -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.pseudo
                    )
                }
                R.string.theme -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.theme
                    )
                }
                R.string.langue -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.langue
                    )
                }
                R.string.advanced_parameters -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.advanced_parameters
                    )
                }
                R.string.messages -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.advanced_parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.messages
                    )
                }
                R.string.network_connection -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.advanced_parameters
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
                when (menu) {
                    R.string.parameters -> {
                        ParametersColumn(
                            resIds = listOf(
                                R.string.pseudo,
                                R.string.theme,
                                R.string.langue,
                                R.string.advanced_parameters
                            ),
                            onClickParameter =
                            {
                                when (it) {
                                    R.string.pseudo -> {
                                        Log.i("Parameters", "Pseudo touched")
                                        CoroutineScope(Dispatchers.Default).launch {
                                            settingsRepository.pseudo.collect { pseudo ->
                                                pseudoCurrent = pseudo
                                            }

                                        }
                                        menu = R.string.pseudo
                                    }
                                    R.string.theme -> {
                                        Log.i("Parameters", "Theme touched")
                                        menu = R.string.theme
                                    }
                                    R.string.langue -> {
                                        Log.i("Parameters", "Language touched")
                                        menu = R.string.langue
                                    }
                                    R.string.advanced_parameters -> {
                                        Log.i("Parameters", "Advanced parameters touched")
                                        menu = R.string.advanced_parameters
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }
                    R.string.pseudo -> {
                        UserParameter(
                            pseudo = community.pseudo,
                            communityId = community.communityId,
                            onClose = {
                                menu = R.string.parameters
                            },
                        )
                    }
                    R.string.langue -> {
                        ParametersColumn(
                            resIds = listOf(R.string.french, R.string.english),
                            onClickParameter = {
                                when(it) {
                                    R.string.french -> {LocaleHelper.setLocale(context,LocaleHelper.FRENCH)}
                                    R.string.english -> {LocaleHelper.setLocale(context,LocaleHelper.ENGLISH)}
                                }
                                menu = R.string.parameters
                            },
                            selectedParameter = if (LocaleHelper.getLanguage(context) == LocaleHelper.FRENCH) R.string.french else R.string.english
                        )
                    }
                    R.string.theme -> {
                        ParametersColumn(
                            resIds = listOf(R.string.light, R.string.dark),
                            onClickParameter = {
                                Log.i("Theme", "$it")
                                when(it){
                                    R.string.light -> {ThemeHelper.enableDarkTheme(context, false)}
                                    R.string.dark -> {ThemeHelper.enableDarkTheme(context, true)}
                                }
                                enableDarkTheme(ThemeHelper.isDarkThemeEnabled(context))
                            },
                            selectedParameter = if (ThemeHelper.isDarkThemeEnabled(context)) R.string.dark else R.string.light
                        )
                    }
                    R.string.advanced_parameters -> {
                        ParametersColumn(
                            resIds = listOf(R.string.messages, R.string.network_connection),
                            onClickParameter = {
                                when (it) {
                                    R.string.messages -> {
                                        Log.i("Parameters", "Messages touched")
                                        menu = R.string.messages
                                    }
                                    R.string.network_connection -> {
                                        Log.i("Parameters", "Network connection touched")
                                        menu = R.string.network_connection
                                    }
                                }
                            }
                        )
                    }
                    R.string.messages -> {
                        SliderParameterRefreshTime(
                            value = runBlocking { settingsRepository.refreshTime.first() },
                            onClose = {
                                menu = R.string.parameters
                            },
                        )
//                        ParametersColumn(
//                            resIds = listOf(
//                                R.string.refresh_time,
//                                R.string.loading_history
//                            ),
//                            onClickParameter = {}
//                        )
                    }
                    R.string.network_connection -> {
                        ParametersColumn(
                            resIds = listOf(
                                R.string.protocol_choice
                            ),
                            onClickParameter = {}
                        )
                    }
                    else -> {}
                }
            }
            //Bottom Drawer
            if (menu == R.string.parameters) {
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

    /**
     * Composable function that displays the top drawer with an icon button and a text.
     * @param onClickedIcon a function that will be called when the icon button is clicked
     * @param icon the [ImageVector] of the icon button
     * @param resId the resource ID of the string that will be displayed as text
     */
    @Composable
    fun TopDrawer(
        onClickedIcon: () -> Unit,
        icon: ImageVector,
        @StringRes resId: Int
    ) {
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
                    UiText.StringResource(resId).asString(LocalContext.current),
                    tint = MaterialTheme.colors.onBackground
                )
            }
            Text(
                text = UiText.StringResource(resId).asString(LocalContext.current),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }

    /**
     * Composable function that displays a lazy column containing a list of parameters as text buttons
     * @param resIds list of resource IDs representing the parameters to be displayed
     * @param onClickParameter callback function that updates the menu with the selected parameter
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ParametersColumn(
        resIds: List<Int>,
        onClickParameter: (Int) -> Unit,
        selectedParameter: Int? = null
    ) {
        LazyColumn() {
            items(resIds) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag(
                            UiText
                                .StringResource(it)
                                .toString()
                        )
                        .background(MaterialTheme.colors.background)
                        .clickable { onClickParameter(it) },
                    content = {
                        Text(
                            color = if (it == selectedParameter) Blue else MaterialTheme.colors.onBackground,
                            text = UiText.StringResource(it).asString(LocalContext.current),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                            fontWeight = if (it == selectedParameter) FontWeight.Bold else FontWeight.Normal
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

    /**
     * Composable function that displays a bottom drawer in the chat screen with a Home icon and a text that, when clicked,
     * navigates the user back to the main screen.
     * @param navController the NavController object responsible for navigating the user to the desired screen
     */
    @Composable
    fun BottomDrawer(
        navController: NavController
    ) {
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
                text = UiText.StringResource(R.string.back_main_screen).asString(LocalContext.current),
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
            )
        }
    }

    /**
     * Composable function that displays a dropdown menu containing a list of available communities.
     * @param navController NavController to navigate to the chat page when selecting a community.
     * @param communityId The ID of the currently selected community.
     * @param displayMenu Boolean to indicate whether or not the menu should be displayed.
     * @param onDismiss Callback function to be called when the menu is dismissed.
     */
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
                        .size(width = screenWidth, height = screenHeight / 4)
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
                            if (community.communityId != communityId) {
                                DropdownMenuItem(
                                    onClick = {

                                        Utils.showInfoToast(
                                            UiText.StringResource(R.string.commuSwitch).asString(context) + " " + community.name,
                                            context
                                        )

                                        CoroutineScope(IO).launch {
                                            val id = isCommunityStillAvailable(
                                                communityAddress = community.address,
                                                communityName = community.name
                                            )

                                            community.idLastMessage = id
                                            communityVM.insert(community)

                                            CoroutineScope(Main).launch {
                                                navController.navigate("tchat_page/${community.name}/${community.address}/${community.communityId}/${id}")
                                            }
                                        }
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

    /**
     * Sends a message to a given community and address using either DNS or HTTP depending on the specified [isDns] flag.
     * @param message the message to send
     * @param pseudo the pseudo or username of the sender
     * @param community the name of the community to send the message to
     * @param address the address of the recipient
     * @param messages a list of messages to which the sent message will be added
     * @param senderDns a DNS resolver object used to send the message if [isDns] is true
     * @param senderHttp an HTTP resolver object used to send the message if [isDns] is false
     * @param isDns a flag indicating whether to use DNS or HTTP for sending the message
     * @return true if the message was successfully sent, false otherwise
     */
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

    /**
     * Determines whether a community with the specified [communityName] and [communityAddress] is still available.
     * @param communityName the name of the community to check for availability
     * @param communityAddress the address of the community to check for availability
     * @return the ID of the community if it is still available, or -1 if it is not available
     */
    private suspend fun isCommunityStillAvailable(
        communityName: String,
        communityAddress: String,
    ): Int {
        var id: Int
        var isConnectionOk: Boolean
        withContext(IO) {
            if (!testHttp()) {
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
        println(id)
        return if (isConnectionOk) {
            id
        } else {
            -1
        }
    }
}