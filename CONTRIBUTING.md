# Contributing to Ovulation Health App

Thank you for your interest in contributing to the Ovulation Health App! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

Please be respectful and professional in all interactions. We are committed to providing a welcoming and inclusive environment.

## How to Contribute

### Reporting Bugs

1. **Check existing issues** to avoid duplicates
2. **Describe the bug** with clear, concise language
3. **Provide steps to reproduce** the issue
4. **Include system information**:
   - Android version
   - Device model
   - App version
   - LogCat output (if applicable)

### Feature Requests

1. **Search existing issues** first
2. **Describe the feature** and its use case
3. **Explain the expected behavior**
4. **Provide any relevant mockups** or examples

### Pull Requests

#### Before You Start
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make sure you understand the codebase
4. Check existing PRs to avoid duplicates

#### Development Process

1. **Write clean code**:
   - Follow Kotlin style guide
   - Use meaningful variable names
   - Add comments for complex logic
   - Maximum line length: 120 characters

2. **Test your changes**:
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew connectedAndroidTest
   ```

3. **Update documentation**:
   - Add code comments
   - Update README if needed
   - Update API docs if needed
   - Add entry to CHANGELOG.md

4. **Commit with clear messages**:
   ```
   feat: Add new biomarker collection module
   
   - Implement sensor data collection
   - Add DAO for persistence
   - Integrate into prediction engine
   - Add unit tests
   
   Fixes #123
   ```

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**:
   - Provide clear title
   - Link related issues
   - Describe changes
   - Include testing details

#### PR Review Process

- At least 1 approval required
- All CI checks must pass
- Code must follow style guidelines
- Tests must have adequate coverage
- Documentation must be updated

---

## Project Guidelines

### Architecture

Maintain the existing architecture:
```
data/        - Database and models
ml/          - Machine learning components
sensor/      - Hardware sensor access
service/     - Background services
network/     - API communication
ui/          - User interface
```

### Code Style

#### Naming
```kotlin
// Classes: PascalCase
class OvulationAnalyzer { }

// Functions: camelCase
fun analyzeData() { }

// Constants: UPPER_SNAKE_CASE
const val MAX_RETRIES = 3
```

#### Formatting
```kotlin
// Use proper indentation (4 spaces)
// Maximum line length: 120 characters
// Prefer val over var
// Use meaningful names

val result = database.query()  // ✅ Good
var x = db.q()                 // ❌ Bad
```

#### Documentation
```kotlin
/**
 * Analyzes ferning pattern from provided image.
 * 
 * @param imageUri URI to the captured image
 * @return Ferning analysis result with confidence score
 */
suspend fun analyzeFerningImage(imageUri: Uri): FerningAnalysis {
    // Implementation
}
```

### Testing Requirements

- Unit tests for business logic
- Integration tests for database operations
- Mock external dependencies
- Aim for >80% code coverage
- Test error cases

Example:
```kotlin
@Test
fun `calculateCompositeScore returns correct weighted average`() {
    val score = predictionEngine.calculateCompositeScore(
        ferning = 0.95f,
        voice = 0.82f,
        temperature = 0.88f,
        cardiac = 0.75f
    )
    
    val expected = (0.95f * 0.35f) + (0.82f * 0.25f) + (0.88f * 0.25f) + (0.75f * 0.15f)
    assertEquals(expected, score, 0.01f)
}
```

### Database Changes

When modifying database schema:

1. Create a migration file
2. Update entity classes
3. Update DAOs
4. Test migration from previous version
5. Document breaking changes

### ML Model Updates

When updating ML models:

1. Document accuracy metrics
2. Include model size/latency info
3. Provide conversion scripts
4. Test on device performance
5. Include model in assets

---

## Commit Message Format

Use conventional commits:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `test`: Test addition/modification
- `chore`: Build/dependency changes

### Example
```
feat(ml): Implement LSTM-based ferning pattern prediction

Add LSTM model for improved ferning pattern recognition.
Replace CNN with LSTM for temporal analysis.
Improved accuracy to 98.5%.

Fixes #456
```

---

## Documentation

### Code Comments
```kotlin
// Use for single-line explanations
// Keep comments concise and meaningful

/**
 * Multi-line for complex functions.
 * Explain the "why", not just the "what".
 */
fun complexFunction() { }
```

### README Updates
Update README.md if you:
- Add new features
- Change API
- Modify setup process
- Update dependencies

### API Documentation
Update API_SPECIFICATION.md when:
- Adding endpoints
- Modifying responses
- Changing authentication
- Adding new data fields

---

## Branch Naming

```
feature/add-new-biomarker       # New feature
fix/database-sync-issue         # Bug fix
docs/update-api-spec            # Documentation
refactor/ml-engine              # Refactoring
perf/optimize-database-queries  # Performance
```

---

## Release Process

1. **Version Bump** (MAJOR.MINOR.PATCH):
   ```gradle
   versionCode 2
   versionName "1.1.0"
   ```

2. **Update CHANGELOG.md**
3. **Create Git Tag**:
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

4. **Build Release APK/Bundle**:
   ```bash
   ./gradlew bundleRelease
   ```

5. **Create GitHub Release**
6. **Deploy to Play Store**

---

## Getting Help

- **Questions**: Open a discussion
- **Bugs**: File an issue with reproduction steps
- **Features**: Open a feature request issue
- **Security**: Email security@example.com (don't open public issue)

---

## Legal

By contributing, you agree that:
- Your contributions will be licensed under the project's license
- You have the right to grant these rights
- Your contributions don't violate any third-party rights

---

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md
- Release notes
- GitHub insights

---

## Questions?

Don't hesitate to ask! The best way to learn is to ask questions.

---

**Last Updated:** April 6, 2026  
**Maintained by:** Project Team
