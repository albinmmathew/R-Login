# 📌 Features To Do & Roadmap

This document tracks upcoming features, planned enhancements, and architectural plans for **R-Login**.

---

## 1. Moodle LMS Pending Assignments Tracker & Countdown Page `[COMPLETED - v2.0.0]`

### 🎯 Objective
Integrate a native **Pending Assignments Tracker** for LMS Moodle directly into the app. Instead of relying on external email notifications, the app automatically logs in to LMS, extracts all upcoming assignment deadlines across all enrolled courses (including undated assignments), and displays them in a clean, interactive Android page with real-time countdown badges and direct submission links.

### 💡 Key Features
- **In-App Native View**: Rendered natively in a `RecyclerView` with `SwipeRefreshLayout` and segmented filters (`Pending`, `Overdue`, `Completed`).
- **6-State Assignment Lifecycle**:
  - 🟢 **Upcoming**: Due in 7+ days (up to 90 days out).
  - 🟡 **Due Soon**: Due within 7 days with countdown badge.
  - 🔴 **Urgent**: Due within 24 hours with prominent alert badge.
  - ⚠️ **Overdue**: Past deadline but not yet submitted; kept visible for late submissions.
  - 🔵 **No Set Deadline**: Submittable tasks without deadlines (e.g., project reports) tracked with `"No Set Deadline"` badge.
  - ✅ **Completed / Closed**:
    - **Auto-Closed**: Automatically detected as submitted/graded on Moodle. Auto-purged 14 days after deadline.
    - **Manually Closed**: Allows manual closing/dismissal for group submissions, offline lab evaluations, or optional tasks with full "Undo / Restore" capabilities. Auto-purged 14 days after completion.
- **Robust Multi-Source Discovery**:
  - Dual-tier course resolution: Calendar filter dropdown + navigation DOM + AJAX fallback.
  - Per-course assignment index scraping (`/mod/assign/index.php?id=<id>`) with column-agnostic cell parsing for 100% resilient title, date, and submission status detection.
- **Direct One-Tap Submission**:
  - Tapping an assignment card opens the exact assignment submission page in `WebActivity` with pre-filled credentials.
- **Offline Caching**:
  - Caches assignments locally in SQLite for instant startup display.

### 📐 Technical Architecture & Implementation Plan
1. **Data Model ([`AssignmentItem.java`](file:///d:/S3_RCSS/ANDROID/R-Login/app/src/main/java/com/example/rlogin/AssignmentItem.java))**:
   - Fields: `id`, `title`, `courseName`, `dueTimestamp`, `formattedDueDate`, `submissionUrl`, `isSubmitted`, `isManuallyClosed`.
2. **Database Helper ([`Db.java`](file:///d:/S3_RCSS/ANDROID/R-Login/app/src/main/java/com/example/rlogin/Db.java))**:
   - Tracks manually closed assignments and cached JSON responses.
3. **Scraper / Network Engine ([`AssignmentActivity.java`](file:///d:/S3_RCSS/ANDROID/R-Login/app/src/main/java/com/example/rlogin/AssignmentActivity.java))**:
   - Uses hidden `WebView` with `@JavascriptInterface Bridge` to authenticate using credentials from `Db.java`.
   - Queries Moodle's internal AJAX endpoint (`/lib/ajax/service.php` with `core_calendar_get_action_events_by_timesort`) with a fallback DOM parser for `/calendar/view.php?view=upcoming`.
4. **Adapter & UI Layout ([`AssignmentAdapter.java`](file:///d:/S3_RCSS/ANDROID/R-Login/app/src/main/java/com/example/rlogin/AssignmentAdapter.java), `activity_assignment.xml`, `item_assignment.xml`)**:
   - Renders cards with urgency color-coded badges, relative countdowns, "Submit" button, and "Mark as Done" action.
5. **Dashboard Integration ([`MainActivity.java`](file:///d:/S3_RCSS/ANDROID/R-Login/app/src/main/java/com/example/rlogin/MainActivity.java))**:
   - Adds an **"Assignments"** button inside the LMS service card next to "Open".

---

## 2. Auto-WiFi Background Reconnect Service

### 🎯 Objective
Automatically log into the M-WiFi captive portal (`http://172.16.0.20:8090/login.xml`) whenever the user's device connects to the campus Wi-Fi network, without requiring the user to manually open the app and tap "Connect".

---

## 3. Exam Schedule & Timetable Sync (Fedena)

### 🎯 Objective
Scrape upcoming exam schedules and timetable data from Fedena and display them in a native calendar/list view with exam reminders.
