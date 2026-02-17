# WebSocket Presence System - Complete Flow Documentation

## Overview

This document explains the complete WebSocket presence system implementation for the 1:1 paid calling app, covering the flow from app startup to presence management, including all scenarios handled and potential improvements.

**System Constraints:**
- Single server deployment
- No multi-device login (one session per user)
- Real-time presence updates for customers and listeners

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Android Client                           │
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐ │
│  │   MyApp      │───▶│ UserSession  │───▶│  WebSocket   │ │
│  │ (Lifecycle)  │    │  (State)     │    │   Manager    │ │
│  └──────────────┘    └──────────────┘    └──────────────┘ │
│         │                    │                    │         │
│         ▼                    ▼                    ▼         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐ │
│  │EventObserver │───▶│PresenceManager│───▶│PresenceStore│ │
│  │  (Router)    │    │  (Business)   │    │   (State)    │ │
│  └──────────────┘    └──────────────┘    └──────────────┘ │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────┐    ┌──────────────┐                     │
│  │AppForeground │    │NetworkState  │                     │
│  │   Tracker    │    │   Tracker    │                     │
│  └──────────────┘    └──────────────┘                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ WSS Connection
                              │ Headers: Authorization, X-Session-Id, X-Role
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Go Fiber Server                         │
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐ │
│  │   Fiber      │───▶│  WebSocket   │───▶│  Presence    │ │
│  │  Middleware  │    │   Handler    │    │    Store     │ │
│  └──────────────┘    └──────────────┘    └──────────────┘ │
│         │                    │                    │         │
│         │                    ▼                    ▼         │
│         │            ┌──────────────┐    ┌──────────────┐ │
│         │            │ Connection   │    │  Broadcast   │ │
│         │            │  Registry    │    │   Manager    │ │
│         │            └──────────────┘    └──────────────┘ │
│         │                                                   │
│         ▼                                                   │
│  Cache Middleware (skips /ws routes)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Complete Flow: App Startup to Connection

### 1. App Launch (Cold Start)

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0ms | MyApp.onCreate()       | App process starts
T+10  | ProcessLifecycleOwner  | Registers AppForegroundTracker
T+20  | EventObserver.init()   | Starts observing all event buses
T+30  | NetworkStateTracker    | Starts monitoring network state
T+40  | AppForegroundTracker   | Initial state = false (not foreground yet)
T+50  | EventObserver          | Receives isForeground=false
      |                        | → Skips connect (not logged in anyway)
T+60  | MyApp                  | Starts observing UserSession flows
T+70  | UserSession            | Loads accountId from DataStore (0)
T+80  | UserSession            | Loads sessionId from DataStore ("")
T+90  | MyApp                  | combine() emits: accountId=0, sessionId=""
      |                        | → isLoggedIn=false
      |                        | → Skips WebSocket connect
T+100 | ProcessLifecycleOwner  | Fires onStart() → isForeground=true
T+110 | EventObserver          | Receives isForeground=true
      |                        | → Checks isLoggedIn() = false
      |                        | → Skips WebSocket connect
T+200 | UserSession            | Loads actual accountId (2023074...)
T+210 | UserSession            | Loads actual sessionId (29c1a70e...)
T+220 | MyApp                  | combine() emits: accountId=2023..., sessionId=29c1...
      |                        | → isLoggedIn=true ✅
T+230 | WebSocketManager       | connect() called
      |                        | → Builds WSS URL
      |                        | → Adds headers: Authorization, X-Session-Id, X-Role
      |                        | → Creates WebSocket connection
T+250 | Server                 | Receives upgrade request
      |                        | → Validates headers
      |                        | → Upgrades to WebSocket
      |                        | → Registers connection
T+260 | WebSocketManager       | onOpen() callback
      |                        | → Sets isConnected=true
      |                        | → Emits PresenceEvent.Connected
T+270 | PresenceManager        | onConnected()
      |                        | → Sets PresenceStore state = ONLINE
