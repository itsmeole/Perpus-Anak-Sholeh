package com.example.perpusanaksholeh.ui.buku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.EksemplarBuku
import com.example.perpusanaksholeh.databinding.ItemEksemplarBinding
import com.example.perpusanaksholeh.util.BarcodeGenerator

class EksemplarAdapter(
    private val onDeleteClick: (EksemplarBuku) -> Unit,
    private val onDownloadClick: (EksemplarBuku) -> Unit
) : ListAdapter<EksemplarBuku, EksemplarAdapter.EksemplarViewHolder>(EksemplarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EksemplarViewHolder {
        val binding = ItemEksemplarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EksemplarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EksemplarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EksemplarViewHolder(
        private val binding: ItemEksemplarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(eksemplar: EksemplarBuku) {
            binding.tvBarcode.text = eksemplar.barcode

            // Set status badge
            when (eksemplar.status) {
                EksemplarBuku.STATUS_TERSEDIA -> {
                    binding.tvStatus.text = "Tersedia"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_tersedia)
                    binding.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.status_tersedia))
                }
                EksemplarBuku.STATUS_DIPINJAM -> {
                    binding.tvStatus.text = "Dipinjam"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_dipinjam)
                    binding.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.status_dipinjam))
                }
            }

            // Generate barcode image
            val barcodeBitmap = BarcodeGenerator.generateBarcode(eksemplar.barcode, 400, 100)
            binding.ivBarcode.setImageBitmap(barcodeBitmap)

            binding.btnDownload.setOnClickListener {
                onDownloadClick(eksemplar)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(eksemplar)
            }

            // Disable delete if dipinjam
            binding.btnDelete.isEnabled = eksemplar.status == EksemplarBuku.STATUS_TERSEDIA
            binding.btnDelete.alpha = if (eksemplar.status == EksemplarBuku.STATUS_TERSEDIA) 1f else 0.3f
        }
    }

    class EksemplarDiffCallback : DiffUtil.ItemCallback<EksemplarBuku>() {
        override fun areItemsTheSame(oldItem: EksemplarBuku, newItem: EksemplarBuku): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EksemplarBuku, newItem: EksemplarBuku): Boolean {
            return oldItem == newItem
        }
    }
}
