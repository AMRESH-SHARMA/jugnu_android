# WebSocket Presence Debugging Guide

## Quick Log Filtering

```bash
# View all WebSocket logs
adb logcat -s RTM

# View with timestamps
adb logcat -v time -s RTM

# Save to file
adb logcat -s RTM > ws_debug.txt
```

## Log Flow Scenarios

### 1. Normal Connection Flow
```
D/RTM: WS connecting... sessionId=xxx, accountId=123, role=CUSTOMER
D/RTM: WS onOpen() - connection established, response code=101
D/RTM: PresenceManager: onConnected() - current state=OFFLINE
D/RTM: PresenceStore: setState() - OFFLINE -> ONLINE
```

### 2. App Goes to Background
```
D/RTM: EventObserver: App foreground state changed - isForeground=false
D/RTM: PresenceManager: onAppBackground()
D/RTM: WS disconnect() called
D/RTM: WS onClosed() - code=1000, reason=disconnect
D/RTM: PresenceManager: onDisconnected() - current state=ONLINE
D/RTM: PresenceStore: setState() - ONLINE -> OFFLINE
```

### 3. App Comes to Foreground
```
D/RTM: EventObserver: App foreground state changed - isForeground=true
D/RTM: PresenceManager: onAppForeground()
D/RTM: WS connecting... sessionId=xxx, accountId=123, role=CUSTOMER
D/RTM: WS onOpen() - connection established, response code=101
```

### 4. Network Lost
```
D/RTM: NetworkStateTracker: Network lost - Network 100
D/RTM: EventObserver: Network state changed - isAvailable=false
D/RTM: PresenceManager: onNetworkLost()
D/RTM: WS sendNetOffline() sent=true
D/RTM: WS onFailure() - error: Socket closed, response: null
```

### 5. Network Restored
```
D/RTM: NetworkStateTracker: Network available - Network 101
D/RTM: EventObserver: Network state changed - isAvailable=true
D/RTM: PresenceManager: onNetworkAvailable()
D/RTM: WS connecting... sessionId=xxx, accountId=123, role=CUSTOMER
```

### 6. Duplicate Connection
```
D/RTM: WS onMessage() received: {"type":"connection_replaced"}
W/RTM: WS connection replaced by another session - closing old connection
D/RTM: WS disconnect() called
```

### 7. Call Started
```
D/RTM: EventObserver: Call event received - CallEvent.Outgoing(...)
D/RTM: PresenceManager: onCallStarted() - current state=ONLINE
D/RTM: PresenceStore: setState() - ONLINE -> BUSY
D/RTM: WS sendCallStart() sent=true
```

### 8. Call Ended
```
D/RTM: EventObserver: Call event received - CallEvent.Ended
D/RTM: PresenceManager: onCallEnded() - current state=BUSY
D/RTM: WS sendCallEnd() sent=true
D/RTM: PresenceStore: setState() - BUSY -> ONLINE
```

### 9. Receiving Remote Presence Update
```
D/RTM: WS onMessage() received: {"account_id":"456","status":"BUSY"}
D/RTM: WS presence update: accountId=456, status=BUSY
D/RTM: RemotePresenceStore: update() - accountId=456, status=BUSY
D/RTM: EventObserver: Presence event received - PresenceEvent.StatusChanged(...)
D/RTM: PresenceManager: onRemoteStateChanged() - new state=BUSY, current=ONLINE
```

### 10. Connection Failure with Retry
```
E/RTM: WS onFailure() - error: Failed to connect, response: null
D/RTM: PresenceManager: onDisconnected() - current state=ONLINE
D/RTM: PresenceStore: setState() - ONLINE -> OFFLINE
D/RTM: WS scheduleReconnect() in 1000ms
D/RTM: WS connecting... sessionId=xxx, accountId=123, role=CUSTOMER
```

## Common Issues & Solutions

### Issue: WebSocket not connecting
**Look for**:
```
D/RTM: WS connect() skipped - user not logged in
```
**Solution**: Ensure user is logged in with valid session ID

### Issue: Reconnection loop
**Look for**:
```
E/RTM: WS onFailure() - error: ...
D/RTM: WS scheduleReconnect() in 2000ms
D/RTM: WS scheduleReconnect() in 4000ms
D/RTM: WS scheduleReconnect() in 8000ms
```
**Solution**: Check server availability and network connection

### Issue: Duplicate connections not handled
**Look for**: Missing `connection_replaced` message
**Solution**: Ensure server sends `{"type":"connection_replaced"}` when detecting duplicate session ID

### Issue: App stays connected in background
**Look for**: Missing background disconnect logs
**Solution**: Verify `AppForegroundTracker` is registered with `ProcessLifecycleOwner`

### Issue: Network changes not detected
**Look for**: Missing network state change logs
**Solution**: Ensure `NetworkStateTracker.startTracking()` is called in `EventObserver.init`

## Testing Checklist

- [ ] Connect to WebSocket (check session ID in logs)
- [ ] Put app in background (should disconnect)
- [ ] Bring app to foreground (should reconnect)
- [ ] Turn off WiFi (should detect network loss)
- [ ] Turn on WiFi (should reconnect)
- [ ] Switch WiFi to Mobile data (should reconnect)
- [ ] Start a call (should send CALL_START, state → BUSY)
- [ ] End a call (should send CALL_END, state → ONLINE)
- [ ] Login on another device (should receive connection_replaced)
- [ ] Force close app (server should timeout after 60-90s)

## Log Analysis Tips

1. **Filter by specific flow**: `adb logcat -s RTM | grep "connecting"`
2. **Count reconnections**: `adb logcat -s RTM | grep "scheduleReconnect" | wc -l`
3. **Track state changes**: `adb logcat -s RTM | grep "setState"`
4. **Monitor network**: `adb logcat -s RTM | grep "Network"`
5. **Watch messages**: `adb logcat -s RTM | grep "onMessage"`

## Performance Monitoring

Watch for these patterns:

**Good**:
- Reconnect delays increase exponentially (1s, 2s, 4s, 8s...)
- Clean disconnects show code=1000
- State transitions are logical (OFFLINE → ONLINE → BUSY → ONLINE)

**Bad**:
- Rapid reconnection attempts (< 1s apart)
- Unexpected close codes (1006 = abnormal closure)
- State flip-flopping (ONLINE → OFFLINE → ONLINE rapidly)
- Missing pong responses (connection timeout)
