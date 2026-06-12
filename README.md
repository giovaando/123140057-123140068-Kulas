# 🚂 RailLog Nusantara - Manajemen Logistik Kereta Api

> **Aplikasi Logistik Manufaktur Kereta Api Berbasis Adaptive UI dengan Dukungan Kecerdasan Buatan untuk Efisiensi Rantai Pasok dan Verifikasi Dokumen Teknis**

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-brightgreen?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/AI-Google%20Gemini-4285F4?style=for-the-badge&logo=google" />
  <img src="https://img.shields.io/badge/Sprint-4%20%E2%9C%85-success?style=for-the-badge" />
</p>

---

## 📌 Identitas Proyek

| Item | Detail |
|------|--------|
| **Nama Aplikasi** | RailLog Nusantara |
| **Mata Kuliah** | Pengembangan Aplikasi Mobile (IF25-22017) |
| **Program Studi** | Teknik Informatika |
| **Institusi** | Institut Teknologi Sumatera (ITERA) |
| **Dosen Pengampu** | Pak Habib (mh4Scripts) |

---

## 🎥 Dokumentasi Demo

Silakan lihat demonstrasi fitur utama aplikasi kami di bawah ini:

### Video Demo
*   [**Tonton Demo Lengkap di YouTube**]([https://youtu.be/KjUJj9yNHTE])

### Screenshots

| Dashboard Staff  | Pengajuan Material (AI) | Dashboard Admin |
|:----------------:| :---: | :---: |
| ![Dashboard]([]) | ![AI Validation]([LINK_SS_2]) | ![Admin View]([LINK_SS_3]) |

---

## 👥 Tim Pengembang

| NIM | Nama | Peran |
|-----|------|-------|
| 123140057 | Muhammad Nurikhsan | Domain Layer, Database, API Integration |
| 123140068 | Giovan Lado | Presentation Layer, UI/UX, Navigation |

> **Branch:** `project/123140057-123140068-RailLog`

---

## 🎯 Deskripsi Aplikasi

**RailLog Nusantara** adalah aplikasi mobile lintas platform (Android & iOS) yang dirancang untuk mendukung operasional logistik pada industri manufaktur kereta api. Aplikasi ini mengintegrasikan kecerdasan buatan berbasis Google Gemini untuk membantu tim logistik dalam:

- **Memantau rantai pasok** komponen dan suku cadang kereta api secara real-time
- **Memverifikasi dokumen teknis** menggunakan AI Form Validator otomatis (Sprint 4)
- **Mengelola inventaris** dengan alur Requisition Wizard 5-langkah yang bersih
- **Menganalisis prioritas pengadaan** dengan bantuan Contextual AI Assistant

Aplikasi ini dibangun dengan pendekatan **Adaptive UI** yang menyesuaikan tampilan berdasarkan peran pengguna (Staff Gudang dan Admin Logistik).

---

## ✨ Fitur Utama

### Sprint 1 (Foundation) — ✅ Selesai
- [x] Setup project Kotlin Multiplatform
- [x] Arsitektur Clean Architecture + MVVM terdefinisi
- [x] Domain model `SupplyItem` dan `TechnicalDocument`
- [x] SQLDelight schema untuk penyimpanan lokal
- [x] Repository interfaces terdefinisi
- [x] Koin Dependency Injection aktif
- [x] Build berhasil tanpa error

### Sprint 2 (Core Features) — ✅ Selesai
- [x] Dashboard ringkasan status rantai pasok
- [x] Form input item suku cadang baru (CRUD)
- [x] Daftar komponen dengan filter status & kategori
- [x] Penyimpanan lokal dengan SQLDelight
- [x] Navigasi antar screen (Staff & Admin)

### Sprint 3 (Advanced) — ✅ Selesai
- [x] Verifikasi dokumen teknis via AI (Gemini)
- [x] Pencarian komponen dengan debounce 300ms
- [x] Filter berdasarkan kategori subsistem kereta
- [x] Notifikasi komponen kritis
- [x] Offline-first support dengan Auto-save Draft

### Sprint 4 (AI & Polish) — ✅ Selesai (Saat Ini)
- [x] Refactor Requisition Wizard (Alur 5-langkah bersih)
- [x] Implementasi AI Form Validator Otomatis di sisi Admin
- [x] Contextual AI Assistant (Membaca data stok gudang)
- [x] UI Polish: Font Geist & Branding Navy Blue
- [x] Unit tests (✅ 12 Unit Tests Passed)

### Sprint 5 (Final) — ✅ Selesai
- [x] Contextual AI Assistant terhubung ke navigasi (Staff & Admin)
- [x] Cleanup dead routes (Home, AddSupply, dll)
- [x] Technical Doc viewer di Supply Detail
- [x] Refactor unit tests → 19 test valid (menguji kode production langsung)
- [x] Dokumentasi & demo script

---

## 🏗️ Arsitektur & Teknologi

### Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌───────────────┐        ┌───────────────┐                 │
│  │    Screen     │◄──────►│   ViewModel   │                 │
│  │  (Composable) │ State  │  (StateFlow)  │                 │
│  └───────────────┘        └───────┬───────┘                 │
└───────────────────────────────────┼─────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────┘
│                      DOMAIN LAYER │                          │
│                    ┌──────────────▼──────────────┐          │
│                    │  Use Cases (Business Logic) │          │
│                    └──────────────┬──────────────┘          │
│                    ┌──────────────▼──────────────┐          │
│                    │    Repository Interface     │          │
│                    └──────────────┬──────────────┘          │
└───────────────────────────────────┼─────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────┐
│                       DATA LAYER  │                          │
│                    ┌──────────────▼──────────────┐          │
│                    │  Repository Implementation  │          │
│                    └──────────────┬──────────────┘          │
│         ┌──────────────────────┬──┴───────────────────┐     │
│   ┌─────▼──────┐        ┌──────▼─────┐       ┌────────▼───┐ │
│   │ SQLDelight │        │    Ktor    │       │ DataStore  │ │
│   │  (Lokal)  │        │  (Remote) │       │  (Prefs)   │ │
│   └───────────┘        └───────────┘       └────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🎨 Design System Tokens

- Colors: Primary Navy (#00236F), Success Emerald (#10B981), Surface Slate (#F7F9FB).
- Typography: Geist (UI) dan Geist Mono (Technical Data/Tracking ID).
- Grid: Strict 4px baseline grid untuk presisi industrial.

### Tech Stack

| Layer | Teknologi |
|-------|-----------|
| **UI** | Compose Multiplatform, Material 3 |
| **State Management** | StateFlow, ViewModel |
| **Navigation** | Compose Navigation (Type-safe) |
| **Networking** | Ktor Client |
| **Local Database** | SQLDelight |
| **Preferences** | DataStore |
| **Dependency Injection** | Koin 4.x |
| **AI Integration** | Google Gemini API (gemini-2.0-flash) |
| **Testing** | Kotlin Test, Turbine |
| **Platform** | Kotlin Multiplatform (Android + iOS) |

---

## 📁 Struktur Project

```
composeApp/src/
├── commonMain/kotlin/com/example/raillog/
│   ├── core/
│   │   ├── di/                     # Koin modules
│   │   ├── network/                # Ktor client, API config
│   │   └── util/                   # Extensions, helpers
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── entity/             # SQLDelight mappers
│   │   │   └── datastore/          # Preferences
│   │   ├── remote/
│   │   │   ├── api/                # GeminiService
│   │   │   └── dto/                # Request/Response DTOs
│   │   └── repository/             # Repository implementations
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── SupplyItem.kt       # Model suku cadang kereta
│   │   │   └── TechnicalDocument.kt # Model dokumen teknis
│   │   ├── repository/
│   │   │   ├── SupplyRepository.kt
│   │   │   └── AIRepository.kt
│   │   └── usecase/                # Business logic
│   │
│   └── presentation/
│       ├── navigation/             # Routes & NavHost
│       ├── screens/
│       │   ├── staff_main/         # Dashboard & Status
│       │   ├── admin_main/         # Verification Queue
│       │   ├── requisition/        # 5-Step Wizard
│       │   └── ai/                 # Contextual Assistant
│       ├── components/             # Komponen UI reusable
│       └── theme/                  # Material theme (Geist Font)
```

---

## 🚀 Cara Menjalankan

### Setup

**1. Setup `local.properties`**
```bash
cp local.properties.example local.properties
```

Edit `local.properties` (Masukkan API Key Gemini):
```properties
GEMINI_API_KEY=your_api_key_here
```

**2. Sync & Build**
Gunakan **JDK 17** untuk Gradle. Jalankan:
```bash
./gradlew :composeApp:assembleDebug
```

---

## 🧪 Validasi & Testing

Proyek ini telah divalidasi dengan rangkaian unit test menyeluruh (19 skenario) untuk memastikan integritas logika bisnis.

```bash
# Jalankan unit test
./gradlew :composeApp:testDebugUnitTest
```

**Cakupan Test:**
- ✅ Validasi Login (Admin/Staff Role Priority)
- ✅ Validasi Regex Project Code (`[TYPE]-[REGION]-[CODE]`)
- ✅ Logika `canSubmit` pada Requisition Wizard (Signature & Items check)
- ✅ Filter Antrian Verifikasi Admin (Search & Status)
- ✅ Pemetaan Data (Enum Mapping) Database
- ✅ Filter Item Kritis untuk Notifikasi

---

## 📅 Timeline Sprint

| Sprint | Minggu | Status | Target |
|--------|--------|--------|--------|
| Sprint 1: Foundation | 11 | ✅ Selesai | Setup, arsitektur, domain model |
| Sprint 2: Core Features | 12 | ✅ Selesai | CRUD, navigasi, local storage |
| Sprint 3: Advanced | 13 | ✅ Selesai | Search, AI integration, offline |
| Sprint 4: Polish | 14 | ✅ Selesai | Testing, bug fix, UI polish |
| Sprint 5: Final | 15 | ✅ Selesai | Demo UAS, Dokumentasi, Validasi |

---

## 🔧 Troubleshooting

| Masalah | Solusi |
|---------|--------|
| `BorderStroke` error | Pastikan import `androidx.compose.foundation.BorderStroke` sudah ada |
| AI Result Kosong | Pastikan `local.properties` sudah berisi API Key yang valid |
| SQLDelight error | Jalankan Gradle Sync untuk men-generate class database |

---

*Proyek ini dikembangkan sebagai bagian dari mata kuliah Pengembangan Aplikasi Mobile — Institut Teknologi Sumatera (ITERA)*
