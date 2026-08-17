package com.example.perpusanaksholeh.ui.siswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.perpusanaksholeh.R
import com.example.perpusanaksholeh.data.database.entity.Siswa
import com.example.perpusanaksholeh.databinding.FragmentSiswaListBinding

class SiswaListFragment : Fragment() {

    private var _binding: FragmentSiswaListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SiswaViewModel by viewModels()
    private lateinit var adapter: SiswaAdapter

    private var selectedTab = 0 // 0: Siswa, 1: Umum

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiswaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTabs()
        observeData()

        binding.fabAdd.setOnClickListener {
            val bundle = Bundle().apply { 
                putInt("siswaId", -1)
                putString("defaultTipe", if (selectedTab == 1) "umum" else "siswa")
            }
            findNavController().navigate(R.id.siswaFormFragment, bundle)
        }
    }

    private fun setupRecyclerView() {
        adapter = SiswaAdapter { siswa ->
            val bundle = Bundle().apply { putInt("siswaId", siswa.id) }
            findNavController().navigate(R.id.siswaDetailFragment, bundle)
        }
        binding.rvSiswa.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSiswa.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()
            if (selectedTab == 0) {
                viewModel.searchSiswa(query)
            } else {
                viewModel.searchUmum(query)
            }
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                val query = binding.etSearch.text.toString()
                if (selectedTab == 0) {
                    viewModel.searchSiswa(query)
                    viewModel.searchResultsSiswa.value?.let { updateList(it) }
                } else {
                    viewModel.searchUmum(query)
                    viewModel.searchResultsUmum.value?.let { updateList(it) }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun observeData() {
        viewModel.searchResultsSiswa.observe(viewLifecycleOwner) { list ->
            if (selectedTab == 0) {
                updateList(list)
            }
        }

        viewModel.searchResultsUmum.observe(viewLifecycleOwner) { list ->
            if (selectedTab == 1) {
                updateList(list)
            }
        }
    }

    private fun updateList(list: List<Siswa>) {
        adapter.submitList(list)
        binding.tvEmpty.isVisible = list.isEmpty()
        binding.rvSiswa.isVisible = list.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
