package com.differs.game2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.differs.game2048.core.BoardState
import com.differs.game2048.core.Direction
import com.differs.game2048.core.GameEngine
import com.differs.game2048.data.Achievement
import com.differs.game2048.data.AchievementContext
import com.differs.game2048.data.Achievements
import com.differs.game2048.data.GamePreferences
import com.differs.game2048.data.Settings
import com.differs.game2048.data.Stats
import com.differs.game2048.sound.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** UI-facing snapshot of a running game. */
data class GameUiState(
    val board: BoardState,
    val bestScore: Int = 0,
    val canUndo: Boolean = false,
    val recentlyUnlocked: Achievement? = null
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = GamePreferences(app)
    private val engine = GameEngine()
    private val sound = SoundManager(app)

    private var boardSize = 4
    private var undoStack = ArrayDeque<BoardState>()
    private var movesThisGame = 0
    private var statsRecorded = false

    private val _uiState = MutableStateFlow(GameUiState(engine.newGame(4)))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val settings: StateFlow<Settings> =
        prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    val stats: StateFlow<Stats> =
        prefs.stats.stateIn(viewModelScope, SharingStarted.Eagerly, Stats())

    val unlockedAchievements: StateFlow<Set<String>> =
        prefs.unlockedAchievements.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            val s = prefs.settings.first()
            boardSize = s.boardSize.coerceAtLeast(4) // Ensure minimum board size of 4
            val saved = prefs.savedGame(boardSize).first()
            val best = prefs.bestScore(boardSize).first()
            val board = saved ?: engine.newGame(boardSize)
            statsRecorded = saved?.over == true
            _uiState.value = GameUiState(board = board, bestScore = best, canUndo = false)
        }
    }

    fun onSwipe(direction: Direction) {
        val state = _uiState.value
        val board = state.board
        if (board.over || (board.won && !board.keepPlaying)) return

        val result = engine.move(board, direction)
        if (!result.moved) return

        undoStack.addLast(board)
        if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
        movesThisGame++

        val settingsNow = settings.value
        sound.playMove(settingsNow.soundEnabled)
        sound.vibrateTick(settingsNow.hapticsEnabled)
        if (result.gained > 0) sound.playMerge(settingsNow.soundEnabled)

        val spawned = engine.addRandomTile(result.board) ?: result.board
        val finalBoard = spawned.copy(over = !engine.movesAvailable(spawned))

        val newBest = maxOf(state.bestScore, finalBoard.score)
        _uiState.value = state.copy(board = finalBoard, bestScore = newBest, canUndo = undoStack.isNotEmpty())

        persist(finalBoard, newBest)
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        movesThisGame = (movesThisGame - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(board = previous, canUndo = undoStack.isNotEmpty())
        viewModelScope.launch { prefs.saveGame(previous) }
    }

    fun newGame() {
        undoStack.clear()
        movesThisGame = 0
        statsRecorded = false
        val board = engine.newGame(boardSize)
        _uiState.value = _uiState.value.copy(board = board, canUndo = false)
        viewModelScope.launch { prefs.saveGame(board) }
    }

    fun keepPlaying() {
        val b = _uiState.value.board
        _uiState.value = _uiState.value.copy(board = b.copy(keepPlaying = true))
    }

    fun changeBoardSize(size: Int) {
        if (size == boardSize) return
        // Ensure board size is valid (minimum 3, maximum 10)
        val validSize = size.coerceIn(3..10)
        boardSize = validSize
        viewModelScope.launch {
            prefs.setBoardSize(validSize)
            val saved = prefs.savedGame(validSize).first()
            val best = prefs.bestScore(validSize).first()
            undoStack.clear()
            movesThisGame = 0
            statsRecorded = false
            val board = saved ?: engine.newGame(validSize)
            _uiState.value = GameUiState(board = board, bestScore = best, canUndo = false)
        }
    }

    fun clearUnlockedToast() {
        _uiState.value = _uiState.value.copy(recentlyUnlocked = null)
    }

    fun setTheme(mode: com.differs.game2048.data.ThemeMode) {
        viewModelScope.launch { prefs.setTheme(mode) }
    }

    fun setSound(enabled: Boolean) {
        viewModelScope.launch { prefs.setSound(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { prefs.setHaptics(enabled) }
    }

    private fun persist(board: BoardState, best: Int) {
        viewModelScope.launch {
            prefs.saveGame(board)
            prefs.updateBestScore(board.size, best)
            val highest = board.tiles.maxOfOrNull { it.value } ?: 0
            if (board.over && !statsRecorded) {
                statsRecorded = true
                prefs.recordGameFinished(board.won, highest, movesThisGame, board.score)
            }
            evaluateAchievements(highest, board.score)
        }
    }

    private suspend fun evaluateAchievements(highestTile: Int, score: Int) {
        val ctx = AchievementContext(prefs.stats.first(), highestTile, score)
        val earned = Achievements.ALL.filter { it.predicate(ctx) }.map { it.id }.toSet()
        val newly = prefs.unlockAchievements(earned)
        val toShow = newly.firstNotNullOfOrNull { Achievements.byId(it) }
        if (toShow != null) _uiState.value = _uiState.value.copy(recentlyUnlocked = toShow)
    }

    override fun onCleared() {
        sound.release()
        super.onCleared()
    }

    private companion object {
        const val MAX_UNDO = 20
    }
}
