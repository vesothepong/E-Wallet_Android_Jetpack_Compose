# PocketPay — Reference UI Design Specification (Figma Blueprint)

![Target UI Design](/Users/rosvesothepong/.gemini/antigravity-cli/brain/10d50aa6-abee-4d32-9c08-bea364a95a11/.user_uploaded/uploaded_media_1793423703593.jpg)

This specification breaks down your chosen reference UI into exact Figma components, tokens, auto-layout hierarchies, and generation prompts.

---

## 1. Visual Style & Color Tokens

| Component / Role | Hex Code | Visual Description |
| :--- | :--- | :--- |
| **Hero Card Gradient** | `#4F46E5` $\rightarrow$ `#3B82F6` | Deep Royal Indigo to Electric Blue |
| **Income Subtext** | `#FFFFFF` | Crisp White text with `$4,820.00` bold |
| **Expense Subtext** | `#FDA4AF` / `#FB7185` | Soft Coral / Salmon Rose for expenses |
| **Pill Badge (+2.4%)** | `#38BDF8` (18% Alpha) | Translucent Cyan with `#38BDF8` text |
| **Quick Action 1: Send** | `#4F46E5` | Vibrant Indigo Squircle + White Paper Plane |
| **Quick Action 2: Receive** | `#10B981` | Emerald Green Squircle + White QR Code |
| **Quick Action 3: Scan & Pay** | `#F59E0B` | Warm Amber Squircle + White Viewfinder |
| **Quick Action 4: Top Up** | `#8B5CF6` | Vivid Purple Squircle + White Wallet Icon |
| **Monthly Budget Bar** | `#4F46E5` | Deep Indigo filled progress track |
| **Background** | `#F8FAFC` | Light Cool Slate background |
| **Card Surfaces** | `#FFFFFF` | Pure white cards with soft drop shadows |
| **Dividers & Outlines** | `#F1F5F9` | Ultra-subtle border stroke |

---

## 2. Component Hierarchy & Auto-Layout Specs

### A. Top App Header (`Auto-Layout: Horizontal`, Height: `56px`)
* **Left Group (User Profile):**
  * Avatar Circle (`44 x 44 px`):
    * Background: `#6366F1` (Indigo).
    * White text: `"K"` (Bold, `18px`).
    * Online Status Dot: `10 x 10 px` Emerald Green (`#10B981`) anchored to bottom-right.
  * User Greeting Text Column (Spacing: `2px`):
    * Label: `"Welcome back,"` (`12px`, `#64748B`, Medium).
    * Name: `"Kantumruy Dev"` (`16px`, `#0F172A`, Bold).
* **Right Group (Header Actions, Spacing: `10px`):**
  * Circular Button 1 (`40 x 40 px`, Surface: `#F1F5F9`):
    * Icon: Theme toggle (Sun icon, `#0F172A`).
  * Circular Button 2 (`40 x 40 px`, Surface: `#F1F5F9`):
    * Icon: Notification bell with `#EF4444` unread badge dot at top-right.

---

### B. Total Balance Hero Card (`Corner Radius: 26px`, Padding: `22px`)
* **Container:** Gradient from `#4F46E5` (top-left) to `#2563EB` (bottom-right) + soft ambient blue shadow (`Y: 10px, Blur: 24px, Alpha: 0.18`).
* **Header Line:**
  * Left: `"TOTAL BALANCE"` (`12px`, White 80% opacity, Bold, Letter-spacing: `+0.5px`).
  * Right: Eye toggle icon (`18px`, White 80% opacity).
* **Hero Amount Row (Spacing: `10px`, Margin-top: `6px`):**
  * Text: `"$12,450.80"` (`34px`, White, ExtraBold).
  * Pill Badge: `"+2.4%"` (Padding: `horizontal 8px, vertical 3px`, Radius: `12px`, Background: `rgba(56, 189, 248, 0.22)`, Text: `#E0F2FE`, Bold `11px`).
* **Divider:** `1px` line (`rgba(255, 255, 255, 0.15)`), Margin: `vertical 16px`.
* **Two-Column Sub-Metrics (Auto-Layout: Space-Between):**
  * **Column 1 (Income):**
    * Label: `"Income (Oct)"` (`12px`, White 75%).
    * Value: `"$4,820.00"` (`16px`, White, Bold).
  * **Column 2 (Expenses):**
    * Label: `"Expenses (Oct)"` (`12px`, White 75%).
    * Value: `"$1,635.20"` (`16px`, `#FDA4AF` Salmon Rose, Bold).

---

### C. 4-Icon Quick Actions (`Auto-Layout: Horizontal`, Space-Between)
Each item is an auto-layout column (Spacing: `8px`, Center-aligned):

1. **Send:**
   * Button: `56 x 56 px`, Radius: `18px`, Fill: `#4F46E5` (Indigo) + Paper plane icon (`24px`, White).
   * Label: `"Send"` (`12px`, `#0F172A`, SemiBold).
2. **Receive:**
   * Button: `56 x 56 px`, Radius: `18px`, Fill: `#10B981` (Emerald) + QR code icon (`24px`, White).
   * Label: `"Receive"` (`12px`, `#0F172A`, SemiBold).
