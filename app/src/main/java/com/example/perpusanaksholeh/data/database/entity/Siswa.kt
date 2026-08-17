package com.example.perpusanaksholeh.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "siswa",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class Siswa(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nomor_kartu")
    val nomorKartu: String,

    @ColumnInfo(name = "nama_siswa")
    val namaSiswa: String,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "tipe")
    val tipe: String = "siswa", // "siswa" atau "umum"

    @ColumnInfo(name = "kelas")
    val kelas: String? = null,

    @ColumnInfo(name = "usia_range")
    val usiaRange: String? = null,

    @ColumnInfo(name = "alamat")
    val alamat: String? = null,

    @ColumnInfo(name = "pekerjaan")
    val pekerjaan: String? = null,

    @ColumnInfo(name = "instansi")
    val instansi: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
