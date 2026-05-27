package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.PredictionViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.theme.StadiumDark
import com.example.ui.theme.StadiumSurface
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.MutedSlate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val navController = rememberNavController()
    // Setup predicting main MVVM ViewModel using custom factory and application context
    val viewModel: PredictionViewModel = viewModel(
        factory = PredictionViewModel.Factory(
            application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation bar on secondary Match Details Screen
    val displayBottomBar = currentRoute != "match_details"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (displayBottomBar) {
                NavigationBar(
                    containerColor = StadiumSurface,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = currentRoute == "home" || currentRoute == null,
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PitchGreen,
                            indicatorColor = PitchGreen,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_home")
                    )

                    // 2. Groups
                    NavigationBarItem(
                        selected = currentRoute == "groups",
                        onClick = {
                            navController.navigate("groups") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "Groups") },
                        label = { Text("Groups", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PitchGreen,
                            indicatorColor = PitchGreen,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_groups")
                    )

                    // 3. Bracket
                    NavigationBarItem(
                        selected = currentRoute == "bracket",
                        onClick = {
                            navController.navigate("bracket") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Playoffs") },
                        label = { Text("Playoffs", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = TrophyGold,
                            indicatorColor = TrophyGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_playoffs")
                    )

                    // 4. Team Analysis
                    NavigationBarItem(
                        selected = currentRoute == "teams",
                        onClick = {
                            navController.navigate("teams") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Rosters") },
                        label = { Text("Rosters", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PitchGreen,
                            indicatorColor = PitchGreen,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_rosters")
                    )

                    // 5. AI Insights
                    NavigationBarItem(
                        selected = currentRoute == "insights",
                        onClick = {
                            navController.navigate("insights") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = "AI") },
                        label = { Text("AI", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = TrophyGold,
                            indicatorColor = TrophyGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_analytics")
                    )

                    // 6. Saved Brackets Profile Drawer
                    NavigationBarItem(
                        selected = currentRoute == "saved",
                        onClick = {
                            navController.navigate("saved") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Saved") },
                        label = { Text("Saved", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = PitchGreen,
                            indicatorColor = PitchGreen,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate
                        ),
                        modifier = Modifier.testTag("tab_saved")
                    )
                }
            }
        },
        containerColor = StadiumDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToGroups = { navController.navigate("groups") },
                    onNavigateToBracket = { navController.navigate("bracket") },
                    onNavigateToInsights = { navController.navigate("insights") },
                    onNavigateToTeams = { navController.navigate("teams") }
                )
            }
            composable("groups") {
                GroupsScreen(
                    viewModel = viewModel,
                    onNavigateToMatchDetails = { match ->
                        viewModel.selectMatch(match)
                        navController.navigate("match_details")
                    }
                )
            }
            composable("bracket") {
                BracketScreen(
                    viewModel = viewModel,
                    onNavigateToMatchDetails = { match ->
                        viewModel.selectMatch(match)
                        navController.navigate("match_details")
                    }
                )
            }
            composable("match_details") {
                MatchDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("teams") {
                TeamAnalysisScreen(viewModel = viewModel)
            }
            composable("insights") {
                InsightsScreen(viewModel = viewModel)
            }
            composable("saved") {
                SavedBracketsScreen(viewModel = viewModel)
            }
        }
    }
}
