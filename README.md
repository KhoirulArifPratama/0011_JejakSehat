# JejakSehat

Aplikasi Android Jetpack Compose untuk mengelola rekam medis keluarga.

## Roadmap Per Minggu

### Week 1 — Fondasi Project

Week 1 menyiapkan struktur Android, backend Express, skema database awal, autentikasi JWT, serta dokumentasi kontribusi.

### Week 2 — Penguatan dan Integrasi

Week 2 melanjutkan fondasi dengan fokus pada dokumentasi API, konfigurasi lingkungan yang aman, validasi data, pengujian, dan kesiapan integrasi Android–backend.

- [x] Penataan roadmap dan status implementasi
- [x] Konfigurasi environment backend terdokumentasi
- [x] Kontrak endpoint API terdokumentasi
- [x] Validasi input dan pengujian diperkuat
- [ ] Integrasi penuh sinkronisasi data Android–backend
- [ ] Pengujian end-to-end dengan MySQL aktif

## Status Implementasi

- [x] Jetpack Compose + Material 3
- [x] Navigation Compose
- [x] Login/register dasar dan role Pasien/Admin
- [x] Dashboard role-aware
- [x] CRUD anggota keluarga dasar
- [x] CRUD fasilitas kesehatan dasar
- [x] CRUD riwayat kesehatan dasar
- [x] CRUD kunjungan dasar
- [x] Pencarian pada data utama
- [x] Build debug berhasil
- [ ] Backend REST API Express
- [ ] JWT access token dan refresh token server
- [ ] Database MySQL
- [ ] Retrofit dan interceptor token
- [ ] DataStore token persistence
- [ ] Penyimpanan dan pemilihan gambar dokumen medis
- [x] Backend Express + skema MySQL awal
- [x] Endpoint JWT register/login/refresh/logout/me
- [x] Endpoint anggota dan faskes dengan otorisasi role

## Menjalankan

Buka folder ini di Android Studio, tunggu Gradle sync, kemudian jalankan `app` pada emulator Android API 26+.

APK debug:

`app/build/outputs/apk/debug/app-debug.apk`

## Akun Demo

- Pasien: gunakan email biasa yang valid dan password minimal 8 karakter.
- Admin: gunakan email yang mengandung kata `admin` dan password minimal 8 karakter.

## Backend API

Backend berada di folder `backend/`.

```bash
cd backend
npm install
copy .env.example .env
```

Backend berada di folder `backend/` pada root project. Buat database MySQL dengan menjalankan `backend/schema.sql`, isi nilai `backend/.env`, lalu jalankan perintah berikut dari folder `backend/`:

```bash
npm run seed:admin
npm start
```

Seeder membuat akun Admin tanpa register publik. Default akun demo:

```text
Email: admin@jejaksehat.local
Password: Admin12345
```

Base URL API: `http://localhost:3000/api`

Endpoint yang tersedia:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `GET|POST /anggota`
- `GET|POST /faskes` (POST hanya Admin)

Android emulator memakai `http://10.0.2.2:3000/api` untuk mengakses backend di komputer host.

> Android saat ini masih memiliki data demo lokal sebagai fallback. Integrasi Retrofit akan dilakukan setelah alamat backend dipastikan saat demo.
