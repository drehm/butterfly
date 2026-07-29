# Butterfly Release Checklist

Quick reference for releasing a new version.

## 5-Minute Release Process

```bash
# 1. Edit pom.xml - change version from X.Y.Z to NEW.VERSION
#    Example: 0.0.0.1 → 0.0.0.2

# 2. Commit
git add pom.xml
git commit -m "Bump version to NEW.VERSION"
git push

# 3. Build
mvn clean package -DskipTests

# 4. Release (replace NEW.VERSION with your version)
gh release create vNEW.VERSION target/butterfly-NEW.VERSION.jar

# 5. Verify
gh release view vNEW.VERSION
```

## What Gets Updated Automatically

- ✅ `Main.java` version (loaded from app.properties)
- ✅ JAR filename in `pom.xml`
- ✅ UpdateChecker gets correct version
- ✅ GitHub release asset available

## Common Versions

| Type | From | To | Example |
|------|------|-----|---------|
| Patch | 0.0.0.1 | 0.0.0.2 | Bug fixes, small patches |
| Minor | 0.0.0.2 | 0.1.0 | New features |
| Major | 0.1.0 | 1.0.0 | Breaking changes |

## Verify Deployment

```bash
# Check version loaded correctly
java -cp "target/classes;." com.weareplanet.butterfly.Main

# Check release on GitHub
gh release view vNEW.VERSION --json assets
```

## If Something Goes Wrong

```bash
# Undo last commit (before push)
git reset --soft HEAD~1

# Delete release (after push)
gh release delete vNEW.VERSION

# Force push (if committed)
git push --force
```

---

**Full documentation:** See `RELEASE_PROCESS.md` for detailed steps
