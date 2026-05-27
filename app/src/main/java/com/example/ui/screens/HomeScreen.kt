package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun HomeScreen(
    viewModel: PredictionViewModel,
    onNavigateToGroups: () -> Unit,
    onNavigateToBracket: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToTeams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val teamsList = viewModel.teamsList
    
    val totalGoals = viewModel.getTotalPredictedGoals()
    val upsetRate = viewModel.getUpsetPercentage()
    val champ = viewModel.getMostSelectedChampion()
    val completedCount = matches.count { it.predictedScoreA != null }
    val progress = if (matches.isNotEmpty()) completedCount.toFloat() / matches.size else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Brand Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(StadiumSurface, Color(0xFF064E3B).copy(alpha = 0.3f))
                        )
                    )
                    .border(1.5.dp, StadiumBorder, RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(PitchGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FIFA WC 2026",
                            color = PitchGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "BRACKET MASTER",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build your custom bracket, adjust scores, and simulate tournament progression with dynamic AI tactical reports.",
                        color = IceWhite.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onNavigateToGroups,
                            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("predict_now_button")
                        ) {
                            Text("PICK GROUPS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToBracket,
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.5f)))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("BRACKET", fontWeight = FontWeight.Black, fontSize = 12.sp, color = IceWhite)
                        }
                    }
                }
            }
        }

        // Bracket Progress Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREDICTION PROGRESS",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = MutedSlate,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$completedCount / ${matches.size} Matches",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = PitchGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PitchGreen,
                        trackColor = StadiumBorder
                    )
                }
            }
        }

        // Showcase Champion Card
        item {
            SectionHeader(title = "Your Selected Champion")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF271B0B), StadiumSurface),
                            radius = 600f
                        )
                    )
                    .border(1.5.dp, TrophyGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .clickable { onNavigateToBracket() }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (champ != null) {
                            FlagIcon(emoji = champ.flagEmoji, size = 52.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "CHAMPION ELECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TrophyGold,
                                    letterSpacing = 1.5.sp
                               )
                                Text(
                                    text = champ.name.uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Roster: ${champ.keyPlayer} • style: ${champ.tacticalStyle}",
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StadiumBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = TrophyGold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "CHAMPION ELECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MutedSlate,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "NO WINNER SELECTED",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IceWhite.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "Complete the bracket to nominate the winner",
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TrophyGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Live Statistics Row
        item {
            SectionHeader(title = "Dynamic Predictor Metrics")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Upset Gauge
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "UPSET ODDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MutedSlate,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${String.format("%.1f", upsetRate)}%",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = PitchGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lower-rank wins",
                            fontSize = 10.sp,
                            color = MutedSlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Total Goals Counter
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIMULATED GOALS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MutedSlate,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$totalGoals",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = PitchBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Predicted goals",
                            fontSize = 10.sp,
                            color = MutedSlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Fast Action Bar
        item {
            SectionHeader(title = "Instant Diagnostics")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.autoCompleteAllRemaining() },
                    colors = ButtonDefaults.buttonColors(containerColor = StadiumBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PitchGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-predict All", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = { viewModel.resetAllPredictions() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1518)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = RedCard)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
