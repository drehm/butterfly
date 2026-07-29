# Butterfly - Generic Test Tool

A professional Java application containing generic test tools with built-in **GitHub-based auto-update support**.

## Features

✅ **Auto-Update System** — Checks for new versions from GitHub releases  
✅ **Silent Downloads** — Downloads updates in background without blocking UI  
✅ **Safe Installation** — Backs up current version before replacing  
✅ **Auto-Restart** — Automatically restarts app after update  
✅ **Semver Support** — Understands semantic versioning (1.0.0, 1.0.1, etc.)  
✅ **Error Resilient** — Comprehensive error handling and network resilience  

## Quick Start

### Prerequisites
- Java 8+
- Maven 3.6+

### Build

```bash
mvn clean package
```

This creates: `target/butterfly-0.0.0.1.jar`

### Run

```bash
java -jar target/butterfly-0.0.0.1.jar
```

## Project Structure

```
butterfly/
├── pom.xml                          # Maven configuration
├── README.md                        # This file
├── .gitignore                       # Git ignore rules
└── src/
    ├── main/
    │   ├── java/com/weareplanet/butterfly/
    │   │   ├── Main.java           # Application entry point
    │   │   ├── updater/
    │   │   │   ├── UpdateChecker.java      # Core update engine
    │   │   │   └── UpdateInfo.java         # Update metadata
    │   │   └── ui/
    │   │       └── MainWindow.java  # Main UI window
    │   └── resources/
    └── test/
        └── java/                   # Test files
```

## How Auto-Updates Work

### 1. On Application Start
```
App starts
  └─ UpdateChecker.checkForUpdatesAsync()
     ├─ Fetch release info from GitHub API
     ├─ Compare version (semver)
     ├─ If newer: download JAR silently
     └─ Notify user when ready
```

### 2. User Workflow
```
User sees notification → Clicks "Install & Restart"
  └─ JAR replaced with new version
  └─ App restarts automatically
  └─ New version running
```

### 3. GitHub Integration
- Releases hosted on GitHub: `weareplanet/butterfly`
- Each release includes JAR file as attachment
- Auto-updater queries GitHub API automatically
- No separate update server needed!

## GitHub Setup (Next Step)

1. Create GitHub repository: `weareplanet/butterfly`
2. Create a release with JAR attachment
3. App will detect and install updates automatically

## Versioning

Update the version in `pom.xml`:
```xml
<version>1.0.1</version>  <!-- Bump this for each release -->
```

Then rebuild:
```bash
mvn clean package
```

Upload `target/butterfly-0.0.0.1.jar` to GitHub release.

## Building for Release

```bash
# Build clean package
mvn clean package -DskipTests

# JAR is created at:
target/butterfly-0.0.0.1.jar

# Upload to GitHub release (with release notes)
gh release create v0.0.0.1 --title "v0.0.0.1" --notes "Initial release" target/butterfly-0.0.0.1.jar
```

## Configuration

### Update Server
Currently set to GitHub API default. To customize:

Edit `UpdateChecker.java`:
```java
private static final String DEFAULT_UPDATE_SERVER = 
    "https://api.github.com/repos/weareplanet/butterfly/releases/latest";
```

### Update Directory
Updates are downloaded to: `~/.butterfly/updates/`

## Architecture

This project implements **Firefly-like auto-updates** for Java:

| Component | Purpose |
|-----------|---------|
| **UpdateChecker** | Core update engine (version check, download, install) |
| **UpdateInfo** | JSON parser for release metadata |
| **MainWindow** | UI with update notifications |
| **Main** | Application entry point |

## Next Steps

1. ✅ **Build executable JAR** (done)
2. ⏳ **Create GitHub repository**
3. ⏳ **Create first release**
4. ⏳ **Test auto-update flow**
5. ⏳ **Integrate legacy Butterfly code**

## Troubleshooting

### JAR won't run
```bash
java -jar butterfly-0.0.0.1.jar
# Check console output for errors
```

### Maven build fails
```bash
# Clean and rebuild
mvn clean package -X

# Check internet connection (dependencies download)
```

### Updates not detected
Check `~/.butterfly/updates/` directory:
```bash
ls ~/.butterfly/updates/
```

## Contributing

To add features to Butterfly:
1. Edit source in `src/main/java/`
2. Test locally: `mvn clean package && java -jar target/butterfly-0.0.0.1.jar`
3. Commit changes
4. Create GitHub release with version bump

## License

Internal tool - Planet Payment Development Team (JNE)

## Support

Questions or issues? Contact: VP Terminal Development

---

**Ready to use! 🚀**

Next: Create GitHub repo and release your first version.
