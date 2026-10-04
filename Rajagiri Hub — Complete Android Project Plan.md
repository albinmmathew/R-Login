# Rajagiri Hub
### Secure Campus Service Access & Credential Manager

**Platform:** Android  
**Language:** Java  
**Database:** SQLite through Room  
**Primary focus:** Secure local credential management + one-tap campus service access  
**Target environment:** Rajagiri College campus services

---

# 1. Project Overview

Rajagiri Hub is an Android application designed to provide a single, convenient entry point for commonly used Rajagiri campus services.

Students currently have to locate different websites/services and repeatedly deal with authentication. The application will centralize these services and provide a simpler experience.

The application will support two major types of services:

### Network-based authentication

Example:

- M-WiFi

The application will perform the required authentication request in the background and display the result to the user.

### Web-based services

Examples:

- Moodle
- Fedena
- Other Rajagiri portals

The application will manage the credentials where appropriate and provide one-tap access to the corresponding service.

The authentication mechanism for each web service will be investigated individually rather than assuming that all services work in the same way.

---

# 2. Core Objective

The main objective is:

> Build a secure Android application that stores campus-service credentials locally in encrypted form and provides convenient one-tap access to supported Rajagiri services.

The project should demonstrate practical Android development rather than being simply a collection of static screens.

---

# 3. Core Features

## 3.1 Home Dashboard

The main screen provides access to configured campus services.

Example:

```text
RAJAGIRI HUB

Campus Connectivity

M-WiFi
[ CONNECT ]

Campus Services

Moodle       Fedena
[ LOGIN ]    [ LOGIN ]

Other Services
[ OPEN ]     [ OPEN ]

Settings
```

The dashboard is intended to be the primary interaction point.

---

# 4. M-WiFi Module

M-WiFi is the most concrete automated integration because a working PowerShell implementation already exists.

The current authentication mechanism sends an HTTP POST request to:

```text
http://172.16.0.20:8090/login.xml
```

with parameters including:

```text
mode=191
username
password
a=<timestamp>
producttype=0
```

The response is then inspected to determine whether the login succeeded.

---

## 4.1 M-WiFi User Flow

### First use

```text
User opens M-WiFi
        ↓
No credentials stored
        ↓
Setup/Credential page
        ↓
Enter username/password
        ↓
Encrypt credentials
        ↓
Store securely
```

### Subsequent use

```text
User presses CONNECT
        ↓
Retrieve encrypted credentials
        ↓
Decrypt credentials
        ↓
Create HTTP POST
        ↓
Send request in background
        ↓
Receive login.xml response
        ↓
Parse response
        ↓
Success / Failure
        ↓
Display Toast/status
```

---

# 5. M-WiFi Response Handling

The existing script distinguishes between:

```text
LIVE
```

and failure/rejection responses such as:

```text
LOGIN
REJECTED
```

The Android application will implement equivalent response handling.

Expected user experience:

```text
CONNECT
   ↓
"Connecting..."
   ↓
"Connected successfully"
```

or:

```text
CONNECT
   ↓
"Login failed: Invalid credentials"
```

The HTTP operation should not run directly on the Android main/UI thread.

---

# 6. Credential Management

The application will provide a centralized credential-management mechanism.

Instead of creating completely separate credential systems for Moodle, Fedena, M-WiFi, etc., the application will use a generic service/credential model.

Example:

```text
Credentials

M-WiFi
Username: 25204008
Password: ********

Moodle
Username: ********
Password: ********

Fedena
Username: ********
Password: ********
```

The user can add, edit, or remove credentials.

---

# 7. Database

The application will use:

> SQLite through Android Room.

Room will provide the abstraction layer while SQLite remains the underlying local database.

Possible entities include:

## Service

```text
id
name
url
type
authentication_method
```

Example:

```text
M-WiFi
Moodle
Fedena
```

## Credential

```text
id
service_id
username
encrypted_password
```

Credentials should never be stored as plaintext passwords.

---

# 8. Security Architecture

Security is an important part of the project.

The application should not simply store:

```text
username = ...
password = ...
```

in plaintext SQLite.

Instead:

```text
User credentials
       ↓
Encryption
       ↓
Encrypted credential
       ↓
Room / SQLite
```

The encryption key should itself be protected using Android's secure key-storage mechanisms.

The intended architecture is:

```text
                 Android Keystore
                       │
                       ↓
                 Encryption Key
                       │
                       ↓
Credential → Encryption → SQLite
                              │
                              ↓
                       Encrypted data
```

When authentication is required:

