package com.example.perpusanaksholeh.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buku")
data class Buku(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "judul")
    val judul: String,

    @ColumnInfo(name = "penulis")
    val penulis: String,

    @ColumnInfo(name = "penerbit")
    val penerbit: String,

    @ColumnInfo(name = "kategori")
    val kategori: String,

    @ColumnInfo(name = "tahun_terbit")
    val tahunTerbit: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
