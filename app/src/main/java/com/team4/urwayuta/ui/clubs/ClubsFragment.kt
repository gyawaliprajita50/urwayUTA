package com.team4.urwayuta.ui.clubs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.team4.urwayuta.R
import com.team4.urwayuta.data.model.Club
import com.team4.urwayuta.data.model.MembershipStatus
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.FragmentClubsBinding
import com.team4.urwayuta.databinding.FragmentClubDetailBinding
// ── ClubsFragment ──────────────────────────────────────────────────────────

class ClubsFragment : Fragment() {

    private lateinit var binding: FragmentClubsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentClubsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadClubs()
        binding.fabCreateClub.setOnClickListener {
            findNavController().navigate(R.id.createClubFragment)
        }
    }

    override fun onResume() { super.onResume(); loadClubs() }

    private fun loadClubs() {
        val adapter = ClubListAdapter(AppRepository.getAllClubs()) { club ->
            val args = Bundle(); args.putInt("clubID", club.clubID)
            findNavController().navigate(R.id.clubDetailFragment, args)
        }
        binding.rvClubs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClubs.adapter = adapter
    }
}

// ── ClubListAdapter ────────────────────────────────────────────────────────

class ClubListAdapter(
    private val clubs: List<Club>,
    private val onClick: (Club) -> Unit
) : RecyclerView.Adapter<ClubListAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvClubInitials)
        val tvName: TextView = view.findViewById(R.id.tvClubName)
        val tvCat: TextView = view.findViewById(R.id.tvClubCategory)
        val tvStatus: TextView = view.findViewById(R.id.tvMemberStatus)
        val btnSave: TextView = view.findViewById(R.id.btnSaveClub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_club, parent, false)
    )
    override fun getItemCount() = clubs.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val club = clubs[position]
        holder.tvInitials.text = club.logoInitials
        holder.tvInitials.setBackgroundColor(club.logoColor)
        holder.tvName.text = club.clubName
        holder.tvCat.text = club.category

        val status = AppRepository.getMembershipStatus(club.clubID)
        holder.tvStatus.text = status.label()
        holder.tvStatus.setBackgroundResource(status.tagBg())

        val saved = AppRepository.savedClubIDs.contains(club.clubID)
        holder.btnSave.text = if (saved) "🔖" else "🔖"
        holder.btnSave.alpha = if (saved) 1f else 0.3f
        holder.btnSave.setOnClickListener {
            AppRepository.toggleSavedClub(club.clubID)
            notifyItemChanged(position)
        }
        holder.itemView.setOnClickListener { onClick(club) }
    }
}

fun MembershipStatus.label() = when(this) {
    MembershipStatus.NOT_A_MEMBER -> "Not a Member"
    MembershipStatus.PENDING      -> "Pending"
    MembershipStatus.ACTIVE       -> "Active Member"
    MembershipStatus.WITHDRAWAL   -> "Withdrawal Pending"
    MembershipStatus.REJECTED     -> "Not Approved"
}
fun MembershipStatus.tagBg() = when(this) {
    MembershipStatus.ACTIVE   -> R.drawable.bg_tag_green
    MembershipStatus.PENDING  -> R.drawable.bg_tag_yellow
    MembershipStatus.REJECTED -> R.drawable.bg_tag_red
    else                      -> R.drawable.bg_tag_grey
}

// ── ClubDetailFragment ─────────────────────────────────────────────────────

class ClubDetailFragment : Fragment() {

    private lateinit var binding: FragmentClubDetailBinding
    private var clubID = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentClubDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        clubID = arguments?.getInt("clubID") ?: return
        val club = AppRepository.getClub(clubID) ?: return

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.tvClubName.text = club.clubName
        binding.tvClubCategory.text = club.category
        binding.tvClubDescription.text = club.description
        binding.tvClubInitials.text = club.logoInitials
        binding.tvClubInitials.setBackgroundColor(club.logoColor)
        binding.tvLocation.text = club.location
        binding.tvHowToJoin.text = club.howToJoin
        binding.tvEmail.text = club.contactEmail

