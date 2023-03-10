package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.chatavion.client.R
import fr.chatavion.client.communityViewModel
import fr.chatavion.client.connection.dns.DnsResolver
import fr.chatavion.client.connection.http.HttpResolver
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.ui.PSEUDO_SIZE
import fr.chatavion.client.ui.UiText
import fr.chatavion.client.ui.theme.Blue
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.net.*

class AuthentificationView {
    private val communityVM = communityViewModel
    /**
     * AuthentificationView is a composable function that displays a login screen
     * @param navController: NavController - used for navigation between screens
     * @return a Composable function that displays a login screen and navigates to the chat screen if login is successful
     */
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor")
    fun AuthentificationView(navController: NavController) {
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current

        val dnsSender = remember { DnsResolver(context) }
        val httpSender = remember { HttpResolver() }

        val settingsRepository = SettingsRepository(context = context)
        var communityId by remember { mutableStateOf("") }

        var pseudo by remember { mutableStateOf("") }
        var communityName by remember { mutableStateOf("") }
        var communityAddress by remember { mutableStateOf("") }
        var enabled by remember { mutableStateOf(false) }
        var isRegisterOk by remember { mutableStateOf(false) }
        var isConnectionOk by remember { mutableStateOf(false) }
        var idLast by remember { mutableStateOf(0) }

        var displayBurgerMenu by remember { mutableStateOf(false) }
        if (pseudo != "" && communityId != "") {
            isRegisterOk = true
        }
        if (isConnectionOk) {
            val community = Community(
                communityName,
                communityAddress,
                pseudo,
                idLast
            )
            LaunchedEffect("insertId") {
                communityViewModel.insert(community)
                delay(250L)
                val id = withContext(IO) {
                    communityViewModel.getId(communityName, communityAddress)
                }
                navController.navigate("tchat_page/${communityName}/${communityAddress}/${id}/${idLast}")
            }
        }
        BackHandler(enabled = true) {
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f / 4f)
                    .height(Dp(500F)),
                painter = painterResource(id = R.drawable.chatavion_logo),
                contentDescription = "Chatavion logo"
            )
            Text(
                UiText.StringResource(R.string.app_name).asString(LocalContext.current),
                fontFamily = FontFamily.Monospace,
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = 30.sp,
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Column(
                modifier = Modifier
                    .weight(2f / 4f)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterHorizontally)
            ) {

                Text(
                    UiText.StringResource(R.string.id_community).asString(LocalContext.current),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row() {
                    TextField(
                        value = communityId.replace("\n", ""),
                        onValueChange = {
                            communityId = it
                            enabled = communityId != "" && pseudo != ""
                        },
                        placeholder = { Text(text = UiText.StringResource(R.string.community_at_ip_serv).asString(LocalContext.current)) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}),
                        modifier = Modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("textEditCommu")
                    )
                    BurgerMenuCommunity(navController, 0, displayBurgerMenu) {
                        displayBurgerMenu = !displayBurgerMenu
                    }
                    IconButton(
                        modifier = Modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("commDropDown"),
                        onClick = {
                            Log.i("ExpandMore", "ExpandMore pushed")
                            displayBurgerMenu = !displayBurgerMenu
                        }) {
                        if(displayBurgerMenu) {
                            Icon(Icons.Filled.ExpandLess, "expandLess")
                        } else {
                            Icon(Icons.Filled.ExpandMore, "expandMore")
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    UiText.StringResource(R.string.pseudo).asString(LocalContext.current),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = pseudo.replace("\n", ""),
                    onValueChange = {
                        if (it.length <= PSEUDO_SIZE) {
                            pseudo = it
                        }
                        enabled = communityId != "" && pseudo != ""
                    },
                    placeholder = { Text(text = UiText.StringResource(R.string.default_pseudo).asString(LocalContext.current)) },
                    textStyle = TextStyle(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {keyboardController?.hide()}),
                    modifier = Modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("textEditPwd")
                )
            }
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f / 4f)
            ) {
                Button(
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(1f / 3f)
                        .width(200.dp)
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("connectionBtn"),
                    enabled = enabled,
                    onClick = {
                        enabled = false
                        if (isRegisterOk) {
                            communityId = communityId.trim()
                            val count = communityId.count { it == '@' }
                            if (count == 1) {
                                val list = communityId.split("@")
                                communityName = list[0].lowercase().trim()
                                if (communityName.toByteArray().size > 20) {
                                    CoroutineScope(IO).launch {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.community_name_too_long).asString(context),
                                                context
                                            )
                                        }
                                    }
                                    return@Button
                                }
                                communityAddress = list[1].lowercase().trim()
                                if (communityAddress.toByteArray().size > 100) {
                                    CoroutineScope(IO).launch {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.community_name_too_long).asString(context),
                                                context
                                            )
                                        }
                                    }
                                    return@Button
                                }
                                pseudo = pseudo.trim()
                                CoroutineScope(IO).launch {
                                    if (communityAddress == "") {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.community_address_empty).asString(context),
                                                context
                                            )
                                        }
                                        return@launch
                                    }
                                    if(communityName == "") {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.community_name_empty).asString(context),
                                                context
                                            )
                                        }
                                        return@launch
                                    }
                                    val isConnected =
                                        sendButtonConnexion(
                                            communityAddress,
                                            communityName,
                                            dnsSender,
                                            httpSender)
                                    if (isConnected && pseudo != "") {
                                        Log.i("Pseudo", "Setting user pseudo to $pseudo")
                                        settingsRepository.setPseudo(pseudo)
                                        idLast = dnsSender.id
                                        withContext(Main) {
                                            Utils.showInfoToast(
                                                UiText.StringResource(R.string.community_connection_succesful).asString(context),
                                                context
                                            )
                                        }
                                        isConnectionOk = true
                                    } else {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                UiText.StringResource(R.string.community_name_not_exist).asString(context),
                                                context
                                            )
                                        }
                                    }
                                }
                            } else {
                                Utils.showErrorToast(
                                    UiText.StringResource(R.string.community_id_must_have_one_At).asString(context),
                                    context
                                )

                            }
                        }
                        enabled = true
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = Blue,
                        disabledBackgroundColor = MaterialTheme.colors.primaryVariant
                    )
                ) {
                    Text(
                        UiText.StringResource(R.string.join_community).asString(context),
                        color = MaterialTheme.colors.onSecondary
                    )
                }
                Card(Modifier.weight(1f / 2f)) {}
            }
        }
    }

    /**
     * A function to establish a connection with a device using the specified address and community.
     * @param address the address of the device to connect to.
     * @param community the community name of the device to connect to.
     * @return true if the connection was established, false otherwise.
     */
    private suspend fun sendButtonConnexion(
        address: String,
        community: String,
        dnsResolver: DnsResolver,
        httpResolver: HttpResolver
    ): Boolean {
        if (address == "" || community == "") {
            Log.e("Connexion", "Address or community is empty")
            return false
        }

        var returnVal: Boolean
        withContext(IO) {
            if (testHttp()) {
                httpResolver.communityChecker(address, community)
                dnsResolver.id = httpResolver.id
                returnVal = httpResolver.isConnected
            } else {
                dnsResolver.findType(address)
                dnsResolver.communityDetection(address, community)
                httpResolver.id = dnsResolver.id
                returnVal = dnsResolver.isConnected
            }
        }
        return returnVal
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
        val settingsRepository = SettingsRepository(context = context)

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
                            Log.i("test", "${community.communityId} + $communityId")
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
                                                protocol = runBlocking { settingsRepository.protocol.first() },
                                                context = context,
                                            )

                                            community.idLastMessage = if(id<0) 0 else id
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



}

/**
 * A function to test the availability of HTTP requests.
 * @return true if the HTTP request was successful, false otherwise.
 */
fun testHttp(): Boolean {
    val url = URL("https://www.google.com")
    try {
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            Log.i(
                "Test HTTP",
                "\nSent 'GET' request to URL : $url; Response Code : $responseCode"
            )
            return responseCode == 200
        }
    } catch (e: IOException) {
        Log.i("Test HTTP", "Cannot use HTTP")
        return false
    }
}


private suspend fun isCommunityStillAvailable(
    communityName: String,
    communityAddress: String,
    protocol: SettingsRepository.Protocol,
    context: Context
): Int {
    Log.i("Address", communityAddress)
    Log.i("Community", communityName)
    var id: Int
    var isConnectionOk: Boolean

    withContext(IO) {
        if (protocol==SettingsRepository.Protocol.Http && testHttp()) {
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
