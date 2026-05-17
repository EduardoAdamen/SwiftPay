package com.swiftpay.data.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.swiftpay.data.dao.*;
import com.swiftpay.data.entity.*;

@Database(
        entities = {
                User.class,
                Client.class,
                ClientCategory.class,
                Brand.class,
                ProductCategory.class,
                Product.class,
                Supplier.class,
                PurchaseOrder.class,
                PurchaseOrderItem.class,
                Sale.class,
                SaleItem.class,
                SaleStatusHistory.class,
                DiscountCode.class,
                CashRegister.class,
                SystemEvent.class,
                AuditLog.class,
                UserPreferences.class
        },
        version = 1,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class SwiftPayDatabase extends RoomDatabase {

    private static volatile SwiftPayDatabase INSTANCE;
    private static final String DATABASE_NAME = "swiftpay_db";

    // --- DAOs ---
    public abstract UserDao userDao();
    public abstract ClientDao clientDao();
    public abstract ClientCategoryDao clientCategoryDao();
    public abstract BrandDao brandDao();
    public abstract ProductCategoryDao productCategoryDao();
    public abstract ProductDao productDao();
    public abstract SupplierDao supplierDao();
    public abstract PurchaseOrderDao purchaseOrderDao();
    public abstract SaleDao saleDao();
    public abstract SaleItemDao saleItemDao();
    public abstract SaleStatusHistoryDao saleStatusHistoryDao();
    public abstract DiscountCodeDao discountCodeDao();
    public abstract CashRegisterDao cashRegisterDao();
    public abstract SystemEventDao systemEventDao();
    public abstract AuditLogDao auditLogDao();
    public abstract UserPreferencesDao userPreferencesDao();

    public static SwiftPayDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SwiftPayDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    SwiftPayDatabase.class,
                                    DATABASE_NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    DatabaseSeeder.seed(INSTANCE);
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
