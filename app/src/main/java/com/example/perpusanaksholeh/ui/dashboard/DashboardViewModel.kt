package com.example.perpusanaksholeh.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.repository.BukuRepository
import com.example.perpusanaksholeh.data.repository.PeminjamanRepository
import com.example.perpusanaksholeh.data.repository.SiswaRepository
import com.example.perpusanaksholeh.util.BackupHelper
import com.example.perpusanaksholeh.util.Resource
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val siswaRepository = SiswaRepository(database.siswaDao())
    private val bukuRepository = BukuRepository(database.bukuDao(), database.eksemplarBukuDao())
    private val peminjamanRepository = PeminjamanRepository(database.peminjamanDao(), database.eksemplarBukuDao())

    val totalSiswa: LiveData<Int> = siswaRepository.siswaCount
    val totalBuku: LiveData<Int> = bukuRepository.bukuCount
    val totalEksemplar: LiveData<Int> = bukuRepository.totalEksemplar
    val peminjamanAktif: LiveData<Int> = peminjamanRepository.peminjamanAktifCount
    val peminjamanTerlambat: LiveData<Int> = peminjamanRepository.peminjamanTerlambatCount

    private val _backupResult = MutableLiveData<Resource<String>>()
    val backupResult: LiveData<Resource<String>> = _backupResult

    fun exportData(onCompleted: (String) -> Unit) = viewModelScope.launch {
        try {
            val json = BackupHelper.exportDatabase(getApplication())
            onCompleted(json)
        } catch (e: Exception) {
            _backupResult.value = Resource.Error("Gagal ekspor data: ${e.message}")
        }
    }

    fun importData(jsonString: String) = viewModelScope.launch {
        _backupResult.value = Resource.Error("") // Reset state or do loading if needed, here we just do import
        val result = BackupHelper.importDatabase(getApplication(), jsonString)
        _backupResult.value = result
    }
}
