package com.differs.game2048.core

import kotlinx.serialization.Serializable

/** A single tile on the board. [id] is stable across a move so the UI can animate it. */
@Serializable
data class Tile(
    val id: Long,
    val value: Int,
    val row: Int,
    val col: Int,
    val mergedFrom: List<Long> = emptyList(),
    val isNew: Boolean = false
)

/** Direction of a swipe / key press. */
enum class Direction { UP, DOWN, LEFT, RIGHT }

/** Immutable snapshot of a board at a point in time. */
@Serializable
data class BoardState(
    val size: Int,
    val tiles: List<Tile>,
    val score: Int,
    val won: Boolean = false,
    val keepPlaying: Boolean = false,
    val over: Boolean = false
) {
    fun tileAt(row: Int, col: Int): Tile? = tiles.firstOrNull { it.row == row && it.col == col }
}

/** Result of applying a move to a board. */
data class MoveResult(
    val board: BoardState,
    val moved: Boolean,
    val gained: Int
)
