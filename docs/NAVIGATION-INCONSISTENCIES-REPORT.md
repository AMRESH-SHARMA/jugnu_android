# 🧭 Navigation Inconsistencies Report

**Analysis Date:** February 7, 2026  
**Focus:** Navigation architecture and implementation issues  
**Severity:** HIGH - Affects user experience and app stability

---

## 📊 EXECUTIVE SUMMARY

### Issues Found: 8 Critical Navigation Problems

1. **Inconsistent Navigation Patterns** - Mixed approaches across screens
2. **Deprecated SelectUserRole Flow** - Should use AUTH but still in use
3. **Missing Back Stack Management** - Inconsistent popUpTo usage
4. **Global SessionManager in Navigation** - Tight coupling
5. **No Navigation State Persistence** - Lost on configuration changes
6. **Hardcoded Navigation Logic** - Scattered across multiple files
7. **Missing Deep Link Support** - No deep link handling
8. **No Navigation Testing** - Cannot verify navigation flows

---

## 🔴 ISSUE 1: Inconsistent Navigation Patterns

**Severity:** HIGH | **Risk:** 7/10  
**Files:** Multiple navigation graphs and screens

### Problem

**Different navigation approaches used across the app:**

```kotlin
// Pattern 1: Direct navigation (OtpScreen.kt)
navController.navigate(Routes.Graph.HOME) {
    popUpTo(Routes.Graph.AUTH) {
        inclusive = true
    }
}

// Pattern 2: Simple navigation (WalletScreen.kt)
navController.navigate(enterAmount(AmountFlowType.ADD.name))

// Pattern 3: Navigation with callback (HomeNavGraph.kt)
navController.openChat(listener) // Extension function

// Pattern 4: LaunchedEffect navigation (HomeNavGraph.kt)
LaunchedEffect(Unit) {
    viewModel.showOfferEvent.collect {
        navController.navigate(Routes.Screen.Home.OFFER_MODAL)
    }
}

// Pattern 5: Automatic navigation (AppNavGraph.kt)
LaunchedEffect(Unit) {
    CallStore.call.collect { call ->
        when (call?.status) {
            CallStatus.CONNECTED -> {
                navController.navigate(Routes.Screen.Call.ONGOING)
            }
        }
    }
}
```

### Impact
- **Maintenance Nightmare:** Hard to understand navigation flow
- **Bugs:** Different patterns lead to different behaviors
- **Testing:** Cannot test navigation consistently
- **Onboarding:** New developers confused by multiple patterns

### Solution

**Create unified navigation pattern:**

```kotlin
// core/navigation/NavigationManager.kt
@Singleton
class NavigationManager @Inject constructor() {
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    sealed class NavigationEvent {
        data class NavigateTo(
            val route: String,
            val popUpTo: String? = null,
            val inclusive: Boolean = false,
            val singleTop: Boolean = false
        ) : NavigationEvent()
        
        object NavigateBack : NavigationEvent()
        
        data class NavigateToGraph(
            val graph: String,
            val clearBackStack: Boolean = false
        ) : NavigationEvent()
    }
    
    suspend fun navigateTo(
        route: String,
        popUpTo: String? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        _navigationEvents.emit(
            NavigationEvent.NavigateTo(route, popUpTo, inclusive, singleTop)
        )
    }
    
    suspend fun navigateToGraph(graph: String, clearBackStack: Boolean = false) {
        _navigationEvents.emit(
            NavigationEvent.NavigateToGraph(graph, clearBackStack)
        )
    }
    
    suspend fun navigateBack() {
        _navigationEvents.emit(NavigationEvent.NavigateBack)
    }
}

// Update AppNavGraph.kt
@Composable
fun AppNavGraph(
    navigationManager: NavigationManager = hiltViewModel<AppViewModel>().navigationManager
) {
    val navController = rememberNavController()
    
    // Single navigation handler
    LaunchedEffect(Unit) {
        navigationManager.navigationEvents.collect { event ->
            when (event) {
                is NavigationManager.NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route) {
                        event.popUpTo?.let { popUpTo(it) { inclusive = event.inclusive } }
                        launchSingleTop = event.singleTop
                    }
                }
                is NavigationManager.NavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is NavigationManager.NavigationEvent.NavigateToGraph -> {
                    navController.navigate(event.graph) {
                        if (event.clearBackStack) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
    
    // Rest of navigation setup...
}

// Usage in ViewModels
class OtpViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    ...
) : ViewModel() {
    
    fun onOtpVerified() {
        viewModelScope.launch {
            navigationManager.navigateToGraph(
                graph = Routes.Graph.HOME,
                clearBackStack = true
            )
        }
    }
}
```

---

## 🔴 ISSUE 2: Deprecated SelectUserRole Flow Still Active

