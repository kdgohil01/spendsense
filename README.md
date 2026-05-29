# 💰 SpendSense

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-4285F4?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**End-to-End Encrypted Expense Tracker with Beautiful UI**

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Architecture](#-architecture) • [Security](#-security) • [Contributing](#-contributing)

</div>

---

## 📖 Overview

**SpendSense** is a modern Android expense tracking application that prioritizes your privacy with end-to-end encryption. Built with Jetpack Compose and Material Design 3, it offers a beautiful, intuitive interface for managing your daily expenses with weekly and monthly insights.

### Why SpendSense?

- 🔐 **End-to-End Encryption**: All expense data is encrypted using AES-GCM with Android Keystore
- 📊 **Visual Analytics**: Beautiful charts and graphs to visualize your spending patterns
- 📅 **Dual View Modes**: Track expenses by week (7 days) or month (31 days)
- 💾 **Secure Cloud Sync**: Optional Google account integration for encrypted backups
- 🎨 **Modern UI**: Built with Jetpack Compose and Material Design 3
- ⚡ **Offline First**: Works perfectly without internet connection
- 🌙 **Dark Mode Ready**: Supports system theme preferences

---

## ✨ Features

### Core Functionality

- **Expense Management**
  - Add, view, and delete expenses with ease
  - Categorize expenses (Food, Bills, Groceries, Leisure, Travel, Other)
  - Track expenses by specific days of the week or month
  - Real-time expense calculations and summaries

- **Budget Tracking**
  - Set custom budget limits
  - Visual progress indicators showing budget usage
  - Over-budget warnings and alerts
  - Remaining budget calculations

- **Data Visualization**
  - Weekly bar charts showing spending trends
  - Category-based donut charts for monthly overview
  - Interactive day-by-day expense breakdown
  - Color-coded categories for easy identification

- **Security & Privacy**
  - AES-GCM encryption for all sensitive data
  - Android Keystore integration for secure key management
  - Local-first architecture with optional cloud sync
  - No third-party analytics or tracking

### User Experience

- **Splash Screen**: Elegant animated splash screen with app branding
- **Tab Navigation**: Switch between weekly and monthly views seamlessly
- **Interactive Calendar**: Tap any day to view or add expenses
- **Smart Filtering**: Automatic filtering based on selected time period
- **Responsive Design**: Optimized for various screen sizes

---

## 📱 Screenshots

> *Add your app screenshots here*

```
[Splash Screen] [Dashboard] [Add Expense] [Weekly View] [Monthly View]
```

---

## 🚀 Installation

### Prerequisites

- **Android Studio**: [Download here](https://developer.android.com/studio)
- **Android SDK**: API Level 24 (Android 7.0) or higher
- **Java**: Version 11 or higher

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/SpendSense.git
   cd SpendSense
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select `File > Open`
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure API Key (Optional)**
   - Create a `.env` file in the project root
   - Add your Gemini API key:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```
   - Get your API key from [Google AI Studio](https://ai.google.dev/)

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click `Run > Run 'app'` or press `Shift + F10`

### Building APK

**Option 1: Using Android Studio**
```
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

**Option 2: Using Command Line**
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing configuration)
./gradlew assembleRelease
```

**Option 3: Using Batch Script (Windows)**
```bash
build-apk.bat
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🏗️ Architecture

SpendSense follows modern Android development best practices with a clean architecture approach.

### Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room Persistence Library |
| **Dependency Injection** | Manual (Factory Pattern) |
| **Async Operations** | Kotlin Coroutines + Flow |
| **Encryption** | Android Keystore + AES-GCM |
| **Material Design** | Material 3 (Material You) |

### Project Structure

```
app/src/main/java/com/spendsense/app/
├── crypto/
│   └── CryptoManager.kt          # Encryption/Decryption logic
├── data/
│   ├── AppDatabase.kt            # Room database configuration
│   ├── Expense.kt                # Expense entity (encrypted)
│   ├── ExpenseDao.kt             # Database access object
│   └── ExpenseRepository.kt      # Data layer abstraction
├── ui/
│   ├── components/
│   │   └── CanvasCharts.kt       # Custom chart components
│   ├── theme/
│   │   ├── Color.kt              # Color definitions
│   │   ├── Theme.kt              # Theme configuration
│   │   └── Type.kt               # Typography
│   ├── SpendSenseApp.kt          # Main UI composables
│   └── SpendSenseViewModel.kt    # Business logic & state
└── MainActivity.kt                # Entry point
```

### Key Components

#### 1. **CryptoManager** (`crypto/CryptoManager.kt`)
- Handles all encryption/decryption operations
- Uses Android Keystore for secure key storage
- Implements AES-GCM with 128-bit authentication tag
- Fallback obfuscation for edge cases

#### 2. **Room Database** (`data/AppDatabase.kt`)
- Stores encrypted expense data locally
- Single source of truth for all expense records
- Automatic migration support

#### 3. **Repository Pattern** (`data/ExpenseRepository.kt`)
- Abstracts data operations from UI
- Handles encryption/decryption transparently
- Provides reactive data streams using Flow

#### 4. **ViewModel** (`ui/SpendSenseViewModel.kt`)
- Manages UI state and business logic
- Handles user interactions
- Coordinates data operations
- Manages cloud sync simulation

#### 5. **Compose UI** (`ui/SpendSenseApp.kt`)
- Declarative UI built with Jetpack Compose
- Material Design 3 components
- Custom canvas-based charts
- Smooth animations and transitions

---

## 🔒 Security

SpendSense takes your privacy seriously with multiple layers of security:

### Encryption Implementation

- **Algorithm**: AES-GCM (Advanced Encryption Standard - Galois/Counter Mode)
- **Key Storage**: Android Keystore System
- **Key Size**: 256-bit encryption keys
- **Authentication**: 128-bit GCM authentication tag

### What Gets Encrypted?

- ✅ Expense titles
- ✅ Expense amounts
- ❌ Categories (for filtering/analytics)
- ❌ Dates (for calendar functionality)

### Data Storage

- **Local Database**: Room database with encrypted fields
- **Cloud Backup**: Encrypted data only (when enabled)
- **No Plain Text**: Sensitive data never stored in plain text

### Security Best Practices

```kotlin
// Example: How data is encrypted before storage
val encryptedTitle = CryptoManager.encrypt(title)
val encryptedAmount = CryptoManager.encrypt(amount.toString())

// Stored in database as encrypted strings
Expense(
    titleEncrypted = encryptedTitle,
    amountEncrypted = encryptedAmount,
    // ... other fields
)
```

---

## 🎨 UI/UX Highlights

### Design Philosophy

- **Material You**: Follows Material Design 3 guidelines
- **Accessibility**: High contrast ratios and readable fonts
- **Intuitive Navigation**: Clear visual hierarchy
- **Responsive Feedback**: Animations and state changes

### Color Scheme

- **Primary**: Emerald Green (`#00C897`) - Trust and growth
- **Surface**: Dark backgrounds for reduced eye strain
- **Accent**: Category-specific colors for quick identification

### Custom Components

- **Weekly Bar Chart**: Canvas-based visualization of daily spending
- **Category Donut Chart**: Proportional spending by category
- **Interactive Calendar**: Touch-responsive day selector
- **Budget Progress**: Visual gauge with percentage indicators

---

## 🧪 Testing

The project includes comprehensive testing setup:

### Test Types

- **Unit Tests**: Business logic and ViewModel tests
- **Instrumented Tests**: Database and UI tests
- **Screenshot Tests**: Visual regression testing with Roborazzi

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Screenshot tests
./gradlew verifyRoborazziDebug
```

### Test Coverage

- ViewModel logic
- Database operations
- Encryption/Decryption
- UI component rendering

---

## 📦 Dependencies

### Core Libraries

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose")

// Room Database
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")
ksp("androidx.room:room-compiler")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.lifecycle:lifecycle-runtime-compose")
```

### Testing Libraries

```kotlin
// Unit Testing
testImplementation("junit:junit")
testImplementation("org.robolectric:robolectric")

// Screenshot Testing
testImplementation("io.github.takahirom.roborazzi:roborazzi")
testImplementation("io.github.takahirom.roborazzi:roborazzi-compose")

// Android Testing
androidTestImplementation("androidx.test.ext:junit")
androidTestImplementation("androidx.test.espresso:espresso-core")
```

---

## 🛣️ Roadmap

### Planned Features

- [ ] **Export/Import**: CSV and JSON export functionality
- [ ] **Recurring Expenses**: Support for subscription tracking
- [ ] **Multi-Currency**: Support for different currencies
- [ ] **Receipt Scanner**: OCR for receipt scanning
- [ ] **Widgets**: Home screen widgets for quick expense entry
- [ ] **Biometric Lock**: Additional security layer
- [ ] **Custom Categories**: User-defined expense categories
- [ ] **Reports**: Detailed monthly/yearly reports
- [ ] **Notifications**: Budget limit reminders
- [ ] **Backup Scheduling**: Automatic cloud sync

### Future Enhancements

- [ ] Wear OS companion app
- [ ] Tablet optimization
- [ ] Multi-language support
- [ ] Advanced analytics with AI insights
- [ ] Shared budgets for families

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

### Getting Started

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Contribution Guidelines

- Follow Kotlin coding conventions
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

### Code Style

- Use Kotlin idiomatic code
- Follow Material Design guidelines
- Keep functions small and focused
- Add comments for complex logic

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 SpendSense

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com

---

## 🙏 Acknowledgments

- Built with [Android Studio](https://developer.android.com/studio)
- Powered by [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Icons](https://fonts.google.com/icons)
- Inspired by modern expense tracking needs

---

## 📞 Support

If you encounter any issues or have questions:

- **Issues**: [GitHub Issues](https://github.com/yourusername/SpendSense/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/SpendSense/discussions)
- **Email**: support@spendsense.app

---

## ⭐ Show Your Support

If you find this project useful, please consider giving it a star on GitHub! It helps others discover the project and motivates continued development.

---

<div align="center">

**Made with ❤️ and Kotlin**

[⬆ Back to Top](#-spendsense)

</div>
