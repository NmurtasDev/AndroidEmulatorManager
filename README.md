# Android Emulator Manager

[![Java CI with Maven](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/maven.yml/badge.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/maven.yml)
[![CodeQL](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/codeql.yml/badge.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager)
[![Version](https://img.shields.io/badge/version-3.0.0--SNAPSHOT-orange.svg)](https://github.com/NmurtasDev/AndroidEmulatorManager/releases)

A modern **Java-based GUI tool** to download, install, and manage Android SDK and emulators.

## Features

- **Automated SDK Setup**: Download and install Android SDK automatically
- **Cross-Platform**: Works on Windows, Linux, and macOS
- **Emulator Management**: Create, start, stop, and delete Android Virtual Devices (AVDs)
- **Modern Architecture**: Built with Java 21 and Maven
- **Clean UI**: User-friendly Swing-based interface
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
2. **Click "Scarica SDK"** to automatically download and install Android SDK
3. **Create a new AVD** by clicking "Crea Nuovo"
4. **Select an AVD** from the list and click "Avvia" to start the emulator

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

- ✅ **Proper separation of concerns** (UI, Service, Util layers)
- ✅ **Modern Java 21** features and best practices
- ✅ **Maven build system** for dependency management
- ✅ **Professional logging** with SLF4J/Logback
- ✅ **Security improvements** (no command injection vulnerabilities)
- ✅ **Better error handling** and user feedback
- ✅ **Unit tests** for critical functionality
- ✅ **Cross-platform compatibility** tested

## Previous Versions

Older versions (v1 and v2) are available in the `OLD/` directory for reference.

### Key Differences:
- **V1**: Single file, saves SDK to user home
- **V2**: Single file, hardcoded path to `C:\Android\sdk`
- **V3** (current): Modular architecture, configurable paths, Java 21

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