**Severity:** HIGH | **Risk:** 8/10  
**Files:** `AppNavGraph.kt:93`, `SelectUserRoleNavGraph.kt`, `SelectUserRoleScreen.kt`

### Problem

```kotlin
// AppNavGraph.kt
NavHost(
    navController = navController,
    startDestination = Routes.Graph.SELECT_USER_ROLE  // ❌ Should be AUTH
    //TODO
//  startDestination = Routes.Graph.AUTH
) {
    authNavGraph(navController)
    selectUserRoleNavGraph(navController)  // ❌ Deprecated flow
    homeNavGraph(navController)
    // ...
}
```

**Issues:**
- SelectUserRole is deprecated but still the default start
- Auth flow exists but commented out
- Two authentication flows confuse users
- No migration path documented

### Impact
- **User Confusion:** Two ways to enter app
- **Security Risk:** SelectUserRole bypasses proper auth
- **Technical Debt:** Maintaining deprecated code
- **Testing:** Must test both flows

### Solution

**Step 1: Remove deprecated flow**
```kotlin
// AppNavGraph.kt
NavHost(
    navController = navController,
    startDestination = Routes.Graph.AUTH  // ✅ Use proper auth
) {
    authNavGraph(navController)
    // Remove: selectUserRoleNavGraph(navController)
    homeNavGraph(navController)
    listenerNavGraph(navController)
    walletNavGraph(navController)
    callNavGraph(navController)
}
```

**Step 2: Delete deprecated files**
```bash
# Files to delete:
- app/src/main/java/com/example/app/feature/navigation/ui/SelectUserRoleNavGraph.kt
- app/src/main/java/com/example/app/feature/login/ui/SelectUserRoleScreen.kt
- app/src/main/java/com/example/app/feature/login/ui/SelectUserRoleViewModel.kt
```

**Step 3: Update Routes**
```kotlin
// Routes.kt
object Routes {
    object Graph {
        // Remove: const val SELECT_USER_ROLE = "select_user_role_graph"
        const val HOME = "home_graph"
        const val LISTENER = "listener_graph"
        const val CHAT = "chat_graph"
        const val WALLET = "wallet_graph"
        const val CALL = "call_graph"
        const val AUTH = "auth_graph"
    }
    
    object Screen {
        // Remove SelectUserRole object
        // Keep only: Auth, Home, Listener, Chat, Wallet, Call
    }
}
```

**Step 4: Update OTP success navigation**
```kotlin
// OtpScreen.kt
LaunchedEffect(verifyState) {
    if (verifyState is OtpUiState.Success) {
        val data = (verifyState as OtpUiState.Success).data
        
        // Navigate based on user role from backend
        val destination = when (data?.role) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
            else -> Routes.Graph.HOME
        }
        
        navController.navigate(destination) {
            popUpTo(Routes.Graph.AUTH) { inclusive = true }
        }
    }
}
```

---

## 🟠 ISSUE 3: Inconsistent Back Stack Management

**Severity:** MEDIUM | **Risk:** 6/10  
**Files:** Multiple screens with navigation

### Problem

**Different popUpTo patterns:**

```kotlin
// Pattern 1: Clear entire back stack
navController.navigate(Routes.Graph.HOME) {
    popUpTo(Routes.Graph.AUTH) { inclusive = true }
}

// Pattern 2: Clear specific graph
navController.navigate(Routes.Graph.LISTENER) {
    popUpTo(Routes.Graph.CALL) { inclusive = true }
}

// Pattern 3: No back stack management
navController.navigate(Routes.Screen.Home.OFFER_MODAL)

// Pattern 4: Implicit back stack (HomeNavGraph.kt)
chatNavGraph(navController, onBack = { navController.popBackStack() })
```

### Impact
- **Back Button Issues:** Unexpected navigation behavior
- **Memory Leaks:** Old screens not cleared from stack
- **User Confusion:** Back button goes to wrong screen

### Solution

**Define clear back stack rules:**

```kotlin
// core/navigation/NavigationRules.kt
object NavigationRules {
    
    /**
     * Authentication to Home/Listener
     * Clear auth stack completely
     */
    fun NavOptionsBuilder.clearAuthStack() {
        popUpTo(Routes.Graph.AUTH) { inclusive = true }
        launchSingleTop = true
    }
    
    /**
     * Call ended, return to previous screen
     * Clear call stack
     */
    fun NavOptionsBuilder.clearCallStack(userRole: UserRole) {
        val destination = when (userRole) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
        }
        popUpTo(destination) { inclusive = false }
        launchSingleTop = true
    }
    
    /**
     * Modal/Dialog navigation
     * Keep current stack
     */
    fun NavOptionsBuilder.modalNavigation() {
        launchSingleTop = true
    }
    
    /**
     * Nested navigation (Chat, Wallet)
     * Allow back to parent
     */
    fun NavOptionsBuilder.nestedNavigation(parentRoute: String) {
        popUpTo(parentRoute) { inclusive = false }
    }
}

// Usage
navController.navigate(Routes.Graph.HOME) {
    clearAuthStack()
}

navController.navigate(Routes.Graph.LISTENER) {
    clearCallStack(SessionManager.userRole)
}
```

