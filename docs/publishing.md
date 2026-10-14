# Publishing

Gradle installs this library from Maven Central as `io.github.leo-gan:gld-ion-kotlin`. The version in `gradle.properties` is `0.2.0`. Release tags match the Mojo libraries and have no `v` prefix: `0.2.0`.

Maven Central keeps every release. Publish a version only when the tests on that tag passed. A broken `0.2.0` cannot be replaced; the next release is `0.2.1` or `0.3.0`.

## Register once

### Namespace

1. Open [central.sonatype.com](https://central.sonatype.com/) and sign in with GitHub as `leo-gan`.
2. Confirm the account lists the namespace `io.github.leo-gan`. Signing in with GitHub verifies `io.github.<your GitHub username>` automatically. The group in `build.gradle.kts` is already `io.github.leo-gan`.
3. If the namespace is missing, request `io.github.leo-gan` from the namespace page and follow the verification steps: [central.sonatype.org/register/namespace](https://central.sonatype.org/register/namespace/).

### User token

The publisher username and password are a user token, not the GitHub password. Generate one from the account menu on [central.sonatype.com](https://central.sonatype.com/). The steps are also written at [central.sonatype.org/publish/generate-portal-token](https://central.sonatype.org/publish/generate-portal-token/).

The token dialog shows a username and a password. Store both. The dialog does not show the password again.

### Signing key

Maven Central requires a GPG signature on the jar, the sources jar, the javadoc jar, and the POM. The public key has to be on `keyserver.ubuntu.com`, which is the server Central queries.

```bash
gpg --batch --pinentry-mode loopback --passphrase 'choose-a-passphrase' \
  --quick-gen-key "Leonid Ganeline <leo.gan.57@gmail.com>" rsa4096 default 2y
gpg --list-secret-keys --keyid-format LONG
```

The long id is the 16 hex characters after `rsa4096/`. Send the public key, then export the private key:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys KEYID
gpg --export-secret-keys --armor KEYID
```

`gpg --export-secret-keys` prints the private key. Copy the whole block, including the `BEGIN` and `END` lines, into a GitHub secret. Do not commit it. A Base64 copy of that block also works, in `SIGNING_KEY_BASE64`, if the secret field flattens newlines.

### Repository secrets

In this GitHub repository, open Settings, then Secrets and variables, then Actions:

| Name | Value |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | User-token username from the Central Portal. |
| `MAVEN_CENTRAL_PASSWORD` | User-token password. |
| `SIGNING_KEY` | Armored private key from `gpg --export-secret-keys`. |
| `SIGNING_PASSWORD` | Passphrase for that key. Empty when the key has no passphrase. |
| `SIGNING_KEY_BASE64` | Optional. Used when `SIGNING_KEY` is unset. |

## Publish a version

Merge `.github/workflows/publish.yml` to `main` before creating the release.

```bash
git checkout main
git pull
git tag 0.2.0
git push origin 0.2.0
gh release create 0.2.0 --target main --title 0.2.0 --generate-notes
```

The Publish workflow runs `./gradlew test`, then `scripts/publish.sh`. That script runs `publishAndReleaseToMavenCentral`, which uploads the bundle and releases it. Central validation can take several minutes. The artifact is visible on Maven Central about 10 to 30 minutes after the portal marks the deployment published.

Search [central.sonatype.com](https://central.sonatype.com/search?q=gld-ion-kotlin) for `gld-ion-kotlin`.

Check the coordinate without uploading:

```bash
bash scripts/publish.sh --dry-run
```

## Install

```kotlin
dependencies {
    implementation("io.github.leo-gan:gld-ion-kotlin:0.2.0")
}
```