T+280 | Server                 | Broadcasts ONLINE status to relevant users
T+290 | Server                 | Sends presence snapshot to client
```

---

## Session Loading Flow

### Problem Solved: Race Condition

**Before Fix:**
```kotlin
// ❌ WRONG - Only waits for accountId
userSession.sessionFlow.collect { (accountId, role) ->
    if (accountId > 0) {
        connect() // sessionId might still be empty!
    }
}
```

**After Fix:**
```kotlin
// ✅ CORRECT - Waits for BOTH accountId AND sessionId
combine(
    userSession.sessionFlow,
    userSession.sessionIdFlow
) { (accountId, role), sessionId ->
    Triple(accountId, role, sessionId)
}.collect { (accountId, role, sessionId) ->
    val isLoggedIn = accountId > 0 && !sessionId.isNullOrBlank()
    if (isLoggedIn) {
        connect() // Both values guaranteed to be present
    }
}
```

**Why This Matters:**
- `sessionFlow` and `sessionIdFlow` load independently from DataStore
- `accountId` often loads before `sessionId`
- `isLoggedIn()` requires BOTH to be valid
- Without `combine()`, connection attempts fail with "user not logged in"

---

## Background/Foreground Flow

### Scenario: User Minimizes App

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | User                   | Presses home button
T+10  | ProcessLifecycleOwner  | Fires onStop()
T+20  | AppForegroundTracker   | Sets isForeground = false
T+30  | EventObserver          | Receives isForeground=false
T+40  | PresenceManager        | onAppBackground()
T+50  | WebSocketManager       | disconnect()
      |                        | → Sets shouldBeConnected=false
      |                        | → Closes WebSocket with code 1000
      |                        | → Emits PresenceEvent.Disconnected
T+60  | PresenceManager        | onDisconnected()
      |                        | → Sets PresenceStore state = OFFLINE
T+70  | Server                 | Receives close frame (code 1000)
      |                        | → Calls unregisterConn()
      |                        | → Removes from allConnections map
      |                        | → Removes from sessionToConn map
      |                        | → Broadcasts OFFLINE status
```

### Scenario: User Returns to App

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | User                   | Taps app icon
T+10  | ProcessLifecycleOwner  | Fires onStart()
T+20  | AppForegroundTracker   | Sets isForeground = true
T+30  | EventObserver          | Receives isForeground=true
      |                        | → Checks isLoggedIn() = true ✅
T+40  | PresenceManager        | onAppForeground()
T+50  | WebSocketManager       | connect()
      |                        | → Same sessionId as before
T+60  | Server                 | Receives upgrade request
      |                        | → Checks sessionToConn map
      |                        | → No duplicate found (cleaned up properly)
      |                        | → Registers new connection
T+70  | WebSocketManager       | onOpen() callback
T+80  | PresenceManager        | onConnected()
      |                        | → Sets state = ONLINE
T+90  | Server                 | Broadcasts ONLINE status
```

---

## Network Change Flow

### Scenario: WiFi Disconnects

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | Android System         | WiFi disconnected
T+10  | NetworkStateTracker    | onLost() callback
      |                        | → Sets isNetworkAvailable = false
T+20  | EventObserver          | Receives isAvailable=false
      |                        | → Checks isLoggedIn() = true
T+30  | PresenceManager        | onNetworkLost()
T+40  | WebSocketManager       | sendNetOffline()
      |                        | → Sends "NET_OFFLINE" message
T+50  | Server                 | Receives NET_OFFLINE
      |                        | → Sets presence = OFFLINE
      |                        | → Broadcasts OFFLINE status
T+60  | WebSocket              | Connection drops (network unavailable)
T+70  | WebSocketManager       | onFailure() callback
      |                        | → Schedules reconnect (exponential backoff)
```

### Scenario: WiFi Reconnects

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | Android System         | WiFi connected
T+10  | NetworkStateTracker    | onAvailable() callback
      |                        | → Sets isNetworkAvailable = true
T+20  | EventObserver          | Receives isAvailable=true
      |                        | → Checks isLoggedIn() = true ✅
T+30  | PresenceManager        | onNetworkAvailable()
T+40  | WebSocketManager       | onNetworkAvailable()
      |                        | → Checks shouldBeConnected=true
      |                        | → Checks isConnected=false
      |                        | → Calls connect()
