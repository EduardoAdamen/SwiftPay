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

                // Seed Sprint 2: Marcas
                if (db.brandDao().getAllPaged() != null && db.brandDao().getByIdSync(1) == null) {
                    com.swiftpay.data.entity.Brand b1 = new com.swiftpay.data.entity.Brand(); b1.setName("Coca-Cola"); b1.setCreatedAt(now); db.brandDao().insert(b1);
                    com.swiftpay.data.entity.Brand b2 = new com.swiftpay.data.entity.Brand(); b2.setName("Bimbo"); b2.setCreatedAt(now); db.brandDao().insert(b2);
                    com.swiftpay.data.entity.Brand b3 = new com.swiftpay.data.entity.Brand(); b3.setName("Sabritas"); b3.setCreatedAt(now); db.brandDao().insert(b3);

                    // Seed Sprint 2: Categorías de Producto
                    com.swiftpay.data.entity.ProductCategory c1 = new com.swiftpay.data.entity.ProductCategory(); c1.setName("Bebidas"); c1.setCreatedAt(now); db.productCategoryDao().insert(c1);
                    com.swiftpay.data.entity.ProductCategory c2 = new com.swiftpay.data.entity.ProductCategory(); c2.setName("Botanas"); c2.setCreatedAt(now); db.productCategoryDao().insert(c2);

                    // Seed Sprint 2: Productos
                    com.swiftpay.data.entity.Product p1 = new com.swiftpay.data.entity.Product(); p1.setSku("7501055310883"); p1.setName("Coca-Cola 600ml"); p1.setPrice(18.0); p1.setStock(50); p1.setCategoryId(1L); p1.setBrandId(1L); p1.setIsActive(1); p1.setVersion(1); p1.setCreatedAt(now); p1.setUpdatedAt(now); db.productDao().insert(p1);
                    com.swiftpay.data.entity.Product p2 = new com.swiftpay.data.entity.Product(); p2.setSku("7501000111201"); p2.setName("Pan Blanco Bimbo"); p2.setPrice(45.0); p2.setStock(20); p2.setCategoryId(2L); p2.setBrandId(2L); p2.setIsActive(1); p2.setVersion(1); p2.setCreatedAt(now); p2.setUpdatedAt(now); db.productDao().insert(p2);
                    com.swiftpay.data.entity.Product p3 = new com.swiftpay.data.entity.Product(); p3.setSku("7501011131133"); p3.setName("Doritos Nacho"); p3.setPrice(16.0); p3.setStock(30); p3.setCategoryId(2L); p3.setBrandId(3L); p3.setIsActive(1); p3.setVersion(1); p3.setCreatedAt(now); p3.setUpdatedAt(now); db.productDao().insert(p3);
                    com.swiftpay.data.entity.Product p4 = new com.swiftpay.data.entity.Product(); p4.setSku("7501055300075"); p4.setName("Sprite 600ml"); p4.setPrice(18.0); p4.setStock(40); p4.setCategoryId(1L); p4.setBrandId(1L); p4.setIsActive(1); p4.setVersion(1); p4.setCreatedAt(now); p4.setUpdatedAt(now); db.productDao().insert(p4);
                    com.swiftpay.data.entity.Product p5 = new com.swiftpay.data.entity.Product(); p5.setSku("7501011111111"); p5.setName("Cheetos Torciditos"); p5.setPrice(15.0); p5.setStock(35); p5.setCategoryId(2L); p5.setBrandId(3L); p5.setIsActive(1); p5.setVersion(1); p5.setCreatedAt(now); p5.setUpdatedAt(now); db.productDao().insert(p5);
                    Log.d(TAG, "Sprint 2 dummy data seeded successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error seeding database", e);
            }
        });
    }
}
