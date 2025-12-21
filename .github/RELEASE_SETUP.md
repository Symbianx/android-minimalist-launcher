# GitHub Actions CI/CD Setup

This repository uses GitHub Actions to automatically publish releases to the Google Play Store when new GitHub Releases are published.

## Workflow Overview

The workflow is triggered when you publish a GitHub Release (through the GitHub UI or CLI).

### What the workflow does:
1. Checks out the code
2. Sets up JDK 17
3. Extracts version information from the release tag
4. Builds a signed release AAB (Android App Bundle)
5. Uploads the AAB to Google Play Store
6. Attaches the AAB and ProGuard mapping file to the GitHub Release

## Required GitHub Secrets

You need to configure the following secrets in your GitHub repository (Settings → Secrets and variables → Actions):

### 1. `KEYSTORE_BASE64`
Your release keystore file encoded in base64.

To create this:
```bash
# Generate a release keystore if you don't have one
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# Encode it to base64
base64 -i release.keystore | pbcopy  # macOS
# or
base64 release.keystore | xclip -selection clipboard  # Linux
```

Paste the output as the `KEYSTORE_BASE64` secret.

### 2. `KEYSTORE_PASSWORD`
The password for your keystore file.

### 3. `KEY_ALIAS`
The alias for your signing key (e.g., `release`).

### 4. `KEY_PASSWORD`
The password for your signing key.

### 5. `PLAY_STORE_SERVICE_ACCOUNT_JSON`
Google Play Console service account JSON key.

To create this:
1. Go to [Google Play Console](https://play.google.com/console/)
2. Select your app
3. Go to **Setup → API access**
4. Create a new service account or use an existing one
5. Grant the service account permissions: **Admin** (or at least **Release Manager**)
6. Generate a JSON key for the service account
7. Copy the entire JSON content and paste it as the secret value

## How to Release

### Option 1: Using GitHub UI (Recommended)

1. Go to your repository on GitHub
2. Click on **Releases** (right sidebar) → **Draft a new release**
3. Click **Choose a tag** and create a new tag (e.g., `v1.0.0`)
4. Set the release title (e.g., "Version 1.0.0")
5. Add release notes describing the changes
6. Click **Publish release**

The workflow will automatically trigger and deploy to the Play Store.

### Option 2: Using GitHub CLI

```bash
# Make sure all changes are committed
git add .
git commit -m "Prepare for release v1.0.0"
git push origin main

# Create and publish a release
gh release create v1.0.0 \
  --title "Version 1.0.0" \
  --notes "Release notes here"
```

### Option 3: Using Git Tags (then create release)

```bash
# Create and push a tag
git tag v1.0.0
git push origin v1.0.0

# Then go to GitHub and create a release from that tag
```

## Troubleshooting

### Build fails with signing errors
- Verify all keystore-related secrets are correct
- Ensure the keystore base64 encoding is complete (no line breaks)

### Upload to Play Store fails
- Verify the service account JSON is valid
- Ensure the service account has proper permissions
- Check that the package name matches: `com.symbianx.minimalistlauncher`
- Make sure you've created the app in Google Play Console first

### Version conflicts
- Ensure your version code is higher than any previously uploaded version
- The workflow automatically calculates version code from the tag (e.g., v1.2.3 → 10203)

## Customizing the Workflow

### Change the release track
Edit `.github/workflows/release.yml` and modify the `track` parameter:
- `internal` - Internal testing track
- `alpha` - Alpha testing track
- `beta` - Beta testing track
- `production` - Production track

### Build APK instead of AAB
Replace `bundleRelease` with `assembleRelease` and update the file path accordingly.
