// com/swiftpay/util/AuditLogger.java
package com.swiftpay.util;

import android.content.Context;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.AuditLog;
import java.util.concurrent.Executors;

/**
 * Registra acciones de auditoría en la tabla audit_log.
 * RNF 1.5: Log de auditoría para login/logout/cambio de contraseña.
 * Se llama desde los Repositories, NUNCA desde la UI.
 */
public final class AuditLogger {

    private AuditLogger() {}

    /**
     * Registra una acción en el log de auditoría de forma asíncrona.
     *
     * @param context    contexto de la aplicación
     * @param userId     ID del usuario que realizó la acción (puede ser null)
     * @param action     tipo de acción (LOGIN, LOGOUT, CHANGE_PASSWORD, etc.)
     * @param entityType tipo de entidad afectada (USER, CLIENT, PRODUCT, etc.)
     * @param entityId   ID de la entidad afectada (puede ser null)
     * @param details    detalles adicionales de la acción
     */
    public static void log(Context context, Long userId, String action,
                           String entityType, Long entityId, String details) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SwiftPayDatabase db = SwiftPayDatabase.getInstance(context);
                AuditLog log = new AuditLog();
                log.setUserId(userId);
                log.setAction(action);
                log.setEntityType(entityType);
                log.setEntityId(entityId);
                log.setDetails(details);
                log.setCreatedAt(System.currentTimeMillis());
                db.auditLogDao().insert(log);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
