package com.example.perpusanaksholeh.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.perpusanaksholeh.data.database.dao.BukuDao
import com.example.perpusanaksholeh.data.database.dao.EksemplarBukuDao
import com.example.perpusanaksholeh.data.database.dao.PeminjamanDao
import com.example.perpusanaksholeh.data.database.dao.SiswaDao
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.database.entity.Siswa

@Database(
    entities = [Siswa::class, Buku::class, EksemplarBuku::class, Peminjaman::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun siswaDao(): SiswaDao
    abstract fun bukuDao(): BukuDao
    abstract fun eksemplarBukuDao(): EksemplarBukuDao
    abstract fun peminjamanDao(): PeminjamanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perpus_anak_sholeh_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
