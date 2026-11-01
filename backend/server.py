#!/usr/bin/env python3
"""
E-Wallet Cambodian Banking & Bakong KHQR Backend Server
-------------------------------------------------------
Supports:
- NBC Bakong KHQR EMVCo generation with CRC-16 checksum
- NBC Bakong Open API payment checking (check_transaction_by_md5/hash)
- Cambodian Bank account linking (ABA Bank, Wing Bank, ACLEDA Bank, Canadia Bank, etc.)
- Wallet deposit, withdrawal, and transfer APIs
- Webhook receiver for Bakong instant payment notifications
- Persistent SQLite storage for wallet balances, accounts, and transactions
- Graceful sandbox mode: works out of the box, switches to live NBC Bakong when API token is configured
"""

import os
import sys
import json
import sqlite3
import hashlib
import time
import urllib.request
import urllib.error
from http.server import HTTPServer, BaseHTTPRequestHandler
from socketserver import ThreadingMixIn

# ─────────────────────────────────────────────────────────────────────────────
# CONFIGURATION (.env or environment variables)
# ─────────────────────────────────────────────────────────────────────────────

def load_env_file():
    env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
    if os.path.exists(env_path):
        with open(env_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    key, val = line.split("=", 1)
                    os.environ.setdefault(key.strip(), val.strip())

load_env_file()

PORT = int(os.environ.get("PORT", "8080"))
BAKONG_API_URL = os.environ.get("BAKONG_API_URL", "https://api-bakong.nbc.gov.kh/v1")
BAKONG_API_TOKEN = os.environ.get("BAKONG_API_TOKEN", "")
BAKONG_MERCHANT_ID = os.environ.get("BAKONG_MERCHANT_ID", "mch_001")
BAKONG_ACCOUNT_ID = os.environ.get("BAKONG_ACCOUNT_ID", "vesothepong@abaa")
BAKONG_MERCHANT_NAME = os.environ.get("BAKONG_MERCHANT_NAME", "PocketPay Merchant")
BAKONG_MERCHANT_CITY = os.environ.get("BAKONG_MERCHANT_CITY", "Phnom Penh")
BAKONG_ACQUIRING_BANK = os.environ.get("BAKONG_ACQUIRING_BANK", "ABA Bank")
CURRENCY = os.environ.get("CURRENCY", "USD")

DB_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "wallet.db")

# ─────────────────────────────────────────────────────────────────────────────
# DATABASE INITIALIZATION
# ─────────────────────────────────────────────────────────────────────────────

def get_db():
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    with conn:
        conn.executescript("""
        CREATE TABLE IF NOT EXISTS wallet (
            id TEXT PRIMARY KEY,
            wallet_number TEXT UNIQUE,
            balance_cents INTEGER NOT NULL,
            currency TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS bank_accounts (
            id TEXT PRIMARY KEY,
            bank_name TEXT NOT NULL,
            account_number_masked TEXT NOT NULL,
            bakong_account_id TEXT NOT NULL,
            account_type TEXT NOT NULL,
            is_primary INTEGER DEFAULT 0,
            balance_cents INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS transactions (
            id TEXT PRIMARY KEY,
            type TEXT NOT NULL,
            amount_cents INTEGER NOT NULL,
            receiver_name TEXT NOT NULL,
            receiver_wallet TEXT NOT NULL,
            reference_number TEXT NOT NULL,
            status TEXT NOT NULL,
            timestamp_ms INTEGER NOT NULL,
            note TEXT NOT NULL,
            category TEXT NOT NULL
        );
        """)

        # Seed default wallet if empty
        row = conn.execute("SELECT COUNT(*) AS count FROM wallet").fetchone()
        if row["count"] == 0:
            conn.execute(
                "INSERT INTO wallet (id, wallet_number, balance_cents, currency) VALUES (?, ?, ?, ?)",
                ("usr_001", "PKP-1049-5521", 1245080, "USD") # $12,450.80
            )

        # Seed default Cambodian bank accounts if empty
        row_b = conn.execute("SELECT COUNT(*) AS count FROM bank_accounts").fetchone()
        if row_b["count"] == 0:
            conn.executemany(
                """INSERT INTO bank_accounts (id, bank_name, account_number_masked, bakong_account_id, account_type, is_primary, balance_cents)
                   VALUES (?, ?, ?, ?, ?, ?, ?)""",
                [
                    ("bnk_aba_01", "ABA Bank (Bakong)", "•••• 8821", "vesothepong@abaa", "Checking", 1, 850000),
                    ("bnk_wing_02", "Wing Bank (Bakong)", "•••• 3302", "012888999@wing", "Savings", 0, 1450000),
                    ("bnk_acleda_03", "ACLEDA Bank", "•••• 7714", "acleda_user@aclb", "Checking", 0, 620000)
                ]
            )
    conn.close()

