package com.example.perpusanaksholeh.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eksemplar_buku",
    foreignKeys = [
        ForeignKey(
            entity = Buku::class,
            parentColumns = ["id"],
            childColumns = ["buku_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["buku_id"])
    ]
)
data class EksemplarBuku(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "buku_id")
    val bukuId: Int,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "status")
    val status: String = STATUS_TERSEDIA
) {
    companion object {
        const val STATUS_TERSEDIA = "tersedia"
        const val STATUS_DIPINJAM = "dipinjam"
    }
}
