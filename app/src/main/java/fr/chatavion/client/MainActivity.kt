package fr.chatavion.client

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.runtime.setValue
import fr.chatavion.client.util.ThemeHelper

/**
 * The MainActivity class sets the content view to a composed UI using Jetpack Compose
 */
class MainActivity : ComponentActivity() {
    private var darkModeEnabled by mutableStateOf(true)
    /**
     * Sets the activity's content view to a Composed UI and instantiates the ViewModel.
     * @param savedInstanceState the saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            darkModeEnabled = ThemeHelper.isDarkThemeEnabled(context)
            ChatavionTheme(darkThemeEnabled = darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    // Instantiation of ViewModels
                    communityViewModel = viewModel(
                        factory = CommunityViewModel.CommunityFactory(
                            context.applicationContext as Application
                        )
                    )
                    NavigationBasicsApp{darkModeEnabled=it}
                }
            }
        }
    }

}

lateinit var communityViewModel: CommunityViewModel

/**
 * A composable function that defines the basic navigation structure of the app.
 *
 * @param navController The NavController that manages app navigation.
 */
@Composable
fun NavigationBasicsApp(enableDarkTheme: (Boolean) -> Unit) {
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
                tchatView.DrawerAppComponent(
                    navController,
                    community,
                    address,
                    id.toInt(),
                    idLast.toInt(),
                    enableDarkTheme
                )
            }
        }
    }
}
