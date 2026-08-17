package com.example.perpusanaksholeh.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.databinding.FragmentDashboardBinding
import com.example.perpusanaksholeh.util.Resource

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData { jsonString ->
                try {
                    requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(requireContext(), "Data berhasil diekspor!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal menyimpan file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                if (jsonString != null) {
                    viewModel.importData(jsonString)
                } else {
                    Toast.makeText(requireContext(), "File kosong", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membaca file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
        setupActions()
    }

    private fun observeData() {
        viewModel.totalSiswa.observe(viewLifecycleOwner) { count ->
            binding.tvTotalSiswa.text = count.toString()
        }

        viewModel.totalBuku.observe(viewLifecycleOwner) { count ->
            binding.tvTotalBuku.text = count.toString()
        }

        viewModel.totalEksemplar.observe(viewLifecycleOwner) { count ->
            binding.tvTotalEksemplar.text = count.toString()
        }

        viewModel.peminjamanAktif.observe(viewLifecycleOwner) { count ->
            binding.tvPeminjamanAktif.text = count.toString()
        }

        viewModel.peminjamanTerlambat.observe(viewLifecycleOwner) { count ->
            binding.tvTerlambat.text = count.toString()
        }

        viewModel.backupResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data.isNotEmpty()) {
                        Toast.makeText(requireContext(), result.data, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    if (result.message.isNotEmpty()) {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupActions() {
        binding.btnPinjamBuku.setOnClickListener {
            findNavController().navigate(R.id.peminjamanFragment)
        }

        binding.btnKembalikanBuku.setOnClickListener {
            findNavController().navigate(R.id.pengembalianFragment)
        }

        binding.btnExportData.setOnClickListener {
            exportLauncher.launch("perpus_backup_${System.currentTimeMillis()}.json")
        }

        binding.btnImportData.setOnClickListener {
            importLauncher.launch("application/json")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
