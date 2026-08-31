package karika.distribucija.ba.salesrep

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.network.PlatformEnv
import karika.distribucija.ba.salesrep.notifications.NotificationDestination
import karika.distribucija.ba.salesrep.notifications.PushRouteResolver
import karika.distribucija.ba.salesrep.notifications.PushTokenRegistrar
import karika.distribucija.ba.salesrep.session.CurrentUser
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    private data class NavRow(val container: View, val icon: ImageView, val text: TextView, val destinationId: Int)
    private lateinit var navRows: List<NavRow>

    private var notificationsMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        applyEdgeToEdgeInsets()

        navRows = listOf(
            NavRow(findViewById(R.id.row_nav_orders), findViewById(R.id.icon_nav_orders), findViewById(R.id.text_nav_orders), R.id.ordersListFragment),
            NavRow(findViewById(R.id.row_nav_customers), findViewById(R.id.icon_nav_customers), findViewById(R.id.text_nav_customers), R.id.customersListFragment),
            NavRow(findViewById(R.id.row_nav_customer_messages), findViewById(R.id.icon_nav_customer_messages), findViewById(R.id.text_nav_customer_messages), R.id.customerMessagesFragment),
            NavRow(findViewById(R.id.row_nav_admin_messages), findViewById(R.id.icon_nav_admin_messages), findViewById(R.id.text_nav_admin_messages), R.id.adminMessagesFragment),
            NavRow(findViewById(R.id.row_nav_internal_messages), findViewById(R.id.icon_nav_internal_messages), findViewById(R.id.text_nav_internal_messages), R.id.internalMessagesFragment)
        )

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        appBarConfig = AppBarConfiguration(
            setOf(R.id.ordersListFragment, R.id.customersListFragment, R.id.customerMessagesFragment, R.id.adminMessagesFragment, R.id.internalMessagesFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfig)

        findViewById<View>(R.id.button_close_drawer).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.row_nav_orders).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "orders")
            navigateToRoot(R.id.ordersListFragment)
        }
        findViewById<View>(R.id.row_nav_customers).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "customers")
            navigateToRoot(R.id.customersListFragment)
        }
        findViewById<View>(R.id.row_nav_customer_messages).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "customer_messages")
            navigateToRoot(R.id.customerMessagesFragment)
        }
        findViewById<View>(R.id.row_nav_admin_messages).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "admin_messages")
            navigateToRoot(R.id.adminMessagesFragment)
        }
        findViewById<View>(R.id.row_nav_internal_messages).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "internal_messages")
            navigateToRoot(R.id.internalMessagesFragment)
        }
        findViewById<View>(R.id.row_logout).setOnClickListener {
            AnalyticsTracker.trackClick("drawer", "logout")
            drawerLayout.closeDrawer(GravityCompat.START)
            PushTokenRegistrar.unregister()
            (application as SalesRepApp).sessionManager.logout()
            navController.navigate(
                R.id.loginFragment,
                null,
                navOptions { popUpTo(navController.graph.id) { inclusive = true } }
            )
        }
        findViewById<TextView>(R.id.text_app_version).text = PlatformEnv.appVersionName()

        if ((application as SalesRepApp).sessionManager.hasToken()) {
            navController.navigate(
                R.id.ordersListFragment,
                null,
                navOptions { popUpTo(R.id.loginFragment) { inclusive = true } }
            )
            loadRepName()
            handlePushRoute(intent)
        }

        // Kiosk runs this app as the locked payload: back must never pop the whole task away and
        // reveal the launcher underneath, so swallow it instead of falling through to the default
        // finish-the-activity behavior once there's nothing left in the nav back stack.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    navController.popBackStack()
                }
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            AnalyticsTracker.trackScreen(resources.getResourceEntryName(destination.id))
            val isLogin = destination.id == R.id.loginFragment
            toolbar.visibility = if (isLogin) View.GONE else View.VISIBLE
            drawerLayout.setDrawerLockMode(
                if (isLogin) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
            )
            updateSelectedNavRow(destination)
            notificationsMenuItem?.isVisible = destination.id in appBarConfig.topLevelDestinations
            if (destination.id == R.id.ordersListFragment) {
                loadRepName()
            }
        }
    }

    // launchMode="singleTop" means a notification tap while the activity is already running
    // (foreground or background) delivers here instead of a fresh onCreate().
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushRoute(intent)
    }

    /** Resolves a tapped push notification's "route" extra (set by KarikaFcmService) into the
     * same destinations the in-app Notifications list uses, via the notificationsFragment's own
     * nav actions - then pops notificationsFragment itself off so back-navigation lands on
     * Orders instead of an empty Notifications screen. */
    private fun handlePushRoute(intent: Intent?) {
        val route = intent?.getStringExtra("route") ?: return
        val destination = PushRouteResolver.resolve(route) ?: return
        navController.navigate(R.id.notificationsFragment)
        when (destination) {
            is NotificationDestination.OrderDetail -> navController.navigate(
                R.id.action_notifications_to_order_detail,
                bundleOf(
                    "orderId" to 0L,
                    "incrementId" to destination.orderId,
                    "customerId" to 0L,
                    "customerName" to null,
                    "grandTotal" to 0f,
                    "status" to "",
                    "createdAt" to null
                ),
                navOptions { popUpTo(R.id.notificationsFragment) { inclusive = true } }
            )

            is NotificationDestination.Conversation -> navController.navigate(
                if (destination.admin) R.id.action_notifications_to_admin_conversation
                else R.id.action_notifications_to_customer_conversation,
                bundleOf(
                    "threadId" to destination.threadId,
                    "customerName" to destination.customerName,
                    "subject" to destination.subject,
                    "receiverId" to destination.receiverId
                ),
                navOptions { popUpTo(R.id.notificationsFragment) { inclusive = true } }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        notificationsMenuItem = menu.findItem(R.id.action_notifications)
        notificationsMenuItem?.isVisible = navController.currentDestination?.id in appBarConfig.topLevelDestinations
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notifications) {
            AnalyticsTracker.trackClick("toolbar", "notifications")
            navController.navigate(R.id.notificationsFragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToRoot(destinationId: Int) {
        drawerLayout.closeDrawer(GravityCompat.START)
        if (navController.currentDestination?.id == destinationId) return
        if (!navController.popBackStack(destinationId, false)) {
            navController.navigate(
                destinationId,
                null,
                navOptions {
                    popUpTo(R.id.ordersListFragment) { inclusive = false }
                    launchSingleTop = true
                }
            )
        }
    }

    /**
     * With decorFitsSystemWindows(false) the whole window draws edge-to-edge (system bars are
     * transparent overlays), so each piece of shared chrome must reserve its own space for the
     * status/navigation bars. Fragment content (including the login screen's background, which
     * is meant to bleed under the status bar) is left alone - only the toolbar, the fragment
     * container's bottom edge (so sticky footer buttons clear the gesture bar), and the drawer
     * get padding.
     */
    private fun applyEdgeToEdgeInsets() {
        val toolbarInitialTop = toolbar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = toolbarInitialTop + statusBars.top)
            insets
        }

        val fragmentContainer = findViewById<View>(R.id.nav_host_fragment)
        val containerInitialBottom = fragmentContainer.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = containerInitialBottom + systemBars.bottom)
            insets
        }

        val drawerContent = findViewById<View>(R.id.drawer_content_root)
        val drawerInitialTop = drawerContent.paddingTop
        val drawerInitialBottom = drawerContent.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(drawerContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = drawerInitialTop + systemBars.top,
                bottom = drawerInitialBottom + systemBars.bottom
            )
            insets
        }
    }

    private fun updateSelectedNavRow(destination: NavDestination) {
        navRows.forEach { row ->
            val selected = destination.id == row.destinationId
            row.container.setBackgroundResource(
                if (selected) R.drawable.bg_drawer_item_selected else R.drawable.bg_drawer_item_unselected
            )
            val color = getColor(if (selected) R.color.karika_white else R.color.karika_gray6)
            row.icon.setColorFilter(color)
            row.text.setTextColor(color)
            row.text.setTypeface(row.text.typeface, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    private var repNameLoaded = false

    private fun loadRepName() {
        if (repNameLoaded) return
        repNameLoaded = true
        lifecycleScope.launch {
            SalesRepository().getMe().collect { result ->
                if (result is ResultState.Success) {
                    CurrentUser.me = result.data
                    AppLogger.setUser(result.data.employeeId?.toString())
                    AnalyticsTracker.setUser(result.data.employeeId?.toString())
                    findViewById<TextView>(R.id.text_rep_name).text =
                        result.data.name ?: getString(R.string.drawer_role_label)
                }
            }
        }
    }
}
