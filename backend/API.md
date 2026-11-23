# Kontrak API JejakSehat

Base URL: `http://localhost:3000/api`

## Autentikasi

- `POST /auth/register` — membuat akun pasien.
- `POST /auth/login` — mengembalikan access token dan refresh token.
- `POST /auth/refresh` — memperbarui access token.
- `POST /auth/logout` — mengakhiri sesi aktif.
- `GET /auth/me` — membaca profil pengguna terautentikasi.

Endpoint selain register/login memerlukan header `Authorization: Bearer <access_token>`.

## Data Pengguna

- `GET|POST|PUT|DELETE /anggota` — data anggota keluarga milik pengguna.
- `GET|POST|PUT|DELETE /riwayat` — riwayat kesehatan anggota keluarga.
- `GET|POST|PUT|DELETE /kunjungan` — catatan kunjungan kesehatan.
- `GET|POST|PUT|DELETE /dokumen` — metadata dokumen medis.
- `GET|POST|PUT|DELETE /faskes` — fasilitas kesehatan; perubahan hanya untuk Admin.

Endpoint daftar mendukung query `q` untuk pencarian. Kepemilikan data dan role diperiksa oleh backend.
