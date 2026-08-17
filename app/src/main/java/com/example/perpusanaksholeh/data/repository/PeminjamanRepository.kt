package com.example.perpusanaksholeh.data.repository

import androidx.lifecycle.LiveData
import com.example.perpusanaksholeh.data.database.dao.EksemplarBukuDao
import com.example.perpusanaksholeh.data.database.dao.PeminjamanDao
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.model.PeminjamanDetail
import java.util.concurrent.TimeUnit

class PeminjamanRepository(
    private val peminjamanDao: PeminjamanDao,
    private val eksemplarBukuDao: EksemplarBukuDao
) {

    val allPeminjamanDetail: LiveData<List<PeminjamanDetail>> = peminjamanDao.getAllDetail()
    val peminjamanAktifDetail: LiveData<List<PeminjamanDetail>> = peminjamanDao.getAktifDetail()
    val riwayatPeminjamanDetail: LiveData<List<PeminjamanDetail>> = peminjamanDao.getRiwayatDetail()
    val peminjamanAktifCount: LiveData<Int> = peminjamanDao.getAktifCount()
    val peminjamanTerlambatCount: LiveData<Int> = peminjamanDao.getTerlambatCount()

    /**
     * Proses peminjaman buku.
     * @return Result.success(peminjamanId) atau Result.failure(exception)
     */
    suspend fun pinjamBuku(
        siswaId: Int,
        eksemplarId: Int,
        durasiHari: Int = Peminjaman.DURASI_DEFAULT_HARI
    ): Result<Long> {
        return try {
            // Cek apakah eksemplar tersedia
            val eksemplar = eksemplarBukuDao.getById(eksemplarId)
                ?: return Result.failure(Exception("Eksemplar buku tidak ditemukan"))

            if (eksemplar.status != EksemplarBuku.STATUS_TERSEDIA) {
                return Result.failure(Exception("Buku ini sedang dipinjam"))
            }

            // Cek apakah eksemplar sudah punya peminjaman aktif
            val existingPeminjaman = peminjamanDao.getAktifByEksemplar(eksemplarId)
            if (existingPeminjaman != null) {
                return Result.failure(Exception("Buku ini sedang dipinjam oleh orang lain"))
            }

            val now = System.currentTimeMillis()
            val jatuhTempo = now + TimeUnit.DAYS.toMillis(durasiHari.toLong())

            val peminjaman = Peminjaman(
                siswaId = siswaId,
                eksemplarId = eksemplarId,
                tanggalPinjam = now,
                tanggalJatuhTempo = jatuhTempo,
                status = Peminjaman.STATUS_DIPINJAM
            )

            val id = peminjamanDao.insert(peminjaman)

            // Update status eksemplar jadi dipinjam
            eksemplarBukuDao.updateStatus(eksemplarId, EksemplarBuku.STATUS_DIPINJAM)

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Proses pengembalian buku.
     * @return Result.success(denda) atau Result.failure(exception)
     */
    suspend fun kembalikanBuku(eksemplarId: Int): Result<Int> {
        return try {
            val peminjaman = peminjamanDao.getAktifByEksemplar(eksemplarId)
                ?: return Result.failure(Exception("Tidak ada peminjaman aktif untuk buku ini"))

            val now = System.currentTimeMillis()
            var denda = 0

            // Hitung denda jika terlambat
            if (now > peminjaman.tanggalJatuhTempo) {
                val selisihMs = now - peminjaman.tanggalJatuhTempo
                val hariTerlambat = TimeUnit.MILLISECONDS.toDays(selisihMs).toInt() + 1
                denda = hariTerlambat * Peminjaman.DENDA_PER_HARI
            }

            val updatedPeminjaman = peminjaman.copy(
                tanggalKembali = now,
                status = Peminjaman.STATUS_DIKEMBALIKAN,
                denda = denda
            )

            peminjamanDao.update(updatedPeminjaman)

            // Update status eksemplar kembali ke tersedia
            eksemplarBukuDao.updateStatus(eksemplarId, EksemplarBuku.STATUS_TERSEDIA)

            Result.success(denda)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailByEksemplar(eksemplarId: Int): PeminjamanDetail? {
        return peminjamanDao.getAktifDetailByEksemplar(eksemplarId)
    }

    suspend fun getPeminjamanTerlambat(currentTime: Long): List<Peminjaman> {
        return peminjamanDao.getPeminjamanTerlambat(currentTime)
    }

    suspend fun updateStatusTerlambat(peminjaman: Peminjaman) {
        val updated = peminjaman.copy(status = Peminjaman.STATUS_TERLAMBAT)
        peminjamanDao.update(updated)
    }

    suspend fun countAktifBySiswa(siswaId: Int): Int {
        return peminjamanDao.countAktifBySiswa(siswaId)
    }
}
