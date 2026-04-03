# খাবার এক্সপ্রেস (KhabarExpress)

A modern food delivery platform built for Bangladesh.

> **ডিজিটাল পার্টনার: কাশিমপুর দিনকাল**

## Apps

### Buyer App (`/app`)
- Package: `com.khabarexpress.buyer`
- Name: **খাবার এক্সপ্রেস**
- Browse restaurants, order food, track deliveries

### Seller App (`/restaurant-app`)
- Package: `com.khabarexpress.seller`
- Name: **খাবার এক্সপ্রেস - বিক্রেতা**
- Manage menu, accept orders, track earnings

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Navigation**: Compose Navigation
- **Networking**: Retrofit + OkHttp
- **Database**: Room
- **Real-time**: Socket.IO
- **Push Notifications**: Firebase Cloud Messaging
- **Image Loading**: Coil

## Build Requirements

- Android Studio Meerkat or newer
- JDK 11+
- minSdk 24, targetSdk 35
- Gradle 8.13

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Add `google-services.json` to `restaurant-app/` (for Firebase)
4. Build and run

## Version

- versionCode: 1
- versionName: 1.0.0

## License

Copyright © 2025 KhabarExpress. All rights reserved.
