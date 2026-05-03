package com.example.bazaartrackermobile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bazaartrackermobile.data.local.AuthTokenManager
import com.example.bazaartrackermobile.data.remote.ApiService
import com.example.bazaartrackermobile.data.remote.RetrofitClient
import com.example.bazaartrackermobile.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_sales,
                R.id.navigation_products,
                R.id.navigation_vendors,
                R.id.navigation_expenses
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.bottomNav.setupWithNavController(navController)

        setupDrawerHeader()
        setupLogout()
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_dashboard || 
                destination.id == R.id.navigation_sales ||
                destination.id == R.id.navigation_products ||
                destination.id == R.id.navigation_vendors ||
                destination.id == R.id.navigation_expenses) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }
    }

    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvCompanyName = headerView.findViewById<TextView>(R.id.tvCompanyName)

        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        lifecycleScope.launch {
            try {
                val response = apiService.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    tvUserName.text = profile.name
                    tvCompanyName.text = profile.companyName
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun setupLogout() {
        binding.navView.menu.findItem(R.id.navigation_logout).setOnMenuItemClickListener {
            logout()
            true
        }
    }

    private fun logout() {
        AuthTokenManager(this).clearToken()
        RetrofitClient.resetClient()
        // Assuming a LoginActivity exists, relaunch it.
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
