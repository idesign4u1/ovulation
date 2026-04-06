# Development Guide

## Setup Instructions

### Prerequisites
- Android Studio Flamingo or newer
- Android SDK 34 (API 34)
- Kotlin 1.9+
- Java 11+
- Gradle 8.0+

### Initial Setup

1. **Clone the repository:**
```bash
git clone https://github.com/idesign4u1/ovulation.git
cd ovulation
```

2. **Create local properties:**
```bash
echo "sdk.dir=/path/to/Android/sdk" > local.properties
```

3. **Open in Android Studio:**
```bash
./gradlew build
```

4. **Run on device/emulator:**
```bash
./gradlew installDebug
```

---

## Project Structure Details

### `/android/src/main/kotlin/`

#### `OvulationHealthApp.kt`
- Application singleton
- Initializes Room database
- Creates notification channels
- Sets up Timber logging

#### `data/model/`
Contains all Room entity classes:
- `FerningAnalysis` - Saliva ferning pattern data
- `VoiceAnalysis` - Voice frequency measurements
- `TemperatureData` - Body temperature readings
- `CardiacData` - Heart rate and HRV
- `OvulationPrediction` - Ovulation predictions
- `ImplantationWindow` - Implantation window data
- `DailyHealthSummary` - Daily summaries
- `AdminReport` - Reports for server sync

#### `data/db/`
Database layer:
- `OvulationDatabase` - Room database definition
- `Daos.kt` - Data Access Objects (CRUD operations)
- `Converters.kt` - Type conversion for enums and dates

#### `ml/`
Machine learning components:
- `FerningAnalyzer.kt` - TensorFlow Lite ferning detection
- `VoiceAnalyzer.kt` - Voice frequency analysis with FFT
- `PredictionEngine.kt` - Multi-modal prediction combining all biomarkers

#### `sensor/`
Sensor data collection:
- `SensorDataCollector.kt` - Manages camera, microphone, and device sensors
- Handles PPG (PhotoPlethysmography) for heart rate
- Estimates temperature from battery/touch screen

#### `service/`
Background services:
- `DailyTestScheduler.kt` - Schedules daily tests
- `DataSyncService.kt` - Manages data synchronization

#### `receiver/`
Broadcast receivers:
- `DailyTestReceiver.kt` - Triggers daily tests via AlarmManager

#### `work/`
WorkManager jobs:
- `DailyHealthTestWorker.kt` - Executes all daily tests
- `DataSyncWorker.kt` - Syncs data with server

#### `network/`
API communication:
- `ApiService.kt` - Retrofit interface for REST API

#### `ui/`
User interface:
- `MainActivity.kt` - Main activity with navigation
- `screen/Screens.kt` - Jetpack Compose screens
- `theme/Theme.kt` - Material 3 theme
- `theme/Typography.kt` - Text styling

---

## Adding New Features

### 1. Adding a New Biomarker

**Step 1:** Create a data model in `data/model/HealthData.kt`:
```kotlin
@Entity(tableName = "new_biomarker")
data class NewBiomarker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val testDate: LocalDateTime,
    val value: Float,
    val notes: String = ""
)
```

**Step 2:** Create a DAO in `data/db/Daos.kt`:
```kotlin
@Dao
interface NewBiomarkerDao {
    @Insert
    suspend fun insert(data: NewBiomarker): Long
    
    @Query("SELECT * FROM new_biomarker WHERE testDate BETWEEN :startDate AND :endDate")
    fun getDataInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<NewBiomarker>>
    
    // ... other query methods
}
```

**Step 3:** Add DAO to `OvulationDatabase.kt`:
```kotlin
abstract fun newBiomarkerDao(): NewBiomarkerDao
```

**Step 4:** Add collection logic to `DailyHealthTestWorker.kt`:
```kotlin
private suspend fun collectNewBiomarker() {
    // Collection logic
}
```

**Step 5:** Integrate into `PredictionEngine.kt`:
```kotlin
private fun calculateNewBiomarkerScore(data: List<NewBiomarker>): Float {
    // Scoring logic
}
```

### 2. Adding a New Prediction Model

