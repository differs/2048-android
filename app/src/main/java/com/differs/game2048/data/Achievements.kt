package com.differs.game2048.data

/** A single achievement definition plus a predicate over game stats/results. */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val predicate: (AchievementContext) -> Boolean
)

/** Snapshot of the values an achievement can be unlocked against. */
data class AchievementContext(
    val stats: Stats,
    val highestTileThisGame: Int,
    val scoreThisGame: Int
)

object Achievements {
    val ALL: List<Achievement> = listOf(
        Achievement("first_game", "Getting Started", "Play your first game") { it.stats.gamesPlayed >= 1 },
        Achievement("reach_256", "Warming Up", "Reach the 256 tile") { it.highestTileThisGame >= 256 },
        Achievement("reach_512", "On a Roll", "Reach the 512 tile") { it.highestTileThisGame >= 512 },
        Achievement("reach_1024", "Almost There", "Reach the 1024 tile") { it.highestTileThisGame >= 1024 },
        Achievement("reach_2048", "2048!", "Reach the 2048 tile") { it.highestTileThisGame >= 2048 },
        Achievement("reach_4096", "Overachiever", "Reach the 4096 tile") { it.highestTileThisGame >= 4096 },
        Achievement("score_10k", "High Scorer", "Score 10,000 in a single game") { it.scoreThisGame >= 10_000 },
        Achievement("win_5", "Serial Winner", "Win 5 games") { it.stats.gamesWon >= 5 },
        Achievement("veteran", "Veteran", "Play 50 games") { it.stats.gamesPlayed >= 50 }
    )

    fun byId(id: String): Achievement? = ALL.firstOrNull { it.id == id }
}
