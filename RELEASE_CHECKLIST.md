# Butterfly Release Checklist

Quick reference for releasing a new version.

## Release Process (7 Steps)

```bash
# 1. Edit pom.xml - change version to NEW.VERSION
#    Example: 0.0.0.1 → 0.0.0.2

# 2. Commit ALL changes (not just pom.xml)
git add .
git commit -m "Bump version to NEW.VERSION"

# 3. Push to GitHub
git push origin main

# 4. Build
mvn clean package -DskipTests

# 5. Create git tag (marks this commit as a release)
git tag -a vNEW.VERSION -m "Release version NEW.VERSION"
git push origin vNEW.VERSION

# 6. Create GitHub release (upload JAR)
gh release create vNEW.VERSION target/butterfly-NEW.VERSION.jar

# 7. Verify
gh release view vNEW.VERSION
```

### Quick Version (if tag auto-created is ok)

```bash
# Steps 1-4 above, then:
gh release create vNEW.VERSION target/butterfly-NEW.VERSION.jar
gh release view vNEW.VERSION
```

## What Gets Updated Automatically

- ✅ `Main.java` version (loaded from app.properties)
- ✅ JAR filename in `pom.xml`
- ✅ UpdateChecker gets correct version
- ✅ GitHub release asset available
- ✅ Git tag created (marks this commit as release point)

## Why Push ALL Changes First?

- **Backup:** All code is backed up on GitHub
- **History:** Team sees full commit history
- **Tag reference:** Git tag points to complete release commit
- **CI/CD:** Automated pipelines can see all changes
- **Reverting:** Easier to rollback if needed

**Don't just commit pom.xml** - include any other changes in the same commit.

## Git Tag: Explicit vs Auto

| Method | Command | Pros | Cons |
|--------|---------|------|------|
| **Explicit** | `git tag -a vX.Y.Z` + `git push origin vX.Y.Z` | Shows in git log, explicit record | Extra steps |
| **Auto** | `gh release create vX.Y.Z` (tag auto-created) | Fewer steps, still works | Less visible in git |

**Use explicit tags for production.** They appear in `git log`, `git tag -l`, and `git describe`.

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
