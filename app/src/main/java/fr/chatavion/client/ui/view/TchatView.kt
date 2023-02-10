package fr.chatavion.client.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController

class TchatView {

    @Composable
    @SuppressLint("NotConstructor")
    fun TchatView(
        navController: NavController,
        pseudo: String,
        community: String,
        address: String
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Hello $pseudo !\nWelcome to the serveur $community@$address")
            Button(onClick = { navController.navigate("auth_page") }) {
                Icon(Icons.Filled.ArrowBack, "Go back home")
            }
        }
    }
}