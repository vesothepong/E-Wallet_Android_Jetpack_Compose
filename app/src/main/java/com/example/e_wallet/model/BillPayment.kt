package com.example.e_wallet.model

/**
 * Categories of supported billers and utilities in PocketPay.
 * Each category defines descriptive metadata for UI presentation and form prompt guidance.
 */
enum class BillCategory(
    val title: String,
    val prompt: String,
    val exampleHint: String
) {
    ELECTRICITY(
        title = "Electricity",
        prompt = "Meter / Customer Number",
        exampleHint = "e.g. ELEC-992144"
    ),
    WATER(
        title = "Water",
        prompt = "Water Supply Account No.",
        exampleHint = "e.g. WTR-440182"
    ),
    INTERNET(
        title = "Internet & Cable",
        prompt = "Broadband Account ID",
        exampleHint = "e.g. FIB-881920"
    ),
    MOBILE_TOPUP(
        title = "Mobile Top-Up",
        prompt = "Mobile Phone Number",
        exampleHint = "e.g. +1 (555) 019-2834"
    ),
    EDUCATION(
        title = "Education & Tuition",
        prompt = "Student ID / Enrollment No.",
        exampleHint = "e.g. STU-2026-991"
    ),
    GOVERNMENT(
        title = "Government & Taxes",
        prompt = "Tax Reference / Notice ID",
        exampleHint = "e.g. TAX-778819"
    )
}

/**
 * Represents a registered utility provider, telecom operator, or merchant biller.
 *
 * @param billerId Unique identifier of the provider.
 * @param name Official commercial name of the biller.
 * @param category Functional category (e.g., electricity, water, internet, mobile top-up).
 * @param iconName Symbolic key matching vector icon mapping in PocketPayIcons.
 * @param accountHint Helpful placeholder shown to the user in input fields.
 * @param feeCents Convenience or processing fee in cents (0L indicates fee-free bill payment).
 * @param standardAmountsCents Suggested preset bill amounts in cents to speed up payment.
 * @param isPopular Whether this biller is highlighted on the primary quick-pay dashboard.
 * @param providerCode Short code or billing identifier used in generated receipt references.
 */
data class Biller(
    val billerId: String,
    val name: String,
    val category: BillCategory,
    val iconName: String,
    val accountHint: String,
    val feeCents: Long = 0L,
    val standardAmountsCents: List<Long> = listOf(1500L, 2500L, 5000L, 10000L),
    val isPopular: Boolean = true,
    val providerCode: String = ""
)

/**
 * Result of attempting a simulated bill payment transaction.
 */
sealed class BillPaymentResult {
    /**
     * Bill payment succeeded: balances were updated and a ledger transaction was recorded.
     *
     * @param transaction The newly recorded transaction in the ledger.
     * @param newBalance The updated wallet balance in cents.
     * @param billerName Name of the service provider that received payment.
     * @param referenceNumber Generated reference code for tracking and customer service.
     */
    data class Success(
        val transaction: Transaction,
        val newBalance: Long,
        val billerName: String,
        val referenceNumber: String
    ) : BillPaymentResult()

    /**
     * Bill payment failed due to validation, insufficient balance, frozen card, or network simulation.
     *
     * @param message User-friendly error explanation.
     */
    data class Error(val message: String) : BillPaymentResult()
}
