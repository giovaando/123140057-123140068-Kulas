package com.example.raillog.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.raillog.presentation.screens.welcome.WelcomeScreen
import com.example.raillog.presentation.screens.login.LoginScreen
import com.example.raillog.presentation.screens.home.HomeScreen
import com.example.raillog.presentation.screens.addsupply.AddSupplyScreen
import com.example.raillog.presentation.screens.detail.SupplyDetailScreen
import com.example.raillog.presentation.screens.ai.AIAssistantScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Welcome, // Layar pertama dibuka
        modifier = modifier
    ) {
        // Layar Sambutan (Welcome)
        composable<Route.Welcome> {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Welcome) { inclusive = true }
                    }
                }
            )
        }

        // Layar Otentikasi
        composable<Route.Login> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        // Layar Dashboard
        composable<Route.Home> {
            HomeScreen(
                onNavigateToAddNote = {
                    navController.navigate(Route.AddSupply(null))
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Route.SupplyDetail(id))
                },
                onNavigateToAI = {
                    navController.navigate(Route.AIAssistant(null, null))
                }
            )
        }

        // Layar Tambah Suku Cadang
        composable<Route.AddSupply> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.AddSupply>()
            AddSupplyScreen(
                itemId = route.itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Layar Detail Suku Cadang
        composable<Route.SupplyDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SupplyDetail>()
            SupplyDetailScreen(
                itemId = route.itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Route.AddSupply(id))
                }
            )
        }

        // Layar Asisten AI
        composable<Route.AIAssistant> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.AIAssistant>()
            AIAssistantScreen(
                noteId = route.itemId,
                initialText = route.initialText,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}