# R-Login Roadmap Backup & Approval Gate Protocol

This document serves as the master backup plan and sequential execution tracker for R-Login.

---

## Governance Rules

1. Sequential Execution: Each priority item must be reviewed, tested, and explicitly approved by the user before work on the next item begins.
2. Review Before Execution: Before modifying code for an approved phase, a technical explanation will be provided for alignment.
3. No Unapproved Scope Creep: New ideas or UI changes are added to the backlog and evaluated in sequence rather than injected mid-task.

---

## Pending Features (Prioritized Queue)

### Item 1: LMS Assignments Tracker Polish & In-App Security
- Status: Queued (Awaiting User Initiation)
- Target Scope:
  - Descending sort on Completed tab (latest deadline/completion at top).
  - Hardened IPC: Activities receive only site key ("LMS" / "FEDENA") and decrypt locally inside private memory.
- Verification Gate:
  - Verify Completed tab displays in descending order.
  - Verify credentials pass securely without regression across LMS and Fedena activities.

---

### Item 2: Modernization Overhaul
- Status: Long-term Architecture
- Target Scope:
  - Migrate Db.java from raw SQLiteOpenHelper to Android Room.
  - Replace findViewById across all layouts with ViewBinding.
  - Migrate background scrapers to WorkManager workers.
- Verification Gate:
  - Zero regression across database persistence, UI, and background tasks.

---

## Completed Features

### Feature: Quick Settings Tile, 1×1 Home Screen Widget & Silent Error Feedback
- Implemented in: Version 3.0.0
- Summary: Built native Android Quick Settings Tile (`QuickLoginTileService`) allowing 1-tap captive portal connection directly from the top notification pulldown shade, alongside a 1×1 Home Screen Widget (`QuickLoginWidgetProvider`). Added `StatusNotifier` with dedicated silent notification channel (`rlogin_status_channel`, fixed ID 1001) for crisp, non-cluttering error alerts and tactile haptic vibration (1-tick success, double-buzz failure). Includes background auto-connect via `WifiAutoConnectManager`.

### Feature: Neo-Brutalist Theme & Dual-Layout Architecture
- Implemented in: Version 3.0.0
- Summary: Added complete Neo-Brutalist design system designed by Shyam James (@shyamjames) with bold typography, solid ink borders, hard drop shadows, and custom light and dark color palettes. Integrated dynamic layout switching across all core activities and added an inline password visibility toggle in the Credential Vault matching the Neo-Brutalist aesthetic.

### Feature: Application Identity Migration & Package Renaming
- Implemented in: Version 3.0.0
- Summary: Migrated Android application namespace and package ID from com.example.rlogin to production identifier com.rlogin.rlogin. Upgraded build configuration to versionCode 4 and versionName 3.0.0 for major milestone release.

### Feature: 2-Slot Release Notes Caching & Markdown Rich-Text Formatter
- Implemented in: Version 3.0.0
- Summary: Built a 2-slot self-pruning SQLite table (release_cache) that caches CURRENT and PENDING releases with an automatic promotion-on-launch lifecycle. Created GitHubUpdater.formatMarkdown to convert raw GitHub markdown into styled rich text with bold section titles, bullet points, and zero raw hashtag symbols in update and What's New dialogs.

### Feature: Settings Hub, Theming Engine & Updates Center
- Implemented in: Version 2.1.0
- Summary: Added dedicated Settings Hub with toolbar gear icon, dynamic dual-theme switcher (Classic Navy and Custom Theme), appearance mode selector (System, Light, Dark), in-app GitHub release checker, What's New release notes dialog, system window insets integration, and academic disclaimer and legal documentation.

### Feature: Moodle LMS Pending Assignments Tracker & Countdown Page
- Implemented in: Version 2.0.0
- Summary: Native in-app tracker with 6-state urgency lifecycle (Upcoming, Due Soon, Urgent, Overdue, No Set Deadline, Completed), dynamic multi-tier course discovery, 140-day semester retention window based on completion date, persistent manual closures ("Mark as Done" with Undo), one-tap direct submission, and SQLite offline caching.
