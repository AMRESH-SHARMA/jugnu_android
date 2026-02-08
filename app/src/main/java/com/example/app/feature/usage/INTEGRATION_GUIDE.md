# Usage Statistics - Backend Integration Guide

## Quick Start

This guide helps backend developers integrate the Usage Statistics API with the Android app.

## API Endpoint Summary

```
GET /usage/statistics?fromDate=2026-01-30&toDate=2026-02-09
Authorization: Bearer <token>
```

**Response:**
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
      }
    ]
  }
}
```

See [API_SPECIFICATION.md](./API_SPECIFICATION.md) for complete details.

---

## Backend Implementation Checklist

### 1. Database Schema

Ensure your calls table has these fields:

```sql
CREATE TABLE calls (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    call_type VARCHAR(10) NOT NULL,  -- 'AUDIO' or 'VIDEO'
    call_status VARCHAR(20) NOT NULL, -- 'COMPLETED', 'MISSED', 'REJECTED', etc.
    call_start_time TIMESTAMP NOT NULL,
    call_end_time TIMESTAMP,
    duration_seconds INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_date (user_id, call_start_time),
    INDEX idx_status (call_status)
);
```

### 2. Query Logic

**SQL Example (PostgreSQL):**

```sql
SELECT 
    DATE(call_start_time) as date,
    SUM(CASE 
        WHEN call_type = 'AUDIO' 
        THEN CEIL(duration_seconds::DECIMAL / 60) 
        ELSE 0 
    END) as audio_minutes,
    SUM(CASE 
        WHEN call_type = 'VIDEO' 
        THEN CEIL(duration_seconds::DECIMAL / 60) 
        ELSE 0 
    END) as video_minutes
FROM calls
WHERE 
    user_id = $1
    AND call_status = 'COMPLETED'
    AND DATE(call_start_time) BETWEEN $2 AND $3
GROUP BY DATE(call_start_time)
ORDER BY date ASC;
```

**SQL Example (MySQL):**

```sql
SELECT 
    DATE(call_start_time) as date,
    SUM(CASE 
        WHEN call_type = 'AUDIO' 
        THEN CEIL(duration_seconds / 60) 
        ELSE 0 
    END) as audio_minutes,
    SUM(CASE 
        WHEN call_type = 'VIDEO' 
        THEN CEIL(duration_seconds / 60) 
        ELSE 0 
    END) as video_minutes
FROM calls
WHERE 
    user_id = ?
    AND call_status = 'COMPLETED'
    AND DATE(call_start_time) BETWEEN ? AND ?
GROUP BY DATE(call_start_time)
ORDER BY date ASC;
```

### 3. Fill Missing Dates

**Important:** The app expects data for ALL dates in the range, even if there are no calls.

**Example Logic (Pseudo-code):**

```javascript
function fillMissingDates(data, fromDate, toDate) {
    const result = [];
    const current = new Date(fromDate);
    const end = new Date(toDate);
    
    while (current <= end) {
        const dateStr = current.toISOString().split('T')[0];
        const existing = data.find(d => d.date === dateStr);
        
        result.push({
            date: dateStr,
            audioMinutes: existing?.audioMinutes || 0,
            videoMinutes: existing?.videoMinutes || 0
        });
        
        current.setDate(current.getDate() + 1);
    }
    
    return result;
}
```

### 4. Validation

```javascript
function validateRequest(fromDate, toDate) {
    // Check date format
    if (!isValidDate(fromDate) || !isValidDate(toDate)) {
        throw new Error("Invalid date format. Use yyyy-MM-dd");
    }
    
    // Check date order
    if (new Date(fromDate) > new Date(toDate)) {
        throw new Error("fromDate must be before or equal to toDate");
    }
    
    // Check date range
    const daysDiff = Math.ceil(
        (new Date(toDate) - new Date(fromDate)) / (1000 * 60 * 60 * 24)
    );
    
    if (daysDiff > 90) {
        throw new Error("Date range cannot exceed 90 days");
    }
    
    // Check future dates
    if (new Date(toDate) > new Date()) {
        throw new Error("Cannot query future dates");
    }
}
```

### 5. Authentication

Extract user ID from the authentication token:

```javascript
function getUserIdFromToken(req) {
    const token = req.headers.authorization?.replace('Bearer ', '');
    
    if (!token) {
        throw new UnauthorizedError("Missing authentication token");
    }
    
    const decoded = jwt.verify(token, SECRET_KEY);
    return decoded.userId;
}
```

---

## Example Implementations

### Node.js + Express

```javascript
const express = require('express');
const router = express.Router();

