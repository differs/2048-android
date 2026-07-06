package com.differs.game2048.core

import kotlin.random.Random

/**
 * Pure, deterministic (given an RNG) 2048 game engine. Holds no Android
 * dependencies so it can be unit-tested on the JVM.
 */
class GameEngine(private val random: Random = Random.Default) {

    private var nextId = 1L
    private fun newId(): Long = nextId++

    /** Create a fresh board of [size]x[size] with [startTiles] random tiles. */
    fun newGame(size: Int, startTiles: Int = 2): BoardState {
        var board = BoardState(size = size, tiles = emptyList(), score = 0)
        repeat(startTiles) { board = addRandomTile(board) ?: board }
        return board
    }

    /** Add a random tile (2 with 90% probability, else 4). Returns null if the board is full. */
    fun addRandomTile(board: BoardState): BoardState? {
        val empty = emptyCells(board)
        if (empty.isEmpty()) return null
        val (row, col) = empty[random.nextInt(empty.size)]
        val value = if (random.nextDouble() < 0.9) 2 else 4
        val tile = Tile(id = newId(), value = value, row = row, col = col, isNew = true)
        return board.copy(tiles = board.tiles + tile)
    }

    private fun emptyCells(board: BoardState): List<Pair<Int, Int>> {
        val occupied = board.tiles.map { it.row to it.col }.toHashSet()
        val cells = ArrayList<Pair<Int, Int>>()
        for (r in 0 until board.size) for (c in 0 until board.size) {
            if ((r to c) !in occupied) cells.add(r to c)
        }
        return cells
    }

    /**
     * Apply a [direction] move. Returns a [MoveResult] whose board has cleared
     * `isNew`/`mergedFrom` flags reset appropriately and merges resolved. Does
     * NOT spawn a new tile — the caller decides when to spawn (only if moved).
     */
    fun move(board: BoardState, direction: Direction): MoveResult {
        val size = board.size
        // grid[r][c] -> value, plus id tracking
        val grid = Array(size) { r -> Array<Tile?>(size) { c -> board.tileAt(r, c) } }

        var moved = false
        var gained = 0
        val resultTiles = ArrayList<Tile>()

        // Build traversal lines depending on direction.
        val lines: List<List<Pair<Int, Int>>> = buildLines(size, direction)

        for (line in lines) {
            // Collect tiles along the line in travel order (towards the head).
            val ordered = line.mapNotNull { (r, c) -> grid[r][c] }
            var writeIndex = 0
            var idx = 0
            while (idx < ordered.size) {
                val current = ordered[idx]
                val next = ordered.getOrNull(idx + 1)
                val (targetRow, targetCol) = line[writeIndex]
                if (next != null && next.value == current.value) {
                    // Merge current + next.
                    val mergedValue = current.value * 2
                    gained += mergedValue
                    resultTiles.add(
                        Tile(
                            id = newId(),
                            value = mergedValue,
                            row = targetRow,
                            col = targetCol,
                            mergedFrom = listOf(current.id, next.id)
                        )
                    )
                    moved = true
                    idx += 2
                } else {
                    if (current.row != targetRow || current.col != targetCol) moved = true
                    resultTiles.add(current.copy(row = targetRow, col = targetCol, isNew = false, mergedFrom = emptyList()))
                    idx += 1
                }
                writeIndex++
            }
        }

        val won = board.won || resultTiles.any { it.value >= WIN_VALUE }
        var newBoard = board.copy(
            tiles = resultTiles,
            score = board.score + gained,
            won = won
        )
        newBoard = newBoard.copy(over = !movesAvailable(newBoard))
        return MoveResult(newBoard, moved, gained)
    }

    /** The list of cell coordinates for each traversal line, ordered from head (destination) outwards. */
    private fun buildLines(size: Int, direction: Direction): List<List<Pair<Int, Int>>> {
        val lines = ArrayList<List<Pair<Int, Int>>>()
        when (direction) {
            Direction.LEFT -> for (r in 0 until size)
                lines.add((0 until size).map { c -> r to c })
            Direction.RIGHT -> for (r in 0 until size)
                lines.add((size - 1 downTo 0).map { c -> r to c })
            Direction.UP -> for (c in 0 until size)
                lines.add((0 until size).map { r -> r to c })
            Direction.DOWN -> for (c in 0 until size)
                lines.add((size - 1 downTo 0).map { r -> r to c })
        }
        return lines
    }

    /** True if any move is still possible (empty cell or adjacent equal tiles). */
    fun movesAvailable(board: BoardState): Boolean {
        if (emptyCells(board).isNotEmpty()) return true
        val size = board.size
        for (r in 0 until size) for (c in 0 until size) {
            val v = board.tileAt(r, c)?.value ?: continue
            if (c + 1 < size && board.tileAt(r, c + 1)?.value == v) return true
            if (r + 1 < size && board.tileAt(r + 1, c)?.value == v) return true
        }
        return false
    }

    companion object {
        const val WIN_VALUE = 2048
    }
}
