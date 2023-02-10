package fr.chatavion.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.chatavion.client.ui.theme.ChatavionTheme
import fr.chatavion.client.ui.view.AuthentificationView
import fr.chatavion.client.ui.view.TchatView

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
                    NavigationBasicsApp()
                }
            }
        }
    }
}

@Composable
fun NavigationBasicsApp() {
    val navController = rememberNavController()

    val authView = AuthentificationView()
    val tchatView = TchatView()

    NavHost(navController = navController, startDestination = "auth_page") {
        composable("auth_page") {
            authView.AuthentificationView(navController)
        }

        composable("tchat_page/{pseudo}/{community}/{address}") { backStackEntry ->
//            val sender = backStackEntry.arguments?.getBundle("sender")
            val pseudo = backStackEntry.arguments?.getString("pseudo")
            val community = backStackEntry.arguments?.getString("community")
            val address = backStackEntry.arguments?.getString("address")
            if (pseudo != null && community != null && address != null) {
                tchatView.TchatView(navController, pseudo, community, address)
            }
        }
    }
}
