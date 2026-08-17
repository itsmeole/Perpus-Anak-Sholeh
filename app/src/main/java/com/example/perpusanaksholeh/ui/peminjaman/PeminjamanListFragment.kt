package com.example.perpusanaksholeh.ui.peminjaman

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.databinding.FragmentPeminjamanListBinding
import com.google.android.material.tabs.TabLayout

class PeminjamanListFragment : Fragment() {

    private var _binding: FragmentPeminjamanListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PeminjamanViewModel by viewModels()
    private lateinit var adapter: PeminjamanAdapter
    private var currentTab = 0 // 0 = aktif, 1 = riwayat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeminjamanListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        setupActions()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = PeminjamanAdapter()
        binding.rvPeminjaman.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPeminjaman.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                loadData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupActions() {
        binding.btnPinjam.setOnClickListener {
            findNavController().navigate(R.id.peminjamanFragment)
        }

        binding.btnKembali.setOnClickListener {
            findNavController().navigate(R.id.pengembalianFragment)
        }
    }

    private fun loadData() {
        // Remove previous observers
        viewModel.peminjamanAktif.removeObservers(viewLifecycleOwner)
        viewModel.riwayatPeminjaman.removeObservers(viewLifecycleOwner)

        when (currentTab) {
            0 -> {
                viewModel.peminjamanAktif.observe(viewLifecycleOwner) { list ->
                    adapter.submitList(list)
                    binding.tvEmpty.isVisible = list.isEmpty()
                    binding.rvPeminjaman.isVisible = list.isNotEmpty()
                }
            }
            1 -> {
                viewModel.riwayatPeminjaman.observe(viewLifecycleOwner) { list ->
                    adapter.submitList(list)
                    binding.tvEmpty.isVisible = list.isEmpty()
                    binding.rvPeminjaman.isVisible = list.isNotEmpty()
                }
            }
        }
    }

    private fun observeData() {
        // Default: show aktif
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
