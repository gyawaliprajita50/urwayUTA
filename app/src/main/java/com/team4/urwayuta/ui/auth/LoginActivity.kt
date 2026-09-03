package com.team4.urwayuta.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.team4.urwayuta.data.repository.AppRepository
import com.team4.urwayuta.databinding.ActivityLoginBinding
import com.team4.urwayuta.ui.main.MainActivity
import com.team4.urwayuta.R

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var attemptCount = 0
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill demo credentials
        binding.etId.setText("cse3310")
        binding.etPassword.setText("team4")

        binding.btnSignIn.setOnClickListener { tryLogin() }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnResetLock.setOnClickListener {
            attemptCount = 0
            binding.layoutLogin.visibility = View.VISIBLE
            binding.layoutLocked.visibility = View.GONE
            binding.tvError.visibility = View.GONE
        }

        // Password show/hide toggle
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etPassword.inputType = if (passwordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
            )
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    override fun onResume() {
        super.onResume()
        intent.getStringExtra("prefill_email")?.let { email ->
            binding.etId.setText(email)
            binding.etPassword.setText("")
            binding.etPassword.requestFocus()
        }
    }

    private fun tryLogin() {
        val id = binding.etId.text.toString().trim()
        val pw = binding.etPassword.text.toString()

        val student = AppRepository.findStudent(id, pw)
        if (student != null) {
            AppRepository.students.indexOfFirst { it.studentID == student.studentID }.let { idx ->
                if (idx >= 0) AppRepository.students[idx] = student
            }
            CurrentSession.studentID    = student.studentID
            CurrentSession.studentName  = student.fullName
            CurrentSession.studentEmail = student.email
            CurrentSession.role         = student.role
            CurrentSession.password     = pw

            attemptCount = 0
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            attemptCount++
            if (attemptCount >= 3) {
                binding.layoutLogin.visibility = View.GONE
                binding.layoutLocked.visibility = View.VISIBLE
            } else {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "Incorrect UTA ID or password. Attempt $attemptCount of 3."
            }
        }
    }
}

/** Simple in-memory session state */
object CurrentSession {
    var studentID: String = ""
    var studentName: String = ""
    var studentEmail: String = ""
    var role: String = "student"
    var password: String = ""

    fun isLoggedIn() = studentID.isNotEmpty()

    fun clear() {
        studentID = ""; studentName = ""; studentEmail = ""
        role = "student"; password = ""
    }
}