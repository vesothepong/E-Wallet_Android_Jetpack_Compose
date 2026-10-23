package com.example.e_wallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.e_wallet.ui.history.TransactionHistoryScreen
import com.example.e_wallet.ui.home.HomeScreen

/** Top-level navigation destinations. */
object AppDestinations {
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
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
                }
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
