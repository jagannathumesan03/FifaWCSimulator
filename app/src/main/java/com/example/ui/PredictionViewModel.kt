package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TournamentDatabase.getDatabase(application)
    private val repository = PredictionRepository(
        db.matchPredictionDao(),
        db.savedBracketDao()
    )

    // Current lists / details
    val teamsList = repository.teams

    // Matches Flow containing full structural groups and knockout fixtures
    val matches: StateFlow<List<Match>> = repository.getFullMatchesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedBrackets: StateFlow<List<SavedBracketEntity>> = repository.getSavedBracketsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived states
    val groupStandings: StateFlow<Map<String, List<GroupStandingsEntry>>> = matches
        .map { repository.computeStandings(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Selected nodes
    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    private val _selectedTeam = MutableStateFlow<Team?>(null)
    val selectedTeam: StateFlow<Team?> = _selectedTeam.asStateFlow()

    // AI insights tracking
    private val _aiMatchInsight = MutableStateFlow<String>("")
    val aiMatchInsight: StateFlow<String> = _aiMatchInsight.asStateFlow()

    private val _isGeneratingMatchInsight = MutableStateFlow(false)
    val isGeneratingMatchInsight: StateFlow<Boolean> = _isGeneratingMatchInsight.asStateFlow()

    private val _tournamentAnalysis = MutableStateFlow<String>("")
    val tournamentAnalysis: StateFlow<String> = _tournamentAnalysis.asStateFlow()

    private val _isGeneratingTournamentAnalysis = MutableStateFlow(false)
    val isGeneratingTournamentAnalysis: StateFlow<Boolean> = _isGeneratingTournamentAnalysis.asStateFlow()

    init {
        // Handle pre-selection
        _selectedTeam.value = teamsList.firstOrNull()
    }

    // Predictions updater
    fun predictMatch(matchId: Int, scoreA: Int?, scoreB: Int?, winnerId: String?) {
        viewModelScope.launch {
            repository.savePrediction(
                matchId = matchId,
                scoreA = scoreA,
                scoreB = scoreB,
                winnerId = winnerId,
                isCompleted = (scoreA != null && scoreB != null)
            )
            // If we predicted on the selected match, update active state container as well
            val currentSelected = _selectedMatch.value
            if (currentSelected?.id == matchId) {
                _selectedMatch.value = currentSelected.copy(
                    predictedScoreA = scoreA,
                    predictedScoreB = scoreB,
                    predictedWinnerId = winnerId,
                    isPredictionCompleted = (scoreA != null && scoreB != null)
                )
            }
        }
    }

    fun selectMatch(match: Match) {
        _selectedMatch.value = match
        _aiMatchInsight.value = "" // clear previous
        fetchMatchInsight(match)
    }

    fun selectTeam(teamId: String) {
        _selectedTeam.value = teamsList.find { it.id == teamId }
    }

    // Reset current actions
    fun resetGroup(groupCode: String) {
        viewModelScope.launch {
            val currentGroupMatches = matches.value.filter { it.group == groupCode }
            for (m in currentGroupMatches) {
                repository.savePrediction(m.id, null, null, null, false)
            }
        }
    }

    fun resetAllPredictions() {
        viewModelScope.launch {
            repository.resetAllPredictions()
            _selectedMatch.value = null
            _aiMatchInsight.value = ""
            _tournamentAnalysis.value = ""
        }
    }

    // Auto simulate / randomize
    fun randomizeGroup(groupCode: String) {
        viewModelScope.launch {
            val grpMatches = matches.value.filter { it.group == groupCode }
            for (m in grpMatches) {
                val (scoreA, scoreB) = repository.simulateScoresForMatch(m.teamAId!!, m.teamBId!!)
                val winner = when {
                    scoreA > scoreB -> m.teamAId
                    scoreB > scoreA -> m.teamBId
                    else -> "DRAW"
                }
                repository.savePrediction(m.id, scoreA, scoreB, winner, true)
            }
        }
    }

    fun autoCompleteGroup(groupCode: String) {
        viewModelScope.launch {
            val grpMatches = matches.value.filter { it.group == groupCode }
            for (m in grpMatches) {
                if (m.predictedScoreA == null) {
                    val (scoreA, scoreB) = repository.simulateScoresForMatch(m.teamAId!!, m.teamBId!!)
                    val winner = when {
                        scoreA > scoreB -> m.teamAId
                        scoreB > scoreA -> m.teamBId
                        else -> "DRAW"
                    }
                    repository.savePrediction(m.id, scoreA, scoreB, winner, true)
                }
            }
        }
    }

    fun randomizeAllGroups() {
        viewModelScope.launch {
            val grpMatches = matches.value.filter { !it.isKnockout }
            for (m in grpMatches) {
                val (scoreA, scoreB) = repository.simulateScoresForMatch(m.teamAId!!, m.teamBId!!)
                val winner = when {
                    scoreA > scoreB -> m.teamAId
                    scoreB > scoreA -> m.teamBId
                    else -> "DRAW"
                }
                repository.savePrediction(m.id, scoreA, scoreB, winner, true)
            }
        }
    }

    fun autoCompleteAllRemaining() {
        viewModelScope.launch {
            val allMatches = matches.value
            for (m in allMatches) {
                if (m.predictedScoreA == null && m.teamAId != null && m.teamBId != null && m.teamAId != "TBD" && !m.teamAId.startsWith("TBD_") && m.teamBId != "TBD" && !m.teamBId.startsWith("TBD_")) {
                    val (scoreA, scoreB) = repository.simulateScoresForMatch(m.teamAId, m.teamBId)
                    val winner = when {
                        scoreA > scoreB -> m.teamAId
                        scoreB > scoreA -> m.teamBId
                        else -> if (m.isKnockout) m.teamAId else "DRAW"
                    }
                    repository.savePrediction(m.id, scoreA, scoreB, winner, true)
                }
            }
        }
    }

    // Dynamic stats calculators for dashboard visualizers
    fun getUpsetPercentage(): Float {
        val completed = matches.value.filter { it.predictedScoreA != null && it.teamAId != null && it.teamBId != null }
        if (completed.isEmpty()) return 0f

        var upsets = 0
        for (m in completed) {
            val teamA = teamsList.first { it.id == m.teamAId }
            val teamB = teamsList.first { it.id == m.teamBId }
            
            val winner = m.getWinnerId()
            if (winner != null && winner != "DRAW") {
                val diff = teamA.rating - teamB.rating
                if (diff >= 5 && winner == teamB.id) {
                    upsets++
                } else if (diff <= -5 && winner == teamA.id) {
                    upsets++
                }
            }
        }
        return (upsets.toFloat() / completed.size) * 100f
    }

    fun getTotalPredictedGoals(): Int {
        return matches.value.sumOf { (it.predictedScoreA ?: 0) + (it.predictedScoreB ?: 0) }
    }

    fun getMostSelectedChampion(): Team? {
        val finalMatch = matches.value.find { it.id == 104 } ?: return null
        val championId = finalMatch.getWinnerId() ?: return null
        return teamsList.find { it.id == championId }
    }

    fun getToughestBracketPath(): String {
        // Analyzes average rank or standard of winners in R32 to QF
        val completedKnockout = matches.value.filter { it.isKnockout && it.predictedScoreA != null }
        if (completedKnockout.isEmpty()) return "Fill in predictions to see path analysis"
        
        // Find path for champion
        val champ = getMostSelectedChampion() ?: return "Determine a champion to see their pathway difficulty"
        val pathMatches = matches.value.filter { m ->
            m.isKnockout && (m.teamAId == champ.id || m.teamBId == champ.id)
        }
        
        val avgRating = pathMatches.mapNotNull { m ->
            val oppId = if (m.teamAId == champ.id) m.teamBId else m.teamAId
            teamsList.find { it.id == oppId }?.rating
        }.average()

        return when {
            avgRating >= 84 -> "Brutal (Avg Opponent Rating: ${String.format("%.1f", avgRating)} — Elite clashes predicted)"
            avgRating >= 78 -> "Moderate (Avg Opponent Rating: ${String.format("%.1f", avgRating)} — Highly balanced obstacles)"
            avgRating > 0 -> "Favorable (Avg Opponent Rating: ${String.format("%.1f", avgRating)} — Comfortable pacing for ${champ.name})"
            else -> "Easy path predicted"
        }
    }

    // AI prediction queries
    private fun fetchMatchInsight(match: Match) {
        if (match.teamAId == null || match.teamBId == null) {
            _aiMatchInsight.value = "Fixture teams are still pending qualification outcomes."
            return
        }
        viewModelScope.launch {
            _isGeneratingMatchInsight.value = true
            val teamA = teamsList.first { it.id == match.teamAId }
            val teamB = teamsList.first { it.id == match.teamBId }

            val prompt = """
                Provide a short 3-sentence visual analysis for World Cup 2026 match:
                Team A: ${teamA.name} (FIFA Rating: ${teamA.rating}, style: ${teamA.tacticalStyle})
                Team B: ${teamB.name} (FIFA Rating: ${teamB.rating}, style: ${teamB.tacticalStyle})
                
                Detail:
                1. Likely tactical flow of the game based on style.
                2. Key player influence focus on: ${teamA.keyPlayer} vs ${teamB.keyPlayer}.
                3. High risk elements or upset potentials (e.g. set pieces or counters).
            """.trimIndent()

            _aiMatchInsight.value = GeminiService.fetchAnalysis(prompt)
            _isGeneratingMatchInsight.value = false
        }
    }

    fun fetchTournamentDifficultyReport() {
        viewModelScope.launch {
            _isGeneratingTournamentAnalysis.value = true
            
            val champ = getMostSelectedChampion()?.name ?: "Undetermined"
            val totalGoals = getTotalPredictedGoals()
            val upsetRate = String.format("%.1f", getUpsetPercentage())
            val pathDifficulty = getToughestBracketPath()

            val prompt = """
                Conduct a comprehensive tournament overview briefing of the user's World Cup predictions layout:
                - Selected Champion: $champ
                - Simulated Total Goals: $totalGoals goals across predicted games
                - Calculated Upset Percentage: $upsetRate%
                - Representative Path to Victory: $pathDifficulty

                Weave these data values into an exciting premium analyst report. Group it into 3 clear blocks:
                1. "CHAMPION ANALYSIS": Does the champion have a tactical blueprint or a difficult historic hurdle?
                2. "BRACKET DYNAMICS & PATHS": Mention any early powerhouse head-to-heads or blocks of death.
                3. "TOURNAMENT ANOMALIES": Mention whether the predicted $upsetRate% upset rate indicates an predictable linear bracket or absolute chaotic underdog dominance.
            """.trimIndent()

            _tournamentAnalysis.value = GeminiService.fetchAnalysis(
                prompt = prompt,
                systemPrompt = "You are a lead FIFA World Cup director of sports intelligence. Output the final brief structured cleanly into readable paragraphs with header keys."
            )
            _isGeneratingTournamentAnalysis.value = false
        }
    }

    // Saved Brackets CRUD
    fun saveBracketAs(name: String) {
        viewModelScope.launch {
            val allPreds = matches.value.map {
                MatchPredictionEntity(it.id, it.predictedScoreA, it.predictedScoreB, it.predictedWinnerId, it.isPredictionCompleted)
            }
            repository.saveCompleteBracket(name, allPreds)
        }
    }

    fun loadBracket(id: String) {
        viewModelScope.launch {
            repository.loadSavedBracket(id)
        }
    }

    fun deleteBracket(id: String) {
        viewModelScope.launch {
            repository.deleteSavedBracket(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PredictionViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