```text
SQLite
  ↓
Encrypted credential
  ↓
Keystore-protected decryption
  ↓
Plain credential temporarily available
  ↓
Authentication request
```

The plaintext password should not be unnecessarily retained.

---

# 9. Optional Biometric Protection

A biometric lock can be added after the basic credential system works.

Possible flow:

```text
Open Rajagiri Hub
       ↓
Biometric authentication
       ↓
Successful
       ↓
Credential vault available
```

This can use Android's `BiometricPrompt`.

The biometric feature is an enhancement rather than a prerequisite for the first working version.

---

# 10. Moodle and Fedena

Moodle and Fedena should not be treated exactly like M-WiFi.

M-WiFi has a known HTTP authentication mechanism.

Moodle/Fedena use normal web authentication, and the exact authentication/session mechanism needs to be investigated.

The planned process is:

```text
Investigate login mechanism
        ↓
Determine authentication method
        ↓
Create small prototype
        ↓
Determine whether credentials/session
can be handled appropriately
        ↓
Choose implementation
```

Possible approaches may include:

- Browser-based authentication
- WebView-based authentication
- Supported web authentication/session mechanisms
- Simple browser launching where automated authentication is not technically appropriate

The final method will depend on the actual authentication architecture of the services.

The project should **not assume that an authenticated session can always be transferred from the Android application to the external browser**.

---

# 11. Browser Integration

For services that simply need to be opened in the user's browser, Android's implicit Intent mechanism can be used.

Conceptually:

```text
Rajagiri Hub
      ↓
ACTION_VIEW Intent
      ↓
Android
      ↓
Default browser
      ↓
Campus website
```

This allows the application to avoid forcing the user to use a specific browser.

---

# 12. Planned Application Areas

The application will be kept relatively compact.

## 12.1 Home

Main dashboard containing service cards and actions.

---

## 12.2 M-WiFi Setup

Used when M-WiFi credentials have not yet been configured.

Responsibilities:

- Username entry
- Password entry
- Credential validation
- Secure storage

---

## 12.3 Credential Manager

Central location for saved service credentials.

Responsibilities:

- View configured services
- Add credentials
- Edit credentials
- Delete credentials

---

## 12.4 Service Detail / Credential Page

A reusable screen for configuring an individual service.

Possible information:

```text
Service name
Website
Username
Password
Authentication type
```

For example:

```text
Moodle

Website:
moodle.rajagiri.edu

Username:
********

Password:
********

[ SAVE ]
```

---

## 12.5 Settings

Possible settings:

```text
Security
  App Lock
  Biometric Unlock

Credentials
  Manage Credentials

History
  Clear History

About
  Application information
```

---

# 13. Optional Login History

Login history is **not part of the essential core functionality**.

It is a supporting feature that can make the database more meaningful.

Example:

```text
Login History

Today

✓ M-WiFi
  09:12 AM
  Login successful

✓ Moodle
  09:18 AM
  Login successful

✕ Fedena
  10:42 AM
  Login failed
```

Possible database structure:

```text
LOGIN_HISTORY

id
service_id
timestamp
status
message
```

This feature can demonstrate:

- INSERT
- SELECT
- Sorting
- Filtering
- Deletion

If the application starts becoming unnecessarily large, this feature can be omitted.

---

# 14. Proposed Architecture

The application will follow a layered architecture rather than placing everything inside `MainActivity`.

Conceptually:

```text
                    UI
                     │
                     ↓
              View / Activity
                     │
                     ↓
                 ViewModel
                     │
                     ↓
                Repository
              /            \
             ↓              ↓
        Room Database     Network
             │              │
          SQLite           OkHttp
                             │
                             ↓
                         M-WiFi
```

The exact architecture will be learned and implemented progressively.

The purpose is to avoid creating a large Activity containing:

- UI code
- Database code
- Encryption code
- HTTP code
- Authentication logic

all in one file.

---

# 15. Main Technical Components

The project will teach and use:

## Android

- Activities
- Activity lifecycle
- XML layouts
- Views
- Event listeners
- Intents
- Navigation
- Android resources
- Permissions
- Connectivity APIs

## Java

- Classes
- Interfaces
- Objects
- Collections
- Exception handling
- Threads/concurrency
- Callbacks

## Database

- SQLite
- Room
- Entities
- DAO
- CRUD
- Relationships
- Queries

## Networking

- HTTP
- HTTP POST
- Request body
- Headers
- Content-Type
- URL encoding
- Timeouts
- OkHttp
- XML parsing
- JSON parsing where applicable

## Security

