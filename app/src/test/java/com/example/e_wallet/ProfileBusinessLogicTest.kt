package com.example.e_wallet

import com.example.e_wallet.data.InMemoryWalletRepository
import com.example.e_wallet.data.PinChangeResult
import com.example.e_wallet.ui.profile.ChangePinStep
import com.example.e_wallet.ui.profile.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for User Profile, Settings, and Security PIN management:
 * - Profile info retrieval & edit updates Single Source of Truth repository.
 * - Validation guards for user editing (name length, valid email structure, phone).
 * - Multi-step PIN verification: Old PIN check, New PIN validation, and Confirmation match.
 * - Dynamic PIN verification: Changing PIN in Profile impacts Send & QR Scanner PIN checks.
 * - Virtual Card freeze toggle and preferences management.
 * - Demo data reset reverts profile and security settings to default.
 */
class ProfileBusinessLogicTest {

    private lateinit var repository: InMemoryWalletRepository
    private lateinit var profileViewModel: ProfileViewModel
    private val testScope = CoroutineScope(Dispatchers.Unconfined + Job())

    @Before
    fun setUp() {
        repository = InMemoryWalletRepository()
        profileViewModel = ProfileViewModel(repository, testScope)
    }

    @Test
    fun testInitialProfileStateMatchesRepositoryDefaults() {
        val state = profileViewModel.uiState.value
        assertEquals("Alex Morgan", state.user.fullName)
        assertEquals("alex.morgan@pocketpay.demo", state.user.email)
        assertEquals("+1 (555) 234-5678", state.user.phoneNumber)
        assertEquals("1234", state.currentPin)
        assertTrue(state.isBiometricsEnabled)
        assertTrue(state.isNotificationsEnabled)
        assertFalse(state.isCardFrozen)
        assertFalse(state.isCardDetailsRevealed)
    }

    @Test
    fun testUpdateProfileWithValidDataUpdatesRepository() {
        profileViewModel.openEditProfileDialog()
        profileViewModel.onEditNameChanged("Alex J. Morgan")
        profileViewModel.onEditEmailChanged("alex.new@pocketpay.demo")
        profileViewModel.onEditPhoneChanged("+1 (555) 999-0000")
        profileViewModel.saveProfile()

        // Verify repository is updated
        assertEquals("Alex J. Morgan", repository.currentUser.value.fullName)
        assertEquals("alex.new@pocketpay.demo", repository.currentUser.value.email)
        assertEquals("+1 (555) 999-0000", repository.currentUser.value.phoneNumber)

        // Verify dialog closed
        assertFalse(profileViewModel.uiState.value.showEditProfileDialog)
        assertNull(profileViewModel.uiState.value.editProfileError)
    }

    @Test
    fun testUpdateProfileWithInvalidEmailFailsValidation() {
        profileViewModel.openEditProfileDialog()
        profileViewModel.onEditEmailChanged("invalid-email-address")
        profileViewModel.saveProfile()

        // Should keep dialog open with error
        assertTrue(profileViewModel.uiState.value.showEditProfileDialog)
        assertNotNull(profileViewModel.uiState.value.editProfileError)
        // Repository should retain original email
        assertEquals("alex.morgan@pocketpay.demo", repository.currentUser.value.email)
    }

    @Test
    fun testChangePinSuccessFlow() {
        profileViewModel.openChangePinSheet()
        assertEquals(ChangePinStep.OLD_PIN, profileViewModel.uiState.value.pinStep)

        // Enter correct current PIN: 1234
        "1234".forEach { profileViewModel.onPinDigit(it.toString()) }
        assertEquals(ChangePinStep.NEW_PIN, profileViewModel.uiState.value.pinStep)

        // Enter new PIN: 5678
        "5678".forEach { profileViewModel.onPinDigit(it.toString()) }
        assertEquals(ChangePinStep.CONFIRM_PIN, profileViewModel.uiState.value.pinStep)

        // Confirm new PIN: 5678
        "5678".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Verify PIN updated in repository
        assertEquals("5678", repository.currentPin.value)
        assertTrue(repository.verifyPin("5678"))
        assertFalse(repository.verifyPin("1234"))
        assertFalse(profileViewModel.uiState.value.showChangePinSheet)
    }

    @Test
    fun testChangePinFailsOnIncorrectOldPin() {
        profileViewModel.openChangePinSheet()

        // Enter wrong current PIN: 9999
        "9999".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Must remain on OLD_PIN step with error
        assertEquals(ChangePinStep.OLD_PIN, profileViewModel.uiState.value.pinStep)
        assertNotNull(profileViewModel.uiState.value.pinError)
        assertEquals("1234", repository.currentPin.value)
    }

    @Test
    fun testChangePinFailsWhenNewPinMatchesOldPin() {
        profileViewModel.openChangePinSheet()

        // Enter correct current PIN: 1234
        "1234".forEach { profileViewModel.onPinDigit(it.toString()) }
        assertEquals(ChangePinStep.NEW_PIN, profileViewModel.uiState.value.pinStep)

        // Enter same PIN: 1234
        "1234".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Should reject and remain on NEW_PIN step with error
        assertEquals(ChangePinStep.NEW_PIN, profileViewModel.uiState.value.pinStep)
        assertNotNull(profileViewModel.uiState.value.pinError)
        assertEquals("1234", repository.currentPin.value)
    }

    @Test
    fun testChangePinFailsOnMismatchedConfirmPin() {
        profileViewModel.openChangePinSheet()

        // Enter correct current PIN: 1234
        "1234".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Enter new PIN: 5678
        "5678".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Enter mismatched confirm PIN: 5679
        "5679".forEach { profileViewModel.onPinDigit(it.toString()) }

        // Should remain on CONFIRM_PIN step with error
        assertEquals(ChangePinStep.CONFIRM_PIN, profileViewModel.uiState.value.pinStep)
        assertNotNull(profileViewModel.uiState.value.pinError)
        assertEquals("1234", repository.currentPin.value)
    }

    @Test
    fun testToggleFreezeCard() {
        assertFalse(repository.isCardFrozen.value)

        profileViewModel.toggleCardFreeze()
        assertTrue(repository.isCardFrozen.value)

        profileViewModel.toggleCardFreeze()
        assertFalse(repository.isCardFrozen.value)
    }

    @Test
    fun testToggleRevealCardDetails() {
        assertFalse(profileViewModel.uiState.value.isCardDetailsRevealed)
        profileViewModel.toggleRevealCardDetails()
        assertTrue(profileViewModel.uiState.value.isCardDetailsRevealed)
    }

    @Test
    fun testPreferencesToggles() {
        profileViewModel.setBiometricsEnabled(false)
        assertFalse(repository.isBiometricsEnabled.value)

        profileViewModel.setNotificationsEnabled(false)
        assertFalse(repository.isNotificationsEnabled.value)

        profileViewModel.setCurrency("EUR")
        assertEquals("EUR", profileViewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun testResetDemoDataRestoresAllDefaults() {
        // Change profile, freeze card, and change PIN
        repository.updateProfile("Jane Doe", "jane@demo.com", "+1 000 000")
        repository.changePin("1234", "8888")
        repository.setCardFrozen(true)
        repository.setBiometricsEnabled(false)

        // Reset
        profileViewModel.resetDemoData()

        // Verify restoration
        assertEquals("Alex Morgan", repository.currentUser.value.fullName)
        assertEquals("1234", repository.currentPin.value)
        assertFalse(repository.isCardFrozen.value)
        assertTrue(repository.isBiometricsEnabled.value)
    }
}
