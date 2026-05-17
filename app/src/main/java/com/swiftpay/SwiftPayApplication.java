package com.swiftpay;

import android.app.Application;
import com.swiftpay.data.db.SwiftPayDatabase;

/**
 * Clase Application de SwiftPay.
 * Inicializa la base de datos y registra callbacks del ciclo de vida.
 */
public class SwiftPayApplication extends Application {

    private SwiftPayDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar la base de datos (ejecuta DatabaseSeeder en onCreate de Room)
        database = SwiftPayDatabase.getInstance(this);
    }

    public SwiftPayDatabase getDatabase() {
        return database;
    }
}
