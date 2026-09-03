package com.team4.urwayuta.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.team4.urwayuta.R
import com.team4.urwayuta.data.model.CampusSpot
import com.team4.urwayuta.data.model.SpotType
import com.team4.urwayuta.data.repository.AppRepository

// ── SpotAdapter ────────────────────────────────────────────────────────────

class SpotAdapter(
    private val spots: List<CampusSpot>,
    private val onClick: (CampusSpot) -> Unit
) : RecyclerView.Adapter<SpotAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:   TextView  = view.findViewById(R.id.tvSpotName)
        val tvSub:    TextView  = view.findViewById(R.id.tvSpotSub)
        val tvDetail: TextView  = view.findViewById(R.id.tvSpotDetail)
        val tvAvail:  TextView  = view.findViewById(R.id.tvSpotAvail)
        val btnHeart: TextView  = view.findViewById(R.id.btnHeart)
        val ivIcon:   ImageView = view.findViewById(R.id.ivSpotIcon)
        val tvRec:    TextView  = view.findViewById(R.id.tvRecBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_spot, parent, false)
    )

    override fun getItemCount() = spots.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val spot = spots[position]

        // Name
        holder.tvName.text = spot.spotName

        // Rec badge
        holder.tvRec.visibility = if (spot.isRecommended) View.VISIBLE else View.GONE

        // Sub and detail
        holder.tvSub.text = "${spot.buildingName} · ${spot.floor}"
        holder.tvDetail.text = buildDetail(spot)

        // Availability
        holder.tvAvail.visibility = View.VISIBLE
        holder.tvAvail.text = if (spot.available) "Available" else "Unavailable"
        holder.tvAvail.setBackgroundResource(
            if (spot.available) R.drawable.bg_tag_green else R.drawable.bg_tag_red
        )

        // Icon based on spot type
        val iconRes = when (spot.spotType) {
            SpotType.MICROWAVE  -> R.drawable.ic_microwave
            SpotType.SEATING    -> R.drawable.ic_seating
            SpotType.VENDING    -> R.drawable.ic_vending
            SpotType.CAFE       -> R.drawable.ic_cafe
            SpotType.LIBRARY    -> R.drawable.ic_library
            SpotType.PHOTO      -> R.drawable.ic_photo
            SpotType.STUDY      -> R.drawable.ic_study
            SpotType.CLICK_SPOT -> R.drawable.ic_map_pin
        }
        holder.ivIcon.setImageResource(iconRes)

        // Heart — custom pins (empty buildingName) always show filled red heart
        holder.btnHeart.visibility = View.VISIBLE
        val isCustomPin = spot.buildingName.isEmpty()
        val fav = isCustomPin || AppRepository.isFavorite(spot.spotID)
        holder.btnHeart.text = if (fav) "♥" else "♡"
        holder.btnHeart.setTextColor(
            if (fav) holder.itemView.context.getColor(R.color.heart_active)
            else holder.itemView.context.getColor(R.color.heart_inactive)
        )
        holder.btnHeart.setOnClickListener {
            if (!isCustomPin) {
                if (AppRepository.isFavorite(spot.spotID)) AppRepository.removeFavorite(spot.spotID)
                else AppRepository.saveFavorite("", spot.spotID)
                notifyItemChanged(position)
            }
        }

        holder.itemView.setOnClickListener { onClick(spot) }
    }

    private fun buildDetail(spot: CampusSpot): String {
        val parts = mutableListOf<String>()
        if (spot.description.isNotBlank()) parts.add(spot.description)
        if (spot.seats > 0) parts.add("${spot.seats} seats")
        return parts.joinToString(" — ")
    }
}

// ── BuildingBottomSheet ────────────────────────────────────────────────────

class BuildingBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            building: String,
            spots: List<CampusSpot>,
            walkTimes: Map<String, String>,
            coords: Pair<Double, Double>?
        ): BuildingBottomSheet {
            val sheet = BuildingBottomSheet()
            val args = Bundle()
            args.putString("building", building)
            args.putIntArray("spotIDs", spots.map { it.spotID }.toIntArray())
            val wt = walkTimes.entries.joinToString("|") { "${it.key}:${it.value}" }
            args.putString("walkTimes", wt)
            if (coords != null) {
                args.putDouble("lat", coords.first)
                args.putDouble("lng", coords.second)
            }
            sheet.arguments = args
            return sheet
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.sheet_building, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val building     = arguments?.getString("building") ?: return
        val spotIDs      = arguments?.getIntArray("spotIDs") ?: return
        val walkTimesStr = arguments?.getString("walkTimes") ?: ""
        val lat = arguments?.getDouble("lat")
        val lng = arguments?.getDouble("lng")

        view.findViewById<TextView>(R.id.tvBuildingName).text =
            AppRepository.buildingFullNames[building] ?: building

        val tvWalk = view.findViewById<TextView>(R.id.tvWalkTimes)
        if (walkTimesStr.isNotEmpty()) {
            tvWalk.text = walkTimesStr.split("|").joinToString("   ") { entry ->
                val parts = entry.split(":")
                "${parts[0]}: ${parts[1]}"
            }
        }

        view.findViewById<View>(R.id.btnNavigate)?.setOnClickListener {
            if (lat != null && lng != null) {
                val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }

        val spots = spotIDs.toList().mapNotNull { AppRepository.getSpotDetails(it) }
        val rv = view.findViewById<RecyclerView>(R.id.rvBuildingSpots)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SpotAdapter(spots) { spot ->
            dismiss()
            SpotDetailBottomSheet.newInstance(spot.spotID)
                .show(parentFragmentManager, "spot_detail")
        }
    }
}

// ── SpotDetailBottomSheet ──────────────────────────────────────────────────

class SpotDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(spotID: Int): SpotDetailBottomSheet {
            val sheet = SpotDetailBottomSheet()
            val args = Bundle(); args.putInt("spotID", spotID)
            sheet.arguments = args
            return sheet
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.sheet_spot_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spotID = arguments?.getInt("spotID") ?: return
        val spot   = AppRepository.getSpotDetails(spotID) ?: return

        view.findViewById<TextView>(R.id.tvDetailName).text     = spot.spotName
        view.findViewById<TextView>(R.id.tvDetailType).text     = spot.getSpotType()
        view.findViewById<TextView>(R.id.tvDetailLocation).text = spot.getAddress()
        view.findViewById<TextView>(R.id.tvDetailDesc).text     = spot.description
        view.findViewById<TextView>(R.id.tvDetailAvail).text    =
            if (spot.checkAvailability()) "Available" else "Unavailable"
        if (spot.seats > 0) {
            view.findViewById<TextView>(R.id.tvDetailSeats).text = "${spot.seats} seats"
        }

        val btnFav = view.findViewById<TextView>(R.id.btnFavDetail)
        fun updateFav() {
            btnFav.text = if (AppRepository.isFavorite(spotID)) "Remove from Favorites"
            else "Save to Favorites"
        }
        updateFav()
        btnFav.setOnClickListener {
            if (AppRepository.isFavorite(spotID)) AppRepository.removeFavorite(spotID)
            else AppRepository.saveFavorite("", spotID)
            updateFav()
        }

        view.findViewById<View>(R.id.btnNavigateSpot)?.setOnClickListener {
            val coords = AppRepository.buildingCoords[spot.buildingName]
            val lat = spot.latitude ?: coords?.first ?: 32.7306
            val lng = spot.longitude ?: coords?.second ?: -97.1145
            val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}