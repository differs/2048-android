package com.differs.game2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.differs.game2048.core.BoardState
import com.differs.game2048.data.ThemeMode

@Composable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Win / game-over overlay drawn on top of the board. */
@Composable
fun GameOverlay(
    board: BoardState,
    onNewGame: () -> Unit,
    onKeepPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showWin = board.won && !board.keepPlaying
    val showOver = board.over && !board.won
    if (!showWin && !showOver) return

    Column(
        modifier = modifier
            .matchOverlay()
            .background(
                if (showWin) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                else Color(0xEEEEE4DA)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (showWin) "You win!" else "Game over!",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (showWin) MaterialTheme.colorScheme.onPrimary else Color(0xFF776E65)
        )
        Spacer(Modifier.padding(8.dp))
        Row {
            if (showWin) {
                Button(
                    onClick = onKeepPlaying,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Keep going", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
            }
            Button(onClick = onNewGame) {
                Text("New game", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun Modifier.matchOverlay(): Modifier = this
    .fillMaxSize()
    .clip(RoundedCornerShape(12.dp))
