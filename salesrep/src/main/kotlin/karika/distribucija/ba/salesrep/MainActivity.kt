package karika.distribucija.ba.salesrep

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        val navView: NavigationView = findViewById(R.id.nav_view)

        setSupportActionBar(toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val appBarConfig = AppBarConfiguration(setOf(R.id.ordersListFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)

        if ((application as SalesRepApp).sessionManager.hasToken()) {
            navController.navigate(
                R.id.ordersListFragment,
                null,
                androidx.navigation.navOptions {
                    popUpTo(R.id.loginFragment) { inclusive = true }
                }
            )
            loadRepName(navView)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isLogin = destination.id == R.id.loginFragment
            (findViewById<View>(R.id.toolbar)).visibility = if (isLogin) View.GONE else View.VISIBLE
            drawerLayout.setDrawerLockMode(
                if (isLogin) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
            )
            if (destination.id == R.id.ordersListFragment) {
                navView.setCheckedItem(R.id.nav_orders)
                loadRepName(navView)
            }
        }

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_orders -> {
                    if (navController.currentDestination?.id != R.id.ordersListFragment) {
                        navController.popBackStack(R.id.ordersListFragment, false)
                    }
                    true
                }
                R.id.nav_logout -> {
                    (application as SalesRepApp).sessionManager.logout()
                    navController.navigate(
                        R.id.loginFragment,
                        null,
                        androidx.navigation.navOptions {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    )
                    true
                }
                else -> {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private var repNameLoaded = false

    private fun loadRepName(navView: NavigationView) {
        if (repNameLoaded) return
        repNameLoaded = true
        lifecycleScope.launch {
            SalesRepository().getMe().collect { result ->
                if (result is ResultState.Success) {
                    navView.getHeaderView(0)
                        ?.findViewById<TextView>(R.id.text_rep_name)
                        ?.text = result.data.name ?: getString(R.string.drawer_role_label)
                }
            }
        }
    }
}
