package com.example.perpusanaksholeh.ui.siswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.databinding.FragmentSiswaDetailBinding
import com.example.perpusanaksholeh.ui.peminjaman.PeminjamanAdapter
import com.example.perpusanaksholeh.ui.peminjaman.PeminjamanViewModel
import com.example.perpusanaksholeh.util.BarcodeGenerator
import com.example.perpusanaksholeh.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SiswaDetailFragment : Fragment() {

    private var _binding: FragmentSiswaDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SiswaViewModel by viewModels()
    private val peminjamanViewModel: PeminjamanViewModel by viewModels()
    private var currentSiswa: Siswa? = null
    private lateinit var peminjamanAdapter: PeminjamanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiswaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val siswaId = arguments?.getInt("siswaId") ?: return

        setupRecyclerView()
        loadData(siswaId)
        setupActions()
        observeResults()
    }

    private fun setupRecyclerView() {
        peminjamanAdapter = PeminjamanAdapter()
        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.adapter = peminjamanAdapter
    }

    private fun loadData(siswaId: Int) {
        viewModel.loadSiswa(siswaId)
        viewModel.selectedSiswa.observe(viewLifecycleOwner) { siswa ->
            if (siswa != null) {
                currentSiswa = siswa
                displaySiswa(siswa)
            }
        }

        peminjamanViewModel.allPeminjaman.observe(viewLifecycleOwner) { list ->
            val filtered = list.filter { it.siswaId == siswaId }
            peminjamanAdapter.submitList(filtered)
            binding.tvRiwayatKosong.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRiwayat.visibility = if (filtered.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun displaySiswa(siswa: Siswa) {
        binding.tvNamaSiswa.text = siswa.namaSiswa
        binding.tvNomorKartu.text = siswa.nomorKartu
        binding.tvBarcode.text = siswa.barcode
        binding.tvBarcodeValue.text = siswa.barcode

        binding.tvDetailTipe.text = if (siswa.tipe == "umum") "Umum" else "Siswa"
        
        val isSiswa = (siswa.tipe == "siswa")
        
        binding.layoutDetailKelas.visibility = if (isSiswa) View.VISIBLE else View.GONE
        binding.tvDetailKelas.text = siswa.kelas ?: "-"
        
        binding.layoutDetailUsia.visibility = if (!isSiswa) View.VISIBLE else View.GONE
        binding.tvDetailUsia.text = siswa.usiaRange ?: "-"
        
        binding.layoutDetailAlamat.visibility = if (!isSiswa) View.VISIBLE else View.GONE
        binding.tvDetailAlamat.text = siswa.alamat ?: "-"
        
        binding.layoutDetailPekerjaan.visibility = if (!isSiswa) View.VISIBLE else View.GONE
        binding.tvDetailPekerjaan.text = siswa.pekerjaan ?: "-"
        
        binding.layoutDetailInstansi.visibility = if (!isSiswa) View.VISIBLE else View.GONE
        binding.tvDetailInstansi.text = siswa.instansi ?: "-"

        val barcodeBitmap = BarcodeGenerator.generateBarcode(siswa.barcode)
        binding.ivBarcode.setImageBitmap(barcodeBitmap)
    }

    private fun setupActions() {
        binding.btnEdit.setOnClickListener {
            currentSiswa?.let { siswa ->
                val bundle = Bundle().apply { putInt("siswaId", siswa.id) }
                findNavController().navigate(R.id.siswaFormFragment, bundle)
            }
        }

        binding.btnHapus.setOnClickListener {
            currentSiswa?.let { siswa ->
                val typeLabel = if (siswa.tipe == "umum") "Pengunjung" else "Siswa"
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hapus $typeLabel")
                    .setMessage("Apakah Anda yakin ingin menghapus $typeLabel ini?")
                    .setPositiveButton(getString(R.string.ya)) { _, _ ->
                        viewModel.delete(siswa)
                    }
                    .setNegativeButton(getString(R.string.tidak), null)
                    .show()
            }
        }

        binding.btnDownloadCard.setOnClickListener {
            currentSiswa?.let { siswa ->
                val cardBinding = com.example.perpusanaksholeh.databinding.LayoutDownloadSiswaBinding.inflate(layoutInflater)
                cardBinding.tvNamaSiswa.text = siswa.namaSiswa
                cardBinding.tvNomorKartu.text = siswa.nomorKartu
                cardBinding.tvTipe.text = if (siswa.tipe == "umum") "Umum" else "Siswa"
                
                val isSiswa = (siswa.tipe == "siswa")
                if (isSiswa) {
                    cardBinding.tvDetailFieldLabel.text = "Kelas"
                    cardBinding.tvDetailFieldValue.text = siswa.kelas ?: "-"
                    cardBinding.layoutInstansiField.visibility = View.GONE
                } else {
                    cardBinding.tvDetailFieldLabel.text = "Kategori Usia"
                    cardBinding.tvDetailFieldValue.text = siswa.usiaRange ?: "-"
                    cardBinding.layoutInstansiField.visibility = View.VISIBLE
                    cardBinding.tvInstansi.text = siswa.instansi ?: "-"
                }
                cardBinding.tvBarcodeValue.text = siswa.barcode
                val barcodeBitmap = BarcodeGenerator.generateBarcode(siswa.barcode, 400, 100)
                cardBinding.ivBarcode.setImageBitmap(barcodeBitmap)

                val fileName = "Kartu_Anggota_${siswa.namaSiswa.replace(" ", "_")}_${siswa.barcode}"
                com.example.perpusanaksholeh.util.ImageExportHelper.exportViewToPng(cardBinding.root, fileName, requireContext())
            }
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
