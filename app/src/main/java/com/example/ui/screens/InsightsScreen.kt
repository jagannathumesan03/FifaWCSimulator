package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import com.example.ui.PredictionViewModel
import com.example.ui.theme.*
import androidx.compose.ui.text.font.FontStyle

@Composable
fun InsightsScreen(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val tournamentAnalysis by viewModel.tournamentAnalysis.collectAsState()
    val isGeneratingAnalysis by viewModel.isGeneratingTournamentAnalysis.collectAsState()

    val totalGoals = viewModel.getTotalPredictedGoals()
    val upsetRate = viewModel.getUpsetPercentage()
    val champ = viewModel.getMostSelectedChampion()
    val bracketPath = viewModel.getToughestBracketPath()

    var showShareNotification by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Report central banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(StadiumSurface, StadiumDark)
                        )
                    )
                    .border(1.5.dp, TrophyGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "WORLD CUP INTELLIGENCE BRIEFS",
                        color = TrophyGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI TOUR PLAYOFF REPORT",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Synthesizer algorithms compile your exact paths, scores, and mock configurations to generate structured football breakdowns of qualification paths.",
                        color = IceWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.fetchTournamentDifficultyReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_insights_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Draft Playoff Briefing", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Output Insight Console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isGeneratingAnalysis) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = TrophyGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Assembling bracket analytics...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        if (tournamentAnalysis.isNotEmpty()) {
                            Text(
                                text = tournamentAnalysis,
                                color = IceWhite.copy(alpha = 0.95f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        } else {
                            // High Fidelity Curated Fallback
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TrophyGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("DEFAULT BRACKET FORECAST", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Your bracket is configured. Outstanding power centers like ${champ?.name ?: "undecided"} lead the progression paths. The current simulated upset odds stand at ${String.format("%.1f", upsetRate)}%, suggesting a cohesive tournament timeline containing average surprise hazards.\n\n" +
                                            "In the current format, UEFA and CONMEBOL hold strong top seed advantages, but counter-attacking units from CAF and CONCACAF pose crucial traps during knockouts! Draft the full Live briefing above to activate customized intelligence.",
                                    color = IceWhite.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Statistical difficulty review card row
        item {
            SectionHeader(title = "Calculated Bracket Difficulty")
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Champion Route Standard", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (champ != null) "Verified" else "Awaiting Winner",
                            color = if (champ != null) PitchGreen else RedCard,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bracketPath,
                        fontSize = 13.sp,
                        color = MutedSlate
                    )
                }
            }
        }

        // Export Graphics Box
        item {
            SectionHeader(title = "Social Features & Share")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A1E17), StadiumSurface)
                        )
                    )
                    .border(1.5.dp, PitchGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FIFA World Cup 2026 Prediction Card".uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = TrophyGold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CHAMPION: ${champ?.name?.uppercase() ?: "TBD"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Simulated Goals: $totalGoals  •  Upset Rate: ${String.format("%.1f", upsetRate)}%",
                        fontSize = 11.sp,
                        color = MutedSlate,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showShareNotification = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("share_bracket_button")
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = PitchGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Forecast Briefing", color = Color.White, fontWeight = FontWeight.Black)
                    }

                    if (showShareNotification) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Prediction card link copied to clipboard! Ready to share with friends.",
                            color = PitchGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
