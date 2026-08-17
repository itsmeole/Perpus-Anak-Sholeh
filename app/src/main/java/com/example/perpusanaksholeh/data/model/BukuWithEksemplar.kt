package com.example.perpusanaksholeh.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku

data class BukuWithEksemplar(
    @Embedded val buku: Buku,

    @Relation(
        parentColumn = "id",
        entityColumn = "buku_id"
    )
    val eksemplarList: List<EksemplarBuku>
) {
    val jumlahTotal: Int get() = eksemplarList.size
    val jumlahTersedia: Int get() = eksemplarList.count { it.status == EksemplarBuku.STATUS_TERSEDIA }
    val jumlahDipinjam: Int get() = eksemplarList.count { it.status == EksemplarBuku.STATUS_DIPINJAM }
}