3. **Scan & Pay:**
   * Button: `56 x 56 px`, Radius: `18px`, Fill: `#F59E0B` (Amber) + Scanner viewfinder icon (`24px`, White).
   * Label: `"Scan & Pay"` (`12px`, `#0F172A`, SemiBold).
4. **Top Up:**
   * Button: `56 x 56 px`, Radius: `18px`, Fill: `#8B5CF6` (Purple) + Wallet icon (`24px`, White).
   * Label: `"Top Up"` (`12px`, `#0F172A`, SemiBold).

---

### D. Monthly Budget Card (`White Surface`, Radius: `20px`, Padding: `16px`)
* **Header Row:**
  * Left: Circular purple icon container (`34 x 34 px`, `#EDE9FE`) with Pie/Clock icon (`#6366F1`) + `"Monthly Budget"` (`15px`, `#0F172A`, Bold).
  * Right: `"62% Used"` (`13px`, `#4F46E5`, SemiBold).
* **Progress Bar (Margin: `vertical 12px`):**
  * Height: `8px`, Radius: `4px`.
  * Background Track: `#E2E8F0` (Light gray).
  * Active Progress (62%): `#4F46E5` (Indigo).
* **Footer Row (Space-Between):**
  * Left: `"Spent: $1,240.00"` (`12px`, `#64748B`, Medium).
  * Right: `"Limit: $2,000.00"` (`12px`, `#64748B`, Medium).

---

### E. Recent Transactions Section
* **Header Row:**
  * Title: `"Recent Transactions"` (`17px`, `#0F172A`, Bold).
  * Action: `"See All"` (`13px`, `#4F46E5`, SemiBold).
* **Card Item 1: Apple Store**
  * Container: White card, Radius: `16px`, Padding: `14px`, Border: `1px solid #F1F5F9`.
  * Icon: Soft rose square container (`42 x 42 px`, `#FEE2E2`) + Shopping bag icon (`#EF4444`).
  * Text Column:
    * Title: `"Apple Store"` (`15px`, `#0F172A`, Bold).
    * Subtitle: `"Today, 2:45 PM • Electronics"` (`12px`, `#94A3B8`).
  * Amount: `"-$799.00"` (`15px`, `#0F172A`, ExtraBold).
* **Card Item 2: Salary Deposit**
  * Container: White card, Radius: `16px`, Padding: `14px`, Border: `1px solid #F1F5F9`.
  * Icon: Mint green square container (`42 x 42 px`, `#DCFCE7`) + Arrow down-left icon (`#10B981`).
  * Text Column:
    * Title: `"Salary Deposit"` (`15px`, `#0F172A`, Bold).
    * Subtitle: `"Yesterday, 9:00 AM • Transfer"` (`12px`, `#94A3B8`).
  * Amount: `"+$4,250.00"` (`15px`, `#10B981`, ExtraBold).

---

### F. Bottom Navigation Bar (`White Surface`, Height: `72px`, 4 Tabs)
* **Tab 1 — Home:** Grid icon (Active, `#4F46E5`) + `"Home"` text label.
* **Tab 2 — Transfer:** Double horizontal arrows (`#94A3B8`) + `"Transfer"`.
* **Tab 3 — Cards:** Credit card icon (`#94A3B8`) + `"Cards"`.
* **Tab 4 — Profile:** User silhouette icon (`#94A3B8`) + `"Profile"`.

---

## 3. Copy-Paste Figma AI / Galileo Prompt

```text
Create a clean mobile fintech home screen on a 393x852 frame in modern light mode. 
At the top, include a header with a purple avatar circle 'K' having a small green active status dot, 'Welcome back,' in light gray, and 'Kantumruy Dev' in bold black. Place two circular gray icon buttons on the right: a sun theme toggle and a notification bell with a red badge dot.
Below, add a 26px rounded hero balance card with an indigo-to-blue gradient (#4F46E5 to #3B82F6). The card displays 'TOTAL BALANCE' in subtle white with an eye toggle icon, large '$12,450.80' text, and a '+2.4%' translucent cyan pill badge. Under a thin white divider, show two columns: 'Income (Oct)' with '$4,820.00' in white, and 'Expenses (Oct)' with '$1,635.20' in soft coral pink.
Under the card, display 4 vibrant squircle action buttons with labels below:
1. Indigo button with paper plane icon labeled 'Send'
2. Emerald green button with QR code icon labeled 'Receive'
3. Amber orange button with scanner viewfinder icon labeled 'Scan & Pay'
4. Purple button with wallet icon labeled 'Top Up'
Next, add a white 'Monthly Budget' card showing a small clock icon, '62% Used' in blue, an indigo progress bar, and footer text 'Spent: $1,240.00' and 'Limit: $2,000.00'.
Below, include a 'Recent Transactions' header with 'See All' button, followed by two transaction cards:
- 'Apple Store' (-$799.00) with a light red shopping bag icon container and timestamp 'Today, 2:45 PM • Electronics'
- 'Salary Deposit' (+$4,250.00) with a light green income arrow icon container and timestamp 'Yesterday, 9:00 AM • Transfer'
At the bottom, place a clean 4-item navigation bar: Home (active indigo), Transfer, Cards, and Profile. Use Plus Jakarta Sans typography and soft 16px card elevations.
```
