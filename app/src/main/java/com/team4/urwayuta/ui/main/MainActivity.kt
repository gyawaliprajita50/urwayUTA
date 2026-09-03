package com.team4.urwayuta.ui.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.team4.urwayuta.R
import com.team4.urwayuta.databinding.ActivityMainBinding
import com.team4.urwayuta.ui.auth.CurrentSession
import com.team4.urwayuta.ui.auth.LoginActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // Drawer setup
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.nav_open, R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        // White background for drawer
        binding.navView.setBackgroundColor(Color.WHITE)

        // Remove global tint so we can set per-item colors
        binding.navView.itemIconTintList = null
        binding.navView.itemTextColor = ColorStateList.valueOf(Color.parseColor("#1A1A1A"))

        // Set Sign Out item to red, others to black
        val menu = binding.navView.menu
        menu.findItem(R.id.nav_change_password)?.iconTintList =
            ColorStateList.valueOf(Color.parseColor("#1A1A1A"))
        menu.findItem(R.id.nav_memberships)?.iconTintList =
            ColorStateList.valueOf(Color.parseColor("#1A1A1A"))
        menu.findItem(R.id.nav_sign_out)?.iconTintList =
            ColorStateList.valueOf(Color.parseColor("#C62828"))

        // Make Sign Out title red using a SpannableString
        val signOutItem = menu.findItem(R.id.nav_sign_out)
        val spannable = android.text.SpannableString(signOutItem.title)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(Color.parseColor("#C62828")),
            0, spannable.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        signOutItem.title = spannable

        // Set drawer header with user info
        val header = binding.navView.getHeaderView(0)
        header.findViewById<android.widget.TextView>(R.id.tvDrawerName)?.text =
            CurrentSession.studentName
        header.findViewById<android.widget.TextView>(R.id.tvDrawerID)?.text =
            CurrentSession.studentID
        header.findViewById<android.widget.TextView>(R.id.tvDrawerInitials)?.text =
            CurrentSession.studentName.take(2).uppercase()
    }

    // Called by android:onClick="openDrawer" in fragment toolbars
    fun openDrawer(view: android.view.View) {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_change_password -> {
                navController.navigate(R.id.changePasswordFragment)
            }
            R.id.nav_memberships -> {
                binding.bottomNav.selectedItemId = R.id.accountFragment
            }
            R.id.nav_sign_out -> {
                CurrentSession.clear()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}