package com.team4.urwayuta.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.team4.urwayuta.R
import com.team4.urwayuta.data.model.CampusSpot
import com.team4.urwayuta.data.model.CustomPin
import com.team4.urwayuta.data.model.SpotType
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private var googleMap: GoogleMap? = null
    private var currentTab = 0
    private var droppedPin: Marker? = null
    private var droppedLatLng: LatLng? = null
    private val customPinMarkers = mutableMapOf<Marker, CustomPin>()

    private val buildingLatLng = mapOf(
        "ERB"  to LatLng(32.7322, -97.1149),
        "NH"   to LatLng(32.7316, -97.1141),
        "UH"   to LatLng(32.7295, -97.1148),
        "SEIR" to LatLng(32.7289, -97.1148)
    )
    private val buildingCoords = mapOf(
        "ERB"  to Pair(32.7322, -97.1149),
        "NH"   to Pair(32.7316, -97.1141),
        "UH"   to Pair(32.7295, -97.1148),
        "SEIR" to Pair(32.7289, -97.1148)
    )
    private val utaCampusCenter = LatLng(32.7306, -97.1145)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupTabs()
        setupLinkButton()
        binding.btnBuildings.setOnClickListener { showBuildingPickerSheet() }
        binding.btnCustomPin.setOnClickListener { findNavController().navigate(R.id.customPinFragment) }
        showMapTab()
    }

    override fun onResume() {
        super.onResume()
        if (currentTab == 1) showFavoritesTab()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(utaCampusCenter, 16f))
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            setOnMapClickListener { latLng -> dropNavigationPin(latLng) }
            setOnMarkerClickListener { marker ->
                val customPin = customPinMarkers[marker]
                if (customPin != null) {
                    showPinSheet(marker.position, customPin.description, onRemove = {
                        marker.remove()
                        customPinMarkers.remove(marker)
                        AppRepository.removeCustomPin(customPin.pinID)
                    })
                    true
                } else if (marker == droppedPin) {
                    droppedLatLng?.let { showPinSheet(it, null, onRemove = {
                        droppedPin?.remove(); droppedPin = null; droppedLatLng = null
                    }) }
                    true
                } else false
            }
        }
    }

    private fun dropNavigationPin(latLng: LatLng) {
        droppedPin?.remove()
        droppedPin = googleMap?.addMarker(
            MarkerOptions().position(latLng).title("Navigate here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true)
        )
        droppedLatLng = latLng
        showPinSheet(latLng, null, onRemove = {
            droppedPin?.remove(); droppedPin = null; droppedLatLng = null
        })
    }

    private fun showPinSheet(
        latLng: LatLng,
        description: String?,
        onRemove: () -> Unit,
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.sheet_navigation_pin, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<android.widget.TextView>(R.id.tvPinCoords).text =
            description?.takeIf { it.isNotBlank() }
                ?: "%.5f, %.5f".format(latLng.latitude, latLng.longitude)

        sheetView.findViewById<android.widget.Button>(R.id.btnNavigatePin).setOnClickListener {
            dialog.dismiss()
            val uri = Uri.parse("google.navigation:q=${latLng.latitude},${latLng.longitude}&mode=w")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        sheetView.findViewById<android.widget.Button>(R.id.btnClearPin).setOnClickListener {
            onRemove()
            dialog.dismiss()
            if (currentTab == 1) showFavoritesTab()
        }

        sheetView.findViewById<android.widget.TextView>(R.id.btnDismissPin).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener { onDismiss?.invoke() }
        dialog.show()
    }

    private fun showBuildingPickerSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.sheet_building_picker, null)
        dialog.setContentView(sheetView)
        sheetView.findViewById<View>(R.id.rowErb).setOnClickListener { dialog.dismiss(); openBuildingSheet("ERB") }
        sheetView.findViewById<View>(R.id.rowNh).setOnClickListener { dialog.dismiss(); openBuildingSheet("NH") }
        sheetView.findViewById<View>(R.id.rowUh).setOnClickListener { dialog.dismiss(); openBuildingSheet("UH") }
        sheetView.findViewById<View>(R.id.rowSeir).setOnClickListener { dialog.dismiss(); openBuildingSheet("SEIR") }
        sheetView.findViewById<View>(R.id.btnNavigateErb).setOnClickListener { dialog.dismiss(); navigateToBuilding("ERB") }
        sheetView.findViewById<View>(R.id.btnNavigateNh).setOnClickListener { dialog.dismiss(); navigateToBuilding("NH") }
        sheetView.findViewById<View>(R.id.btnNavigateUh).setOnClickListener { dialog.dismiss(); navigateToBuilding("UH") }
        sheetView.findViewById<View>(R.id.btnNavigateSeir).setOnClickListener { dialog.dismiss(); navigateToBuilding("SEIR") }
        sheetView.findViewById<View>(R.id.btnCancelPicker).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun navigateToBuilding(building: String) {
        val coords = buildingCoords[building] ?: return
        val uri = Uri.parse("google.navigation:q=${coords.first},${coords.second}&mode=w")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun openBuildingSheet(building: String) {
        val spots  = AppRepository.getNearbySpots(building)
        val routes = AppRepository.walkingTimes[building] ?: emptyMap()
        val coords = AppRepository.buildingCoords[building]
        BuildingBottomSheet.newInstance(building, spots, routes, coords)
            .show(childFragmentManager, "building_sheet")
        buildingLatLng[building]?.let {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))
        }
    }

    private fun setupTabs() {
        binding.tabMap.setOnClickListener       { switchTab(0) }
        binding.tabFavorites.setOnClickListener { switchTab(1) }
        binding.tabForYou.setOnClickListener    { switchTab(2) }
    }

    private fun switchTab(tab: Int) {
        currentTab = tab
        val accent = requireContext().getColor(R.color.colorAccent)
        val hint   = requireContext().getColor(R.color.colorTextHint)
        binding.tabMap.setTextColor(if (tab == 0) accent else hint)
        binding.tabFavorites.setTextColor(if (tab == 1) accent else hint)
        binding.tabForYou.setTextColor(if (tab == 2) accent else hint)
        binding.tabMap.setBackgroundResource(if (tab == 0) R.drawable.tab_selected_bottom else 0)
        binding.tabFavorites.setBackgroundResource(if (tab == 1) R.drawable.tab_selected_bottom else 0)
        binding.tabForYou.setBackgroundResource(if (tab == 2) R.drawable.tab_selected_bottom else 0)
        when (tab) { 0 -> showMapTab(); 1 -> showFavoritesTab(); 2 -> showForYouTab() }
    }

    private fun showMapTab() {
        binding.mapContainer.visibility       = View.VISIBLE
        binding.layoutFavHeader.visibility    = View.GONE
        binding.layoutForYouHeader.visibility = View.GONE
        binding.rvSpots.visibility            = View.GONE
        binding.tvEmptyFav.visibility         = View.GONE
    }

    private fun showFavoritesTab() {
        binding.mapContainer.visibility       = View.GONE
        binding.layoutForYouHeader.visibility = View.GONE
        binding.layoutFavHeader.visibility    = View.VISIBLE

        val favSpots   = AppRepository.getFavoriteSpots()
        val customPins = AppRepository.customPins

        if (favSpots.isEmpty() && customPins.isEmpty()) {
            binding.rvSpots.visibility    = View.GONE
            binding.tvEmptyFav.visibility = View.VISIBLE
            return
        }

        binding.tvEmptyFav.visibility = View.GONE
        binding.rvSpots.visibility    = View.VISIBLE
        binding.rvSpots.layoutManager = LinearLayoutManager(requireContext())

        val pinAsSpots = customPins.map { pin ->
            CampusSpot(
                spotID       = pin.pinID,
                spotName     = pin.name,
                spotType     = SpotType.CLICK_SPOT,
                buildingName = "",
                floor        = "",
                available    = true,
                latitude     = pin.xPercent.toDouble(),
                longitude    = pin.yPercent.toDouble()
            )
        }

        val customPinIDs = customPins.map { it.pinID }.toSet()
        val allItems = favSpots + pinAsSpots

        binding.rvSpots.adapter = SpotAdapter(allItems) { spot ->
            if (customPinIDs.contains(spot.spotID)) {
                val pin = customPins.find { it.pinID == spot.spotID }
                if (pin != null) {
                    val latLng = LatLng(pin.xPercent.toDouble(), pin.yPercent.toDouble())
                    showPinSheet(
                        latLng      = latLng,
                        description = pin.description.ifBlank { pin.name },
                        onRemove    = {
                            AppRepository.removeCustomPin(pin.pinID)
                            showFavoritesTab()
                        }
                    )
                }
            } else {
                openSpotDetail(spot)
            }
        }
    }

    private fun showForYouTab() {
        binding.mapContainer.visibility       = View.GONE
        binding.layoutFavHeader.visibility    = View.GONE
        binding.tvEmptyFav.visibility         = View.GONE
        binding.layoutForYouHeader.visibility = View.VISIBLE

        val recommended = AppRepository.getRecommendedSpots()
        if (recommended.isEmpty()) {
            binding.rvSpots.visibility = View.GONE
        } else {
            binding.rvSpots.visibility    = View.VISIBLE
            binding.rvSpots.layoutManager = LinearLayoutManager(requireContext())
            binding.rvSpots.adapter = SpotAdapter(recommended) { spot -> openSpotDetail(spot) }
        }
    }

    private fun setupLinkButton() {
        binding.btnFullMap.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://maps.uta.edu/?id=2229#!ct/88375,88424,88425?s/")))
        }
    }

    private fun openSpotDetail(spot: CampusSpot) {
        SpotDetailBottomSheet.newInstance(spot.spotID).show(childFragmentManager, "spot_detail")
    }
}