package com.example.raillog.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import com.example.raillog.presentation.screens.welcome.WelcomeScreen
import com.example.raillog.presentation.screens.login.LoginScreen
import com.example.raillog.presentation.screens.staff_main.StaffMainScreen
import com.example.raillog.presentation.screens.requisition.RequisitionScreen
import com.example.raillog.presentation.screens.requisition.RequisitionViewModel
import com.example.raillog.presentation.screens.admin_main.AdminMainScreen
import com.example.raillog.presentation.screens.admin_main.VerificationDetailScreen
import com.example.raillog.presentation.screens.home.HomeScreen
import com.example.raillog.presentation.screens.addsupply.AddSupplyScreen
import com.example.raillog.presentation.screens.detail.SupplyDetailScreen
import com.example.raillog.presentation.screens.ai.AIAssistantScreen
import org.koin.compose.viewmodel.koinViewModel

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
        // ==========================================
        // 1. ALUR OTENTIKASI
        // ==========================================

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
                onLoginSuccess = { role ->
                    // Membaca peran pengguna dari LoginScreen
                    if (role == "admin") {
                        navController.navigate(Route.AdminMain) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.StaffMain) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ==========================================
        // 2. ALUR STAF GUDANG
        // ==========================================

        composable<Route.StaffMain> {
            StaffMainScreen(
                onNavigateToNewRequisition = {
                    navController.navigate(Route.RequisitionWizard(draftId = null))
                },
                onNavigateToResumeDraft = { draftId ->
                    navController.navigate(Route.RequisitionWizard(draftId = draftId))
                }
            )
        }

        composable<Route.RequisitionWizard> { backStackEntry ->
            val viewModel: RequisitionViewModel = koinViewModel()
            RequisitionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {                          // ← Ganti onSubmissionSuccess
                    navController.navigate(Route.StaffMain) {
                        popUpTo(Route.StaffMain) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 3. ALUR ADMIN LOGISTIK
        // ==========================================

        composable<Route.AdminMain> {
            AdminMainScreen(
                onNavigateToVerificationDetail = { reqId ->
                    navController.navigate(Route.VerificationDetail(reqId))
                }
            )
        }

        composable<Route.VerificationDetail> { backStackEntry ->
            // Mengambil requisitionId spesifik yang diklik oleh Admin
            val route = backStackEntry.toRoute<Route.VerificationDetail>()
            VerificationDetailScreen(
                requisitionId = route.requisitionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 4. RUTE LAWAS (Jika masih digunakan)
        // ==========================================

        composable<Route.Home> {
            HomeScreen({}, {}, {})
        }

        composable<Route.AddSupply> {
            AddSupplyScreen(null, {})
        }

        composable<Route.SupplyDetail> {
            SupplyDetailScreen(0L, {}, {})
        }

        composable<Route.AIAssistant> {
            AIAssistantScreen(null, null, {})
        }
    }
}