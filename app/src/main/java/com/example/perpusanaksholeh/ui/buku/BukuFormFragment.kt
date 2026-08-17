package com.example.perpusanaksholeh.ui.buku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.databinding.FragmentBukuFormBinding
import com.example.perpusanaksholeh.util.Resource

class BukuFormFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBukuFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BukuViewModel by viewModels()
    private var editBukuId: Int = -1
    private var existingBuku: Buku? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBukuFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editBukuId = arguments?.getInt("bukuId", -1) ?: -1

        if (editBukuId != -1) {
            binding.tvFormTitle.text = "Ubah Buku"
            loadBukuData()
        }

        binding.btnSimpan.setOnClickListener {
            saveBuku()
        }

        observeResults()
    }

    private fun loadBukuData() {
        viewModel.loadBuku(editBukuId)
        viewModel.selectedBuku.observe(viewLifecycleOwner) { bukuWithEksemplar ->
            if (bukuWithEksemplar != null) {
                val buku = bukuWithEksemplar.buku
                existingBuku = buku
                binding.etJudul.setText(buku.judul)
                binding.etPenulis.setText(buku.penulis)
                binding.etPenerbit.setText(buku.penerbit)
                binding.etKategori.setText(buku.kategori)
                binding.etTahunTerbit.setText(buku.tahunTerbit)
            }
        }
    }

    private fun saveBuku() {
        val judul = binding.etJudul.text.toString().trim()
        val penulis = binding.etPenulis.text.toString().trim()
        val penerbit = binding.etPenerbit.text.toString().trim()
        val kategori = binding.etKategori.text.toString().trim()
        val tahunTerbit = binding.etTahunTerbit.text.toString().trim()

        var hasError = false
        if (judul.isEmpty()) {
            binding.tilJudul.error = "Judul wajib diisi"
            hasError = true
        } else binding.tilJudul.error = null

        if (penulis.isEmpty()) {
            binding.tilPenulis.error = "Penulis wajib diisi"
            hasError = true
        } else binding.tilPenulis.error = null

        if (penerbit.isEmpty()) {
            binding.tilPenerbit.error = "Penerbit wajib diisi"
            hasError = true
        } else binding.tilPenerbit.error = null

        if (kategori.isEmpty()) {
            binding.tilKategori.error = "Kategori wajib diisi"
            hasError = true
        } else binding.tilKategori.error = null

        if (tahunTerbit.isEmpty()) {
            binding.tilTahunTerbit.error = "Tahun terbit wajib diisi"
            hasError = true
        } else binding.tilTahunTerbit.error = null

        if (hasError) return

        if (editBukuId != -1 && existingBuku != null) {
            val updatedBuku = existingBuku!!.copy(
                judul = judul,
                penulis = penulis,
                penerbit = penerbit,
                kategori = kategori,
                tahunTerbit = tahunTerbit
            )
            viewModel.updateBuku(updatedBuku)
        } else {
            val buku = Buku(
                judul = judul,
                penulis = penulis,
                penerbit = penerbit,
                kategori = kategori,
                tahunTerbit = tahunTerbit
            )
            viewModel.insertBuku(buku)
        }
    }

    private fun observeResults() {
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
