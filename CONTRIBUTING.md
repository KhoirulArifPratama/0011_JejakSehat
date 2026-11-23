# Panduan Kontribusi

1. Buat perubahan kecil yang memiliki tujuan jelas.
2. Jalankan pengujian atau build yang relevan sebelum commit.
3. Gunakan pesan commit berbentuk `type: deskripsi`, misalnya `feat: add health history form`.
4. Jangan commit `local.properties`, `.env`, password, token, atau folder dependency.
5. Perbarui dokumentasi jika alur penggunaan atau struktur project berubah.
6. Untuk progres mingguan, gunakan commit kecil yang dapat ditinjau dan awali ringkasannya dengan konteks Week 2 jika relevan.
7. Sebelum pull request, jalankan `./gradlew test` dan pastikan backend dapat dimulai dengan konfigurasi `.env` lokal.
