package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.db.entity.Community
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class AuthentificationView {
    private val sender = DnsResolver()

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
            val id = community.communityId
            communityViewModel.insert(community)
            navController.navigate("tchat_page/${communityName}/${communityAddress}/${id}")
            isConnectionOk = false
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
                                communityName = list[0]
                                communityAddress = list[1]
                                Log.i("Community", communityName)
                                Log.i("Address", communityAddress)
                                CoroutineScope(IO).launch {
                                    isConnectionOk =
                                        sendButtonConnexion(communityAddress, communityName)
                                    if (isConnectionOk) {
                                        Log.i("Pseudo", "Setting user pseudo to $pseudo")
                                        settingsRepository.setPseudo(pseudo)
                                    }
                                    enabled = true
                                }
                            } else {
                                enabled = true
                                showToast(
                                    "Il doit y avoir un et un seul @",
                                    context
                                )
                            }
                        }
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

    private fun showToast(text: String, context: Context) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private suspend fun sendButtonConnexion(
        address: String,
        community: String
    ): Boolean {
        var returnVal: Boolean
        withContext(IO) {
            sender.findType(address)
            returnVal = sender.communityDetection(community, address)
        }
        if (returnVal)
            Log.i("Connexion", "Success")
        else
            Log.i("Connexion", "Error")
        return returnVal
    }
}