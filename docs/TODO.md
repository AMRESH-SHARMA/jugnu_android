# TODO - Call System Issues

## High Priority

### 1. Add Accept/Reject Notification Actions (Background Scenario)
**Status:** Not Implemented  
**Priority:** High  
**Impact:** User Experience

**Problem:**
When callee's app is in background/killed and receives an incoming call notification, they can only open the app. There are no Accept/Reject action buttons on the notification.

**Current Behavior:**
- Notification shows with full-screen intent only
- User must open app to accept/reject call
- Poor UX compared to native phone calls

**Required Changes:**
1. Add Accept/Reject action buttons to `IncomingCallNotificationManager.showIncomingCall()`
2. Create `BroadcastReceiver` to handle Accept/Reject actions from notification
3. In receiver, call `AcceptCall` or `RejectCall` use cases
4. Handle RTM login if needed (app might be killed)

**Files to Modify:**
- `jugnu_android/app/src/main/java/com/example/app/core/call/notification/IncomingCallNotificationManager.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/call/CallNotificationReceiver.kt`

**Reference:**
- Android CallStyle API: https://developer.android.com/reference/android/app/Notification.CallStyle
- Use `Notification.CallStyle.forIncomingCall()` for Android 12+

---

### 2. Backend FCM Notifications for Accept/Reject Events
**Status:** Not Implemented  
**Priority:** High  
**Impact:** Reliability

**Problem:**
When callee accepts/rejects a call, only RTM (peer-to-peer) notification is sent to caller. If caller's app is in background, they won't receive the notification because RTM requires active connection.

