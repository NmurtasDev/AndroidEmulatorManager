# Android Emulator Manager

A Java Swing application to manage Android emulators with automatic SDK download and setup capabilities.

## Features

- Automatic Android SDK download and installation
- Create, start, stop and delete Android Virtual Devices (AVDs)
- Multi-language support (English and Italian)
- User-friendly GUI with progress tracking
- Configuration persistence

## Project Structure

The application has been refactored into smaller, manageable classes:

- `AndroidEmulatorManager` - Main GUI class
- `LocalizationManager` - Handles internationalization
- `ConfigurationManager` - Manages application configuration
- `SdkManager` - Handles SDK download and installation
- `EmulatorManager` - Manages AVD operations
- `CommandExecutor` - Utility for executing system commands

## Building

This is a Maven project. To build:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory that includes all resources (including .properties files).

## Testing

The project includes comprehensive unit tests using JUnit 5 and Mockito. To run tests:

```bash
mvn test
```

### Test Coverage

- **60 total tests** covering all main classes
- **LocalizationManager** - 9 tests (message loading, formatting, error handling)
- **ConfigurationManager** - 10 tests (SDK path management, persistence)  
- **SdkManager** - 12 tests (path validation, setup operations)
- **EmulatorManager** - 15 tests (AVD operations, process management)
- **CommandExecutor** - 14 tests (command execution, OS compatibility)

All tests are designed to be cross-platform compatible and handle edge cases gracefully.

## Running

```bash
java -jar target/android-emulator-manager.jar
```

## Requirements

- Java 11 or higher
- Internet connection for SDK download (optional, if SDK already installed)

### Java Version Compatibility

This application is **compiled with Java 11** and uses Android SDK Command Line Tools (version 10406996) that are compatible with Java 11. This ensures maximum compatibility across different Java versions.

**Supported Android API Levels:**
- Android API 30, 31, 32, 33, 34 (with system images and build tools)
- Default API level: 33

**Important:** The application automatically sets `SKIP_JDK_VERSION_CHECK=true` to bypass SDK version checks when running with Java 11.

## Localization

The application supports Italian and English languages. Locale is automatically detected from the system settings.