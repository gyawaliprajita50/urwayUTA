package com.team4.urwayuta.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.team4.urwayuta.data.model.Student
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedRole = "student"
    private var generatedEmail = ""
    private var generatedID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Role toggle
        binding.btnStudent.setOnClickListener { setRole("student") }
        binding.btnFaculty.setOnClickListener { setRole("faculty") }

        // Auto-generate email as user types
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateEmailPreview() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.etFirstName.addTextChangedListener(watcher)
        binding.etMiddleName.addTextChangedListener(watcher)
        binding.etLastName.addTextChangedListener(watcher)
        binding.etLastFourId.addTextChangedListener(watcher)

        binding.btnCreateAccount.setOnClickListener { submitRegistration() }
        binding.btnSignInNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("prefill_email", generatedEmail)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun setRole(role: String) {
        selectedRole = role
        if (role == "student") {
            binding.btnStudent.isSelected = true
            binding.btnFaculty.isSelected = false
            binding.layoutStudentFields.visibility = View.VISIBLE
            binding.tvFacultyNote.visibility = View.GONE
        } else {
            binding.btnStudent.isSelected = false
            binding.btnFaculty.isSelected = true
            binding.layoutStudentFields.visibility = View.GONE
            binding.tvFacultyNote.visibility = View.VISIBLE
        }
    }

    private fun updateEmailPreview() {
        val fn = binding.etFirstName.text.toString().trim()
        val mn = binding.etMiddleName.text.toString().trim()
        val ln = binding.etLastName.text.toString().trim()
        val id = binding.etLastFourId.text.toString().trim()
        if (fn.isNotEmpty() && ln.isNotEmpty()) {
            generatedEmail = AppRepository.generateEmail(fn, mn, ln, id)
            binding.tvEmailPreview.text = generatedEmail
        }
    }

    private fun submitRegistration() {
        val fn = binding.etFirstName.text.toString().trim()
        val mn = binding.etMiddleName.text.toString().trim()
        val ln = binding.etLastName.text.toString().trim()
        val idStr = binding.etLastFourId.text.toString().trim()
        val major = binding.etMajor.text.toString().trim()
        val pw = binding.etPassword.text.toString().trim()

        fun showErr(msg: String) {
            binding.tvRegError.text = msg
            binding.tvRegError.visibility = View.VISIBLE
        }

        if (fn.isEmpty() || ln.isEmpty()) { showErr("First and last name are required."); return }
        if (selectedRole == "faculty") { showErr("Faculty registration coming soon."); return }
        if (!idStr.matches(Regex("\\d{4}"))) { showErr("Enter exactly the last 4 digits of your Student ID."); return }
        if (pw.length < 6) { showErr("Password must be at least 6 characters."); return }
        if (major.isEmpty()) { showErr("Please enter your major."); return }

        generatedEmail = AppRepository.generateEmail(fn, mn, ln, idStr)
        generatedID = fn.take(3).lowercase() + idStr

        val student = Student(
            studentID = generatedID,
            firstName = fn,
            middleName = mn,
            lastName = ln,
            email = generatedEmail,
            major = major,
            role = "student",
            passwordHash = pw // plain-text for prototype
        )

        val success = AppRepository.registerStudent(student)
        if (!success) { showErr("An account with this name and ID already exists."); return }

        // Show success
        binding.tvRegError.visibility = View.GONE
        binding.scrollForm.visibility = View.GONE
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.tvSuccessEmail.text = "Email: $generatedEmail"
    }
}
