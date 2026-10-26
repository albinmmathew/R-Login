# R-Login — Campus Connectivity & Credential Manager

R-Login is an Android application designed for Rajagiri College of Social Sciences students. It centralizes authentication for common campus services, automates Wi-Fi network logins, parses student attendance reports into native analytics, and tracks LMS Moodle assignments.

---

## Download App (APK)

If you are a student looking to install and use the app on your Android device:

1. Visit the [**Latest GitHub Release**](https://github.com/albinmmathew/R-Login/releases/latest).
2. Under the **Assets** section, download the latest `app-release.apk`.
3. Open the downloaded APK on your device to install.
   *(If prompted, enable "Install unknown apps" for your browser or file manager).*

> **Google Play Protect Notice:**  
> Because R-Login is distributed directly via GitHub releases rather than the Google Play Store, Play Protect may display an "Unrecognized app" or "Harmful app blocked" prompt upon installation. Tap **"More details"** -> **"Install anyway"**. The application is open-source, safe, and stores all student credentials encrypted 100% locally on your personal device.

---

## Features

### 1. M-WiFi Background Auto Connect
- Performs an asynchronous background HTTP POST request to the M-WiFi captive portal (`http://172.16.0.20:8090/login.xml`).
- Direct network binding via Wi-Fi interface ensuring seamless login even when mobile data is active.
- Parses XML response payloads (`<status>LIVE</status>`) and displays real-time connection status via Toast notifications without requiring a browser page to remain open.

### 2. Consolidated Attendance Calculator (Fedena Integration)
- **Native Card View**: Scrapes the student attendance table from Fedena in the background and presents the data in a native Android `RecyclerView` with clear subject cards.
- **Total Classes Held Analytics**: Automatically calculates total classes conducted for each subject using the formula:
  $$\text{Total Classes Held} = \text{Round}\left(\frac{\text{Hours Attended}}{\text{Percentage}} \times 100\right)$$
- **Sticky Top Toggle**: Features a fixed top toggle switch `[ Without OD | With Duty Leave (OD) ]` that stays pinned while scrolling, letting students compare regular vs. duty leave attendance instantly.

### 3. Moodle LMS Assignment Tracker & Due Date Management
- **Urgency Lifecycles**: 6 status tiers (Urgent <24h, Due Soon <3 days, Upcoming, Overdue, No Set Deadline, Completed).
- **Offline Caching**: Caches parsed assignments locally in SQLite for instant offline viewing.
- **140-Day Semester Retention**: Tracks completed assignments for a 140-day window before automatic cleanup.
- **Manual Completion & Quick Links**: Allows manual "Mark as Done" with Undo and direct 1-tap links to assignment submission pages.

### 4. Settings Hub & Dynamic Theming Engine
- **Dedicated Settings Screen**: Accessible from the toolbar gear icon on the main dashboard.
- **Dual Theme Support**: Switch seamlessly between Classic Navy and Custom Theme.
- **Appearance Switcher**: Choose between System Default, Light Mode, and Dark Mode.
- **Updates & Release Notes**: Check for updates directly inside the app with integrated release changelog.

### 5. Hardware-Backed Encryption & Security
- **AES-GCM Encryption**: Credentials are encrypted before saving using secret keys generated in the hardware-backed **Android KeyStore**.
- **Local SQLite Vault**: Stored inside an isolated, sandboxed SQLite database (`app.db`). Zero data is sent to external servers.

---

## Release Notes & Updates

- **In-App:** In the app, navigate to **Settings -> Updates & Releases -> What's New** to view release highlights and new features directly. Tap **"View on GitHub"** inside the dialog to open the full release notes and contributor credits online.
- **Online:** All historical release notes, APK assets, and changelogs are published on the [GitHub Releases](https://github.com/albinmmathew/R-Login/releases) page.

---

## Project Architecture

```text
com.rlogin.rlogin/
├── MainActivity.java        # Main Dashboard with Material 3 Service Cards & Status Badges
├── SettingsActivity.java    # Settings Hub: Theme selection, Dark Mode toggle, Updates, About
├── ThemeManager.java        # Centralized theme and night mode manager
├── AssignmentActivity.java  # Moodle LMS Assignment Tracker with filters & offline cache
├── AssignmentAdapter.java   # RecyclerView Adapter for assignment cards & countdown badges
├── AssignmentItem.java      # Data model for assignments with urgency calculation
├── EditActivity.java        # Credential Vault screen with Outlined Text Inputs & Eye Toggle
├── WebActivity.java         # Integrated Browser Activity with Top Toolbar & Chrome launcher
├── ReportActivity.java      # Native Attendance Report Activity with JavaScript DOM parser
├── ReportAdapter.java       # RecyclerView Adapter for rendering Subject Attendance Cards
├── SubjectItem.java         # Data Model for subject metrics & calculated totals
├── GitHubUpdater.java       # In-app background update checker and APK installer
├── Net.java                 # Wi-Fi network routing & captive portal authentication
├── Db.java                  # SQLite database manager for credentials & assignment cache
└── Crypt.java               # Hardware-backed AES-GCM encryption & decryption manager
```

---

## Tech Stack & Technologies Used

- **Language**: Pure Java
- **Architecture**: Activity-based Layered Pattern
- **Database**: SQLite (`SQLiteOpenHelper`)
- **Security**: AES-GCM Encryption + Android KeyStore API
- **Networking**: `HttpURLConnection` + `WebView` JavaScript Interface (`@JavascriptInterface`)
- **UI Toolkit**: Google Material 3 Design Components (`MaterialCardView`, `MaterialButtonToggleGroup`, `TextInputLayout`)

---

## How to Build & Run (For Developers)

### Prerequisites
- Android Studio (2022.3.1 or newer recommended)
- Android SDK API 24+ (Android 7.0 or higher)
- JDK 11 or higher

### Steps
1. Clone this repository:
   ```bash
   git clone https://github.com/albinmmathew/R-Login.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync and resolve project dependencies.
4. Connect an Android device or start an emulator.
5. Click **Run** (`Shift + F10`) or build the APK via **Build -> Build APKs**.

---

## Disclaimer & Copyright

Copyright (c) 2026 Albin M Mathew. All rights reserved.

This application is created for educational and academic purposes as part of the MCA program. All campus service names and portals (M-WiFi, Fedena, LMS Moodle) belong to their respective owners. Credentials are stored 100% locally on the user's personal device. Zero personal data or credentials are collected or shared externally.
