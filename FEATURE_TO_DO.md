# 📌 Features To Do & Roadmap

This document tracks upcoming features, planned enhancements, and architectural plans for **R-Login**.

---

## 1. Moodle LMS Pending Assignments Tracker & Countdown Page

### 🎯 Objective
Integrate a native **Pending Assignments Tracker** for LMS Moodle directly into the app. Instead of relying on external email notifications, the app will automatically log in to LMS, extract all upcoming assignment deadlines across all enrolled courses, and display them in a clean, interactive Android page with real-time countdown badges and direct submission links.

### 💡 Key Features
- **In-App Native View**: No email setups required; pending assignments are rendered natively in a `RecyclerView` within the app.
- **Unlimited & Long-Term Tracking (No Top-3 / 14-Day Limit)**:
  - Connects to Moodle's internal service API (`/lib/ajax/service.php` using `core_calendar_get_action_events_by_timesort`) or multi-month calendar scraping.
  - Queries upcoming deadlines up to 90 days in advance, ensuring assignments due next month or later are never missed.
- **Urgency Color Coding & Countdowns**:
  - 🔴 **Urgent**: Due within 24 hours (e.g., *Due in 5 hours*)
  - 🟡 **Soon**: Due within 7 days
  - 🟢 **Upcoming**: Due in 2+ weeks
- **Direct One-Tap Submission**:
  - Tapping an assignment card opens the exact assignment submission page in `WebActivity` with pre-filled credentials.
- **Background WorkManager & Local Phone Notifications (Optional)**:
  - Periodically checks for upcoming deadlines in the background and triggers native Android system notifications (e.g., *"Assignment Due: Mobile App Dev due in 6 hours!"*).

### 📐 Technical Architecture & Implementation Plan
1. **Data Model ([`AssignmentItem.java`](file:///D:/S3_RCSS/ANDROID/SampleProject_Rlogin/app/src/main/java/com/example/sampleproject_rlogin/AssignmentItem.java))**:
   - Fields: `title`, `courseName`, `dueDateStr`, `dueTimestamp`, `submissionUrl`, `status`.
2. **Scraper / Network Engine ([`AssignmentActivity.java`](file:///D:/S3_RCSS/ANDROID/SampleProject_Rlogin/app/src/main/java/com/example/sampleproject_rlogin/AssignmentActivity.java))**:
   - Uses hidden `WebView` or `HttpURLConnection` to log in using LMS credentials from `Db.java`.
   - Executes JavaScript / JSON parsing to fetch action events from Moodle.
3. **Adapter & UI Layout ([`AssignmentAdapter.java`](file:///D:/S3_RCSS/ANDROID/SampleProject_Rlogin/app/src/main/java/com/example/sampleproject_rlogin/AssignmentAdapter.java) & [`activity_assignment.xml`](file:///D:/S3_RCSS/ANDROID/SampleProject_Rlogin/app/src/main/res/layout/activity_assignment.xml))**:
   - Renders cards in a `RecyclerView` with Material 3 styling and swipe-to-refresh (`SwipeRefreshLayout`).
4. **Dashboard Integration ([`MainActivity.java`](file:///D:/S3_RCSS/ANDROID/SampleProject_Rlogin/app/src/main/java/com/example/sampleproject_rlogin/MainActivity.java))**:
   - Adds a **"Pending Assignments"** button inside the LMS service card.

---

## 2. Auto-WiFi Background Reconnect Service

### 🎯 Objective
Automatically log into the M-WiFi captive portal (`http://172.16.0.20:8090/login.xml`) whenever the user's device connects to the campus Wi-Fi network, without requiring the user to manually open the app and tap "Connect".

---

## 3. Exam Schedule & Timetable Sync (Fedena)

### 🎯 Objective
Scrape upcoming exam schedules and timetable data from Fedena and display them in a native calendar/list view with exam reminders.
