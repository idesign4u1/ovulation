# Ovulation Health API Specification

## API Base URL
```
https://health-api.example.com/api/v1
```

## Authentication
- Token-based authentication (Bearer)
- Each request must include: `Authorization: Bearer {token}`

## Endpoints

### 1. Health Metrics Upload
**POST** `/health-metrics`

Upload daily health report and predictions.

**Request:**
```json
{
  "userId": "user123",
  "reportDate": "2026-04-06T08:00:00",
  "startDate": "2026-03-30T00:00:00",
  "endDate": "2026-04-06T23:59:59",
  "reportData": {
    "prediction_date": "2026-04-06T08:15:00",
    "estimated_ovulation": "2026-04-09T00:00:00",
    "confidence": 0.92,
    "ferning_score": 0.95,
    "voice_score": 0.82,
    "temperature_score": 0.88,
    "cardiac_score": 0.75,
    "status": "PREDICTED"
  },
  "graphData": {
    "temperature_chart": [...],
    "heart_rate_chart": [...],
    "ferning_progression": [...],
    "voice_frequency_chart": [...]
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Health metrics uploaded successfully",
  "dataId": "report_20260406_001"
}
```

**HTTP Status:**
- 200: Success
- 400: Invalid request format
- 401: Unauthorized
- 500: Server error

---

### 2. Ferning Analysis Upload
**POST** `/ferning-analysis` (Multipart)

Upload ferning image and analysis data.

**Form Data:**
- `image`: Binary image file (PNG/JPEG)
- `data`: JSON analysis metadata

**Data JSON:**
```json
{
  "testDate": "2026-04-06T08:00:00",
  "ferningPattern": "FULL",
  "confidence": 0.97,
  "notes": "Clear ferning pattern indicating high estrogen levels"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Ferning analysis uploaded successfully",
  "dataId": "ferning_20260406_001"
}
```

---

### 3. Voice Analysis Upload
**POST** `/voice-analysis` (Multipart)

Upload voice recording and analysis data.

**Form Data:**
- `audio`: Binary audio file (MP3/M4A)
- `metadata`: JSON analysis data

**Metadata JSON:**
```json
{
  "testDate": "2026-04-06T08:10:00",
  "fundamentalFrequency": 215.3,
  "frequencyShift": 5.2,
  "shimmer": 2.5,
  "jitter": 0.8,
  "notes": "Frequency shift detected indicating approaching ovulation"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Voice analysis uploaded successfully",
  "dataId": "voice_20260406_001"
}
```

---

### 4. Get Ovulation Predictions
**GET** `/predictions/ovulation`

Retrieve ovulation prediction history.

**Query Parameters:**
- `user_id`: User identifier (required)
- `start_date`: ISO 8601 timestamp (required)
- `end_date`: ISO 8601 timestamp (required)

**Request Example:**
```
GET /predictions/ovulation?user_id=user123&start_date=2026-03-01T00:00:00&end_date=2026-04-06T23:59:59
```

**Response:**
```json
{
  "userId": "user123",
  "predictions": [
    {
      "date": "2026-04-06T08:15:00",
      "estimatedOvulation": "2026-04-09T00:00:00",
      "confidence": 0.92,
      "status": "PREDICTED"
    },
    {
      "date": "2026-03-10T08:15:00",
      "estimatedOvulation": "2026-03-13T00:00:00",
      "confidence": 0.89,
      "status": "CONFIRMED"
    }
  ]
}
```

---

### 5. Get Implantation Windows
**GET** `/predictions/implantation-window`

Retrieve implantation window predictions.

**Query Parameters:**
- `user_id`: User identifier (required)
- `start_date`: ISO 8601 timestamp (required)
- `end_date`: ISO 8601 timestamp (required)

**Response:**
```json
{
  "userId": "user123",
  "windows": [
    {
      "startDate": "2026-04-14T00:00:00",
      "endDate": "2026-04-18T23:59:59",
      "confidence": 0.88,
      "status": "PREDICTED"
    }
  ]
}
```

---

### 6. Send Alert
**POST** `/alerts`

Send notification alert to admin system.

**Request:**
```json
{
  "userId": "user123",
  "alertType": "OVULATION_PREDICTED",
  "severity": "WARNING",
  "message": "Ovulation predicted with 92% confidence",
  "data": {
    "ovulation_date": "2026-04-09T00:00:00",
    "confidence": 0.92,
    "ferning_score": 0.95,
    "voice_score": 0.82
  }
}
```

