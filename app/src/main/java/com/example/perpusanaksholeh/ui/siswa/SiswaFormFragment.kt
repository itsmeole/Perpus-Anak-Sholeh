package com.example.perpusanaksholeh.ui.siswa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.databinding.FragmentSiswaFormBinding
import com.example.perpusanaksholeh.ui.scanner.ScannerActivity
import com.example.perpusanaksholeh.util.BarcodeGenerator
import com.example.perpusanaksholeh.util.Resource

class SiswaFormFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSiswaFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SiswaViewModel by viewModels()
    private var editSiswaId: Int = -1
    private var existingSiswa: Siswa? = null

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedBarcode = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            if (scannedBarcode != null) {
                binding.etBarcode.setText(scannedBarcode)
                updateBarcodePreview(scannedBarcode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiswaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editSiswaId = arguments?.getInt("siswaId", -1) ?: -1

        setupDropdowns()
        setupActions()
        observeResults()

        val defaultTipe = arguments?.getString("defaultTipe", "siswa") ?: "siswa"
        if (editSiswaId != -1) {
            binding.tvFormTitle.text = "Ubah Pengunjung"
            loadSiswaData()
        } else {
            binding.actvTipe.setText(if (defaultTipe == "umum") "Umum" else "Siswa", false)
            updateFieldVisibility(defaultTipe)
        }
    }

    private fun setupDropdowns() {
        val tipeOptions = arrayOf("Siswa", "Umum")
        val tipeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipeOptions)
        binding.actvTipe.setAdapter(tipeAdapter)

        val usiaOptions = arrayOf(
            "Anak-anak (0 - 12 tahun)",
            "Remaja (13 - 17 tahun)",
            "Dewasa (18 - 59 tahun)",
            "Lansia (60 tahun ke atas)"
        )
        val usiaAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, usiaOptions)
        binding.actvUsiaRange.setAdapter(usiaAdapter)
    }

    private fun updateFieldVisibility(tipe: String) {
        val isSiswa = (tipe == "siswa")
        binding.tilKelas.isVisible = isSiswa
        
        binding.tilUsiaRange.isVisible = !isSiswa
        binding.tilAlamat.isVisible = !isSiswa
        binding.tilPekerjaan.isVisible = !isSiswa
        binding.tilInstansi.isVisible = !isSiswa

        if (isSiswa) {
            binding.tilNamaSiswa.hint = "Nama Siswa"
            binding.tilBarcode.hint = "Barcode Siswa"
        } else {
            binding.tilNamaSiswa.hint = "Nama Pengunjung"
            binding.tilBarcode.hint = "Barcode Pengunjung"
        }
    }

    private fun loadSiswaData() {
        viewModel.loadSiswa(editSiswaId)
        viewModel.selectedSiswa.observe(viewLifecycleOwner) { siswa ->
            if (siswa != null) {
                existingSiswa = siswa
                binding.etNomorKartu.setText(siswa.nomorKartu)
                binding.etNamaSiswa.setText(siswa.namaSiswa)
                binding.etBarcode.setText(siswa.barcode)
                binding.actvTipe.setText(if (siswa.tipe == "umum") "Umum" else "Siswa", false)
                
                if (siswa.tipe == "umum") {
                    binding.actvUsiaRange.setText(siswa.usiaRange ?: "", false)
                    binding.etAlamat.setText(siswa.alamat ?: "")
                    binding.etPekerjaan.setText(siswa.pekerjaan ?: "")
                    binding.etInstansi.setText(siswa.instansi ?: "")
                } else {
                    binding.etKelas.setText(siswa.kelas ?: "")
                }
                updateFieldVisibility(siswa.tipe)
                updateBarcodePreview(siswa.barcode)
            }
        }
    }

    private fun setupActions() {
        binding.actvTipe.setOnItemClickListener { _, _, position, _ ->
            val selectedType = if (position == 1) "umum" else "siswa"
            updateFieldVisibility(selectedType)
        }

        binding.btnScanBarcode.setOnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }

        binding.btnGenerateBarcode.setOnClickListener {
            val tipeStr = binding.actvTipe.text.toString().trim()
            val prefix = if (tipeStr == "Umum") "VSTR" else "STD"
            val code = BarcodeGenerator.generateUniqueCode(prefix)
            binding.etBarcode.setText(code)
            updateBarcodePreview(code)
        }

        binding.btnSimpan.setOnClickListener {
            saveSiswa()
        }
    }

    private fun saveSiswa() {
        val tipe = if (binding.actvTipe.text.toString().trim() == "Umum") "umum" else "siswa"
        val nomorKartu = binding.etNomorKartu.text.toString().trim()
        val namaSiswa = binding.etNamaSiswa.text.toString().trim()
        val barcode = binding.etBarcode.text.toString().trim()

        if (nomorKartu.isEmpty()) {
            binding.tilNomorKartu.error = "Nomor kartu wajib diisi"
            return
        }
        if (namaSiswa.isEmpty()) {
            binding.tilNamaSiswa.error = "Nama wajib diisi"
            return
        }
        if (barcode.isEmpty()) {
            binding.tilBarcode.error = "Barcode wajib diisi"
            return
        }

        var kelas: String? = null
        var usiaRange: String? = null
        var alamat: String? = null
        var pekerjaan: String? = null
        var instansi: String? = null

        if (tipe == "siswa") {
            kelas = binding.etKelas.text.toString().trim()
            if (kelas.isEmpty()) {
                binding.tilKelas.error = "Kelas wajib diisi"
                return
            }
        } else {
            usiaRange = binding.actvUsiaRange.text.toString().trim()
            alamat = binding.etAlamat.text.toString().trim()
            pekerjaan = binding.etPekerjaan.text.toString().trim()
            instansi = binding.etInstansi.text.toString().trim()

            if (usiaRange.isEmpty()) {
                binding.tilUsiaRange.error = "Kategori usia wajib diisi"
                return
            }
            if (alamat.isEmpty()) {
                binding.tilAlamat.error = "Alamat wajib diisi"
                return
            }
        }

        binding.tilNomorKartu.error = null
        binding.tilNamaSiswa.error = null
        binding.tilBarcode.error = null
        binding.tilKelas.error = null
        binding.tilUsiaRange.error = null
        binding.tilAlamat.error = null

        if (editSiswaId != -1 && existingSiswa != null) {
            val updatedSiswa = existingSiswa!!.copy(
                nomorKartu = nomorKartu,
                namaSiswa = namaSiswa,
                barcode = barcode,
                tipe = tipe,
                kelas = kelas,
                usiaRange = usiaRange,
                alamat = alamat,
                pekerjaan = pekerjaan,
                instansi = instansi
            )
            viewModel.update(updatedSiswa)
        } else {
            val siswa = Siswa(
                nomorKartu = nomorKartu,
                namaSiswa = namaSiswa,
                barcode = barcode,
                tipe = tipe,
                kelas = kelas,
                usiaRange = usiaRange,
                alamat = alamat,
                pekerjaan = pekerjaan,
                instansi = instansi
            )
            viewModel.insert(siswa)
        }
    }

    private fun updateBarcodePreview(barcode: String) {
        if (barcode.isNotEmpty()) {
            val bitmap = BarcodeGenerator.generateBarcode(barcode)
            binding.ivBarcodePreview.setImageBitmap(bitmap)
            binding.tvBarcodeText.text = barcode
            binding.cardBarcodePreview.isVisible = true
        } else {
            binding.cardBarcodePreview.isVisible = false
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
