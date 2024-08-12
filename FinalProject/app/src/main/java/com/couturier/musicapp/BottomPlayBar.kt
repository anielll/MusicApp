package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.couturier.musicapp.databinding.BottomBarBinding

class BottomPlayBar : Fragment(){
    private var _binding: BottomBarBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomBarBinding.inflate(inflater, container, false)
        return binding.root
    }
}