**Current Behavior:**
- Callee accepts → RTM sent to caller (works only if caller's app is foreground)
- Callee rejects → RTM sent to caller (works only if caller's app is foreground)
- If caller is in background, they never know call was accepted/rejected

**Required Changes:**
1. In `AcceptCall` handler, send FCM to caller after accepting
2. In `RejectCall` handler, send FCM to caller after rejecting
3. Android: Handle `EVENT_CALL_ACCEPTED` and `EVENT_CALL_REJECTED` in `FcmService`

**Files to Modify:**
- `jugnu_backend/internal/module/call/handler.go` (AcceptCall, RejectCall methods)
- `jugnu_android/app/src/main/java/com/example/app/services/fcm/FcmService.kt`

**Implementation Example:**
```go
// In AcceptCall handler, after saving to DB:
go func() {
    var session user.UserSession
    err := database.DB.Raw(`
        SELECT us.fcm_token
        FROM users u
        JOIN user_sessions us ON us.user_id = u.id
        WHERE u.account_id = ? AND us.is_active = true
        ORDER BY us.id DESC
        LIMIT 1
    `, call.CallerAccountID).Scan(&session).Error

    if err == nil && session.FCMToken != "" {
        payload := map[string]string{
            "event":     constants.EventCallAccepted,
            "callId":    req.CallID,
            "channel":   call.Channel,
            "rtcToken":  rtcToken,
        }
        _ = fcm.SendToDevice(session.FCMToken, payload)
    }
}()
```

---

## Medium Priority

### 3. Migrate isProfileSetup Logic from Client to Server
**Status:** Not Implemented  
**Priority:** Medium  
**Impact:** Architecture, Data Consistency

**Problem:**
Currently, profile completion status (`isProfileComplete`) is managed entirely on the client side:
- Stored in local DataStore
- Set based on `isNewUser` flag from login response
- Can become out of sync if user logs in from multiple devices
- No server-side validation of profile completeness

**Current Flow:**
1. User logs in → Backend returns `isNewUser: true/false`
2. Android sets `isProfileComplete = !isNewUser` locally
3. If incomplete, shows ProfileSetupScreen
4. After setup, marks `isProfileComplete = true` locally
5. No server-side tracking of profile completion

**Issues:**
- User completes profile on Device A
- Logs in on Device B → Still shows as incomplete (because backend doesn't track it)
- Profile completion status is device-specific, not account-specific
- Backend has no way to enforce profile completion for certain features

**Required Changes:**

#### Backend:
1. Add `is_profile_complete` boolean field to `users` table
2. Update field when profile setup API is called
3. Return `isProfileComplete` in login/session responses
4. Add validation: certain endpoints require complete profile

```go
// In users table migration
type User struct {
    // ... existing fields ...
    IsProfileComplete bool `gorm:"default:false" json:"isProfileComplete"`
}

// In login response
type LoginResponse struct {
    // ... existing fields ...
    IsProfileComplete bool `json:"isProfileComplete"`
}

// In profile setup endpoint
func (h *UserHandler) SetupProfile(c *fiber.Ctx) error {
    // ... setup logic ...
    
    user.IsProfileComplete = true
    database.DB.Save(&user)
    
    return response.Success(c, user, "Profile setup complete", 200)
}
```

#### Android:
1. Remove local `isProfileComplete` from DataStore
2. Get status from API responses only
3. Update SessionManager to use server value
4. Remove `saveProfileComplete()` calls

```kotlin
// In OtpViewModel - use server value
SessionManager.isProfileComplete = data.isProfileComplete

// In ProfileSetupViewModel - trust server response
when (val result = setupProfileUseCase(nickname, gender, interestedIn)) {
    is ApiResult.Success -> {
        SessionManager.isProfileComplete = true // Server confirmed
        _setupState.value = ProfileSetupUiState.Success("Profile setup complete")
    }
}

// Remove from UserPreferencesRepository
// - Remove KEY_IS_PROFILE_COMPLETE
// - Remove saveProfileComplete()
// - Remove isProfileCompleteFlow
```

**Benefits:**
- Single source of truth (server)
- Consistent across all devices
- Server can enforce profile completion requirements
- Easier to debug profile-related issues

**Files to Modify:**

Backend:
- `jugnu_backend/internal/database/migrations/` (add migration)
- `jugnu_backend/internal/module/user/model.go`
- `jugnu_backend/internal/module/auth/handler.go` (login response)
- `jugnu_backend/internal/module/user/handler.go` (setup profile)

Android:
- `jugnu_android/app/src/main/java/com/example/app/core/session/SessionManager.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/preferences/user/data/UserPreferencesRepository.kt`
- `jugnu_android/app/src/main/java/com/example/app/feature/login/ui/OtpViewModel.kt`
- `jugnu_android/app/src/main/java/com/example/app/feature/login/ui/ProfileSetupViewModel.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/session/SessionInitializer.kt`

**Testing:**
- [ ] New user completes profile → `isProfileComplete = true` on server
- [ ] User logs in on different device → Shows correct profile status
- [ ] Existing users (before migration) → Default to incomplete, prompt setup
- [ ] Profile completion persists across app reinstalls
- [ ] Server validates profile completion for protected endpoints

---

### 4. Call Received Acknowledgment - Timeout Handling
**Status:** Partially Implemented  
**Priority:** Medium  
**Impact:** User Experience

**Problem:**
Caller shows "Connecting..." until callee's device acknowledges receipt. If callee never receives the call (offline, notification blocked, etc.), caller stays on "Connecting..." indefinitely.

**Current Implementation:**
- Caller: `OUTGOING_CONNECTING` → waits for `EVENT_CALL_RECEIVED` → `OUTGOING_RINGING`
- If `EVENT_CALL_RECEIVED` never arrives, caller stuck on "Connecting..."

**Required Changes:**
1. Add timeout (e.g., 10 seconds) in `CallViewModel.startCall()`
2. If no acknowledgment received within timeout, show "Ringing..." anyway
3. Or show "Unable to reach" and auto-cancel call

**Files to Modify:**
- `jugnu_android/app/src/main/java/com/example/app/feature/call/ui/CallViewModel.kt`

---

### 4. RTM Reliability - Fallback to FCM
**Status:** Not Implemented  
**Priority:** Medium  
**Impact:** Reliability

**Problem:**
RTM (peer-to-peer) messages can be lost due to network issues. Currently, if RTM fails, no fallback mechanism exists.

**Recommendation:**
For critical events (Accept, Reject, End), always use both:
1. RTM for instant delivery (when both apps are active)
2. Backend API + FCM as reliable fallback

**Current Implementation:**
- Foreground: RTM only (fast but unreliable)
- Background: FCM + API (reliable but slower)

**Better Approach:**
- Always: RTM + API call to backend
- Backend sends FCM to ensure delivery
- Redundant but guarantees reliability

---

## Low Priority

### 5. Home Screen Balance Refresh on Resume
**Status:** ✅ Implemented  
**Date:** 2024

**Problem:**
Balance was not refreshed when returning to home screen after call ended, showing stale balance.

**Solution:**
Added `viewModel.refreshProfile()` call in `DisposableEffect` when `ON_RESUME` lifecycle event fires.

**Files Modified:**
- `jugnu_android/app/src/main/java/com/example/app/feature/home/ui/HomeViewModel.kt` (made `refreshProfile()` public)
- `jugnu_android/app/src/main/java/com/example/app/feature/home/ui/HomeScreen.kt` (added refresh on resume)

---

### 6. Notification Sound Customization
**Status:** Not Implemented  
**Priority:** Low  
**Impact:** User Experience

**Current Behavior:**
- Incoming call notification has no sound (handled by MediaPlayer)
- System notification sound is disabled

**Potential Enhancement:**
- Add custom ringtone selection in settings
- Allow users to choose notification sound vs. full ringtone

---

### 7. Call History - Missed Call Tracking
**Status:** Partially Implemented  
**Priority:** Low  
**Impact:** Feature Completeness

**Current Implementation:**
- Missed call notification shown
- No persistent call history

**Potential Enhancement:**
- Store missed calls in local database
- Show missed call badge/count
- Call history screen with filters

---

## Completed ✅

### ✅ Call Received Acknowledgment (P2P)
**Status:** Implemented  
**Date:** 2024

**Implementation:**
- Added `EVENT_CALL_RECEIVED` constant
- Callee sends acknowledgment via RTM when receiving call (foreground)
- Callee sends acknowledgment via API when receiving FCM (background)
- Backend relays acknowledgment to caller via FCM
- Caller updates status from "Connecting..." to "Ringing..."

**Files Modified:**
- `jugnu_android/app/src/main/java/com/example/app/AppConstants.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/call/CallEvent.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/call/CallManager.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/observer/EventObserver.kt`
- `jugnu_android/app/src/main/java/com/example/app/core/rtm/RtmEventListenerImpl.kt`
- `jugnu_android/app/src/main/java/com/example/app/services/fcm/FcmService.kt`
- `jugnu_android/app/src/main/java/com/example/app/feature/call/data/CallApi.kt`
- `jugnu_android/app/src/main/java/com/example/app/feature/call/data/CallDto.kt`
- `jugnu_android/app/src/main/java/com/example/app/feature/call/data/CallRepository.kt`
- `jugnu_backend/internal/constants/constants.go`
- `jugnu_backend/internal/module/call/model.go`
- `jugnu_backend/internal/module/call/route.go`
- `jugnu_backend/internal/module/call/handler.go`

---

### ✅ Ringtone Stops on Call Cancellation (Background)
**Status:** Fixed  
**Date:** 2024

**Problem:**
When caller cancelled call and callee's app was in background, ringtone continued playing because notification wasn't dismissed.

**Solution:**
Uncommented dismiss logic in `FcmService` for call termination events (`EVENT_CALL_CANCELLED`, `EVENT_CALL_REJECTED`, `EVENT_CALL_ENDED`).

**Files Modified:**
- `jugnu_android/app/src/main/java/com/example/app/services/fcm/FcmService.kt`

---

## Notes

### Architecture Decisions

**RTM vs FCM:**
- RTM: Fast, peer-to-peer, requires both apps active
- FCM: Reliable, works when app is killed, but slower
- Current approach: Use RTM for foreground, FCM for background

**Call Status Flow:**
```
Caller:
OUTGOING_CONNECTING (waiting for server)
  ↓ (server responds)
OUTGOING_CONNECTING (waiting for callee acknowledgment)
  ↓ (callee acknowledges via EVENT_CALL_RECEIVED)
OUTGOING_RINGING (callee's phone is ringing)
  ↓ (callee accepts)
CONNECTING (joining RTC)
  ↓ (RTC connected)
CONNECTED (call active)

Callee:
INCOMING_RINGING (received call notification)
  ↓ (accepts)
CONNECTING (joining RTC)
  ↓ (RTC connected)
CONNECTED (call active)
```

### Testing Checklist

When implementing changes, test these scenarios:

**Call Initiation:**
- [ ] Caller app foreground, callee app foreground
- [ ] Caller app foreground, callee app background
- [ ] Caller app foreground, callee app killed
- [ ] Caller app background, callee app foreground

**Call Acceptance:**
- [ ] Callee accepts from app (foreground)
- [ ] Callee accepts from notification (background)
- [ ] Callee accepts from notification (killed)

**Call Rejection:**
- [ ] Callee rejects from app (foreground)
- [ ] Callee rejects from notification (background)
- [ ] Callee rejects from notification (killed)

**Call Cancellation:**
- [ ] Caller cancels before callee answers (all callee states)
- [ ] Verify ringtone stops in all scenarios

**Audio Management:**
- [ ] Ringtone plays when call arrives
- [ ] Ringtone stops when call accepted
- [ ] Ringtone stops when call rejected
- [ ] Ringtone stops when call cancelled
- [ ] Dialing tone plays for caller
- [ ] Dialing tone stops when connected

**Network Scenarios:**
- [ ] Poor network (RTM messages delayed)
- [ ] No network (offline)
- [ ] Network switches (WiFi to mobile data)
