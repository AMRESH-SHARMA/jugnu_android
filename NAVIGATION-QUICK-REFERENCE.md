# 🧭 Navigation Quick Reference Guide

Quick reference for the updated navigation architecture.

---

## 📱 NAVIGATION FLOW

### **Authentication Flow**
```
App Start
    ↓
Splash Screen
    ↓
App Config Check
    ↓
Login Screen (Phone Number)
    ↓
OTP Screen
    ↓
Backend determines role
    ↓
┌─────────────────┬─────────────────┐
│   CUSTOMER      │    LISTENER     │
│   → HOME        │   → LISTENER    │
└─────────────────┴─────────────────┘
```

### **Customer Navigation**
```
HOME (Listener List)
    ├── Chat with Listener
    │   ├── Voice Call
    │   └── Video Call
    ├── Wallet
    │   └── Add Money
    └── User Profile
        └── Wallet
```

### **Listener Navigation**
```
LISTENER (Dashboard)
    ├── Wallet
    │   └── Withdraw Money
    └── Stats & Analytics
```

---

## 🔑 KEY NAVIGATION PATTERNS

### **1. Role-Based Routing (OtpScreen.kt)**
```kotlin
val destination = when (data?.role) {
    UserRole.LISTENER -> Routes.Graph.LISTENER
    UserRole.CUSTOMER -> Routes.Graph.HOME
    else -> Routes.Graph.HOME
}

navController.navigate(destination) {
    popUpTo(Routes.Graph.AUTH) { inclusive = true }
    launchSingleTop = true
}
```

### **2. Chat Navigation (HomeNavGraph.kt)**
```kotlin
HomeScreen(
    navController = navController,
    onListenerClick = { listener ->
        navController.navigate(Routes.Screen.Chat.chatRoute(listener.id)) {
            launchSingleTop = true
        }
    }
)
```

### **3. Wallet Navigation**
```kotlin
// From any screen
navController.navigate(Routes.Graph.WALLET) {
    launchSingleTop = true
}

// To enter amount screen
navController.navigate(
    Routes.Screen.Wallet.enterAmountRoute("ADD")  // or "WITHDRAW"
) {
    launchSingleTop = true
}
```

### **4. Call Initiation (Future)**
```kotlin
// In ChatScreen - via ViewModel
onVoiceCall = {
    viewModel.initiateVoiceCall(listenerId)
    // Navigation happens via CallStore observer
}
```

---

## 📂 NAVIGATION GRAPHS

### **Graph Hierarchy**
```
AppNavGraph (Root)
├── AUTH Graph
│   ├── Login Screen
│   └── OTP Screen
├── HOME Graph (Customer)
│   ├── Home Screen (Listener List)
│   └── Offer Modal
├── LISTENER Graph (Listener)
│   └── Dashboard Screen
├── CHAT Graph (Shared)
│   └── Chat Screen
├── WALLET Graph (Shared)
│   ├── Wallet Screen
│   └── Enter Amount Screen
└── CALL Graph (Shared)
    └── Ongoing Call Screen
```

### **Graph Routes**
```kotlin
Routes.Graph.AUTH       // Authentication flow
Routes.Graph.HOME       // Customer home
Routes.Graph.LISTENER   // Listener dashboard
Routes.Graph.CHAT       // Chat screens
Routes.Graph.WALLET     // Wallet screens
Routes.Graph.CALL       // Call screens
```

---

## 🎯 SCREEN ROUTES

### **Auth Screens**
```kotlin
Routes.Screen.Auth.LOGIN                    // Login screen
Routes.Screen.Auth.otpRoute(phoneNumber)    // OTP screen
```

### **Home Screens**
```kotlin
Routes.Screen.Home.ROOT                     // Home screen
Routes.Screen.Home.OFFER_MODAL              // Offer dialog
```

### **Listener Screens**
```kotlin
Routes.Screen.Listener.DASHBOARD            // Listener dashboard
```