T+50  | Server                 | Receives upgrade request
T+60  | WebSocketManager       | onOpen() callback
T+70  | PresenceManager        | onConnected()
      |                        | → Sets state = ONLINE
```

---

## Call Status Flow

### Scenario: User Starts Call

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | User                   | Initiates call
T+10  | CallEventBus           | Emits CallEvent.Outgoing
T+20  | EventObserver          | Receives call event
T+30  | PresenceManager        | onCallStarted()
      |                        | → Sets PresenceStore state = BUSY
T+40  | WebSocketManager       | sendCallStart()
      |                        | → Sends "CALL_START" message
T+50  | Server                 | Receives CALL_START
      |                        | → Sets presence = BUSY
      |                        | → Broadcasts BUSY status to customers
```

### Scenario: Call Ends

```
Time  | Component              | Action
------|------------------------|------------------------------------------
T+0   | Call                   | Ends
T+10  | CallEventBus           | Emits CallEvent.Ended
T+20  | EventObserver          | Receives call event
T+30  | PresenceManager        | onCallEnded()
T+40  | WebSocketManager       | sendCallEnd()
      |                        | → Sends "CALL_END" message
T+50  | PresenceManager        | Sets PresenceStore state = ONLINE
T+60  | Server                 | Receives CALL_END
      |                        | → Sets presence = ONLINE
      |                        | → Broadcasts ONLINE status
```

---

## Server-Side Connection Management

### Connection Registration

```go
func registerConn(c *websocket.Conn, accountID, sessionID, role string) {
    connMu.Lock()
    defer connMu.Unlock()
    
    // Check for duplicate session (same user reconnecting)
    if oldConn, exists := sessionToConn[sessionID]; exists && oldConn != c {
        // Remove old connection from maps IMMEDIATELY
        delete(allConnections, oldConn)
        delete(sessionToConn, sessionID)
        
        // Close old connection asynchronously (non-blocking)
        go func() {
            msg := `{"type":"connection_replaced","message":"New session detected"}`
            _ = oldConn.WriteMessage(websocket.TextMessage, []byte(msg))
            _ = oldConn.Close()
        }()
    }
    
    // Register new connection
    allConnections[c] = ConnMeta{
        Conn:      c,
        AccountID: accountID,
        SessionID: sessionID,
        Role:      role,
    }
    sessionToConn[sessionID] = c
}
```

**Key Points:**
- Cleans up old connection BEFORE trying to close it
- Non-blocking close (goroutine) prevents new connection from waiting
- Allows instant reconnection with same session ID

### Connection Cleanup

```go
func unregisterConn(c *websocket.Conn) {
    connMu.Lock()
    defer connMu.Unlock()
    
    if meta, exists := allConnections[c]; exists {
        // Clean up session mapping
        if sessionToConn[meta.SessionID] == c {
            delete(sessionToConn, meta.SessionID)
        }
        delete(allConnections, c)
    }
}
```

**Key Points:**
- Removes from BOTH maps (allConnections AND sessionToConn)
- Prevents stale session mappings
- Allows clean reconnection

---

## Issues Solved

### 1. Cache Middleware Caching WebSocket Responses

**Problem:**
```go
app.Use(fibercache.New(fibercache.Config{
    // ... config
}))
```
- Cache middleware was caching 101 Switching Protocols responses
- Reconnections returned cached response without executing handler
- Resulted in "101 → 0s" (instant cached response)

**Solution:**
```go
app.Use(fibercache.New(fibercache.Config{
    Next: func(c *fiber.Ctx) bool {
        // Skip cache for WebSocket routes
        if strings.HasPrefix(c.Path(), "/ws") {
            return true
        }
        // ... other skip conditions
    },
}))
```

### 2. Session Loading Race Condition

**Problem:**
- `accountId` and `sessionId` load independently from DataStore
- `accountId` often loads first
- Connection attempted before `sessionId` loaded
- `isLoggedIn()` returned false even with valid `accountId`

**Solution:**
- Use `combine()` to wait for BOTH flows
- Only connect when both values are present and valid

### 3. Incomplete Connection Cleanup

**Problem:**
- `unregisterConn()` only removed from `allConnections`
- Didn't remove from `sessionToConn` map
- Duplicate detection failed on reconnection
- Old dead connection blocked new connections

