package com.differs.game2048.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.differs.game2048.core.BoardState
import com.differs.game2048.core.Direction
import com.differs.game2048.core.Tile
import com.differs.game2048.ui.theme.tileColors
import kotlin.math.abs

@Composable
fun GameBoard(
    board: BoardState,
    dark: Boolean,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    require(board.size > 0) { "Board size must be positive, got: ${board.size}" }
    
    val size = board.size
    val cellGap = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(cellGap)
            .pointerInput(size) {
                var totalDrag = androidx.compose.ui.geometry.Offset.Zero
                detectDragGestures(
                    onDragStart = { totalDrag = androidx.compose.ui.geometry.Offset.Zero },
                    onDrag = { change, amount ->
                        change.consume()
                        totalDrag += amount
                    },
                    onDragEnd = {
                        val (dx, dy) = totalDrag
                        val threshold = 40f
                        if (abs(dx) > abs(dy)) {
                            if (dx > threshold) onSwipe(Direction.RIGHT)
                            else if (dx < -threshold) onSwipe(Direction.LEFT)
                        } else {
                            if (dy > threshold) onSwipe(Direction.DOWN)
                            else if (dy < -threshold) onSwipe(Direction.UP)
                        }
                    }
                )
            }
    ) {
        val boardPx = maxWidth
        val cellSize: Dp = (boardPx - cellGap * (size - 1)) / size

        // Static empty cells.
        for (r in 0 until size) {
            for (c in 0 until size) {
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .offset(
                            x = (cellSize + cellGap) * c,
                            y = (cellSize + cellGap) * r
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        // Animated tiles keyed by stable id.
        for (tile in board.tiles) {
            key(tile.id) {
                AnimatedTile(tile = tile, cellSize = cellSize, gap = cellGap, dark = dark)
            }
        }
    }
}

@Composable
private fun AnimatedTile(tile: Tile, cellSize: Dp, gap: Dp, dark: Boolean) {
    val targetX = (cellSize + gap) * tile.col
    val targetY = (cellSize + gap) * tile.row
    val animX by animateDpAsState(targetValue = targetX, animationSpec = spring(stiffness = 900f), label = "x")
    val animY by animateDpAsState(targetValue = targetY, animationSpec = spring(stiffness = 900f), label = "y")

    // Pop-in for new tiles, gentle bump for merges; 1f otherwise. The tile is
    // keyed by a stable id, so each new/merged tile gets a fresh Animatable that
    // springs from its initial scale to 1f.
    val initialScale = when {
        tile.isNew -> 0.3f
        tile.mergedFrom.isNotEmpty() -> 1.18f
        else -> 1f
    }
    val scale = remember { Animatable(initialScale) }
    LaunchedEffect(Unit) {
        if (initialScale != 1f) scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 700f))
    }
    val (bg, fg) = tileColors(tile.value, dark)

    Box(
        modifier = Modifier
            .size(cellSize)
            .offset(x = animX, y = animY)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val fontSize = with(density) {
            val base = cellSize.toPx() * 0.42f
            val shrink = when {
                tile.value >= 1024 -> 0.62f
                tile.value >= 128 -> 0.78f
                else -> 1f
            }
            (base * shrink).toSp()
        }
        Text(
            text = tile.value.toString(),
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
