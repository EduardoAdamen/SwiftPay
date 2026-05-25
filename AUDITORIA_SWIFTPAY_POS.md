# Auditoria SwiftPay POS

Fecha: 2026-05-25

## 1. Resumen Ejecutivo de Conformidad

- RF cubiertos: 15/24 = 62.5%.
- RNF cubiertos: 4/11 = 36.4%.
- UX cubiertos: 3/13 = 23.1%.
- Build/runtime: FALLIDO. `gradlew.bat assembleDebug --no-daemon --stacktrace` falla en `app/src/main/java/com/swiftpay/util/PdfGenerator.java` por caracteres no mapeables UTF-8 en los textos `Descripcion` y `Gracias por su compra`.

## 2. Mapeo de Arquitectura e Inmutabilidad

- SharedPreferences cifradas: FALLIDO. `SessionManager` usa `EncryptedSharedPreferences`, pero acepta fallback a `SharedPreferences` plano.
- Room: PARCIAL. La base declara las tablas requeridas, pero `users.username` no es `UNIQUE` y `password_hash` no esta anotado `@NonNull`.
- Control de roles UI: PARCIAL. Se infla un menu distinto por rol, pero no se usa `menu.setGroupVisible()` como exige RF 1.4.
- Paging 3: CUMPLE para productos, marcas, clientes, descuentos y ventas.
- Inmutabilidad financiera: CUMPLE en `SaleItem` con `unit_price` y `catalog_price`.
- Bloqueo optimista: PARCIAL. DAO y repositorio validan `version`; falta excepcion tipada y dialogo UI de conflicto.

## 3. Tabla de Errores o Desviaciones Encontradas

| Modulo/Sprint | ID Requisito | Tipo | Hallazgo | Archivo(s) Afectado(s) | Severidad |
| --- | --- | --- | --- | --- | --- |
| Build | N/A | RNF | FALLIDO: el proyecto no compila por caracteres incompatibles con UTF-8. | `app/src/main/java/com/swiftpay/util/PdfGenerator.java` | Alta |
| UI/UX | UX-B5/B6/B8 | UX | FALLIDO: hay colores hardcodeados en layouts; no todo usa referencias semanticas y hay contrastes no certificables. | `fragment_dashboard.xml`, `fragment_event_list.xml`, `fragment_settings.xml`, `item_event.xml`, `fragment_barcode_scanner.xml` | Alta |
| UI/UX | UX-B6 | UX | FALLIDO: fuentes existen, pero multiples `TextView` definen `android:textSize` directo y no usan estilos globales. | `res/layout/*.xml`, `res/values/styles.xml` | Media |
| UI/UX | UX-F1/F2 | UX | FALLIDO: no existe aplicacion global de `Configuration.fontScale` 0.85/1.0/1.3 ni modo accesible 56dp. | `MainActivity.java`, `SettingsFragment.java`, `UserPreferences.java` | Alta |
| UI/UX | UX-C1/C2/C5 | UX | FALLIDO: `ProductPagingAdapter` usa Glide directo, ignora `imagesEnabled`; no hay metrica o enforcement de cache <2s. | `ProductPagingAdapter.java`, `ImageLoader.java` | Alta |
| UI/UX | UX-C4 | UX | FALLIDO: carga de imagen valida tamano con `InputStream.available()`, no tamano real; producto no usa `ImageUtils` en el flujo. | `ImageUtils.java`, `ProductFormFragment.java` | Alta |
| UI/UX | UX-A4 | UX | FALLIDO: no existe `LoadingStateView` ni estados Cargando/Error/Vacio en cada listado. | `ui/*ListFragment.java`, `res/layout/*list*.xml` | Alta |
| Sesion | RNF 1.4/1.5 | RNF | FALLIDO: fallback no cifrado y expiracion navega a login sin `popUpTo` inclusivo. | `SessionManager.java`, `MainActivity.java` | Alta |
| Seguridad | RNF 1.1/1.2/1.3 | RNF | FALLIDO: dependencia jbcrypt existe, pero `PasswordUtils` implementa PBKDF2; regex solo valida longitud minima. | `PasswordUtils.java`, `ValidationUtils.java` | Alta |
| Productos | RNF 4.4 | RNF | PARCIAL: `WHERE id AND version` existe, pero no hay excepcion tipada ni dialogo de conflicto en UI. | `ProductDao.java`, `ProductRepository.java`, `ProductFormFragment.java` | Media |
| Transacciones | RNF 2.9/7.10 | RNF | PARCIAL: se usa `db.runInTransaction`, pero el plan exige metodos Room anotados con `@Transaction`. | `SaleRepository.java`, `PurchaseOrderRepository.java` | Media |
| Room | users | RNF | FALLIDO: `username` no es `UNIQUE`; `password_hash` no esta marcado NOT NULL. | `User.java` | Alta |
| Auth | RF 1.4/RF 1.9 | RF | FALLIDO: no se usa `setGroupVisible`; cambio obligatorio bloquea drawer, pero no impide backstack/navegacion global y no refresca flag de sesion. | `MainActivity.java`, `ForcePasswordChangeFragment.java` | Alta |
| Clientes | RF 3.1 | RF | FALLIDO: `ClientFormFragment` no valida RFC/email/telefono antes de persistir; el boton solo vuelve atras. | `ClientFormFragment.java`, `ValidationUtils.java` | Alta |
| PDF | RF 2.8 | RF | FALLIDO: `PdfGenerator` escribe en `filesDir/receipts`, pero `file_paths.xml` solo expone `external-files-path receipts`; `Intent` no agrega `FLAG_GRANT_READ_URI_PERMISSION`. | `PdfGenerator.java`, `file_paths.xml`, `SaleDetailFragment.java` | Alta |
| Notificaciones | UX-D1-D6 | UX | FALLIDO: canal no se inicializa en `Application`, no lee `notification_sound`, no hay iconos contextuales ni `DialogFragment` foreground. | `SwiftPayApplication.java`, `NotificationHelper.java`, `AlarmReceiver.java` | Alta |
| Ajustes UX | UX-E1/E2/E4/B7 | UX | FALLIDO: cambio de tema reinicia actividad, esquemas de color devuelven `-1`, vista compacta y animaciones no se aplican. | `SettingsFragment.java`, `ThemeManager.java`, adapters | Alta |
| Datos | Limpieza | RNF | FALLIDO: hay literales `` `n`` incrustados en manifest y nav graph; aunque XML parsea, son texto basura en recursos criticos. | `AndroidManifest.xml`, `nav_graph.xml` | Media |

## 4. Bloques de Codigo Correctivos

### 4.1 `SessionManager.java`

```java
package com.swiftpay.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Gestiona exclusivamente la sesion cifrada del usuario y el timeout de inactividad.
 */
