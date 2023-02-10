package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fr.chatavion.client.ui.theme.White
import fr.chatavion.client.util.Util


class TchatView {

    @Composable
    @SuppressLint("NotConstructor")
    fun TchatView(
        navController: NavController,
        pseudo: String,
        community: String,
        address: String
    ) {
        val messages = remember { mutableStateListOf<String>() }
        var msg by remember { mutableStateOf("") }
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .weight(1f / 3f)
                            .background(MaterialTheme.colors.background)
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            ),
                            onClick = {
                                Log.i("menu", "Menu pushed")
                            }) {
                            Icon(Icons.Filled.Menu, "menu")
                        }
                    }
                    Column(Modifier.weight(1f / 3f)) {
                        Text(
                            text = "Communauté",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                    Row(Modifier.weight(1f / 3f)) {
                        Column(Modifier.weight(1f / 2f)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                ),
                                onClick = {
                                    Log.i("expandMore", "ExpandMore pushed")
                                }) {
                                Icon(Icons.Filled.ExpandMore, "expandMore")
                            }
                        }
                        Column(Modifier.weight(1f / 2f)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                ),
                                onClick = {
                                    Log.i("wifi", "Wifi pushed")
                                }) {
                                Icon(Icons.Filled.Wifi, "wifi")
                            }
                        }
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                    backgroundColor = MaterialTheme.colors.background
                ) {
                    Box(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .fillMaxWidth(0.8f)
                    ) {
                        TextField(
                            value = msg,
                            onValueChange = { msg = it },
                            label = {Text(text = "Message text...")},
                            textStyle = TextStyle(fontSize = 16.sp),
                            //placeholder = { Text(text = "Message text...") },
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                        )
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        onClick = {
                            if (msg != "") {
                                messages.add(msg)
                                Log.i("Send", "Msg sent : $msg")
                                msg = ""
                            }
                        }) {
                        Icon(Icons.Filled.Send, "send")
                    }
                }
            }
        ) { innerTag ->
            Divider(color = White, thickness = 1.dp)
            Column(Modifier.padding(innerTag))
            {
                LazyColumn(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.background)
                        .padding(PaddingValues(horizontal = 25.dp, vertical = 15.dp)),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(messages) { message ->
                        DisplayCenterText(message, pseudo)
                    }
                }
            }
        }
    }


    @Composable
    fun DisplayCenterText(text: String, pseudo: String) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        )
        {
            Text(
                text = "$pseudo",
                color = MaterialTheme.colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()

            )
            Spacer(modifier = Modifier.size(3.dp))
            Text(
                text ="$text",
                color = MaterialTheme.colors.onPrimary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
    @Composable
    fun DisplayCenterText1(text: String, pseudo: String) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp, 5.dp)
                .height(35.dp)
        )
        {
            Text(
                text = "$pseudo: $text",
                color = MaterialTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .height(20.dp)
            )
        }
    }
}

//
//fun TchatView(
//    navController: NavController,
//    pseudo: String,
//    community: String,
//    address: String
//) {
//    val messages = remember { mutableStateListOf<String>() }
//    var msg by remember { mutableStateOf("") }
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                backgroundColor = MaterialTheme.colors.background,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(
//                    Modifier
//                        .weight(1f / 3f)
//                        .background(MaterialTheme.colors.background)
//                ) {
//                    Button(
//                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
//                        elevation = ButtonDefaults.elevation(
//                            defaultElevation = 0.dp,
//                            pressedElevation = 0.dp,
//                            disabledElevation = 0.dp
//                        ),
//                        onClick = {
//                            Log.i("menu", "Menu pushed")
//                        }) {
//                        Icon(Icons.Filled.Menu, "menu")
//                    }
//                }
//                Column(Modifier.weight(1f / 3f)) {
//                    Text(
//                        text = "Communauté",
//                        modifier = Modifier.align(Alignment.CenterHorizontally),
//                        color = MaterialTheme.colors.onPrimary
//                    )
//                }
//                Row(Modifier.weight(1f / 3f)) {
//                    Column(Modifier.weight(1f / 2f)) {
//                        Button(
//                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
//                            elevation = ButtonDefaults.elevation(
//                                defaultElevation = 0.dp,
//                                pressedElevation = 0.dp,
//                                disabledElevation = 0.dp
//                            ),
//                            onClick = {
//                                Log.i("expandMore", "ExpandMore pushed")
//                            }) {
//                            Icon(Icons.Filled.ExpandMore, "expandMore")
//                        }
//                    }
//                    Column(Modifier.weight(1f / 2f)) {
//                        Button(
//                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
//                            elevation = ButtonDefaults.elevation(
//                                defaultElevation = 0.dp,
//                                pressedElevation = 0.dp,
//                                disabledElevation = 0.dp
//                            ),
//                            onClick = {
//                                Log.i("wifi", "Wifi pushed")
//                            }) {
//                            Icon(Icons.Filled.Wifi, "wifi")
//                        }
//                    }
//                }
//            }
//        },
//        bottomBar = {
//            BottomAppBar(
//                cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
//                backgroundColor = MaterialTheme.colors.background
//            ) {
//                Box(
//                    Modifier
//                        .align(Alignment.CenterVertically)
//                        .fillMaxWidth(0.8f)
//                ) {
//                    TextField(
//                        value = msg,
//                        onValueChange = { msg = it },
//                        label = {},
//                        textStyle = TextStyle(fontSize = 16.sp),
//                        placeholder = { Text(text = "Message text...") },
//                        colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
//                    )
//                }
//                Button(
//                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
//                    elevation = ButtonDefaults.elevation(
//                        defaultElevation = 0.dp,
//                        pressedElevation = 0.dp,
//                        disabledElevation = 0.dp
//                    ),
//                    onClick = {
//                        if (msg != "") {
//                            messages.add(msg)
//                            Log.i("Send", "Msg sent : $msg")
//                            msg = ""
//                        }
//                    }) {
//                    Icon(Icons.Filled.Send, "send")
//                }
//            }
//        } innerTag ->
