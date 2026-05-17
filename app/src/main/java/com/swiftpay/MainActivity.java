package com.swiftpay;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.swiftpay.data.preferences.SessionManager;

/**
 * Activity principal de SwiftPay. Host de navegación con DrawerLayout.
 * Controla el menú lateral según el rol del usuario y el timeout de sesión.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Forzar acceso a la BD para trigger del Seeder
        SwiftPayApplication app = (SwiftPayApplication) getApplication();
        app.getDatabase().userDao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar validez de sesión en cada resume
        if (sessionManager.isLoggedIn() && !sessionManager.checkSessionValidity()) {
            sessionManager.clearSession();
            // Navegar a login si la sesión expiró
            if (navController != null) {
                navController.navigate(R.id.loginFragment);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Actualizar última actividad en cada interacción táctil
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Configura el menú del Navigation Drawer según el rol del usuario.
     */
    public void setupMenuForRole(String role) {
        navigationView.getMenu().clear();
        if (role == null) return;

        switch (role) {
            case "VENDEDOR":
                navigationView.inflateMenu(R.menu.menu_vendedor);
                break;
            case "ADMINISTRADOR":
                navigationView.inflateMenu(R.menu.menu_admin);
                break;
            case "GESTOR_PRODUCTOS":
                navigationView.inflateMenu(R.menu.menu_gestor);
                break;
        }

        // Actualizar header del drawer
        updateNavHeader();
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvName = headerView.findViewById(R.id.tv_nav_name);
            TextView tvRole = headerView.findViewById(R.id.tv_nav_role);
            if (tvName != null) tvName.setText(sessionManager.getFullName());
            if (tvRole != null) tvRole.setText(sessionManager.getRole());
        }
    }

    /** Bloquea/desbloquea el drawer (bloqueado en pantalla de login) */
    public void setDrawerLocked(boolean locked) {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(
                    locked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!locked);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_logout) {
            // RF 1.4: Logout
            com.swiftpay.util.AuditLogger.log(this, sessionManager.getUserId(), "LOGOUT", "USER", sessionManager.getUserId(), "Cierre de sesión manual");
            sessionManager.clearSession();
            if (navController != null) {
                navController.navigate(R.id.loginFragment);
            }
        } else if (itemId == R.id.nav_profile) {
            if (navController != null) navController.navigate(R.id.profileFragment);
        } else if (itemId == R.id.nav_users) {
            if (navController != null) navController.navigate(R.id.userManagementFragment);
        } else {
            // Para las demás opciones de menú que no tienen fragmentos aún, navegar a dashboard o dejar que NavigationUI intente.
            // Para el foundation, si el fragment existe, NavigationUI lo manejará.
            if (navController != null) {
                NavigationUI.onNavDestinationSelected(item, navController);
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
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