Extend `PredictionEngine.predictOvulation()`:
```kotlin
val newScore = calculateNewBiomarkerScore(newBiomarkerData)

val compositeScore = (
    ferningScore * 0.35f +
    voiceScore * 0.25f +
    temperatureScore * 0.25f +
    cardiacScore * 0.10f +
    newScore * 0.05f  // Add new component
)
```

### 3. Adding a New UI Screen

Create in `ui/screen/Screens.kt`:
```kotlin
@Composable
fun NewScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Screen content
    }
}
```

Add navigation in `MainActivity.kt`:
```kotlin
composable("new_screen") {
    NewScreen(navController)
}
```

---

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Camera permission works
- [ ] Microphone permission works
- [ ] Sensor data collected
- [ ] Database saves correctly
- [ ] Predictions calculate
- [ ] Data syncs to server
- [ ] UI renders properly
- [ ] Navigation works

---

## Building for Release

### 1. Update version
Edit `build.gradle`:
```gradle
versionCode 2
versionName "1.1.0"
```

### 2. Update changelog
Add entry to `CHANGELOG.md`

### 3. Generate signed APK
```bash
./gradlew bundleRelease
```

### 4. Create GitHub release
```bash
gh release create v1.1.0 --draft
```

---

## Debugging

### Enable Timber Logging
```kotlin
// Already configured in OvulationHealthApp.kt
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### Database Debugging
```bash
# Access database via adb
adb shell
cd /data/data/com.ovulation.health/databases/
sqlite3 ovulation_health.db
sqlite> SELECT * FROM ferning_analysis;
```

### Network Debugging
Enable network inspection in `build.gradle`:
```gradle
debugImplementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
```

Add interceptor to Retrofit:
```kotlin
.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
```

---

## Performance Optimization

### 1. ML Model Optimization
- Use model quantization (int8) to reduce size
- Cache interpreter instance
- Use GPU delegate for faster inference

### 2. Database Optimization
- Add indexes on frequently queried columns
- Use pagination for large queries
- Archive old data periodically

### 3. Memory Management
- Release resources in `onDestroy()`
- Use weak references for long-lived listeners
- Monitor memory with Android Profiler

### 4. Battery Optimization
- Use WorkManager instead of Services
- Schedule background tasks during idle
- Use inexact alarms when possible

---

## Common Issues and Solutions

### Issue: TensorFlow Lite Model Not Found
**Solution:** Ensure model file is in `android/src/main/assets/models/`

### Issue: Permission Denied
**Solution:** Check `AndroidManifest.xml` and grant runtime permissions

### Issue: Data Not Syncing
**Solution:** Check network connectivity and API server status

### Issue: Tests Not Running
**Solution:** Verify AlarmManager permissions and device battery optimization

---

## Code Style Guide

### Naming Conventions
- Classes: PascalCase
- Functions: camelCase
- Constants: UPPER_SNAKE_CASE
- Private members: prefix with underscore (_)

### Kotlin Best Practices
```kotlin
// ✅ Good
val user = getUserData()
suspend fun collectData() { }
data class DataModel(val id: Int, val name: String)

// ❌ Bad
var userData = getUserData()
fun collectData() { }
class DataModel { var id: Int; var name: String }
```

### Comments
```kotlin
// Single line for brief explanations

/**
 * Multi-line documentation for functions.
 * Use KDoc format for IDE autocompletion.
 */
```

---

## Continuous Integration

### GitHub Actions
Create `.github/workflows/android-build.yml`:
```yaml
name: Android Build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
      - run: ./gradlew build
```

---

## Deployment

### Play Store Deployment
1. Build signed bundle
2. Test in internal testing track
3. Promote to beta
4. Promote to production

### Server Setup
1. Deploy REST API backend
2. Configure database
3. Set up webhooks
4. Enable monitoring

---

## Resources

### Official Documentation
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [TensorFlow Lite](https://www.tensorflow.org/lite/guide/android)
- [Retrofit](https://square.github.io/retrofit/)

### Scientific References
- Ferning patterns: Plant et al. (2024)
- Voice frequency: Wennig et al. (2022)
- BBT monitoring: WHO guidelines (2023)
- HRV analysis: Task Force Report (2015)

---

## Support

For development questions, check:
- GitHub Issues
- Android Developers Forum
- Stack Overflow [android] tag

---

**Last Updated:** April 6, 2026  
**Maintained by:** Project Team
