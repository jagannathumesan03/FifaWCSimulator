package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.GroupStandingsEntry
import com.example.data.Match
import com.example.ui.PredictionViewModel
import com.example.ui.theme.*
import androidx.compose.ui.text.font.FontStyle

@Composable
fun GroupsScreen(
    viewModel: PredictionViewModel,
    onNavigateToMatchDetails: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val groupStandings by viewModel.groupStandings.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    
    val groups = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
    
    val activeStandings = groupStandings[selectedGroup] ?: emptyList()
    val activeMatches = matches.filter { it.group == selectedGroup }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
    ) {
        // Horizontal Group Select Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(StadiumDark)
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { grp ->
                val isSelected = selectedGroup == grp
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) IceWhite else StadiumSurface)
                        .border(1.dp, if (isSelected) IceWhite else StadiumBorder, RoundedCornerShape(50.dp))
                        .clickable { viewModel.selectGroup(grp) }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("group_selector_$grp"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GROUP $grp",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.Black else MutedSlate
                    )
                }
            }
        }

        // Selected Group Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Title Info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GROUP $selectedGroup STANDINGS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.randomizeGroup(selectedGroup) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumSurface),
                            modifier = Modifier.border(1.dp, StadiumBorder, RoundedCornerShape(50.dp))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Simulate", tint = PitchGreen)
                        }
                        IconButton(
                            onClick = { viewModel.resetGroup(selectedGroup) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = StadiumSurface),
                            modifier = Modifier.border(1.dp, StadiumBorder, RoundedCornerShape(50.dp))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = RedCard)
                        }
                    }
                }
            }

            // Standings Table Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Table Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Team", modifier = Modifier.weight(2.2f), fontSize = 11.sp, color = MutedSlate, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("P", modifier = Modifier.weight(0.5f), fontSize = 11.sp, color = MutedSlate, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("GD", modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = MutedSlate, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("Pts", modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = MutedSlate, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                        
                        Divider(color = StadiumBorder, thickness = 1.dp)

                        // Standing list
                        activeStandings.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Position and flag
                                Row(
                                    modifier = Modifier.weight(2.2f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (index < 2) PitchGreen else IceWhite,
                                        modifier = Modifier.width(18.dp)
                                    )
                                    FlagIcon(emoji = entry.flagEmoji, size = 28.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = entry.teamName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("${entry.played}", modifier = Modifier.weight(0.5f), fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
                                Text(
                                    text = if (entry.goalsDifference > 0) "+${entry.goalsDifference}" else "${entry.goalsDifference}",
                                    modifier = Modifier.weight(0.7f),
                                    fontSize = 13.sp,
                                    color = if (entry.goalsDifference > 0) PitchGreen else if (entry.goalsDifference < 0) RedCard else Color.White,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.points}",
                                    modifier = Modifier.weight(0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (index < 2) TrophyGold else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (index < activeStandings.lastIndex) {
                                Divider(color = StadiumBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Group Matches Header
            item {
                Text(
                    text = "GROUP FIXTURES (TAP TO PREDICT)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MutedSlate,
                    letterSpacing = 1.8.sp
                )
            }

            // List of 6 fixtures
            items(activeMatches) { match ->
                val teamA = viewModel.teamsList.first { it.id == match.teamAId }
                val teamB = viewModel.teamsList.first { it.id == match.teamBId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMatchDetails(match) },
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (match.isPredictionCompleted) PitchGreen.copy(alpha = 0.4f) else StadiumBorder,
                                StadiumBorder.copy(alpha = 0.2f)
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                            if (match.isPredictionCompleted) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PitchGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("SIMULATED", color = PitchGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Match Teams and scores
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Team A
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlagIcon(emoji = teamA.flagEmoji, size = 32.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(teamA.id, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                    Text(teamA.name, fontSize = 11.sp, color = MutedSlate, maxLines = 1)
                                }
                            }

                            // Scores Panel
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                if (match.predictedScoreA != null && match.predictedScoreB != null) {
                                    Text(
                                        text = "${match.predictedScoreA}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (match.predictedScoreA!! > match.predictedScoreB!!) PitchGreen else Color.White
                                    )
                                    Text(" : ", fontSize = 18.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${match.predictedScoreB}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (match.predictedScoreB!! > match.predictedScoreA!!) PitchGreen else Color.White
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StadiumBorder.copy(alpha = 0.6f))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("VS", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    }
                                }
                            }

                            // Team B
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(teamB.id, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                    Text(teamB.name, fontSize = 11.sp, color = MutedSlate, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                FlagIcon(emoji = teamB.flagEmoji, size = 32.dp)
                            }
                        }

                        // Outcome Prediction Quick Shortcuts
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val activeWinner = match.predictedWinnerId
                            val scoreA = match.predictedScoreA
                            val scoreB = match.predictedScoreB

                            Button(
                                onClick = { viewModel.predictMatch(match.id, 2, 1, teamA.id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scoreA != null && scoreB != null && scoreA > scoreB) PitchGreen else StadiumBorder
                                )
                            ) {
                                Text(
                                    text = "${teamA.id} WIN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (scoreA != null && scoreB != null && scoreA > scoreB) Color.Black else Color.White
                                )
                            }

                            Button(
                                onClick = { viewModel.predictMatch(match.id, 1, 1, "DRAW") },
                                modifier = Modifier.weight(0.8f).height(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scoreA != null && scoreB != null && scoreA == scoreB) PitchBlue else StadiumBorder
                                )
                            ) {
                                Text(
                                    text = "DRAW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (scoreA != null && scoreB != null && scoreA == scoreB) Color.Black else Color.White
                                )
                            }

                            Button(
                                onClick = { viewModel.predictMatch(match.id, 1, 2, teamB.id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scoreA != null && scoreB != null && scoreB > scoreA) PitchGreen else StadiumBorder
                                )
                            ) {
                                Text(
                                    text = "${teamB.id} WIN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (scoreA != null && scoreB != null && scoreB > scoreA) Color.Black else Color.White
                                )
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
