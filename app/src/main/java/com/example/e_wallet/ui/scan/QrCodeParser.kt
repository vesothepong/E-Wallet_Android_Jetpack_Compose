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
            id = "preset_brown_coffee",
            title = "Brown Coffee (Bakong KHQR)",
            subtitle = "KHQR • $4.50",
            iconEmoji = "🇰🇭",
            rawPayload = "00020101021229340010bakong@nbc0116vesothepong@abaa52045411530384054044.505802KH5912Brown Coffee6010Phnom Penh6304CA27"
        ),
        DemoQrPreset(
            id = "preset_aba_merchant",
            title = "ABA Merchant (Bakong KHQR)",
            subtitle = "KHQR • $12.50",
            iconEmoji = "🏦",
            rawPayload = "00020101021229340010bakong@nbc0116vesothepong@abaa520454115303840540512.505802KH5912ABA Merchant6010Phnom Penh630438FE"
        ),
        DemoQrPreset(
            id = "preset_wing_mall",
            title = "Wing Mall (Bakong KHQR)",
            subtitle = "KHQR • Flexible",
            iconEmoji = "🛍️",
            rawPayload = "00020101021129320010bakong@nbc0114012888999@wing5204541153038405802KH5909Wing Mall6010Phnom Penh6304EE88"
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
        )
    )

    /**
     * Parses any QR payload string into a structured [ParsedQrPayment].
     * Supports:
     * - NBC Bakong KHQR (EMVCo Merchant Presented Mode `000201...`)
     * - `pocketpay://receive`, `pocketpay://merchant`, `pocketpay://pay`
     * - Raw wallet numbers (`PKP-...`)
     * - Bakong Account IDs (`...@abaa`, `...@wing`, `...@aclb`)
     */
    fun parse(rawString: String, knownBeneficiaries: List<Beneficiary> = emptyList()): ParsedQrPayment? {
        val trimmed = rawString.trim()
        if (trimmed.isBlank()) return null

        return try {
            when {
                // 1. National Bank of Cambodia (NBC) Bakong KHQR EMVCo format
                trimmed.startsWith("000201") -> {
                    parseKhqr(trimmed)
                }
                // 2. PocketPay URI scheme
                trimmed.startsWith("pocketpay://", ignoreCase = true) -> {
                    parsePocketPayUri(trimmed, knownBeneficiaries)
                }
                // 3. Raw PocketPay wallet
                trimmed.startsWith("PKP-", ignoreCase = true) -> {
                    parsePlainOrFallback(trimmed, knownBeneficiaries)
                }
                // 4. Bakong Account IDs directly (e.g. vesothepong@abaa, 012888999@wing)
                trimmed.contains("@abaa", ignoreCase = true) ||
                trimmed.contains("@wing", ignoreCase = true) ||
                trimmed.contains("@aclb", ignoreCase = true) ||
                trimmed.contains("@bakong", ignoreCase = true) -> {
                    parseDirectBakongAccount(trimmed)
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

    /**
     * Decodes NBC Bakong KHQR (EMVCo Merchant Presented Mode standard).
     * Standard EMVCo Tag-Length-Value format.
     */
    fun parseKhqr(raw: String): ParsedQrPayment? {
        if (!raw.startsWith("000201")) return null

        val tags = mutableMapOf<String, String>()
        var i = 0
        while (i + 4 <= raw.length) {
            val tag = raw.substring(i, i + 2)
            val len = raw.substring(i + 2, i + 4).toIntOrNull() ?: break
            val start = i + 4
            val end = start + len
            if (end > raw.length) break
            tags[tag] = raw.substring(start, end)
            i = end
        }

        // Tag 29 (Individual) or Tag 30 (Merchant)
        val accountData = tags["29"] ?: tags["30"] ?: ""
        val bakongAccount = extractBakongAccount(accountData).ifBlank { "khqr@bakong" }

        val merchantName = tags["59"] ?: "Bakong KHQR Merchant"
        val city = tags["60"] ?: "Phnom Penh"
        val currencyCode = tags["53"] ?: "840" // 840 = USD, 116 = KHR
        val amountStr = tags["54"]
        val amountCents = amountStr?.toDoubleOrNull()?.let { (it * 100).toLong() }?.takeIf { it > 0 }

        val initials = merchantName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "KH" }

        val note = if (currencyCode == "116") "Bakong KHQR (KHR, $city)" else "Bakong KHQR ($city)"

        return ParsedQrPayment(
            rawString = raw,
            receiverName = merchantName,
            receiverWalletNumber = bakongAccount,
            receiverId = "bakong_${bakongAccount.replace("@", "_")}",
            presetAmountCents = amountCents,
            note = note,
            isMerchant = true,
            avatarInitials = initials,
            merchantCategory = "Bakong KHQR Payment"
        )
    }

    private fun extractBakongAccount(rawAccountData: String): String {
        if (rawAccountData.contains("@")) {
            val parts = rawAccountData.split(Regex("[^a-zA-Z0-9@_.-]+"))
            val match = parts.firstOrNull { it.contains("@") && it.length > 3 }
            if (match != null) return match
        }

        var i = 0
        while (i + 4 <= rawAccountData.length) {
            val subTag = rawAccountData.substring(i, i + 2)
            val subLen = rawAccountData.substring(i + 2, i + 4).toIntOrNull() ?: break
            val start = i + 4
            val end = start + subLen
            if (end > rawAccountData.length) break
            val subVal = rawAccountData.substring(start, end)
            if (subTag == "01") return subVal
            i = end
        }
        return rawAccountData
    }

    private fun parseDirectBakongAccount(rawAccount: String): ParsedQrPayment {
        val bankLabel = when {
            rawAccount.endsWith("@abaa", ignoreCase = true) -> "ABA Bank"
            rawAccount.endsWith("@wing", ignoreCase = true) -> "Wing Bank"
            rawAccount.endsWith("@aclb", ignoreCase = true) -> "ACLEDA Bank"
            else -> "Bakong Member"
        }
        return ParsedQrPayment(
            rawString = rawAccount,
            receiverName = "$bankLabel ($rawAccount)",
            receiverWalletNumber = rawAccount,
            receiverId = "bakong_${rawAccount.replace("@", "_")}",
            presetAmountCents = null,
            note = "Direct Bakong Transfer",
            isMerchant = false,
            avatarInitials = "BK",
            merchantCategory = "Cambodian Bank Transfer"
        )
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
