package com.example.perpusanaksholeh.ui.peminjaman

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.perpusanaksholeh.databinding.FragmentPeminjamanBinding
import com.example.perpusanaksholeh.ui.scanner.ScannerActivity
import com.example.perpusanaksholeh.util.Resource

class PeminjamanFragment : Fragment() {

    private var _binding: FragmentPeminjamanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PeminjamanViewModel by viewModels()

    private var scanMode: String = ""

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedBarcode = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            val scannedBarcodes = result.data?.getStringArrayListExtra(ScannerActivity.EXTRA_SCAN_RESULTS)
            
            if (scanMode == "buku" && scannedBarcodes != null) {
                viewModel.findMultipleEksemplarsByBarcodes(scannedBarcodes)
            } else if (scannedBarcode != null) {
                when (scanMode) {
                    "siswa" -> viewModel.findSiswaByBarcode(scannedBarcode)
                    "buku" -> viewModel.findEksemplarByBarcode(scannedBarcode)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeminjamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        observeData()
    }

    private fun setupActions() {
        binding.btnScanSiswa.setOnClickListener {
            scanMode = "siswa"
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }

        binding.btnCariSiswa.setOnClickListener {
            val query = binding.etCariSiswa.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchSiswa(query)
                binding.etCariSiswa.setText("")
            } else {
                Toast.makeText(requireContext(), "Masukkan nama atau barcode pengunjung", Toast.LENGTH_SHORT).show()
            }
        }

        binding.etCariSiswa.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            ) {
                val query = binding.etCariSiswa.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchSiswa(query)
                    binding.etCariSiswa.setText("")
                }
                true
            } else {
                false
            }
        }

        binding.btnScanBuku.setOnClickListener {
            scanMode = "buku"
            val intent = Intent(requireContext(), ScannerActivity::class.java).apply {
                putExtra(ScannerActivity.EXTRA_IS_CONTINUOUS, true)
            }
            scannerLauncher.launch(intent)
        }

        binding.btnCariBuku.setOnClickListener {
            val query = binding.etCariBarcode.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchBook(query)
                binding.etCariBarcode.setText("")
            } else {
                Toast.makeText(requireContext(), "Masukkan nama atau barcode buku", Toast.LENGTH_SHORT).show()
            }
        }

        binding.etCariBarcode.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            ) {
                val query = binding.etCariBarcode.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchBook(query)
                    binding.etCariBarcode.setText("")
                }
                true
            } else {
                false
            }
        }

        binding.btnProses.setOnClickListener {
            val durasiText = binding.etDurasi.text.toString().trim()
            val durasi = durasiText.toIntOrNull() ?: 7
            if (durasi <= 0) {
                Toast.makeText(requireContext(), "Durasi harus lebih dari 0 hari", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.pinjamBuku(durasi)
        }
    }

    private fun observeData() {
        viewModel.selectedSiswa.observe(viewLifecycleOwner) { siswa ->
            binding.layoutSiswaInfo.isVisible = siswa != null
            if (siswa != null) {
                binding.tvSiswaName.text = if (siswa.tipe == "umum") "${siswa.namaSiswa} (Umum)" else "${siswa.namaSiswa} (Siswa)"
                binding.tvSiswaKartu.text = "No. Kartu: ${siswa.nomorKartu}"
                binding.btnScanSiswa.text = "✓ ${siswa.namaSiswa}"
            } else {
                binding.btnScanSiswa.text = "Scan Barcode Pengunjung"
            }
        }

        viewModel.selectedEksemplars.observe(viewLifecycleOwner) { eksemplars ->
            binding.layoutBukuInfo.isVisible = eksemplars != null && eksemplars.isNotEmpty()
            if (eksemplars != null && eksemplars.isNotEmpty()) {
                binding.tvBukuBarcode.text = "${eksemplars.size} buku terpilih"
                binding.btnScanBuku.text = "✓ ${eksemplars.size} Buku Terpilih"
            } else {
                binding.btnScanBuku.text = "Scan Barcode Buku"
            }
        }

        viewModel.bukuJudul.observe(viewLifecycleOwner) { judul ->
            binding.tvBukuJudul.text = judul ?: ""
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.visitorSearchResults.observe(viewLifecycleOwner) { list ->
            if (list != null && list.isNotEmpty()) {
                val items = list.map { "${it.namaSiswa} (${if (it.tipe == "umum") "Umum" else "Siswa"} - ${it.nomorKartu})" }.toTypedArray()
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Pengunjung")
                    .setItems(items) { _, which ->
                        viewModel.selectSiswa(list[which])
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        viewModel.clearSearchResults()
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        viewModel.clearSearchResults()
                    }
                    .show()
            }
        }

        viewModel.bookSearchResults.observe(viewLifecycleOwner) { list ->
            if (list != null && list.isNotEmpty()) {
                val items = list.map { it.second }.toTypedArray()
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Buku")
                    .setItems(items) { _, which ->
                        viewModel.selectEksemplar(list[which].first)
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        viewModel.clearSearchResults()
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        viewModel.clearSearchResults()
                    }
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        _binding = null
    }
}
