package com.example.perpusanaksholeh.data.repository

import androidx.lifecycle.LiveData
import com.example.perpusanaksholeh.data.database.dao.SiswaDao
import com.example.perpusanaksholeh.data.database.entity.Siswa

class SiswaRepository(private val siswaDao: SiswaDao) {

    val allSiswa: LiveData<List<Siswa>> = siswaDao.getAll()
    val siswaCount: LiveData<Int> = siswaDao.getCount()

    fun searchSiswa(query: String): LiveData<List<Siswa>> {
        return siswaDao.search(query)
    }

    suspend fun insert(siswa: Siswa): Long {
        return siswaDao.insert(siswa)
    }

    suspend fun update(siswa: Siswa) {
        siswaDao.update(siswa)
    }

    suspend fun delete(siswa: Siswa) {
        siswaDao.delete(siswa)
    }

    suspend fun getById(id: Int): Siswa? {
        return siswaDao.getById(id)
    }

    suspend fun getByBarcode(barcode: String): Siswa? {
        return siswaDao.getByBarcode(barcode)
    }

    suspend fun isBarcodeExists(barcode: String): Boolean {
        return siswaDao.countByBarcode(barcode) > 0
    }

    suspend fun isBarcodeExistsExcluding(barcode: String, excludeId: Int): Boolean {
        return siswaDao.countByBarcodeExcluding(barcode, excludeId) > 0
    }

    fun getByType(tipe: String): LiveData<List<Siswa>> {
        return siswaDao.getByType(tipe)
    }

    fun searchByType(tipe: String, query: String): LiveData<List<Siswa>> {
        return siswaDao.searchByType(tipe, query)
    }
}
