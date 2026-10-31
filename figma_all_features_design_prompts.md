# PocketPay — Master Figma Redesign Prompt Pack (All 22 Screens)

This master prompt pack allows you to redesign **every single feature and screen** in the PocketPay Android codebase into Figma, strictly adhering to the exact visual design system of your chosen reference UI.

---

## 🎨 The Universal Design System (Applied to All Screens)

When generating in Figma AI, Galileo, v0, or Uizard, keep these tokens consistent across every screen:

* **Frame Size:** Mobile `393 x 852 px` (iPhone 15 / Android standard)
* **Background:** Light Cool Slate (`#F8FAFC`)
* **Surfaces:** Pure White (`#FFFFFF`) with `16px`–`24px` rounded corners and subtle border stroke (`#F1F5F9`)
* **Primary Brand Accent:** Royal Indigo (`#4F46E5`)
* **Secondary Brand Accent:** Electric Blue (`#3B82F6`)
* **Semantic Colors:**
  * Income / Success: Emerald Green (`#10B981`)
  * Expense / Danger: Soft Salmon / Coral Rose (`#EF4444` / `#FDA4AF`)
  * Warning / Action: Warm Amber (`#F59E0B`)
  * Auxiliary / Savings: Vivid Purple (`#8B5CF6`)
* **Typography:** Plus Jakarta Sans or Inter (Display: `34px Bold`, Titles: `17-18px Bold`, Body: `14-15px Medium`, Labels: `11-12px Regular`)
* **Iconography:** Clean 24px monoline outline icons with rounded caps

---

## 📱 Module 1: Authentication & Onboarding

### Screen 1: Splash & Welcome Onboarding
> **Figma AI Prompt:**
> ```text
> Design a modern mobile fintech onboarding screen for "PocketPay" on a 393x852 frame in light mode. 
> At the top, show a subtle progress bar (Slide 1 of 3). In the upper center, render a 3D-styled minimalist isometric illustration of a glowing holographic bank card and digital coins floating with soft indigo (#4F46E5) and mint green (#10B981) light reflections.
> In the bottom third, place a bold header: "Smart Digital Banking for Your Everyday Life" (28px bold, #0F172A), followed by subtext: "Send money instantly, scan QR codes seamlessly, and manage personal budgets with zero fees." (#64748B, 15px).
> Add a 3-dot pagination carousel indicator. At the bottom, include a full-width 52px primary indigo button (#4F46E5) labeled "Get Started" and a text button below labeled "Already have an account? Sign In" in indigo.
> ```

### Screen 2: Login & Biometric Unlock
> **Figma AI Prompt:**
> ```text
> Design a sleek mobile login screen for "PocketPay" on a 393x852 frame in clean light mode.
> Header shows a back arrow, small PocketPay brand logo (letter 'P' in indigo squircle), and title "Welcome Back!" (24px bold) with subtitle "Enter your 4-digit security PIN or sign in with biometrics" (#64748B).
> In the center, display user avatar circle with initials "KD", name "Kantumruy Dev", and masked phone number "•••• •••• 8810".
> Below, display 4 circular PIN dot indicators with 2 dots filled in primary indigo (#4F46E5).
> Include a prominent 50px tall rounded button labeled "Sign In with Biometrics" featuring a white Fingerprint icon inside a soft mint-green (#10B981) button.
> Below, place a clean 3x4 numeric keypad with rounded square keys (1-9, C in red, 0, backspace icon). Add a bottom text link "Forgot PIN?".
> ```

### Screen 3: Registration & KYC Setup
> **Figma AI Prompt:**
> ```text
> Design a mobile KYC signup screen for "PocketPay" on a 393x852 frame in light mode.
> Header shows "Create Account" (22px bold) with a "Step 2 of 3" pill badge.
> Form fields inside rounded white card containers with 1px light gray borders:
> 1. Full Legal Name input with person icon
> 2. Email Address input with mail icon
> 3. Phone Number input with country flag selector (+1)
> 4. National ID / Passport Photo upload container with dashed border and camera icon
> Below, add a checkbox with text "I agree to the Terms of Service & Privacy Policy".
> At the bottom, place a 52px tall full-width primary indigo button (#4F46E5) labeled "Continue & Set PIN".
> ```

