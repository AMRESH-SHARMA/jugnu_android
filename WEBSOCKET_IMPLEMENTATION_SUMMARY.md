# WebSocket Presence Implementation Summary

## ✅ Implemented Features

### 1. Session ID in WebSocket Header
**File**: `PresenceWebSocketManager.kt`

- Added `X-Session-Id` header to WebSocket connection
- Now sends: `Authorization: Bearer {accountId}`, `X-Session-Id: {sessionId}`, `X-Role: {role}`
- Server can now identify and handle duplicate connections from same user

```kotlin
.addHeader("Authorization", "Bearer $accountId")
.addHeader("X-Session-Id", sessionId)
.addHeader("X-Role", role)
```

### 2. App Lifecycle Integration (Background/Foreground)
**Files**: `PresenceWebSocketManager.kt`, `PresenceManager.kt`, `EventObserver.kt`

- Disconnects WebSocket when app goes to background (`onAppBackground()`)
- Reconnects WebSocket when app comes to foreground (`onAppForeground()`)
- Integrated with existing `AppForegroundTracker` via `EventObserver`
- Prevents battery drain from maintaining connection in background

**Flow**:
```
App Background → EventObserver detects → PresenceManager.onAppBackground() → WebSocket disconnects
App Foreground → EventObserver detects → PresenceManager.onAppForeground() → WebSocket reconnects
```

### 3. Network Change Detection
**New File**: `NetworkStateTracker.kt`

- Monitors network availability using Android ConnectivityManager
- Detects WiFi ↔ Mobile data switches
- Tracks network capabilities (internet + validated)
- Automatically reconnects WebSocket when network becomes available
- Sends offline status when network is lost

**Features**:
- Real-time network state tracking via StateFlow
- Automatic callback registration/unregistration
- Handles network capability changes
- Integrated with `EventObserver` for automatic presence updates

**Flow**:
```
Network Lost → NetworkStateTracker detects → PresenceManager.onNetworkLost() → Send NET_OFFLINE
Network Available → NetworkStateTracker detects → PresenceManager.onNetworkAvailable() → Reconnect
```

### 4. Duplicate Connection Handling
**File**: `PresenceWebSocketManager.kt`

- Detects `connection_replaced` message from server
- Gracefully closes old connection when duplicate detected
- Server can now send: `{"type": "connection_replaced", "message": "..."}`
- Client logs and disconnects without reconnecting

```kotlin
if (obj.optString("type") == "connection_replaced") {
    Log.w(TAG, "WS connection replaced by another session - closing old connection")
    disconnect()
    return
}
```

### 5. Centralized Logging with "RTM" Tag
**All Files Updated**

All WebSocket-related logs now use consistent `Log.d("RTM", "...")` format:

**PresenceWebSocketManager**:
- Connection attempts with session details
- Message send/receive events
- Connection state changes
- Error conditions

**PresenceManager**:
- State transitions
- Lifecycle events
- Network events

**PresenceStore & RemotePresenceStore**:
- State updates
- Remote presence changes

**EventObserver**:
- Event routing
- Lifecycle changes
- Network changes

**NetworkStateTracker**:
- Network state changes
- Capability changes

## 🔍 Debug Log Examples

```
D/RTM: WS connecting... sessionId=abc123, accountId=456, role=CUSTOMER
D/RTM: WS onOpen() - connection established, response code=101
D/RTM: PresenceManager: onConnected() - current state=OFFLINE
D/RTM: PresenceStore: setState() - OFFLINE -> ONLINE
D/RTM: WS onMessage() received: {"account_id":"789","status":"BUSY"}
D/RTM: WS presence update: accountId=789, status=BUSY
D/RTM: RemotePresenceStore: update() - accountId=789, status=BUSY
D/RTM: EventObserver: App foreground state changed - isForeground=false
D/RTM: PresenceManager: onAppBackground()
D/RTM: WS disconnect() called
D/RTM: NetworkStateTracker: Network lost - Network 123
D/RTM: EventObserver: Network state changed - isAvailable=false
D/RTM: PresenceManager: onNetworkLost()
D/RTM: WS sendNetOffline() sent=true
```

## 📋 Server-Side Requirements

For full functionality, your server needs to:

1. **Accept Session ID Header**: Read `X-Session-Id` from WebSocket upgrade request
2. **Handle Duplicate Connections**: 
   - Track connections by session ID
   - When new connection with same session ID arrives, send `{"type": "connection_replaced"}` to old connection
   - Close old connection after sending message
3. **Handle Status Messages**:
   - `CALL_START` - User started a call
   - `CALL_END` - User ended a call
   - `NET_OFFLINE` - User lost network
   - `NET_ONLINE` - User regained network

## 🎯 What This Solves

✅ **Session ID**: Server can identify which connection belongs to which session
✅ **Background/Foreground**: Saves battery, prevents zombie connections
✅ **Network Changes**: Handles WiFi/mobile switches gracefully
✅ **Duplicate Connections**: Prevents multiple connections from same user
✅ **Debugging**: Easy to filter logs with `adb logcat -s RTM`

## 🚀 Testing Commands

```bash
# Filter only WebSocket logs
adb logcat -s RTM

# Clear logs and watch in real-time
adb logcat -c && adb logcat -s RTM

# Save logs to file
adb logcat -s RTM > websocket_logs.txt
```

## 📝 Next Steps (Not Implemented Yet)

- Token expiry handling during long sessions
- Message acknowledgment (ack/nack)
- Rate limiting for rapid status changes
- Fallback to polling if WebSocket repeatedly fails
- Connection metrics tracking
- Remote presence cleanup (memory management)