- Encryption
- AES concepts
- Android Keystore
- Secure key management
- BiometricPrompt
- Secure credential handling

## Android background execution

- Main/UI thread
- Background operations
- Asynchronous HTTP requests
- UI updates after background work

## Web integration

- Browser Intents
- Web authentication
- Sessions/cookies
- WebView concepts where required

---

# 16. Learning Phase

Before building the actual Rajagiri Hub, the required Android concepts will be learned through small independent examples.

The learning sequence will be:

```text
1. Android Studio & project structure
        ↓
2. AndroidManifest
        ↓
3. Activity
        ↓
4. Activity lifecycle
        ↓
5. XML layouts
        ↓
6. Views
        ↓
7. View IDs
        ↓
8. Java ↔ XML interaction
        ↓
9. Event listeners
        ↓
10. Intent
        ↓
11. Explicit vs implicit Intent
        ↓
12. Activity navigation
        ↓
13. RecyclerView
        ↓
14. Dialogs
        ↓
15. Local storage
        ↓
16. SQLite
        ↓
17. Room
        ↓
18. HTTP fundamentals
        ↓
19. OkHttp
        ↓
20. XML/JSON parsing
        ↓
21. Background operations
        ↓
22. Android connectivity
        ↓
23. Encryption
        ↓
24. Android Keystore
        ↓
25. Biometrics
        ↓
26. Browser/web authentication
```

---

# 17. Learning Method

The learning process will follow a specific methodology.

## During concept learning

Each lesson will contain:

1. Goal
2. Detailed concept explanation
3. Important terminology
4. How the concept works
5. Small live example
6. Sample Java/Android code
7. Explanation of important code
8. Small exercise
9. Expected result
10. Connection to the future project

The examples will be intentionally small so that the concepts are understandable.

---

# 18. Project Implementation Method

After the learning phase, the actual Rajagiri Hub will be built separately.

The assistant will **not provide the complete project code by default**.

Instead, implementation will proceed step-by-step.

For each implementation step:

```text
Goal
  ↓
Concept required
  ↓
Detailed explanation
  ↓
Files/components to create
  ↓
Specific task for the developer
  ↓
Small sample code where needed
  ↓
Developer implements
  ↓
Test
  ↓
Debug
  ↓
Next step
```

For new concepts, sample code will be provided.

Complete project code will only be provided when explicitly requested.

---

# 19. Project Implementation Roadmap

## Phase 1 — Project foundation

Create:

- Android project
- Package structure
- Main Activity
- Basic XML UI
- Resource structure

---

## Phase 2 — Dashboard

Implement:

- Main dashboard
- Service cards
- Buttons
- Click listeners
- Navigation

At this stage the service actions can initially be placeholders.

---

## Phase 3 — Service model

Design the service representation.

Example:

```text
Service
 ├── name
 ├── URL
 ├── icon
 ├── type
 └── authentication method
```

This prevents the application from becoming a collection of hard-coded special cases.

---

## Phase 4 — Database

Introduce Room.

Implement:

```text
Service Entity
Credential Entity
DAO
Database
Repository
```

Start with basic CRUD.

---

## Phase 5 — Credential management

Implement:

```text
Add credential
Edit credential
Delete credential
Retrieve credential
```

Initially understand the database flow before adding encryption.

---

## Phase 6 — Encryption

Introduce:

```text
AES
Android Keystore
Encryption/decryption
```

Replace plaintext password storage with encrypted storage.

---

## Phase 7 — HTTP networking

Learn:

```text
HTTP
POST
Request body
Headers
Content-Type
URL encoding
Timeouts
OkHttp
```

Use a simple test endpoint first.

Do not immediately connect it to M-WiFi.

---

## Phase 8 — XML response parsing

Learn how to process the server response.

Then implement the equivalent of:

```text
LIVE
LOGIN
REJECTED
message
```

from the existing M-WiFi script.

---

## Phase 9 — Background execution

Move the network request away from the UI thread.

Desired flow:

```text
Button
 ↓
Background request
 ↓
Response
 ↓
UI update
```

---

## Phase 10 — M-WiFi integration

Combine:

```text
Room
+
Encryption
+
Credential retrieval
+
OkHttp
+
XML parsing
+
Background execution
```

Final M-WiFi flow:

```text
CONNECT
   ↓
Credential Repository
   ↓
Encrypted credential
   ↓
Decrypt
   ↓
HTTP POST
   ↓
login.xml
   ↓
Parse response
   ↓
Success / Failure
   ↓
Toast / Status
```

This will be the first major complete module.

---

