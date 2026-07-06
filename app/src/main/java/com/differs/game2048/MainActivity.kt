package com.differs.game2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.differs.game2048.ui.AchievementsScreen
import com.differs.game2048.ui.GameScreen
import com.differs.game2048.ui.GameViewModel
import com.differs.game2048.ui.SettingsScreen
import com.differs.game2048.ui.StatsScreen
import com.differs.game2048.ui.isDark
import com.differs.game2048.ui.theme.Game2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

private object Routes {
    const val GAME = "game"
    const val SETTINGS = "settings"
    const val STATS = "stats"
    const val ACHIEVEMENTS = "achievements"
}

@Composable
private fun App() {
    val viewModel: GameViewModel = viewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Game2048Theme(darkTheme = isDark(settings.theme)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Routes.GAME) {
                composable(Routes.GAME) {
                    GameScreen(
                        viewModel = viewModel,
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenStats = { navController.navigate(Routes.STATS) },
                        onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(Routes.STATS) {
                    StatsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(Routes.ACHIEVEMENTS) {
                    AchievementsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
