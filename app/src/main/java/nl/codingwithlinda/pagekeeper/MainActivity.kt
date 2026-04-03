package nl.codingwithlinda.pagekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.navigation.MainNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PageKeeperTheme {
                AppNavigation(
                    onImportBook = { /* TODO: open file picker */ },
                    onLibrary = { /* TODO: navigate to library */ },
                    onFavorites = { /* TODO: navigate to favorites */ },
                    onFinished = { /* TODO: navigate to finished */ }
                ) {
                    MainNav()
                }
            }
        }
    }
}