---

## 📱 Module 2: Home Dashboard & Quick Actions

### Screen 4: Home Dashboard (Reference Screen)
> **Figma AI Prompt:**
> ```text
> Create a clean mobile fintech home screen on a 393x852 frame in modern light mode.
> At the top, include a header with a purple avatar circle 'K' having a small green active status dot, 'Welcome back,' in light gray, and 'Kantumruy Dev' in bold black. Place two circular gray icon buttons on the right: a sun theme toggle and a notification bell with a red badge dot.
> Below, add a 26px rounded hero balance card with an indigo-to-blue gradient (#4F46E5 to #3B82F6). The card displays 'TOTAL BALANCE' in subtle white with an eye toggle icon, large '$12,450.80' text, and a '+2.4%' translucent cyan pill badge. Under a thin white divider, show two columns: 'Income (Oct)' with '$4,820.00' in white, and 'Expenses (Oct)' with '$1,635.20' in soft coral pink.
> Under the card, display 4 vibrant squircle action buttons with labels below:
> 1. Indigo button with paper plane icon labeled 'Send'
> 2. Emerald green button with QR code icon labeled 'Receive'
> 3. Amber orange button with scanner viewfinder icon labeled 'Scan & Pay'
> 4. Purple button with wallet icon labeled 'Top Up'
> Next, add a white 'Monthly Budget' card showing a small clock icon, '62% Used' in blue, an indigo progress bar, and footer text 'Spent: $1,240.00' and 'Limit: $2,000.00'.
> Below, include a 'Recent Transactions' header with 'See All' button, followed by two transaction cards:
> - 'Apple Store' (-$799.00) with a light red shopping bag icon container and timestamp 'Today, 2:45 PM • Electronics'
> - 'Salary Deposit' (+$4,250.00) with a light green income arrow icon container and timestamp 'Yesterday, 9:00 AM • Transfer'
> At the bottom, place a clean 4-item navigation bar: Home (active indigo), Transfer, Cards, and Profile.
> ```

### Screen 5: Send Money Modal (Bottom Sheet)
> **Figma AI Prompt:**
> ```text
> Design a mobile Send Money modal bottom sheet for "PocketPay" on a 393px width frame.
> Header shows drag handle, "Send Money" title, and close button.
> Top section: Horizontal scrollable row of "Recent Beneficiaries" with circular profile avatars and names (Sarah, David, Alex, Emily, "+ New").
> Middle section: "Recipient Wallet Number or Phone" input field with a search/paste icon.
> Amount section: Large editable numeric input with "$" prefix ($250.00) and 4 quick amount chips ($10, $50, $100, $250).
> Note field: "Transfer Note (e.g. Dinner, Rent)" with 80 character limit.
> Breakdown card: Transfer Amount $250.00, Fee Free ($0.00), Wallet Balance $12,450.80, Balance After $12,200.80.
> Bottom CTA: 52px tall primary indigo button labeled "Review & Send $250.00".
> ```

### Screen 6: Receive Money & Personal QR Generator
> **Figma AI Prompt:**
> ```text
> Design a mobile Receive Money screen on a 393x852 frame in clean light mode.
> Header: Back arrow and title "Receive Money".
> Center container: Large white card (24px radius, soft shadow) with user's personal QR code (200x200px) with PocketPay 'P' logo in the center.
> Below the QR: User's name "Kantumruy Dev", PocketPay ID "PKP-8821-4402" with a 1-tap "Copy" pill button.
> Underneath, add a toggle switch: "Set Request Amount" (Dynamic Invoice). When active, it displays an amount input field of "$45.00".
> Bottom Action Row: Two side-by-side buttons:
> 1. Outlined button with download icon: "Save Image"
> 2. Primary indigo button (#4F46E5) with share icon: "Share QR Link".
> ```

