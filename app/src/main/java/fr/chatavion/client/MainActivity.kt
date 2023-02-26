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
import fr.chatavion.client.db.viewModel.CommunityViewModel
import fr.chatavion.client.ui.theme.ChatavionTheme
import fr.chatavion.client.ui.view.AuthentificationView
import fr.chatavion.client.ui.view.TchatView


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatavionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val context = LocalContext.current

                    // Instantiation of ViewModels
                    communityViewModel = viewModel(
                        factory = CommunityViewModel.CommunityFactory(
                            context.applicationContext as Application
                        )
                    )

                    NavigationBasicsApp()
                }
            }
        }
    }
}

lateinit var communityViewModel: CommunityViewModel

@Composable
fun NavigationBasicsApp() {
    val navController = rememberNavController()

    val authView = AuthentificationView()
    val tchatView = TchatView()

    NavHost(navController = navController, startDestination = "auth_page") {
        composable("auth_page") {
            authView.AuthentificationView(navController)
        }

        composable("tchat_page/{community}/{address}/{id}/{idLast}") { backStackEntry ->
            val community = backStackEntry.arguments?.getString("community")
            val address = backStackEntry.arguments?.getString("address")
            val id = backStackEntry.arguments?.getString("id")
            val idLast = backStackEntry.arguments?.getString("idLast")
            if (community != null && address != null && id != null && idLast != null) {
                tchatView.DrawerAppComponent(navController, community, address, id.toInt(), idLast.toInt())
            }
        }
    }
}
