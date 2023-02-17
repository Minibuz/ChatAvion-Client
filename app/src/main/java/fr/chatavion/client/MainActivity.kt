package fr.chatavion.client

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.chatavion.client.db.viewModel.MessageViewModel
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
                    val context = LocalContext.current

                    messageViewModel = viewModel(
                        factory =
                        MessageViewModel.MessageFactory(
                            context.applicationContext as Application
                        )
                    )



                    NavigationBasicsApp()
                }
            }
        }
    }
}

lateinit var messageViewModel: MessageViewModel

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
            val pseudo = backStackEntry.arguments?.getString("pseudo")
            val community = backStackEntry.arguments?.getString("community")
            val address = backStackEntry.arguments?.getString("address")
            if (pseudo != null && community != null && address != null) {
                tchatView.DrawerAppComponent(pseudo, community, address)
            }
        }
    }
}
