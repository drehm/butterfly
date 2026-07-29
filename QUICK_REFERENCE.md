# ButterflyV2 - Quick Reference

## Project Location
```
D:\Workspace_Butterfly\butterflyV2
```

## File Locations

| File | Location |
|------|----------|
| **Source Code** | `src/main/java/com/weareplanet/butterfly/` |
| **Icons** | `src/main/resources/icons/` |
| **JAR Output** | `target/butterfly-1.0.0.jar` |
| **Documentation** | README.md, GETTING_STARTED.md, GITHUB_SETUP.md |
| **Maven Config** | `pom.xml` |

## Key Classes

| Class | Purpose |
|-------|---------|
| `Main.java` | Application entry point, initializes updater |
| `MainWindow.java` | UI window with icon and update notifications |
| `UpdateChecker.java` | Core auto-update engine |
| `UpdateInfo.java` | JSON parser for update metadata |

## Building & Running

### Build JAR
```bash
cd D:\Workspace_Butterfly\butterflyV2
mvn clean package
```

### Run Locally
```bash
java -jar target/butterfly-1.0.0.jar
```

### Quick Test
```bash
# Compile
javac -d target/classes -encoding UTF-8 -source 8 -target 8 src/main/java/com/weareplanet/butterfly/*.java

# Run
java -cp target/classes com.weareplanet.butterfly.Main
```

## Version Updates

To release a new version:

### 1. Update pom.xml
```xml
<version>1.0.1</version>  <!-- Change this -->
```

### 2. Update Main.java
```java
private static final String APP_VERSION = "1.0.1";  // Change this
```

### 3. Rebuild
```bash
mvn clean package
```

### 4. Create Release on GitHub
```bash
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

Then upload JAR to GitHub release.

## GitHub Commands

### Initial Setup
```bash
cd D:\Workspace_Butterfly\butterflyV2
git remote add origin https://github.com/drehm/butterfly.git
git branch -m main
git push -u origin main
```

### Update Code
```bash
git add .
git commit -m "Your message"
git push origin main
```

### Create Release
```bash
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

## Project Status

✅ **Complete:**
- Maven structure
- Auto-update system
- Butterfly icons (5 sizes)
- Swing UI
- Git repository initialized
- Documentation

⏳ **Next:**
- Create private repository on GitHub.com
- Push code to GitHub
- Create first release with JAR attachment
- Test auto-update flow

## GitHub Repository

**Name:** butterfly  
**Visibility:** Private  
**URL:** https://github.com/drehm/butterfly  

## Auto-Update Flow

```
1. User runs app
2. UpdateChecker queries GitHub API
3. Checks latest release version
4. Compares with current version
5. If newer: downloads silently
6. Shows notification to user
7. User clicks "Install & Restart"
8. App replaces JAR and restarts
9. New version running!
```

## Important Files

```
butterflyV2/
├── pom.xml                 # Maven configuration
├── README.md              # Main documentation
├── GETTING_STARTED.md     # Setup guide
├── GITHUB_SETUP.md        # GitHub instructions
├── .gitignore            # Git ignore rules
└── src/
    └── main/
        ├── java/         # Source code
        └── resources/    # Icons
```

## Troubleshooting

**JAR won't build?**
- Ensure Java 8+ is installed
- Check pom.xml is valid
- Run: `mvn clean package -X` for debug output

**Icons missing?**
- Check: `src/main/resources/icons/` exists
- Files should be: butterfly_16.png, butterfly_32.png, etc.
- Rebuild JAR

**Git push fails?**
- Use GitHub Personal Access Token (not password)
- Check repository is private
- Verify remote: `git remote -v`

---

**Status:** Ready for GitHub! 🚀
