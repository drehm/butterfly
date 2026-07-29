# Push to GitHub - Step-by-Step Commands

## Prerequisites
- GitHub account (you have: drehm)
- GitHub Personal Access Token (if using HTTPS)

---

## Step 1: Create Private Repository on GitHub.com

1. Go to: **https://github.com/new**
2. Fill in:
   - **Repository name:** `butterfly`
   - **Description:** `Payment Terminal Management Application`
   - **Visibility:** Select `Private` ✓
   - **DO NOT initialize with README** (you already have one)
3. Click: **Create repository**

**You'll see a page with commands - copy the HTTPS URL**

---

## Step 2: Get Your GitHub Personal Access Token

1. Go to: https://github.com/settings/tokens
2. Click: **Generate new token → Generate new token (classic)**
3. Give it a name: `butterfly-push`
4. Check these scopes:
   - ✓ `repo` (full control)
   - ✓ `workflow` (optional, for CI/CD)
5. Click: **Generate token**
6. **COPY THE TOKEN** - you won't see it again!
7. Paste it somewhere safe temporarily

---

## Step 3: Push Code to GitHub

Open PowerShell and run these commands:

```powershell
# Navigate to your project
cd D:\Workspace_Butterfly\butterflyV2

# Add GitHub as remote
git remote add origin https://github.com/drehm/butterfly.git

# Rename branch to main (GitHub default)
git branch -m main

# Push to GitHub
git push -u origin main
```

**When prompted for password:** Use your GitHub Personal Access Token (paste the one you copied)

**Expected output:**
```
Enumerating objects: 16, done.
Counting objects: 100% (16/16), done.
...
[new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

## Step 4: Verify on GitHub

1. Go to: **https://github.com/drehm/butterfly**
2. Verify:
   - ✓ Repository shows as Private
   - ✓ All files are there
   - ✓ Git history is visible
   - ✓ README.md displays

---

## Step 5: Create First Release

Run these commands:

```powershell
cd D:\Workspace_Butterfly\butterflyV2

# Create a tag for version 1.0.0
git tag -a v1.0.0 -m "Initial release: Butterfly v1.0.0 with auto-update support"

# Push the tag to GitHub
git push origin v1.0.0
```

---

## Step 6: Upload JAR to GitHub Release

1. Go to: **https://github.com/drehm/butterfly/releases**
2. Click: **Create a release**
3. Fill in:
   - **Tag version:** `v1.0.0` (should auto-select)
   - **Release title:** `Butterfly v1.0.0`
   - **Description:**
     ```
     Initial release of Butterfly with auto-update support
     
     Features:
     - GitHub-based auto-updates
     - Butterfly icon (5 sizes)
     - Professional Swing UI
     - Ready for production
     
     Installation:
     java -jar butterfly-1.0.0.jar
     ```
4. Click: **Choose files** or **Attach binaries**
5. Select: `D:\Workspace_Butterfly\butterflyV2\target\butterfly-1.0.0.jar`
6. Click: **Publish release**

---

## Step 7: Test Auto-Updates

1. Make sure the release is published
2. Run your app:
   ```bash
   java -jar D:\Workspace_Butterfly\butterflyV2\target\butterfly-1.0.0.jar
   ```
3. It should attempt to check GitHub (will fail silently since private repo)

---

## Complete Command Sequence

Copy & paste all at once:

```powershell
cd D:\Workspace_Butterfly\butterflyV2
git remote add origin https://github.com/drehm/butterfly.git
git branch -m main
git push -u origin main
git tag -a v1.0.0 -m "Initial release: Butterfly v1.0.0 with auto-update support"
git push origin v1.0.0
```

Then manually:
1. Go to GitHub and create release
2. Upload JAR file
3. Publish

---

## Troubleshooting

### "fatal: remote origin already exists"
Run: `git remote remove origin` first, then try adding again

### "Authentication failed"
- Use Personal Access Token, NOT your password
- Token must have `repo` scope

### "Permission denied"
- Check token has `repo` scope
- Check your username is correct (drehm)

### JAR file not in target folder?
Rebuild:
```bash
cd D:\Workspace_Butterfly\butterflyV2
mvn clean package
```

---

## After Successful GitHub Setup

✅ Private repository created  
✅ Code pushed to GitHub  
✅ v1.0.0 release published  
✅ JAR file attached  
✅ Auto-update system ready  

**Next:** Update to v1.0.1 to test auto-updates:
1. Update pom.xml version to `1.0.1`
2. Update Main.java version to `"1.0.1"`
3. Run: `mvn clean package`
4. Create v1.0.1 tag and release
5. Your app will detect the update!

---

**Status:** Ready to push! 🚀