        // Clickable email — opens mail app
        binding.tvEmail.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${club.contactEmail}")))
        }
        binding.cardEmail.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${club.contactEmail}")))
        }

        // Form link
        if (club.formUrl != null) {
            binding.cardForm.visibility = View.VISIBLE
            binding.tvFormLink.text = club.formLabel ?: club.formUrl
            binding.cardForm.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(club.formUrl)))
            }
        } else binding.cardForm.visibility = View.GONE

        // Discord link
        if (club.discordUrl != null) {
            binding.cardDiscord.visibility = View.VISIBLE
            binding.tvDiscordLink.text = club.discordLabel ?: club.discordUrl
            binding.cardDiscord.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(club.discordUrl)))
            }
        } else binding.cardDiscord.visibility = View.GONE

        // Save button
        fun updateSave() {
            val saved = AppRepository.savedClubIDs.contains(clubID)
            binding.btnSaveClub.text = if (saved) "Saved" else "Save Club"
            binding.btnSaveClub.alpha = if (saved) 1f else 0.6f
        }
        updateSave()
        binding.btnSaveClub.setOnClickListener { AppRepository.toggleSavedClub(clubID); updateSave() }

        updateMembershipUI(club)
    }

    private fun updateMembershipUI(club: Club) {
        val status = AppRepository.getMembershipStatus(clubID)
        binding.tvMemberStatus.text = status.label()
        binding.tvMemberStatus.setBackgroundResource(status.tagBg())

        // Hide all state views first
        binding.layoutNone.visibility = View.GONE
        binding.layoutPending.visibility = View.GONE
        binding.layoutActive.visibility = View.GONE
        binding.layoutWithdrawal.visibility = View.GONE
        binding.layoutRejected.visibility = View.GONE

        when (status) {
            MembershipStatus.NOT_A_MEMBER -> {
                binding.layoutNone.visibility = View.VISIBLE
                binding.btnApply.setOnClickListener {
                    AppRepository.setMembershipStatus(clubID, MembershipStatus.PENDING)
                    updateMembershipUI(club)
                    Snackbar.make(requireView(), "Application submitted!", Snackbar.LENGTH_SHORT).show()
                }
            }
            MembershipStatus.PENDING -> {
                binding.layoutPending.visibility = View.VISIBLE
                binding.tvPendingEmail.text = club.contactEmail
                binding.tvPendingEmail.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${club.contactEmail}")))
                }
                binding.btnWithdrawPending.setOnClickListener {
                    AppRepository.setMembershipStatus(clubID, MembershipStatus.NOT_A_MEMBER)
                    updateMembershipUI(club)
                }
            }
            MembershipStatus.ACTIVE -> {
                binding.layoutActive.visibility = View.VISIBLE
                binding.btnWithdrawActive.setOnClickListener {
                    AppRepository.setMembershipStatus(clubID, MembershipStatus.WITHDRAWAL)
                    updateMembershipUI(club)
                }
                binding.btnRsvp.setOnClickListener {
                    Snackbar.make(requireView(), "RSVP confirmed!", Snackbar.LENGTH_SHORT).show()
                    binding.btnRsvp.text = "RSVPd"
                    binding.btnRsvp.isEnabled = false
                }
            }
            MembershipStatus.WITHDRAWAL -> {
                binding.layoutWithdrawal.visibility = View.VISIBLE
            }
            MembershipStatus.REJECTED -> {
                binding.layoutRejected.visibility = View.VISIBLE
                binding.tvRejectedEmail.text = club.contactEmail
                binding.tvRejectedEmail.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${club.contactEmail}")))
                }
                binding.btnReapply.setOnClickListener {
                    AppRepository.setMembershipStatus(clubID, MembershipStatus.PENDING)
                    updateMembershipUI(club)
                }
            }
        }
    }
}