public final class SessionManager {
    private static final String PREFS_NAME = "swiftpay_session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_LAST_ACTIVITY = "lastActivityTimestamp";
    private static final String KEY_IS_TEMP_PASSWORD = "isTemporaryPassword";
    private static final long SESSION_TIMEOUT_MS = 30L * 60L * 1000L;

    private final SharedPreferences prefs;

    public SessionManager(@NonNull Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context.getApplicationContext(),
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar almacenamiento cifrado de sesion.", e);
        }
    }

    public void createSession(long userId, String username, String fullName, String role, boolean isTemporaryPassword) {
        long now = System.currentTimeMillis();
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_ROLE, role)
                .putLong(KEY_LOGIN_TIMESTAMP, now)
                .putLong(KEY_LAST_ACTIVITY, now)
                .putBoolean(KEY_IS_TEMP_PASSWORD, isTemporaryPassword)
                .commit();
    }

    public boolean checkSessionValidity() {
        long lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0L);
        return lastActivity > 0L && System.currentTimeMillis() - lastActivity <= SESSION_TIMEOUT_MS;
    }

    public void updateLastActivity() {
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }

    public void markTemporaryPasswordResolved() {
        prefs.edit().putBoolean(KEY_IS_TEMP_PASSWORD, false).apply();
    }

    public boolean isLoggedIn() { return prefs.getLong(KEY_USER_ID, -1L) != -1L; }
    public void clearSession() { prefs.edit().clear().commit(); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1L); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, ""); }
    public boolean isTemporaryPassword() { return prefs.getBoolean(KEY_IS_TEMP_PASSWORD, false); }
    public boolean hasRole(String role) { return role != null && role.equals(getRole()); }
}
```

### 4.2 `PasswordUtils.java`

```java
package com.swiftpay.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidades de contrasena basadas estrictamente en jbcrypt.
 */
public final class PasswordUtils {
    private static final int BCRYPT_COST = 12;

    private PasswordUtils() {}

    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena no puede ser null.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return password != null
                && hashedPassword != null
                && hashedPassword.startsWith("$2")
                && BCrypt.checkpw(password, hashedPassword);
    }
}
```

### 4.3 `ValidationUtils.java`

```java
package com.swiftpay.util;

import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Validaciones centralizadas de entradas de usuario.
 */
public final class ValidationUtils {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Pattern RFC_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidRFC(String rfc) {
        return rfc != null && RFC_PATTERN.matcher(rfc.trim().toUpperCase()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            return "La contrasena debe tener minimo 8 caracteres, una letra y un numero";
        }
        return null;
    }
}
```

### 4.4 `file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="receipts" path="receipts/" />
    <files-path name="images" path="images/" />
</paths>
```

### 4.5 `User.java` cabecera corregida

```java
@Entity(tableName = "users", indices = @Index(value = "username", unique = true))
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "password_hash")
    private String passwordHash;
}
```

### 4.6 Correcciones obligatorias adicionales

- Reemplazar `ProductPagingAdapter` para usar solo `ImageLoader.loadImage(...)` con la preferencia `imagesEnabled`.
- Implementar `LoadingStateView` reusable y conectarlo a todos los adapters Paging mediante `LoadStateListener`.
- Reemplazar navegacion de expiracion por `NavOptions.Builder().setPopUpTo(R.id.nav_graph, true)`.
- Mover `NotificationHelper.createNotificationChannel(this)` a `SwiftPayApplication.onCreate()`.
- Agregar lectura de `UserPreferences.notificationSound`, `setSound(...)`, icono contextual por tipo y `DialogFragment` fullscreen si `ProcessLifecycleOwner` indica foreground.
- Definir estilos `Theme.SwiftPay.Emerald`, `Purple`, `Coral`, `Teal` y aplicar `activity.getTheme().applyStyle(...)`; `ThemeManager.getColorSchemeResId()` no puede retornar `-1`.
- Eliminar los literales `` `n`` de `AndroidManifest.xml` y `nav_graph.xml`.
- Cambiar textos no ASCII o asegurar UTF-8 real en `PdfGenerator.java`; por ejemplo `Descripcion` y `Gracias por su compra`.
