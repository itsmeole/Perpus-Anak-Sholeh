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
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.Peminjaman
import com.example.perpusanaksholeh.databinding.FragmentPengembalianBinding
import com.example.perpusanaksholeh.ui.scanner.ScannerActivity
import com.example.perpusanaksholeh.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PengembalianFragment : Fragment() {

    private var _binding: FragmentPengembalianBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PeminjamanViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedBarcode = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            if (scannedBarcode != null) {
                viewModel.findPeminjamanByBarcode(scannedBarcode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengembalianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        observeData()
    }

    private fun setupActions() {
        binding.btnScanBuku.setOnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }

        binding.btnKembalikan.setOnClickListener {
            viewModel.kembalikanBuku()
        }
    }

    private fun observeData() {
        viewModel.pengembalianDetail.observe(viewLifecycleOwner) { detail ->
            binding.cardDetail.isVisible = detail != null
            binding.btnKembalikan.isVisible = detail != null

            if (detail != null) {
                binding.tvNamaSiswa.text = detail.namaSiswa
                binding.tvJudulBuku.text = detail.judulBuku
                binding.tvTanggalPinjam.text = dateFormat.format(Date(detail.tanggalPinjam))
                binding.tvJatuhTempo.text = dateFormat.format(Date(detail.tanggalJatuhTempo))

                when (detail.status) {
                    Peminjaman.STATUS_DIPINJAM -> {
                        binding.tvStatus.text = "Dipinjam"
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_dipinjam)
                    }
                    Peminjaman.STATUS_TERLAMBAT -> {
                        binding.tvStatus.text = "Terlambat"
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_terlambat)
                    }
                }

                val now = System.currentTimeMillis()
                if (now > detail.tanggalJatuhTempo) {
                    val selisihMs = now - detail.tanggalJatuhTempo
                    val hariTerlambat = TimeUnit.MILLISECONDS.toDays(selisihMs).toInt() + 1
                    val denda = hariTerlambat * Peminjaman.DENDA_PER_HARI

                    binding.layoutDenda.isVisible = true
                    binding.tvDenda.text = "Rp ${String.format("%,d", denda)} ($hariTerlambat hari)"
                } else {
                    binding.layoutDenda.isVisible = false
                }

                binding.btnScanBuku.text = "✓ Buku ditemukan - scan ulang?"
            } else {
                binding.btnScanBuku.text = "Scan Barcode Buku"
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        _binding = null
    }
}
