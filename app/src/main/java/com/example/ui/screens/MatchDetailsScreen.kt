package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Match
import com.example.ui.PredictionViewModel
import com.example.ui.theme.*
import androidx.compose.ui.text.font.FontStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    viewModel: PredictionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maybeMatch by viewModel.selectedMatch.collectAsState()
    val match = maybeMatch ?: return

    val teamsList = viewModel.teamsList
    val teamA = teamsList.find { it.id == match.teamAId }
    val teamB = teamsList.find { it.id == match.teamBId }

    val aiMatchInsight by viewModel.aiMatchInsight.collectAsState()
    val isGeneratingInsight by viewModel.isGeneratingMatchInsight.collectAsState()

    var scoreAState by remember(match.id) { mutableStateOf(match.predictedScoreA ?: 0) }
    var scoreBState by remember(match.id) { mutableStateOf(match.predictedScoreB ?: 0) }
    var winnerState by remember(match.id) { mutableStateOf(match.predictedWinnerId ?: teamA?.id ?: "DRAW") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(match.stage.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp, fontStyle = FontStyle.Italic) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StadiumSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Predict button to save the score
            Surface(
                color = StadiumDark,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val finalSelectedWinner = when {
                        scoreAState > scoreBState -> teamA?.id
                        scoreBState > scoreAState -> teamB?.id
                        else -> {
                            if (match.isKnockout) winnerState else "DRAW"
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.predictMatch(match.id, scoreAState, scoreBState, finalSelectedWinner)
                            onBack()
                        },
                        enabled = teamA != null && teamB != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_predictions_button")
                    ) {
                        Text("Apply Prediction", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = StadiumDark
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Team vs card with score adjustment controllers
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PREDICT FINAL SCORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TrophyGold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Team A Info
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (teamA != null) {
                                    FlagIcon(emoji = teamA.flagEmoji, size = 56.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(teamA.id, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                    Text(teamA.name, fontSize = 11.sp, color = MutedSlate, textAlign = TextAlign.Center, maxLines = 1)
                                } else {
                                    Text("TBD", fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }

                            // Score selectors
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                // Team A score decrement/increment
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { scoreAState = (scoreAState + 1).coerceAtMost(9) },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumDark),
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, StadiumBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$scoreAState",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    IconButton(
                                        onClick = { scoreAState = (scoreAState - 1).coerceAtLeast(0) },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumDark),
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, StadiumBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }

                                Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MutedSlate, modifier = Modifier.padding(horizontal = 8.dp))

                                // Team B score decrement/increment
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { scoreBState = (scoreBState + 1).coerceAtMost(9) },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumDark),
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, StadiumBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$scoreBState",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    IconButton(
                                        onClick = { scoreBState = (scoreBState - 1).coerceAtLeast(0) },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumDark),
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, StadiumBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            // Team B Info
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (teamB != null) {
                                    FlagIcon(emoji = teamB.flagEmoji, size = 56.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(teamB.id, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                    Text(teamB.name, fontSize = 11.sp, color = MutedSlate, textAlign = TextAlign.Center, maxLines = 1)
                                } else {
                                    Text("TBD", fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }

                        // Knockout Golden penalty option selection if DRAW
                        if (match.isKnockout && scoreAState == scoreBState && teamA != null && teamB != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "WHO QUALIFIES (PENALTY PENNANT)?",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = TrophyGold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { winnerState = teamA.id },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (winnerState == teamA.id) PitchGreen else StadiumDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (winnerState == teamA.id) PitchGreen else StadiumBorder)
                                ) {
                                    Text(teamA.id, color = if (winnerState == teamA.id) Color.Black else Color.White, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { winnerState = teamB.id },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (winnerState == teamB.id) PitchGreen else StadiumDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (winnerState == teamB.id) PitchGreen else StadiumBorder)
                                ) {
                                    Text(teamB.id, color = if (winnerState == teamB.id) Color.Black else Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Simulated expected graphics
            item {
                SectionHeader(title = "Statistical Expected Rates")
                Card(
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Simulated xG A: ${match.xGA}", fontSize = 11.sp, color = PitchGreen, fontWeight = FontWeight.Black)
                            Text("Simulated xG B: ${match.xGB}", fontSize = 11.sp, color = PitchBlue, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            val totalXG = (match.xGA + match.xGB).coerceAtLeast(0.1f)
                            val shareA = match.xGA / totalXG
                            Box(
                                modifier = Modifier
                                    .weight(shareA)
                                    .fillMaxHeight()
                                    .background(PitchGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f - shareA)
                                    .fillMaxHeight()
                                    .background(PitchBlue)
                            )
                        }
                    }
                }
            }

            // Tactical Pitch Canvas diagram
            item {
                SectionHeader(title = "Tactical Formations Layout")
                if (teamA != null && teamB != null) {
                    TacticalPitchVisualizer(
                        homeTeamEmoji = teamA.flagEmoji,
                        awayTeamEmoji = teamB.flagEmoji,
                        homeName = teamA.id,
                        awayName = teamB.id
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(StadiumSurface)
                            .border(1.dp, StadiumBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pitch alignment pending dynamic bracket outcomes", color = MutedSlate, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // AI analysis box
            item {
                SectionHeader(title = "AI Tactical Analyst Report")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(StadiumSurface)
                        .border(1.5.dp, PitchGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    if (isGeneratingInsight) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = PitchGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Generating custom tactical report...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        if (aiMatchInsight.isNotEmpty()) {
                            Text(
                                text = aiMatchInsight,
                                color = IceWhite.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        } else {
                            Text(
                                text = "Select match and predict your score to prompt the AI analyst report automatically.",
                                color = MutedSlate,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
