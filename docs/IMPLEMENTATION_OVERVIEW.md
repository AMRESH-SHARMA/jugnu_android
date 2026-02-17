# WebSocket Presence - Complete Implementation Overview

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         MyApp                                │
│  - Registers AppForegroundTracker with ProcessLifecycleOwner│
│  - Starts NetworkStateTracker                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      EventObserver                           │
│  - Observes CallEventBus                                     │
│  - Observes PresenceEventBus                                 │
│  - Observes AppForegroundTracker.isForeground                │
│  - Observes NetworkStateTracker.isNetworkAvailable           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    PresenceManager                           │
│  - onConnected() / onDisconnected()                          │
│  - onCallStarted() / onCallEnded()                           │
│  - onAppBackground() / onAppForeground()                     │
│  - onNetworkLost() / onNetworkAvailable()                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              PresenceWebSocketManager                        │
│  - connect() / disconnect()                                  │
│  - sendCallStart() / sendCallEnd()                           │
│  - sendNetOffline() / sendNetOnline()                        │
│  - onAppBackground() / onAppForeground()                     │
│  - onNetworkLost() / onNetworkAvailable()                    │
│  - Handles reconnection with exponential backoff             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    OkHttp WebSocket                          │
│  - Automatic ping/pong every 30s                             │
│  - Headers: Authorization, X-Session-Id, X-Role              │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

### Local Presence (Your Status)
```
PresenceStore (StateFlow<PresenceState>)
    ↓
    OFFLINE / ONLINE / BUSY
    ↓
    Observed by UI components
```

### Remote Presence (Others' Status)
```
Server Broadcast
    ↓
PresenceWebSocketManager.onMessage()
    ↓
RemotePresenceStore.update(accountId, status)
    ↓
PresenceEventBus.emit(StatusChanged)
    ↓
Observed by UI components
```

## Event Flow Examples

### 1. User Opens App
```
1. MyApp.onCreate()
2. ProcessLifecycleOwner registers AppForegroundTracker
3. NetworkStateTracker.startTracking()
4. EventObserver.init() starts observing
5. User logs in → PresenceWebSocketManager.connect()
6. WebSocket opens → PresenceManager.onConnected()
7. PresenceStore.setState(ONLINE)
```

### 2. User Minimizes App
```
1. AppForegroundTracker detects onStop()
2. AppForegroundTracker.isForeground = false
3. EventObserver receives foreground change
4. PresenceManager.onAppBackground()
5. PresenceWebSocketManager.disconnect()
6. PresenceStore.setState(OFFLINE)
```

### 3. User Returns to App
```
1. AppForegroundTracker detects onStart()
2. AppForegroundTracker.isForeground = true
3. EventObserver receives foreground change
4. PresenceManager.onAppForeground()
5. PresenceWebSocketManager.connect()
6. WebSocket opens → PresenceStore.setState(ONLINE)
```

### 4. Network Disconnects
```
1. NetworkStateTracker detects network loss
2. NetworkStateTracker.isNetworkAvailable = false
3. EventObserver receives network change
4. PresenceManager.onNetworkLost()
5. PresenceWebSocketManager.sendNetOffline()
6. WebSocket fails → scheduleReconnect()
```

### 5. Network Reconnects
```
1. NetworkStateTracker detects network available
2. NetworkStateTracker.isNetworkAvailable = true
3. EventObserver receives network change
4. PresenceManager.onNetworkAvailable()
5. PresenceWebSocketManager.connect()
6. WebSocket opens → PresenceStore.setState(ONLINE)
```

### 6. User Starts Call
```
1. CallEventBus.emit(CallEvent.Outgoing)
2. EventObserver receives call event
3. PresenceManager.onCallStarted()
4. PresenceStore.setState(BUSY)
5. PresenceWebSocketManager.sendCallStart()
6. Server broadcasts BUSY to other users
```

### 7. User Ends Call
```
1. CallEventBus.emit(CallEvent.Ended)
2. EventObserver receives call event
3. PresenceManager.onCallEnded()
4. PresenceWebSocketManager.sendCallEnd()
5. PresenceStore.setState(ONLINE)
6. Server broadcasts ONLINE to other users
```

### 8. Duplicate Connection Detected
```
1. User logs in on Device B
2. Server receives new connection with same session ID
3. Server sends {"type":"connection_replaced"} to Device A
4. Device A receives message
5. PresenceWebSocketManager.disconnect() (no reconnect)
6. Device B remains connected
```

