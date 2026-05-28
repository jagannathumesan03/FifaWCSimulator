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
        val thirdPlaceSlotGroups = linkedMapOf(
            74 to listOf("A", "B", "C", "D", "F"),
            77 to listOf("C", "D", "F", "G", "H"),
            79 to listOf("C", "E", "F", "H", "I"),
            80 to listOf("E", "H", "I", "J", "K"),
            81 to listOf("B", "E", "F", "I", "J"),
            82 to listOf("A", "E", "H", "I", "J"),
            85 to listOf("E", "F", "G", "I", "J"),
            87 to listOf("D", "E", "I", "J", "L")
        )
        val thirdPlaceAssignments = assignThirdPlaceTeams(bestThirds, thirdPlaceSlotGroups)

        fun winner(group: String) = winners[group] ?: "TBD_${group}_1"
        fun runner(group: String) = runnersUp[group] ?: "TBD_${group}_2"
        fun thirdPlace(matchId: Int): String {
            val eligibleGroups = thirdPlaceSlotGroups.getValue(matchId)
            return thirdPlaceAssignments[matchId] ?: "TBD_3RD_${eligibleGroups.joinToString("")}"
        }

        data class RoundOf32Slot(
            val id: Int,
            val teamAId: String?,
            val teamBId: String?,
            val label: String
        )

        val r32Slots = listOf(
            RoundOf32Slot(73, runner("A"), runner("B"), "Match 73 - Runner-up Group A vs Runner-up Group B"),
            RoundOf32Slot(74, winner("E"), thirdPlace(74), "Match 74 - Winner Group E vs 3rd Group A/B/C/D/F"),
            RoundOf32Slot(75, winner("F"), runner("C"), "Match 75 - Winner Group F vs Runner-up Group C"),
            RoundOf32Slot(76, winner("C"), runner("F"), "Match 76 - Winner Group C vs Runner-up Group F"),
            RoundOf32Slot(77, winner("I"), thirdPlace(77), "Match 77 - Winner Group I vs 3rd Group C/D/F/G/H"),
            RoundOf32Slot(78, runner("E"), runner("I"), "Match 78 - Runner-up Group E vs Runner-up Group I"),
            RoundOf32Slot(79, winner("A"), thirdPlace(79), "Match 79 - Winner Group A vs 3rd Group C/E/F/H/I"),
            RoundOf32Slot(80, winner("L"), thirdPlace(80), "Match 80 - Winner Group L vs 3rd Group E/H/I/J/K"),
            RoundOf32Slot(81, winner("D"), thirdPlace(81), "Match 81 - Winner Group D vs 3rd Group B/E/F/I/J"),
            RoundOf32Slot(82, winner("G"), thirdPlace(82), "Match 82 - Winner Group G vs 3rd Group A/E/H/I/J"),
            RoundOf32Slot(83, runner("K"), runner("L"), "Match 83 - Runner-up Group K vs Runner-up Group L"),
            RoundOf32Slot(84, winner("H"), runner("J"), "Match 84 - Winner Group H vs Runner-up Group J"),
            RoundOf32Slot(85, winner("B"), thirdPlace(85), "Match 85 - Winner Group B vs 3rd Group E/F/G/I/J"),
            RoundOf32Slot(86, winner("J"), runner("H"), "Match 86 - Winner Group J vs Runner-up Group H"),
            RoundOf32Slot(87, winner("K"), thirdPlace(87), "Match 87 - Winner Group K vs 3rd Group D/E/I/J/L"),
            RoundOf32Slot(88, runner("D"), runner("G"), "Match 88 - Runner-up Group D vs Runner-up Group G")
        )

        // Build Round of 32 Matches
        val r32Matches = mutableListOf<Match>()
        for (slot in r32Slots) {
            val pred = predictionsMap[slot.id]
            
            r32Matches.add(
                Match(
                    id = slot.id,
                    teamAId = slot.teamAId,
                    teamBId = slot.teamBId,
                    group = null,
                    isKnockout = true,
                    stage = "R32",
                    date = slot.label,
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

    private fun assignThirdPlaceTeams(
        bestThirds: List<String>,
        slotGroupsByMatch: Map<Int, List<String>>
    ): Map<Int, String> {
        val groupByTeamId = teams.associate { it.id to it.group }
        val matchIds = slotGroupsByMatch.keys.toList()

        fun search(index: Int, usedTeamIds: Set<String>): Map<Int, String>? {
            if (index == matchIds.size) return emptyMap()

            val matchId = matchIds[index]
            val eligibleGroups = slotGroupsByMatch.getValue(matchId)
            val candidates = bestThirds.filter { teamId ->
                teamId !in usedTeamIds && groupByTeamId[teamId] in eligibleGroups
            }

            for (candidate in candidates) {
                val rest = search(index + 1, usedTeamIds + candidate)
                if (rest != null) {
                    return rest + (matchId to candidate)
                }
            }

            return null
        }

        return search(index = 0, usedTeamIds = emptySet()) ?: run {
            val assignments = mutableMapOf<Int, String>()
            val usedTeamIds = mutableSetOf<String>()
            for ((matchId, eligibleGroups) in slotGroupsByMatch) {
                val candidate = bestThirds.firstOrNull { teamId ->
                    teamId !in usedTeamIds && groupByTeamId[teamId] in eligibleGroups
                }
                if (candidate != null) {
                    assignments[matchId] = candidate
                    usedTeamIds.add(candidate)
                }
            }
            assignments
        }
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
