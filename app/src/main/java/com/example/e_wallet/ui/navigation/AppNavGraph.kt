package com.example.e_wallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.e_wallet.ui.history.TransactionHistoryScreen
import com.example.e_wallet.ui.home.HomeScreen
import com.example.e_wallet.ui.profile.ProfileScreen

/** Top-level navigation destinations. */
object AppDestinations {
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(AppDestinations.TRANSACTION_HISTORY)
                },
                onNavigateToProfile = {
                    navController.navigate(AppDestinations.PROFILE)
                }
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
