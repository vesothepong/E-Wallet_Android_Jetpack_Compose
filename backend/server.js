const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

// Load .env manually if dotenv isn't installed
const envPath = path.join(__dirname, '.env');
if (fs.existsSync(envPath)) {
  const envContent = fs.readFileSync(envPath, 'utf8');
  envContent.split('\n').forEach(line => {
    line = line.trim();
    if (line && !line.startsWith('#') && line.includes('=')) {
      const [key, ...vals] = line.split('=');
      process.env[key.trim()] = vals.join('=').trim();
    }
  });
}

const PORT = parseInt(process.env.PORT || '8080', 10);
const BAKONG_API_URL = process.env.BAKONG_API_URL || 'https://api-bakong.nbc.gov.kh/v1';
const BAKONG_API_TOKEN = process.env.BAKONG_API_TOKEN || '';
const BAKONG_ACCOUNT_ID = process.env.BAKONG_ACCOUNT_ID || 'vesothepong@abaa';
const BAKONG_MERCHANT_NAME = process.env.BAKONG_MERCHANT_NAME || 'PocketPay Store';
const BAKONG_MERCHANT_CITY = process.env.BAKONG_MERCHANT_CITY || 'Phnom Penh';
const CURRENCY = process.env.CURRENCY || 'USD';

// In-memory data store for Node.js
let walletState = {
  walletNumber: 'PKP-1049-5521',
  balanceCents: 1245080,
  currency: CURRENCY
};

let bankAccounts = [
  {
    id: 'bnk_aba_01',
    bank_name: 'ABA Bank (Bakong)',
    account_number_masked: '•••• 8821',
    bakong_account_id: 'vesothepong@abaa',
    account_type: 'Checking',
    is_primary: true,
    balance_cents: 850000
  },
  {
    id: 'bnk_wing_02',
    bank_name: 'Wing Bank (Bakong)',
    account_number_masked: '•••• 3302',
    bakong_account_id: '012888999@wing',
    account_type: 'Savings',
    is_primary: false,
    balance_cents: 1450000
  },
  {
    id: 'bnk_acleda_03',
    bank_name: 'ACLEDA Bank',
    account_number_masked: '•••• 7714',
    bakong_account_id: 'acleda_user@aclb',
    account_type: 'Checking',
    is_primary: false,
    balance_cents: 620000
  }
];

let transactions = [];

// EMVCo CRC-16 CCITT (Polynomial 0x1021, Initial 0xFFFF)
function calculateCrc16(data) {
  let crc = 0xFFFF;
  for (let i = 0; i < data.length; i++) {
    crc ^= (data.charCodeAt(i) << 8);
    for (let j = 0; j < 8; j++) {
      if ((crc & 0x8000) !== 0) {
        crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
      } else {
        crc = (crc << 1) & 0xFFFF;
      }
    }
  }
  return crc.toString(16).toUpperCase().padStart(4, '0');
}

function tlv(tag, value) {
  const len = String(value.length).padStart(2, '0');
  return `${tag}${len}${value}`;
}

function generateKhqr(bakongId, merchant, amountDollars) {
  let payload = tlv('00', '01');
  const pointOfInitiation = (amountDollars && amountDollars > 0) ? '12' : '11';
  payload += tlv('01', pointOfInitiation);
  const accountInfo = tlv('00', 'bakong@nbc') + tlv('01', bakongId);
  payload += tlv('29', accountInfo);
  payload += tlv('52', '5411');
  payload += tlv('53', CURRENCY === 'USD' ? '840' : '116');
  if (amountDollars && amountDollars > 0) {
    payload += tlv('54', amountDollars.toFixed(2));
  }
  payload += tlv('58', 'KH');
  payload += tlv('59', merchant.slice(0, 25));
  payload += tlv('60', BAKONG_MERCHANT_CITY.slice(0, 15));
  const prefix = payload + '6304';
  return prefix + calculateCrc16(prefix);
}

