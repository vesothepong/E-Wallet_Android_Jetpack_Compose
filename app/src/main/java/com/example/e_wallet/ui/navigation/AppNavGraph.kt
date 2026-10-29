package com.example.e_wallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.e_wallet.ui.auth.LoginScreen
import com.example.e_wallet.ui.auth.OnboardingScreen
import com.example.e_wallet.ui.auth.SplashScreen
import com.example.e_wallet.ui.bills.BillPaymentScreen
import com.example.e_wallet.ui.history.TransactionHistoryScreen
import com.example.e_wallet.ui.home.HomeScreen
import com.example.e_wallet.ui.notifications.NotificationsScreen
import com.example.e_wallet.ui.profile.ProfileScreen
import com.example.e_wallet.ui.savings.SavingsScreen
import com.example.e_wallet.ui.wallet.WalletScreen

/** Top-level navigation destinations for PocketPay. */
object AppDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val PROFILE = "profile"
    const val WALLET = "wallet"
    const val NOTIFICATIONS = "notifications"
    const val BILL_PAYMENT = "bill_payment"
    const val SAVINGS = "savings"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH
    ) {
        composable(AppDestinations.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.HOME) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(AppDestinations.TRANSACTION_HISTORY)
                },
                onNavigateToProfile = {
                    navController.navigate(AppDestinations.PROFILE)
                },
                onNavigateToWallet = {
                    navController.navigate(AppDestinations.WALLET)
                },
                onNavigateToNotifications = {
                    navController.navigate(AppDestinations.NOTIFICATIONS)
                },
                onNavigateToBills = {
                    navController.navigate(AppDestinations.BILL_PAYMENT)
                },
                onNavigateToSavings = {
                    navController.navigate(AppDestinations.SAVINGS)
                }
            )
        }

        composable(AppDestinations.SAVINGS) {
            SavingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.WALLET) {
            WalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.BILL_PAYMENT) {
            BillPaymentScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
