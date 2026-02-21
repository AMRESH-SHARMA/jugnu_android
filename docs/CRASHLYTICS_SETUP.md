# Firebase Crashlytics Setup

## Overview
Firebase Crashlytics has been integrated to automatically track and report app crashes in production.

## What Was Added

### 1. Dependencies (build.gradle.kts)
```kotlin
// Project-level
id("com.google.firebase.crashlytics") version "3.0.2" apply false

// App-level
id("com.google.firebase.crashlytics")
implementation("com.google.firebase:firebase-crashlytics")
```

### 2. Initialization (MyApp.kt)
- Crashlytics initialized in `onCreate()`
- Automatic crash collection enabled
- Custom keys added: app_version, version_code
- User ID set when available

### 3. User Tracking (SessionManager.kt)
- User ID automatically set in Crashlytics when user logs in
- Helps identify which users are experiencing crashes

## Features

### Automatic Crash Reporting
- ✅ All uncaught exceptions automatically reported
- ✅ Stack traces with line numbers
- ✅ Device info (model, OS version, etc.)
- ✅ App version tracking
- ✅ User ID tracking (when logged in)

### Custom Keys
- `app_version`: Version name (e.g., "15.0")
- `version_code`: Version code (e.g., 15)
- `user_id`: Account ID (set on login)

## Viewing Crashes

### Firebase Console
1. Go to: https://console.firebase.google.com
2. Select your project
3. Navigate to: Crashlytics (left sidebar)
4. View crashes, stack traces, and affected users

### Key Metrics
- Crash-free users percentage
- Number of crashes
- Affected devices
- Most common crashes
- Crash trends over time

## Testing Crashlytics

### Force a Test Crash
Add this button in debug builds to test:

```kotlin
Button(onClick = {
    throw RuntimeException("Test Crash for Crashlytics")
}) {
    Text("Force Crash (Debug Only)")
}
```

### Verify Setup
1. Force a crash in debug build
2. Restart the app (crashes are sent on next launch)
3. Wait 5-10 minutes
4. Check Firebase Console → Crashlytics

## Best Practices

### 1. Add Custom Logs
```kotlin
FirebaseCrashlytics.getInstance().log("User attempted to make call")
```

### 2. Add Custom Keys
```kotlin
FirebaseCrashlytics.getInstance().setCustomKey("call_type", "video")
```

### 3. Record Non-Fatal Exceptions
```kotlin
try {
    // risky operation
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
}
```

### 4. Set User Properties
```kotlin
FirebaseCrashlytics.getInstance().setCustomKey("user_role", "LISTENER")
```

## Privacy Considerations

### Data Collected
- Stack traces
- Device info (model, OS version)
- App version
- User ID (account ID)
- Custom keys and logs

### GDPR Compliance
- User ID is anonymized (just a number)
- No PII (name, email, phone) is sent
- Users can opt-out via app settings (if implemented)

## Troubleshooting

### Crashes Not Appearing
1. Wait 5-10 minutes after crash
2. Ensure app was restarted after crash
3. Check internet connection
4. Verify `google-services.json` is present
5. Check Firebase Console project settings

### Disable in Debug
To disable Crashlytics in debug builds:
```kotlin
if (BuildConfig.DEBUG) {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
}
```

## Cost
- ✅ **100% FREE**
- ✅ Unlimited crashes
- ✅ Unlimited users
- ✅ No premium tier

## Next Steps

### Optional Enhancements
1. Add breadcrumb logging for user actions
2. Add custom keys for call state
3. Add non-fatal exception tracking
4. Set up crash alerts (email/Slack)
5. Add opt-out setting for users

### Monitoring
- Check Crashlytics dashboard weekly
- Fix high-priority crashes first
- Monitor crash-free users percentage
- Track crash trends after releases

## Resources
- [Firebase Crashlytics Docs](https://firebase.google.com/docs/crashlytics)
- [Android Setup Guide](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Best Practices](https://firebase.google.com/docs/crashlytics/customize-crash-reports)
