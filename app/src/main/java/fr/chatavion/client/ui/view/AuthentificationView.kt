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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.chatavion.client.R
import fr.chatavion.client.ui.theme.Gray
import fr.chatavion.client.ui.theme.White

class AuthentificationView {

    @Composable
    @SuppressLint("NotConstructor")
    fun AuthentificationView(navController: NavController) {
        var id by remember { mutableStateOf("") }
        var pseudo by remember { mutableStateOf("") }
        var isRegisterOk = false
        if (pseudo != "" && id != "")
            isRegisterOk = true
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f / 3f)
                    .height(Dp(500F)),
                painter = painterResource(id = R.drawable.chatavion_logo),
                contentDescription = "Chatavion logo"
            )
            Column(
                modifier = Modifier
                    .weight(1f / 3f)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Id de communauté",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = id,
                    onValueChange = { id = it },
                    placeholder  = { Text(text = "communauté@IPserveur") },
                    textStyle = TextStyle(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    "Pseudo",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                TextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    placeholder  = { Text(text = "chienjet") },
                    textStyle = TextStyle(fontSize = 16.sp)
                )
            }
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f / 3f)
            ) {
                Card(Modifier.weight(2f / 5f)) {}
                Button(
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(1f / 3f)
                        .width(200.dp),
                    onClick = {
                        Log.d("FullPage", "Button pushed by $pseudo on $id")
                        if (isRegisterOk)
                            navController.navigate("tchat_page")
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
}