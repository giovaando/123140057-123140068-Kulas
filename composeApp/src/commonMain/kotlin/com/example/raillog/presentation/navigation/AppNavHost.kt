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
import com.example.raillog.presentation.screens.staff_main.StaffMainScreen
import com.example.raillog.presentation.screens.home.HomeScreen
import com.example.raillog.presentation.screens.addsupply.AddSupplyScreen
import com.example.raillog.presentation.screens.detail.SupplyDetailScreen
import com.example.raillog.presentation.screens.ai.AIAssistantScreen
import com.example.raillog.presentation.screens.requisition.RequisitionScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Welcome,
        modifier = modifier
    ) {
        composable<Route.Welcome> {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Welcome) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Login> {
            LoginScreen(
                onNavigateToHome = {
                    // Sementara mengarahkan langsung ke StaffMainScreen
                    navController.navigate(Route.StaffMain) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        // --- Rute Baru untuk Staff Gudang ---
        composable<Route.StaffMain> {
            StaffMainScreen(
                onNavigateToNewRequisition = {
                    navController.navigate(Route.RequisitionWizard) // Arahkan ke rute Form
                }
            )
        }

        // --- Rute Form 5 Langkah ---
        composable<Route.RequisitionWizard> {
            RequisitionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    // Ketika submit sukses, kembali ke halaman utama Staff
                    navController.navigate(Route.StaffMain) {
                        popUpTo(Route.StaffMain) { inclusive = true }
                    }
                }
            )
        }

        // --- Rute Lama ---
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

        composable<Route.AddSupply> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.AddSupply>()
            AddSupplyScreen(
                itemId = route.itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

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