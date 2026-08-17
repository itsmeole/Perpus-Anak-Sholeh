package com.example.perpusanaksholeh.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.util.NotificationHelper
import java.util.concurrent.TimeUnit

class LateReturnWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val peminjamanDao = database.peminjamanDao()
        val eksemplarBukuDao = database.eksemplarBukuDao()
        val bukuDao = database.bukuDao()
        val siswaDao = database.siswaDao()

        val currentTime = System.currentTimeMillis()

        try {
            val terlambatList = peminjamanDao.getPeminjamanTerlambat(currentTime)

            if (terlambatList.isEmpty()) {
                return Result.success()
            }

            var notifCount = 0
            for (peminjaman in terlambatList) {
                if (peminjaman.status != Peminjaman.STATUS_TERLAMBAT) {
                    val updated = peminjaman.copy(status = Peminjaman.STATUS_TERLAMBAT)
                    peminjamanDao.update(updated)
                }

                val siswa = siswaDao.getById(peminjaman.siswaId)
                val eksemplar = eksemplarBukuDao.getById(peminjaman.eksemplarId)
                val buku = if (eksemplar != null) bukuDao.getById(eksemplar.bukuId) else null

                if (siswa != null && buku != null) {
                    val selisihMs = currentTime - peminjaman.tanggalJatuhTempo
                    val hariTerlambat = TimeUnit.MILLISECONDS.toDays(selisihMs).toInt() + 1
                    val denda = hariTerlambat * Peminjaman.DENDA_PER_HARI

                    val message = "${siswa.namaSiswa} terlambat ${hariTerlambat} hari " +
                            "mengembalikan \"${buku.judul}\". " +
                            "Denda: Rp ${String.format("%,d", denda)}"

                    NotificationHelper.showLateReturnNotification(
                        applicationContext,
                        peminjaman.id,
                        "⚠️ Keterlambatan Pengembalian",
                        message
                    )
                    notifCount++
                }
            }

            if (notifCount > 1) {
                NotificationHelper.showLateReturnNotification(
                    applicationContext,
                    -1,
                    "📚 Perpus Anak Sholeh",
                    "Ada $notifCount buku yang terlambat dikembalikan. Segera tindak lanjuti!"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
