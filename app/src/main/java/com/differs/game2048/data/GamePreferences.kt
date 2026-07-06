package com.differs.game2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.differs.game2048.core.BoardState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_2048")

/** App-wide theme choice. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val boardSize: Int = 4
)

data class Stats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val highestTile: Int = 0,
    val totalMoves: Int = 0,
    val totalScore: Int = 0
)

/** Central persistence: settings, best scores (per board size), stats and saved game. */
class GamePreferences(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val SOUND = booleanPreferencesKey("sound")
        val HAPTICS = booleanPreferencesKey("haptics")
        val BOARD_SIZE = intPreferencesKey("board_size")

        val GAMES_PLAYED = intPreferencesKey("games_played")
        val GAMES_WON = intPreferencesKey("games_won")
        val HIGHEST_TILE = intPreferencesKey("highest_tile")
        val TOTAL_MOVES = intPreferencesKey("total_moves")
        val TOTAL_SCORE = intPreferencesKey("total_score")

        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")

        fun bestScore(size: Int) = intPreferencesKey("best_score_$size")
        fun savedGame(size: Int) = stringPreferencesKey("saved_game_$size")
    }

    val unlockedAchievements: Flow<Set<String>> =
        context.dataStore.data.map { it[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet() }

    /** Adds [ids] to the unlocked set and returns the ids that were newly unlocked. */
    suspend fun unlockAchievements(ids: Set<String>): Set<String> {
        var newlyUnlocked: Set<String> = emptySet()
        context.dataStore.edit { p ->
            val current = p[Keys.UNLOCKED_ACHIEVEMENTS] ?: emptySet()
            newlyUnlocked = ids - current
            if (newlyUnlocked.isNotEmpty()) p[Keys.UNLOCKED_ACHIEVEMENTS] = current + newlyUnlocked
        }
        return newlyUnlocked
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            theme = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            boardSize = p[Keys.BOARD_SIZE] ?: 4
        )
    }

    val stats: Flow<Stats> = context.dataStore.data.map { p ->
        Stats(
            gamesPlayed = p[Keys.GAMES_PLAYED] ?: 0,
            gamesWon = p[Keys.GAMES_WON] ?: 0,
            highestTile = p[Keys.HIGHEST_TILE] ?: 0,
            totalMoves = p[Keys.TOTAL_MOVES] ?: 0,
            totalScore = p[Keys.TOTAL_SCORE] ?: 0
        )
    }

    fun bestScore(size: Int): Flow<Int> = context.dataStore.data.map { it[Keys.bestScore(size)] ?: 0 }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setSound(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUND] = enabled }
    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setBoardSize(size: Int) = context.dataStore.edit { it[Keys.BOARD_SIZE] = size }

    suspend fun updateBestScore(size: Int, score: Int) = context.dataStore.edit { p ->
        val current = p[Keys.bestScore(size)] ?: 0
        if (score > current) p[Keys.bestScore(size)] = score
    }

    suspend fun recordGameFinished(won: Boolean, highestTile: Int, moves: Int, score: Int) =
        context.dataStore.edit { p ->
            p[Keys.GAMES_PLAYED] = (p[Keys.GAMES_PLAYED] ?: 0) + 1
            if (won) p[Keys.GAMES_WON] = (p[Keys.GAMES_WON] ?: 0) + 1
            p[Keys.HIGHEST_TILE] = maxOf(p[Keys.HIGHEST_TILE] ?: 0, highestTile)
            p[Keys.TOTAL_MOVES] = (p[Keys.TOTAL_MOVES] ?: 0) + moves
            p[Keys.TOTAL_SCORE] = (p[Keys.TOTAL_SCORE] ?: 0) + score
        }

    suspend fun saveGame(board: BoardState) = context.dataStore.edit { p ->
        p[Keys.savedGame(board.size)] = json.encodeToString(BoardState.serializer(), board)
    }

    fun savedGame(size: Int): Flow<BoardState?> = context.dataStore.data.map { p ->
        p[Keys.savedGame(size)]?.let {
            runCatching { json.decodeFromString(BoardState.serializer(), it) }.getOrNull()
        }
    }

    suspend fun clearSavedGame(size: Int) = context.dataStore.edit { it.remove(Keys.savedGame(size)) }
}
