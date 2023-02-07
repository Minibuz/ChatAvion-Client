package fr.chatavion.client

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chatavion.client.ui.theme.Blue
import fr.chatavion.client.ui.theme.ChatavionTheme
import fr.chatavion.client.ui.theme.Gray
import fr.chatavion.client.ui.theme.Red

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
                    FullPage()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatavionTheme {
        Greeting("Android")
    }
}

@SuppressLint("NotConstructor")
@Composable
fun MainPageView() {
    val words = remember { mutableStateListOf<String>() }
    var text by remember { mutableStateOf("") }

    words.add("Hello World!")
    words.add("Hello Chatavion!")
    words.add("Hello ChienJet!")
    Scaffold(topBar = {
        TopAppBar(
            Modifier
                .fillMaxWidth()
                .fillMaxWidth()
        ) {
            DisplayCenterText("Chatavion")
        }
    }) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Box(
                Modifier
                    .weight(1f / 3f)
            ) {
                val imageId = R.drawable.chatavion_logo
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dp(500F)),
                    painter = painterResource(id = imageId),
                    contentDescription = "Chatavion logo"
                )
            }
            Column(
                Modifier
                    .weight(1f / 3f)
                    .fillMaxWidth()
            ) {

            }
            Row(
                modifier = Modifier
                    .weight(1f / 3f)
            ) {

            }
        }
    }
}

@Composable
fun FullPage() {
    var id by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
//        Image(imageResource(R.drawable.chatavion_logo), Modifier.fillMaxSize().gravity(Alignment.CenterHorizontally))
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
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1f / 3f),
            onClick = {
                Log.d("FullPage", "Button pushed by $pseudo on $id")
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
        ) {
            Text("Rejoindre", color = MaterialTheme.colors.onSecondary)
        }
    }
}

@Composable
fun DisplayCenterText(text: String) {
    Text(
        text = text,
//        color = Color.White,
        fontSize = 20.sp,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .wrapContentHeight(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun inputText(): String {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        value = text,
        onValueChange = { text = it },
        trailingIcon = {
            Icon(
                Icons.Default.Clear,
                contentDescription = "clear text",
                modifier = Modifier
                    .clickable {
                        text = TextFieldValue("")
                    }
            )
        }
    )
    return text.text
}