router.get('/usage/statistics', authenticateToken, async (req, res) => {
    try {
        const { fromDate, toDate } = req.query;
        const userId = req.user.id;
        
        // Validate
        validateRequest(fromDate, toDate);
        
        // Query database
        const rawData = await db.query(`
            SELECT 
                DATE(call_start_time) as date,
                SUM(CASE WHEN call_type = 'AUDIO' THEN CEIL(duration_seconds / 60) ELSE 0 END) as audioMinutes,
                SUM(CASE WHEN call_type = 'VIDEO' THEN CEIL(duration_seconds / 60) ELSE 0 END) as videoMinutes
            FROM calls
            WHERE user_id = $1 AND call_status = 'COMPLETED'
                AND DATE(call_start_time) BETWEEN $2 AND $3
            GROUP BY DATE(call_start_time)
            ORDER BY date ASC
        `, [userId, fromDate, toDate]);
        
        // Fill missing dates
        const dailyUsage = fillMissingDates(rawData.rows, fromDate, toDate);
        
        res.json({
            success: true,
            message: "Usage statistics fetched successfully",
            data: { dailyUsage }
        });
        
    } catch (error) {
        res.status(400).json({
            success: false,
            message: error.message,
            data: null
        });
    }
});

module.exports = router;
```

### Python + Flask

```python
from flask import Blueprint, request, jsonify
from datetime import datetime, timedelta
from functools import wraps

usage_bp = Blueprint('usage', __name__)

@usage_bp.route('/usage/statistics', methods=['GET'])
@require_auth
def get_usage_statistics():
    try:
        from_date = request.args.get('fromDate')
        to_date = request.args.get('toDate')
        user_id = g.user_id
        
        # Validate
        validate_request(from_date, to_date)
        
        # Query database
        query = """
            SELECT 
                DATE(call_start_time) as date,
                SUM(CASE WHEN call_type = 'AUDIO' THEN CEIL(duration_seconds / 60.0) ELSE 0 END) as audio_minutes,
                SUM(CASE WHEN call_type = 'VIDEO' THEN CEIL(duration_seconds / 60.0) ELSE 0 END) as video_minutes
            FROM calls
            WHERE user_id = %s AND call_status = 'COMPLETED'
                AND DATE(call_start_time) BETWEEN %s AND %s
            GROUP BY DATE(call_start_time)
            ORDER BY date ASC
        """
        
        raw_data = db.execute(query, (user_id, from_date, to_date))
        
        # Fill missing dates
        daily_usage = fill_missing_dates(raw_data, from_date, to_date)
        
        return jsonify({
            'success': True,
            'message': 'Usage statistics fetched successfully',
            'data': {'dailyUsage': daily_usage}
        })
        
    except ValueError as e:
        return jsonify({
            'success': False,
            'message': str(e),
            'data': None
        }), 400
```

### Java + Spring Boot

```java
@RestController
@RequestMapping("/usage")
public class UsageController {
    