const server = http.createServer((req, res) => {
  // CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const url = req.url.split('?')[0];

  function sendJson(code, data) {
    res.writeHead(code, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(data, null, 2));
  }

  if (req.method === 'GET') {
    if (url === '/api/v1/health' || url === '/') {
      return sendJson(200, {
        status: 'ONLINE',
        service: 'PocketPay Cambodian Banking & Bakong KHQR Server (Node.js)',
        version: '1.0.0',
        bakong_configured: Boolean(BAKONG_API_TOKEN && BAKONG_API_TOKEN !== 'your_bakong_developer_token_here'),
        bakong_account: BAKONG_ACCOUNT_ID,
        merchant_name: BAKONG_MERCHANT_NAME,
        currency: CURRENCY
      });
    }

    if (url === '/api/v1/wallet/balance') {
      return sendJson(200, {
        wallet_number: walletState.walletNumber,
        balance_cents: walletState.balanceCents,
        currency: walletState.currency
      });
    }

    if (url === '/api/v1/bank/accounts') {
      return sendJson(200, { accounts: bankAccounts });
    }

    if (url === '/api/v1/wallet/transactions') {
      return sendJson(200, { transactions });
    }

    return sendJson(404, { error: 'Not found' });
  }

  if (req.method === 'POST') {
    let bodyRaw = '';
    req.on('data', chunk => { bodyRaw += chunk; });
    req.on('end', () => {
      let body = {};
      try { if (bodyRaw) body = JSON.parse(bodyRaw); } catch (e) {}

      if (url === '/api/v1/bank/khqr/generate') {
        const amountCents = body.amount_cents || 0;
        const amountDollars = amountCents > 0 ? (amountCents / 100.0) : null;
        const bakongId = body.bakong_account_id || BAKONG_ACCOUNT_ID;
        const merchant = body.merchant_name || BAKONG_MERCHANT_NAME;

        const khqr = generateKhqr(bakongId, merchant, amountDollars);
        const md5 = crypto.createHash('md5').update(khqr).digest('hex');

        return sendJson(200, {
          khqr_payload: khqr,
          md5_hash: md5,
          bakong_account: bakongId,
          amount_cents: amountCents,
          currency: CURRENCY
        });
      }

      if (url === '/api/v1/bank/khqr/check') {
        return sendJson(200, {
          status: 'SIMULATED',
          is_paid: true,
          message: 'Bakong token not configured; sandbox mode payment verified',
          hash: body.md5_hash || ''
        });
      }

      if (url === '/api/v1/bank/link') {
        const bankName = body.bank_name || 'ABA Bank (Bakong)';
        const bakongAcc = (body.bakong_account_id || '').trim();
        const accountNum = (body.account_number || '').trim();

        const newAccount = {
          id: `bnk_${Date.now()}`,
          bank_name: bankName,
          account_number_masked: accountNum.length >= 4 ? `•••• ${accountNum.slice(-4)}` : (bakongAcc || '•••• 9988'),
          bakong_account_id: bakongAcc || `user_${Date.now()}@abaa`,
          account_type: 'Checking',
          is_primary: false,
          balance_cents: 500000
        };
        bankAccounts.push(newAccount);

        return sendJson(200, {
          success: true,
          message: `Linked ${bankName}`,
          account: newAccount
        });
      }

      if (url === '/api/v1/wallet/deposit') {
        const amount = parseInt(body.amount_cents || '0', 10);
        const source = body.source_description || 'ABA Bank (Bakong)';
        const note = body.note || 'Bakong Bank Top-up';

        walletState.balanceCents += amount;
        const txId = `tx_dep_${Date.now()}`;
        const ref = `REF-DEP-KH-${Math.floor(Date.now() / 1000)}`;

        const tx = {
          id: txId,
          type: 'DEPOSIT',
          amount_cents: amount,
          receiver_name: 'PocketPay Wallet',
          receiver_wallet: walletState.walletNumber,
          reference_number: ref,
          status: 'COMPLETED',
          timestamp_ms: Date.now(),
          note,
          category: 'Top Up'
        };
        transactions.unshift(tx);

        return sendJson(200, {
          success: true,
          new_balance_cents: walletState.balanceCents,
          transaction: tx
        });
      }

      if (url === '/api/v1/wallet/withdraw') {
        const amount = parseInt(body.amount_cents || '0', 10);
        const destination = body.destination_description || 'ABA Bank Checking';
        const note = body.note || 'Withdrawal to Cambodian Bank';

        if (walletState.balanceCents < amount) {
          return sendJson(400, { error: 'Insufficient funds' });
        }

        walletState.balanceCents -= amount;
        const txId = `tx_wth_${Date.now()}`;
        const ref = `REF-WTH-KH-${Math.floor(Date.now() / 1000)}`;

        const tx = {
          id: txId,
          type: 'WITHDRAWAL',
          amount_cents: amount,
          receiver_name: destination,
          receiver_wallet: 'BANK-ACCOUNT',
          reference_number: ref,
          status: 'COMPLETED',
          timestamp_ms: Date.now(),
          note,
          category: 'Cash Out'
        };
        transactions.unshift(tx);

        return sendJson(200, {
          success: true,
          new_balance_cents: walletState.balanceCents,
          transaction: tx
        });
      }

      if (url === '/api/v1/wallet/transfer') {
        const amount = parseInt(body.amount_cents || '0', 10);
        const receiverName = body.receiver_name || 'Bakong Payee';
        const receiverWallet = body.receiver_wallet || 'bakong_recipient';
        const note = body.note || 'Bakong KHQR Payment';

        if (walletState.balanceCents < amount) {
          return sendJson(400, { error: 'Insufficient funds' });
        }

        walletState.balanceCents -= amount;
        const txId = `tx_khqr_${Date.now()}`;
        const ref = `REF-KHQR-${Math.floor(Date.now() / 1000)}`;

        const tx = {
          id: txId,
          type: 'QR_PAYMENT',
          amount_cents: amount,
          receiver_name: receiverName,
          receiver_wallet: receiverWallet,
          reference_number: ref,
          status: 'COMPLETED',
          timestamp_ms: Date.now(),
          note,
          category: 'KHQR Payment'
        };
        transactions.unshift(tx);

        return sendJson(200, {
          success: true,
          new_balance_cents: walletState.balanceCents,
          transaction: tx
        });
      }

      return sendJson(404, { error: 'Not found' });
    });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log('='.repeat(70));
  console.log('  PocketPay Cambodian Bank & NBC Bakong KHQR Server (Node.js)');
  console.log('='.repeat(70));
  console.log(`  • Listening on:       http://0.0.0.0:${PORT}`);
  console.log(`  • Android Emulator:   http://10.0.2.2:${PORT}`);
  console.log(`  • Bakong Account ID:  ${BAKONG_ACCOUNT_ID}`);
  console.log(`  • Merchant Name:      ${BAKONG_MERCHANT_NAME}`);
  console.log('='.repeat(70));
});
