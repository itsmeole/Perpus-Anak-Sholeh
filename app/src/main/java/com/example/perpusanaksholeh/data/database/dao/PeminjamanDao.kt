package com.example.perpusanaksholeh.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.model.PeminjamanDetail

@Dao
interface PeminjamanDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(peminjaman: Peminjaman): Long

    @Update
    suspend fun update(peminjaman: Peminjaman)

    @Query("""
        SELECT 
            p.id AS peminjaman_id,
            p.siswa_id,
            s.nama_siswa,
            s.nomor_kartu,
            p.eksemplar_id,
            e.barcode AS eksemplar_barcode,
            b.id AS buku_id,
            b.judul AS judul_buku,
            b.penulis,
            p.tanggal_pinjam,
            p.tanggal_jatuh_tempo,
            p.tanggal_kembali,
            p.status,
            p.denda
        FROM peminjaman p
        INNER JOIN siswa s ON p.siswa_id = s.id
        INNER JOIN eksemplar_buku e ON p.eksemplar_id = e.id
        INNER JOIN buku b ON e.buku_id = b.id
        ORDER BY p.tanggal_pinjam DESC
    """)
    fun getAllDetail(): LiveData<List<PeminjamanDetail>>

    @Query("""
        SELECT 
            p.id AS peminjaman_id,
            p.siswa_id,
            s.nama_siswa,
            s.nomor_kartu,
            p.eksemplar_id,
            e.barcode AS eksemplar_barcode,
            b.id AS buku_id,
            b.judul AS judul_buku,
            b.penulis,
            p.tanggal_pinjam,
            p.tanggal_jatuh_tempo,
            p.tanggal_kembali,
            p.status,
            p.denda
        FROM peminjaman p
        INNER JOIN siswa s ON p.siswa_id = s.id
        INNER JOIN eksemplar_buku e ON p.eksemplar_id = e.id
        INNER JOIN buku b ON e.buku_id = b.id
        WHERE p.status = 'dipinjam' OR p.status = 'terlambat'
        ORDER BY p.tanggal_jatuh_tempo ASC
    """)
    fun getAktifDetail(): LiveData<List<PeminjamanDetail>>

    @Query("""
        SELECT 
            p.id AS peminjaman_id,
            p.siswa_id,
            s.nama_siswa,
            s.nomor_kartu,
            p.eksemplar_id,
            e.barcode AS eksemplar_barcode,
            b.id AS buku_id,
            b.judul AS judul_buku,
            b.penulis,
            p.tanggal_pinjam,
            p.tanggal_jatuh_tempo,
            p.tanggal_kembali,
            p.status,
            p.denda
        FROM peminjaman p
        INNER JOIN siswa s ON p.siswa_id = s.id
        INNER JOIN eksemplar_buku e ON p.eksemplar_id = e.id
        INNER JOIN buku b ON e.buku_id = b.id
        WHERE p.status = 'dikembalikan'
        ORDER BY p.tanggal_kembali DESC
    """)
    fun getRiwayatDetail(): LiveData<List<PeminjamanDetail>>

    @Query("SELECT * FROM peminjaman WHERE eksemplar_id = :eksemplarId AND (status = 'dipinjam' OR status = 'terlambat') LIMIT 1")
    suspend fun getAktifByEksemplar(eksemplarId: Int): Peminjaman?

    @Query("SELECT * FROM peminjaman WHERE siswa_id = :siswaId AND (status = 'dipinjam' OR status = 'terlambat')")
    suspend fun getAktifBySiswa(siswaId: Int): List<Peminjaman>

    @Query("SELECT * FROM peminjaman WHERE id = :id")
    suspend fun getById(id: Int): Peminjaman?

    @Query("SELECT COUNT(*) FROM peminjaman WHERE status = 'dipinjam' OR status = 'terlambat'")
    fun getAktifCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM peminjaman WHERE status = 'terlambat'")
    fun getTerlambatCount(): LiveData<Int>

    @Query("SELECT * FROM peminjaman WHERE (status = 'dipinjam' OR status = 'terlambat') AND tanggal_jatuh_tempo < :currentTime")
    suspend fun getPeminjamanTerlambat(currentTime: Long): List<Peminjaman>

    @Query("""
        SELECT 
            p.id AS peminjaman_id,
            p.siswa_id,
            s.nama_siswa,
            s.nomor_kartu,
            p.eksemplar_id,
            e.barcode AS eksemplar_barcode,
            b.id AS buku_id,
            b.judul AS judul_buku,
            b.penulis,
            p.tanggal_pinjam,
            p.tanggal_jatuh_tempo,
            p.tanggal_kembali,
            p.status,
            p.denda
        FROM peminjaman p
        INNER JOIN siswa s ON p.siswa_id = s.id
        INNER JOIN eksemplar_buku e ON p.eksemplar_id = e.id
        INNER JOIN buku b ON e.buku_id = b.id
        WHERE p.eksemplar_id = :eksemplarId AND (p.status = 'dipinjam' OR p.status = 'terlambat')
        LIMIT 1
    """)
    suspend fun getAktifDetailByEksemplar(eksemplarId: Int): PeminjamanDetail?

    @Query("SELECT COUNT(*) FROM peminjaman WHERE siswa_id = :siswaId AND (status = 'dipinjam' OR status = 'terlambat')")
    suspend fun countAktifBySiswa(siswaId: Int): Int

    @Query("SELECT * FROM peminjaman")
    suspend fun getAllSync(): List<Peminjaman>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(peminjamanList: List<Peminjaman>)

    @Query("DELETE FROM peminjaman")
    suspend fun deleteAll()
}