### **Chat Screens**
```kotlin
Routes.Screen.Chat.chatRoute(listenerId)    // Chat screen
```

### **Wallet Screens**
```kotlin
Routes.Graph.WALLET                         // Wallet home
Routes.Screen.Wallet.enterAmountRoute(type) // Enter amount (ADD/WITHDRAW)
```

### **Call Screens**
```kotlin
Routes.Screen.Call.ONGOING                  // Ongoing call
```

---

## 🔄 BACK NAVIGATION

### **Back Button Behavior**
```kotlin
// Standard back navigation
navController.popBackStack()

// Back to specific destination
navController.popBackStack(
    route = Routes.Screen.Home.ROOT,
    inclusive = false
)

// Clear entire stack
navController.navigate(destination) {
    popUpTo(Routes.Graph.AUTH) { inclusive = true }
}
```

### **Back Stack Examples**

**Customer Flow:**
```
Login → OTP → HOME → Chat → Call
                ↑      ↑      ↑
              Back   Back   Back
```

**Listener Flow:**
```
Login → OTP → LISTENER → Wallet
                ↑          ↑
              Back       Back
```

---

## 🛠️ COMMON NAVIGATION TASKS

### **Navigate to Chat**
```kotlin
navController.navigate(Routes.Screen.Chat.chatRoute(listenerId)) {
    launchSingleTop = true
}
```

### **Navigate to Wallet**
```kotlin
navController.navigate(Routes.Graph.WALLET) {
    launchSingleTop = true
}
```

### **Navigate to Add Money**
```kotlin
navController.navigate(
    Routes.Screen.Wallet.enterAmountRoute("ADD")
) {
    launchSingleTop = true
}
```

### **Navigate to Withdraw Money**
```kotlin
navController.navigate(
    Routes.Screen.Wallet.enterAmountRoute("WITHDRAW")
) {
    launchSingleTop = true
}
```

### **Logout (Clear Stack)**
```kotlin
navController.navigate(Routes.Graph.AUTH) {
    popUpTo(0) { inclusive = true }
    launchSingleTop = true
}
```

---

## ⚠️ IMPORTANT RULES

### **Always Use launchSingleTop**
```kotlin
// ✅ CORRECT
navController.navigate(destination) {
    launchSingleTop = true
}

// ❌ WRONG - Can create duplicates
navController.navigate(destination)
```

### **Use Route Helpers**
```kotlin
// ✅ CORRECT
Routes.Screen.Chat.chatRoute(listenerId)
Routes.Screen.Wallet.enterAmountRoute("ADD")

// ❌ WRONG - Hardcoded strings
"chat/$listenerId"
"wallet/enter_amount/ADD"
```

### **Role-Based Features**
```kotlin
// ✅ CORRECT - Check user role
when (userRole) {
    UserRole.CUSTOMER -> showAddMoney()
    UserRole.LISTENER -> showWithdraw()
}

// ❌ WRONG - Show all features
showAddMoney()
showWithdraw()
```

---

## 🐛 TROUBLESHOOTING

### **Issue: Duplicate screens in back stack**
**Solution:** Add `launchSingleTop = true` to navigation

### **Issue: Back button doesn't work**
**Solution:** Use `navController.popBackStack()` instead of custom logic

### **Issue: Wrong screen after login**
**Solution:** Check role-based routing in OtpScreen.kt

### **Issue: Wallet shows wrong actions**
**Solution:** Verify user role is correctly passed to WalletScreen

---

## 📚 RELATED FILES

- `Routes.kt` - All route definitions
- `AppNavGraph.kt` - Root navigation setup
- `HomeNavGraph.kt` - Customer navigation
- `ListenerNavGraph.kt` - Listener navigation
- `ChatNavGraph.kt` - Chat navigation
- `WalletNavGraph.kt` - Wallet navigation
- `CallNavGraph.kt` - Call navigation

---

**Last Updated:** February 8, 2026
