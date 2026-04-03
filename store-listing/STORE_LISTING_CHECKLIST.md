# Play Store Submission Checklist — খাবার এক্সপ্রেস

## Pre-Submission Checklist

### App Configuration
- [ ] `versionCode = 1` set in both app/build.gradle.kts
- [ ] `versionName = "1.0.0"` set in both app/build.gradle.kts
- [ ] `minSdk = 24` (Android 7.0+)
- [ ] `targetSdk = 35`
- [ ] `compileSdk = 35`
- [ ] `applicationId = "com.khabarexpress.buyer"` (Buyer App)
- [ ] `applicationId = "com.khabarexpress.seller"` (Seller App)

### Signing
- [ ] Keystore file created (`keystore.jks`)
- [ ] `keystore.properties` configured with keystore path and passwords
- [ ] Release build signed with release keystore
- [ ] Upload key certificate saved safely

### Build & Test
- [ ] Release APK/AAB builds successfully
- [ ] No Mapbox dependencies
- [ ] ProGuard/R8 minification enabled
- [ ] App tested on physical device (Android 7+)
- [ ] App tested on emulator (API 24-35)
- [ ] No crash on launch
- [ ] All screens tested

### Buyer App (`com.khabarexpress.buyer`)
- [ ] Splash/Login screen shows "ডিজিটাল পার্টনার: কাশিমপুর দিনকাল"
- [ ] Text-based address input works
- [ ] Restaurant browsing works
- [ ] Cart & checkout works
- [ ] bKash/Nagad/Rocket payment methods visible
- [ ] Order history works
- [ ] Profile screen works

### Seller App (`com.khabarexpress.seller`)
- [ ] Dashboard shows order statistics
- [ ] New order notifications work
- [ ] Order accept/reject works
- [ ] Menu management works
- [ ] Restaurant profile update works
- [ ] "ডিজিটাল পার্টনার: কাশিমপুর দিনকাল" branding visible

### Play Store Store Listing
- [ ] App icon (512x512 PNG, no alpha)
- [ ] Feature graphic (1024x500 PNG or JPG)
- [ ] At least 2 screenshots (phone: 16:9 or 9:16, min 320px)
- [ ] Short description (max 80 chars) — in `store-listing/buyer-app/short-description.txt`
- [ ] Full description (max 4000 chars) — in `store-listing/buyer-app/full-description.txt`
- [ ] App category: Food & Drink
- [ ] Content rating questionnaire completed
- [ ] Privacy policy URL added (required!)

### Legal Documents
- [ ] `PRIVACY_POLICY.md` created and hosted (e.g., GitHub Pages)
- [ ] `TERMS_OF_SERVICE.md` created and hosted
- [ ] Data safety form completed in Play Console

### Backend Setup
- [ ] Backend server deployed
- [ ] API URL updated in `app/src/main/java/com/khabarexpress/buyer/di/NetworkModule.kt`
- [ ] Firebase project configured (optional, for push notifications)
- [ ] `google-services.json` added to app/ directory (if using Firebase)

### Pre-Launch
- [ ] Internal testing done (Play Console internal track)
- [ ] Closed testing (alpha/beta) done
- [ ] Production rollout: Start with 10-20% rollout

## Build Commands

### Generate Release AAB (recommended for Play Store):
```bash
# Buyer App
./gradlew :app:bundleRelease

# Seller App
./gradlew :restaurant-app:bundleRelease
```

### Generate Release APK:
```bash
# Buyer App
./gradlew :app:assembleRelease

# Seller App
./gradlew :restaurant-app:assembleRelease
```

## Important Links
- [Play Console](https://play.google.com/console)
- [Play Store Policy](https://play.google.com/about/developer-content-policy/)
- [Data Safety Guide](https://support.google.com/googleplay/android-developer/answer/10787469)
