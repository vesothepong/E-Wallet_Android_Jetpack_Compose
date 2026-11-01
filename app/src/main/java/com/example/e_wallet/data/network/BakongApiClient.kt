package com.example.e_wallet.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "BakongApiClient"

/**
 * High-performance, coroutine-based REST client for the PocketPay Cambodian Banking & Bakong Server.
 * Connects to http://10.0.2.2:8080 on Android Emulator, or your local LAN IP on physical devices.
 */
object BakongApiClient {

    /**
     * Base URL for the backend server.
     * Default: 10.0.2.2 points to host machine from Android Emulator.
     * Switch to http://192.168.1.X:8080 when running on a physical phone on Wi-Fi.
     */
    var baseUrl: String = "http://10.0.2.2:8080/api/v1"

    data class ServerHealth(
        val isOnline: Boolean,
        val bakongConfigured: Boolean,
        val bakongAccount: String,
        val merchantName: String,
        val currency: String
    )

    data class NetworkBankAccount(
        val id: String,
        val bankName: String,
        val accountNumberMasked: String,
        val bakongAccountId: String,
        val accountType: String,
        val isPrimary: Boolean,
        val balanceCents: Long
    )

    data class NetworkTransaction(
        val id: String,
        val type: String,
        val amountCents: Long,
        val receiverName: String,
        val receiverWallet: String,
        val referenceNumber: String,
        val status: String,
        val timestampMs: Long,
        val note: String,
        val category: String
    )

    data class DepositResultData(
        val success: Boolean,
        val newBalanceCents: Long,
        val transactionId: String,
        val referenceNumber: String
    )

    data class WithdrawResultData(
        val success: Boolean,
        val newBalanceCents: Long,
        val transactionId: String,
        val referenceNumber: String
    )

    data class KhqrGenerationResult(
        val khqrPayload: String,
        val md5Hash: String,
        val bakongAccount: String,
        val amountCents: Long,
        val currency: String
    )

