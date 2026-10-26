# Features To Do

This document tracks upcoming plans, enhancements, and prioritized features for R-Login. Completed items are moved to the bottom with the version in which they were released.

---

## Pending Features (Prioritized Order)

### 1. Fast Auto-Connect on Rajagiri Wi-Fi Detection
- **Objective:** Automatically detect connection to campus Wi-Fi and execute the captive portal login protocol in milliseconds without opening the app or draining battery.
- **Planned Changes:**
  - **NetworkCallback Listener:** Register a system callback using `ConnectivityManager.NetworkCallback` requesting `TRANSPORT_WIFI` (modern Android 10+ standard).
  - **Campus Gateway Recognition:** Detect campus gateway IP (`172.16.0.20:8090`) or SSID (`M-WiFi`).
  - **Network-Bound Protocol:** Trigger `Net.loginWifi()` through the bound Wi-Fi interface so login succeeds even when mobile data is on.
  - **User Controls & Notifications:** In-app toggle on the dashboard to enable/disable auto-login, with a discreet notification upon successful connection.
  - **Quick Settings Tile (Optional):** Notification shade tile for 1-tap connection.

---

### 2. Moodle LMS Assignments Tracker Polish & In-App Security
- **Objective:** Refine assignment list ordering and harden inter-activity credential passing.
- **Planned Changes:**
  - **Descending Sort for Completed Tab:** In the Completed tab, sort items in descending order so the most recently completed or latest deadline items appear at the top instead of the bottom.
  - **Intent Credential Hardening:** Eliminate passing decrypted passwords via `Intent.putExtra("pass", Crypt.dec(...))`. Instead, pass only the service key (`"LMS"`, `"FEDENA"`), and let `AssignmentActivity`, `ReportActivity`, and `WebActivity` fetch and decrypt credentials locally inside their own process memory right before authentication.

---

### 3. Modernization & Architecture Upgrades
- **Objective:** Clean up technical debt and migrate to modern Android Jetpack standards.
- **Planned Changes:**
  - **SQLiteOpenHelper to Room:** Migrate `Db.java` to Android Room for compile-time verified database operations.
  - **findViewById to ViewBinding:** Eliminate `findViewById` and null-pointer risks across layouts.
  - **Background WorkManager:** Move background scraping out of UI Activity lifecycles into dedicated background workers.

---

## Completed Features

### Feature: Settings Hub, Theming Engine & Updates Center
- **Implemented in:** Version 2.1.0
- **Summary:** Added dedicated Settings Hub with toolbar gear icon, dynamic dual-theme switcher (Classic Navy and Custom Theme), appearance mode selector (System, Light, Dark), in-app GitHub release checker, What's New release notes dialog, system window insets integration, and academic disclaimer and legal documentation.

### Feature: Moodle LMS Pending Assignments Tracker & Countdown Page
- **Implemented in:** Version 2.0.0
- **Summary:** Native in-app tracker with 6-state urgency lifecycle (Upcoming, Due Soon, Urgent, Overdue, No Set Deadline, Completed), dynamic multi-tier course discovery, 140-day semester retention window based on completion date, persistent manual closures ("Mark as Done" with Undo), one-tap direct submission, and SQLite offline caching.
