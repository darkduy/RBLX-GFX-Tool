# Hướng dẫn Setup GitHub Actions

## 1. Tạo keystore (ký APK release)

```bash
keytool -genkey -v \
  -keystore rblx-gfx-tool.jks \
  -alias rblx-gfx \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

> `keytool` nằm trong JDK — đảm bảo đã cài JDK 17 và thêm vào PATH.

---

## 2. Encode keystore sang base64

### Linux / macOS
```bash
base64 rblx-gfx-tool.jks > keystore.b64
cat keystore.b64
```

### Windows (PowerShell)
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("rblx-gfx-tool.jks")) | Set-Content keystore.b64 -NoNewline
Get-Content keystore.b64
```

### Windows (CMD)
```cmd
certutil -encode rblx-gfx-tool.jks keystore.b64
```
> Với `certutil`: xóa dòng đầu (`-----BEGIN CERTIFICATE-----`) và dòng cuối (`-----END CERTIFICATE-----`) trước khi dùng.

---

## 3. Thêm GitHub Secrets

Vào **repo GitHub → Settings → Secrets and variables → Actions → New repository secret**:

| Secret name        | Giá trị                                      |
|--------------------|----------------------------------------------|
| `KEYSTORE_BASE64`  | Toàn bộ nội dung file `keystore.b64`         |
| `KEY_ALIAS`        | `rblx-gfx` (alias bạn đặt ở bước 1)         |
| `KEY_PASSWORD`     | Mật khẩu key (nhập khi chạy keytool)        |
| `STORE_PASSWORD`   | Mật khẩu keystore (nhập khi chạy keytool)   |

> **Lưu ý:** Nếu không thêm secrets, workflow vẫn build bình thường nhưng
> APK release sẽ **không được ký** — không cài được trên thiết bị thật.

---

## 4. Cập nhật build.yml để ký APK đúng cách

File `build.yml` đã có bước sign sẵn. Nếu muốn tích hợp signing vào Gradle thay vì apksigner CLI,
thêm block này vào `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        val keystoreB64 = System.getenv("KEYSTORE_BASE64") ?: ""
        if (keystoreB64.isNotEmpty()) {
            // Decode base64 keystore từ env variable
            val keystoreFile = File(buildDir, "keystore_ci.jks")
            keystoreFile.writeBytes(android.util.Base64.decode(keystoreB64, android.util.Base64.DEFAULT))
            storeFile     = keystoreFile
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias      = System.getenv("KEY_ALIAS")
            keyPassword   = System.getenv("KEY_PASSWORD")
        }
    }
}

buildTypes {
    release {
        signingConfig    = signingConfigs.getByName("release")
        isMinifyEnabled  = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

---

## 5. Build workflow

### Tự động
```bash
# Push lên main → build debug APK
git push origin main

# Push tag → build release + tạo GitHub Release kèm APK
git tag v1.0.0
git push origin v1.0.0
```

### Thủ công
GitHub repo → **Actions** → **Build RBLX GFX Tool APK** → **Run workflow** → chọn `debug` / `release` / `both`

---

## 6. Lấy APK sau khi build

- **Artifact** (debug/release): Actions tab → chọn run → cuộn xuống **Artifacts** → Download
- **GitHub Release** (chỉ khi push tag): Releases tab → chọn version → download APK đính kèm

---

## 7. Tên file APK output

```
RBLX-GFX-Tool-v1.0.0-debug.apk
RBLX-GFX-Tool-v1.0.0-release.apk
RBLX-GFX-Tool-v1.0.0-release-signed.apk   ← nếu có keystore secrets
```