# ─────────────────────────────────────────────────────────────────────────────
# KHQR EMVCo SPECIFICATION & CRC16 CHECKSUM
# ─────────────────────────────────────────────────────────────────────────────

def calculate_crc16_ccitt(data: str) -> str:
    """
    Official EMVCo / NBC Bakong CRC16-CCITT algorithm (Polynomial: 0x1021, Initial: 0xFFFF)
    """
    crc = 0xFFFF
    for byte in data.encode("utf-8"):
        crc ^= (byte << 8)
        for _ in range(8):
            if crc & 0x8000:
                crc = ((crc << 1) ^ 0x1021) & 0xFFFF
            else:
                crc = (crc << 1) & 0xFFFF
    return f"{crc:04X}"

def tlv(tag: str, value: str) -> str:
    length = f"{len(value):02d}"
    return f"{tag}{length}{value}"

def generate_bakong_khqr(
    bakong_account_id: str,
    merchant_name: str,
    amount_dollars: float = None,
    currency_code: str = "840", # 840 = USD, 116 = KHR
    city: str = "Phnom Penh",
    bill_number: str = ""
) -> str:
    """
    Generates an EMVCo compliant KHQR payload for Bakong payment.
    """
    # Tag 00: Payload Format Indicator = '01'
    payload = tlv("00", "01")

    # Tag 01: Point of Initiation Method: 11 = Static, 12 = Dynamic
    point_of_initiation = "12" if (amount_dollars and amount_dollars > 0) else "11"
    payload += tlv("01", point_of_initiation)

    # Tag 29: Merchant Account Information for Individual or Merchant Bakong
    # Sub-tag 00 = 'bakong@nbc', Sub-tag 01 = Account ID
    account_info = tlv("00", "bakong@nbc") + tlv("01", bakong_account_id)
    payload += tlv("29", account_info)

    # Tag 52: Merchant Category Code (General: 5411)
    payload += tlv("52", "5411")

    # Tag 53: Transaction Currency (840 = USD, 116 = KHR)
    payload += tlv("53", currency_code)

    # Tag 54: Transaction Amount
    if amount_dollars and amount_dollars > 0:
        amount_str = f"{amount_dollars:.2f}"
        payload += tlv("54", amount_str)

    # Tag 58: Country Code (KH)
    payload += tlv("58", "KH")

    # Tag 59: Merchant Name
    payload += tlv("59", merchant_name[:25])

    # Tag 60: Merchant City
    payload += tlv("60", city[:15])

    # Tag 62: Additional Data Field (Bill Number / Reference)
    if bill_number:
        sub_data = tlv("01", bill_number[:25])
        payload += tlv("62", sub_data)

    # Tag 63: CRC16 Checksum
    prefix_for_crc = payload + "6304"
    checksum = calculate_crc16_ccitt(prefix_for_crc)
    return prefix_for_crc + checksum

# ─────────────────────────────────────────────────────────────────────────────
# BAKONG OPEN API INTERACTION
# ─────────────────────────────────────────────────────────────────────────────

def call_bakong_check_transaction(md5_hash: str):
    """
    Calls the official NBC Bakong Open API endpoint: /check_transaction_by_md5.
    If BAKONG_API_TOKEN is empty, falls back gracefully to simulated success.
    """
    if not BAKONG_API_TOKEN or BAKONG_API_TOKEN == "your_bakong_developer_token_here":
        return {
            "status": "SIMULATED",
            "is_paid": True,
            "message": "Bakong token not configured; simulated sandbox confirmation",
            "hash": md5_hash
        }

    url = f"{BAKONG_API_URL.rstrip('/')}/check_transaction_by_md5"
    headers = {
        "Authorization": f"Bearer {BAKONG_API_TOKEN}",
        "Content-Type": "application/json"
    }
    body = json.dumps({"md5": md5_hash}).encode("utf-8")

    req = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return {
                "status": "SUCCESS",
                "is_paid": data.get("responseCode") == 0,
                "data": data
            }
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        return {
            "status": "ERROR",
            "is_paid": False,
            "error": f"Bakong HTTP {e.code}: {err_msg}"
        }
    except Exception as e:
        return {
            "status": "ERROR",
            "is_paid": False,
            "error": str(e)
        }

