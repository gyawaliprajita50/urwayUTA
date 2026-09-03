package com.team4.urwayuta.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.team4.urwayuta.databinding.FragmentServicesBinding

class ServicesFragment : Fragment() {
    private lateinit var binding: FragmentServicesBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Services tab — placeholder screens with "Coming Soon" badges
    // Real implementation: Printing, Library, Shuttle, Dining
}
