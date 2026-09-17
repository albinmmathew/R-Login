# R-Login — Campus Connectivity & Credential Manager

R-Login is an Android application designed for Rajagiri College students. It centralizes authentication for common campus services, automates Wi-Fi network logins, and parses student attendance reports into native, actionable analytics.

---

## Features

### 1. M-WiFi Background Auto Connect
- Performs an asynchronous background HTTP POST request to the M-WiFi captive portal (`http://172.16.0.20:8090/login.xml`).
- Parses XML response payloads (`<status>LIVE</status>`) and displays real-time connection status via Toast notifications without requiring a browser page to be open.

### 2. Consolidated Attendance Calculator (Fedena Integration)
- **Native Card View**: Scrapes the student attendance table from Fedena in the background and presents the data in a native Android `RecyclerView` with clear subject cards.
- **Total Classes Held Analytics**: Automatically calculates total classes conducted for each subject using the formula:
  $$\text{Total Classes Held} = \text{Round}\left(\frac{\text{Hours Attended}}{\text{Percentage}} \times 100\right)$$
- **Sticky Top Toggle**: Features a fixed top toggle switch `[ Without OD | With Duty Leave (OD) ]` that stays pinned while scrolling, letting students compare regular vs. duty leave attendance instantly.

### 3. Web Portal Integration (Fedena & LMS Moodle)
- Built-in `WebView` browser with one-time credential injection.
- Preserves native CSRF tokens (`authenticity_token`) to ensure valid login submissions.
- Includes a top navigation toolbar with back button history, page titles, and an *"Open in Chrome"* menu option.

### 4. Hardware-Backed Encryption & Security
- **AES-GCM Encryption**: Credentials are encrypted before saving using secret keys generated in the hardware-backed **Android KeyStore**.
- **Local SQLite Vault**: Stored inside an isolated, sandboxed SQLite database (`app.db`). Zero data is sent to external servers.
- **Material 3 Design**: Styled in Deep Navy Blue (`#002868`) with full support for **Light & Dark Modes** and high-contrast status bar controls.

---

## Project Architecture

```text
com.example.sampleproject_rlogin/
├── MainActivity.java     # Main Dashboard with Material 3 Service Cards & Status Badges
├── EditActivity.java     # Credential Vault screen with Outlined Text Inputs & Eye Toggle
├── WebActivity.java      # Integrated Browser Activity with Top Toolbar & Chrome launcher
├── ReportActivity.java   # Native Attendance Report Activity with JavaScript DOM parser
├── ReportAdapter.java    # RecyclerView Adapter for rendering Subject Attendance Cards
├── SubjectItem.java      # Data Model for subject metrics & calculated totals
├── Net.java              # Background Executor thread for M-WiFi HTTP POST requests
├── Db.java               # SQLite OpenHelper database manager
└── Crypt.java            # Hardware-backed AES-GCM encryption & decryption manager
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

## How to Build & Run

### Prerequisites
- Android Studio (2022.3.1 or newer recommended)
- Android SDK API 24+ (Android 7.0 or higher)
- JDK 11 or higher

### Steps
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/SampleProject_Rlogin.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync and resolve project dependencies.
4. Connect an Android device or start an emulator.
5. Click **Run** (`Shift + F10`) or build the APK via **Build -> Build APKs**.

---

## License & Disclaimer

This project is created for educational and academic purposes as part of the MCA program. All campus service names and portals (M-WiFi, Fedena, LMS Moodle) belong to their respective owners. Credentials are stored 100% locally on the user's personal device.
