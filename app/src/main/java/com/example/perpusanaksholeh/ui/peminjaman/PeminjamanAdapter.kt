package com.example.perpusanaksholeh.ui.peminjaman

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.data.model.PeminjamanDetail
import com.example.perpusanaksholeh.databinding.ItemPeminjamanBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PeminjamanAdapter : ListAdapter<PeminjamanDetail, PeminjamanAdapter.PeminjamanViewHolder>(PeminjamanDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeminjamanViewHolder {
        val binding = ItemPeminjamanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PeminjamanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PeminjamanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PeminjamanViewHolder(
        private val binding: ItemPeminjamanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(detail: PeminjamanDetail) {
            binding.tvJudulBuku.text = detail.judulBuku
            binding.tvNamaSiswa.text = detail.namaSiswa
            binding.tvTanggalPinjam.text = dateFormat.format(Date(detail.tanggalPinjam))
            binding.tvJatuhTempo.text = dateFormat.format(Date(detail.tanggalJatuhTempo))

            // Status badge
            when (detail.status) {
                Peminjaman.STATUS_DIPINJAM -> {
                    binding.tvStatus.text = "Dipinjam"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_dipinjam)
                    binding.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.status_dipinjam))
                }
                Peminjaman.STATUS_TERLAMBAT -> {
                    binding.tvStatus.text = "Terlambat"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_terlambat)
                    binding.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.status_terlambat))
                }
                Peminjaman.STATUS_DIKEMBALIKAN -> {
                    binding.tvStatus.text = "Dikembalikan"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_dikembalikan)
                    binding.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.status_dikembalikan))
                }
            }

            // Denda
            if (detail.denda > 0) {
                binding.tvDenda.isVisible = true
                binding.tvDenda.text = "Denda: Rp ${String.format("%,d", detail.denda)}"
            } else {
                binding.tvDenda.isVisible = false
            }
        }
    }

    class PeminjamanDiffCallback : DiffUtil.ItemCallback<PeminjamanDetail>() {
        override fun areItemsTheSame(oldItem: PeminjamanDetail, newItem: PeminjamanDetail): Boolean {
            return oldItem.peminjamanId == newItem.peminjamanId
        }

        override fun areContentsTheSame(oldItem: PeminjamanDetail, newItem: PeminjamanDetail): Boolean {
            return oldItem == newItem
        }
    }
}
