# Perpus Anak Sholeh 📚✨

**Perpus Anak Sholeh** adalah aplikasi manajemen perpustakaan Android Native modern yang dirancang khusus untuk mempermudah operasional pustakawan dalam mengelola data buku, data eksemplar (copy fisik), pendaftaran pengunjung (siswa & umum), pencatatan transaksi peminjaman & pengembalian, serta pencetakan kartu anggota secara digital.

Aplikasi ini dibangun menggunakan bahasa **Kotlin** dengan menerapkan arsitektur **MVVM (Model-View-ViewModel)** serta standar desain **Material Design 3** bertema warna Teal yang flat, bersih, dan modern.

---

## 🚀 Fitur Utama

### 1. 💳 Ekspor Kartu PNG (Landscape Card)
*   **Kartu Anggota Pengunjung**: Mengekspor kartu digital anggota dengan layout landscape profesional (`500dp` x `300dp`) yang berisi nama, kelas/kategori usia, instansi, nomor kartu, dan gambar barcode unik langsung ke folder `Downloads` perangkat.
*   **Kartu Buku/Eksemplar**: Mengekspor kartu rincian buku induk (judul, penulis, penerbit, kategori, tahun terbit) bersanding dengan barcode eksemplar fisik di sisi kanan kartu.
*   **Density-Based Rendering**: Didukung oleh sistem pengonversi dimensi kerapatan layar dinamis (*dpi to pixel conversion*) untuk menjamin gambar kartu tetap tajam, presisi, dan teks tidak akan terpotong pada semua tipe resolusi layar handphone.

### 2. 🔍 Pemindai Barcode Cerdas (ML Kit & CameraX)
*   **Target Area Overlay & Laser Animasi**: Tampilan antarmuka pemindaian dilengkapi overlay hitam transparan dengan bidikan kotak hijau/teal menyala di tengah serta garis laser merah/teal yang bergerak naik-turun sebagai indikator pemindaian aktif.
*   **Continuous Multi-Scanning**: Mode scan berkelanjutan dengan *haptic feedback* (getaran) dan toast notifikasi instan. Dilengkapi cooldown pengaman agar tidak mendeteksi barcode yang sama berulang kali.
*   **Proses Batch Atomik**:
    *   **Tambah Banyak Eksemplar**: Pengguna dapat memindai puluhan barcode eksemplar buku baru sekaligus dan menyimpannya secara massal dalam satu klik.
    *   **Peminjaman Banyak Buku**: Memungkinkan peminjaman lebih dari 1 buku sekaligus dalam satu transaksi peminjaman.

### 3. 👥 Manajemen Pengunjung Terpadu
*   Mendukung dua tipe pengunjung:
    *   **Siswa**: Mencakup informasi nama, kelas, nomor kartu, dan barcode.
    *   **Umum**: Mencakup nama, kategori rentang usia (Anak-anak, Remaja, Dewasa, Lansia), alamat, pekerjaan, instansi, nomor kartu, dan barcode.
*   Pencarian instan menggunakan filter tabbed view.

### 4. 🔄 Sistem Backup & Restore Mandiri
*   Mengekspor seluruh database (data buku, eksemplar, siswa, peminjaman) ke dalam satu file berkode **JSON** terenkripsi ringan melalui Android Storage Access Framework (SAF).
*   Memulihkan (Restore) database kapan saja dengan memilih berkas JSON cadangan yang sudah disimpan sebelumnya.

### 5. ⏰ Pengingat Keterlambatan Otomatis (WorkManager)
*   Menggunakan Android `WorkManager` background scheduler yang berjalan secara berkala setiap hari untuk mendeteksi transaksi peminjaman yang melewati batas durasi pengembalian.
*   Mengirim notifikasi sistem secara otomatis jika terdeteksi buku yang terlambat dikembalikan beserta perhitungan dendanya.

### 6. 🎨 Desain Flat & Immersive Modern
*   **Tema Kasir Teal & Flat Cards**: Menggunakan palet aksen warna Teal (`#2B7D77`), abu-abu muda, dan putih. Semua komponen `CardView` tidak menggunakan bayangan (*elevation="0dp"*) melainkan menggunakan stroke/border tipis transparan agar terlihat minimalis dan modern.
*   **Forced Light Mode**: Aplikasi dikunci khusus pada tema terang (light mode) untuk menghindari bug tata letak visual saat sistem ponsel diubah ke mode gelap.
*   **Greeting Header Dinamis**: Judul toolbar utama menyambut pengguna dengan ucapan *"Selamat Pagi/Siang/Sore/Malam, [Nama User]!"* secara real-time yang berganti sesuai waktu jam lokal perangkat.
*   **BottomSheet Dialog**: Pengisian form tambah buku & pengunjung tampil elegan meluncur dari bawah.
*   **Floating Bottom Navigation**: Menu utama melayang dengan sudut membulat di bagian bawah layar.

---

## 🛠️ Stack Teknologi

*   **Platform**: Android (Min SDK 21, Target SDK 34)
*   **Bahasa Pemrograman**: Kotlin
*   **Arsitektur**: MVVM (Model-View-ViewModel) dengan Jetpack Architecture Components
*   **Database**: Room Database (SQLite ORM) dengan KSP Compiler
*   **Thread & Asynchronous**: Kotlin Coroutines & LiveData
*   **Desain UI**: Material Design 3, View Binding, XML layouts
*   **Navigasi**: Jetpack Navigation Component
*   **Engine Barcode**: Google ML Kit Barcode Scanning & CameraX Jetpack library
*   **Pembuat Barcode**: ZXing (Zebra Crossing) Library
*   **Background Task**: Android WorkManager API
*   **Ekspor Gambar**: Canvas Drawing & Android MediaStore API (Scoped Storage compliant)

---

## 📂 Struktur Folder Proyek

```
app/src/main/java/com/example/perpusanaksholeh/
│
├── data/
│   ├── database/       # Room Database, Entity (Buku, Siswa, Peminjaman), DAO
│   ├── model/          # DTO/Relation Classes (BukuWithEksemplar, PeminjamanDetail)
│   └── repository/     # Data Layer / Repository (Buku, Siswa, Peminjaman)
│
├── ui/
│   ├── dashboard/      # Main Dashboard Fragment & ViewModel
│   ├── siswa/          # Management Pengunjung (List, Detail, Form, Adapter)
│   ├── buku/           # Management Buku & Eksemplar (List, Detail, Form, Adapter)
│   ├── peminjaman/     # Transaksi Pinjam & Kembali (Fragment, ViewModel, Adapter)
│   ├── scanner/        # Pemindai Barcode (ScannerActivity & ML Kit Analyzer)
│   └── profile/        # Detail Profile User & SharedPreferences
│
├── worker/             # Background Worker (LateReturnWorker & NotificationHelper)
│
└── util/               # File Utilitas (BarcodeGenerator, ImageExportHelper, Resource)
```

---

## ⚙️ Cara Menjalankan Aplikasi

1.  **Clone / Download** repositori ini ke dalam direktori lokal Anda.
2.  Buka proyek menggunakan **Android Studio** (Koala atau versi yang lebih baru direkomendasikan).
3.  Biarkan Gradle melakukan sinkronisasi dependensi hingga selesai (*Gradle Sync*).
4.  Hubungkan perangkat Android fisik Anda via USB Debugging / Wifi Debugging.
5.  Klik tombol **Run** (`Shift + F10`) di Android Studio untuk melakukan instalasi APK secara langsung ke perangkat.
6.  *Catatan: Pastikan izin akses kamera diberikan saat aplikasi pertama kali meminta izin pemindaian barcode.*
