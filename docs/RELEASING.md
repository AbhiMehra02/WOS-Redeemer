# Releasing

The first public stable release uses tag **v1**.

## Create the release

```bash
git add .
git commit -m "Prepare first stable release"
git push origin main

git tag -a v1 -m "WOS Gift Redeemer v1"
git push origin v1
```

The GitHub Actions release workflow builds the APK and publishes it as:

`WOSGiftRedeemer-v1.apk`

## Android version metadata

- `versionCode`: `1`
- `versionName`: `1.0`

For store distribution, configure a private release signing key. Do not commit keystores, passwords, or signing secrets to the repository.
