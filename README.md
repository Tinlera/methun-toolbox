<div align="center">

# 🔧 Methun Toolbox

**Android Power User Toolkit**

*Root & Shizuku destekli, gelişmiş Android araç kutusu*

[![Android](https://img.shields.io/badge/Android-13%2B-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-purple?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Private-red)]()

</div>

---

## 📖 Nedir?

**Methun Toolbox**, Android cihazınızda normalde ADB + PC gerektiren gelişmiş sistem ayarlarını doğrudan telefonunuzdan yapmanızı sağlayan bir araç kutusudur. Modern Material 3 arayüzü, üç katmanlı yetki sistemi ve 26+ araçla donatılmıştır.

> 🎯 **Hedef:** PC'ye bağlamadan, tek bir uygulamayla cihazınızı tam kontrol altına alın.

---

## ✨ Özellikler

### 🔑 Üç Katmanlı Yetki Sistemi

| Seviye | Gereksinim | Kapsam |
|--------|-----------|--------|
| 🟢 **Normal** | Yok | Cihaz bilgisi, temel ayarlar |
| 🟡 **Shizuku** | Shizuku uygulaması | ADB seviyesi komutlar (rootsuz!) |
| 🔴 **Root** | KernelSU / Magisk | Tam sistem erişimi |

---

### 📦 Uygulama Yönetimi

| Araç | Açıklama | Yetki |
|------|----------|-------|
| **Debloat Manager** | Sistem uygulamalarını listele, devre dışı bırak veya kaldır | Shizuku |
| **İzin Yöneticisi** | AppOps ile detaylı izin kontrolü (kamera, mikrofon, konum...) | Shizuku |
| **Sessiz APK Yükleyici** | Onay ekranı olmadan APK/Split APK yükleme | Shizuku |
| **Cache Temizleyici** | Tek uygulama veya toplu cache temizleme | Shizuku |

### ⚡ Performans

| Araç | Açıklama | Yetki |
|------|----------|-------|
| **ART Optimizer** | DEX derleme modları (speed, speed-profile, everything) | Shizuku |
| **CPU/GPU Governor** | Çekirdek bazlı governor, frekans ve GPU kontrolü | Root |
| **Termal Kontrol** | Termal zonlar, CPU/batarya sıcaklığı izleme | Root |

### 🌐 Ağ

| Araç | Açıklama | Yetki |
|------|----------|-------|
| **DNS Değiştirici** | Hazır presetler: Cloudflare, Google, AdGuard, NextDNS | Shizuku |
| **hosts Editörü** | Sistem hosts dosyası düzenleme, reklam engelleme listeleri | Root |

### 🛡️ Sistem

| Araç | Açıklama | Yetki |
|------|----------|-------|
| **build.prop Editörü** | Sistem özelliklerini oku/yaz/sil, yedek al | Root |
| **SELinux Toggle** | Enforcing ↔ Permissive geçişi | Root |
| **Partition Bilgisi** | Disk kullanımı, mount noktaları, partition detayları | Root |
| **Magisk/KSU Modülleri** | Modül listele, etkinleştir/devre dışı bırak | Root |
| **System App Manager** | /system/app ve /system/priv-app yönetimi | Root |

### 🔧 Doze & Pil

| Araç | Açıklama | Yetki |
|------|----------|-------|
| **Doze Manager** | Agresif doze, beyaz liste, zorla idle | Shizuku |
| **Gizli Ayar Kısayolları** | 18 gizli Android ayar sayfasına tek dokunuşla erişim | Normal |

### 🎨 Tweaks (Hızlı Ayarlar)

- ⏩ **Animasyon Hızı** — Pencere/geçiş animasyonları (0x → 10x)
- 🔌 **USB Hata Ayıklama** — ADB açma/kapama
- 🌙 **Always-on Display** — AOD kontrolü
- ⏱️ **Ekran Zaman Aşımı** — 15sn → 30dk
- 🧭 **Navigasyon Modu** — Gesture / 3 Buton
- 🔤 **Font Boyutu** — Sistem font ölçeği

---

## 📱 Ekran Görüntüleri

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   🏠 Ana Sayfa    │  │   🛠️ Araçlar     │  │   🔧 Tweaks      │
│                  │  │                  │  │                  │
│  ┌──────────┐   │  │  📦 Uygulama     │  │  ⏩ Animasyon 1x  │
│  │ POCO X7  │   │  │   ▸ Debloat      │  │  🔌 ADB     [ON] │
│  │ Pro      │   │  │   ▸ İzin Yön.    │  │  🌙 AOD    [OFF] │
│  │ Android  │   │  │   ▸ APK Yükle    │  │  ⏱ Timeout  30s  │
│  │ 14       │   │  │                  │  │  🧭 Gesture  [✓] │
│  └──────────┘   │  │  ⚡ Performans    │  │  🔤 Font   1.0x  │
│                  │  │   ▸ ART Opt.     │  │                  │
│  ⚡ Hızlı Erişim │  │   ▸ Governor     │  │                  │
│  [Debloat][DNS] │  │   ▸ Termal       │  │                  │
│  [ART] [Tweaks] │  │                  │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 🏗️ Mimari

```
com.tinlera.toolbox/
├── core/
│   ├── RootBridge.kt          # su -c komut çalıştırma
│   ├── ShizukuManager.kt      # Shizuku IPC (binder lifecycle)
│   └── DeviceInfo.kt          # Cihaz bilgi toplama
├── tools/
│   ├── shizuku/               # ADB seviyesi araçlar
│   │   ├── DebloatManager.kt
│   │   ├── ArtOptimizer.kt
│   │   ├── PermissionManager.kt
│   │   ├── DisplayManager.kt
│   │   ├── DozeManager.kt
│   │   ├── DnsChanger.kt
│   │   ├── IntentManager.kt
│   │   ├── CacheCleaner.kt
│   │   └── ApkInstaller.kt
│   ├── root/                  # Root seviyesi araçlar
│   │   ├── BuildPropEditor.kt
│   │   ├── HostsEditor.kt
│   │   ├── ThermalController.kt
│   │   ├── GovernorManager.kt
│   │   ├── SelinuxManager.kt
│   │   ├── PartitionManager.kt
│   │   ├── ModuleManager.kt
│   │   └── SystemAppManager.kt
│   └── tweaks/
│       └── TweakManager.kt    # Settings tweaks
└── ui/
    ├── navigation/            # Bottom Nav + NavGraph
    ├── screens/               # Ana ekranlar (Home, Tools, Tweaks, About)
    └── screens/tools/         # Araç detay ekranları
```

### Teknoloji Stack

| Bileşen | Teknoloji |
|---------|-----------|
| **Dil** | Kotlin 2.0.21 |
| **UI** | Jetpack Compose + Material 3 |
| **Tema** | Dynamic Color (Material You) |
| **Navigasyon** | Navigation Compose 2.8.5 |
| **Root** | `su -c` komut yürütme |
| **Shizuku** | Shizuku API 13.1.5 (Binder IPC) |
| **Min SDK** | 28 (Android 9+) |
| **Target SDK** | 35 (Android 15) |

---

## 🚀 Kurulum

### Gereksinimler

- Android 9+ (API 28)
- **Shizuku araçları için:** [Shizuku](https://shizuku.rikka.app/) uygulaması yüklü ve aktif
- **Root araçları için:** KernelSU veya Magisk ile root erişimi

### Yükleme

1. [Releases](https://github.com/Tinlera/methun-toolbox/releases) sayfasından APK'yı indirin
2. APK'yı yükleyin
3. Shizuku araçları için → Shizuku'yu başlatın
4. Root araçları için → Root izni verin

### Kaynak Koddan Derleme

```bash
git clone https://github.com/Tinlera/methun-toolbox.git
cd methun-toolbox
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Güvenlik

- **Açık kaynak** — tüm komutlar kaynak kodda görünür
- **Root/Shizuku izinleri** her araç başında açıkça belirtilir
- **Veri toplamaz** — analytics, tracker veya internet bağlantısı yok
- **Yerel çalışır** — tüm işlemler cihaz üzerinde gerçekleşir

---

## 📋 Yol Haritası

- [x] Proje altyapısı + Material 3 tema
- [x] Root Bridge + Shizuku entegrasyonu
- [x] 9 Shizuku aracı (Debloat, ART, DNS, Display, Doze, vb.)
- [x] 8 Root aracı (build.prop, hosts, governor, modules, vb.)
- [x] Tweaks paneli (animasyon, ADB, AOD, font, navigasyon)
- [x] 6 detay ekranı (Debloat, ART, DNS, build.prop, Intents, Modules)
- [ ] Kalan detay ekranları (Thermal, Governor, Hosts, vb.)
- [ ] Batch operasyonlar (toplu debloat profilleri)
- [ ] Profil kaydetme/yükleme
- [ ] Tasker/Automate entegrasyonu

---

## 🤝 Katkıda Bulunma

Şu an özel bir proje. İleride açık kaynak yapılabilir.

---

## 📄 Lisans

Bu proje şu an özel lisans altındadır.

---

<div align="center">

**Methun Toolbox** ile cihazınızın tam kontrolünü elinize alın. 🚀

*POCO X7 Pro (rodin) üzerinde geliştirildi ve test edildi.*

</div>
