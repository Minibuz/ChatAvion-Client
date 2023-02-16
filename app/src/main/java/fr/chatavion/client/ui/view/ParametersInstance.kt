package fr.chatavion.client.ui.view

import android.util.Log
import fr.chatavion.client.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserParameter(
    onClose: () -> Unit
) {
    var pseudo by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                    ) {
                        IconButton(
                            onClick = {
                                Log.i("userParameter", "User parameter")
                                onClose()
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.2f)
                                .align(Alignment.CenterVertically),
                            content = {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    "arrowBack",
                                    tint = MaterialTheme.colors.onBackground
                                )
                            }
                        )
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = "Pseudo",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            Divider(
                thickness = 2.dp,
                color = MaterialTheme.colors.onBackground
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight(2 / 4f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = "Pseudo actuel*"
                        )
                        TextField(
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.onBackground
                            ),
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.background),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp)),
                            value = pseudo,
                            onValueChange = {
                                pseudo = it
                            })
                        Text(
                            color = MaterialTheme.colors.onBackground,
                            text = stringResource(id = R.string.explication_pseudo_community)
                        )
                    }
                }
            }
        }
    }
}