    /** Check if the backend server is reachable */
    suspend fun checkHealth(): ServerHealth = withContext(Dispatchers.IO) {
        try {
            val json = getRequest("$baseUrl/health")
            ServerHealth(
                isOnline = json.optString("status") == "ONLINE",
                bakongConfigured = json.optBoolean("bakong_configured", false),
                bakongAccount = json.optString("bakong_account", ""),
                merchantName = json.optString("merchant_name", ""),
                currency = json.optString("currency", "USD")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable: ${e.message}")
            ServerHealth(
                isOnline = false,
                bakongConfigured = false,
                bakongAccount = "",
                merchantName = "",
                currency = "USD"
            )
        }
    }

    /** Fetch current wallet balance */
    suspend fun getWalletBalance(): Pair<Long, String>? = withContext(Dispatchers.IO) {
        try {
            val json = getRequest("$baseUrl/wallet/balance")
            val balance = json.optLong("balance_cents", 0L)
            val currency = json.optString("currency", "USD")
            Pair(balance, currency)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get balance: ${e.message}")
            null
        }
    }

    /** Fetch linked Cambodian bank accounts */
    suspend fun getBankAccounts(): List<NetworkBankAccount> = withContext(Dispatchers.IO) {
        try {
            val json = getRequest("$baseUrl/bank/accounts")
            val arr = json.optJSONArray("accounts") ?: JSONArray()
            val list = mutableListOf<NetworkBankAccount>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(
                    NetworkBankAccount(
                        id = item.optString("id"),
                        bankName = item.optString("bank_name"),
                        accountNumberMasked = item.optString("account_number_masked"),
                        bakongAccountId = item.optString("bakong_account_id"),
                        accountType = item.optString("account_type", "Checking"),
                        isPrimary = item.optBoolean("is_primary", false),
                        balanceCents = item.optLong("balance_cents", 0L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch bank accounts: ${e.message}")
            emptyList()
        }
    }

    /** Link a Cambodian bank account via Bakong ID or Account Number */
    suspend fun linkBankAccount(
        bankName: String,
        bakongAccountId: String,
        accountNumber: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("bank_name", bankName)
                put("bakong_account_id", bakongAccountId)
                put("account_number", accountNumber)
            }
            val res = postRequest("$baseUrl/bank/link", body)
            res.optBoolean("success", false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to link bank account: ${e.message}")
            false
        }
    }

    /** Deposit money from linked Cambodian bank account into wallet */
    suspend fun deposit(
        amountCents: Long,
        sourceDescription: String,
        note: String
    ): DepositResultData = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("amount_cents", amountCents)
            put("source_description", sourceDescription)
            put("note", note)
        }
        val res = postRequest("$baseUrl/wallet/deposit", body)
        val tx = res.optJSONObject("transaction")
        DepositResultData(
            success = res.optBoolean("success", false),
            newBalanceCents = res.optLong("new_balance_cents", 0L),
            transactionId = tx?.optString("id") ?: "tx_dep_${System.currentTimeMillis()}",
            referenceNumber = tx?.optString("reference_number") ?: "REF-DEP"
        )
    }

    /** Withdraw money from wallet to Cambodian bank account */
    suspend fun withdraw(
        amountCents: Long,
        destinationDescription: String,
        note: String
    ): WithdrawResultData = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("amount_cents", amountCents)
            put("destination_description", destinationDescription)
            put("note", note)
        }
        val res = postRequest("$baseUrl/wallet/withdraw", body)
        val tx = res.optJSONObject("transaction")
        WithdrawResultData(
            success = res.optBoolean("success", false),
            newBalanceCents = res.optLong("new_balance_cents", 0L),
            transactionId = tx?.optString("id") ?: "tx_wth_${System.currentTimeMillis()}",
            referenceNumber = tx?.optString("reference_number") ?: "REF-WTH"
        )
    }

    /** Transfer / Pay QR to Bakong Payee */
    suspend fun transfer(
        amountCents: Long,
        receiverName: String,
        receiverWallet: String,
        note: String
    ): Pair<Long, String> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("amount_cents", amountCents)
            put("receiver_name", receiverName)
            put("receiver_wallet", receiverWallet)
            put("note", note)
        }
        val res = postRequest("$baseUrl/wallet/transfer", body)
        val tx = res.optJSONObject("transaction")
        val newBal = res.optLong("new_balance_cents", 0L)
        val ref = tx?.optString("reference_number") ?: "REF-TRANSFER"
        Pair(newBal, ref)
    }

    /** Generate Bakong KHQR EMVCo string */
    suspend fun generateKhqr(
        amountCents: Long,
        merchantName: String,
        bakongAccountId: String
    ): KhqrGenerationResult? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("amount_cents", amountCents)
                put("merchant_name", merchantName)
                put("bakong_account_id", bakongAccountId)
            }
            val res = postRequest("$baseUrl/bank/khqr/generate", body)
            KhqrGenerationResult(
                khqrPayload = res.getString("khqr_payload"),
                md5Hash = res.getString("md5_hash"),
                bakongAccount = res.getString("bakong_account"),
                amountCents = res.getLong("amount_cents"),
                currency = res.optString("currency", "USD")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate KHQR: ${e.message}")
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP LOW-LEVEL HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun getRequest(urlStr: String): JSONObject {
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 4000
            readTimeout = 6000
            setRequestProperty("Accept", "application/json")
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        conn.disconnect()

        if (code !in 200..299) {
            throw RuntimeException("HTTP $code: $response")
        }
        return JSONObject(response)
    }

    private fun postRequest(urlStr: String, body: JSONObject): JSONObject {
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 4000
            readTimeout = 6000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use {
            it.write(body.toString())
            it.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        conn.disconnect()

        if (code !in 200..299) {
            throw RuntimeException("HTTP $code: $response")
        }
        return JSONObject(response)
    }
}
