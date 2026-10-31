package com.example.e_wallet.ui.scan

import com.example.e_wallet.model.Beneficiary
import java.net.URLDecoder

/**
 * Parsed payload representing a scanned QR code ready for review and payment.
 */
data class ParsedQrPayment(
    val rawString: String,
    val receiverName: String,
    val receiverWalletNumber: String,
    val receiverId: String,
    /** Fixed amount specified in QR (in cents), or null if the user chooses the amount. */
    val presetAmountCents: Long? = null,
    val note: String = "",
    val isMerchant: Boolean = false,
    val avatarInitials: String = "QR",
    val merchantCategory: String = "Payment"
)

/**
 * Pre-configured interactive demo QR options that simulate real-world scanning scenarios.
 */
data class DemoQrPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val rawPayload: String
)

object QrCodeParser {

    val demoPresets = listOf(
        DemoQrPreset(
            id = "preset_starbucks",
            title = "Starbucks Coffee",
            subtitle = "Invoice • $8.50",
            iconEmoji = "☕",
            rawPayload = "pocketpay://merchant?id=mch_001&name=Starbucks%20Coffee&wallet=PKP-MCH-001&amount=850&note=Morning%20Caramel%20Macchiato%20☕"
        ),
        DemoQrPreset(
            id = "preset_mart",
            title = "PocketPay Mart",
            subtitle = "Invoice • $34.20",
            iconEmoji = "🛒",
            rawPayload = "pocketpay://merchant?id=mch_002&name=PocketPay%20Supermarket&wallet=PKP-MCH-002&amount=3420&note=Weekly%20Groceries%20🛒"
        ),
        DemoQrPreset(
            id = "preset_sarah",
            title = "Sarah Chen",
            subtitle = "Peer • Flexible Amount",
            iconEmoji = "👤",
            rawPayload = "pocketpay://receive?wallet=PKP-1049-5521&name=Sarah%20Chen"
        ),
        DemoQrPreset(
            id = "preset_cinema",
            title = "AMC Theatres",
            subtitle = "Invoice • $15.00",
            iconEmoji = "🎬",
            rawPayload = "pocketpay://merchant?id=mch_003&name=AMC%20Cinema&wallet=PKP-MCH-003&amount=1500&note=Movie%20Tickets%20🎟️"
        ),
        DemoQrPreset(
            id = "preset_david",
            title = "David Miller",
            subtitle = "Peer • Fixed $20.00",
            iconEmoji = "🤝",
            rawPayload = "pocketpay://receive?wallet=PKP-3392-8810&name=David%20Miller&amount=2000"
        )
    )

