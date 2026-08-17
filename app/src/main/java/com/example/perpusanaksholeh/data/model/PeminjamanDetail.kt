package com.example.perpusanaksholeh.data.model

import androidx.room.ColumnInfo

/**
 * Custom query result untuk detail peminjaman.
 * Menggabungkan data dari tabel peminjaman, siswa, eksemplar_buku, dan buku.
 */
data class PeminjamanDetail(
    @ColumnInfo(name = "peminjaman_id")
    val peminjamanId: Int,

    @ColumnInfo(name = "siswa_id")
    val siswaId: Int,

    @ColumnInfo(name = "nama_siswa")
    val namaSiswa: String,

    @ColumnInfo(name = "nomor_kartu")
    val nomorKartu: String,

    @ColumnInfo(name = "eksemplar_id")
    val eksemplarId: Int,

    @ColumnInfo(name = "eksemplar_barcode")
    val eksemplarBarcode: String,

    @ColumnInfo(name = "buku_id")
    val bukuId: Int,

    @ColumnInfo(name = "judul_buku")
    val judulBuku: String,

    @ColumnInfo(name = "penulis")
    val penulis: String,

    @ColumnInfo(name = "tanggal_pinjam")
    val tanggalPinjam: Long,

    @ColumnInfo(name = "tanggal_jatuh_tempo")
    val tanggalJatuhTempo: Long,

    @ColumnInfo(name = "tanggal_kembali")
    val tanggalKembali: Long?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "denda")
    val denda: Int
)
