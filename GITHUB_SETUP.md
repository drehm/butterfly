# GitHub Setup Guide for Butterfly

## Step 1: Create Private Repository on GitHub

1. Go to: https://github.com/new
2. Fill in:
   - **Repository name:** `butterfly`
   - **Description:** `Payment Terminal Management Application with Auto-Update Support`
   - **Visibility:** `Private` (select this!)
   - **Initialize repository:** Leave unchecked (we already have code)
3. Click **Create repository**

GitHub will show you the commands to push existing code. Copy them.

---

## Step 2: Connect Local Repository to GitHub

After creating the repository on GitHub, you'll see commands like:

```bash
git remote add origin https://github.com/drehm/butterfly.git
git branch -m main
git push -u origin main
```

Run these commands in your project directory:

```bash
cd D:\Workspace_Butterfly\butterflyV2
git remote add origin https://github.com/drehm/butterfly.git
git branch -m main
git push -u origin main
```

**If prompted for password:** Use a GitHub Personal Access Token (not your password)

---

## Step 3: Create GitHub Personal Access Token

If you don't have one:

1. Go to: https://github.com/settings/tokens
2. Click **Generate new token → Generate new token (classic)**
3. Give it a name: `butterfly-push`
4. Select scopes:
   - ✅ `repo` (full control of private repositories)
   - ✅ `workflow` (for CI/CD later)
5. Click **Generate token**
6. **Copy and save it** (you won't see it again!)

Use this token as your password when pushing.

---

## Step 4: Verify Private Repository

After pushing:

1. Go to: https://github.com/drehm/butterfly
2. Verify:
   - ✓ Private (shows lock icon)
   - ✓ All files uploaded
   - ✓ Git history available

---

## Step 5: Create First Release

Once repository is on GitHub:

```bash
git tag -a v1.0.0 -m "Initial release: Butterfly v1.0.0 with auto-update support"
git push origin v1.0.0
```

Then on GitHub:
1. Go to: https://github.com/drehm/butterfly/releases
2. Click **Create a release**
3. Select tag: `v1.0.0`
4. Title: `Butterfly v1.0.0`
5. Description: Copy from README.md
6. Upload JAR: `butterfly-1.0.0.jar` from your project's `target/` folder
7. Click **Publish release**

---

## Step 6: Test Auto-Update

Once the release is created:

1. Update version in `pom.xml` to `1.0.1`
2. Update version in `Main.java` to `"1.0.1"`
3. Rebuild: `mvn clean package`
4. Create new release with `v1.0.1` tag
5. Run your app - it should detect the new version!

---

## Important Notes

### Private Repository
- ✅ Only you can access
- ✅ Only you can download releases
- ⚠️ Auto-update checks will fail if not authenticated
  - Solution: Add GitHub token to UpdateChecker for private repos

### For Auto-Updates with Private Repos

If your repository is private and you want auto-updates to work, you'll need to:

1. Use an authenticated GitHub API call
2. Add your token as an environment variable or config file
3. Or: Make releases public (assets) but repository private

**For now:** Repository is private, app won't auto-update until we add authentication.

---

## Quick Reference Commands

```bash
# Check status
git status

# See commits
git log --oneline

# Push updates
git push origin main

# Create tag
git tag -a v1.0.1 -m "Release v1.0.1"

# Push tag
git push origin v1.0.1
```

---

**Next:** Follow the steps above, then let me know when GitHub is set up!