    /**
     * Parses any QR payload string into a structured [ParsedQrPayment].
     * Supports `pocketpay://receive`, `pocketpay://merchant`, `pocketpay://pay`,
     * raw wallet numbers, or custom arbitrary QR codes.
     */
    fun parse(rawString: String, knownBeneficiaries: List<Beneficiary> = emptyList()): ParsedQrPayment? {
        val trimmed = rawString.trim()
        if (trimmed.isBlank()) return null

        return try {
            when {
                trimmed.startsWith("pocketpay://", ignoreCase = true) -> {
                    parsePocketPayUri(trimmed, knownBeneficiaries)
                }
                trimmed.startsWith("PKP-", ignoreCase = true) -> {
                    parsePlainOrFallback(trimmed, knownBeneficiaries)
                }
                trimmed.startsWith("pay:", ignoreCase = true) || trimmed.startsWith("wallet:", ignoreCase = true) -> {
                    parsePlainOrFallback(trimmed, knownBeneficiaries)
                }
                else -> {
                    // Check if it matches a known contact/beneficiary by ID or wallet
                    val matchedBen = knownBeneficiaries.firstOrNull {
                        it.walletNumber.equals(trimmed, ignoreCase = true) ||
                        it.receiverUserId.equals(trimmed, ignoreCase = true)
                    }
                    if (matchedBen != null) {
                        parsePlainOrFallback(trimmed, knownBeneficiaries)
                    } else {
                        // Unrecognized format / invalid QR code
                        null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePocketPayUri(uriString: String, knownBeneficiaries: List<Beneficiary>): ParsedQrPayment {
        val withoutScheme = uriString.removePrefix("pocketpay://")
        val host = withoutScheme.substringBefore("?").removeSuffix("/").lowercase()
        val queryString = if (withoutScheme.contains("?")) withoutScheme.substringAfter("?") else ""

        val isMerchant = host == "merchant"

        val params = mutableMapOf<String, String>()
        if (queryString.isNotBlank()) {
            queryString.split("&").forEach { param ->
                val parts = param.split("=", limit = 2)
                if (parts.isNotEmpty()) {
                    val key = parts[0].lowercase()
                    val value = if (parts.size == 2) {
                        try {
                            URLDecoder.decode(parts[1], "UTF-8")
                        } catch (e: Exception) {
                            parts[1]
                        }
                    } else ""
                    params[key] = value
                }
            }
        }

        val wallet = params["wallet"].orEmpty()
        val name = params["name"].orEmpty()
        val id = params["id"].orEmpty()
        val note = params["note"].orEmpty()
        val amountStr = params["amount"]
        val amountCents = amountStr?.toLongOrNull()?.takeIf { it > 0 }

        // Match against known beneficiaries if name or id is missing
        val matchedBen = knownBeneficiaries.firstOrNull {
            it.walletNumber.equals(wallet, ignoreCase = true)
        }

        val resolvedName = when {
            name.isNotBlank() -> name
            matchedBen != null -> matchedBen.displayName
            isMerchant -> "Merchant Store"
            wallet.isNotBlank() -> "User ($wallet)"
            else -> "PocketPay Recipient"
        }

        val resolvedId = when {
            id.isNotBlank() -> id
            matchedBen != null -> matchedBen.receiverUserId
            isMerchant -> "mch_${wallet.hashCode().coerceAtLeast(0)}"
            else -> "usr_qr_${wallet.hashCode().coerceAtLeast(0)}"
        }

        val initials = resolvedName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { if (isMerchant) "MC" else "QR" }

        return ParsedQrPayment(
            rawString = uriString,
            receiverName = resolvedName,
            receiverWalletNumber = wallet.ifBlank { "PKP-UNKNOWN" },
            receiverId = resolvedId,
            presetAmountCents = amountCents,
            note = note,
            isMerchant = isMerchant,
            avatarInitials = initials,
            merchantCategory = if (isMerchant) "Retail / Merchant" else "Personal Transfer"
        )
    }

    private fun parsePlainOrFallback(raw: String, knownBeneficiaries: List<Beneficiary>): ParsedQrPayment {
        val matchedBen = knownBeneficiaries.firstOrNull {
            it.walletNumber.equals(raw, ignoreCase = true) ||
            it.receiverUserId.equals(raw, ignoreCase = true) ||
            it.displayName.equals(raw, ignoreCase = true)
        }

        return if (matchedBen != null) {
            ParsedQrPayment(
                rawString = raw,
                receiverName = matchedBen.displayName,
                receiverWalletNumber = matchedBen.walletNumber,
                receiverId = matchedBen.receiverUserId,
                presetAmountCents = null,
                note = "",
                isMerchant = false,
                avatarInitials = matchedBen.avatarInitials,
                merchantCategory = "Favorite Contact"
            )
        } else {
            val isLikelyWallet = raw.startsWith("PKP-", ignoreCase = true)
            val name = if (isLikelyWallet) "PocketPay User" else raw.take(20)
            ParsedQrPayment(
                rawString = raw,
                receiverName = name,
                receiverWalletNumber = if (isLikelyWallet) raw else "PKP-DEMO-${raw.hashCode().coerceAtLeast(1000)}",
                receiverId = "usr_qr_${raw.hashCode().coerceAtLeast(0)}",
                presetAmountCents = null,
                note = "",
                isMerchant = false,
                avatarInitials = name.take(2).uppercase(),
                merchantCategory = "QR Payee"
            )
        }
    }
}
