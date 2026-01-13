# 🍽️ MessMate – Smart Hostel Mess Management App

**MessMate** is an Android application built to digitize hostel mess operations and reduce food wastage through real-time meal planning and attendance tracking.

This project focuses on **real-world problem solving**, clean architecture, and modern Android development practices.

---

## ✨ Key Features

### 👨‍🎓 Student Side

* View daily mess menu (Breakfast, Lunch, Snacks, Dinner)
* Opt out of meals in advance to reduce food wastage
* Secure login using institute email domain
* Submit meal feedback

### 🧑‍💼 Admin Side

* Admin dashboard for mess management
* QR-based attendance using in-app camera
* Update menus dynamically
* Track meal opt-outs and attendance in real time

---

## 🛠 Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM
* **Dependency Injection:** Koin
* **Backend:** Firebase

  * Authentication (Email/Password)
  * Cloud Firestore
  * Firebase Storage
* **Async:** Kotlin Coroutines & Flow
* **Camera & Scanning:** CameraX + ML Kit (QR scanning)

---

## 📂 Project Structure

```
com.kshitiz.messmate
├── di              # Koin modules
├── ui
│   ├── auth        # Login & signup
│   ├── main        # Student dashboard & menu
│   ├── admin       # Admin portal & QR scanner
│   ├── profile     # User profile
│   └── theme       # Compose theme
├── util            # Utilities & constants
└── MessMateApp.kt  # Application entry point
```

---

## 🚀 What This Project Demonstrates

* End-to-end Android app development
* Real-time data syncing with Firebase
* Role-based access (Admin / Student)
* Modern UI using Jetpack Compose
* Clean separation of concerns with MVVM
* Practical use of camera and QR scanning

---

## 🔮 Future Improvements

* Nutritional information for meals
* Push notifications for menu updates
* Payment support for guest meals

---

### 👨‍💻 Developed by **Kshitiz Raj**
