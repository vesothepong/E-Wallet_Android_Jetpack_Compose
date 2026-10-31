package com.example.e_wallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.e_wallet.ui.analytics.AnalyticsScreen
import com.example.e_wallet.ui.auth.KycSignupScreen
import com.example.e_wallet.ui.auth.LoginScreen
import com.example.e_wallet.ui.auth.OnboardingScreen
import com.example.e_wallet.ui.auth.SplashScreen
import com.example.e_wallet.ui.bills.BillPaymentScreen
import com.example.e_wallet.ui.components.BottomNavTab
import com.example.e_wallet.ui.history.TransactionHistoryScreen
import com.example.e_wallet.ui.home.HomeScreen
import com.example.e_wallet.ui.notifications.NotificationsScreen
import com.example.e_wallet.ui.profile.ProfileScreen
import com.example.e_wallet.ui.savings.SavingsScreen
import com.example.e_wallet.ui.split.SplitBillScreen
import com.example.e_wallet.ui.wallet.WalletScreen

/** Top-level navigation destinations for PocketPay. */
object AppDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val KYC_SIGNUP = "kyc_signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
    const val PROFILE = "profile"
    const val WALLET = "wallet"
    const val NOTIFICATIONS = "notifications"
    const val BILL_PAYMENT = "bill_payment"
    const val SAVINGS = "savings"
    const val ANALYTICS = "analytics"
    const val SPLIT_BILL = "split_bill"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    fun navigateToTab(tab: BottomNavTab) {
        when (tab) {
            BottomNavTab.HOME -> navController.navigate(AppDestinations.HOME) {
                popUpTo(AppDestinations.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            BottomNavTab.TRANSFER -> navController.navigate(AppDestinations.TRANSACTION_HISTORY) {
                launchSingleTop = true
            }
            BottomNavTab.CARDS -> navController.navigate(AppDestinations.WALLET) {
                launchSingleTop = true
            }
            BottomNavTab.PROFILE -> navController.navigate(AppDestinations.PROFILE) {
                launchSingleTop = true
            }
        }
    }

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
                    navController.navigate(AppDestinations.KYC_SIGNUP)
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN)
                }
            )
        }

        composable(AppDestinations.KYC_SIGNUP) {
            KycSignupScreen(
                onBackClick = { navController.popBackStack() },
                onContinue = {
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
                },
                onNavigateToAnalytics = {
                    navController.navigate(AppDestinations.ANALYTICS)
                },
                onNavigateToSplitBill = {
                    navController.navigate(AppDestinations.SPLIT_BILL)
                }
            )
        }

        composable(AppDestinations.SPLIT_BILL) {
            SplitBillScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.ANALYTICS) {
            AnalyticsScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.SAVINGS) {
            SavingsScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.WALLET) {
            WalletScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }

        composable(AppDestinations.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.BILL_PAYMENT) {
            BillPaymentScreen(
                onBackClick = { navController.popBackStack() },
                onTabSelected = ::navigateToTab
            )
        }
    }
}
