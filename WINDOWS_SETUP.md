# Windows Development Setup

Quick guide to set up your development environment on Windows.

## Quick Start (5 minutes)

### 1. Set Up Maven (One-time)

Run this PowerShell script once:

```powershell
powershell -ExecutionPolicy Bypass -File setup-maven.ps1
```

This will:
- ✓ Find your Java installation
- ✓ Download Maven 3.9.6
- ✓ Add Maven to your PATH
- ✓ Verify installation

### 2. Build the Project

```bash
mvn clean package -DskipTests
```

Or use the wrapper:

```bash
.\mvnw.cmd clean package -DskipTests
```

## Making Maven Permanent

To use Maven in ALL future PowerShell sessions, add these lines to your PowerShell profile:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "C:\Users\$env:USERNAME\.m2\apache-maven-3.9.6\bin;" + $env:PATH
```

### Edit Your Profile

1. Open PowerShell as Administrator
2. Run: `code $PROFILE`
3. Add the lines above (replace `jdk-24` with your version if different)
4. Save and restart PowerShell

Or find your profile at:
```
C:\Users\<YourUsername>\Documents\WindowsPowerShell\Microsoft.PowerShell_profile.ps1
```

## Common Maven Commands

```bash
# Build the project
mvn clean package -DskipTests

# Run tests
mvn test

# Compile only (no package)
mvn clean compile

# Check version
mvn -version

# Help
mvn help:describe -Dplugin=help
```

## Troubleshooting

### "mvn: command not found"

**Solution:** Run the setup script:
```powershell
powershell -ExecutionPolicy Bypass -File setup-maven.ps1
```

Or set JAVA_HOME and PATH manually:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "C:\Users\$env:USERNAME\.m2\apache-maven-3.9.6\bin;" + $env:PATH
mvn -version
```

### "JAVA_HOME is not defined correctly"

Your JAVA_HOME points to javapath (Oracle's wrapper). Set it to an actual JDK:

```powershell
# Find available JDK versions
Get-ChildItem "C:\Program Files\Java"

# Use the latest one
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
```

### "Cannot find mvn.cmd"

Maven wasn't downloaded. Run setup again:
```powershell
powershell -ExecutionPolicy Bypass -File setup-maven.ps1
```

## IDE Setup

### IntelliJ IDEA / JetBrains IDEs

1. Open `File → Project Structure → Project`
2. Set Project SDK to your JDK (e.g., jdk-24)
3. Set Project language level to 8+
4. IDEs will auto-detect Maven from `.mvn/wrapper` or system PATH

### Visual Studio Code

1. Install "Extension Pack for Java" by Microsoft
2. Open terminal in VS Code
3. Run: `mvn clean package -DskipTests`

## Environment Variables Set by Setup

After running `setup-maven.ps1`, these are set for your current session:

```
JAVA_HOME = C:\Program Files\Java\jdk-24
MAVEN_HOME = C:\Users\<user>\.m2\apache-maven-3.9.6
PATH = <maven-home>\bin;...
```

To make permanent, edit your PowerShell profile (see "Making Maven Permanent" above).

## See Also

- [RELEASE_PROCESS.md](RELEASE_PROCESS.md) - How to release versions
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) - Quick release reference