# 20. Wi-Fi and Connectivity

After the authentication request works, investigate Android's connectivity APIs.

Possible functionality:

```text
Wi-Fi available?
      ↓
Connected to expected network?
      ↓
Allow/indicate M-WiFi authentication
```

The application should distinguish between:

```text
Device has internet
```

and:

```text
Device is connected to Rajagiri/M-WiFi
```

where technically possible.

---

# 21. Moodle/Fedena Integration

After M-WiFi is complete, investigate each web service separately.

For each:

```text
Identify login page
       ↓
Identify authentication mechanism
       ↓
Test manually
       ↓
Determine session behavior
       ↓
Determine Android-compatible approach
       ↓
Implement
```

This prevents the project architecture from being based on assumptions.

---

# 22. Security Hardening

After functionality works:

- Protect credentials
- Minimize plaintext credential lifetime
- Use Keystore appropriately
- Add biometric protection if desired
- Validate user input
- Avoid logging passwords
- Avoid storing sensitive information unnecessarily
- Handle authentication errors safely

---

# 23. Optional Login History

If included:

```text
Authentication attempt
       ↓
Result
       ↓
Store history record
       ↓
Display in History page
```

The history should not contain passwords.

---

# 24. Final Application Flow

The target application will ultimately behave approximately like this:

```text
                    OPEN APP
                       │
                       ↓
                   HOME PAGE
                       │
          ┌────────────┼────────────┐
          ↓            ↓            ↓
       M-WiFi        Moodle       Fedena
          │            │            │
          ↓            ↓            ↓
      Connect       Login/Open    Login/Open
          │            │            │
          ↓            └─────┬──────┘
   Background HTTP            │
          │                   ↓
          ↓                 Browser/
   XML Response             Web Flow
          │
      ┌───┴────┐
      ↓        ↓
   SUCCESS   FAILURE
      │        │
      ↓        ↓
    Status    Error
      │
      ↓
 Optional History
```

---

# 25. Database Architecture

The conceptual database will eventually resemble:

```text
SERVICE
────────────────────
id
name
url
type
authentication_method


CREDENTIAL
────────────────────
id
service_id
username
encrypted_password


LOGIN_HISTORY
────────────────────
id
service_id
timestamp
status
message
```

Relationship:

```text
SERVICE
   │
   ├────────────── CREDENTIAL
   │
   └────────────── LOGIN_HISTORY
```

The exact schema will be finalized during the database-design phase.

---

# 26. What Makes This More Than a Generic Login App

The project isn't simply:

> "An app containing buttons that open websites."

Its technical core is:

```text
Secure Credential Vault
        +
Multiple Authentication Strategies
        +
Android Networking
        +
Campus Wi-Fi Authentication
        +
Local Database
        +
Encryption
        +
Android System Integration
```

The M-WiFi integration is particularly useful because it involves a real authentication protocol rather than merely opening a webpage.

The application also demonstrates that different services can use different authentication mechanisms while being presented through one consistent interface.

---

# 27. Features to Avoid Overloading the Project

The following are **not initially required**:

- Cloud synchronization
- Social features
- Chat
- Complex analytics
- AI
- ML
- Unnecessary gamification
- Large administrative backend

The goal is to make the application technically strong rather than artificially large.

---

# 28. Minimum Viable Project

If time becomes limited, the project should prioritize:

```text
1. Android UI
2. Service dashboard
3. Room database
4. Encrypted credential storage
5. M-WiFi HTTP authentication
6. Background operation
7. Success/error handling
8. Browser access for web services
```

This alone forms a solid project.

---

# 29. Recommended Final Version

The preferred final version is:

```text
Rajagiri Hub
│
├── Home Dashboard
│
├── M-WiFi
│   ├── Credential setup
│   ├── Secure storage
│   ├── Background authentication
│   └── Connection result
│
├── Moodle
│   ├── Credential management
│   └── Web authentication/access
│
├── Fedena
│   ├── Credential management
│   └── Web authentication/access
│
├── Credential Manager
│
├── Optional Login History
│
└── Settings
    ├── Security
    └── Biometric protection
```

---

# 30. Development Philosophy

The project will be developed in two distinct stages.

## Stage A — Learn

Learn Android concepts independently using small examples.

No large project code.

## Stage B — Build

Implement Rajagiri Hub step-by-step.

No complete code unless explicitly requested.

The developer should understand each component before moving to the next one.

The final goal is not merely:

> "An Android application that works."

It is:

> **An Android application that the developer understands well enough to explain, modify, debug, and defend as an MCA project.**