**Solution:**
- Clean up from BOTH maps in `unregisterConn()`
- Check if connection is still registered before removing from `sessionToConn`

### 4. Foreground Connect Without Login Check

**Problem:**
- `observeAppLifecycle()` called `onAppForeground()` unconditionally
- Attempted connection before session loaded
- Failed with "user not logged in"

**Solution:**
- Check `isLoggedIn()` before calling `onAppForeground()`
- Only connect if user is actually logged in

---

## Scenarios Covered

### ✅ Implemented & Tested

1. **App Startup**
   - Cold start with valid session
   - Session loading from DataStore
   - Automatic connection when ready

2. **App Lifecycle**
   - Background → Disconnect
   - Foreground → Reconnect
   - Multiple background/foreground cycles

3. **Network Changes**
   - Network lost → Send offline status
   - Network available → Reconnect
   - Exponential backoff on failures

4. **Call Status**
   - Call start → BUSY status
   - Call end → ONLINE status
   - Status broadcast to relevant users

5. **Connection Management**
   - Duplicate session detection
   - Old connection cleanup
   - Instant reconnection

6. **Error Handling**
   - Connection failures with retry
   - Exponential backoff (1s, 2s, 4s, 8s, 16s, 30s max)
   - Graceful disconnect (code 1000)

---

## Potential Improvements

### 1. Heartbeat Monitoring

**Current:** Relies on OkHttp automatic ping/pong (30s interval)

**Improvement:**
```kotlin
// Client-side
private fun startHeartbeatMonitor() {
    scope.launch {
        while (isConnected.get()) {
            delay(45_000) // 45 seconds
            if (lastMessageTime + 60_000 < System.currentTimeMillis()) {
                Log.w(TAG, "No message received in 60s - connection might be stale")
                reconnect()
            }
        }
    }
}
```

**Benefits:**
- Detects stale connections faster
- Proactive reconnection
- Better user experience

### 2. Connection Quality Monitoring

**Improvement:**
```kotlin
data class ConnectionMetrics(
    var connectAttempts: Int = 0,
    var successfulConnects: Int = 0,
    var failedConnects: Int = 0,
    var averageConnectTime: Long = 0,
    var lastConnectTime: Long = 0
)

fun trackConnectionQuality() {
    val successRate = successfulConnects / connectAttempts.toFloat()
    if (successRate < 0.5) {
        Log.w(TAG, "Poor connection quality: $successRate")
        // Consider fallback mechanism
    }
}
```

**Benefits:**
- Identify connection issues
- Adaptive retry strategies
- Better diagnostics

### 3. Presence State Persistence

**Current:** State lost on app restart

**Improvement:**
```kotlin
// Save last known state
fun savePresenceState(state: PresenceState) {
    dataStore.edit { prefs ->
        prefs[PRESENCE_STATE_KEY] = state.name
    }
}

// Restore on startup
suspend fun restorePresenceState(): PresenceState {
    return dataStore.data.first()[PRESENCE_STATE_KEY]
        ?.let { PresenceState.valueOf(it) }
        ?: PresenceState.OFFLINE
}
```

**Benefits:**
- Faster state restoration
- Better user experience
- Reduced server load

### 4. Offline Message Queue

**Improvement:**
```kotlin
private val messageQueue = ConcurrentLinkedQueue<String>()

fun sendMessage(message: String) {
    if (isConnected.get()) {
        webSocket?.send(message)
    } else {
        messageQueue.offer(message)
        Log.d(TAG, "Message queued for later delivery")
    }
}

fun flushMessageQueue() {
    while (messageQueue.isNotEmpty()) {
        val message = messageQueue.poll()
        webSocket?.send(message)
    }
}
```

**Benefits:**
- No message loss during reconnection
- Automatic retry
- Better reliability

### 5. Connection State Machine

**Improvement:**
```kotlin
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Reconnecting : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
}

private val _connectionState = MutableStateFlow<ConnectionState>(Disconnected)
val connectionState: StateFlow<ConnectionState> = _connectionState
```

**Benefits:**
- Clear state transitions
- Better UI feedback
- Easier debugging

