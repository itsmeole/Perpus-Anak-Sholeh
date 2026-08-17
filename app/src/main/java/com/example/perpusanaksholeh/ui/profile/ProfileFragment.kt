package com.example.perpusanaksholeh.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.perpusanaksholeh.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("user_profile", Context.MODE_PRIVATE)

        // Load values
        val nama = prefs.getString("nama", "Admin") ?: "Admin"
        val email = prefs.getString("email", "admin@perpusanaksholeh.com") ?: "admin@perpusanaksholeh.com"
        val jabatan = prefs.getString("jabatan", "Operator Perpustakaan") ?: "Operator Perpustakaan"
        val instansi = prefs.getString("instansi", "Perpustakaan Anak Sholeh") ?: "Perpustakaan Anak Sholeh"

        // Set inputs
        binding.etNama.setText(nama)
        binding.etEmail.setText(email)
        binding.etJabatan.setText(jabatan)
        binding.etInstansi.setText(instansi)

        // Set displays
        binding.tvProfileNameDisplay.text = nama
        binding.tvProfileRoleDisplay.text = jabatan

        binding.btnSimpan.setOnClickListener {
            val newNama = binding.etNama.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim()
            val newJabatan = binding.etJabatan.text.toString().trim()
            val newInstansi = binding.etInstansi.text.toString().trim()

            if (newNama.isEmpty()) {
                binding.tilNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            } else {
                binding.tilNama.error = null
            }

            prefs.edit().apply {
                putString("nama", newNama)
                putString("email", newEmail)
                putString("jabatan", newJabatan)
                putString("instansi", newInstansi)
                apply()
            }

            binding.tvProfileNameDisplay.text = newNama
            binding.tvProfileRoleDisplay.text = newJabatan

            Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
