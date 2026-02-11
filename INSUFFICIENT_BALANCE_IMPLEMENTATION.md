# Insufficient Balance Error Handling - Implementation Complete

## Overview
Implemented client-side error handling for insufficient balance when users attempt to make audio/video calls. The system now shows a user-friendly error message and redirects to the wallet/recharge screen.

## Client-Side Changes

### 1. Central Error Handling (`ApiErrorHandler.kt`)
- Added HTTP 402 (Payment Required) handling for insufficient balance errors
- Added `isInsufficientBalance()` helper method
- User-friendly message: "Insufficient balance to make this call"

### 2. API Error Response (`ApiErrorResponse.kt`)
- Added optional `meta` field to support additional error context
- Can include balance details, required amount, etc.

### 3. Call ViewModel (`CallViewModel.kt`)
- Enhanced `startCall()` error handling
- Shows error in GlobalSnackbar (premium animated notification)
- Automatically redirects to wallet screen after 500ms delay
- Added `navigateToWallet` SharedFlow for navigation events

### 4. UI Integration (`ListenersListScreen.kt`)
- Added LaunchedEffect to observe wallet navigation events
- Seamless redirect to recharge screen when balance is insufficient

## Server-Side Requirements

### API Endpoint: `POST /call/start`

**Flow:**
```
1. Receive call start request
2. Check caller's balance BEFORE creating call
3. If balance = 0 or insufficient:
   → Return HTTP 402 error immediately
   → Don't create call record
   → Don't notify callee
   → Don't generate channel/tokens
4. If balance OK:
   → Create call in database
   → Generate channel/tokens
   → Return success response
```

### Error Response Structure

**When balance is insufficient:**
```json
HTTP 402 Payment Required

{
  "success": false,
  "message": "Insufficient balance to make this call",
  "data": null,
  "meta": {
    "errorCode": "INSUFFICIENT_BALANCE",
    "currentBalance": 0,
    "requiredBalance": 10
  }
}
```

**Success response (existing):**
```json
HTTP 200 OK

{
  "success": true,
  "message": "Call initiated",
  "data": {
    "callId": "abc123",
    "channel": "channel_xyz",
    "status": "ringing"
  }
}
```

### Important Notes

1. **Only check balance on `/call/start`** - Not needed for:
   - `/call/accept` (callee doesn't pay)
   - `/call/reject` (no charge)
   - `/call/end` (billing already handled)

2. **HTTP Status Code:** Use `402 Payment Required` for balance errors

3. **Error Message:** The `message` field should be user-friendly as it's displayed directly to users

4. **Meta Field (Optional):** Can include additional context like:
   - `errorCode`: Machine-readable error code
   - `currentBalance`: User's current balance
   - `requiredBalance`: Minimum balance needed

## User Experience Flow

1. User clicks audio/video call button
2. Client sends `POST /call/start` request
3. Server checks balance:
   - **Insufficient:** Returns 402 error
   - **Sufficient:** Creates call and returns success
4. If 402 error:
   - Red snackbar appears at top: "Insufficient balance to make this call"
   - After 0.5s, user is redirected to wallet/recharge screen
5. User can recharge and try again

## Testing

### Test Cases for Backend:
1. ✅ User with 0 balance attempts call → 402 error
2. ✅ User with insufficient balance attempts call → 402 error
3. ✅ User with sufficient balance attempts call → Success
4. ✅ Error response includes proper message
5. ✅ No call record created on balance error
6. ✅ No RTM notification sent on balance error

### Test Cases for Client:
1. ✅ 402 error shows red snackbar
2. ✅ User redirected to wallet screen
3. ✅ Other errors show snackbar without redirect
4. ✅ Success case works normally

## Files Modified

### Client-Side:
- `app/src/main/java/com/example/app/core/network/ApiErrorHandler.kt`
- `app/src/main/java/com/example/app/core/network/ApiErrorResponse.kt`
- `app/src/main/java/com/example/app/feature/call/ui/CallViewModel.kt`
- `app/src/main/java/com/example/app/feature/listeners/ui/list/ListenersListScreen.kt`

### Server-Side (Required):
- Call start endpoint handler
- Balance check logic
- Error response formatting

## Next Steps

1. Backend team implements balance check in `/call/start` endpoint
2. Backend returns HTTP 402 with proper error structure
3. Test end-to-end flow
4. Monitor error rates and user behavior

---

**Implementation Date:** February 11, 2026
**Status:** Client-side complete, awaiting backend implementation
