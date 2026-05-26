// app/src/main/java/com/swiftpay/MainActivity.java
package com.swiftpay;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.util.AuditLogger;
import com.swiftpay.util.ThemeManager;
import java.util.concurrent.Executors;

/**
 * Main host Activity for SwiftPay navigation, role-aware drawer access,
 * session timeout, font scale (UX-F1), accessibility mode (UX-F2),
 * color scheme overlay (UX-B7) and foreground alarm dialog (UX-D6).
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SessionManager sessionManager;

    /**
     * Applies font scale and accessibility configuration BEFORE the theme and
     * layout are inflated.  This is the correct lifecycle hook for
     * {@link Configuration#fontScale} changes (UX-F1).
     * To prevent crashes, we read this from a simple, safe SharedPreferences file.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        String fontSize = "NORMAL";
        try {
            android.content.SharedPreferences prefs = newBase.getSharedPreferences("swiftpay_ux_prefs", Context.MODE_PRIVATE);
            fontSize = prefs.getString("font_size", "NORMAL");
        } catch (Exception ignored) {
            // First launch
        }
        Context ctx = ThemeManager.applyFontScale(newBase, fontSize);
        super.attachBaseContext(ctx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String colorScheme = "DEFAULT";
        int accessibilityMode = 0;
        String wallpaperUri = null;
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("swiftpay_ux_prefs", Context.MODE_PRIVATE);
            colorScheme = prefs.getString("color_scheme", "DEFAULT");
            accessibilityMode = prefs.getInt("accessibility_mode", 0);
            wallpaperUri = prefs.getString("wallpaper_path", null);
        } catch (Exception ignored) {}

        // UX-B7: apply color scheme overlay BEFORE setContentView.
        ThemeManager.applyColorScheme(this, colorScheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // UX-E3 + UX-F2: apply wallpaper + accessibility sizing early.
        ThemeManager.applyWallpaper(this, wallpaperUri);
        ThemeManager.applyAccessibilitySizing(this, accessibilityMode == 1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        if (navController != null) {
            androidx.navigation.ui.AppBarConfiguration appBarConfiguration =
                    new androidx.navigation.ui.AppBarConfiguration.Builder(
                            R.id.loginFragment,
                            R.id.forcePasswordChangeFragment,
                            R.id.nav_dashboard,
                            R.id.productListFragment,
                            R.id.brandListFragment,
                            R.id.clientListFragment,
                            R.id.discountListFragment,
                            R.id.saleListFragment,
                            R.id.nav_suppliers,
                            R.id.nav_purchase_orders,
                            R.id.settingsFragment,
                            R.id.profileFragment,
                            R.id.userManagementFragment)
                            .setOpenableLayout(drawerLayout)
                            .build();

            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                if (destId == R.id.loginFragment || destId == R.id.forcePasswordChangeFragment) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }
                    setDrawerLocked(true);
                } else {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }
                    setDrawerLocked(false);
                }
            });
        }

        ((SwiftPayApplication) getApplication()).getDatabase().userDao();
        if (sessionManager.isLoggedIn()) {
            setupMenuForRole(sessionManager.getRole());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Session timeout check — navigates with popUpTo(nav_graph, inclusive=true).
        if (sessionManager.isLoggedIn() && !sessionManager.checkSessionValidity()) {
            sessionManager.clearSession();
            navigateClearingBackStack(R.id.loginFragment);
            setDrawerLocked(true);
            return;
        }

        // Force password change guard — blocks navigation and drawer.
        if (sessionManager.isLoggedIn()
                && sessionManager.isTemporaryPassword()
                && navController != null
                && navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.forcePasswordChangeFragment) {
            navigateClearingBackStack(R.id.forcePasswordChangeFragment);
            setDrawerLocked(true);
        }

        // UX-D6: If AlarmReceiver flagged a foreground alert, show it as a dialog.
        if (AlarmReceiver.consumePendingAlert()) {
            showForegroundAlertDialog(
                    AlarmReceiver.getPendingAlertTitle(),
                    AlarmReceiver.getPendingAlertMessage());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Configures the drawer by role using menu item visibility to avoid separator lines.
     *
     * @param role authenticated SwiftPay role
     */
    public void setupMenuForRole(String role) {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.menu_admin);

        android.view.Menu menu = navigationView.getMenu();

        boolean showDashboard = "ADMINISTRADOR".equals(role);
        boolean showSales = "ADMINISTRADOR".equals(role) || "VENDEDOR".equals(role);
        boolean showClients = "ADMINISTRADOR".equals(role) || "VENDEDOR".equals(role);
        boolean showDiscounts = "ADMINISTRADOR".equals(role) || "VENDEDOR".equals(role);
        boolean showProducts = "ADMINISTRADOR".equals(role) || "VENDEDOR".equals(role) || "GESTOR_PRODUCTOS".equals(role);
        boolean showBrands = "ADMINISTRADOR".equals(role) || "VENDEDOR".equals(role) || "GESTOR_PRODUCTOS".equals(role);
        boolean showSuppliers = "ADMINISTRADOR".equals(role) || "GESTOR_PRODUCTOS".equals(role);
        boolean showPurchaseOrders = "ADMINISTRADOR".equals(role) || "GESTOR_PRODUCTOS".equals(role);
        boolean showUsers = "ADMINISTRADOR".equals(role);

        setItemVisible(menu, R.id.nav_dashboard, showDashboard);
        setItemVisible(menu, R.id.saleListFragment, showSales);
        setItemVisible(menu, R.id.clientListFragment, showClients);
        setItemVisible(menu, R.id.discountListFragment, showDiscounts);
        setItemVisible(menu, R.id.productListFragment, showProducts);
        setItemVisible(menu, R.id.brandListFragment, showBrands);
        setItemVisible(menu, R.id.nav_suppliers, showSuppliers);
        setItemVisible(menu, R.id.nav_purchase_orders, showPurchaseOrders);
        setItemVisible(menu, R.id.userManagementFragment, showUsers);

        setItemVisible(menu, R.id.nav_profile, role != null);
        setItemVisible(menu, R.id.nav_settings, role != null);
        setItemVisible(menu, R.id.nav_logout, role != null);

        updateNavHeader();
    }

    private void setItemVisible(android.view.Menu menu, int id, boolean visible) {
        MenuItem item = menu.findItem(id);
        if (item != null) {
            item.setVisible(visible);
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            return;
        }
        TextView tvName = headerView.findViewById(R.id.tv_nav_name);
        TextView tvRole = headerView.findViewById(R.id.tv_nav_role);
        android.widget.ImageView ivAvatar = headerView.findViewById(R.id.iv_nav_avatar);
        
        if (tvName != null) {
            tvName.setText(sessionManager.getFullName());
        }
        if (tvRole != null) {
            tvRole.setText(sessionManager.getRole());
        }
        
        if (ivAvatar != null && sessionManager.isLoggedIn()) {
            SwiftPayDatabase db = ((SwiftPayApplication) getApplication()).getDatabase();
            db.userDao().getById(sessionManager.getUserId()).observe(this, user -> {
                if (user != null && user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                    com.swiftpay.util.ImageLoader.loadLocalProfileImage(this, user.getProfileImagePath(), ivAvatar, true);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_account_circle);
                }
            });
        }
    }

    /** Locks or unlocks the drawer. */
    public void setDrawerLocked(boolean locked) {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(
                    locked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (sessionManager.isTemporaryPassword() && item.getItemId() != R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.nav_logout) {
            AuditLogger.log(this, sessionManager.getUserId(), "LOGOUT", "USER",
                    sessionManager.getUserId(), "Cierre de sesion manual");
            sessionManager.clearSession();
            navigateClearingBackStack(R.id.loginFragment);
            setDrawerLocked(true);
        } else if (itemId == R.id.nav_settings) {
            navigate(itemId, R.id.action_global_nav_settings);
        } else if (itemId == R.id.nav_profile) {
            navigate(itemId, R.id.profileFragment);
        } else if (itemId == R.id.userManagementFragment) {
            navigate(itemId, R.id.userManagementFragment);
        } else if (itemId == R.id.productListFragment) {
            navigate(itemId, R.id.productListFragment);
        } else if (itemId == R.id.brandListFragment) {
            navigate(itemId, R.id.brandListFragment);
        } else if (itemId == R.id.clientListFragment) {
            navigate(itemId, R.id.clientListFragment);
        } else if (itemId == R.id.discountListFragment) {
            navigate(itemId, R.id.discountListFragment);
        } else if (itemId == R.id.nav_dashboard) {
            navigate(itemId, R.id.nav_dashboard);
        } else if (itemId == R.id.nav_suppliers) {
            navigate(itemId, R.id.nav_suppliers);
        } else if (itemId == R.id.nav_purchase_orders) {
            navigate(itemId, R.id.nav_purchase_orders);
        } else if (itemId == R.id.saleListFragment) {
            navigate(itemId, R.id.saleListFragment);
        } else if (navController != null) {
            NavigationUI.onNavDestinationSelected(item, navController);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigate(int menuItemId, int destinationId) {
        if (navController != null) {
            navController.navigate(destinationId);
        }
    }

    /**
     * Navigates to the given destination clearing the entire back stack.
     * Uses {@code popUpTo(R.id.nav_graph, inclusive = true)} as required by
     * the audit (RNF 1.4/1.5).
     */
    private void navigateClearingBackStack(int destinationId) {
        if (navController == null) {
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        navController.navigate(destinationId, null, options);
    }

    /**
     * UX-D6: Shows a full-screen alert dialog when an alarm fires while the
     * app is in the foreground.
     */
    private void showForegroundAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Routes the toolbar Up arrow and the system Back button through the
     * NavController so every module (Products, Brands, Clients, Suppliers…)
     * navigates back to its list correctly instead of crashing.
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null && navController.navigateUp()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (sessionManager != null && sessionManager.isLoggedIn() && sessionManager.isTemporaryPassword()) {
            return;
        }
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (navController != null && navController.navigateUp()) {
            // NavController handled it (e.g. popped a detail screen)
        } else {
            super.onBackPressed();
        }
    }

    public NavController getNavController() {
        return navController;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
