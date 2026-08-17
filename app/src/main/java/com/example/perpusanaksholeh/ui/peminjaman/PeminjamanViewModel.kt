package com.example.perpusanaksholeh.ui.peminjaman

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.data.model.PeminjamanDetail
import com.example.perpusanaksholeh.data.repository.BukuRepository
import com.example.perpusanaksholeh.data.repository.PeminjamanRepository
import com.example.perpusanaksholeh.data.repository.SiswaRepository
import com.example.perpusanaksholeh.util.Resource
import kotlinx.coroutines.launch

class PeminjamanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val peminjamanRepository = PeminjamanRepository(database.peminjamanDao(), database.eksemplarBukuDao())
    private val siswaRepository = SiswaRepository(database.siswaDao())
    private val bukuRepository = BukuRepository(database.bukuDao(), database.eksemplarBukuDao())

    val peminjamanAktif: LiveData<List<PeminjamanDetail>> = peminjamanRepository.peminjamanAktifDetail
    val riwayatPeminjaman: LiveData<List<PeminjamanDetail>> = peminjamanRepository.riwayatPeminjamanDetail
    val allPeminjaman: LiveData<List<PeminjamanDetail>> = peminjamanRepository.allPeminjamanDetail

    private val _operationResult = MutableLiveData<Resource<String>>()
    val operationResult: LiveData<Resource<String>> = _operationResult

    private val _selectedSiswa = MutableLiveData<Siswa?>()
    val selectedSiswa: LiveData<Siswa?> = _selectedSiswa

    private val _selectedEksemplars = MutableLiveData<List<EksemplarBuku>>(emptyList())
    val selectedEksemplars: LiveData<List<EksemplarBuku>> = _selectedEksemplars

    private val _selectedEksemplar = MutableLiveData<EksemplarBuku?>()
    val selectedEksemplar: LiveData<EksemplarBuku?> = _selectedEksemplar

    private val _bukuJudul = MutableLiveData<String?>()
    val bukuJudul: LiveData<String?> = _bukuJudul

    private val _pengembalianDetail = MutableLiveData<PeminjamanDetail?>()
    val pengembalianDetail: LiveData<PeminjamanDetail?> = _pengembalianDetail

    fun findSiswaByBarcode(barcode: String) = viewModelScope.launch {
        val siswa = siswaRepository.getByBarcode(barcode)
        _selectedSiswa.value = siswa
        if (siswa == null) {
            _operationResult.value = Resource.Error("Siswa dengan barcode '$barcode' tidak ditemukan")
        }
    }

    fun findEksemplarByBarcode(barcode: String) = findMultipleEksemplarsByBarcodes(listOf(barcode))

    fun findMultipleEksemplarsByBarcodes(barcodes: List<String>) = viewModelScope.launch {
        val list = mutableListOf<EksemplarBuku>()
        val titles = StringBuilder()

        for (barcode in barcodes) {
            val eksemplar = bukuRepository.getEksemplarByBarcode(barcode)
            if (eksemplar != null) {
                if (eksemplar.status == EksemplarBuku.STATUS_DIPINJAM) {
                    _operationResult.value = Resource.Error("Buku dengan barcode '$barcode' sedang dipinjam")
                    continue
                }
                list.add(eksemplar)
                val buku = bukuRepository.getBukuById(eksemplar.bukuId)
                val judul = buku?.judul ?: "Unknown"
                titles.append("• $judul ($barcode)\n")
            } else {
                _operationResult.value = Resource.Error("Buku dengan barcode '$barcode' tidak ditemukan")
            }
        }

        _selectedEksemplars.value = list
        _selectedEksemplar.value = list.firstOrNull()
        if (list.isNotEmpty()) {
            _bukuJudul.value = titles.toString().trim()
        } else {
            _bukuJudul.value = null
        }
    }

    fun pinjamBuku(durasiHari: Int = Peminjaman.DURASI_DEFAULT_HARI) = viewModelScope.launch {
        val siswa = _selectedSiswa.value
        val eksemplars = _selectedEksemplars.value ?: emptyList()

        if (siswa == null) {
            _operationResult.value = Resource.Error("Pilih pengunjung terlebih dahulu")
            return@launch
        }
        if (eksemplars.isEmpty()) {
            _operationResult.value = Resource.Error("Pilih minimal satu buku terlebih dahulu")
            return@launch
        }

        var successCount = 0
        var failMessage: String? = null

        for (eksemplar in eksemplars) {
            val result = peminjamanRepository.pinjamBuku(siswa.id, eksemplar.id, durasiHari)
            result.fold(
                onSuccess = {
                    successCount++
                },
                onFailure = { e ->
                    failMessage = e.message
                }
            )
        }

        if (successCount == eksemplars.size) {
            _operationResult.value = Resource.Success("Berhasil meminjam $successCount buku! Durasi: $durasiHari hari")
            clearSelection()
        } else if (successCount > 0) {
            _operationResult.value = Resource.Success("Berhasil meminjam $successCount buku. Beberapa gagal: $failMessage")
            clearSelection()
        } else {
            _operationResult.value = Resource.Error("Gagal meminjam: $failMessage")
        }
    }

    fun findPeminjamanByBarcode(barcode: String) = viewModelScope.launch {
        val eksemplar = bukuRepository.getEksemplarByBarcode(barcode)
        if (eksemplar == null) {
            _pengembalianDetail.value = null
            _operationResult.value = Resource.Error("Eksemplar buku dengan barcode '$barcode' tidak ditemukan")
            return@launch
        }

        val detail = peminjamanRepository.getDetailByEksemplar(eksemplar.id)
        _pengembalianDetail.value = detail
        if (detail == null) {
            _operationResult.value = Resource.Error("Buku ini tidak sedang dipinjam")
        }
    }

    fun kembalikanBuku() = viewModelScope.launch {
        val detail = _pengembalianDetail.value
        if (detail == null) {
            _operationResult.value = Resource.Error("Tidak ada data peminjaman")
            return@launch
        }

        val result = peminjamanRepository.kembalikanBuku(detail.eksemplarId)
        result.fold(
            onSuccess = { denda ->
                val message = if (denda > 0) {
                    "Buku berhasil dikembalikan. Denda: Rp ${String.format("%,d", denda)}"
                } else {
                    "Buku berhasil dikembalikan. Tepat waktu!"
                }
                _operationResult.value = Resource.Success(message)
                _pengembalianDetail.value = null
            },
            onFailure = { e ->
                _operationResult.value = Resource.Error("Gagal mengembalikan: ${e.message}")
            }
        )
    }

    fun clearSelection() {
        _selectedSiswa.value = null
        _selectedEksemplar.value = null
        _selectedEksemplars.value = emptyList()
        _bukuJudul.value = null
        _pengembalianDetail.value = null
    }
}
