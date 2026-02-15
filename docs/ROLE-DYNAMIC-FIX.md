# ✅ Dynamic Role Fix - COMPLETED

**Date:** February 8, 2026  
**Issue:** User role was hardcoded to CUSTOMER  
**Status:** Fixed - Role now comes from backend

---

## 🐛 PROBLEM IDENTIFIED

### **Hardcoded Role in Multiple Places**

1. **SessionManager.kt**
   ```kotlin
   var userRole: UserRole = UserRole.CUSTOMER  // ❌ Hardcoded
   ```

2. **OtpViewModel.kt**
   ```kotlin
   userPreferencesRepository.saveUserPrefs(
       id = data.accountId,
       role = UserRole.CUSTOMER  // ❌ Hardcoded
   )
   ```

3. **VerifyOtpResult.kt**
   ```kotlin
   data class VerifyOtpResult(
       val sessionId: String,
       val accountId: Long,
       val isNewUser: Boolean
       // ❌ Missing role field
   )
   ```

---

## ✅ SOLUTION IMPLEMENTED

### **1. Updated VerifyOtpResult Model**

**File:** `app/src/main/java/com/example/app/feature/login/domain/AuthModel.kt`

```kotlin
@Serializable
@Parcelize
data class VerifyOtpResult(
    val sessionId: String,
    val accountId: Long,
    val isNewUser: Boolean,
    val role: String  // ✅ NEW: "CUSTOMER" or "LISTENER" from backend
) : Parcelable
```

**Changes:**
- ✅ Added `role: String` field to receive role from backend
- ✅ Backend should return "CUSTOMER" or "LISTENER"

---

### **2. Updated OtpViewModel**

**File:** `app/src/main/java/com/example/app/feature/login/ui/OtpViewModel.kt`

```kotlin
fun verifyOtp(phone: String, otp: String) {
    viewModelScope.launch {
        _otpVerifyState.value = OtpUiState.Loading
        val fcmToken = userSession.fcmToken

        when (val result = verifyOtpUseCase(phone, otp, fcmToken)) {
            is ApiResult.Success -> {
                val data = result.data

                if (data != null) {
                    // ✅ Parse role from backend response
                    val userRole = when (data.role.uppercase()) {
                        "LISTENER" -> UserRole.LISTENER
                        "CUSTOMER" -> UserRole.CUSTOMER
                        else -> UserRole.CUSTOMER // Default fallback
                    }
                    
                    // ✅ Save to SessionManager
                    SessionManager.sessionId = data.sessionId
                    SessionManager.userRole = userRole
                    SessionManager.userAccountId = data.accountId
                    
                    // ✅ Persist to DataStore
                    userPreferencesRepository.saveSessionId(data.sessionId)
                    userPreferencesRepository.saveUserPrefs(
                        id = data.accountId,
                        role = userRole
                    )
                }

                _otpVerifyState.value = OtpUiState.Success(data)
            }

            is ApiResult.Error -> {
                _otpVerifyState.value =
                    OtpUiState.Error(result.message ?: "Invalid OTP")
            }
        }
    }
}
```

**Changes:**
- ✅ Parse role from backend response
- ✅ Convert string to UserRole enum
- ✅ Save to SessionManager (runtime)
- ✅ Save to UserPreferences (persisted)
- ✅ Fallback to CUSTOMER if role is invalid

---

### **3. Updated OtpScreen Navigation**

**File:** `app/src/main/java/com/example/app/feature/login/ui/OtpScreen.kt`

```kotlin
LaunchedEffect(otpVerifyState) {
    when (otpVerifyState) {
        is OtpUiState.Success -> {
            val data = (otpVerifyState as OtpUiState.Success).data as? VerifyOtpResult
            
            Toast.makeText(context, "✓ Login successful!", Toast.LENGTH_SHORT).show()
            
            // ✅ Route based on user role from backend
            val destination = if (data != null) {
                when (data.role.uppercase()) {
                    "LISTENER" -> Routes.Graph.LISTENER
                    "CUSTOMER" -> Routes.Graph.HOME
                    else -> Routes.Graph.HOME
                }
            } else {
                Routes.Graph.HOME // Fallback
            }
            
            // Navigate and clear auth stack
            navController.navigate(destination) {
                popUpTo(Routes.Graph.AUTH) { inclusive = true }
                launchSingleTop = true
            }
        }
        // ...
    }
}
```

**Changes:**
- ✅ Extract role from VerifyOtpResult
- ✅ Route to LISTENER graph if role is "LISTENER"
- ✅ Route to HOME graph if role is "CUSTOMER"
- ✅ Fallback to HOME if role is invalid

---

## 🔄 COMPLETE FLOW

### **Backend Response Expected**
```json
{
  "sessionId": "abc123...",
  "accountId": 12345,
  "isNewUser": false,
  "role": "CUSTOMER"  // or "LISTENER"
}
```

### **Flow After OTP Verification**

```
1. User enters OTP
   ↓
2. Backend verifies OTP
   ↓
3. Backend returns: sessionId, accountId, isNewUser, role
   ↓
4. OtpViewModel receives response
   ↓
5. Parse role string → UserRole enum
   ↓
6. Save to SessionManager (runtime)
   ↓
7. Save to UserPreferences (persisted)
   ↓
8. OtpScreen reads role from response
   ↓
9. Navigate based on role:
   - CUSTOMER → HOME (Listener List)
   - LISTENER → LISTENER (Dashboard)
```

