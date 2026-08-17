package com.example.perpusanaksholeh.ui.buku

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
import com.example.perpusanaksholeh.databinding.FragmentBukuListBinding

class BukuListFragment : Fragment() {

    private var _binding: FragmentBukuListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BukuViewModel by viewModels()
    private lateinit var adapter: BukuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBukuListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeData()

        binding.fabAdd.setOnClickListener {
            val bundle = Bundle().apply { putInt("bukuId", -1) }
            findNavController().navigate(R.id.bukuFormFragment, bundle)
        }
    }

    private fun setupRecyclerView() {
        adapter = BukuAdapter { bukuWithEksemplar ->
            val bundle = Bundle().apply { putInt("bukuId", bukuWithEksemplar.buku.id) }
            findNavController().navigate(R.id.bukuDetailFragment, bundle)
        }
        binding.rvBuku.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBuku.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.search(text.toString())
        }
    }

    private fun observeData() {
        viewModel.searchResults.observe(viewLifecycleOwner) { bukuList ->
            adapter.submitList(bukuList)
            binding.tvEmpty.isVisible = bukuList.isEmpty()
            binding.rvBuku.isVisible = bukuList.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
