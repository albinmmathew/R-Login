# R-Login Roadmap Backup & Approval Gate Protocol

This document serves as the master backup plan and sequential execution tracker for R-Login.

---

## Governance Rules

1. Sequential Execution: Each priority item must be reviewed, tested, and explicitly approved by the user before work on the next item begins.
2. Review Before Execution: Before modifying code for an approved phase, a technical explanation will be provided for alignment.
3. No Unapproved Scope Creep: New ideas or UI changes are added to the backlog and evaluated in sequence rather than injected mid-task.

---

## Pending Features (Prioritized Queue)

### Item 1: Fast Auto-Connect on Rajagiri Wi-Fi Detection
- Status: Awaiting User Approval
- Target Scope:
  - NetworkCallback listener for sub-second Wi-Fi detection.
  - Detection of campus gateway (172.16.0.20 / M-WiFi).
  - Network-bound auto-login (overriding active mobile data).
  - In-app toggle on dashboard and connection notification.
- Verification Gate:
  - Test Wi-Fi connection on campus; verify auto-login fires within 1-2 seconds with mobile data enabled.

---

### Item 2: In-App Release Notes Markdown Formatting & Rich Text Viewer
- Status: Queued (Blocked on Item 1)
- Target Scope:
  - Process raw GitHub Releases markdown into Android Spanned rich text formatting.
  - Strip literal hashtag (#) and asterisk characters and style section titles with bold typography.
  - Provide a scrollable, styled modal view for update notes and What's New dialog.
- Verification Gate:
  - Verify release notes modal renders headers in bold, bullet lists cleanly, and contains zero raw markdown syntax characters.

---

### Item 3: LMS Assignments Tracker Polish & In-App Security
- Status: Queued (Blocked on Item 2)
- Target Scope:
  - Descending sort on Completed tab (latest deadline/completion at top).
  - Hardened IPC: Activities receive only site key ("LMS" / "FEDENA") and decrypt locally inside private memory.
- Verification Gate:
  - Verify Completed tab displays in descending order.
  - Verify credentials pass securely without regression across LMS and Fedena activities.

---

### Item 4: Modernization Overhaul
- Status: Long-term Architecture
- Target Scope:
  - Migrate Db.java from raw SQLiteOpenHelper to Android Room.
  - Replace findViewById across all layouts with ViewBinding.
  - Migrate background scrapers to WorkManager workers.
- Verification Gate:
  - Zero regression across database persistence, UI, and background tasks.

---

## Completed Features

### Feature: Settings Hub, Theming Engine & Updates Center
- Implemented in: Version 2.1.0
- Summary: Added dedicated Settings Hub with toolbar gear icon, dynamic dual-theme switcher (Classic Navy and Custom Theme), appearance mode selector (System, Light, Dark), in-app GitHub release checker, What's New release notes dialog, system window insets integration, and academic disclaimer and legal documentation.

### Feature: Moodle LMS Pending Assignments Tracker & Countdown Page
- Implemented in: Version 2.0.0
- Summary: Native in-app tracker with 6-state urgency lifecycle (Upcoming, Due Soon, Urgent, Overdue, No Set Deadline, Completed), dynamic multi-tier course discovery, 140-day semester retention window based on completion date, persistent manual closures ("Mark as Done" with Undo), one-tap direct submission, and SQLite offline caching.