---

## 📊 ROLE HANDLING

### **Role Enum**
```kotlin
enum class UserRole {
    CUSTOMER,
    LISTENER
}
```

### **Role Storage**

**Runtime (SessionManager):**
```kotlin
SessionManager.userRole = UserRole.CUSTOMER  // or LISTENER
```

**Persisted (DataStore):**
```kotlin
userPreferencesRepository.saveUserPrefs(
    id = accountId,
    role = userRole
)
```

### **Role Access**

**From SessionManager:**
```kotlin
val role = SessionManager.userRole
```

**From UserPreferences:**
```kotlin
val role = userPreferencesRepository.getUserRole()
```

---

## 🎯 NAVIGATION BY ROLE

### **Customer (CUSTOMER)**
```
HOME Graph
├── Listener List
├── Chat with Listener
├── Wallet (Add Money)
└── User Profile
```

### **Listener (LISTENER)**
```
LISTENER Graph
├── Dashboard
├── Stats & Analytics
├── Wallet (Withdraw Money)
└── Customer List (Future)
```

---

## ⚠️ BACKEND REQUIREMENTS

### **OTP Verification Endpoint**

**Request:**
```json
POST /api/auth/verify-otp
{
  "phone": "+1234567890",
  "otp": "123456",
  "fcmToken": "firebase_token_here"
}
```

**Response:**
```json
{
  "sessionId": "session_abc123",
  "accountId": 12345,
  "isNewUser": false,
  "role": "CUSTOMER"  // ✅ REQUIRED: "CUSTOMER" or "LISTENER"
}
```

**Role Values:**
- `"CUSTOMER"` - Regular user who chats with listeners
- `"LISTENER"` - Professional listener who receives calls/chats

---

## 🧪 TESTING

### **Test Case 1: Customer Login**
1. Login with customer phone number
2. Enter OTP
3. Backend returns `role: "CUSTOMER"`
4. App navigates to HOME (Listener List)
5. Verify SessionManager.userRole = CUSTOMER
6. Verify wallet shows "Add Money"

### **Test Case 2: Listener Login**
1. Login with listener phone number
2. Enter OTP
3. Backend returns `role: "LISTENER"`
4. App navigates to LISTENER (Dashboard)
5. Verify SessionManager.userRole = LISTENER
6. Verify wallet shows "Withdraw Money"

### **Test Case 3: Invalid Role**
1. Backend returns `role: "INVALID"`
2. App defaults to CUSTOMER
3. App navigates to HOME
4. Verify SessionManager.userRole = CUSTOMER

### **Test Case 4: Session Persistence**
1. Login as LISTENER
2. Close app
3. Reopen app
4. Verify still shows LISTENER dashboard
5. Verify SessionManager.userRole = LISTENER

---

## 📝 MIGRATION NOTES

### **For Existing Users**

If users are already logged in with hardcoded CUSTOMER role:

**Option 1: Force Re-login**
```kotlin
// Clear session and force login
SessionManager.sessionId = ""
userPreferencesRepository.clearSession()
// Navigate to login
```

**Option 2: Fetch Role from Backend**
```kotlin
// Add API endpoint to fetch current user role
suspend fun getCurrentUserRole(): UserRole {
    val response = api.getUserProfile()
    return response.role
}
```

---

## ✅ VERIFICATION CHECKLIST

- [x] VerifyOtpResult includes role field
- [x] OtpViewModel parses role from backend
- [x] SessionManager.userRole is set dynamically
- [x] UserPreferences saves role
- [x] OtpScreen routes based on role
- [x] No compilation errors
- [ ] Backend returns role in OTP response
- [ ] Test customer login flow
- [ ] Test listener login flow
- [ ] Test session persistence
- [ ] Test role-specific features

---

## 🚀 DEPLOYMENT

### **Before Deployment**
1. ✅ Update backend to return role in OTP verification
2. ✅ Test with both CUSTOMER and LISTENER accounts
3. ✅ Verify session persistence works
4. ✅ Test role-specific features (wallet, navigation)

### **After Deployment**
1. Monitor login success rates
2. Check for role-related errors
3. Verify navigation works for both roles
4. Monitor SessionManager state

---

## 📚 RELATED FILES

**Modified Files:**
- `app/src/main/java/com/example/app/feature/login/domain/AuthModel.kt`
- `app/src/main/java/com/example/app/feature/login/ui/OtpViewModel.kt`
- `app/src/main/java/com/example/app/feature/login/ui/OtpScreen.kt`

**Related Files:**
- `app/src/main/java/com/example/app/core/session/SessionManager.kt`
- `app/src/main/java/com/example/app/core/preferences/user/data/UserPreferencesRepository.kt`
- `app/src/main/java/com/example/app/feature/navigation/ui/AppNavGraph.kt`

---

**Status:** ✅ Fixed and Ready  
**Compilation:** ✅ No Errors  
**Backend Required:** ⚠️ Must return role field in OTP response