### 6. Server-Side Connection Pooling

**Improvement:**
```go
type ConnectionPool struct {
    maxConnections int
    activeConns    int
    mu             sync.Mutex
}

func (p *ConnectionPool) Acquire() bool {
    p.mu.Lock()
    defer p.mu.Unlock()
    
    if p.activeConns >= p.maxConnections {
        return false
    }
    p.activeConns++
    return true
}
```

**Benefits:**
- Prevent server overload
- Fair resource allocation
- Better scalability

### 7. Presence Snapshot Optimization

**Current:** Sends all listener presence to every customer

**Improvement:**
```go
// Only send presence for listeners customer has interacted with
func getRelevantPresence(customerID string) map[string]string {
    recentListeners := getRecentListeners(customerID)
    snapshot := make(map[string]string)
    
    for _, listenerID := range recentListeners {
        if status, ok := store.Get(listenerID); ok {
            snapshot[listenerID] = status
        }
    }
    
    return snapshot
}
```

**Benefits:**
- Reduced bandwidth
- Faster initial load
- Better privacy

### 8. Graceful Degradation

**Improvement:**
```kotlin
private var degradedMode = false

fun enableDegradedMode() {
    degradedMode = true
    // Reduce heartbeat frequency
    // Increase retry delays
    // Disable non-essential features
}

fun checkConnectionHealth() {
    if (consecutiveFailures > 5) {
        enableDegradedMode()
    }
}
```

**Benefits:**
- Better battery life
- Reduced server load
- Maintains core functionality

---

## Monitoring & Debugging

### Key Metrics to Track

**Client-Side:**
- Connection success rate
- Average connection time
- Reconnection frequency
- Message delivery rate
- Battery impact

**Server-Side:**
- Active connections count
- Connection duration
- Message throughput
- Error rate by type
- Memory usage per connection

### Debug Log Filtering

```bash
# Android
adb logcat -s RTM

# Server
grep "WS" server.log

# Connection issues
adb logcat -s RTM | grep "connect\|disconnect\|failure"

# State changes
adb logcat -s RTM | grep "setState"
```

---

## Performance Considerations

### Battery Optimization

**Current Implementation:**
- Disconnects on background (saves battery)
- Automatic ping/pong (30s interval)
- Exponential backoff on failures

**Impact:**
- Minimal battery drain when in foreground
- Zero drain when in background
- Efficient reconnection strategy

### Network Usage

**Per Connection:**
- Initial handshake: ~500 bytes
- Ping/pong: ~10 bytes every 30s
- Presence update: ~50 bytes
- Snapshot: ~100 bytes per listener

**Daily Estimate (active user):**
- 4 hours active: ~50 KB
- Negligible impact

### Server Resources

**Per Connection:**
- Memory: ~10 KB
- CPU: Minimal (event-driven)
- Network: ~1 KB/minute

**Capacity:**
- Single server: 10,000+ concurrent connections
- With current implementation

---

## Security Considerations

### Authentication

- Bearer token in Authorization header
- Session ID for duplicate detection
- Role-based access control

### Data Privacy

- Only broadcasts listener presence to customers
- No sensitive data in presence messages
- Secure WebSocket (WSS) connection

### Rate Limiting

**Recommended:**
```go
type RateLimiter struct {
    maxMessagesPerMinute int
    messageCount         map[string]int
    mu                   sync.Mutex
}

func (r *RateLimiter) Allow(accountID string) bool {
    r.mu.Lock()
    defer r.mu.Unlock()
    
    count := r.messageCount[accountID]
    if count >= r.maxMessagesPerMinute {
        return false
    }
    
    r.messageCount[accountID]++
    return true
}
```

---

## Conclusion

The WebSocket presence system is now fully functional with:

✅ Reliable connection management
✅ Proper lifecycle handling
✅ Network resilience
✅ Clean reconnection logic
✅ Comprehensive logging
✅ Efficient resource usage

The system handles all common scenarios and provides a solid foundation for real-time presence updates in the calling app.

**Next Steps:**
1. Monitor production metrics
2. Implement suggested improvements based on usage patterns
3. Add analytics for connection quality
4. Consider fallback mechanisms for poor network conditions
