# UI Improvements - Profile Setup & Wallet

## Changes Implemented

### 1. Wallet Screen - Removed Coin Animation ✅
**File:** `WalletScreen.kt`

**Before:**
- Balance displayed with animated counting effect (1200ms animation)
- Used `animateFloatAsState` with tween animation

**After:**
- Balance displays instantly without animation
- Cleaner, faster user experience
- Removed unused animation imports

**Impact:** Wallet screen loads faster, no distracting animations

---

### 2. Profile Setup - Changed to Gender Selection ✅
**Files:** 
- Android: `ProfileSetupScreen.kt`, `ProfileSetupViewModel.kt`, `SetupProfileUseCase.kt`, `UserRepository.kt`, `UserDto.kt`
- Backend: `user/handler.go`, `user/dto.go`

**Before:**
- Asked for "Interested In" during registration
- User manually selected MALE or FEMALE
- Only `interested_in` column was updated

**After:**
- Now asks for "Your Gender" 
- Automatically sets "Interested In" as opposite gender
- Both `gender` and `interested_in` columns are updated in database

**Database Schema:**
```sql
users table:
- gender VARCHAR         -- User's actual gender (MALE/FEMALE)
- interested_in VARCHAR  -- User's preference (opposite of gender)
```

**Android Flow:**
```kotlin
// User selects gender
val selectedGender = "MALE"

// System auto-sets interestedIn as opposite
val interestedIn = if (selectedGender == "MALE") "FEMALE" else "MALE"

// Both sent to backend
viewModel.setupProfile(nickname, selectedGender, interestedIn)
```

**Backend API:**
```json
PATCH /api/v1/users/profile
{
  "nickname": "John",
  "gender": "MALE",
  "interestedIn": "FEMALE"
}
```

**Impact:** 
- Better UX - users only answer one question
- System infers preference automatically
- Both gender and preference stored separately in database

---

### 3. User Settings - Interested In Option ✅
**File:** `UserSettingScreen.kt` (Already implemented)

**Current Behavior:**
- Users can change "Interested In" preference in settings
- Shows dialog with MALE/FEMALE options
- Updates only `interested_in` column (gender remains unchanged)

**No changes needed** - This was already working correctly

---

## User Flow

### New User Registration:
1. Enter phone number
2. Verify OTP
3. **Profile Setup Screen:**
   - Enter nickname
   - Select gender (Male/Female)
   - **Grant notification permission (Android 13+)**
     - Shows permission card with status
     - Cannot proceed without granting permission
     - Shows dialog if permission denied
   - System automatically sets "Interested In" as opposite
   - Both gender + interestedIn sent to backend
4. Navigate to home

### Existing User:
- Can change "Interested In" preference anytime in User Settings
- Gender field is not editable after initial setup
- Independent of gender selection

---

## Notification Permission

### Android 13+ (API 33+):
- **POST_NOTIFICATIONS** permission is required
- **New users:** Must grant permission during profile setup to proceed
- **Existing users:** Permission dialog shown once on first app launch after reinstall
- Shows visual card with permission status (new users)
- Displays friendly dialog explaining why permission is needed
- Continue button is disabled until permission granted (new users only)
- Existing users can dismiss dialog ("Not Now" option)

### Android 12 and below:
- Notification permission granted by default
- No user action required
- Permission card/dialog not shown

### Implementation Details:

**New Users (ProfileSetupScreen):**
```kotlin
// Blocking - must grant to continue
var notificationPermissionGranted by remember { mutableStateOf(false) }

Button(onClick = {
    if (!notificationPermissionGranted) {
        Toast.makeText(context, "Please grant notification permission", Toast.LENGTH_LONG).show()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        // Proceed with profile setup
    }
})
```

**Existing Users (HomeScreen):**
```kotlin
// Non-blocking - shows once, can dismiss
LaunchedEffect(Unit) {
    if (!isNotificationPermissionGranted) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasAskedBefore = prefs.getBoolean("notification_permission_asked", false)
        
        if (!hasAskedBefore) {
            showNotificationDialog = true
            prefs.edit().putBoolean("notification_permission_asked", true).apply()
        }
    }
}
```

### User Experience:

**New Users:**
1. Complete OTP verification
2. Enter nickname
3. Select gender
4. **Must grant notification permission** (blocking)
5. Profile setup complete

**Existing Users (Reinstall):**
1. Complete OTP verification
2. Navigate to home
3. **Friendly dialog appears once** (non-blocking)
4. Can choose "Enable" or "Not Now"
5. Dialog won't show again

---

## Database Updates

### Backend Changes:
1. `UpdateProfileRequest` now accepts `gender` field
2. `UpdateProfile` handler updates both `gender` and `interested_in` columns
3. Both fields are optional (nullable) for backward compatibility

### Migration:
No migration needed - `gender` and `interested_in` columns already exist in `users` table

---

## Testing Checklist

- [x] Wallet screen shows balance without animation
- [x] Profile setup asks for "Your Gender" (not "Interested In")
- [x] After OTP, new users see profile setup
- [x] Selecting Male sets gender=MALE, interestedIn=FEMALE
- [x] Selecting Female sets gender=FEMALE, interestedIn=MALE
- [x] Backend receives and stores both gender and interestedIn
- [x] User settings still allows changing "Interested In"
- [x] No compilation errors in Android or Backend
- [x] Notification permission requested on Android 13+ (new users - blocking)
- [x] Cannot proceed without granting notification permission (new users)
- [x] Permission dialog shown if denied (new users)
- [x] Permission card shows current status (new users)
- [x] Existing users see notification dialog once on first launch
- [x] Existing users can dismiss dialog with "Not Now"
- [x] Dialog doesn't show again after first time (uses SharedPreferences)

