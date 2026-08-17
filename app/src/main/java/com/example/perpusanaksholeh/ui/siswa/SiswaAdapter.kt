package com.example.perpusanaksholeh.ui.siswa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.databinding.ItemSiswaBinding

class SiswaAdapter(
    private val onItemClick: (Siswa) -> Unit
) : ListAdapter<Siswa, SiswaAdapter.SiswaViewHolder>(SiswaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val binding = ItemSiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SiswaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SiswaViewHolder(
        private val binding: ItemSiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(siswa: Siswa) {
            binding.tvNamaSiswa.text = if (siswa.tipe == "umum") "${siswa.namaSiswa} (Umum)" else siswa.namaSiswa
            
            if (siswa.tipe == "umum") {
                val instansiText = if (!siswa.instansi.isNullOrBlank()) " | ${siswa.instansi}" else ""
                val usiaText = if (!siswa.usiaRange.isNullOrBlank()) " | ${siswa.usiaRange}" else ""
                binding.tvNomorKartu.text = "No: ${siswa.nomorKartu}${usiaText}${instansiText}"
            } else {
                val kelasText = if (!siswa.kelas.isNullOrBlank()) " | Kelas ${siswa.kelas}" else ""
                binding.tvNomorKartu.text = "No: ${siswa.nomorKartu}${kelasText}"
            }
            
            binding.tvBarcode.text = "Barcode: ${siswa.barcode}"

            binding.root.setOnClickListener {
                onItemClick(siswa)
            }
        }
    }

    class SiswaDiffCallback : DiffUtil.ItemCallback<Siswa>() {
        override fun areItemsTheSame(oldItem: Siswa, newItem: Siswa): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Siswa, newItem: Siswa): Boolean {
            return oldItem == newItem
        }
    }
}
