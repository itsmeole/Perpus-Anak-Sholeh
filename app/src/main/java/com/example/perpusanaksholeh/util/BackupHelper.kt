package com.example.perpusanaksholeh.util

import android.content.Context
import androidx.room.withTransaction
import com.example.perpusanaksholeh.data.database.AppDatabase
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.database.entity.Siswa
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    suspend fun exportDatabase(context: Context): String {
        val database = AppDatabase.getDatabase(context)
        val response = JSONObject()
        response.put("version", 1)

        // 1. Siswa
        val siswaList = database.siswaDao().getAllSync()
        val siswaArray = JSONArray()
        for (siswa in siswaList) {
            val obj = JSONObject().apply {
                put("id", siswa.id)
                put("nomor_kartu", siswa.nomorKartu)
                put("nama_siswa", siswa.namaSiswa)
                put("barcode", siswa.barcode)
                put("tipe", siswa.tipe)
                put("kelas", siswa.kelas ?: JSONObject.NULL)
                put("usia_range", siswa.usiaRange ?: JSONObject.NULL)
                put("alamat", siswa.alamat ?: JSONObject.NULL)
                put("pekerjaan", siswa.pekerjaan ?: JSONObject.NULL)
                put("instansi", siswa.instansi ?: JSONObject.NULL)
                put("created_at", siswa.createdAt)
            }
            siswaArray.put(obj)
        }
        response.put("siswa", siswaArray)

        // 2. Buku
        val bukuList = database.bukuDao().getAllSync()
        val bukuArray = JSONArray()
        for (buku in bukuList) {
            val obj = JSONObject().apply {
                put("id", buku.id)
                put("judul", buku.judul)
                put("penulis", buku.penulis)
                put("penerbit", buku.penerbit)
                put("kategori", buku.kategori)
                put("tahun_terbit", buku.tahunTerbit)
                put("created_at", buku.createdAt)
            }
            bukuArray.put(obj)
        }
        response.put("buku", bukuArray)

        // 3. Eksemplar
        val eksemplarList = database.eksemplarBukuDao().getAllSync()
        val eksemplarArray = JSONArray()
        for (eksemplar in eksemplarList) {
            val obj = JSONObject().apply {
                put("id", eksemplar.id)
                put("buku_id", eksemplar.bukuId)
                put("barcode", eksemplar.barcode)
                put("status", eksemplar.status)
            }
            eksemplarArray.put(obj)
        }
        response.put("eksemplar_buku", eksemplarArray)

        // 4. Peminjaman
        val peminjamanList = database.peminjamanDao().getAllSync()
        val peminjamanArray = JSONArray()
        for (peminjaman in peminjamanList) {
            val obj = JSONObject().apply {
                put("id", peminjaman.id)
                put("siswa_id", peminjaman.siswaId)
                put("eksemplar_id", peminjaman.eksemplarId)
                put("tanggal_pinjam", peminjaman.tanggalPinjam)
                put("tanggal_jatuh_tempo", peminjaman.tanggalJatuhTempo)
                put("tanggal_kembali", peminjaman.tanggalKembali ?: JSONObject.NULL)
                put("status", peminjaman.status)
                put("denda", peminjaman.denda)
            }
            peminjamanArray.put(obj)
        }
        response.put("peminjaman", peminjamanArray)

        return response.toString(4)
    }

    suspend fun importDatabase(context: Context, jsonString: String): Resource<String> {
        return try {
            val root = JSONObject(jsonString)
            
            // Parse Siswa
            val siswaList = mutableListOf<Siswa>()
            val siswaArray = root.optJSONArray("siswa") ?: JSONArray()
            for (i in 0 until siswaArray.length()) {
                val obj = siswaArray.getJSONObject(i)
                val tipe = obj.optString("tipe", "siswa")
                val kelas = if (obj.isNull("kelas")) null else obj.getString("kelas")
                val usiaRange = if (obj.isNull("usia_range")) null else obj.getString("usia_range")
                val alamat = if (obj.isNull("alamat")) null else obj.getString("alamat")
                val pekerjaan = if (obj.isNull("pekerjaan")) null else obj.getString("pekerjaan")
                val instansi = if (obj.isNull("instansi")) null else obj.getString("instansi")
                siswaList.add(
                    Siswa(
                        id = obj.getInt("id"),
                        nomorKartu = obj.getString("nomor_kartu"),
                        namaSiswa = obj.getString("nama_siswa"),
                        barcode = obj.getString("barcode"),
                        tipe = tipe,
                        kelas = kelas,
                        usiaRange = usiaRange,
                        alamat = alamat,
                        pekerjaan = pekerjaan,
                        instansi = instansi,
                        createdAt = obj.getLong("created_at")
                    )
                )
            }

            // Parse Buku
            val bukuList = mutableListOf<Buku>()
            val bukuArray = root.optJSONArray("buku") ?: JSONArray()
            for (i in 0 until bukuArray.length()) {
                val obj = bukuArray.getJSONObject(i)
                bukuList.add(
                    Buku(
                        id = obj.getInt("id"),
                        judul = obj.getString("judul"),
                        penulis = obj.getString("penulis"),
                        penerbit = obj.getString("penerbit"),
                        kategori = obj.getString("kategori"),
                        tahunTerbit = obj.getString("tahun_terbit"),
                        createdAt = obj.getLong("created_at")
                    )
                )
            }

            // Parse Eksemplar
            val eksemplarList = mutableListOf<EksemplarBuku>()
            val eksemplarArray = root.optJSONArray("eksemplar_buku") ?: JSONArray()
            for (i in 0 until eksemplarArray.length()) {
                val obj = eksemplarArray.getJSONObject(i)
                eksemplarList.add(
                    EksemplarBuku(
                        id = obj.getInt("id"),
                        bukuId = obj.getInt("buku_id"),
                        barcode = obj.getString("barcode"),
                        status = obj.getString("status")
                    )
                )
            }

            // Parse Peminjaman
            val peminjamanList = mutableListOf<Peminjaman>()
            val peminjamanArray = root.optJSONArray("peminjaman") ?: JSONArray()
            for (i in 0 until peminjamanArray.length()) {
                val obj = peminjamanArray.getJSONObject(i)
                val tglKembali = if (obj.isNull("tanggal_kembali")) null else obj.getLong("tanggal_kembali")
                peminjamanList.add(
                    Peminjaman(
                        id = obj.getInt("id"),
                        siswaId = obj.getInt("siswa_id"),
                        eksemplarId = obj.getInt("eksemplar_id"),
                        tanggalPinjam = obj.getLong("tanggal_pinjam"),
                        tanggalJatuhTempo = obj.getLong("tanggal_jatuh_tempo"),
                        tanggalKembali = tglKembali,
                        status = obj.getString("status"),
                        denda = obj.getInt("denda")
                    )
                )
            }

            val database = AppDatabase.getDatabase(context)
            
            // Suspend transaction
            database.withTransaction {
                database.peminjamanDao().deleteAll()
                database.eksemplarBukuDao().deleteAll()
                database.siswaDao().deleteAll()
                database.bukuDao().deleteAll()

                database.bukuDao().insertAll(bukuList)
                database.siswaDao().insertAll(siswaList)
                database.eksemplarBukuDao().insertAll(eksemplarList)
                database.peminjamanDao().insertAll(peminjamanList)
            }
            
            Resource.Success("Data backup berhasil dipulihkan")
        } catch (e: Exception) {
            Resource.Error("Gagal memulihkan backup: ${e.message}")
        }
    }
}
