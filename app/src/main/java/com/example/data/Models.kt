package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class Team(
    val id: String,          // Short code e.g., "ARG", "BRA"
    val name: String,
    val group: String,       // "A" to "L"
    val confederation: String, // "CONMEBOL", "UEFA", "CAF", "CONCACAF", "AFC", "OFC"
    val rating: Int,         // FIFA rating or Power Index (60-95)
    val flagEmoji: String,
    val rank: Int,           // FIFA World Ranking
    val keyPlayer: String,
    val tacticalStyle: String,
    val darkHorse: Boolean = false
)

data class Match(
    val id: Int,
    val teamAId: String?,
    val teamBId: String?,
    val group: String?,      // "A"-"L" for group stage, null for knockout
    val isKnockout: Boolean,
    val stage: String,        // "GROUP", "R32", "R16", "QF", "SF", "THIRD", "FINAL"
    val date: String,
    val xGA: Float = 0.0f,
    val xGB: Float = 0.0f,
    // Dynamic/User Predicted Data (can be loaded from SQLite)
    var predictedScoreA: Int? = null,
    var predictedScoreB: Int? = null,
    var predictedWinnerId: String? = null, // "ARG", "BRA" or "DRAW"
    val isPredictionCompleted: Boolean = false
) {
    // Helper to get selected winner ID
    fun getWinnerId(): String? {
        if (predictedScoreA == null || predictedScoreB == null) return null
        return when {
            predictedScoreA!! > predictedScoreB!! -> teamAId
            predictedScoreB!! > predictedScoreA!! -> teamBId
            else -> {
                // For knockout, we require a winner. Default to teamA or user selected winner
                predictedWinnerId ?: teamAId
            }
        }
    }
}

data class GroupStandingsEntry(
    val teamId: String,
    val teamName: String,
    val flagEmoji: String,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var goalsDifference: Int = 0,
    var points: Int = 0,
    var ranking: Int = 1,
    var qualificationChances: Int = 100 // Dynamic percentage
)

@Entity(tableName = "match_predictions")
data class MatchPredictionEntity(
    @PrimaryKey val matchId: Int,
    val scoreA: Int?,
    val scoreB: Int?,
    val winnerId: String?,
    val isCompleted: Boolean
)

@Entity(tableName = "saved_brackets")
data class SavedBracketEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val predictionsJson: String // Serialized payload of predictions
)
