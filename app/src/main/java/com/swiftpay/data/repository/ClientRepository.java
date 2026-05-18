package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Client;
import com.swiftpay.util.AuditLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para la gestión de Clientes.
 * RF 3.1 - 3.9: CRUD, paginación, filtros combinados y métricas.
 */
public class ClientRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public ClientRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<PagingData<Client>> getFilteredClients(String queryStr, Long categoryId, Integer isActive) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM clients WHERE 1=1");
        List<Object> args = new ArrayList<>();

        if (queryStr != null && !queryStr.trim().isEmpty()) {
            queryBuilder.append(" AND (full_name LIKE ? OR email LIKE ? OR rfc LIKE ?)");
            String searchStr = "%" + queryStr.trim() + "%";
            args.add(searchStr);
            args.add(searchStr);
            args.add(searchStr);
        }

        if (categoryId != null && categoryId > 0) {
            queryBuilder.append(" AND category_id = ?");
            args.add(categoryId);
        }

        if (isActive != null) {
            queryBuilder.append(" AND is_active = ?");
            args.add(isActive);
        }

        queryBuilder.append(" ORDER BY full_name ASC");

        SimpleSQLiteQuery sqliteQuery = new SimpleSQLiteQuery(queryBuilder.toString(), args.toArray());

        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.clientDao().getFilteredClients(sqliteQuery)
        ));
    }

    public LiveData<Client> getClientById(long id) {
        return db.clientDao().getById(id);
    }

    public interface OperationCallback {
        void onResult(boolean success, String message);
    }

    public void saveClient(Client client, long userId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                if (client.getId() == 0) {
                    client.setCreatedAt(now);
                    client.setUpdatedAt(now);
                    long newId = db.clientDao().insert(client);
                    AuditLogger.log(context, userId, "CREATE_CLIENT", "CLIENT", newId, "Cliente creado: " + client.getFullName());
                } else {
                    client.setUpdatedAt(now);
                    db.clientDao().update(client);
                    AuditLogger.log(context, userId, "UPDATE_CLIENT", "CLIENT", client.getId(), "Cliente actualizado: " + client.getFullName());
                }
                callback.onResult(true, "Cliente guardado exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al guardar el cliente: " + e.getMessage());
            }
        });
    }

    public void deleteOrDeactivateClient(Client client, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int salesCount = db.clientDao().getSalesCountForClient(client.getId());
                if (salesCount > 0) {
                    // Solo desactivar
                    client.setIsActive(0);
                    client.setUpdatedAt(System.currentTimeMillis());
                    db.clientDao().update(client);
                    AuditLogger.log(context, adminUserId, "DEACTIVATE_CLIENT", "CLIENT", client.getId(), "Cliente desactivado (tiene ventas)");
                    callback.onResult(true, "El cliente tiene historial de ventas y fue desactivado.");
                } else {
                    // Eliminar
                    db.clientDao().delete(client);
                    AuditLogger.log(context, adminUserId, "DELETE_CLIENT", "CLIENT", client.getId(), "Cliente eliminado");
                    callback.onResult(true, "Cliente eliminado exitosamente.");
                }
            } catch (Exception e) {
                callback.onResult(false, "Error al eliminar el cliente: " + e.getMessage());
            }
        });
    }

    public interface ClientStatsCallback {
        void onResult(int totalPurchases, double totalSpent);
    }

    public void getClientStats(long clientId, ClientStatsCallback callback) {
        executor.execute(() -> {
            int count = db.clientDao().getSalesCountForClient(clientId);
            Double total = db.clientDao().getTotalSalesForClient(clientId);
            if (total == null) total = 0.0;
            callback.onResult(count, total);
        });
    }
}
