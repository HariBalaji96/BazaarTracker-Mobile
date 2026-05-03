package com.example.bazaartrackermobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        binding.btnChangePassword.setOnClickListener {
            // Navigate to Change Password if exists, or show toast
            Toast.makeText(requireContext(), "Change Password coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Terms & Conditions", Toast.LENGTH_SHORT).show()
        }

        binding.btnPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
        
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.tvVersion.text = pInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logout() {
        AuthTokenManager(requireContext()).clearToken()
        RetrofitClient.resetClient()
        val intent = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        intent?.let {
            startActivity(it)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
