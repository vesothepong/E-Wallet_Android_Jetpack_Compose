package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.ui.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Authentication, Splash, Onboarding & User Registration:
 * - Single source of truth: Authentication state updates repository and gatekeeps app navigation.
 * - Onboarding state progression: completes onboarding and persists flag.
 * - Login validation: credentials & 4-digit PIN verification with demo fill.
 * - Biometric login shortcut checks biometric settings.
 * - New user registration: validates fields, initializes new demo wallet with $100 welcome bonus, and creates welcome notification.
 * - Logout session reset: revokes authentication state.
 */
class AuthBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var authViewModel: AuthViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        authViewModel = AuthViewModel(repository, testScope)
    }

    @Test
    fun testInitialAuthStateIsUnauthenticatedAndOnboardingIncomplete() {
        assertFalse("Initial state should be unauthenticated", repository.isAuthenticated.value)
        assertFalse("Initial onboarding should be incomplete", repository.isOnboardingCompleted.value)
    }

    @Test
    fun testCompleteOnboardingUpdatesRepository() {
        repository.completeOnboarding()
        assertTrue("Onboarding should be marked complete", repository.isOnboardingCompleted.value)
    }

    @Test
    fun testLoginWithCorrectDemoEmailAndPinSucceeds() {
        val result = repository.login("alex.morgan@pocketpay.demo", "1234")
        assertTrue("Login with correct email and PIN should succeed", result)
        assertTrue("Repository isAuthenticated should be true", repository.isAuthenticated.value)
    }

    @Test
    fun testLoginWithPhoneNumberAndPinSucceeds() {
        val result = repository.login("+1 (555) 234-5678", "1234")
        assertTrue("Login with phone number should succeed", result)
        assertTrue(repository.isAuthenticated.value)
    }

    @Test
    fun testLoginWithDemoAliasSucceeds() {
        val result = repository.login("demo", "1234")
        assertTrue("Login with demo alias should succeed", result)
        assertTrue(repository.isAuthenticated.value)
    }

    @Test
    fun testLoginWithWrongPinFails() {
        val result = repository.login("alex.morgan@pocketpay.demo", "9999")
        assertFalse("Login with wrong PIN should fail", result)
        assertFalse("Repository isAuthenticated should remain false", repository.isAuthenticated.value)
    }

    @Test
    fun testLoginWithUnknownIdentifierFails() {
        val result = repository.login("unknown.user@other.com", "1234")
        assertFalse("Login with unknown identifier should fail", result)
        assertFalse(repository.isAuthenticated.value)
    }

    @Test
    fun testBiometricLoginWhenEnabledSucceeds() {
        repository.setBiometricsEnabled(true)
        val result = repository.loginWithBiometrics()
        assertTrue("Biometric login should succeed when enabled", result)
        assertTrue(repository.isAuthenticated.value)
    }

    @Test
    fun testBiometricLoginWhenDisabledFails() {
        repository.setBiometricsEnabled(false)
        val result = repository.loginWithBiometrics()
        assertFalse("Biometric login should fail when disabled", result)
        assertFalse(repository.isAuthenticated.value)
    }

    @Test
    fun testLogoutRevokesAuthentication() {
        repository.login("alex.morgan@pocketpay.demo", "1234")
        assertTrue(repository.isAuthenticated.value)

        repository.logout()
        assertFalse("Logout should revoke authentication", repository.isAuthenticated.value)
    }

    @Test
    fun testRegistrationWithValidDataCreatesNewUserAndWallet() {
        val name = "Jordan Lee"
        val email = "jordan.lee@pocketpay.demo"
        val phone = "+1 (555) 888-7766"
        val pin = "4321"

        val success = repository.register(name, email, phone, pin)
        assertTrue("Registration should succeed", success)
        assertTrue("User should be authenticated after register", repository.isAuthenticated.value)
        assertTrue("Onboarding should be marked completed", repository.isOnboardingCompleted.value)

        // Verify user profile
        assertEquals(name, repository.currentUser.value.fullName)
        assertEquals(email, repository.currentUser.value.email)
        assertEquals(phone, repository.currentUser.value.phoneNumber)
        assertEquals(pin, repository.currentPin.value)

        // Verify welcome bonus ($100.00 = 10,000 cents)
        assertEquals(10000L, repository.currentWallet.value.currentBalance)
        assertEquals(10000L, repository.currentWallet.value.availableBalance)

        // Verify welcome notification
        val latestNotif = repository.notifications.value.first()
        assertTrue(latestNotif.title.contains("Welcome"))
    }

    @Test
    fun testRegistrationWithInvalidEmailRejected() {
        val success = repository.register("Jordan Lee", "invalid-email", "+1 (555) 888-7766", "4321")
        assertFalse("Registration with malformed email should fail", success)
    }

    @Test
    fun testRegistrationWithShortPinRejected() {
        val success = repository.register("Jordan Lee", "jordan@demo.com", "+1 (555) 888-7766", "12")
        assertFalse("PIN with fewer than 4 digits must be rejected", success)
    }

    @Test
    fun testAuthViewModelValidationAndDemoFill() {
        // Toggle mode
        assertFalse(authViewModel.uiState.value.isRegisterMode)
        authViewModel.toggleAuthMode()
        assertTrue(authViewModel.uiState.value.isRegisterMode)
        authViewModel.toggleAuthMode()
        assertFalse(authViewModel.uiState.value.isRegisterMode)

        // 1-Tap Demo Fill
        authViewModel.fillDemoUser()
        assertEquals("alex.morgan@pocketpay.demo", authViewModel.uiState.value.identifierInput)
        assertEquals("1234", authViewModel.uiState.value.pinInput)
    }

    @Test
    fun testResetDemoDataRevertsAuthAndOnboarding() {
        repository.login("alex.morgan@pocketpay.demo", "1234")
        repository.completeOnboarding()
        assertTrue(repository.isAuthenticated.value)
        assertTrue(repository.isOnboardingCompleted.value)

        repository.resetDemoData()
        assertFalse("resetDemoData must clear authentication", repository.isAuthenticated.value)
        assertFalse("resetDemoData must clear onboarding status", repository.isOnboardingCompleted.value)
    }
}
