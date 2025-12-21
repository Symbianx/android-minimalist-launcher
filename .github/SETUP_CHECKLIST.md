# CI/CD Setup Checklist

## ✅ Initial Setup (Complete these steps once)

### 1. Generate Release Keystore
- [ ] Generate a release keystore if you don't have one:
```bash
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```
- [ ] Store the keystore file in a secure location (NOT in the repository)
- [ ] Remember the keystore password, key alias, and key password

### 2. Set Up Google Play Console
- [ ] Create your app in [Google Play Console](https://play.google.com/console/)
- [ ] Complete the app details (name, description, screenshots, etc.)
- [ ] Complete the content rating questionnaire
- [ ] Set up pricing and distribution
- [ ] Perform the first release manually (upload an AAB through the console)
- [ ] Wait for the app to be reviewed and published

### 3. Create Google Play Service Account
- [ ] Go to Google Play Console → Setup → API access
- [ ] Create a new service account or link to an existing one
- [ ] Download the JSON key file
- [ ] Grant the service account "Admin" or "Release Manager" permissions

### 4. Configure GitHub Secrets
Go to your repository → Settings → Secrets and variables → Actions

Add the following secrets:
- [ ] `KEYSTORE_BASE64` - Base64-encoded keystore file
  ```bash
  base64 -i release.keystore | pbcopy  # macOS
  ```
- [ ] `KEYSTORE_PASSWORD` - Your keystore password
- [ ] `KEY_ALIAS` - Your key alias (e.g., "release")
- [ ] `KEY_PASSWORD` - Your key password
- [ ] `PLAY_STORE_SERVICE_ACCOUNT_JSON` - Content of the service account JSON file

### 5. Verify Build Configuration
- [ ] Ensure [app/build.gradle.kts](app/build.gradle.kts) has the release signing config
- [ ] Verify package name matches: `com.symbianx.minimalistlauncher`
- [ ] Confirm ProGuard rules are properly configured

### 6. Test the Workflow
- [ ] Create a test release on GitHub:
  - Go to Releases → Draft a new release
  - Create tag `v0.0.1-test`
  - Add title and notes
  - Click "Publish release"
- [ ] Monitor the GitHub Actions workflow
- [ ] Verify the build completes successfully
- [ ] Check Google Play Console for the upload
- [ ] Delete the test release and tag from GitHub

## 📝 Before Each Release
- [ ] Update release notes in `distribution/whatsnew/whatsnew-en-US`
- [ ] Commit all changes
- [ ] Create a GitHub Release with a version tag (e.g., `v1.0.0`)

## 🔍 Verification
After pushing a tag, verify:
- [ ] GitHub Actions workflow completes successfully
- [ ] Release appears in Google Play Console
- [ ] AAB and mapping.txt are attached to GitHub release
- [ ] Reblishing a release on GitHub, verify:
- [ ] GitHub Actions workflow completes successfully
- [ ] Release appears in Google Play Console
- [ ] AAB and mapping.txt are attached to the GitHub R)
- Setup documentation: [.github/RELEASE_SETUP.md](.github/RELEASE_SETUP.md)
- Release guide: [RELEASE.md](RELEASE.md)

## 📚 Additional Resources
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [Android App Bundle Documentation](https://developer.android.com/guide/app-bundle)
