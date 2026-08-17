package com.example.perpusanaksholeh.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "peminjaman",
    foreignKeys = [
        ForeignKey(
            entity = Siswa::class,
            parentColumns = ["id"],
            childColumns = ["siswa_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EksemplarBuku::class,
            parentColumns = ["id"],
            childColumns = ["eksemplar_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["siswa_id"]),
        Index(value = ["eksemplar_id"])
    ]
)
data class Peminjaman(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "siswa_id")
    val siswaId: Int,

    @ColumnInfo(name = "eksemplar_id")
    val eksemplarId: Int,

    @ColumnInfo(name = "tanggal_pinjam")
    val tanggalPinjam: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "tanggal_jatuh_tempo")
    val tanggalJatuhTempo: Long,

    @ColumnInfo(name = "tanggal_kembali")
    val tanggalKembali: Long? = null,

    @ColumnInfo(name = "status")
    val status: String = STATUS_DIPINJAM,

    @ColumnInfo(name = "denda")
    val denda: Int = 0
) {
    companion object {
        const val STATUS_DIPINJAM = "dipinjam"
        const val STATUS_DIKEMBALIKAN = "dikembalikan"
        const val STATUS_TERLAMBAT = "terlambat"
        const val DENDA_PER_HARI = 500 // Rp 500 per hari
        const val DURASI_DEFAULT_HARI = 7 // 7 hari
    }
}
