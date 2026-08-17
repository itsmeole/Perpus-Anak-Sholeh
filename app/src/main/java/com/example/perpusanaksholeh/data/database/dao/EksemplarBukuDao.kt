package com.example.perpusanaksholeh.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku

@Dao
interface EksemplarBukuDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(eksemplar: EksemplarBuku): Long

    @Delete
    suspend fun delete(eksemplar: EksemplarBuku)

    @Query("SELECT * FROM eksemplar_buku WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): EksemplarBuku?

    @Query("SELECT * FROM eksemplar_buku WHERE buku_id = :bukuId ORDER BY barcode ASC")
    fun getByBukuId(bukuId: Int): LiveData<List<EksemplarBuku>>

    @Query("SELECT * FROM eksemplar_buku WHERE buku_id = :bukuId ORDER BY barcode ASC")
    suspend fun getByBukuIdSync(bukuId: Int): List<EksemplarBuku>

    @Query("UPDATE eksemplar_buku SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("SELECT COUNT(*) FROM eksemplar_buku WHERE barcode = :barcode")
    suspend fun countByBarcode(barcode: String): Int

    @Query("SELECT COUNT(*) FROM eksemplar_buku")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM eksemplar_buku WHERE id = :id")
    suspend fun getById(id: Int): EksemplarBuku?

    @Query("SELECT * FROM eksemplar_buku")
    suspend fun getAllSync(): List<EksemplarBuku>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eksemplarList: List<EksemplarBuku>)

    @Query("DELETE FROM eksemplar_buku")
    suspend fun deleteAll()
}
