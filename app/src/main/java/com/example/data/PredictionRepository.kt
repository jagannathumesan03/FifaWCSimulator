package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class PredictionRepository(
    private val predictionDao: MatchPredictionDao,
    private val savedBracketDao: SavedBracketDao
) {
    // Expose all 48 teams
    val teams: List<Team> = TournamentSeeds.teamsList

    // Expose dynamic raw predictions flow from Room
    val predictionsFlow: Flow<List<MatchPredictionEntity>> = predictionDao.getAllPredictionsFlow()

    // Clear and reset all db predictions
    suspend fun resetAllPredictions() {
        predictionDao.clearAllPredictions()
    }

    // Save individual prediction
    suspend fun savePrediction(matchId: Int, scoreA: Int?, scoreB: Int?, winnerId: String?, isCompleted: Boolean) {
        predictionDao.insertPrediction(
            MatchPredictionEntity(
                matchId = matchId,
                scoreA = scoreA,
                scoreB = scoreB,
                winnerId = winnerId,
                isCompleted = isCompleted
            )
        )
    }

    // Helper to merge database predictions with structural match nodes
    fun getFullMatchesFlow(): Flow<List<Match>> {
        val initialGroupMatches = TournamentSeeds.getInitialGroupMatches()
        
        return predictionsFlow.map { entities ->
            val predictionsMap = entities.associateBy { it.matchId }
            val mergedMatches = mutableListOf<Match>()

            // 1. Add Group-Stage Matches
            for (gm in initialGroupMatches) {
                val pred = predictionsMap[gm.id]
                val copy = gm.copy(
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
                mergedMatches.add(copy)
            }

            // 2. Compute Standings to resolve Round of 32 nodes
            val (winners, runnersUp, bestThirds) = computeQualifiers(mergedMatches)
            val knockoutMatches = generateKnockoutFixtures(winners, runnersUp, bestThirds, predictionsMap)
            mergedMatches.addAll(knockoutMatches)

            mergedMatches
        }
    }

    // Single-shot merged matches resolver for compute loops
    suspend fun getFullMatchesSnapshot(): List<Match> {
        val initialGroupMatches = TournamentSeeds.getInitialGroupMatches()
        val entities = predictionDao.getAllPredictions()
        val predictionsMap = entities.associateBy { it.matchId }
        val mergedMatches = mutableListOf<Match>()

        for (gm in initialGroupMatches) {
            val pred = predictionsMap[gm.id]
            mergedMatches.add(
                gm.copy(
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
            )
        }

        val (winners, runnersUp, bestThirds) = computeQualifiers(mergedMatches)
        val knockoutMatches = generateKnockoutFixtures(winners, runnersUp, bestThirds, predictionsMap)
        mergedMatches.addAll(knockoutMatches)

        return mergedMatches
    }

    // Dynamic Standings Calculator
    fun computeStandings(matches: List<Match>): Map<String, List<GroupStandingsEntry>> {
        val groupMap = mutableMapOf<String, MutableMap<String, GroupStandingsEntry>>()

        // Initialize entries
        for (team in teams) {
            val grp = team.group
            val grpTeams = groupMap.getOrPut(grp) { mutableMapOf() }
            grpTeams[team.id] = GroupStandingsEntry(
                teamId = team.id,
                teamName = team.name,
                flagEmoji = team.flagEmoji
            )
        }

        // Apply completed predictions
        val groupMatches = matches.filter { !it.isKnockout }
        for (m in groupMatches) {
            if (m.teamAId == null || m.teamBId == null) continue
            val scoreA = m.predictedScoreA
            val scoreB = m.predictedScoreB
            if (scoreA != null && scoreB != null) {
                val entryA = groupMap[m.group]?.get(m.teamAId)
                val entryB = groupMap[m.group]?.get(m.teamBId)

                if (entryA != null && entryB != null) {
                    entryA.played++
                    entryB.played++

                    entryA.goalsFor += scoreA
                    entryA.goalsAgainst += scoreB
                    entryA.goalsDifference = entryA.goalsFor - entryA.goalsAgainst

                    entryB.goalsFor += scoreB
                    entryB.goalsAgainst += scoreA
                    entryB.goalsDifference = entryB.goalsFor - entryB.goalsAgainst

                    when {
                        scoreA > scoreB -> {
                            entryA.won++
                            entryA.points += 3
                            entryB.lost++
                        }
                        scoreB > scoreA -> {
                            entryB.won++
                            entryB.points += 3
                            entryA.lost++
                        }
                        else -> {
                            entryA.drawn++
                            entryA.points += 1
                            entryB.drawn++
                            entryB.points += 1
                        }
                    }
                }
            }
        }

        // Sort groups based on tie-breakers
        val sortedGroupMap = mutableMapOf<String, List<GroupStandingsEntry>>()
        for ((grp, entriesMap) in groupMap) {
            val sortedList = entriesMap.values.sortedWith(
                compareByDescending<GroupStandingsEntry> { it.points }
                    .thenByDescending { it.goalsDifference }
                    .thenByDescending { it.goalsFor }
                    .thenBy { getTeamRank(it.teamId) } // Tie-breaker fallback
            )
            // Add standings placements and qualification indicator values
            sortedList.forEachIndexed { idx, entry ->
                entry.ranking = idx + 1
                entry.qualificationChances = when (idx) {
                    0 -> 100
                    1 -> 100
                    2 -> 66 // Best 3rd places
                    else -> 0
                }
            }
            sortedGroupMap[grp] = sortedList
        }

        return sortedGroupMap
    }

    private fun getTeamRank(teamId: String): Int {
        return teams.firstOrNull { it.id == teamId }?.rank ?: 100
    }

    // Compute Qualifiers (Top 2 + Best 8 Third Places)
    fun computeQualifiers(matches: List<Match>): Triple<Map<String, String>, Map<String, String>, List<String>> {
        val standings = computeStandings(matches)
        val winners = mutableMapOf<String, String>() // Group -> TeamId
        val runnersUp = mutableMapOf<String, String>() // Group -> TeamId
        val thirdPlaces = mutableListOf<GroupStandingsEntry>()

        for ((grp, list) in standings) {
            if (list.size >= 3) {
                winners[grp] = list[0].teamId
                runnersUp[grp] = list[1].teamId
                thirdPlaces.add(list[2])
            }
        }

        // Sort third places to find the top 8
        val sortedThirds = thirdPlaces.sortedWith(
            compareByDescending<GroupStandingsEntry> { it.points }
                .thenByDescending { it.goalsDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { getTeamRank(it.teamId) }
        ).take(8).map { it.teamId }

        return Triple(winners, runnersUp, sortedThirds)
    }

    // Auto-Simulate logic using rating delta math
    fun simulateScoresForMatch(teamAId: String, teamBId: String): Pair<Int, Int> {
        val teamA = teams.first { it.id == teamAId }
        val teamB = teams.first { it.id == teamBId }

        val diff = teamA.rating - teamB.rating
        val rand = Random.nextDouble()

        // Base lambda values
        var baseA = 1.3
        var baseB = 1.3

        if (diff > 0) {
            baseA += (diff * 0.08)
            baseB -= (diff * 0.04)
        } else {
            baseA += (diff * 0.04)
            baseB -= (diff * 0.08)
        }

        baseA = baseA.coerceAtLeast(0.2)
        baseB = baseB.coerceAtLeast(0.2)

        // Poisson pseudo random
        val goalsA = poissonRandom(baseA)
        val goalsB = poissonRandom(baseB)

        return Pair(goalsA, goalsB)
    }

    private fun poissonRandom(lambda: Double): Int {
        val limit = Math.exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= Random.nextDouble()
        } while (p > limit)
        return k - 1
    }

    // Generate Knockout Fixtures dynamically (73 to 104)
    private fun generateKnockoutFixtures(
        winners: Map<String, String>,
        runnersUp: Map<String, String>,
        bestThirds: List<String>,
        predictionsMap: Map<Int, MatchPredictionEntity>
    ): List<Match> {
        val knockoutList = mutableListOf<Match>()

        // 1. Round of 32 (16 Matches: IDs 73 to 88)
        // Set fixed mapping of slots
        val r32SlotPairs = listOf(
            // Win A vs Best 3rd-1
            Triple("A", "winner", bestThirds.getOrNull(0) ?: "TBD_3RD_1"),
            Triple("B", "winner", runnersUp["C"] ?: "TBD_C_2"),
            Triple("C", "winner", bestThirds.getOrNull(1) ?: "TBD_3RD_2"),
            Triple("D", "winner", runnersUp["E"] ?: "TBD_E_2"),
            Triple("E", "winner", bestThirds.getOrNull(2) ?: "TBD_3RD_3"),
            Triple("F", "winner", runnersUp["G"] ?: "TBD_G_2"),
            Triple("G", "winner", bestThirds.getOrNull(3) ?: "TBD_3RD_4"),
            Triple("H", "winner", runnersUp["I"] ?: "TBD_I_2"),
            Triple("I", "winner", bestThirds.getOrNull(4) ?: "TBD_3RD_5"),
            Triple("J", "winner", runnersUp["K"] ?: "TBD_K_2"),
            Triple("K", "winner", bestThirds.getOrNull(5) ?: "TBD_3RD_6"),
            Triple("L", "winner", runnersUp["A"] ?: "TBD_A_2"),
            // 3rd place mappings
            Triple("A", "runner", runnersUp["B"] ?: "TBD_B_2"),
            Triple("C", "runner", runnersUp["D"] ?: "TBD_D_2"),
            Triple("E", "runner", runnersUp["F"] ?: "TBD_F_2"),
            Triple("G", "runner", runnersUp["H"] ?: "TBD_H_2")
        )

        // Build Round of 32 Matches
        val r32Matches = mutableListOf<Match>()
        for (i in 0 until 16) {
            val matchId = 73 + i
            val pair = r32SlotPairs[i]
            
            val teamA = if (pair.second == "winner") winners[pair.first] else runnersUp[pair.first]
            val teamB = pair.third

            val pred = predictionsMap[matchId]
            
            r32Matches.add(
                Match(
                    id = matchId,
                    teamAId = teamA,
                    teamBId = teamB,
                    group = null,
                    isKnockout = true,
                    stage = "R32",
                    date = "Round of 32 — Game ${i + 1}",
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
            )
        }
        knockoutList.addAll(r32Matches)

        // 2. Round of 16 (8 Matches: IDs 89 to 96)
        // R16-1: Winner 73 vs Winner 74, etc.
        val r16Matches = mutableListOf<Match>()
        for (i in 0 until 8) {
            val matchId = 89 + i
            val parentAId = 73 + (i * 2)
            val parentBId = 74 + (i * 2)

            val teamA = r32Matches.first { it.id == parentAId }.getWinnerId()
            val teamB = r32Matches.first { it.id == parentBId }.getWinnerId()

            val pred = predictionsMap[matchId]

            r16Matches.add(
                Match(
                    id = matchId,
                    teamAId = teamA,
                    teamBId = teamB,
                    group = null,
                    isKnockout = true,
                    stage = "R16",
                    date = "Round of 16 — Game ${i + 1}",
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
            )
        }
        knockoutList.addAll(r16Matches)

        // 3. Quarter-Finals (4 Matches: IDs 97 to 100)
        val qfMatches = mutableListOf<Match>()
        for (i in 0 until 4) {
            val matchId = 97 + i
            val parentAId = 89 + (i * 2)
            val parentBId = 90 + (i * 2)

            val teamA = r16Matches.first { it.id == parentAId }.getWinnerId()
            val teamB = r16Matches.first { it.id == parentBId }.getWinnerId()

            val pred = predictionsMap[matchId]

            qfMatches.add(
                Match(
                    id = matchId,
                    teamAId = teamA,
                    teamBId = teamB,
                    group = null,
                    isKnockout = true,
                    stage = "QF",
                    date = "Quarter-Final ${i + 1}",
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
            )
        }
        knockoutList.addAll(qfMatches)

        // 4. Semi-Finals (2 Matches: IDs 101 to 102)
        val sfMatches = mutableListOf<Match>()
        for (i in 0 until 2) {
            val matchId = 101 + i
            val parentAId = 97 + (i * 2)
            val parentBId = 98 + (i * 2)

            val teamA = qfMatches.first { it.id == parentAId }.getWinnerId()
            val teamB = qfMatches.first { it.id == parentBId }.getWinnerId()

            val pred = predictionsMap[matchId]

            sfMatches.add(
                Match(
                    id = matchId,
                    teamAId = teamA,
                    teamBId = teamB,
                    group = null,
                    isKnockout = true,
                    stage = "SF",
                    date = "Semi-Final ${i + 1}",
                    predictedScoreA = pred?.scoreA,
                    predictedScoreB = pred?.scoreB,
                    predictedWinnerId = pred?.winnerId,
                    isPredictionCompleted = pred?.isCompleted ?: false
                )
            )
        }
        knockoutList.addAll(sfMatches)

        // 5. Third Place Play-off (Match ID 103)
        val sf1Loser = getLoser(sfMatches.firstOrNull { it.id == 101 })
        val sf2Loser = getLoser(sfMatches.firstOrNull { it.id == 102 })
        val predThird = predictionsMap[103]
        val thirdPlaceMatch = Match(
            id = 103,
            teamAId = sf1Loser,
            teamBId = sf2Loser,
            group = null,
            isKnockout = true,
            stage = "THIRD",
            date = "Third Place Play-off",
            predictedScoreA = predThird?.scoreA,
            predictedScoreB = predThird?.scoreB,
            predictedWinnerId = predThird?.winnerId,
            isPredictionCompleted = predThird?.isCompleted ?: false
        )
        knockoutList.add(thirdPlaceMatch)

        // 6. World Cup Final (Match ID 104)
        val sf1Winner = sfMatches.firstOrNull { it.id == 101 }?.getWinnerId()
        val sf2Winner = sfMatches.firstOrNull { it.id == 102 }?.getWinnerId()
        val predFinal = predictionsMap[104]
        val finalMatch = Match(
            id = 104,
            teamAId = sf1Winner,
            teamBId = sf2Winner,
            group = null,
            isKnockout = true,
            stage = "FINAL",
            date = "World Cup Final — New York New Jersey",
            predictedScoreA = predFinal?.scoreA,
            predictedScoreB = predFinal?.scoreB,
            predictedWinnerId = predFinal?.winnerId,
            isPredictionCompleted = predFinal?.isCompleted ?: false
        )
        knockoutList.add(finalMatch)

        return knockoutList
    }

    private fun getLoser(m: Match?): String? {
        if (m == null) return null
        val w = m.getWinnerId() ?: return null
        return if (w == m.teamAId) m.teamBId else m.teamAId
    }

    // Save predictions payload as a bracket template (using Room)
    suspend fun saveCompleteBracket(name: String, predictions: List<MatchPredictionEntity>) {
        val jsonPayload = predictions.joinToString(",") {
            "${it.matchId}:${it.scoreA ?: "x"}:${it.scoreB ?: "x"}:${it.winnerId ?: ""}:${if (it.isCompleted) 1 else 0}"
        }
        savedBracketDao.insertBracket(
            SavedBracketEntity(
                name = name,
                predictionsJson = jsonPayload
            )
        )
    }

    // Load predictions from saved bracket
    suspend fun loadSavedBracket(bracketId: String) {
        val bracket = savedBracketDao.getBracketById(bracketId) ?: return
        predictionDao.clearAllPredictions()
        
        val entities = mutableListOf<MatchPredictionEntity>()
        val tokens = bracket.predictionsJson.split(",")
        for (t in tokens) {
            val parts = t.split(":")
            if (parts.size == 5) {
                val matchId = parts[0].toIntOrNull() ?: continue
                val scoreA = parts[1].toIntOrNull()
                val scoreB = parts[2].toIntOrNull()
                val winnerId = parts[3].ifEmpty { null }
                val isCompleted = parts[4] == "1"

                entities.add(MatchPredictionEntity(matchId, scoreA, scoreB, winnerId, isCompleted))
            }
        }
        predictionDao.insertAll(entities)
    }

    // List of saved brackets
    fun getSavedBracketsFlow(): Flow<List<SavedBracketEntity>> {
        return savedBracketDao.getAllBracketsFlow()
    }

    // Delete a bracket config
    suspend fun deleteSavedBracket(id: String) {
        savedBracketDao.deleteBracketById(id)
    }
}
