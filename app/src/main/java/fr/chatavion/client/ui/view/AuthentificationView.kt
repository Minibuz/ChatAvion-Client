package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.IOException
import java.net.*

class AuthentificationView {
    private val dnsSender = DnsResolver()
    private val httpSender = HttpResolver()

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    @SuppressLint("NotConstructor")
    fun AuthentificationView(navController: NavController) {
        val context = LocalContext.current
        val settingsRepository = SettingsRepository(context = context)
        var communityId by remember { mutableStateOf("") }
        var pseudo by remember { mutableStateOf("") }
        var communityName by remember { mutableStateOf("") }
        var communityAddress by remember { mutableStateOf("") }
        var enabled by remember { mutableStateOf(true) }
        var isRegisterOk by remember { mutableStateOf(false) }
        var isConnectionOk by remember { mutableStateOf(false) }
        if (pseudo != "" && communityId != "") {
            isRegisterOk = true
        }
        if (isConnectionOk) {
            val community = Community(
                communityName,
                communityAddress,
                pseudo
            )
            LaunchedEffect("insertId") {
                communityViewModel.insert(community)
                val id = withContext(IO) {
                    communityViewModel.getId(communityName, communityAddress)
                }
                Log.i("CommunityID", "$id")
                navController.navigate("tchat_page/${communityName}/${communityAddress}/${id}")
            }
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
            Column(
                modifier = Modifier
                    .weight(2f / 4f)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    stringResource(R.string.id_community),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = communityId.replace("\n", ""),
                    onValueChange = { communityId = it },
                    placeholder = { Text(text = stringResource(R.string.communityAtIpServ)) },
                    textStyle = TextStyle(fontSize = 16.sp),
                    modifier = Modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("textEditCommu")
                )
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    stringResource(R.string.pseudo),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = pseudo.replace("\n", ""),
                    onValueChange = { if (it.length <= 35) pseudo = it },
                    placeholder = { Text(text = stringResource(R.string.default_pseudo)) },
                    textStyle = TextStyle(fontSize = 16.sp),
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
                        Log.d("FullPage", "Button pushed by $pseudo on $communityId")
                        enabled = false
                        if (isRegisterOk) {
                            communityId = communityId.trim()
                            val count = communityId.count { it == '@' }
                            if (count == 1) {
                                val list = communityId.split("@")
                                communityName = list[0].lowercase().trim()
                                communityAddress = list[1].lowercase().trim()
                                Log.i("Community", communityName)
                                Log.i("Address", communityAddress)
                                CoroutineScope(IO).launch {
                                    isConnectionOk =
                                        sendButtonConnexion(communityAddress, communityName)
                                    if (isConnectionOk) {
                                        Log.i("Pseudo", "Setting user pseudo to $pseudo")
                                        settingsRepository.setPseudo(pseudo)
                                        withContext(Main) {
                                            Utils.showInfoToast(
                                                R.string.commuConnection.toString(),
                                                context
                                            )
                                        }
                                    } else {
                                        withContext(Main) {
                                            Utils.showErrorToast(
                                                R.string.commuConnectionFailed.toString(),
                                                context
                                            )
                                        }
                                    }
                                }
                            } else {
                                Utils.showErrorToast(
                                    context.getString(R.string.community_id_must_have_one_At),
                                    context
                                )

                            }
                        }
                        enabled = true
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                ) {
                    val color = if (isRegisterOk) MaterialTheme.colors.secondaryVariant
                    else MaterialTheme.colors.primaryVariant
                    Text(stringResource(R.string.join_community), color = color)
                }
                Card(Modifier.weight(2f / 3f)) {}
            }
        }
    }

    private suspend fun sendButtonConnexion(
        address: String,
        community: String
    ): Boolean {
        if(address == "" || community == "") {
            Log.e("Connexion", "Address or community is empty")
            return false
        }

        var returnVal: Boolean
        withContext(IO) {
            returnVal = if (testHttp()) {
                httpSender.communityChecker(address, community)
            } else {
                dnsSender.findType(address)
                dnsSender.communityDetection(address, community)
            }
        }
        if (returnVal)
            Log.i("Connexion", "Success")
        else
            Log.i("Connexion", "Error")
        return returnVal
    }

    private fun testHttp(): Boolean {
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
        } catch (e : IOException) {
            return false
        }
    }
}