# Usage Statistics API Specification

## Overview
This document describes the API endpoint for fetching user call usage statistics.

## Endpoint

**Method:** `GET`  
**URL:** `/usage/statistics`  
**Authentication:** Required (Bearer Token)

---

## Request Parameters

| Parameter | Type     | Required | Description                              |
|-----------|----------|----------|------------------------------------------|
| `fromDate`| String   | Yes      | Start date in ISO format (yyyy-MM-dd)    |
| `toDate`  | String   | Yes      | End date in ISO format (yyyy-MM-dd)      |

### Example Request

```http
GET /usage/statistics?fromDate=2026-01-30&toDate=2026-02-09
Authorization: Bearer <access_token>
```

---

## Response Structure

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Usage statistics fetched successfully",
  "data": {
    "dailyUsage": [
      {
        "date": "2026-01-30",
        "audioMinutes": 45,
        "videoMinutes": 30
      },
      {
        "date": "2026-01-31",
        "audioMinutes": 60,
        "videoMinutes": 25
      },
      {
        "date": "2026-02-01",
        "audioMinutes": 30,
        "videoMinutes": 50
      }
    ]
  }
}
```

### Error Response (400/500)

```json
{
  "success": false,
  "message": "Invalid date range",
  "data": null
}
```

---

## Response Fields

### Root Object

| Field     | Type    | Description                           |
|-----------|---------|---------------------------------------|
| `success` | Boolean | Indicates if request was successful   |
| `message` | String  | Human-readable message                |
| `data`    | Object  | Contains usage statistics data        |

### Data Object

| Field        | Type  | Description                    |
|--------------|-------|--------------------------------|
| `dailyUsage` | Array | Array of daily usage records   |

### DailyUsage Object

| Field          | Type    | Description                                    |
|----------------|---------|------------------------------------------------|
| `date`         | String  | Date in ISO format (yyyy-MM-dd)                |
| `audioMinutes` | Integer | Total minutes spent on audio calls that day    |
| `videoMinutes` | Integer | Total minutes spent on video calls that day    |

---

## Business Rules

1. **Date Range Validation**
   - `fromDate` must be before or equal to `toDate`
   - Maximum date range: 90 days
   - Dates cannot be in the future

2. **Data Completeness**
   - Include all dates in the range, even if no calls occurred
   - Days with no calls should have `audioMinutes: 0` and `videoMinutes: 0`

3. **Call Duration Calculation**
   - Round durations to the nearest minute
   - Include only completed calls (exclude missed/rejected/failed calls)
   - Duration = call end time - call start time

4. **User Context**
   - Return data only for the authenticated user
   - Use session token or JWT to identify the user

5. **Call Type Classification**
   - `AUDIO`: Voice-only calls
   - `VIDEO`: Video calls (may include audio)

---

## Error Codes

| Status Code | Error Message              | Description                           |
|-------------|----------------------------|---------------------------------------|
| 400         | Invalid date range         | fromDate is after toDate              |
| 400         | Date range too large       | Range exceeds 90 days                 |
| 400         | Invalid date format        | Date not in yyyy-MM-dd format         |
| 401         | Unauthorized               | Missing or invalid authentication     |
| 500         | Internal server error      | Unexpected server error               |

---

## Example Backend Implementation (Pseudo SQL)

```sql
SELECT 
    DATE(call_start_time) as date,
    SUM(CASE WHEN call_type = 'AUDIO' THEN CEIL(duration_seconds / 60) ELSE 0 END) as audioMinutes,
    SUM(CASE WHEN call_type = 'VIDEO' THEN CEIL(duration_seconds / 60) ELSE 0 END) as videoMinutes
FROM calls
WHERE 
    user_id = :userId 
    AND call_status = 'COMPLETED'
    AND DATE(call_start_time) BETWEEN :fromDate AND :toDate
GROUP BY DATE(call_start_time)
ORDER BY date ASC
```

---

## Testing

### Test Cases

1. **Valid Request**
   - Request with valid date range
   - Expected: 200 OK with data

2. **No Data**
   - Request for dates with no calls
   - Expected: 200 OK with empty arrays (0 minutes)

3. **Invalid Date Range**
   - fromDate > toDate
   - Expected: 400 Bad Request

4. **Unauthorized**
   - Request without auth token
   - Expected: 401 Unauthorized

5. **Large Date Range**
   - Request for > 90 days
   - Expected: 400 Bad Request

---

## Integration Notes

- The Android app will call this endpoint when the user opens the Usage Statistics screen
- Data is cached locally for 5 minutes to reduce API calls
- The app supports three filter presets: 10 days, 30 days, and custom range
- Custom range allows users to select any from/to dates
