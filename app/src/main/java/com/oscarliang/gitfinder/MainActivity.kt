package com.oscarliang.gitfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oscarliang.gitfinder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWindow()
        initNavController()
    }

    private fun initWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            // Prevent the app bar from overlapping the status bar
            binding.appbar.updatePadding(
                top = insets.top,
            )
            // Prevent the view from overlapping the navigation bar in landscape mode
            binding.root.updatePadding(
                right = insets.right,
            )
            windowInsets
        }
    }

    private fun initNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav?.setupWithNavController(navController)
        binding.navView?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showNav = destination.id != R.id.detailFragment
            binding.bottomNav?.isVisible = showNav
            binding.navView?.isVisible = showNav
        }
    }

}