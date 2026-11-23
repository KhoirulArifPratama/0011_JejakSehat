# Checklist Demo JejakSehat

## Persiapan

- [ ] Jalankan MySQL.
- [ ] Jalankan `backend/schema.sql`.
- [ ] Isi `backend/.env`.
- [ ] Jalankan `npm install` pada folder `backend`.
- [ ] Jalankan `npm run seed:admin`.
- [ ] Jalankan `npm start`.
- [ ] Hubungkan emulator ke backend dengan `adb reverse tcp:3000 tcp:3000`.
- [ ] Jalankan APK pada emulator Android API 26+.

## Alur Pasien

- [ ] Register akun Pasien.
- [ ] Login Pasien.
- [ ] Tunjukkan role Pasien pada Profil.
- [ ] Tambah anggota keluarga.
- [ ] Cari anggota berdasarkan nama.
- [ ] Hapus anggota dengan dialog konfirmasi.
- [ ] Tambah riwayat penyakit/alergi.
- [ ] Cari riwayat.
- [ ] Tambah kunjungan.
- [ ] Cari kunjungan.
- [ ] Pilih dokumen JPG/PNG.
- [ ] Tunjukkan metadata nama dan ukuran dokumen.
- [ ] Logout.

## Alur Admin

- [ ] Login menggunakan akun hasil seeder.
- [ ] Tunjukkan role Admin.
- [ ] Tambah faskes.
- [ ] Cari faskes berdasarkan nama/kota.
- [ ] Ubah atau hapus faskes dari API/Admin.
- [ ] Tunjukkan bahwa menu data medis Pasien tidak tersedia untuk Admin.

## Poin Teknis Wawancara

- Compose-only untuk UI.
- MVVM dengan ViewModel dan StateFlow.
- Navigation Compose untuk routing.
- Retrofit + Gson untuk REST API.
- DataStore untuk token.
- JWT Access Token dan Refresh Token.
- MySQL sebagai database utama, bukan Room.
- File dokumen disimpan di internal storage perangkat.
- Role dan kepemilikan data diperiksa backend.