### Screen 7: Top-Up / Deposit Bottom Sheet
> **Figma AI Prompt:**
> ```text
> Design a mobile Top-Up / Deposit modal bottom sheet for "PocketPay" on a 393px width frame.
> Title: "Top Up Wallet Balance" with subtitle "Current Balance: $12,450.80".
> Source selector: Radio list of funding accounts:
> 1. Chase Checking Account (•••• 4412) with bank logo (Selected)
> 2. PocketPay Visa Debit Card (•••• 5521)
> 3. "+ Link New Bank Account"
> Amount entry: Display large input field "$100.00" with quick chips ($20, $50, $100, $500).
> Info badge: "Instant deposit • Zero transaction fee".
> Bottom button: 52px primary indigo button labeled "Deposit $100.00 Now".
> ```

### Screen 8: Withdraw to Bank Bottom Sheet
> **Figma AI Prompt:**
> ```text
> Design a mobile Withdraw modal bottom sheet for "PocketPay" on a 393px width frame.
> Title: "Withdraw to Bank Account".
> Destination card: Bank of America Checking (•••• 9011) with verified green badge.
> Amount input: Large "$500.00" input with "Max ($12,450.80)" text link button on the right.
> Delivery speed selector:
> 1. Standard (1-3 business days) — Free (Selected)
> 2. Instant Transfer (Within minutes) — 1.5% Fee ($7.50)
> Breakdown card: Withdrawal Amount $500.00, Transfer Fee $0.00, Total Debited $500.00.
> Bottom button: 52px primary indigo button labeled "Confirm Withdrawal".
> ```

---

## 📱 Module 3: QR Code Payment Suite

### Screen 9: QR Scanner Viewfinder Screen
> **Figma AI Prompt:**
> ```text
> Design a mobile QR Code Scanner screen on a 393x852 frame in dark mode theme (#0F172A).
> Top bar: Back arrow, "Scan QR Code" title in white, and a manual code keyboard icon on the right.
> Center: 260x260px rounded viewfinder box (24px radius) with neon cyan (#38BDF8) corner brackets and an animated glowing horizontal laser scanning line across the middle.
> Subtext: "Align PocketPay or Merchant QR within frame".
> Control pills floating under viewfinder: Flash/Torch icon button, Photo Gallery icon button, and Camera Flip icon button inside translucent dark pill container.
> Bottom section: "Try Demo QRs" with horizontal scroll of 1-tap test chips:
> - Starbucks Coffee ($8.50)
> - PocketPay Mart ($34.20)
> - Sarah Chen (Open Amount).
> ```

### Screen 10: QR Payment Confirmation & Deficit Pre-Check
> **Figma AI Prompt:**
> ```text
> Design a mobile QR Payment Confirmation modal bottom sheet on a 393px width frame in light mode.
> Header: Back arrow and "Payment Confirmation" title.
> Merchant Badge Card: Soft indigo surface with initials "SB", merchant name "Starbucks Coffee", "Verified Merchant" teal badge, and wallet ID "PKP-MCH-001".
> Amount Box: Locked invoice container with lock icon, badge "INVOICE AMOUNT FIXED BY QR", and large bold "$8.50" text.
> Breakdown card: Amount $8.50, Fee Free, Total Debit $8.50, Wallet Balance $12,450.80, Remaining Balance $12,442.30 in green.
> Pre-Check Insufficient Funds Banner (Design variant): Soft coral-rose container (#FEE2E2) with info icon, bold text "Insufficient Balance", and description "You need $4.20 more to complete this payment".
> Bottom button: 52px tall primary indigo button labeled "Confirm & Authorize ($8.50)" (or disabled state if insufficient balance).
> ```

