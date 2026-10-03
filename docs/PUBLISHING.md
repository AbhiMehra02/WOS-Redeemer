# Publishing to GitHub

## Create the repository

Create an empty GitHub repository named, for example, `WOS-Gift-Redeemer`. Do not initialize another README or `.gitignore`.

## Push the source

From Termux, macOS/Linux Terminal, or Git Bash:

```bash
git init -b main
git config user.name "YOUR_GITHUB_USERNAME"
git config user.email "YOUR_GITHUB_EMAIL"
git add .
git commit -m "Initial public release v1"
git remote add origin https://github.com/YOUR_USERNAME/WOS-Gift-Redeemer.git
git push -u origin main
```

## Publish v1

On GitHub open **Releases > Draft a new release**:

- Tag: `v1`
- Target: `main`
- Title: `WOS Gift Redeemer v1`
- Pre-release: No
- Attach: `WOSGiftRedeemer-v1.apk`

Publish the release. Keep APK binaries in GitHub Releases rather than committing them to the source repository.