    @Autowired
    private UsageService usageService;
    
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse<UsageStatisticsDto>> getUsageStatistics(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            Long userId = ((CustomUserDetails) userDetails).getUserId();
            
            // Validate
            validateDateRange(fromDate, toDate);
            
            // Get data
            List<DailyUsageDto> dailyUsage = usageService.getUsageStatistics(
                userId, 
                LocalDate.parse(fromDate), 
                LocalDate.parse(toDate)
            );
            
            UsageStatisticsDto data = new UsageStatisticsDto(dailyUsage);
            
            return ResponseEntity.ok(new BaseResponse<>(
                true,
                "Usage statistics fetched successfully",
                data
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new BaseResponse<>(
                false,
                e.getMessage(),
                null
            ));
        }
    }
}
```

---

## Testing

### Test with cURL

```bash
# Replace with your actual token and dates
curl -X GET "http://localhost:8080/usage/statistics?fromDate=2026-01-30&toDate=2026-02-09" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

### Expected Response

```json
{
  "success": true,
  "message": "Usage statistics fetched successfully",
  "data": {
    "dailyUsage": [
      {"date": "2026-01-30", "audioMinutes": 45, "videoMinutes": 30},
      {"date": "2026-01-31", "audioMinutes": 0, "videoMinutes": 0},
      {"date": "2026-02-01", "audioMinutes": 60, "videoMinutes": 25},
      {"date": "2026-02-02", "audioMinutes": 30, "videoMinutes": 50},
      {"date": "2026-02-03", "audioMinutes": 0, "videoMinutes": 0},
      {"date": "2026-02-04", "audioMinutes": 75, "videoMinutes": 40},
      {"date": "2026-02-05", "audioMinutes": 20, "videoMinutes": 15},
      {"date": "2026-02-06", "audioMinutes": 90, "videoMinutes": 60},
      {"date": "2026-02-07", "audioMinutes": 55, "videoMinutes": 35},
      {"date": "2026-02-08", "audioMinutes": 40, "videoMinutes": 45},
      {"date": "2026-02-09", "audioMinutes": 65, "videoMinutes": 55}
    ]
  }
}
```

### Test Cases

1. **Valid Request**
```bash
GET /usage/statistics?fromDate=2026-02-01&toDate=2026-02-10
Expected: 200 OK with 10 days of data
```

2. **No Calls**
```bash
GET /usage/statistics?fromDate=2025-01-01&toDate=2025-01-10
Expected: 200 OK with all zeros
```

3. **Invalid Date Order**
```bash
GET /usage/statistics?fromDate=2026-02-10&toDate=2026-02-01
Expected: 400 Bad Request
```

4. **Missing Auth**
```bash
GET /usage/statistics?fromDate=2026-02-01&toDate=2026-02-10
(without Authorization header)
Expected: 401 Unauthorized
```

5. **Large Range**
```bash
GET /usage/statistics?fromDate=2025-01-01&toDate=2026-12-31
Expected: 400 Bad Request (exceeds 90 days)
```

---

## Performance Tips

1. **Add Database Index**
```sql
CREATE INDEX idx_calls_user_date_status 
ON calls(user_id, call_start_time, call_status);
```

2. **Cache Results**
- Cache results for 5 minutes per user
- Invalidate cache when new call is completed

3. **Limit Query Scope**
- Only query completed calls
- Consider archiving old data

4. **Optimize Date Filling**
- Generate date range in database if possible
- Use efficient date iteration

---

## Common Issues

### Issue 1: Missing Dates in Response
**Problem:** App shows gaps in chart  
**Solution:** Ensure you fill all dates between fromDate and toDate with 0 values

### Issue 2: Wrong Duration Calculation
**Problem:** Minutes don't match actual call duration  
**Solution:** Use CEIL() to round up partial minutes

### Issue 3: Performance Issues
**Problem:** Query takes too long  
**Solution:** Add proper indexes, limit date range to 90 days

### Issue 4: Timezone Issues
**Problem:** Dates don't match user's timezone  
**Solution:** Store timestamps in UTC, convert to user's timezone for grouping

---

## Contact

If you have questions about the API integration, please contact the Android team or refer to:
- [API_SPECIFICATION.md](./API_SPECIFICATION.md) - Complete API docs
- [README.md](./README.md) - Feature overview
