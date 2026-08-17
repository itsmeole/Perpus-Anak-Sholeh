package com.example.perpusanaksholeh.ui.buku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perpusanaksholeh.data.model.BukuWithEksemplar
import com.example.perpusanaksholeh.databinding.ItemBukuBinding

class BukuAdapter(
    private val onItemClick: (BukuWithEksemplar) -> Unit
) : ListAdapter<BukuWithEksemplar, BukuAdapter.BukuViewHolder>(BukuDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BukuViewHolder {
        val binding = ItemBukuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BukuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BukuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BukuViewHolder(
        private val binding: ItemBukuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bukuWithEksemplar: BukuWithEksemplar) {
            val buku = bukuWithEksemplar.buku
            binding.tvJudul.text = buku.judul
            binding.tvPenulis.text = "${buku.penulis} • ${buku.tahunTerbit}"
            binding.tvKategori.text = buku.kategori
            binding.tvEksemplarInfo.text = "${bukuWithEksemplar.jumlahTotal} eks (${bukuWithEksemplar.jumlahTersedia} tersedia)"

            binding.root.setOnClickListener {
                onItemClick(bukuWithEksemplar)
            }
        }
    }

    class BukuDiffCallback : DiffUtil.ItemCallback<BukuWithEksemplar>() {
        override fun areItemsTheSame(oldItem: BukuWithEksemplar, newItem: BukuWithEksemplar): Boolean {
            return oldItem.buku.id == newItem.buku.id
        }

        override fun areContentsTheSame(oldItem: BukuWithEksemplar, newItem: BukuWithEksemplar): Boolean {
            return oldItem == newItem
        }
    }
}
