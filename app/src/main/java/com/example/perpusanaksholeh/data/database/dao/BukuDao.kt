package com.example.perpusanaksholeh.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.model.BukuWithEksemplar

@Dao
interface BukuDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(buku: Buku): Long

    @Update
    suspend fun update(buku: Buku)

    @Delete
    suspend fun delete(buku: Buku)

    @Query("SELECT * FROM buku ORDER BY judul ASC")
    fun getAll(): LiveData<List<Buku>>

    @Query("SELECT * FROM buku WHERE id = :id")
    suspend fun getById(id: Int): Buku?

    @Query("SELECT * FROM buku WHERE judul LIKE '%' || :query || '%' OR penulis LIKE '%' || :query || '%' OR kategori LIKE '%' || :query || '%' ORDER BY judul ASC")
    fun search(query: String): LiveData<List<Buku>>

    @Transaction
    @Query("SELECT * FROM buku ORDER BY judul ASC")
    fun getAllWithEksemplar(): LiveData<List<BukuWithEksemplar>>

    @Transaction
    @Query("SELECT * FROM buku WHERE id = :id")
    suspend fun getByIdWithEksemplar(id: Int): BukuWithEksemplar?

    @Transaction
    @Query("SELECT * FROM buku WHERE judul LIKE '%' || :query || '%' OR penulis LIKE '%' || :query || '%' OR kategori LIKE '%' || :query || '%' ORDER BY judul ASC")
    fun searchWithEksemplar(query: String): LiveData<List<BukuWithEksemplar>>

    @Query("SELECT COUNT(*) FROM buku")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM buku")
    suspend fun getAllSync(): List<Buku>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bukuList: List<Buku>)

    @Query("DELETE FROM buku")
    suspend fun deleteAll()
}
