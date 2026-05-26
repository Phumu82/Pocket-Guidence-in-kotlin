# 💰 Pocket Guidance — Personal Finance Android App

> **A comprehensive personal finance management app built in Kotlin for Android, featuring expense tracking, budget management, savings goals, shared expense splitting, and a gamified financial learning system.**

---

## 📱 App Overview

Pocket Guidance helps South African users take control of their finances. Built entirely in **Kotlin** with **Room Database**, the app runs fully offline — no internet connection required. All data is stored securely on the device.

The app was designed with students and young professionals in mind — people who need a simple, visual way to track spending, set goals, and build financial literacy.

---

## 🎬 Demonstration Video

▶️ **[Watch the full app demo on YouTube](https://youtube.com/(https://youtu.be/v5Hv45jkJYw))**

> The video demonstrates all features running on a real Android device with a full voice-over walkthrough.

---

## 📸 Screenshots

| Splash Screen | Login | Dashboard |
|---|---|---|
![Login](screenshots/login.png<img width="319" height="715" alt="Screenshot 2026-05-26 130404" src="https://github.com/user-attachments/assets/37aa4d91-038c-4408-b81e-f1d1577101f8" />
) | ![Dashboard](screenshots/dashboard.png) |

| Goals | Reports | Earn & Learn |
|---|---|---|
| ![Goals](screenshots/goals.png) | ![Reports](screenshots/reports.png) | ![Earn](screenshots/earn.png) |

---

## ✨ Features

### 🔐 Authentication
- Secure signup and login with **SHA-256 password hashing**
- Session persists across app restarts via `SharedPreferences`
- 4-step onboarding: income → rent budget → first goal → dashboard

### 💸 Expense & Income Tracking
- Log income and expenses with category, description, date
- **Attach receipt photos** via camera or gallery
- Date range filter to view transactions by period
- Category breakdown with running totals

### 💳 Budget Management
- Set spending limits per category
- **Visual progress bars** showing % of budget used
- ⚠️ Warning alerts when 90%+ of budget is consumed
- Monthly budget goal tracking

### 🎯 Savings Goals
- Create goals with target amount, deadline, and frequency
- **Live savings calculator** — shows how much to save daily/weekly/monthly
- 🎉 Goal completion celebration dialog
- Dashboard shows average progress across all goals

### 📊 Reports & Graphs
- **Pie chart** — spending breakdown by category
- **Bar chart** — income vs expenses vs balance
- **Savings progress bar chart** — current vs target per goal
- **User-selectable date period** for all transaction views
- Minimum and maximum spending goals displayed visually

### 📈 Visual Goal Progress 
- Per-category budget progress bars with colour coding (green → orange → red)
- Goal progress bars showing % achieved
- Savings calculator preview (daily/weekly/monthly amounts needed)
- Dashboard summary card showing balance, income, and expenses

### 🏅 Gamification 
- **Quiz-based rewards** — complete financial literacy quizzes to earn R7–R10 per correct answer
- Earnings are **deposited directly into your account balance** via Room DB
- **Course completion tracking** with progress bars per course
- **Goal celebration dialogs** when savings goals are reached
- 4 quiz courses: Budgeting, Banking, Credit & Loans, Investing 101

---

## ⭐ Own Feature 1 — Earn & Learn Quiz System

> **Documented here as required by the assignment brief.**

### What it is
A gamified financial education system where users complete multiple-choice quizzes on financial topics and earn **real money rewards** that are deposited directly into their app balance.

### How it works
1. User opens **Earn & Learn** from the Dashboard
2. Selects one of 4 financial courses (Budgeting, Banking, Credit & Loans, Investing 101)
3. Answers 10 multiple-choice questions
4. Each **correct answer** earns a **random reward between R7 and R10**
5. Reward is immediately recorded as an income transaction in Room DB
6. User's balance updates in real time on the Dashboard
7. Course completion is tracked with a progress bar

### Why it's unique
- Combines **financial education with tangible rewards**
- Random reward amount (R7–R10) adds excitement and unpredictability
- Earning is **per question** — if you close the app mid-quiz you keep what you earned
- Course completion percentages persist via `SharedPreferences`

### Where to find it
- Dashboard → **Earn** quick action card
- `EarnActivity.kt` — full quiz engine
- `CourseAdapter.kt` + `QuizAnswerAdapter.kt` — RecyclerView adapters
- `activity_earn.xml` — two-panel layout (course list ↔ quiz)

---

## ⭐ Own Feature 2 — Shared Expense Groups

> **Documented here as required by the assignment brief.**

### What it is
A group expense splitting system that allows multiple people (flat mates, friends, family) to share expenses and track who owes who.

### How it works
1. User creates a **group** with a name and comma-separated members (e.g. "Alice, Bob, Carol")
2. Any group member can **add a shared expense** — specifying the title, amount, who paid, and split type
3. The app calculates **balances** — who owes money and who is owed money
4. **Settle** button records a settlement transaction in Room DB
5. **Remind** button sends a notification to the person who owes money

### Balance Calculation
```
Alice paid R150 dinner for 3 people:
  Share per person = R150 ÷ 3 = R50
  Alice: +R100 (she's owed R100)
  Bob:   -R50  (he owes R50)
  Carol: -R50  (she owes R50)
```

### Why it's unique
- Members are stored as JSON in the `GroupEntity` — no complex join tables needed
- Settling a debt records a `settlement` type transaction affecting the main balance
- Supports Equal, Percentage, and Custom split types

### Where to find it
- Dashboard → **Groups** quick action card
- `GroupsActivity.kt` — full groups screen
- `GroupAdapter.kt` — balance display with settle/remind buttons
- `GroupEntity.kt` + `GroupExpenseEntity.kt` — Room DB entities
- `GroupDao.kt` + `GroupExpenseDao.kt` — database queries

---

## 🗂️ Project Structure

```
PocketGuidance/
├── app/
│   ├── src/main/
│   │   ├── java/com/pocketguidance/
│   │   │   ├── data/
│   │   │   │   ├── db/
│   │   │   │   │   ├── entities/      ← 9 Room entities (tables)
│   │   │   │   │   ├── dao/           ← 9 DAO interfaces (queries)
│   │   │   │   │   └── AppDatabase.kt ← Room database singleton
│   │   │   │   └── repository/
│   │   │   │       ├── FinanceRepository.kt
│   │   │   │       └── AuthRepository.kt
│   │   │   ├── ui/
│   │   │   │   ├── activities/        ← 17 screens
│   │   │   │   └── adapters/          ← 8 RecyclerView adapters
│   │   │   ├── utils/                 ← SessionManager, FormatUtils, etc.
│   │   │   └── PocketGuidanceApp.kt   ← Application class
│   │   └── res/
│   │       ├── layout/                ← 29 XML layouts
│   │       ├── drawable/              ← Logo + shape drawables
│   │       └── values/                ← Colors, themes, strings
│   └── build.gradle
├── gradle/libs.versions.toml
└── README.md
```

---

## 🗃️ Database Schema (Room DB)

| Entity | Table | Purpose |
|---|---|---|
| `UserEntity` | `users` | Login credentials (SHA-256 hashed passwords) |
| `TransactionEntity` | `transactions` | All income, expenses, contributions |
| `BudgetEntity` | `budgets` | Per-category spending limits |
| `GoalEntity` | `goals` | Savings goals with progress |
| `CategoryEntity` | `categories` | Default + custom spending categories |
| `UserPrefsEntity` | `user_prefs` | Currency, income, dark mode, onboarding |
| `GroupEntity` | `groups` | Expense sharing groups |
| `GroupExpenseEntity` | `group_expenses` | Individual shared expenses |
| `InvestmentEntity` | `investments` | Portfolio tracking |

All entities use **Foreign Keys** to `UserEntity` with `CASCADE DELETE` — deleting a user removes all their data.

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| **Kotlin** | 100% Kotlin — no Java |
| **Room DB** | Local SQLite database with reactive Flows |
| **Kotlin Coroutines** | All database operations run on background threads |
| **ViewBinding** | Type-safe XML view access |
| **MPAndroidChart** | Pie charts and bar charts |
| **Glide** | Receipt photo and avatar image loading |
| **Gson** | JSON serialisation for group members |
| **AndroidX SplashScreen** | Proper splash screen API (API 26+) |
| **Material Components** | Buttons, cards, text fields, bottom sheets |
| **FileProvider** | Secure camera photo URI sharing |

---

## 🔐 Security

- Passwords stored as **SHA-256 hashes** — never in plain text
- Receipt photos stored in **app-private storage** — inaccessible to other apps
- Camera uses **FileProvider** for secure URI sharing
- Session stored in `SharedPreferences` with `MODE_PRIVATE`

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android device or emulator running API 26+ (Android 8.0+)
- JDK 17

### Build & Run
```bash
# Clone the repository
git clone https://github.com/Phumu82/PocketGuidance.git

# Open in Android Studio
File → Open → select PocketGuidance folder

# Sync Gradle (downloads dependencies automatically)
# Then run on device or emulator
```

### Build APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK will be in `app/build/outputs/apk/debug/`

---

## 🧪 Automated Testing (GitHub Actions)

This project uses **GitHub Actions** to automatically build and test the app on every push.

See `.github/workflows/build.yml` for the CI/CD configuration.

[![Android CI](https://github.com/Phumu82/PocketGuidance/actions/workflows/build.yml/badge.svg)](https://github.com/Phumu82/PocketGuidance/actions/workflows/build.yml)

---

## 📋 GitHub Actions Setup

```yaml
# .github/workflows/build.yml
name: Android CI
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Gradle
        run: ./gradlew assembleDebug
      - name: Run Tests
        run: ./gradlew test
```

---

## 📝 Logging

The app uses Android's `Log` class throughout to demonstrate understanding of the code flow:

```kotlin
Log.d("DashboardActivity", "Balance refreshed: $balance")  // Debug
Log.i("AuthRepository", "Login success userId=$id")         // Info
Log.w("FinanceRepository", "Category already exists")       // Warning
Log.e("AddExpenseActivity", "Failed to save transaction")   // Error
```

Logs can be viewed in Android Studio's **Logcat** filtered by tag (e.g. `DashboardActivity`).

---

## 🎨 Design Decisions

### Brand Identity
The Pocket Guidance logo features a **blue pocket** (representing savings/wallet) with a **green upward arrow** (representing financial growth). These colours are used throughout:
- Dark background `#000000` — Auth screens match logo background
- Primary green `#4A7C59` — Buttons, progress bars, accents
- Light green `#8FBC8F` — Secondary accents, taglines

### Architecture
- **No ViewModel** — Activities communicate directly with Repositories via `lifecycleScope`
- **Repository pattern** — All business logic isolated from UI
- **Reactive UI** — `Flow` from Room means the UI updates automatically when data changes

### UX Decisions
- Bottom sheets for forms (less jarring than new screens)
- Inline validation errors (not toast popups)
- Progress bars with colour shifts (green → orange → red as you approach limits)
- Celebration dialogs to make milestones feel rewarding

---

## 📚 References

- Android Room Documentation: https://developer.android.com/training/data-storage/room
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Glide Image Loading: https://github.com/bumptech/glide
- AndroidX SplashScreen: https://developer.android.com/guide/topics/ui/splash-screen
- Kotlin Coroutines Guide: https://kotlinlang.org/docs/coroutines-guide.html
- Material Design Components: https://material.io/develop/android
- SHA-256 Hashing: https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
- GitHub Actions Android: https://github.com/marketplace/actions/automated-build-android-app-with-github-action

---

## 👤 Author

** Phumudzo Oasis Munyai **
Student Number: ST10450008
Module: IMAD 25/26/27
Institution: The Independent Institute of Education (IIE)
Year: 2026

---

## 📄 License

This project was created for academic purposes as part of the IIE IMAD module assessment.

---

*Pocket Guidance — Take control of your finances, one rand at a time. 💚*
