# Android Emulator Manager

[![Java CI with Maven](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/maven.yml/badge.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/maven.yml)
[![Code Quality](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/code-quality.yml/badge.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/code-quality.yml)
[![codecov](https://codecov.io/gh/NmurtasDev/AndroidEmulatorManager/branch/main/graph/badge.svg)](https://codecov.io/gh/NmurtasDev/AndroidEmulatorManager)
[![CodeQL](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/codeql.yml/badge.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager)
[![Version](https://img.shields.io/badge/version-3.0.0--beta-orange.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/releases)

A modern **Java-based GUI tool** to download, install, and manage Android SDK and emulators.

## Features

- **Automated SDK Setup**: Download and install Android SDK automatically with license agreement
- **Card-Based Device UI**: Modern card interface with pagination (10 devices per page)
- **Smart Accordions**: Auto-collapsing SDK configuration and expandable logs
- **Cross-Platform**: Works on Windows, Linux, and macOS
- **Emulator Management**: Create, start, stop, rename, and delete Android Virtual Devices (AVDs)
- **Device Information**: Display Android version, device type, and running status
- **AVD Name Validation**: Prevent invalid characters and spaces in AVD names
- **Dark Theme Support**: Automatically adapts to system theme (Gnome, KDE, Windows)
- **Modern Architecture**: Built with Java 21 and Maven
- **Automated Releases**: GitHub Actions pipeline for multi-platform builds
- **Logging**: Comprehensive logging with SLF4J and Logback

## Requirements

- **Java 21** or higher
- **Maven 3.8+** (for building from source)
- Internet connection (for SDK download)

## Quick Start

### Download Pre-built JAR

Download the latest release from the [Releases](https://github.com/NmurtasDev/AndroidEmulatorManager/releases) page and run:

```bash
java -jar android-emulator-manager-3.0.0.jar
```

### Build from Source

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
mvn clean package
java -jar target/android-emulator-manager-3.0.0-jar-with-dependencies.jar
```
### Build with EXE output (require java)

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
mvn clean package -Pwindows
```
### Build with EXE output (dont require java)

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
jlink --output jre --add-modules java.base,java.desktop --strip-debug --compress=2 --no-header-files --no-man-pages
mvn clean package -Pwindows-standalone
```

### Build macOS App Bundle

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
mvn clean package -Pmacos
# Output: target/AndroidEmulatorManager.app/
```

### Build Linux App Bundle

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
mvn clean package -Plinux
# Output: target/AndroidEmulatorManager/ (executable: bin/AndroidEmulatorManager)
```

### Build Linux DEB Package

```bash
git clone https://github.com/NmurtasDev/AndroidEmulatorManager.git
cd AndroidEmulatorManager
mvn clean package -Plinux-deb
# Output: target/android-emulator-manager_3.0.0_amd64.deb
```

## Platform-Specific Distributions

| Platform | Profile | Output | Includes JRE |
|----------|---------|--------|--------------|
| **Windows** | `windows` | `.exe` (requires Java) | ❌ |
| **Windows** | `windows-standalone` | `.exe` (standalone) | ✅ |
| **macOS** | `macos` | `.app` bundle | ✅ |
| **Linux** | `linux` | app-image | ✅ |
| **Linux** | `linux-deb` | `.deb` package | ✅ |
| **All** | default | `.jar` (universal) | ❌ |

## Usage

1. **Launch the application**
2. **SDK Configuration** (if not already configured):
   - Expand the SDK Configuration accordion
   - Click "Download SDK" and accept the Android SDK License Agreement
   - Wait for automatic download and installation
3. **Create AVDs**:
   - Click "Create New AVD"
   - Choose API level and device type
   - Enter a valid name (letters, numbers, underscores, hyphens only)
4. **Manage Devices**:
   - View devices as cards showing Android version, device type, and status
   - Use ▶ to start, ■ to stop, ✎ to rename, 🗑 to delete
   - Navigate pages if you have more than 10 devices
5. **Monitor Activity**:
   - Click the Log accordion to view detailed operation logs
   - Use "Clear" button to reset the log

## SDK Installation Paths

- **Default**: `$HOME/Android/sdk` (user home directory)
- **Custom**: Use the "Sfoglia" button to select a different location

The tool follows [Google's official Android SDK documentation](https://developer.android.com/studio/command-line).

## Project Structure

```
AndroidEmulatorManager/
├── src/
│   ├── main/
│   │   ├── java/net/nicolamurtas/android/emulator/
│   │   │   ├── AndroidEmulatorManager.java    # Main application
│   │   │   ├── service/                       # Business logic
│   │   │   ├── ui/                            # Swing UI components
│   │   │   └── util/                          # Utilities
│   │   └── resources/
│   │       └── logback.xml                    # Logging configuration
│   └── test/
│       └── java/                              # Unit tests
├── pom.xml                                    # Maven configuration
└── README.md
```

## Architecture Improvements (v3.0)

This version introduces major architectural improvements over previous versions:

### Technical Improvements
- ✅ **Proper separation of concerns** (UI, Service, Util layers)
- ✅ **Modern Java 21** features (text blocks, records, switch expressions)
- ✅ **Maven build system** for dependency management
- ✅ **Professional logging** with SLF4J/Logback
- ✅ **Security improvements** (no command injection vulnerabilities)
- ✅ **Better error handling** and user feedback
- ✅ **Unit tests** for critical functionality
- ✅ **Cross-platform compatibility** tested on Windows, Linux (Gnome/KDE), macOS

### UI/UX Improvements
- ✅ **Card-based device interface** with pagination
- ✅ **Smart accordions** for SDK and logs (auto-collapse/expand)
- ✅ **Android SDK License Agreement** dialog before download
- ✅ **Real-time device info** (Android version, device type, running status)
- ✅ **AVD name validation** to prevent errors
- ✅ **Dark theme support** with automatic system theme detection
- ✅ **Rename AVD functionality** directly from UI

### CI/CD & Quality
- ✅ **Automated releases** via GitHub Actions on tag push
- ✅ **Multi-platform builds** (JAR, Windows EXE)
- ✅ **Pre-release support** for beta/RC versions
- ✅ **CodeQL security scanning** on every commit
- ✅ **Automated code coverage** with JaCoCo and Codecov
- ✅ **Code quality checks** on every PR
- ✅ **Unit test execution** with detailed coverage reports (55 tests)

## Release Pipeline

This project uses **automated releases** via GitHub Actions. When a version tag is pushed, the pipeline automatically:

1. Builds Universal JAR and Windows EXE
2. Creates a GitHub Release (stable or pre-release based on tag format)
3. Uploads all binaries automatically
4. Generates release notes from commits

### Creating a Release

See [RELEASE.md](RELEASE.md) for detailed instructions on creating releases.

**Quick example:**
```bash
# For a stable release
git tag v3.0.0
git push origin v3.0.0

# For a pre-release (beta, RC, alpha)
git tag v3.0.0-beta
git push origin v3.0.0-beta
```

The pipeline detects pre-releases automatically (tags containing `-`) and marks them accordingly on GitHub.

## Previous Versions

Older versions (v1 and v2) are available in the `OLD/` directory for reference.

### Key Differences:
- **V1**: Single file, saves SDK to user home
- **V2**: Single file, hardcoded path to `C:\Android\sdk`
- **V3** (current): Modular architecture, card UI, accordions, automated releases, Java 21

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

MIT License - see LICENSE file for details

## Author

**Nicola Murtas**
Adobe Experience Manager Developer
📧 portfolio@nicolamurtas.net
🔗 [nicolamurtas.net](https://nicolamurtas.net)
🐙 [GitHub](https://github.com/NmurtasDev)

## Feedback

If you have suggestions, questions, or encounter any issues, please open an issue on GitHub or contact me directly.

---

**Note**: This tool requires Java 21. Tested on Windows 11, Ubuntu 22.04, and macOS Ventura.