### Screen 11: Payment Authorization & Digital Success Receipt
> **Figma AI Prompt:**
> ```text
> Design a mobile payment digital receipt modal bottom sheet on a 393px width frame in light mode.
> Top: Large emerald green circular checkmark icon (#10B981) with subtle glow, header "Payment Successful!", and large bold green amount "-$8.50".
> Receipt card (20px radius, soft gray surface):
> - Paid To: Starbucks Coffee
> - Transaction Type: QR Payment
> - Reference No: REF-QRP-883921
> - Timestamp: Oct 31, 2026, 2:45 PM
> - Payment Source: PocketPay Wallet
> - Remaining Balance: $12,442.30
> - Status: "COMPLETED" in green pill badge.
> Bottom row: Two side-by-side buttons:
> 1. Outlined button with share icon labeled "Share Receipt"
> 2. Primary indigo button labeled "Done".
> ```

---

## 📱 Module 4: Bill Payments & Utilities

### Screen 12: Bill Payments Categories Hub
> **Figma AI Prompt:**
> ```text
> Design a mobile Bill Payment Hub screen on a 393x852 frame in clean light mode.
> Header: Back arrow, "Pay Bills & Utilities", and search bar "Search biller (e.g. Electric, Water)".
> Grid section (2x3 cards with vibrant squircle icons):
> 1. Electricity (Amber squircle #F59E0B with lightning icon)
> 2. Water Utility (Blue squircle #3B82F6 with water drop icon)
> 3. Internet & Fiber (Indigo squircle #4F46E5 with wifi icon)
> 4. Mobile Top-Up (Purple squircle #8B5CF6 with smartphone icon)
> 5. Tuition & School (Teal squircle #0D9488 with school hat icon)
> 6. Cable TV (Rose squircle #EF4444 with television icon).
> Bottom section: "Upcoming Bills / Auto-Pay" card showing "City Water Utility — Due in 3 days ($42.50)" with a 1-tap "Pay Now" button.
> ```

### Screen 13: Biller Invoice Detail & Payment
> **Figma AI Prompt:**
> ```text
> Design a mobile Utility Biller Invoice screen on a 393x852 frame in light mode.
> Header shows "Electricity Bill — National Power Co." with logo.
> Form fields:
> - Customer Account / Meter Number: "MTR-9921401" with verified green check.
> - Bill Month: "October 2026".
> Invoice Summary Card:
> - Electricity Usage: 340 kWh
> - Energy Charges: $68.00
> - Government Tax (10%): $6.80
> - Late Fee: $0.00
> - Total Amount Due: "$74.80" (large bold text, #0F172A).
> Toggle switch: "Enable Auto-Pay for this utility".
> Bottom button: 52px primary indigo button labeled "Pay $74.80 from PocketPay".
> ```

---

## 📱 Module 5: Split Bill System

### Screen 14: Split Bill Dashboard
> **Figma AI Prompt:**
> ```text
> Design a mobile Split Bill Dashboard screen on a 393x852 frame in clean light mode.
> Header: "Split Bills" with a "+ Create Split" button in the top right.
> Summary Card (Two-column gradient card):
> - Left: "You are owed" / "$145.00" in emerald green
> - Right: "You owe" / "$30.00" in soft coral rose.
> Active Splits List:
> Card 1: "Friday Sushi Dinner 🍣"
> - Total: $120.00 • Your share: $30.00
> - Progress: "3 of 4 Paid" with horizontal green progress bar
> - Participant avatars with green checkmarks for paid members (Sarah, Alex, David) and clock icon for pending (Emily).
> - Action button: "Remind Emily".
> Card 2: "Apartment Internet & Wifi 🌐"
> - Total: $60.00 • Status: "All Paid" green badge.
> Bottom navigation bar visible at bottom.
> ```