---

## 🟠 ISSUE 4: Global SessionManager in Navigation Logic

**Severity:** MEDIUM | **Risk:** 6/10  
**File:** `AppNavGraph.kt:67-78`

### Problem

```kotlin
// AppNavGraph.kt
LaunchedEffect(Unit) {
    CallStore.call.collect { call ->
        when (call?.status) {
            CallStatus.ENDED, null -> {
                when (SessionManager.userRole) {  // ❌ Global state
                    UserRole.LISTENER -> {
                        navController.navigate(Routes.Graph.LISTENER) {
                            popUpTo(Routes.Graph.CALL) { inclusive = true }
                        }
                    }
                    UserRole.CUSTOMER -> {
                        navController.navigate(Routes.Graph.HOME) {
                            popUpTo(Routes.Graph.CALL) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}
```

**Issues:**
- Direct dependency on global SessionManager
- Hard to test
- Tight coupling
- No error handling if role is null

### Solution

```kotlin
// AppNavGraph.kt
@Composable
fun AppNavGraph(
    viewModel: AppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val userRole by viewModel.userRole.collectAsState()
    
    LaunchedEffect(Unit) {
        CallStore.call
            .drop(1)
            .collect { call ->
                when (call?.status) {
                    CallStatus.ENDED, null -> {
                        val destination = viewModel.getPostCallDestination()
                        navController.navigate(destination) {
                            popUpTo(Routes.Graph.CALL) { inclusive = true }
                        }
                    }
                }
            }
    }
}

// AppViewModel.kt
class AppViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    val userRole: StateFlow<UserRole> = sessionRepository.sessionState
        .map { it?.role ?: UserRole.CUSTOMER }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserRole.CUSTOMER)
    
    fun getPostCallDestination(): String {
        return when (userRole.value) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
        }
    }
}
```

---

## 🟡 ISSUE 5: No Navigation State Persistence

**Severity:** MEDIUM | **Risk:** 5/10  
**File:** `AppNavGraph.kt`

### Problem

```kotlin
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()  // ❌ Lost on rotation
    // ...
}
```

### Solution

Already covered in UI/UX Issues (MEDIUM-009). Use `rememberSaveable` with custom saver.

---

## 🟡 ISSUE 6: Hardcoded Navigation Logic Scattered

**Severity:** MEDIUM | **Risk:** 5/10  
**Files:** Multiple screens

### Problem

Navigation logic duplicated across screens:

```kotlin
// OtpScreen.kt
navController.navigate(Routes.Graph.HOME) {
    popUpTo(Routes.Graph.AUTH) { inclusive = true }
}

// SelectUserRoleScreen.kt
navController.navigate(Routes.Graph.HOME) {
    popUpTo(Routes.Screen.SelectUserRole.ROOT) { inclusive = true }
}

// AppNavGraph.kt
navController.navigate(Routes.Graph.HOME) {
    popUpTo(Routes.Graph.CALL) { inclusive = true }
}
```

### Solution

**Centralize navigation actions:**

```kotlin
// core/navigation/NavigationActions.kt
class NavigationActions(private val navController: NavController) {
    
    fun navigateToHome(clearBackStack: Boolean = true) {
        navController.navigate(Routes.Graph.HOME) {
            if (clearBackStack) {
                popUpTo(0) { inclusive = true }
            }
            launchSingleTop = true
        }
    }
    
    fun navigateToListener(clearBackStack: Boolean = true) {
        navController.navigate(Routes.Graph.LISTENER) {
            if (clearBackStack) {
                popUpTo(0) { inclusive = true }
            }
            launchSingleTop = true
        }
    }
    
    fun navigateAfterAuth(role: UserRole) {
        val destination = when (role) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
        }
        navController.navigate(destination) {
            popUpTo(Routes.Graph.AUTH) { inclusive = true }
            launchSingleTop = true
        }
    }
    
    fun navigateAfterCall(role: UserRole) {
        val destination = when (role) {
            UserRole.LISTENER -> Routes.Graph.LISTENER
            UserRole.CUSTOMER -> Routes.Graph.HOME
        }
        navController.navigate(destination) {
            popUpTo(Routes.Graph.CALL) { inclusive = true }
        }
    }
}

// Provide as composable
@Composable
fun rememberNavigationActions(
    navController: NavController = rememberNavController()
): NavigationActions {
    return remember(navController) {
        NavigationActions(navController)
    }
}

// Usage
@Composable
fun OtpScreen(navController: NavController) {
    val navigationActions = rememberNavigationActions(navController)
    
    LaunchedEffect(verifyState) {
        if (verifyState is OtpUiState.Success) {
            navigationActions.navigateAfterAuth(userRole)
        }
    }
}
```

