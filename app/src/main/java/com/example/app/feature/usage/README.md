# Usage Statistics Feature

## Overview
The Usage Statistics feature provides users with visual insights into their call usage patterns, showing daily audio and video call durations over time.

## Features

### 📊 Visual Chart
- **Dual-line chart** displaying audio (green) and video (yellow) call durations
- **Smooth animations** when data loads or filters change
- **Interactive data points** showing exact values
- **Grid lines** for easy reading
- **Date labels** on X-axis
- **Duration labels** on Y-axis (in minutes)

### 🔍 Filters
1. **10 Days** - Shows last 10 days of usage
2. **30 Days** - Shows last 30 days of usage
3. **Custom Range** - Allows selecting specific from/to dates

### 📈 Summary Cards
- **Audio Calls Total** - Sum of all audio call minutes in selected range
- **Video Calls Total** - Sum of all video call minutes in selected range

### 📅 Date Picker
- Material Design 3 date picker
- Separate pickers for "From" and "To" dates
- Only available when "Custom" filter is selected

## Architecture

### Package Structure
```
feature/usage/
├── ui/
│   ├── UsageStatisticsScreen.kt      # Main screen composable
│   └── UsageStatisticsViewModel.kt   # ViewModel with business logic
├── data/
│   ├── UsageApi.kt                   # Retrofit API interface
│   ├── UsageDto.kt                   # Data transfer objects
│   └── UsageRepository.kt            # Repository for data operations
├── API_SPECIFICATION.md              # API documentation
└── README.md                         # This file
```

### Data Flow
```
UI (Screen) 
    ↓
ViewModel (Business Logic)
    ↓
Repository (Data Layer)
    ↓
API (Network)
    ↓
Backend Server
```

## Components

### UsageStatisticsScreen
Main composable containing:
- `FilterSection` - Filter chips and date selectors
- `SummaryCard` - Total minutes display
- `UsageChart` - Line chart visualization
- `ChartLegend` - Color legend for chart lines
- `CustomDatePickerDialog` - Date picker dialog

### UsageStatisticsViewModel
Manages:
- Filter selection state
- Custom date range state
- Chart data generation (currently static)
- Total calculations

### Data Models

#### `DailyUsage`
```kotlin
data class DailyUsage(
    val date: LocalDate,
    val audioMinutes: Int,
    val videoMinutes: Int
)
```

#### `FilterType`
```kotlin
enum class FilterType {
    TEN_DAYS,
    THIRTY_DAYS,
    CUSTOM
}
```

#### `UsageStatisticsUiState`
```kotlin
data class UsageStatisticsUiState(
    val selectedFilter: FilterType,
    val chartData: List<DailyUsage>,
    val totalAudioMinutes: Int,
    val totalVideoMinutes: Int,
    val customFromDate: LocalDate?,
    val customToDate: LocalDate?
)
```

## Navigation

### Routes
- **Route:** `Routes.Screen.Usage.STATISTICS`
- **Path:** `"usage_statistics"`

### Entry Points
1. **Home Screen** → User Settings → Usage Statistics
2. **Listener Dashboard** → Settings → Usage Statistics

### Navigation Code
```kotlin
navController.navigate(Routes.Screen.Usage.STATISTICS) {
    launchSingleTop = true
}
```

## Current Implementation

### Static Data
The feature currently uses **static/mock data** for testing:
- Random values between 10-120 minutes for audio
- Random values between 5-100 minutes for video
- Generated based on selected filter range

### API Integration (Ready)
All data layer components are ready for API integration:
- `UsageApi` interface defined
- `UsageRepository` with API call logic
- DTO to domain model mapping

## Future Integration

### Steps to Connect Real API

1. **Add API to NetworkModule**
```kotlin
@Provides
@Singleton
fun provideUsageApi(retrofit: Retrofit): UsageApi {
    return retrofit.create(UsageApi::class.java)
}
```

2. **Inject Repository in ViewModel**
```kotlin
@HiltViewModel
class UsageStatisticsViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel()
```

3. **Replace Static Data**
```kotlin
private fun loadData() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        val result = usageRepository.getUsageStatistics(
            fromDate = calculateFromDate(),
            toDate = calculateToDate()
        )
        
        when (result) {
            is ApiResult.Success -> {
                val data = result.data
                _uiState.value = _uiState.value.copy(
                    chartData = data,
                    totalAudioMinutes = data.sumOf { it.audioMinutes },
                    totalVideoMinutes = data.sumOf { it.videoMinutes },
                    isLoading = false
                )
            }
            is ApiResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    error = result.message,
                    isLoading = false
                )
            }
        }
    }
}
```

## UI Specifications

### Colors
- **Audio Line:** `#4CAF50` (Green)
- **Video Line:** `#FFC107` (Yellow/Amber)
- **Grid Lines:** Surface color with 10% opacity
- **Background:** Material Theme surface colors

### Dimensions
- **Chart Height:** 300dp
- **Chart Padding:** 40dp (for labels)
- **Line Width:** 6dp
- **Point Radius:** 8dp (outer), 4dp (inner)
- **Filter Chip Height:** 40dp
- **Date Selector Height:** 56dp

### Animations
- **Chart Lines:** 1000ms ease-in-out animation
- **Filter Selection:** Instant color change
- **Data Updates:** Smooth transition with progress animation

## Testing

### Manual Testing Checklist
- [ ] Chart displays correctly with data
- [ ] Filter chips change selection
- [ ] 10 days filter shows 10 data points
- [ ] 30 days filter shows 30 data points
- [ ] Custom filter shows date selectors
- [ ] Date picker opens and closes
- [ ] Selected dates update the chart
- [ ] Summary cards show correct totals
- [ ] Chart animates smoothly
- [ ] Back button returns to settings
- [ ] Works on both customer and listener roles

### Edge Cases
- [ ] No data available (empty state)
- [ ] Single day of data
- [ ] Very high usage values (chart scaling)
- [ ] Zero usage days
- [ ] Custom range with same from/to date

## Dependencies

### Required Libraries
- Jetpack Compose (UI)
- Hilt (Dependency Injection)
- Retrofit (API calls)
- Navigation Compose (Navigation)
- Material 3 (Design system)

### No External Chart Library
This implementation uses **Canvas API** for custom chart rendering, avoiding external dependencies.

## Performance Considerations

- Chart rendering is optimized with `remember` and `derivedStateOf`
- Animation progress is controlled to prevent excessive recomposition
- Data is cached in ViewModel to avoid unnecessary API calls
- Date calculations are performed once per filter change

## Accessibility

- All interactive elements have content descriptions
- Color is not the only indicator (legend provided)
- Touch targets meet minimum size requirements (48dp)
- Text contrast meets WCAG guidelines

## Known Limitations

1. **Static Data:** Currently using mock data
2. **No Caching:** Data is not persisted locally
3. **No Pull-to-Refresh:** Manual refresh not implemented
4. **No Export:** Cannot export data to CSV/PDF
5. **No Detailed View:** Cannot tap data points for details

## Future Enhancements

- [ ] Add loading states
- [ ] Add error handling UI
- [ ] Add pull-to-refresh
- [ ] Add data export functionality
- [ ] Add detailed view on data point tap
- [ ] Add comparison with previous period
- [ ] Add weekly/monthly aggregation views
- [ ] Add call count (not just duration)
- [ ] Add average call duration
- [ ] Add peak usage time analysis
