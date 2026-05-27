package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
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

@Composable
fun BracketScreen(
    viewModel: PredictionViewModel,
    onNavigateToMatchDetails: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val teamsList = viewModel.teamsList

    val rounds = listOf(
        "R32" to "ROUND OF 32",
        "R16" to "ROUND OF 16",
        "QF" to "QUARTER-FINALS",
        "SF" to "SEMI-FINALS",
        "THIRD" to "3RD PLAYOFF",
        "FINAL" to "THE GRAND FINAL"
    )
    var selectedRoundKey by remember { mutableStateOf("R32") }

    val activeMatches = matches.filter { it.stage == selectedRoundKey }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
    ) {
        // Horizontal Scrollable Round tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(StadiumDark)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rounds) { (key, title) ->
                val isSelected = selectedRoundKey == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) TrophyGold else StadiumSurface)
                        .border(1.dp, if (isSelected) TrophyGold else StadiumBorder, RoundedCornerShape(50.dp))
                        .clickable { selectedRoundKey = key }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("bracket_tab_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.Black else MutedSlate,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Round Matches List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "TAP TEAM TO ADVANCE TO THE NEXT ROUND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MutedSlate,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            items(activeMatches) { match ->
                val teamA = teamsList.find { it.id == match.teamAId }
                val teamB = teamsList.find { it.id == match.teamBId }
                val winnerId = match.getWinnerId()

                Card(
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (match.isPredictionCompleted) TrophyGold.copy(alpha = 0.4f) else StadiumBorder,
                                StadiumBorder.copy(alpha = 0.2f)
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Match Name Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.date.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MutedSlate,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (match.isPredictionCompleted) TrophyGold else StadiumBorder,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Match Grid Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Team A Selection Node
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (winnerId != null && winnerId == teamA?.id) PitchGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (winnerId != null && winnerId == teamA?.id) PitchGreen.copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        teamA?.let {
                                            viewModel.predictMatch(match.id, 2, 1, it.id)
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (teamA != null) {
                                    FlagIcon(emoji = teamA.flagEmoji, size = 44.dp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(teamA.id, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                    Text(teamA.name, fontSize = 10.sp, color = MutedSlate, maxLines = 1, textAlign = TextAlign.Center)
                                    Text("Rating ${teamA.rating}", fontSize = 9.sp, color = PitchGreen, fontWeight = FontWeight.Bold)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(StadiumBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("?", color = IceWhite, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(match.teamAId ?: "TBD", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MutedSlate)
                                }
                            }

                            // Center VS Divider
                            Column(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .clickable { onNavigateToMatchDetails(match) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (match.predictedScoreA != null && match.predictedScoreB != null) {
                                    Text(
                                        text = "${match.predictedScoreA} - ${match.predictedScoreB}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text("Details", fontSize = 9.sp, color = PitchGreen, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StadiumBorder.copy(alpha = 0.6f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("VS", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // Team B Selection Node
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (winnerId != null && winnerId == teamB?.id) PitchGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (winnerId != null && winnerId == teamB?.id) PitchGreen.copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        teamB?.let {
                                            viewModel.predictMatch(match.id, 1, 2, it.id)
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (teamB != null) {
                                    FlagIcon(emoji = teamB.flagEmoji, size = 44.dp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(teamB.id, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                    Text(teamB.name, fontSize = 10.sp, color = MutedSlate, maxLines = 1, textAlign = TextAlign.Center)
                                    Text("Rating ${teamB.rating}", fontSize = 9.sp, color = PitchGreen, fontWeight = FontWeight.Bold)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(StadiumBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("?", color = IceWhite, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(match.teamBId ?: "TBD", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MutedSlate)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
