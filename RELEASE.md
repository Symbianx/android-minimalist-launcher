# Quick Release Guide

## Prerequisites (One-time setup)
1. Set up GitHub Secrets as described in [.github/RELEASE_SETUP.md](.github/RELEASE_SETUP.md)
2. Create your app in Google Play Console
3. Complete the initial upload manually (first release must be done manually)

## Release Process

### Option 1: GitHub UI (Recommended)
1. Go to your repository on GitHub
2. Click **Releases** → **Draft a new release**
3. Create a new tag (e.g., `v1.0.0`)
4. Add release title and notes
5. Click **Publish release** - This triggers the CI/CD automatically

### Option 2: GitHub CLI
```bash
# 1. Make sure all changes are committed
git add .
git commit -m "Prepare for release v1.0.0"
git push origin main
Release tags should follow semantic versioning: `vMAJOR.MINOR.PATCH`

Examples:
- `v1.0.0` - First release (version code: 10000)
- `v1.0.1` - Patch release (version code: 10001)
- `v1.1.0` - Minor release (version code: 10100)
- `v2.0.0` - Major release (version code: 20000)

### Monitor Release
1. Go to [Actions tab](../../actions)
2. Check the "Release to Play Store" workflow
3. Once complete, verify in Google Play Console

### Delete/Rollback a Release
If you need to remove a release:

**Via GitHub UI:**
- Go to Releases → Click the release → Delete release

**Via GitHub CLI:**
```bash
gh release delete v1.0.0 --yes
```

**Delete the tag too:**
```bash
git tag -d v1.0.0

### Version Format
Tags should follow semantic versioning: `vMAJOR.MINOR.PATCH`

Examples:
- `v1.0.0` - First release (version code: 10000)
- `v1.0.1` - Patch release (version code: 10001)
- `v1.1.0` - Minor release (version code: 10100)
- `v2.0.0` - Major release (version code: 20000)

### Monitor Release
1. Go to [Actions tab](../../actions)
2. Check the "Release to Play Store" workflow
3. Once complete, verify in Google Play Console

### Rollback a Release
If you need to rollback:
```bash
# Delete the tag locally
git tag -d v1.0.0

# Delete the tag remotely
git push origin :refs/tags/v1.0.0
```

Then manage the rollback in Google Play Console.

## Release Notes
Update release notes in `distribution/whatsnew/whatsnew-en-US` before creating the tag.

## Local Release Build (Testing)
To test the release build locally without deploying:

```bash
# Build unsigned release AAB
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

## Troubleshooting
See [.github/RELEASE_SETUP.md](.github/RELEASE_SETUP.md#troubleshooting) for common issues.
