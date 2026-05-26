package com.swiftpay.data.db;

import android.util.Log;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.swiftpay.util.PasswordUtils;

/**
 * Inserta datos iniciales en la BD usando SQL directo sobre {@link SupportSQLiteDatabase}.
 *
 * POR QUÉ NO SE USAN LOS DAOs DE ROOM AQUÍ:
 * El callback {@link androidx.room.RoomDatabase.Callback#onCreate} es invocado desde
 * dentro de {@code SQLiteOpenHelper.getWritableDatabase()}. Si desde ahí se llaman
 * DAOs de Room, éstos intentan llamar {@code getWritableDatabase()} de nuevo en el
 * mismo hilo, lo que lanza {@code IllegalStateException: getWritableDatabase called
 * recursively}. Esa excepción es capturada silenciosamente y el admin NUNCA se inserta.
 *
 * La solución correcta es usar {@code db.execSQL()} directamente con el parámetro
 * {@link SupportSQLiteDatabase} que Room ya provee en el callback.
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";
    public static volatile String lastError = null;

    /**
     * Inserta todos los datos iniciales. Se llama desde {@code onCreate(SupportSQLiteDatabase)}.
     * Es síncrono y seguro — no recurre a getWritableDatabase().
     */
    public static void seed(SupportSQLiteDatabase db) {
        try {
            // Verificar si el usuario administrador ya existe para evitar re-sembrar y duplicados
            android.database.Cursor cursor = db.query("SELECT COUNT(*) FROM users WHERE username = 'admin'", new Object[0]);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                        Log.d(TAG, "Admin user already exists, skipping database seeding");
                        return;
                    }
                } finally {
                    cursor.close();
                }
            }

            long now = System.currentTimeMillis();

            // ── Usuario Admin ──────────────────────────────────────────────
            String hash = PasswordUtils.hashPassword("Admin1234");
            db.execSQL("INSERT OR IGNORE INTO users " +
                    "(full_name, username, password_hash, role, is_active, is_temporary_password, created_at, updated_at) " +
                    "VALUES ('Administrador del Sistema','admin','" + hash + "','ADMINISTRADOR',1,1," + now + "," + now + ")");
            Log.d(TAG, "Admin user seeded");

            // ── Marcas ─────────────────────────────────────────────────────
            db.execSQL("INSERT OR IGNORE INTO brands (name, description, created_at, updated_at) VALUES ('Coca-Cola', NULL," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO brands (name, description, created_at, updated_at) VALUES ('Bimbo', NULL," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO brands (name, description, created_at, updated_at) VALUES ('Sabritas', NULL," + now + "," + now + ")");

            // ── Categorías de Producto ─────────────────────────────────────
            db.execSQL("INSERT OR IGNORE INTO product_categories (name, description, created_at) VALUES ('Bebidas', NULL," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO product_categories (name, description, created_at) VALUES ('Botanas', NULL," + now + ")");

            // ── Productos ──────────────────────────────────────────────────
            // El proveedor (id=1) se inserta más abajo; como el FK se valida al final de la transacción implícita,
            // insertamos los productos con supplier_id=NULL primero y los actualizamos después del proveedor.
            db.execSQL("INSERT OR IGNORE INTO products (sku, name, price, stock, category_id, brand_id, supplier_id, is_active, version, created_at, updated_at) VALUES ('7501055310883','Coca-Cola 600ml',18.0,50,1,1,NULL,1,1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO products (sku, name, price, stock, category_id, brand_id, supplier_id, is_active, version, created_at, updated_at) VALUES ('7501000111201','Pan Blanco Bimbo',45.0,20,2,2,NULL,1,1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO products (sku, name, price, stock, category_id, brand_id, supplier_id, is_active, version, created_at, updated_at) VALUES ('7501011131133','Doritos Nacho',16.0,30,2,3,NULL,1,1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO products (sku, name, price, stock, category_id, brand_id, supplier_id, is_active, version, created_at, updated_at) VALUES ('7501055300075','Sprite 600ml',18.0,40,1,1,NULL,1,1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO products (sku, name, price, stock, category_id, brand_id, supplier_id, is_active, version, created_at, updated_at) VALUES ('7501011111111','Cheetos Torciditos',15.0,35,2,3,NULL,1,1," + now + "," + now + ")");

            // ── Categorías de Cliente ──────────────────────────────────────
            db.execSQL("INSERT OR IGNORE INTO client_categories (name, description, created_at) VALUES ('Mayorista', NULL," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO client_categories (name, description, created_at) VALUES ('Minorista', NULL," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO client_categories (name, description, created_at) VALUES ('VIP', NULL," + now + ")");

            // ── Clientes ───────────────────────────────────────────────────
            db.execSQL("INSERT INTO clients (full_name, phone, email, rfc, category_id, notes, is_active, created_at, updated_at) VALUES ('Juan Perez','5512345678','juan@example.com','PEPJ800101XYZ',1,NULL,1," + now + "," + now + ")");
            db.execSQL("INSERT INTO clients (full_name, phone, email, rfc, category_id, notes, is_active, created_at, updated_at) VALUES ('Maria Gomez','5598765432','maria@example.com',NULL,2,NULL,1," + now + "," + now + ")");
            db.execSQL("INSERT INTO clients (full_name, phone, email, rfc, category_id, notes, is_active, created_at, updated_at) VALUES ('Carlos Ruiz','5544332211',NULL,NULL,3,NULL,1," + now + "," + now + ")");
            db.execSQL("INSERT INTO clients (full_name, phone, email, rfc, category_id, notes, is_active, created_at, updated_at) VALUES ('Ana Lopez',NULL,NULL,NULL,2,NULL,1," + now + "," + now + ")");
            db.execSQL("INSERT INTO clients (full_name, phone, email, rfc, category_id, notes, is_active, created_at, updated_at) VALUES ('Pedro Diaz','5566778899',NULL,NULL,1,NULL,0," + now + "," + now + ")");

            // ── Códigos de Descuento ───────────────────────────────────────
            long plus30d = now + 30L * 24 * 60 * 60 * 1000;
            long minus1d = now - 24L * 60 * 60 * 1000;
            db.execSQL("INSERT OR IGNORE INTO discount_codes (code, discount_percentage, expiration_date, is_active, created_at, updated_at) VALUES ('VERANO20',20.0," + plus30d + ",1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO discount_codes (code, discount_percentage, expiration_date, is_active, created_at, updated_at) VALUES ('EXPIRADO10',10.0," + minus1d + ",1," + now + "," + now + ")");
            db.execSQL("INSERT OR IGNORE INTO discount_codes (code, discount_percentage, expiration_date, is_active, created_at, updated_at) VALUES ('INACTIVO50',50.0," + plus30d + ",0," + now + "," + now + ")");

            // ── Proveedor y Orden de Compra ────────────────────────────────
            db.execSQL("INSERT INTO suppliers (name, rfc, phone, email, notes, created_at, updated_at) VALUES ('Distribuidora Mexicana',NULL,'5512345678','ventas@distmex.com','Proveedor principal de abarrotes'," + now + "," + now + ")");
            // Ahora que el proveedor existe, asociamos los productos con él
            db.execSQL("UPDATE products SET supplier_id = 1 WHERE supplier_id IS NULL");
            db.execSQL("INSERT INTO purchase_orders (supplier_id, total, status, created_at, received_at) VALUES (1,5000.0,'COMPLETADA'," + now + ",NULL)");

            // ── Venta de Ejemplo ───────────────────────────────────────────
            db.execSQL("INSERT INTO sales (client_id, seller_id, subtotal, discount_percentage, discount_code_id, total, payment_method, amount_received, change_amount, status, cash_register_id, created_at, updated_at) VALUES (1,1,130.0,0.0,NULL,150.0,'EFECTIVO',0.0,0.0,'PAGADA',NULL," + now + "," + now + ")");

            Log.d(TAG, "Database seeded successfully");

        } catch (Exception e) {
            lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            Log.e(TAG, "Error seeding database", e);
        }
    }
}