### Screen 15: Create New Split Bill Modal
> **Figma AI Prompt:**
> ```text
> Design a mobile Create Split Bill modal bottom sheet on a 393px width frame.
> Title: "Create New Split Bill".
> Input 1: Bill Title "e.g. Weekend BBQ Party".
> Input 2: Total Bill Amount "$160.00".
> Contact Picker: Horizontal chips of added friends (Sarah Chen, David Miller, Alex Vance, "+ Add Friend").
> Split Method Selector:
> 1. "Split Equally" ($40.00 each) — Selected
> 2. "Custom / Unequal Split"
> 3. "By Percentages".
> Summary Box: "You will pay $160.00 now and request $40.00 from 3 friends."
> Bottom button: 52px primary indigo button labeled "Send Split Requests".
> ```

---

## 📱 Module 6: Cards & Wallet Management

### Screen 16: Cards & Accounts Hub (Tab 3)
> **Figma AI Prompt:**
> ```text
> Design a mobile Cards & Accounts Management screen on a 393x852 frame in light mode.
> Header: "My Cards & Accounts" with "+ Add" icon button.
> Card Carousel: Premium virtual debit card (320x190px) featuring holographic dark indigo gradient (#1E1B4B to #312E81), silver EMV chip, contactless payment icon, PocketPay logo in white, masked card number "•••• •••• •••• 5521", expiry "09/29", and Visa Platinum badge.
> Card Action Controls (Row of 3 pill buttons under card):
> 1. "❄️ Freeze Card" (Interactive toggle)
> 2. "🔒 Online Payments" (Active green badge)
> 3. "👁️ Show Details".
> Linked Bank Accounts Section:
> - Chase Bank Checking (•••• 4412) with bank icon and "$8,320.00" balance
> - Bank of America Savings (•••• 9011) with bank icon and "$14,500.00" balance.
> Bottom navigation bar with "Cards" tab active in indigo.
> ```

---

## 📱 Module 7: Spending Analytics & Budgeting

### Screen 17: Spending Analytics Dashboard
> **Figma AI Prompt:**
> ```text
> Design a modern mobile Spending Analytics screen on a 393x852 frame in clean light mode.
> Top Filter Bar: Pill segmented controls "[Week] [Month (Active)] [Year]".
> Donut Chart Section:
> - Clean multi-colored donut chart with center label "Total Spend / $1,635.20"
> - Category Legend with color dots:
>   - Food & Dining 38% (#4F46E5 Indigo)
>   - Shopping 26% (#10B981 Green)
>   - Utilities & Bills 18% (#F59E0B Amber)
>   - Entertainment 12% (#8B5CF6 Purple)
>   - Others 6% (#94A3B8 Gray).
> Daily Spend Bar Chart:
> - 7 vertical rounded bars representing Mon through Sun with spending height, Friday highlighted in indigo as peak spend day ($320.00).
> Financial Health Card:
> - Radial score gauge "86 / 100 — Excellent Financial Health" with tip "You saved 18% more than last month!".
> ```

### Screen 18: Category Budgets & Limit Editor
> **Figma AI Prompt:**
> ```text
> Design a mobile Category Budgets screen on a 393x852 frame in light mode.
> Header: "Monthly Budgets" with "+ Add Budget" button.
> Budget Cards List:
> 1. Food & Dining: Fork/knife icon in indigo container, "$380.00 spent of $500.00 limit" (76%), indigo progress bar, edit pencil icon.
> 2. Shopping & Retail: Shopping bag icon in purple container, "$210.00 spent of $300.00 limit" (70%), purple progress bar.
> 3. Entertainment: Popcorn icon in amber container, "$145.00 spent of $150.00 limit" (96%), red warning indicator "Near Limit!".
> Modal overlay (Half sheet): "Edit Food & Dining Budget", slider control setting new monthly cap "$600.00", and button "Save Budget".
> ```

---

## 📱 Module 8: Savings Goals

