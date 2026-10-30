# Features To Do

This document tracks upcoming plans, enhancements, and prioritized features for R-Login. Completed items are moved to the bottom with the version in which they were released.

---

## Pending Features (Prioritized Order)

### 1. Moodle LMS Assignments Tracker Polish & In-App Security
- **Objective:** Refine assignment list ordering and harden inter-activity credential passing.
- **Planned Changes:**
  - **Descending Sort for Completed Tab:** In the Completed tab, sort items in descending order so the most recently completed or latest deadline items appear at the top instead of the bottom.
  - **Intent Credential Hardening:** Eliminate passing decrypted passwords via `Intent.putExtra("pass", Crypt.dec(...))`. Instead, pass only the service key (`"LMS"`, `"FEDENA"`), and let `AssignmentActivity`, `ReportActivity`, and `WebActivity` fetch and decrypt credentials locally inside their own process memory right before authentication.

---

### 2. Modernization & Architecture Upgrades
- **Objective:** Clean up technical debt and migrate to modern Android Jetpack standards.
- **Planned Changes:**
  - **SQLiteOpenHelper to Room:** Migrate `Db.java` to Android Room for compile-time verified database operations.
  - **findViewById to ViewBinding:** Eliminate `findViewById` and null-pointer risks across layouts.
  - **Background WorkManager:** Move background scraping out of UI Activity lifecycles into dedicated background workers.

---

## Completed Features

### Feature: Quick Settings Tile, 1×1 Home Screen Widget & Silent Error Feedback
- **Implemented in:** Version 3.0.0
- **Summary:** Built native Android Quick Settings Tile (`QuickLoginTileService`) allowing 1-tap captive portal connection directly from the top notification pulldown shade, alongside a 1×1 Home Screen Widget (`QuickLoginWidgetProvider`). Added `StatusNotifier` with dedicated silent notification channel (`rlogin_status_channel`, fixed ID 1001) for crisp, non-cluttering error alerts and tactile haptic vibration (1-tick success, double-buzz failure). Includes background auto-connect via `WifiAutoConnectManager`.

### Feature: Neo-Brutalist Theme & Dual-Layout Architecture
- **Implemented in:** Version 3.0.0
- **Summary:** Added complete Neo-Brutalist design system designed by Shyam James (@shyamjames) with bold typography, solid ink borders, hard drop shadows, and custom light and dark color palettes. Integrated dynamic layout switching across all core activities and added an inline password visibility toggle in the Credential Vault matching the Neo-Brutalist aesthetic.

### Feature: Application Identity Migration & Package Renaming
- **Implemented in:** Version 3.0.0
- **Summary:** Migrated Android application namespace and package ID from com.example.rlogin to production identifier com.rlogin.rlogin. Upgraded build configuration to versionCode 4 and versionName 3.0.0 for major milestone release.

### Feature: 2-Slot Release Notes Caching & Markdown Rich-Text Formatter
- **Implemented in:** Version 3.0.0
- **Summary:** Built a 2-slot self-pruning SQLite table (release_cache) that caches CURRENT and PENDING releases with an automatic promotion-on-launch lifecycle. Created GitHubUpdater.formatMarkdown to convert raw GitHub markdown into styled rich text with bold section titles, bullet points, and zero raw hashtag symbols in update and What's New dialogs.

### Feature: Settings Hub, Theming Engine & Updates Center
- **Implemented in:** Version 2.1.0
- **Summary:** Added dedicated Settings Hub with toolbar gear icon, dynamic dual-theme switcher (Classic Navy and Custom Theme), appearance mode selector (System, Light, Dark), in-app GitHub release checker, What's New release notes dialog, system window insets integration, and academic disclaimer and legal documentation.

### Feature: Moodle LMS Pending Assignments Tracker & Countdown Page
- **Implemented in:** Version 2.0.0
- **Summary:** Native in-app tracker with 6-state urgency lifecycle (Upcoming, Due Soon, Urgent, Overdue, No Set Deadline, Completed), dynamic multi-tier course discovery, 140-day semester retention window based on completion date, persistent manual closures ("Mark as Done" with Undo), one-tap direct submission, and SQLite offline caching.
