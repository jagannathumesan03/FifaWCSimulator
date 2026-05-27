package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PredictionViewModel
import com.example.ui.theme.*
import androidx.compose.ui.text.font.FontStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedBracketsScreen(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val brackets by viewModel.savedBrackets.collectAsState()
    var newBracketName by remember { mutableStateOf("") }
    var operationResult by remember { mutableStateOf("") }

    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StadiumDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Save current bracket section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Save Active Bracket Draft")
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(StadiumSurface)
                    .border(1.dp, StadiumBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = newBracketName,
                    onValueChange = { newBracketName = it },
                    label = { Text("Bracket Name", color = MutedSlate) },
                    placeholder = { Text("e.g. My Sleeper Tournament", color = MutedSlate.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bracket_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PitchGreen,
                        unfocusedBorderColor = StadiumBorder,
                        focusedLabelColor = PitchGreen,
                        unfocusedLabelColor = MutedSlate
                    ),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (newBracketName.isNotBlank()) {
                            viewModel.saveBracketAs(newBracketName.trim())
                            newBracketName = ""
                            operationResult = "Bracket saved successfully!"
                        } else {
                            operationResult = "Please enter a valid bracket title."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_bracket_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Current Layout", color = Color.Black, fontWeight = FontWeight.Black)
                }

                if (operationResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = operationResult,
                        color = if (operationResult.contains("success")) PitchGreen else RedCard,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // List of brackets header
        item {
            SectionHeader(title = "Saved Bracket Layouts (${brackets.size})")
        }

        // Saved list
        if (brackets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(StadiumSurface)
                        .border(1.dp, StadiumBorder, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved brackets. Use the builder above to store your custom models.",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(brackets) { bracket ->
                val dateStr = formatter.format(Date(bracket.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = StadiumSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StadiumBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bracket.name.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Created: $dateStr",
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Load button
                            IconButton(
                                onClick = {
                                    viewModel.loadBracket(bracket.id)
                                    operationResult = "Loaded layout: ${bracket.name}"
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = PitchGreen.copy(alpha = 0.15f)),
                                modifier = Modifier.border(1.dp, PitchGreen.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Load", tint = PitchGreen)
                            }

                            // Delete button
                            IconButton(
                                onClick = {
                                    viewModel.deleteBracket(bracket.id)
                                    operationResult = "Deleted layout."
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = RedCard.copy(alpha = 0.15f)),
                                modifier = Modifier.border(1.dp, RedCard.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedCard)
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