---

## 🟡 ISSUE 7: Missing Deep Link Support

**Severity:** MEDIUM | **Risk:** 4/10  
**Files:** All navigation graphs

### Problem

No deep link handling for:
- Incoming call notifications
- Chat messages
- Payment links
- Shared content

### Solution

```kotlin
// AndroidManifest.xml
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="jugnu"
            android:host="app" />
    </intent-filter>
</activity>

// Add deep links to navigation
composable(
    route = Routes.Screen.Call.ONGOING,
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "jugnu://app/call/{callId}"
        }
    )
) {
    OnGoingCallScreen()
}

composable(
    route = Routes.Screen.Chat.ROOT,
    deepLinks = listOf(
        navDeepLink {
            uriPattern = "jugnu://app/chat/{listenerId}"
        }
    )
) {
    ChatScreen()
}
```

---

## 🟡 ISSUE 8: No Navigation Testing

**Severity:** MEDIUM | **Risk:** 4/10  
**Files:** No test files exist

### Problem

No tests for navigation flows:
- Auth → Home flow
- Call → Home flow
- Back button behavior
- Deep link handling

### Solution

```kotlin
// feature/navigation/NavigationTest.kt
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun authFlow_navigatesToHomeAfterOtpVerification() {
        // Start at login
        composeTestRule.onNodeWithText("Login").assertExists()
        
        // Enter phone and request OTP
        composeTestRule.onNodeWithTag("phone_input").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Send OTP").performClick()
        
        // Verify OTP screen
        composeTestRule.onNodeWithText("Enter OTP").assertExists()
        
        // Enter OTP
        composeTestRule.onNodeWithTag("otp_input").performTextInput("123456")
        composeTestRule.onNodeWithText("Verify").performClick()
        
        // Should navigate to Home
        composeTestRule.waitUntil(5000) {
            composeTestRule.onNodeWithText("Home").isDisplayed()
        }
    }
    
    @Test
    fun backButton_fromHome_exitsApp() {
        // Navigate to home
        // ...
        
        // Press back
        Espresso.pressBack()
        
        // App should exit (activity finished)
        assertTrue(composeTestRule.activity.isFinishing)
    }
}
```

---

## 📋 SUMMARY & ACTION PLAN

### Immediate Actions (Week 1)
1. ✅ Remove SelectUserRole flow completely
2. ✅ Set AUTH as startDestination
3. ✅ Create NavigationManager for unified pattern
4. ✅ Define NavigationRules for back stack

### Short Term (Week 2-3)
5. ✅ Centralize navigation actions
6. ✅ Remove SessionManager from navigation
7. ✅ Add navigation state persistence
8. ✅ Implement deep link support

### Long Term (Week 4+)
9. ✅ Write navigation tests
10. ✅ Document navigation architecture
11. ✅ Add navigation analytics
12. ✅ Create navigation diagram

---

## 🎯 RECOMMENDED NAVIGATION ARCHITECTURE

```
┌─────────────────────────────────────────────────────────┐
│                     App Launch                          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
            ┌────────────────┐
            │  Splash Screen │
            └────────┬───────┘
                     │
                     ▼
         ┌───────────────────────┐
         │  Check Session Valid? │
         └───────┬───────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
   ┌────────┐      ┌──────────┐
   │  AUTH  │      │   HOME   │ (Customer)
   │ Graph  │      │  Graph   │
   └────┬───┘      └────┬─────┘
        │               │
        │               ├─→ Listeners
        │               ├─→ Chat
        │               ├─→ Wallet
        │               └─→ Call
        │
        ▼
   ┌──────────┐
   │ LISTENER │ (Listener)
   │  Graph   │
   └────┬─────┘
        │
        ├─→ Dashboard
        ├─→ Earnings
        ├─→ Wallet
        └─→ Call

Note: SelectUserRole removed, Auth is entry point
```

---

**Files to Update:**
- ✅ `AppNavGraph.kt` - Remove SelectUserRole, add NavigationManager
- ✅ `Routes.kt` - Remove SelectUserRole routes
- ❌ Delete `SelectUserRoleNavGraph.kt`
- ❌ Delete `SelectUserRoleScreen.kt`
- ❌ Delete `SelectUserRoleViewModel.kt`
- ✅ Create `NavigationManager.kt`
- ✅ Create `NavigationActions.kt`
- ✅ Create `NavigationRules.kt`
