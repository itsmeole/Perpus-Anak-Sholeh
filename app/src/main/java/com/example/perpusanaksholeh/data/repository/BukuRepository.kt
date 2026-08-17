package com.example.perpusanaksholeh.data.repository

import androidx.lifecycle.LiveData
import com.example.perpusanaksholeh.data.database.dao.BukuDao
import com.example.perpusanaksholeh.data.database.dao.EksemplarBukuDao
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.model.BukuWithEksemplar

class BukuRepository(
    private val bukuDao: BukuDao,
    private val eksemplarBukuDao: EksemplarBukuDao
) {

    val allBukuWithEksemplar: LiveData<List<BukuWithEksemplar>> = bukuDao.getAllWithEksemplar()
    val bukuCount: LiveData<Int> = bukuDao.getCount()
    val totalEksemplar: LiveData<Int> = eksemplarBukuDao.getTotalCount()

    fun searchBuku(query: String): LiveData<List<BukuWithEksemplar>> {
        return bukuDao.searchWithEksemplar(query)
    }

    suspend fun insertBuku(buku: Buku): Long {
        return bukuDao.insert(buku)
    }

    suspend fun updateBuku(buku: Buku) {
        bukuDao.update(buku)
    }

    suspend fun deleteBuku(buku: Buku) {
        bukuDao.delete(buku)
    }

    suspend fun getBukuById(id: Int): Buku? {
        return bukuDao.getById(id)
    }

    suspend fun getBukuWithEksemplarById(id: Int): BukuWithEksemplar? {
        return bukuDao.getByIdWithEksemplar(id)
    }

    // Eksemplar operations
    suspend fun insertEksemplar(eksemplar: EksemplarBuku): Long {
        return eksemplarBukuDao.insert(eksemplar)
    }

    suspend fun deleteEksemplar(eksemplar: EksemplarBuku) {
        eksemplarBukuDao.delete(eksemplar)
    }

    suspend fun getEksemplarByBarcode(barcode: String): EksemplarBuku? {
        return eksemplarBukuDao.getByBarcode(barcode)
    }

    fun getEksemplarByBukuId(bukuId: Int): LiveData<List<EksemplarBuku>> {
        return eksemplarBukuDao.getByBukuId(bukuId)
    }

    suspend fun isEksemplarBarcodeExists(barcode: String): Boolean {
        return eksemplarBukuDao.countByBarcode(barcode) > 0
    }

    suspend fun getEksemplarById(id: Int): EksemplarBuku? {
        return eksemplarBukuDao.getById(id)
    }
}
