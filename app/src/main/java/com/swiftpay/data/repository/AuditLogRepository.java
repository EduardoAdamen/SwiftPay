package com.swiftpay.data.repository;

import android.content.Context;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.AuditLog;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Repository para operaciones sobre la tabla audit_log.
 */
public class AuditLogRepository {

    private final SwiftPayDatabase db;

    public AuditLogRepository(Context context) {
        this.db = SwiftPayDatabase.getInstance(context);
    }

    /** Inserta un log de auditoría de forma asíncrona */
    public void insert(AuditLog log) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.auditLogDao().insert(log);
        });
    }

    /** Obtiene los últimos N registros de auditoría (síncrono, llamar desde background) */
    public List<AuditLog> getRecent(int limit) {
        return db.auditLogDao().getRecent(limit);
    }
}