**Valid Alert Types:**
- `OVULATION_PREDICTED`: Ovulation is predicted
- `OVULATION_CONFIRMED`: Ovulation confirmed by multiple indicators
- `IMPLANTATION_WINDOW`: Implantation window is active
- `ANOMALY_DETECTED`: Unusual pattern detected
- `DATA_SYNC_SUCCESS`: Data successfully synced
- `DATA_SYNC_FAILED`: Data sync failed
- `TEST_OVERDUE`: Daily test is overdue

**Severity Levels:**
- `INFO`: Informational
- `WARNING`: Warning (needs attention)
- `CRITICAL`: Critical (immediate action needed)

**Response:**
```json
{
  "success": true,
  "alertId": "alert_20260406_001",
  "message": "Alert sent to administrators"
}
```

---

### 7. Get User Settings
**GET** `/users/{user_id}/settings`

Retrieve user configuration settings.

**Response:**
```json
{
  "userId": "user123",
  "testTime": "08:00",
  "cycleLength": 28,
  "notificationsEnabled": true,
  "dataRetentionDays": 365,
  "adminEmail": "admin@example.com",
  "timezone": "America/New_York"
}
```

---

### 8. Update User Settings
**PUT** `/users/{user_id}/settings`

Update user configuration settings.

**Request:**
```json
{
  "testTime": "09:00",
  "cycleLength": 30,
  "notificationsEnabled": true,
  "timezone": "America/Los_Angeles"
}
```

**Response:**
```json
{
  "userId": "user123",
  "testTime": "09:00",
  "cycleLength": 30,
  "notificationsEnabled": true,
  "dataRetentionDays": 365,
  "adminEmail": "admin@example.com",
  "timezone": "America/Los_Angeles"
}
```

---

### 9. Health Check
**GET** `/health`

Server health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "timestamp": "2026-04-06T15:30:00Z",
  "version": "1.0.0"
}
```

**HTTP Status:**
- 200: Server is healthy
- 503: Service unavailable

---

## Error Responses

All error responses follow this format:

```json
{
  "success": false,
  "error": "error_code",
  "message": "Human readable error message",
  "details": "Additional error information"
}
```

### Common Error Codes

| Code | HTTP | Message |
|------|------|---------|
| `INVALID_REQUEST` | 400 | Invalid request format |
| `UNAUTHORIZED` | 401 | Authentication failed |
| `FORBIDDEN` | 403 | User does not have permission |
| `NOT_FOUND` | 404 | Resource not found |
| `VALIDATION_ERROR` | 422 | Request validation failed |
| `INTERNAL_ERROR` | 500 | Internal server error |
| `SERVICE_UNAVAILABLE` | 503 | Service temporarily unavailable |

---

## Rate Limiting

- **Limit**: 100 requests per minute per API key
- **Headers**:
  - `X-RateLimit-Limit`: 100
  - `X-RateLimit-Remaining`: Remaining requests
  - `X-RateLimit-Reset`: Unix timestamp when limit resets

---

## Data Formats

### Date/Time Format
ISO 8601: `YYYY-MM-DDTHH:mm:ssZ` or `YYYY-MM-DDTHH:mm:ss±HH:mm`

### Confidence Scores
Float between 0 and 1 (0.0 = 0%, 1.0 = 100%)

### Ferning Patterns
- `NONE`: No ferning
- `PARTIAL`: Partial ferning pattern
- `FULL`: Full ferning pattern

### Prediction Status
- `PREDICTED`: Prediction made based on indicators
- `CONFIRMED`: Prediction confirmed by multiple high-confidence indicators
- `PASSED`: The predicted window has passed

---

## Webhooks (Optional)

### Admin Notification Webhook
When important events occur, the system can POST to configured webhooks:

```json
{
  "event": "ovulation_detected",
  "userId": "user123",
  "timestamp": "2026-04-06T08:15:00Z",
  "data": {
    "ovulation_date": "2026-04-09T00:00:00",
    "confidence": 0.92
  }
}
```

---

## Example Implementation

### Uploading Health Metrics (curl)
```bash
curl -X POST https://health-api.example.com/api/v1/health-metrics \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "reportDate": "2026-04-06T08:00:00",
    "reportData": {...}
  }'
```

### Uploading Ferning Image (curl)
```bash
curl -X POST https://health-api.example.com/api/v1/ferning-analysis \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "image=@path/to/image.jpg" \
  -F "data={...}"
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-04-06 | Initial API specification |

---

## Support

For API issues or questions, contact: api-support@example.com
