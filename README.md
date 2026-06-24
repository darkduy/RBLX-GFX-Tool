# RBLX GFX Tool 🎮

![Build](https://github.com/<YOUR_USERNAME>/RBLXGFXTool/actions/workflows/build.yml/badge.svg)
![Release](https://img.shields.io/github/v/release/<YOUR_USERNAME>/RBLXGFXTool)
![Android](https://img.shields.io/badge/Android-10%2B-green)

Ứng dụng Android chỉnh đồ họa Roblox toàn diện — không cần root (hoặc có root để patch trực tiếp).

## Tính năng

| Nhóm | Chi tiết |
|------|----------|
| **Resolution / FPS** | Scale màn hình 50–100%, giới hạn FPS 30/60/120/Unlimited, Graphics Quality 1–10 |
| **Shadows** | Bật/tắt, 4 mức chất lượng (Off → High) |
| **Textures** | Bật/tắt, Low/Medium/High |
| **Particles** | Particles, vụ nổ, mây, cỏ |
| **Lighting** | Compatibility / ShadowMap / Voxel / Future |
| **Post FX** | Bloom, Sun Rays, Depth of Field, Ambient Occlusion |
| **Fog** | Bật/tắt, điều chỉnh start/end distance |
| **Presets** | Performance ⚡ / Balanced ⚖️ / Ultra 💎 + lưu preset tùy chỉnh |
| **Overlay** | Floating button trên Roblox để quick-switch preset |

## Yêu cầu

- Android 10+ (API 29)
- Android Studio Hedgehog hoặc mới hơn
- JDK 17
- (Tùy chọn) Thiết bị root với Magisk/KernelSU

## Build

```bash
# Clone project, mở Android Studio
# File > Open > chọn thư mục RobloxGFXTool

# Build debug APK
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Cách sử dụng

1. Cài APK và mở app
2. Cấp quyền **Quản lý tất cả file** (Settings > Apps > Special app access)
3. Mở Roblox ít nhất **một lần** để tạo thư mục config
4. Chỉnh các cài đặt theo ý muốn
5. Chọn chế độ **FILE** (không root) hoặc **ROOT** (có root)
6. Nhấn **Áp dụng Config**
7. Khởi động lại Roblox để áp dụng

## Kiến trúc

```
app/
├── data/
│   ├── model/
│   │   ├── GfxSettings.kt          # Data model + presets
│   │   └── RobloxConfigMapper.kt   # Convert → Roblox JSON (Fast Flags)
│   └── repository/
│       └── GfxRepository.kt        # Persistence + file/root apply
├── domain/
│   ├── RootCommandRunner.kt        # Root shell helper
│   └── RobloxDetector.kt           # Detect installed Roblox packages
├── service/
│   └── OverlayService.kt           # Foreground floating overlay
└── ui/
    ├── GfxViewModel.kt             # State management (MVI-lite)
    ├── AppNavHost.kt               # Navigation
    ├── theme/Theme.kt              # Dark minimalist theme
    ├── components/Components.kt    # Reusable Compose components
    └── screens/
        ├── MainScreen.kt           # Màn hình chính
        ├── AboutScreen.kt          # Hướng dẫn
        └── PermissionScreen.kt     # Hướng dẫn cấp quyền
```

## Config được sinh ra

App ghi `ClientAppSettings.json` vào:
- `Android/data/com.roblox.client/files/ClientAppSettings.json` (FILE mode)
- `/data/data/com.roblox.client/files/ClientAppSettings.json` (ROOT mode)

Các Fast Flags được sử dụng: `DFIntRenderResolutionScale`, `DFFlagRenderBloom`, `DFFlagRenderSunRays`, `DFIntParticleMaxNumParticles`, v.v.

## License

MIT — Dự án cá nhân, không liên kết với Roblox Corporation.
