# PocketPay Cambodian Banking & NBC Bakong KHQR Server

This backend server connects your PocketPay Android e-wallet app to **Cambodian Banks** and the **National Bank of Cambodia (NBC) Bakong / KHQR** network.

---

## 🚀 Quick Start

Run the server with Python:

```bash
cd backend
python3 server.py
```

The server will automatically start on `http://0.0.0.0:8080`.

* **Android Emulator endpoint**: `http://10.0.2.2:8080`
* **Physical Device (via Wi-Fi)**: `http://<your-computer-ip>:8080`

---

## 🏦 Configuring Your Real Bakong Merchant Account

Open the `.env` file in this directory and replace the placeholders with your real credentials:

```ini
# NBC Bakong Open API (Production or Sandbox)
BAKONG_API_URL=https://api-bakong.nbc.gov.kh/v1

# Your Bakong Bearer Token (obtain from NBC developer portal or partner bank)
BAKONG_API_TOKEN=your_bakong_developer_token_here

# Your Cambodian Merchant Details
BAKONG_MERCHANT_ID=mch_001
BAKONG_ACCOUNT_ID=vesothepong@abaa
BAKONG_MERCHANT_NAME=PocketPay Store
BAKONG_MERCHANT_CITY=Phnom Penh
BAKONG_ACQUIRING_BANK=ABA Bank
CURRENCY=USD
```

* If `BAKONG_API_TOKEN` is left as default, the server runs in **Sandbox / Simulation Mode** so you can test all deposits, withdrawals, and QR payments immediately without errors.
* When you paste your real Bakong JWT token, the server automatically queries NBC Bakong's live Open API (`check_transaction_by_md5`).

---

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Server status and Bakong config info |
| `GET` | `/api/v1/wallet/balance` | Returns wallet balance in cents and currency |
| `GET` | `/api/v1/wallet/transactions` | Recent transactions list |
| `GET` | `/api/v1/bank/accounts` | Linked Cambodian bank accounts (ABA, Wing, ACLEDA, etc.) |
| `POST` | `/api/v1/bank/link` | Links a Cambodian bank account via Bakong ID or Account Number |
| `POST` | `/api/v1/bank/khqr/generate` | Generates official EMVCo KHQR string with CRC-16 checksum |
| `POST` | `/api/v1/bank/khqr/check` | Queries Bakong Open API for transaction status by MD5 hash |
| `POST` | `/api/v1/wallet/deposit` | Deposits funds into wallet from linked Cambodian bank account |
| `POST` | `/api/v1/wallet/withdraw` | Withdraws funds to Cambodian bank account |
| `POST` | `/api/v1/wallet/transfer` | Transfers funds to another Bakong ID / phone number |
| `POST` | `/api/v1/webhook/bakong` | Receives live payment push notifications from NBC Bakong |

---

## 🧪 Testing with cURL

1. **Check Server Health**:
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

2. **Generate a KHQR**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/bank/khqr/generate \
     -H "Content-Type: application/json" \
     -d '{"amount_cents": 1250, "merchant_name": "Brown Coffee", "bakong_account_id": "vesothepong@abaa"}'
   ```

3. **Deposit from Cambodian Bank**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet/deposit \
     -H "Content-Type: application/json" \
     -d '{"amount_cents": 5000, "source_description": "ABA Bank (Bakong)", "note": "Top up from ABA"}'
   ```
