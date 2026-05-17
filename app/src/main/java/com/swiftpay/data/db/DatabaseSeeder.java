package com.swiftpay.data.db;

import android.util.Log;
import com.swiftpay.data.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.Executors;

/**
 * Clase encargada de insertar datos iniciales en la base de datos.
 * Se ejecuta únicamente en la primera creación de la BD (onCreate).
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    /**
     * Inserta el usuario administrador por defecto.
     * username: admin, password: Admin1234
     */
    public static void seed(SwiftPayDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Verificar si ya existe un admin
                User existing = db.userDao().getByUsername("admin");
                if (existing != null) {
                    Log.d(TAG, "Admin user already exists, skipping seed");
                    return;
                }

                long now = System.currentTimeMillis();
                User admin = new User();
                admin.setFullName("Administrador del Sistema");
                admin.setUsername("admin");
                admin.setPasswordHash(BCrypt.hashpw("Admin1234", BCrypt.gensalt()));
                admin.setRole("ADMINISTRADOR");
                admin.setIsActive(1);
                admin.setIsTemporaryPassword(1);
                admin.setCreatedAt(now);
                admin.setUpdatedAt(now);

                db.userDao().insert(admin);
                Log.d(TAG, "Admin user seeded successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error seeding database", e);
            }
        });
    }
}
