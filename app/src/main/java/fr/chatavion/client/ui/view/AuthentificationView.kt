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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.chatavion.client.R
import fr.chatavion.client.connection.dns.DnsResolver
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class AuthentificationView {

    @Composable
    @SuppressLint("NotConstructor")
    fun AuthentificationView(navController: NavController) {
        val sender = DnsResolver()
        val context = LocalContext.current
        var id by remember { mutableStateOf("") }
        var pseudo by remember { mutableStateOf("") }
        var community by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var isRegisterOk by remember { mutableStateOf(false) }
        var isConnectionOk by remember { mutableStateOf(false) }
        if (pseudo != "" && id != "") {
            isRegisterOk = true
        }
        if (isConnectionOk) {
            navController.navigate("tchat_page/${pseudo}/${community}/${address}")
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
                    "Id de communauté",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = id.replace("\n", ""),
                    onValueChange = { id = it },
                    placeholder = { Text(text = "communauté@IPserveur") },
                    textStyle = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    "Pseudo",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = pseudo.replace("\n", ""),
                    onValueChange = { pseudo = it },
                    placeholder = { Text(text = "chienjet") },
                    textStyle = TextStyle(fontSize = 16.sp)
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
                        .width(200.dp),
                    onClick = {
                        Log.d("FullPage", "Button pushed by $pseudo on $id")
                        if (isRegisterOk) {
                            id = id.trim()
                            val count = id.count { it == '@' }
                            if (count == 1) {
                                val list = id.split("@")
                                community = list[0]
                                address = list[1]
                                Log.i("Community", community)
                                Log.i("Address", address)
                                CoroutineScope(IO).launch {
                                    isConnectionOk = sendButtonConnexion(sender, address, community)
                                }
                            } else {
                                showToast(
                                    "L'id de communauté doit contenir un \"@\" séparant le nom de communauté de l'adresse du serveur",
                                    context
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                ) {
                    val color = if (isRegisterOk) MaterialTheme.colors.secondaryVariant
                    else MaterialTheme.colors.primaryVariant
                    Text("Rejoindre", color = color)
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
        sender: DnsResolver,
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