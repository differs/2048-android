package com.differs.game2048.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.differs.game2048.core.GameEngine
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dark = isDark(settings.theme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header row: title + scores.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2048", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Join the tiles, get to 2048!",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                ScoreBox("SCORE", state.board.score)
                Spacer(Modifier.width(8.dp))
                ScoreBox("BEST", state.bestScore)
            }

            Spacer(Modifier.size(16.dp))

            // Action row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::newGame) {
                    Icon(Icons.Filled.Refresh, contentDescription = "New game", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = viewModel::undo, enabled = state.canUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (state.canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenAchievements) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onOpenStats) {
                    Icon(Icons.Filled.BarChart, contentDescription = "Statistics", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.size(12.dp))

            Box {
                GameBoard(
                    board = state.board,
                    dark = dark,
                    onSwipe = viewModel::onSwipe
                )
                GameOverlay(
                    board = state.board,
                    onNewGame = viewModel::newGame,
                    onKeepPlaying = viewModel::keepPlaying
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                "Swipe to move tiles. Same numbers merge into one.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        // Achievement toast.
        val unlocked = state.recentlyUnlocked
        AnimatedVisibility(
            visible = unlocked != null,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            if (unlocked != null) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Achievement unlocked!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(unlocked.title, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
        LaunchedEffect(unlocked) {
            if (unlocked != null) {
                delay(2500)
                viewModel.clearUnlockedToast()
            }
        }
    }
}

@Composable
private fun ScoreBox(label: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = MaterialTheme.colorScheme.surfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("$value", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