### Screen 19: Savings Goals Hub
> **Figma AI Prompt:**
> ```text
> Design a mobile Savings Goals screen for "PocketPay" on a 393x852 frame in clean light mode.
> Header: "Savings Goals" with subtitle "Total Saved: $6,450.00".
> Grid of Goal Cards:
> Card 1: "🏖️ Summer Vacation to Bali"
> - Saved: $1,800.00 of $3,000.00 goal (60%)
> - Linear progress bar in emerald green (#10B981)
> - Subtext: "$1,200.00 remaining • Target: Aug 2027"
> - 1-tap button: "+ Add Funds".
> Card 2: "💻 MacBook Pro M4 Setup"
> - Saved: $2,400.00 of $2,500.00 goal (96%)
> - Progress bar in primary indigo (#4F46E5)
> - Badge: "Almost Done!".
> Card 3: "🛡️ 6-Month Emergency Fund"
> - Saved: $2,250.00 of $5,000.00 goal (45%).
> Floating Action Button at bottom right: "+ New Goal" in primary indigo.
> ```

---

## 📱 Module 9: Transaction History & Detail

### Screen 20: Transaction History List
> **Figma AI Prompt:**
> ```text
> Design a mobile Transaction History screen on a 393x852 frame in clean light mode.
> Header: "All Transactions" with search bar "Search by name, merchant, or ID".
> Filter Chips Row: "[All (Active)] [Income] [Expenses] [Transfers] [Bills]".
> Grouped Date List:
> Section "Today, Oct 31, 2026":
> - Apple Store (-$799.00) • Shopping bag icon • Electronics
> - Starbucks Coffee (-$8.50) • Coffee cup icon • Dining
> Section "Yesterday, Oct 30, 2026":
> - Salary Deposit (+$4,250.00 in green) • Income arrow • Transfer
> - City Water Utility (-$42.50) • Water icon • Bill Payment
> Section "Oct 28, 2026":
> - David Miller (+$20.00 in green) • Person icon • Peer Transfer.
> ```

### Screen 21: Full Transaction Detail Modal
> **Figma AI Prompt:**
> ```text
> Design a mobile Transaction Detail modal bottom sheet on a 393px width frame.
> Center Top: Merchant avatar "Apple Store" with verified badge, bold title "Apple Store", and large amount "-$799.00".
> Status Pill: "Payment Completed" in emerald green with checkmark.
> Detail List inside soft card container:
> - Transaction ID: tx_992140188
> - Reference Number: REF-APL-88219
> - Payment Method: PocketPay Virtual Visa (•••• 5521)
> - Date & Time: Oct 31, 2026, 2:45 PM
> - Category: Electronics & Gadgets
> - Note: "MacBook accessories & adapter"
> - Transfer Fee: Free ($0.00).
> Action Buttons:
> 1. "Download PDF Invoice" (Outlined button)
> 2. "Report an Issue / Dispute" (Text button in red).
> ```

---

## 📱 Module 10: Profile & Security Settings

### Screen 22: Profile & Security Settings (Tab 4)
> **Figma AI Prompt:**
> ```text
> Design a mobile Profile & Security Settings screen on a 393x852 frame in clean light mode.
> Header Profile Card:
> - Purple avatar circle "K" with verified green check badge
> - Name "Kantumruy Dev" (18px bold)
> - Email "kantumruy.dev@pocketpay.io"
> - Tier badge: "KYC Verified • Tier 2 Account".
> Menu Sections (White card groups with arrow icons):
> Group 1 — Account & Security:
> - Change 4-Digit Security PIN
> - Biometric Authentication (Fingerprint / Face ID toggle switch - ON)
> - Two-Factor Authentication (2FA) (Active)
> Group 2 — Preferences:
> - Currency Selector ("USD ($)" pill badge)
> - App Theme ("Light" with sun icon)
> - Push Notifications (Toggle switch - ON)
> Group 3 — Support & Legal:
> - Help Center & 24/7 Live Chat
> - Privacy Policy & Terms
> Red Logout Button: "Log Out of PocketPay" with exit icon.
> Bottom navigation bar with "Profile" tab active in indigo.
> ```
