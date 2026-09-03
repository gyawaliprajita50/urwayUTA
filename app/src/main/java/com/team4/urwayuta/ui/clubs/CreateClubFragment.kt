package com.team4.urwayuta.ui.clubs

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.team4.urwayuta.data.model.Club
import com.team4.urwayuta.data.model.MembershipStatus
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.FragmentCreateClubBinding

class CreateClubFragment : Fragment() {

    private lateinit var binding: FragmentCreateClubBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateClubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Make "uta.edu/student-affairs" part bold and blue
        val fullText = "Creating a student organization requires university approval. Visit uta.edu/student-affairs for details."
        val spannable = android.text.SpannableString(fullText)
        val linkStart = fullText.indexOf("uta.edu")
        val linkEnd = linkStart + "uta.edu/student-affairs".length
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#003594")),
            linkStart, linkEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            linkStart, linkEnd,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvUtaLink.text = spannable
        binding.btnBackCreate.setOnClickListener { findNavController().popBackStack() }
        // Make the warning notice link tappable
        binding.tvUtaLink.setOnClickListener {
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.uta.edu/student-affairs")))
        }
        binding.btnBackToClubs.setOnClickListener { findNavController().popBackStack() }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateSubmitButton() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        }
        binding.etClubName.addTextChangedListener(watcher)
        binding.etClubDesc.addTextChangedListener(watcher)
        binding.etClubCat.addTextChangedListener(watcher)

        binding.btnCreateClub.setOnClickListener { submitClub() }
    }

    private fun updateSubmitButton() {
        val valid = binding.etClubName.text.isNotBlank() &&
                    binding.etClubDesc.text.isNotBlank() &&
                    binding.etClubCat.text.isNotBlank()
        binding.btnCreateClub.alpha = if (valid) 1f else 0.5f
    }

    private fun submitClub() {
        val name = binding.etClubName.text.toString().trim()
        val desc = binding.etClubDesc.text.toString().trim()
        val cat  = binding.etClubCat.text.toString().trim()

        if (name.isBlank() || desc.isBlank() || cat.isBlank()) {
            binding.tvCreateError.visibility = View.VISIBLE
            binding.tvCreateError.text = "Club name, description, and category are required."
            return
        }

        val duplicate = AppRepository.getAllClubs().any {
            it.clubName.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            binding.tvCreateError.visibility = View.VISIBLE
            binding.tvCreateError.text = "A club with this name already exists."
            return
        }

        val newId = System.currentTimeMillis().toInt()
        val newClub = Club(
            clubID       = newId,
            clubName     = name,
            category     = cat,
            description  = desc,
            location     = binding.etClubLoc.text.toString().trim().ifBlank { "TBD" },
            contactEmail = binding.etClubEmail.text.toString().trim().ifBlank { "N/A" },
            howToJoin    = "Contact the club organizer via email.",
            logoInitials = name.take(2).uppercase(),
            logoColor    = Color.parseColor("#546E7A"),
            isUserCreated = true
        )

        AppRepository.clubs.add(newClub)
        AppRepository.memberships[newId] = MembershipStatus.NOT_A_MEMBER

        binding.tvCreateError.visibility = View.GONE
        binding.scrollCreateForm.visibility = View.GONE
        binding.layoutCreateSuccess.visibility = View.VISIBLE
    }
}
