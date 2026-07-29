# Quick Start Guide - ButterflyV2

## Opening in IntelliJ IDEA

### Step 1: Open Project
1. Launch **IntelliJ IDEA**
2. Click **File → Open**
3. Navigate to: `D:\Workspace_Butterfly\butterflyV2`
4. Click **Open**
5. Click **Trust Project** if prompted

### Step 2: Wait for Indexing
- IntelliJ will index the project
- Maven will download dependencies automatically
- This takes 1-2 minutes on first open

### Step 3: Verify Setup
Look in the bottom right corner:
- Should see "Maven projects need to be imported" → Click **Import Changes**
- Wait for download to complete ✓

### Step 4: Run the Application
1. Open: `src/main/java/com/weareplanet/butterfly/Main.java`
2. Right-click → **Run 'Main.main()'**
3. You should see the Butterfly window open! 🎉

## Building the JAR

### Option 1: Using IntelliJ
1. **Build → Build Project**
2. Or: **Build → Build Artifacts → butterfly:butterfly**
3. JAR appears in: `target/butterfly-1.0.0.jar`

### Option 2: Using Terminal
```bash
cd D:\Workspace_Butterfly\butterflyV2
mvn clean package
```

Then run it:
```bash
java -jar target/butterfly-1.0.0.jar
```

## Project Overview

**Main Classes:**
- `Main.java` - Application entry point with auto-updater
- `MainWindow.java` - GUI (Swing-based)
- `UpdateChecker.java` - Auto-update engine
- `UpdateInfo.java` - Update metadata parser

**Key Features:**
✅ Auto-checks for updates from GitHub  
✅ Downloads updates silently  
✅ Prompts user to install  
✅ Restarts automatically  

## Updating Version for Release

When you're ready to release a new version:

### 1. Update pom.xml
```xml
<version>1.0.1</version>  <!-- Change from 1.0.0 to 1.0.1 -->
```

### 2. Build
```bash
mvn clean package
```

### 3. Upload to GitHub
```bash
gh release create v1.0.1 \
  --title "v1.0.1" \
  --notes "Bug fixes and improvements" \
  target/butterfly-1.0.0.jar
```

Users running v1.0.0 will see the update and can install it!

## Troubleshooting

### "Java file not found"
- Make sure you have Java 8+ installed
- Right-click project: **Mark Directory as → Sources Root**

### "Maven not found"
- IntelliJ should have built-in Maven
- If not: Settings → Build, Execution, Deployment → Maven → Configure

### "Dependencies won't download"
- Check internet connection
- Try: **File → Invalidate Caches** → Restart IntelliJ

### Application won't run
- Check Java is Java 8+: `java -version`
- Rebuild project: **Build → Clean Project**, then **Build → Build Project**

## Next Steps

1. ✅ Project created and tested locally
2. ⏳ Create GitHub repository
3. ⏳ Commit this code
4. ⏳ Create first release with JAR
5. ⏳ Test auto-update from that release

Ready to move to GitHub? Let me know!
