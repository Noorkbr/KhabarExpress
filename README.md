# খাবার এক্সপ্রেস 🍛 (KhabarExpress)

### বাংলাদেশের দ্রুততম ফুড ডেলিভারি প্ল্যাটফর্ম 🇧🇩🚀

> **ডিজিটাল পার্টনার: কাশিমপুর দিনকাল** 🤝

[![Android](https://img.shields.io/badge/Android-24%2B-green)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-orange)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Proprietary-red)](LICENSE)

---

## 📱 Two Apps — Buyer + Seller

### 🛒 Buyer App (`/app`)
- **Package:** `com.khabarexpress.buyer`
- **App Name:** খাবার এক্সপ্রেস
- **Features:** Browse restaurants → Order food → Track delivery → Pay with bKash/Nagad/Rocket/COD
- **Location:** Text-based address input (no GPS/maps required)

### 🏪 Seller App (`/restaurant-app`)
- **Package:** `com.khabarexpress.seller`
- **App Name:** খাবার এক্সপ্রেস - বিক্রেতা
- **Features:** Manage menu → Accept orders → Track earnings → Update restaurant profile

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt |
| **Navigation** | Compose Navigation |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 |
| **Local Database** | Room 2.6 |
| **Preferences** | DataStore |
| **Real-time Updates** | Socket.IO |
| **Push Notifications** | Firebase Cloud Messaging (optional) |
| **Image Loading** | Coil 2.7 |
| **Build System** | Gradle 8.7.3 (KTS) |

> ✅ **No Mapbox** — Location is entered as text by buyers and sellers

---

## 🏦 Payment Methods (Bangladesh)

| Method | Type |
|--------|------|
| **bKash** | Mobile Banking |
| **Nagad** | Mobile Banking |
| **Rocket** | Mobile Banking |
| **Upay** | Mobile Banking |
| **ক্যাশ অন ডেলিভারি** | Cash on Delivery |
| **কার্ড পেমেন্ট** | Visa/Mastercard |

**Currency:** BDT (৳ Bangladeshi Taka)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Meerkat (2024.3.1) or newer
- JDK 11+
- Android SDK with API 24–35

### 1. Clone the Repository
```bash
git clone https://github.com/Noorkbr/KhabarExpress.git
cd KhabarExpress
```

### 2. Open in Android Studio
```
File → Open → Select the KhabarExpress folder
```

### 3. Build the Apps
```bash
# Build Buyer App (debug)
./gradlew :app:assembleDebug

# Build Seller App (debug)
./gradlew :restaurant-app:assembleDebug
```

**No Mapbox tokens needed!** The project uses text-based location input.

### 4. (Optional) Firebase Setup
For push notifications, add `google-services.json`:
- Download from [Firebase Console](https://console.firebase.google.com)
- Place `google-services.json` in `app/` for buyer app
- Place `google-services.json` in `restaurant-app/` for seller app

---

## 📦 Play Store Build

### Create Signing Keystore
```bash
keytool -genkey -v -keystore khabarexpress.jks -keyalg RSA -keysize 2048 -validity 10000 -alias khabarexpress
```

### Configure `keystore.properties`
```properties
storeFile=khabarexpress.jks
storePassword=your_store_password
keyAlias=khabarexpress
keyPassword=your_key_password
```

### Build Release AAB (for Play Store)
```bash
# Buyer App
./gradlew :app:bundleRelease

# Seller App
./gradlew :restaurant-app:bundleRelease
```

AAB files will be in:
- `app/build/outputs/bundle/release/app-release.aab`
- `restaurant-app/build/outputs/bundle/release/restaurant-app-release.aab`

---

## 📁 Project Structure

```
KhabarExpress/
├── app/                          # 🛒 Buyer App
│   ├── src/main/java/com/khabarexpress/buyer/
│   │   ├── data/                 # Data layer (models, API, Room DB)
│   │   ├── di/                   # Hilt dependency injection
│   │   ├── domain/               # Use cases & repository interfaces
│   │   ├── navigation/           # Navigation graph
│   │   ├── presentation/         # ViewModels & UI screens
│   │   ├── service/              # Background services
│   │   ├── ui/                   # Theme & common UI
│   │   ├── util/                 # Utility classes
│   │   ├── KhabarExpressApplication.kt
│   │   └── MainActivity.kt
│   └── src/main/res/
├── restaurant-app/               # 🏪 Seller App
│   └── src/main/java/com/khabarexpress/seller/
├── backend/                      # ⚙️ Node.js Backend API
├── admin-panel/                  # 🖥️ Web Admin Dashboard (React + Vite)
├── store-listing/                # 📋 Play Store Descriptions
│   ├── buyer-app/
│   ├── seller-app/
│   └── STORE_LISTING_CHECKLIST.md
├── PRIVACY_POLICY.md
├── TERMS_OF_SERVICE.md
├── settings.gradle.kts
├── build.gradle.kts
└── gradle/libs.versions.toml
```

---

## ⚙️ Backend

The Node.js backend is in `/backend`. See [backend/README.md](backend/README.md) for setup instructions.

**Railway Deployment:**
1. Set the Railway service root directory to `backend`
2. Add required environment variables (`PORT`, `NODE_ENV`, `DATABASE_URL`, `JWT_SECRET`, `REFRESH_TOKEN_SECRET`)
3. Deploy — Railway will detect the Dockerfile and build automatically
4. Verify: `curl https://<your-railway-url>/health`

See [backend/README.md](backend/README.md) for full Railway deployment details.

**Text-Based Address System:**
- Buyers type their delivery address (e.g., "কাশিমপুর, গাজীপুর")
- No geocoding API required
- Simple, reliable, works everywhere in Bangladesh

---

## 🖥️ Admin Panel (Web Dashboard)

A full-featured web admin dashboard is available at `admin-panel/`.

### Setup

```bash
# 1. Seed the admin user (run once)
cd backend
node src/seeds/seedAdmin.js

# 2. Start the admin panel
cd admin-panel
npm install
cp .env.example .env
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

### Default Admin Credentials

| Field    | Value              |
|----------|--------------------|
| Phone    | `+8801883688374`   |
| Password | `16741210@Noor`    |

### Features

| Page           | Description                                  |
|----------------|----------------------------------------------|
| 🏠 Dashboard   | KPIs, revenue chart, recent orders, profit summary |
| 📦 Orders      | Full order management with status updates    |
| 🏪 Restaurants | Approval/reject/suspend restaurants          |
| 👥 Users       | Manage users, ban/unban, change roles        |
| 🛵 Riders      | View all riders and statuses                 |
| 💳 Payments    | Transaction history, profit tracking, CSV export |
| 📊 Reports     | Financial reports with date range filters    |
| 🎟️ Promo Codes | Create and manage discount codes             |
| 📍 Zones       | Delivery zone management                     |
| ⚙️ Settings    | Platform config, payment gateway toggles     |

See [admin-panel/README.md](admin-panel/README.md) for full documentation.

---

## 📋 Version Info

| Property | Value |
|----------|-------|
| versionCode | 1 |
| versionName | 1.0.0 |
| minSdk | 24 (Android 7.0) |
| targetSdk | 35 (Android 15) |
| compileSdk | 35 |

---

## 🤝 Digital Partner

**কাশিমপুর দিনকাল** — Official Digital Partner of খাবার এক্সপ্রেস

---

## 📄 Legal

- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms of Service](TERMS_OF_SERVICE.md)

---

## 📞 Contact

**Email:** support@khabarexpress.com

---

*Copyright © 2025 KhabarExpress. All rights reserved.*  
*ডিজিটাল পার্টনার: কাশিমপুর দিনকাল*