## File Structure

```
app/src/main/java/com/example/app/core/
│
├── websocket/
│   ├── PresenceWebSocketManager.kt      ← WebSocket connection management
│   ├── PresenceManager.kt               ← Business logic coordinator
│   ├── PresenceStore.kt                 ← Local user presence state
│   ├── RemotePresenceStore.kt           ← Remote users presence state
│   ├── PresenceEventBus.kt              ← Event communication
│   ├── PresenceEvent.kt                 ← Event types
│   ├── PresenceState.kt                 ← State enum (ONLINE/OFFLINE/BUSY)
│   ├── PresenceBroadcastMessage.kt      ← Message models
│   └── ...
│
├── observer/
│   ├── EventObserver.kt                 ← Central event router
│   ├── AppForegroundTracker.kt          ← Lifecycle tracking
│   ├── NetworkStateTracker.kt           ← Network monitoring (NEW)
│   └── ScreenStateTracker.kt
│
└── di/
    └── WebSocketModule.kt               ← OkHttp configuration
```

## Key Components

### PresenceWebSocketManager
- Manages WebSocket connection lifecycle
- Handles reconnection with exponential backoff
- Sends/receives presence messages
- Responds to lifecycle and network events

### PresenceManager
- Coordinates between events and WebSocket
- Manages state transitions
- Enforces business rules (e.g., BUSY during calls)

### EventObserver
- Central hub for all event routing
- Observes multiple event sources
- Routes events to appropriate managers

### NetworkStateTracker
- Monitors Android ConnectivityManager
- Detects network availability changes
- Provides StateFlow for reactive updates

### AppForegroundTracker
- Implements DefaultLifecycleObserver
- Tracks app foreground/background state
- Registered with ProcessLifecycleOwner

## Configuration

### WebSocket Headers
```kotlin
Authorization: Bearer {accountId}
X-Session-Id: {sessionId}
X-Role: {CUSTOMER|LISTENER}
```

### Ping/Pong
```kotlin
pingInterval = 30 seconds (automatic via OkHttp)
```

### Reconnection
```kotlin
Initial delay: 1 second
Max delay: 30 seconds
Strategy: Exponential backoff (1s → 2s → 4s → 8s → 16s → 30s)
```

### Timeouts
```kotlin
connectTimeout: 10 seconds
readTimeout: 0 (no timeout for WebSocket)
writeTimeout: 10 seconds
```

## Server Message Types

### Incoming (Server → Client)
```json
// Presence snapshot (on connect)
{"type": "presence_snapshot", "data": {"123": "ONLINE", "456": "BUSY"}}

// Single presence update
{"account_id": "123", "status": "ONLINE"}

// Connection replaced
{"type": "connection_replaced", "message": "New session detected"}
```

### Outgoing (Client → Server)
```
CALL_START    - User started a call
CALL_END      - User ended a call
NET_OFFLINE   - Network lost
NET_ONLINE    - Network restored
```

## State Machine

```
┌─────────┐
│ OFFLINE │ ◄─────────────────────────┐
└────┬────┘                           │
     │ connect()                      │ disconnect()
     │                                │ network lost
     ▼                                │
┌─────────┐                           │
│ ONLINE  │ ──────────────────────────┤
└────┬────┘                           │
     │ onCallStarted()                │
     │                                │
     ▼                                │
┌─────────┐                           │
│  BUSY   │ ──────────────────────────┘
└─────────┘  onCallEnded()
```

## Testing

### Manual Testing
1. Open app → Check connection logs
2. Minimize app → Check disconnect logs
3. Restore app → Check reconnect logs
4. Turn off WiFi → Check network lost logs
5. Turn on WiFi → Check network available logs
6. Start call → Check BUSY state
7. End call → Check ONLINE state

### Log Commands
```bash
# Real-time monitoring
adb logcat -s RTM

# Save to file
adb logcat -s RTM > debug.log

# Filter specific events
adb logcat -s RTM | grep "connecting"
adb logcat -s RTM | grep "setState"
adb logcat -s RTM | grep "Network"
```

## Performance Considerations

- WebSocket disconnects in background to save battery
- Exponential backoff prevents server overload
- Network state tracking prevents unnecessary reconnection attempts
- Ping/pong keeps connection alive efficiently
- State changes are logged for debugging

## Security

- Bearer token authentication
- Session ID for duplicate detection
- Role-based access control via X-Role header
- WSS (WebSocket Secure) for encrypted communication