# ─────────────────────────────────────────────────────────────────────────────
# HTTP REQUEST HANDLER
# ─────────────────────────────────────────────────────────────────────────────

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True

class WalletApiHandler(BaseHTTPRequestHandler):

    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def send_json(self, status_code: int, data: dict):
        body = json.dumps(data, indent=2).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(body)

    def parse_json_body(self):
        content_len = int(self.headers.get("Content-Length", 0))
        if content_len == 0:
            return {}
        raw = self.rfile.read(content_len).decode("utf-8")
        return json.loads(raw)

    def do_GET(self):
        path = self.path.split("?")[0].rstrip("/")

        if path in ("", "/api/v1/health"):
            self.send_json(200, {
                "status": "ONLINE",
                "service": "PocketPay Cambodian Banking & Bakong KHQR Server",
                "version": "1.0.0",
                "bakong_configured": bool(BAKONG_API_TOKEN and BAKONG_API_TOKEN != "your_bakong_developer_token_here"),
                "bakong_account": BAKONG_ACCOUNT_ID,
                "merchant_name": BAKONG_MERCHANT_NAME,
                "currency": CURRENCY
            })
            return

        conn = get_db()
        try:
            if path == "/api/v1/wallet/balance":
                row = conn.execute("SELECT * FROM wallet LIMIT 1").fetchone()
                self.send_json(200, {
                    "wallet_number": row["wallet_number"],
                    "balance_cents": row["balance_cents"],
                    "currency": row["currency"]
                })
                return

            if path == "/api/v1/bank/accounts":
                rows = conn.execute("SELECT * FROM bank_accounts ORDER BY is_primary DESC").fetchall()
                accounts = [
                    {
                        "id": r["id"],
                        "bank_name": r["bank_name"],
                        "account_number_masked": r["account_number_masked"],
                        "bakong_account_id": r["bakong_account_id"],
                        "account_type": r["account_type"],
                        "is_primary": bool(r["is_primary"]),
                        "balance_cents": r["balance_cents"]
                    }
                    for r in rows
                ]
                self.send_json(200, {"accounts": accounts})
                return

            if path == "/api/v1/wallet/transactions":
                rows = conn.execute("SELECT * FROM transactions ORDER BY timestamp_ms DESC LIMIT 50").fetchall()
                txs = [
                    {
                        "id": r["id"],
                        "type": r["type"],
                        "amount_cents": r["amount_cents"],
                        "receiver_name": r["receiver_name"],
                        "receiver_wallet": r["receiver_wallet"],
                        "reference_number": r["reference_number"],
                        "status": r["status"],
                        "timestamp_ms": r["timestamp_ms"],
                        "note": r["note"],
                        "category": r["category"]
                    }
                    for r in rows
                ]
                self.send_json(200, {"transactions": txs})
                return

            self.send_json(404, {"error": "Not found"})
        finally:
            conn.close()

    def do_POST(self):
        path = self.path.split("?")[0].rstrip("/")
        body = self.parse_json_body()
        conn = get_db()

        try:
            # 1. Generate KHQR for Bakong Payment
            if path == "/api/v1/bank/khqr/generate":
                amount_cents = body.get("amount_cents", 0)
                amount_dollars = (amount_cents / 100.0) if amount_cents > 0 else None
                bakong_acc = body.get("bakong_account_id") or BAKONG_ACCOUNT_ID
                merchant = body.get("merchant_name") or BAKONG_MERCHANT_NAME
                bill = body.get("bill_number") or f"INV-{int(time.time())}"

                qr_payload = generate_bakong_khqr(
                    bakong_account_id=bakong_acc,
                    merchant_name=merchant,
                    amount_dollars=amount_dollars,
                    currency_code="840" if CURRENCY == "USD" else "116",
                    city=BAKONG_MERCHANT_CITY,
                    bill_number=bill
                )
                md5_hash = hashlib.md5(qr_payload.encode("utf-8")).hexdigest()

                self.send_json(200, {
                    "khqr_payload": qr_payload,
                    "md5_hash": md5_hash,
                    "bakong_account": bakong_acc,
                    "amount_cents": amount_cents,
                    "currency": CURRENCY
                })
                return

            # 2. Check KHQR Payment Status
            if path == "/api/v1/bank/khqr/check":
                md5_hash = body.get("md5_hash") or ""
                result = call_bakong_check_transaction(md5_hash)
                self.send_json(200, result)
                return

            # 3. Link Real Cambodian Bank Account
            if path == "/api/v1/bank/link":
                bank_name = body.get("bank_name", "ABA Bank (Bakong)")
                bakong_acc = body.get("bakong_account_id", "").strip()
                account_num = body.get("account_number", "").strip()

                if not bakong_acc and not account_num:
                    self.send_json(400, {"error": "Bakong Account ID or Account Number required"})
                    return

                new_id = f"bnk_{int(time.time())}"
                masked = f"•••• {account_num[-4:]}" if len(account_num) >= 4 else (bakong_acc or "•••• 9988")
                initial_balance = 500000 # $5,000.00 mock bank balance

                with conn:
                    conn.execute(
                        """INSERT INTO bank_accounts (id, bank_name, account_number_masked, bakong_account_id, account_type, is_primary, balance_cents)
                           VALUES (?, ?, ?, ?, ?, ?, ?)""",
                        (new_id, bank_name, masked, bakong_acc or f"user_{new_id}@abaa", "Checking", 0, initial_balance)
                    )

                self.send_json(200, {
                    "success": True,
                    "message": f"Successfully linked {bank_name} ({bakong_acc})",
                    "account": {
                        "id": new_id,
                        "bank_name": bank_name,
                        "account_number_masked": masked,
                        "bakong_account_id": bakong_acc,
                        "balance_cents": initial_balance
                    }
                })
                return

            # 4. Deposit from Cambodian Bank / Bakong into Wallet
            if path == "/api/v1/wallet/deposit":
                amount_cents = int(body.get("amount_cents", 0))
                note = body.get("note", "Bakong Bank Top-up")
                source = body.get("source_description", "ABA Bank (Bakong)")

                if amount_cents <= 0:
                    self.send_json(400, {"error": "Invalid deposit amount"})
                    return

                with conn:
                    # Increment wallet balance
                    conn.execute("UPDATE wallet SET balance_cents = balance_cents + ? WHERE id = 'usr_001'", (amount_cents,))
                    updated_wallet = conn.execute("SELECT * FROM wallet WHERE id = 'usr_001'").fetchone()

                    # Record transaction
                    tx_id = f"tx_dep_{int(time.time() * 1000)}"
                    ref = f"REF-DEP-KH-{int(time.time())}"
                    now_ms = int(time.time() * 1000)

                    conn.execute(
                        """INSERT INTO transactions (id, type, amount_cents, receiver_name, receiver_wallet, reference_number, status, timestamp_ms, note, category)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        (tx_id, "DEPOSIT", amount_cents, "PocketPay Wallet", updated_wallet["wallet_number"], ref, "COMPLETED", now_ms, note, "Top Up")
                    )

                self.send_json(200, {
                    "success": True,
                    "new_balance_cents": updated_wallet["balance_cents"],
                    "transaction": {
                        "id": tx_id,
                        "type": "DEPOSIT",
                        "amount_cents": amount_cents,
                        "reference_number": ref,
                        "source": source,
                        "status": "COMPLETED",
                        "timestamp_ms": now_ms
                    }
                })
                return

            # 5. Withdraw to Cambodian Bank / Bakong Account
            if path == "/api/v1/wallet/withdraw":
                amount_cents = int(body.get("amount_cents", 0))
                destination = body.get("destination_description", "ABA Bank Checking")
                note = body.get("note", "Withdrawal to Cambodian Bank")

                row = conn.execute("SELECT * FROM wallet WHERE id = 'usr_001'").fetchone()
                if row["balance_cents"] < amount_cents:
                    self.send_json(400, {"error": "Insufficient funds in wallet"})
                    return

                with conn:
                    conn.execute("UPDATE wallet SET balance_cents = balance_cents - ? WHERE id = 'usr_001'", (amount_cents,))
                    updated_wallet = conn.execute("SELECT * FROM wallet WHERE id = 'usr_001'").fetchone()

                    tx_id = f"tx_wth_{int(time.time() * 1000)}"
                    ref = f"REF-WTH-KH-{int(time.time())}"
                    now_ms = int(time.time() * 1000)

                    conn.execute(
                        """INSERT INTO transactions (id, type, amount_cents, receiver_name, receiver_wallet, reference_number, status, timestamp_ms, note, category)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        (tx_id, "WITHDRAWAL", amount_cents, destination, "BANK-ACCOUNT", ref, "COMPLETED", now_ms, note, "Cash Out")
                    )

                self.send_json(200, {
                    "success": True,
                    "new_balance_cents": updated_wallet["balance_cents"],
                    "transaction": {
                        "id": tx_id,
                        "type": "WITHDRAWAL",
                        "amount_cents": amount_cents,
                        "reference_number": ref,
                        "destination": destination,
                        "status": "COMPLETED",
                        "timestamp_ms": now_ms
                    }
                })
                return

            # 6. Transfer / Pay QR to Bakong Payee
            if path == "/api/v1/wallet/transfer":
                amount_cents = int(body.get("amount_cents", 0))
                receiver_name = body.get("receiver_name", "Bakong Payee")
                receiver_wallet = body.get("receiver_wallet", "bakong_recipient")
                note = body.get("note", "Bakong KHQR Payment")

                row = conn.execute("SELECT * FROM wallet WHERE id = 'usr_001'").fetchone()
                if row["balance_cents"] < amount_cents:
                    self.send_json(400, {"error": "Insufficient funds"})
                    return

                with conn:
                    conn.execute("UPDATE wallet SET balance_cents = balance_cents - ? WHERE id = 'usr_001'", (amount_cents,))
                    updated_wallet = conn.execute("SELECT * FROM wallet WHERE id = 'usr_001'").fetchone()

                    tx_id = f"tx_khqr_{int(time.time() * 1000)}"
                    ref = f"REF-KHQR-{int(time.time())}"
                    now_ms = int(time.time() * 1000)

                    conn.execute(
                        """INSERT INTO transactions (id, type, amount_cents, receiver_name, receiver_wallet, reference_number, status, timestamp_ms, note, category)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        (tx_id, "QR_PAYMENT", amount_cents, receiver_name, receiver_wallet, ref, "COMPLETED", now_ms, note, "KHQR Payment")
                    )

                self.send_json(200, {
                    "success": True,
                    "new_balance_cents": updated_wallet["balance_cents"],
                    "transaction": {
                        "id": tx_id,
                        "type": "QR_PAYMENT",
                        "amount_cents": amount_cents,
                        "reference_number": ref,
                        "receiver_name": receiver_name,
                        "status": "COMPLETED",
                        "timestamp_ms": now_ms
                    }
                })
                return

            # 7. Bakong Webhook Receiver
            if path == "/api/v1/webhook/bakong":
                # Webhook from NBC Bakong on completed payment
                print(f"[BAKONG WEBHOOK] Received event: {json.dumps(body)}")
                self.send_json(200, {"responseCode": 0, "responseMessage": "Success"})
                return

            self.send_json(404, {"error": "Not found"})
        finally:
            conn.close()

# ─────────────────────────────────────────────────────────────────────────────
# MAIN ENTRYPOINT
# ─────────────────────────────────────────────────────────────────────────────

def main():
    init_db()
    server = ThreadedHTTPServer(("0.0.0.0", PORT), WalletApiHandler)
    print("=" * 70)
    print("  PocketPay Cambodian Bank & NBC Bakong KHQR Server")
    print("=" * 70)
    print(f"  • Listening on:       http://0.0.0.0:{PORT}")
    print(f"  • Android Emulator:   http://10.0.2.2:{PORT}")
    print(f"  • Bakong Account ID:  {BAKONG_ACCOUNT_ID}")
    print(f"  • Merchant Name:      {BAKONG_MERCHANT_NAME}")
    print(f"  • Acquiring Bank:     {BAKONG_ACQUIRING_BANK}")
    if BAKONG_API_TOKEN and BAKONG_API_TOKEN != "your_bakong_developer_token_here":
        print(f"  • Bakong Open API:    LIVE ({BAKONG_API_URL})")
    else:
        print(f"  • Bakong Open API:    SANDBOX / SIMULATED (Fill token in .env)")
    print("=" * 70)

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server gracefully...")
        server.server_close()

if __name__ == "__main__":
    main()
