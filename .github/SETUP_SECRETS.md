# Hướng dẫn Setup Signing APK

## 1. Tạo keystore

```bash
keytool -genkey -v \
  -keystore rblx-gfx-tool.jks \
  -alias rblx-gfx \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

> `keytool` nằm trong JDK — đảm bảo đã cài JDK 17 và thêm vào PATH.  
> Nhớ kỹ password bạn nhập — cần điền đúng vào GitHub Secrets ở bước 3.

---

## 2. Encode keystore sang base64 (KHÔNG có newline)

> ⚠️ Bắt buộc encode **không có newline** — nếu có newline, bước ký APK sẽ báo lỗi "keystore password was incorrect" dù password đúng.

### Linux
```bash
base64 -w 0 rblx-gfx-tool.jks > keystore.b64
cat keystore.b64
```

### macOS
```bash
base64 -i rblx-gfx-tool.jks | tr -d '\n' > keystore.b64
cat keystore.b64
```

### Windows (PowerShell)
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("rblx-gfx-tool.jks")) | Set-Content keystore.b64 -NoNewline
Get-Content keystore.b64
```

---

## 3. Verify keystore trước khi upload

```bash
# Decode thử và list alias — không được báo lỗi
cat keystore.b64 | tr -d '\n\r ' | base64 -d > test.jks
keytool -list -keystore test.jks -storepass YOUR_STORE_PASSWORD

# Output đúng trông như thế này:
# Keystore type: PKCS12
# Your keystore contains 1 entry
# rblx-gfx, ...
```

Nếu ra được danh sách alias → secrets sẽ hoạt động.

---

## 4. Thêm GitHub Secrets

Vào **repo GitHub → Settings → Secrets and variables → Actions → New repository secret**:

| Secret name        | Giá trị                                    |
|--------------------|--------------------------------------------|
| `KEYSTORE_BASE64`  | Toàn bộ nội dung file `keystore.b64`       |
| `KEY_ALIAS`        | `rblx-gfx`                                 |
| `STORE_PASSWORD`   | Password keystore nhập khi chạy keytool    |
| `KEY_PASSWORD`     | Password key nhập khi chạy keytool         |

> Nếu không thêm secrets, workflow vẫn build bình thường nhưng APK sẽ **không được ký**.

---

## 5. Trigger build

### Push tag → build + ký APK
```bash
git tag v1.0.0
git push origin v1.0.0
```

### Push lên main/develop → build APK
```bash
git push origin main
```

### Chạy thủ công
GitHub repo → **Actions** → **Build RBLX GFX Tool APK** → **Run workflow**

---

## 6. Lấy APK sau khi build

**Actions tab → chọn run → cuộn xuống Artifacts → Download**

---

## 7. Tên file APK output

```
RBLX-GFX-Tool-v1.0.0-release.apk          ← unsigned (không có keystore)
RBLX-GFX-Tool-v1.0.0-release-signed.apk   ← signed (có keystore secrets)
```