package com.example.perpusanaksholeh.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.perpusanaksholeh.data.database.entity.Siswa

@Dao
interface SiswaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(siswa: Siswa): Long

    @Update
    suspend fun update(siswa: Siswa)

    @Delete
    suspend fun delete(siswa: Siswa)

    @Query("SELECT * FROM siswa ORDER BY nama_siswa ASC")
    fun getAll(): LiveData<List<Siswa>>

    @Query("SELECT * FROM siswa WHERE id = :id")
    suspend fun getById(id: Int): Siswa?

    @Query("SELECT * FROM siswa WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): Siswa?

    @Query("SELECT * FROM siswa WHERE nama_siswa LIKE '%' || :query || '%' OR barcode = :query OR nomor_kartu = :query ORDER BY nama_siswa ASC")
    suspend fun searchSiswaSync(query: String): List<Siswa>

    @Query("SELECT * FROM siswa WHERE nama_siswa LIKE '%' || :query || '%' OR nomor_kartu LIKE '%' || :query || '%' ORDER BY nama_siswa ASC")
    fun search(query: String): LiveData<List<Siswa>>

    @Query("SELECT COUNT(*) FROM siswa")
    fun getCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM siswa WHERE barcode = :barcode")
    suspend fun countByBarcode(barcode: String): Int

    @Query("SELECT COUNT(*) FROM siswa WHERE barcode = :barcode AND id != :excludeId")
    suspend fun countByBarcodeExcluding(barcode: String, excludeId: Int): Int

    @Query("SELECT * FROM siswa")
    suspend fun getAllSync(): List<Siswa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(siswaList: List<Siswa>)

    @Query("DELETE FROM siswa")
    suspend fun deleteAll()

    @Query("SELECT * FROM siswa WHERE tipe = :tipe ORDER BY nama_siswa ASC")
    fun getByType(tipe: String): LiveData<List<Siswa>>

    @Query("SELECT * FROM siswa WHERE tipe = :tipe AND (nama_siswa LIKE '%' || :query || '%' OR nomor_kartu LIKE '%' || :query || '%') ORDER BY nama_siswa ASC")
    fun searchByType(tipe: String, query: String): LiveData<List<Siswa>>
}
