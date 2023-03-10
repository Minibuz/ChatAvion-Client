package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
        val keyboardController = LocalSoftwareKeyboardController.current

        val settingsRepository = SettingsRepository(context = context)
        val refreshTime by settingsRepository.refreshTime.collectAsState(initial = 0L)
        val protocol by settingsRepository.protocol.collectAsState(initial = SettingsRepository.Protocol.Http)
        val amountMessage by settingsRepository.historyLoading.collectAsState(initial = 10)

        val community by communityVM.getById(communityId)
            .observeAsState(Community(communityName, communityAddress, "", lastId, communityId))

        val dnsResolver = remember { DnsResolver(context) }
        val httpResolver = remember { HttpResolver() }
        val messages = remember { mutableStateListOf<Message>() }
        var msg by remember { mutableStateOf("") }
        var remainingCharacter by remember { mutableStateOf(MESSAGE_SIZE) }
        var enableSendingMessage by remember { mutableStateOf(true) }
        var displayBurgerMenu by remember { mutableStateOf(false) }
        var connectionIsHttp by remember { mutableStateOf(true) }
        LaunchedEffect("HTTP tester") {
            withContext(IO) {
                connectionIsHttp = testHttp()
            }
        }

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
                            if (displayBurgerMenu) {
                                Icon(Icons.Filled.ExpandLess, "expandLess")
                            } else {
                                Icon(Icons.Filled.ExpandMore, "expandMore")
                            }
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
                        Column {
                            Icon(
                                Icons.Filled.Wifi,
                                "wifi",
                                modifier = Modifier.align(CenterHorizontally)
                            )
                            Text(
                                text = UiText.StringResource(
                                    if ((protocol == SettingsRepository.Protocol.Http) && connectionIsHttp) R.string.HTTP else R.string.DNS
                                ).asString(context)
                            )
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
                            placeholder = {
                                Text(
                                    text = UiText.StringResource(R.string.message_text)
                                        .asString(context)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {keyboardController?.hide()}),
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
                                    if(remainingCharacter < 0) {
                                        CoroutineScope(Main).launch {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.message_too_long)
                                                    .asString(context), context
                                            )
                                        }
                                        return@launch
                                    }

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
                                            connectionIsHttp,
                                            protocol
                                        )

                                        if (ret) {
                                            msg = ""
                                            remainingCharacter = MESSAGE_SIZE
                                        } else {
                                            CoroutineScope(Main).launch {
                                                Utils.showErrorToast(
                                                    UiText.StringResource(R.string.message_cannot_be_send)
                                                        .asString(context), context
                                                )
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
                    dnsResolver.id = community.idLastMessage - (amountMessage - 1)
                    dnsResolver.id = if (dnsResolver.id < 0) 0 else dnsResolver.id
                    httpResolver.id = community.idLastMessage - (amountMessage - 1)
                    httpResolver.id = if (httpResolver.id < 0) 0 else httpResolver.id

                    while (true) {
                        Log.i("History", "Retrieve the history")

                        if (protocol == SettingsRepository.Protocol.Http && connectionIsHttp) {
                            httpHistoryRetrieval(
                                httpResolver,
                                communityName,
                                communityAddress,
                                messages,
                                amountMessage
                            )
                            dnsResolver.id = httpResolver.id
                        } else {
                            dnsHistoryRetrieval(
                                dnsResolver,
                                communityName,
                                communityAddress,
                                messages,
                                amountMessage
                            )
                            httpResolver.id = dnsResolver.id
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

            val customTextSelectionColors = TextSelectionColors(
                handleColor = Blue,
                backgroundColor = Blue.copy(alpha = 0.4f)
            )

            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                SelectionContainer {
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
                ) {
                    coroutineScope.launch { drawerState.close() }
                }
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
        enableDarkTheme: (Boolean) -> Unit,
        close: () -> Unit
    ) {
        val community by communityVM.getById(communityId).observeAsState(Community("", "", "", -1))

        val context = LocalContext.current
        var pseudoCurrent by remember { mutableStateOf("") }
        var menu by remember { mutableStateOf(R.string.parameters) }
        val settingsRepository = SettingsRepository(context = context)
        val protocol by settingsRepository.protocol.collectAsState(SettingsRepository.Protocol.Http)

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
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            close()
                        },
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
                R.string.language -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.language
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
                R.string.refresh_time -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.advanced_parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.refresh_time
                    )
                }
                R.string.loading_history -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.advanced_parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.loading_history
                    )
                }
                R.string.protocol_choice -> {
                    TopDrawer(
                        onClickedIcon = {
                            Log.i("Top drawer", "Touched")
                            menu = R.string.advanced_parameters
                        },
                        icon = Icons.Filled.ArrowBack,
                        resId = R.string.protocol_choice
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
                                R.string.language,
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
                                    R.string.language -> {
                                        Log.i("Parameters", "Language touched")
                                        menu = R.string.language
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
                    R.string.language -> {
                        ParametersColumn(
                            resIds = listOf(R.string.french, R.string.english),
                            onClickParameter = {
                                when (it) {
                                    R.string.french -> {
                                        LocaleHelper.setLocale(context, LocaleHelper.FRENCH)
                                        Utils.showInfoToast(
                                            UiText.StringResource(R.string.language_chosen)
                                                .asString(context), context
                                        )
                                    }
                                    R.string.english -> {
                                        LocaleHelper.setLocale(context, LocaleHelper.ENGLISH)
                                        Utils.showInfoToast(
                                            UiText.StringResource(R.string.language_chosen)
                                                .asString(context), context
                                        )
                                    }
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
                                when (it) {
                                    R.string.light -> {
                                        ThemeHelper.enableDarkTheme(context, false)
                                    }
                                    R.string.dark -> {
                                        ThemeHelper.enableDarkTheme(context, true)
                                    }
                                }
                                enableDarkTheme(ThemeHelper.isDarkThemeEnabled(context))
                                menu = R.string.parameters
                            },
                            selectedParameter = if (ThemeHelper.isDarkThemeEnabled(context)) R.string.dark else R.string.light
                        )
                    }
                    R.string.advanced_parameters -> {
                        ParametersColumn(
                            resIds = listOf(
                                R.string.refresh_time,
                                R.string.loading_history,
                                R.string.protocol_choice
                            ),
                            onClickParameter = {
                                when (it) {
                                    R.string.refresh_time -> {
                                        Log.i("Parameters", "Refresh time touched")
                                        menu = R.string.refresh_time
                                    }
                                    R.string.loading_history -> {
                                        Log.i("Parameters", "Loading history touched")
                                        menu = R.string.loading_history
                                    }
                                    R.string.protocol_choice -> {
                                        Log.i("Parameters", "Protocol choice touched")
                                        menu = R.string.protocol_choice
                                    }
                                }
                            }
                        )
                    }
                    R.string.refresh_time -> {
                        SliderParameterRefreshTime(
                            value = runBlocking { settingsRepository.refreshTime.first() },
                            onClose = {
                                menu = R.string.parameters
                            },
                        )
                    }
                    R.string.loading_history -> {
                        SliderParameterAmountMessage(
                            value = runBlocking { settingsRepository.historyLoading.first() },
                            onClose = {
                                menu = R.string.parameters
                            },
                        )
                    }
                    R.string.protocol_choice -> {
                        ParametersColumn(
                            resIds = listOf(R.string.DNS, R.string.HTTP),
                            onClickParameter = {
                                Log.i("Theme", "$it")
                                when (it) {
                                    R.string.DNS -> {
                                        CoroutineScope(IO).launch {
                                            settingsRepository.setProtocol(SettingsRepository.Protocol.Dns)
                                        }
                                        Utils.showInfoToast(
                                            UiText.StringResource(R.string.protocol_dns_chosen)
                                                .asString(context), context
                                        )
                                    }
                                    R.string.HTTP -> {
                                        CoroutineScope(IO).launch {
                                            settingsRepository.setProtocol(SettingsRepository.Protocol.Http)
                                        }
                                        Utils.showInfoToast(
                                            UiText.StringResource(R.string.protocol_http_chosen)
                                                .asString(context), context
                                        )
                                    }
                                }
                                menu = R.string.parameters
                            },
                            selectedParameter = if (protocol == SettingsRepository.Protocol.Dns) R.string.DNS else R.string.HTTP
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
                text = UiText.StringResource(R.string.back_main_screen)
                    .asString(LocalContext.current),
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
                                        Toast.makeText(
                                            context,
                                            UiText.StringResource(R.string.drop_down_commu_conn_first).asString(context)
                                                    +" \"" + community.name + "\" "
                                                    + UiText.StringResource(R.string.drop_down_commu_conn_second).asString(context)
                                                    + " \"" + community.pseudo + "\"",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        CoroutineScope(IO).launch {
                                            val id = isCommunityStillAvailable(
                                                communityAddress = community.address,
                                                communityName = community.name,
                                                context = context,
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
     * Sends a message to a given community and address using either DNS or HTTP depending on the specified [isHttp] flag.
     * @param message the message to send
     * @param pseudo the pseudo or username of the sender
     * @param community the name of the community to send the message to
     * @param address the address of the recipient
     * @param messages a list of messages to which the sent message will be added
     * @param senderDns a DNS resolver object used to send the message if [isHttp] is false
     * @param senderHttp an HTTP resolver object used to send the message if [isHttp] is true
     * @param isHttp a flag indicating whether to use DNS or HTTP for sending the message
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
        isHttp: Boolean,
        protocol: SettingsRepository.Protocol
    ): Boolean {
        var returnVal: Boolean
        withContext(IO) {
            returnVal = if (isHttp && protocol == SettingsRepository.Protocol.Http) {
                senderHttp.sendMessage(community, address, pseudo, message)
            } else {
                senderDns.sendMessage(community, address, pseudo, message)
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
        context: Context
    ): Int {
        var id: Int
        var isConnectionOk: Boolean
        withContext(IO) {
            if (testHttp()) {
                val httpSender = HttpResolver()
                isConnectionOk = httpSender.communityChecker(communityAddress, communityName)
                id = httpSender.id
            } else {
                val dnsSender = DnsResolver(context)
                dnsSender.findType(communityAddress)
                isConnectionOk = dnsSender.communityDetection(communityAddress, communityName)
                id = dnsSender.id
            }
        }
        return if (isConnectionOk) {
            id
        } else {
            -1
        }
    }
}