package fr.chatavion.client.ui.view

import android.util.Log
import fr.chatavion.client.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun UserParameter(

) {
    var pseudo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .align(Alignment.CenterHorizontally)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                ) {
                    IconButton(
                        onClick = {
                                  Log.i("userParameter", "User parameter")
                                  },
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .align(Alignment.CenterVertically),
                        content = {
                            Icon(Icons.Filled.ArrowBack,
                                "arrowBack")
                        }
                    )
                    Text(
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
        )
        Box(
            modifier = Modifier
                .fillMaxHeight(2/4f)
                .align(Alignment.CenterHorizontally)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "Pseudo actuel*"
                )
                TextField(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp)),
                    value = pseudo,
                    onValueChange = {
                        pseudo = it
                    })
                Text(
                    text = stringResource(id = R.string.explication_pseudo_community)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewParameter() {
    UserParameter()
}