package com.example.perpusanaksholeh.ui.siswa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.data.repository.SiswaRepository
import com.example.perpusanaksholeh.util.Resource
import kotlinx.coroutines.launch

class SiswaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SiswaRepository(database.siswaDao())
    val allSiswa: LiveData<List<Siswa>> = repository.allSiswa

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Siswa>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.allSiswa
        } else {
            repository.searchSiswa(query)
        }
    }

    private val _searchQuerySiswa = MutableLiveData<String>("")
    val searchResultsSiswa: LiveData<List<Siswa>> = _searchQuerySiswa.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getByType("siswa")
        } else {
            repository.searchByType("siswa", query)
        }
    }

    private val _searchQueryUmum = MutableLiveData<String>("")
    val searchResultsUmum: LiveData<List<Siswa>> = _searchQueryUmum.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getByType("umum")
        } else {
            repository.searchByType("umum", query)
        }
    }

    private val _operationResult = MutableLiveData<Resource<String>>()
    val operationResult: LiveData<Resource<String>> = _operationResult

    private val _selectedSiswa = MutableLiveData<Siswa?>()
    val selectedSiswa: LiveData<Siswa?> = _selectedSiswa

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun searchSiswa(query: String) {
        _searchQuerySiswa.value = query
    }

    fun searchUmum(query: String) {
        _searchQueryUmum.value = query
    }

    fun insert(siswa: Siswa) = viewModelScope.launch {
        try {
            val typeLabel = if (siswa.tipe == "umum") "Pengunjung" else "Siswa"
            if (repository.isBarcodeExists(siswa.barcode)) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh $typeLabel lain")
                return@launch
            }
            val eksemplarDao = database.eksemplarBukuDao()
            if (eksemplarDao.countByBarcode(siswa.barcode) > 0) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh eksemplar buku")
                return@launch
            }
            repository.insert(siswa)
            _operationResult.value = Resource.Success("$typeLabel berhasil ditambahkan")
        } catch (e: Exception) {
            val typeLabel = if (siswa.tipe == "umum") "pengunjung" else "siswa"
            _operationResult.value = Resource.Error("Gagal menambahkan $typeLabel: ${e.message}")
        }
    }

    fun update(siswa: Siswa) = viewModelScope.launch {
        try {
            val typeLabel = if (siswa.tipe == "umum") "Pengunjung" else "Siswa"
            if (repository.isBarcodeExistsExcluding(siswa.barcode, siswa.id)) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh $typeLabel lain")
                return@launch
            }
            val eksemplarDao = database.eksemplarBukuDao()
            if (eksemplarDao.countByBarcode(siswa.barcode) > 0) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh eksemplar buku")
                return@launch
            }
            repository.update(siswa)
            _operationResult.value = Resource.Success("$typeLabel berhasil diperbarui")
        } catch (e: Exception) {
            val typeLabel = if (siswa.tipe == "umum") "pengunjung" else "siswa"
            _operationResult.value = Resource.Error("Gagal memperbarui $typeLabel: ${e.message}")
        }
    }

    fun delete(siswa: Siswa) = viewModelScope.launch {
        try {
            val typeLabel = if (siswa.tipe == "umum") "Pengunjung" else "Siswa"
            repository.delete(siswa)
            _operationResult.value = Resource.Success("$typeLabel berhasil dihapus")
        } catch (e: Exception) {
            val typeLabel = if (siswa.tipe == "umum") "pengunjung" else "siswa"
            _operationResult.value = Resource.Error("Gagal menghapus $typeLabel: ${e.message}")
        }
    }

    fun loadSiswa(id: Int) = viewModelScope.launch {
        _selectedSiswa.value = repository.getById(id)
    }

    fun getByBarcode(barcode: String, callback: (Siswa?) -> Unit) = viewModelScope.launch {
        val siswa = repository.getByBarcode(barcode)
        callback(siswa)
    }
}
