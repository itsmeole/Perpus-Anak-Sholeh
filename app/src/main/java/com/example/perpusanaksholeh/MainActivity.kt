package com.example.perpusanaksholeh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.perpusanaksholeh.databinding.ActivityMainBinding
import com.example.perpusanaksholeh.util.NotificationHelper
import com.example.perpusanaksholeh.worker.LateReturnWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Memaksa tema terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan status bar (Notification bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Jangan beri padding di bagian atas agar layout memenuhi seluruh layar ke atas
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupNotifications()
        scheduleLateReturnCheck()
        requestNotificationPermission()

        val prefs = getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "nama") {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navHostFragment?.navController?.currentDestination?.let { dest ->
                updateTitleForDestination(dest.id)
            }
        }
    }

    private fun updateTitleForDestination(destinationId: Int) {
        val showGreeting = destinationId in listOf(
            R.id.dashboardFragment,
            R.id.bukuListFragment,
            R.id.siswaListFragment,
            R.id.peminjamanListFragment,
            R.id.profileFragment
        )
        if (showGreeting) {
            val prefs = getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
            val userName = prefs.getString("nama", "Admin") ?: "Admin"
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val greeting = when (hour) {
                in 0..11 -> "Selamat pagi"
                in 12..14 -> "Selamat siang"
                in 15..17 -> "Selamat sore"
                else -> "Selamat malam"
            }
            binding.toolbar.title = "$greeting, $userName!"
        } else {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            binding.toolbar.title = navHostFragment?.navController?.currentDestination?.label ?: getString(R.string.app_name)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Update toolbar title on navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateTitleForDestination(destination.id)

            // Show/hide bottom nav for certain screens
            val showBottomNav = destination.id in listOf(
                R.id.dashboardFragment,
                R.id.bukuListFragment,
                R.id.siswaListFragment,
                R.id.peminjamanListFragment,
                R.id.profileFragment
            )
            binding.bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
        }

        // Handle deep link from notification
        intent?.getStringExtra("navigate_to")?.let { target ->
            if (target == "peminjaman") {
                navController.navigate(R.id.peminjamanListFragment)
            }
        }
    }

    private fun setupNotifications() {
        NotificationHelper.createNotificationChannel(this)
    }

    private fun scheduleLateReturnCheck() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val lateReturnWork = PeriodicWorkRequestBuilder<LateReturnWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "late_return_check",
            ExistingPeriodicWorkPolicy.KEEP,
            lateReturnWork
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}