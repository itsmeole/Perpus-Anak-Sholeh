package com.example.perpusanaksholeh.ui.buku

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.model.BukuWithEksemplar
import com.example.perpusanaksholeh.data.repository.BukuRepository
import com.example.perpusanaksholeh.util.Resource
import kotlinx.coroutines.launch

class BukuViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BukuRepository(database.bukuDao(), database.eksemplarBukuDao())

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<BukuWithEksemplar>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.allBukuWithEksemplar
        } else {
            repository.searchBuku(query)
        }
    }

    private val _operationResult = MutableLiveData<Resource<String>>()
    val operationResult: LiveData<Resource<String>> = _operationResult

    private val _selectedBuku = MutableLiveData<BukuWithEksemplar?>()
    val selectedBuku: LiveData<BukuWithEksemplar?> = _selectedBuku

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun insertBuku(buku: Buku) = viewModelScope.launch {
        try {
            val id = repository.insertBuku(buku)
            _operationResult.value = Resource.Success("Buku berhasil ditambahkan (ID: $id)")
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Gagal menambahkan buku: ${e.message}")
        }
    }

    fun updateBuku(buku: Buku) = viewModelScope.launch {
        try {
            repository.updateBuku(buku)
            _operationResult.value = Resource.Success("Buku berhasil diperbarui")
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Gagal memperbarui buku: ${e.message}")
        }
    }

    fun deleteBuku(buku: Buku) = viewModelScope.launch {
        try {
            repository.deleteBuku(buku)
            _operationResult.value = Resource.Success("Buku berhasil dihapus")
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Gagal menghapus buku: ${e.message}")
        }
    }

    fun loadBuku(id: Int) = viewModelScope.launch {
        _selectedBuku.value = repository.getBukuWithEksemplarById(id)
    }

    fun addMultipleEksemplar(bukuId: Int, barcodes: List<String>) = viewModelScope.launch {
        try {
            val duplicateBarcodes = mutableListOf<String>()
            val successBarcodes = mutableListOf<String>()
            val siswaDao = database.siswaDao()

            for (barcode in barcodes) {
                if (repository.isEksemplarBarcodeExists(barcode) || siswaDao.countByBarcode(barcode) > 0) {
                    duplicateBarcodes.add(barcode)
                } else {
                    val eksemplar = EksemplarBuku(
                        bukuId = bukuId,
                        barcode = barcode
                    )
                    repository.insertEksemplar(eksemplar)
                    successBarcodes.add(barcode)
                }
            }

            loadBuku(bukuId)

            if (successBarcodes.isNotEmpty()) {
                val message = if (duplicateBarcodes.isNotEmpty()) {
                    "${successBarcodes.size} eksemplar berhasil ditambahkan. ${duplicateBarcodes.size} dilewati karena barcode duplikat."
                } else {
                    "${successBarcodes.size} eksemplar berhasil ditambahkan"
                }
                _operationResult.value = Resource.Success(message)
            } else if (duplicateBarcodes.isNotEmpty()) {
                _operationResult.value = Resource.Error("Gagal menambahkan: Semua barcode sudah terdaftar")
            }
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Error: ${e.message}")
        }
    }

    fun addEksemplar(bukuId: Int, barcode: String) = viewModelScope.launch {
        try {
            if (repository.isEksemplarBarcodeExists(barcode)) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh eksemplar lain")
                return@launch
            }
            val siswaDao = database.siswaDao()
            if (siswaDao.countByBarcode(barcode) > 0) {
                _operationResult.value = Resource.Error("Barcode sudah digunakan oleh siswa")
                return@launch
            }

            val eksemplar = EksemplarBuku(
                bukuId = bukuId,
                barcode = barcode
            )
            repository.insertEksemplar(eksemplar)
            loadBuku(bukuId)
            _operationResult.value = Resource.Success("Eksemplar berhasil ditambahkan")
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Gagal menambahkan eksemplar: ${e.message}")
        }
    }

    fun deleteEksemplar(eksemplar: EksemplarBuku) = viewModelScope.launch {
        try {
            if (eksemplar.status == EksemplarBuku.STATUS_DIPINJAM) {
                _operationResult.value = Resource.Error("Tidak bisa menghapus eksemplar yang sedang dipinjam")
                return@launch
            }
            repository.deleteEksemplar(eksemplar)
            loadBuku(eksemplar.bukuId)
            _operationResult.value = Resource.Success("Eksemplar berhasil dihapus")
        } catch (e: Exception) {
            _operationResult.value = Resource.Error("Gagal menghapus eksemplar: ${e.message}")
        }
    }

    fun getEksemplarByBarcode(barcode: String, callback: (EksemplarBuku?) -> Unit) = viewModelScope.launch {
        val eksemplar = repository.getEksemplarByBarcode(barcode)
        callback(eksemplar)
    }

    fun getEksemplarByBukuId(bukuId: Int): LiveData<List<EksemplarBuku>> {
        return repository.getEksemplarByBukuId(bukuId)
    }
}
