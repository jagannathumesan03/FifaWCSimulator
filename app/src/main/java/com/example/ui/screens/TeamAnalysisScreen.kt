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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Team
import com.example.ui.PredictionViewModel
import com.example.ui.theme.*
import androidx.compose.ui.text.font.FontStyle

@Composable
fun TeamAnalysisScreen(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val maybeTeam by viewModel.selectedTeam.collectAsState()
    val selectedTeam = maybeTeam ?: viewModel.teamsList.first()

    val matches by viewModel.matches.collectAsState()
    val teamsList = viewModel.teamsList

    // Evaluate simulated path for the selected team
    val teamPath = remember(matches, selectedTeam.id) {
        val path = mutableListOf<String>()
        
        // 1. Group stage games
        val groupMatches = matches.filter { !it.isKnockout && (it.teamAId == selectedTeam.id || it.teamBId == selectedTeam.id) }
        for (gm in groupMatches) {
            val oppId = if (gm.teamAId == selectedTeam.id) gm.teamBId else gm.teamAId
            val opp = teamsList.find { it.id == oppId }
            val scoreOwn = if (gm.teamAId == selectedTeam.id) gm.predictedScoreA else gm.predictedScoreB
            val scoreOpp = if (gm.teamAId == selectedTeam.id) gm.predictedScoreB else gm.predictedScoreA
            
            val status = if (scoreOwn != null && scoreOpp != null) {
                when {
                    scoreOwn > scoreOpp -> "WON ${scoreOwn}-${scoreOpp}"
                    scoreOpp > scoreOwn -> "LOST ${scoreOwn}-${scoreOpp}"
                    else -> "DREW ${scoreOwn}-${scoreOpp}"
                }
            } else {
                "PENDING"
            }
            path.add("GROUP Stage Match: vs ${opp?.name ?: "TBD"} ($status)")
        }

        // 2. Playoff games
        val knockoutMatches = matches.filter { m -> m.isKnockout && (m.teamAId == selectedTeam.id || m.teamBId == selectedTeam.id) }
            .sortedBy { m ->
                when (m.stage) {
                    "R32" -> 1
                    "R16" -> 2
                    "QF" -> 3
                    "SF" -> 4
                    "THIRD" -> 5
                    "FINAL" -> 6
                    else -> 10
                }
            }
        
        for (km in knockoutMatches) {
            val oppId = if (km.teamAId == selectedTeam.id) km.teamBId else km.teamAId
            val opp = teamsList.find { it.id == oppId }
            val winner = km.getWinnerId()
            
            val status = if (winner != null) {
                if (winner == selectedTeam.id) "ADVANCED" else "ELIMINATED"
            } else {
                "PENDING PLAY"
            }
            path.add("${km.stage.uppercase()}: vs ${opp?.name ?: "TBD"} ($status)")
        }

        path
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Scrollable list of ALL countries
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SELECT COUNTRY PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MutedSlate,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teamsList) { team ->
                    val isSelected = selectedTeam.id == team.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) IceWhite else StadiumSurface)
                            .border(1.dp, if (isSelected) IceWhite else StadiumBorder, RoundedCornerShape(50.dp))
                            .clickable { viewModel.selectTeam(team.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(team.flagEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = team.id,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.Black else MutedSlate
                            )
                        }
                    }
                }
            }
        }

        // Selected Country Card Details
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(StadiumSurface, StadiumDark)
                        )
                    )
                    .border(1.5.dp, PitchGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlagIcon(emoji = selectedTeam.flagEmoji, size = 52.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "TEAM ARCHETYPE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PitchGreen,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = selectedTeam.name.uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.White
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PitchGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "RANK ${selectedTeam.rank}",
                                color = PitchGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = StadiumBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("KEY INFLUENCE PLAYER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MutedSlate, letterSpacing = 1.sp)
                    Text(selectedTeam.keyPlayer, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("TACTICAL SYSTEM FOCUS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MutedSlate, letterSpacing = 1.sp)
                    Text(selectedTeam.tacticalStyle, fontSize = 13.sp, color = IceWhite.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("FIFA RATINGS POWER INDEX", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MutedSlate, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { selectedTeam.rating / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TrophyGold,
                            trackColor = StadiumBorder
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${selectedTeam.rating}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Pathfinder Map
        item {
            SectionHeader(title = "Custom Bracket Path Analyzer")
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = Brush.linearGradient(listOf(StadiumBorder, StadiumBorder.copy(alpha = 0.2f)))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (teamPath.isEmpty()) {
                        Text(
                            "Complete predictions to map simulated pathways.",
                            color = MutedSlate,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        teamPath.forEachIndexed { index, checkpoint ->
                            val isWin = checkpoint.contains("WON") || checkpoint.contains("ADVANCED")
                            val isLost = checkpoint.contains("LOST") || checkpoint.contains("ELIMINATED")
                            val indicatorColor = if (isWin) PitchGreen else if (isLost) RedCard else TrophyGold

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(indicatorColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = checkpoint,
                                    color = IceWhite.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (index < teamPath.size - 1) {
                                Divider(color = StadiumBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
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
