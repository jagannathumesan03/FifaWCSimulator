package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FlagIcon(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(StadiumBorder.copy(alpha = 0.5f))
            .border(1.dp, StadiumBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.55).sp,
            lineHeight = (size.value * 0.55).sp
        )
    }
}

@Composable
fun TacticalPitchVisualizer(
    modifier: Modifier = Modifier,
    homeTeamEmoji: String = "⚽",
    awayTeamEmoji: String = "🛡️",
    homeName: String = "A",
    awayName: String = "B"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF064E3B), StadiumDark)
                )
            )
            .border(1.5.dp, PitchGreen.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
    ) {
        // Draw football pitch lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lineColor = Color.White.copy(alpha = 0.15f)
            val strokeWidth = 1.5.dp.toPx()

            // Outer margin boundary
            drawRect(
                color = lineColor,
                topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                size = size.copy(width = w - 24.dp.toPx(), height = h - 24.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Half-way line
            drawLine(
                color = lineColor,
                start = Offset(w / 2, 12.dp.toPx()),
                end = Offset(w / 2, h - 12.dp.toPx()),
                strokeWidth = strokeWidth
            )

            // Center circle
            drawCircle(
                color = lineColor,
                radius = 30.dp.toPx(),
                center = Offset(w / 2, h / 2),
                style = Stroke(width = strokeWidth)
            )

            // Left Penalty area
            drawRect(
                color = lineColor,
                topLeft = Offset(12.dp.toPx(), h / 4),
                size = size.copy(width = 30.dp.toPx(), height = h / 2),
                style = Stroke(width = strokeWidth)
            )

            // Right Penalty area
            drawRect(
                color = lineColor,
                topLeft = Offset(w - 42.dp.toPx(), h / 4),
                size = size.copy(width = 30.dp.toPx(), height = h / 2),
                style = Stroke(width = strokeWidth)
            )
        }

        // Home Team Formation Node
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlagIcon(emoji = homeTeamEmoji, size = 36.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = homeName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Ball in Center
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("⚽", fontSize = 11.sp)
        }

        // Away Team Formation Node
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlagIcon(emoji = awayTeamEmoji, size = 36.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = awayName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MutedSlate
        )
        trailing?.invoke()
    }
}
