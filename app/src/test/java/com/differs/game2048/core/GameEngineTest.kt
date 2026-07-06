package com.differs.game2048.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val engine = GameEngine(Random(42))

    private fun board(size: Int, vararg tiles: Triple<Int, Int, Int>): BoardState {
        var id = 1L
        return BoardState(
            size = size,
            tiles = tiles.map { (r, c, v) -> Tile(id = id++, value = v, row = r, col = c) },
            score = 0
        )
    }

    @Test
    fun newGameHasTwoTiles() {
        val b = engine.newGame(4)
        assertEquals(2, b.tiles.size)
        assertTrue(b.tiles.all { it.value == 2 || it.value == 4 })
    }

    @Test
    fun mergeEqualTilesLeft() {
        val b = board(4, Triple(0, 0, 2), Triple(0, 1, 2))
        val result = engine.move(b, Direction.LEFT)
        assertTrue(result.moved)
        assertEquals(4, result.gained)
        val merged = result.board.tileAt(0, 0)
        assertEquals(4, merged?.value)
        assertEquals(1, result.board.tiles.size)
    }

    @Test
    fun noTripleMergeInSingleMove() {
        val b = board(4, Triple(0, 0, 2), Triple(0, 1, 2), Triple(0, 2, 2))
        val result = engine.move(b, Direction.LEFT)
        // 2,2,2 -> 4,2 (only one merge per line per move)
        assertEquals(2, result.board.tiles.size)
        assertEquals(4, result.board.tileAt(0, 0)?.value)
        assertEquals(2, result.board.tileAt(0, 1)?.value)
    }

    @Test
    fun slideWithoutMerge() {
        val b = board(4, Triple(0, 3, 2))
        val result = engine.move(b, Direction.LEFT)
        assertTrue(result.moved)
        assertEquals(0, result.gained)
        assertEquals(2, result.board.tileAt(0, 0)?.value)
    }

    @Test
    fun noMoveWhenBlocked() {
        val b = board(2, Triple(0, 0, 2), Triple(0, 1, 4), Triple(1, 0, 4), Triple(1, 1, 2))
        val result = engine.move(b, Direction.LEFT)
        assertFalse(result.moved)
    }

    @Test
    fun reachingWinValueSetsWon() {
        val b = board(4, Triple(0, 0, 1024), Triple(0, 1, 1024))
        val result = engine.move(b, Direction.LEFT)
        assertTrue(result.board.won)
        assertEquals(2048, result.board.tileAt(0, 0)?.value)
    }

    @Test
    fun gameOverWhenFullAndNoMerges() {
        val b = board(2, Triple(0, 0, 2), Triple(0, 1, 4), Triple(1, 0, 4), Triple(1, 1, 2))
        assertFalse(engine.movesAvailable(b))
    }

    @Test
    fun movesAvailableWithEmptyCell() {
        val b = board(2, Triple(0, 0, 2))
        assertTrue(engine.movesAvailable(b))
    }
}
