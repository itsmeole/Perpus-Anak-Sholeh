package com.example.perpusanaksholeh.ui.buku

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.Buku
import com.example.perpusanaksholeh.data.model.BukuWithEksemplar
import com.example.perpusanaksholeh.databinding.DialogEksemplarFormBinding
import com.example.perpusanaksholeh.databinding.FragmentBukuDetailBinding
import com.example.perpusanaksholeh.ui.scanner.ScannerActivity
import com.example.perpusanaksholeh.util.BarcodeGenerator
import com.example.perpusanaksholeh.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BukuDetailFragment : Fragment() {

    private var _binding: FragmentBukuDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BukuViewModel by viewModels()
    private var currentBuku: Buku? = null
    private var currentBukuId: Int = -1
    private lateinit var eksemplarAdapter: EksemplarAdapter

    private var dialogBarcodeCallback: ((String) -> Unit)? = null
    private var dialogMultipleBarcodeCallback: ((List<String>) -> Unit)? = null

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedBarcode = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            val scannedBarcodes = result.data?.getStringArrayListExtra(ScannerActivity.EXTRA_SCAN_RESULTS)
            if (scannedBarcodes != null) {
                dialogMultipleBarcodeCallback?.invoke(scannedBarcodes)
            } else if (scannedBarcode != null) {
                dialogBarcodeCallback?.invoke(scannedBarcode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBukuDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBukuId = arguments?.getInt("bukuId") ?: return

        setupRecyclerView()
        loadData()
        setupActions()
        observeResults()
    }

    private fun setupRecyclerView() {
        eksemplarAdapter = EksemplarAdapter(
            onDeleteClick = { eksemplar ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.hapus_eksemplar))
                    .setMessage(getString(R.string.konfirmasi_hapus_eksemplar))
                    .setPositiveButton(getString(R.string.ya)) { _, _ ->
                        viewModel.deleteEksemplar(eksemplar)
                    }
                    .setNegativeButton(getString(R.string.tidak), null)
                    .show()
            },
            onDownloadClick = { eksemplar ->
                currentBuku?.let { buku ->
                    val cardBinding = com.example.perpusanaksholeh.databinding.LayoutDownloadEksemplarBinding.inflate(layoutInflater)
                    cardBinding.tvJudul.text = buku.judul
                    cardBinding.tvPenulis.text = buku.penulis
                    cardBinding.tvPenerbit.text = buku.penerbit ?: "-"
                    cardBinding.tvKategori.text = buku.kategori ?: "-"
                    cardBinding.tvTahunTerbit.text = buku.tahunTerbit?.toString() ?: "-"
                    
                    cardBinding.tvBarcodeValue.text = eksemplar.barcode
                    val barcodeBitmap = BarcodeGenerator.generateBarcode(eksemplar.barcode, 400, 100)
                    cardBinding.ivBarcode.setImageBitmap(barcodeBitmap)

                    val fileName = "Kartu_Buku_${buku.judul.replace(" ", "_")}_${eksemplar.barcode}"
                    com.example.perpusanaksholeh.util.ImageExportHelper.exportViewToPng(cardBinding.root, fileName, requireContext())
                }
            }
        )
        binding.rvEksemplar.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEksemplar.adapter = eksemplarAdapter
    }

    private fun loadData() {
        viewModel.loadBuku(currentBukuId)
        viewModel.selectedBuku.observe(viewLifecycleOwner) { bukuWithEksemplar ->
            if (bukuWithEksemplar != null) {
                currentBuku = bukuWithEksemplar.buku
                displayBuku(bukuWithEksemplar)
            }
        }
    }

    private fun displayBuku(bukuWithEksemplar: BukuWithEksemplar) {
        val buku = bukuWithEksemplar.buku
        binding.tvJudul.text = buku.judul
        binding.tvPenulis.text = buku.penulis
        binding.tvPenerbit.text = buku.penerbit
        binding.tvKategori.text = buku.kategori
        binding.tvTahunTerbit.text = buku.tahunTerbit
        binding.tvJumlahEksemplar.text = "${bukuWithEksemplar.jumlahTotal} (${bukuWithEksemplar.jumlahTersedia} tersedia)"

        eksemplarAdapter.submitList(bukuWithEksemplar.eksemplarList)
        binding.tvEksemplarKosong.isVisible = bukuWithEksemplar.eksemplarList.isEmpty()
        binding.rvEksemplar.isVisible = bukuWithEksemplar.eksemplarList.isNotEmpty()
    }

    private fun setupActions() {
        binding.btnEdit.setOnClickListener {
            currentBuku?.let { buku ->
                val bundle = Bundle().apply { putInt("bukuId", buku.id) }
                findNavController().navigate(R.id.bukuFormFragment, bundle)
            }
        }

        binding.btnHapus.setOnClickListener {
            currentBuku?.let { buku ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.hapus_buku))
                    .setMessage(getString(R.string.konfirmasi_hapus_buku))
                    .setPositiveButton(getString(R.string.ya)) { _, _ ->
                        viewModel.deleteBuku(buku)
                    }
                    .setNegativeButton(getString(R.string.tidak), null)
                    .show()
            }
        }

        binding.btnTambahEksemplar.setOnClickListener {
            showAddEksemplarDialog()
        }
    }

    private fun showAddEksemplarDialog() {
        val dialogBinding = DialogEksemplarFormBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnScan.setOnClickListener {
            dialogMultipleBarcodeCallback = { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val joined = barcodes.joinToString("\n")
                    dialogBinding.etBarcode.setText(joined)
                    Toast.makeText(requireContext(), "${barcodes.size} barcode terpilih. Silakan tinjau sebelum disimpan.", Toast.LENGTH_LONG).show()
                }
            }
            val intent = Intent(requireContext(), ScannerActivity::class.java).apply {
                putExtra(ScannerActivity.EXTRA_IS_CONTINUOUS, true)
            }
            scannerLauncher.launch(intent)
        }

        dialogBinding.btnGenerate.setOnClickListener {
            val code = BarcodeGenerator.generateUniqueCode("BK")
            dialogBinding.etBarcode.setText(code)
        }

        dialogBinding.btnSimpan.setOnClickListener {
            val rawText = dialogBinding.etBarcode.text.toString().trim()
            if (rawText.isEmpty()) {
                dialogBinding.tilBarcode.error = "Barcode wajib diisi"
                return@setOnClickListener
            }
            val barcodes = rawText.split(Regex("[\\n,]+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (barcodes.isEmpty()) {
                dialogBinding.tilBarcode.error = "Barcode wajib diisi"
                return@setOnClickListener
            }
            if (barcodes.size == 1) {
                viewModel.addEksemplar(currentBukuId, barcodes[0])
            } else {
                viewModel.addMultipleEksemplar(currentBukuId, barcodes)
            }
            dialog.dismiss()
        }

        dialogBinding.btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeResults() {
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_SHORT).show()
                    if (result.data.contains("Buku berhasil dihapus")) {
                        findNavController().popBackStack()
                    }
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
