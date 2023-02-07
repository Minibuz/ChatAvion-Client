package fr.chatavion.client

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import fr.chatavion.client.ui.theme.ChatavionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatavionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainPage()
                }
            }
        }
    }
}

@Composable
fun MainPage() {
    var id by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }

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
                label = {},
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
                label = {},
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
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f / 3f)
                    .width(200.dp),
                onClick = {
                    Log.d("FullPage", "Button pushed by $pseudo on $id")
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Text("Rejoindre", color = MaterialTheme.colors.onSecondary)
            }
            Card(Modifier.weight(2f / 3f)) {}
        }
    }
}