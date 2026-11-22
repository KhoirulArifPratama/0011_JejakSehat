# Landasan Arsitektur

## Komponen

- **Android app**: Kotlin, Jetpack Compose, Material 3, dan Navigation Compose.
- **Backend**: Node.js dan Express sebagai REST API.
- **Database**: MySQL dengan skema pada `backend/schema.sql`.
- **Authentication**: JWT access token dan refresh token.

## Alur awal

1. Pengguna membuka aplikasi Android.
2. Pengguna melakukan login atau register.
3. Aplikasi menampilkan dashboard sesuai role Pasien atau Admin.
4. Data keluarga, fasilitas kesehatan, dan riwayat kesehatan dikelola melalui fitur masing-masing.
5. Backend menjadi lapisan integrasi untuk autentikasi dan data persisten.

## Prinsip pengembangan

- Fitur dikembangkan bertahap dan diuji sebelum integrasi berikutnya.
- Informasi sensitif tidak disimpan di repository; gunakan `.env` lokal.
- Perubahan besar dicatat pada README dan commit dengan pesan yang spesifik.
