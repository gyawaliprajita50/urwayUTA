package com.team4.urwayuta.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.team4.urwayuta.data.model.CustomPin
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.FragmentCustomPinBinding

class CustomPinFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCustomPinBinding
    private var pinMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null
    private val utaCenter = LatLng(32.7306, -97.1145)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBackPin.setOnClickListener { findNavController().popBackStack() }

        val mapFrag = childFragmentManager
            .findFragmentById(com.team4.urwayuta.R.id.pinMap) as SupportMapFragment
        mapFrag.getMapAsync(this)

        binding.switchVisibility.setOnCheckedChangeListener { _, isChecked ->
            binding.tvVisibilityLabel.text = if (isChecked) "Public" else "Private"
        }

        binding.btnSavePin.setOnClickListener { savePin() }
    }

    override fun onMapReady(map: GoogleMap) {
        pinMap = map
        pinMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(utaCenter, 15f))
        pinMap?.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            pinMap?.clear()
            pinMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("My Pin")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            binding.tvPinCoords.text =
                "Pin placed at %.4f, %.4f".format(latLng.latitude, latLng.longitude)
        }
    }

    private fun savePin() {
        val name = binding.etPinName.text.toString().trim()
        if (name.isBlank()) {
            binding.tvPinError.visibility = View.VISIBLE
            binding.tvPinError.text = "Spot name is required."
            return
        }
        // Use time-based ID > 10000 to never clash with real spot IDs (1-40)
        val pinID = (System.currentTimeMillis() % 1_000_000 + 10_000).toInt()
        val pin = CustomPin(
            pinID       = pinID,
            studentID   = com.team4.urwayuta.ui.auth.CurrentSession.studentID,
            name        = name,
            description = binding.etPinDesc.text.toString().trim(),
            xPercent    = selectedLatLng?.latitude?.toFloat() ?: utaCenter.latitude.toFloat(),
            yPercent    = selectedLatLng?.longitude?.toFloat() ?: utaCenter.longitude.toFloat(),
            isPublic    = binding.switchVisibility.isChecked
        )
        AppRepository.addCustomPin(pin)
        findNavController().popBackStack()
    }
}