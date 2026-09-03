package com.team4.urwayuta.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.team4.urwayuta.R
import com.team4.urwayuta.data.model.MembershipStatus
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.FragmentAccountBinding
import com.team4.urwayuta.databinding.FragmentChangePasswordBinding
import com.team4.urwayuta.ui.auth.CurrentSession
import com.team4.urwayuta.ui.auth.LoginActivity
import com.team4.urwayuta.ui.clubs.label
import androidx.navigation.fragment.findNavController

// ── AccountFragment ────────────────────────────────────────────────────────

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadAccountData()

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.changePasswordFragment)
        }

        binding.btnSignOut.setOnClickListener {
            CurrentSession.studentID = ""
            CurrentSession.studentName = ""
            CurrentSession.password = ""
            CurrentSession.role = ""
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAccountData()
    }

    private fun loadAccountData() {
        // Profile
        binding.tvInitials.text = CurrentSession.studentName.take(2).uppercase()
        binding.tvFullName.text = CurrentSession.studentName
        binding.tvStudentId.text = "ID: ${CurrentSession.studentID}"
        binding.tvRole.text = if (CurrentSession.role == "faculty") "Faculty" else "Student"

        // Stats
        val allClubs = AppRepository.getAllClubs()
        val active  = allClubs.count { AppRepository.getMembershipStatus(it.clubID) == MembershipStatus.ACTIVE }
        val pending = allClubs.count { AppRepository.getMembershipStatus(it.clubID) == MembershipStatus.PENDING }
        val saved   = AppRepository.savedClubIDs.size

        binding.tvActiveCount.text  = active.toString()
        binding.tvPendingCount.text = pending.toString()
        binding.tvSavedCount.text   = saved.toString()

        // Memberships text
        val memberClubs = allClubs.filter {
            AppRepository.getMembershipStatus(it.clubID) != MembershipStatus.NOT_A_MEMBER
        }
        binding.tvMemberships.text = if (memberClubs.isEmpty()) {
            "No memberships yet"
        } else {
            memberClubs.joinToString("\n") { "• ${it.clubName} — ${AppRepository.getMembershipStatus(it.clubID).label()}" }
        }

        // Saved clubs text
        val savedClubs = allClubs.filter { AppRepository.savedClubIDs.contains(it.clubID) }
        binding.tvSavedClubs.text = if (savedClubs.isEmpty()) {
            "No saved clubs yet"
        } else {
            savedClubs.joinToString("\n") { "• ${it.clubName}" }
        }
    }
}

// ── ChangePasswordFragment ─────────────────────────────────────────────────

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }

        binding.btnUpdatePassword.setOnClickListener {
            val cur = binding.etCurrentPassword.text.toString()
            val nw  = binding.etNewPassword.text.toString()
            val cf  = binding.etConfirmPassword.text.toString()

            binding.tvPwError.visibility = View.GONE

            when {
                cur.isEmpty() || nw.isEmpty() || cf.isEmpty() ->
                    showErr("All fields are required.")
                nw.length < 6 ->
                    showErr("New password must be at least 6 characters.")
                nw != cf ->
                    showErr("Passwords do not match.")
                cur != CurrentSession.password ->
                    showErr("Current password is incorrect.")
                else -> {
                    CurrentSession.password = nw
                    val stu = AppRepository.students.find { it.studentID == CurrentSession.studentID }
                    stu?.let {
                        AppRepository.students[AppRepository.students.indexOf(it)] = it.copy(passwordHash = nw)
                    }
                    binding.etCurrentPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmPassword.text?.clear()
                    binding.tvPwError.visibility = View.VISIBLE
                    binding.tvPwError.text = "Password changed successfully!"
                    binding.tvPwError.setTextColor(requireContext().getColor(R.color.colorSuccess))
                    binding.tvPwError.setBackgroundResource(R.drawable.bg_success)
                }
            }
        }
    }

    private fun showErr(msg: String) {
        binding.tvPwError.visibility = View.VISIBLE
        binding.tvPwError.text = msg
        binding.tvPwError.setTextColor(requireContext().getColor(R.color.colorError))
        binding.tvPwError.setBackgroundResource(R.drawable.bg_error)
    }
}