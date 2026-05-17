# Plan Maestro de Implementación — SwiftPay POS
## PARTE 1: Análisis Previo del Documento

---

## 1.1 Mapa de Dependencias entre Módulos

| Módulo | Depende de | Razón |
|--------|-----------|-------|
| MO_02 Ventas | MO_01, MO_03, MO_04, MO_06 | Requiere usuario autenticado, cliente, productos del catálogo y códigos de descuento |
| MO_03 Clientes | MO_01 | Requiere rol para controlar acceso (VENDEDOR registra, ADMIN gestiona estado) |
| MO_04 Productos | MO_01, MO_05 | Requiere rol y marca asociada al producto |
| MO_05 Marcas | MO_01 | Requiere rol GESTOR DE PRODUCTOS |
| MO_06 Descuentos | MO_01 | Requiere rol ADMINISTRADOR |
| MO_07 Proveedores | MO_01, MO_04 | Requiere rol y productos para órdenes de compra |
| MO_08 Panel | MO_01, MO_02, MO_03 | Requiere eventos generados por ventas y clientes nuevos |

**Orden crítico de implementación:** MO_01 → MO_05 → MO_04 → MO_03 → MO_06 → MO_02 → MO_07 → MO_08

---

## 1.2 Riesgos Técnicos Identificados

| # | Riesgo | Impacto | Mitigación |
|---|--------|---------|------------|
| R1 | Impresión Bluetooth PDF (RF 2.8) | Alto | Usar iTextPDF para generar PDF. Para impresión Bluetooth usar Android BluetoothSocket con ESC/POS. Proveer opción de "compartir PDF" como fallback |
| R2 | Escáner de barras con cámara (RF 4.4) | Medio | Usar ZXing Android Embedded. Manejar gracefully la denegación de permisos |
| R3 | Timeout de sesión 30 min (RF 1.5) | Medio | Implementar ActivityLifecycleCallbacks en Application. Verificar en cada onResume() |
| R4 | Actualización atómica de stock (RF 2.9, RF 7.10) | Alto | Usar @Transaction de Room |
| R5 | Bloqueo optimista en productos (RNF 4.4) | Medio | Columna version INTEGER. En cada UPDATE verificar que version no haya cambiado |
| R6 | Paginación en listas grandes | Medio | Usar Paging 3 con PagingSource de Room |
| R7 | Validación RFC México | Bajo | Regex: `^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$` |

---

## 1.3 Decisiones de Arquitectura Críticas (INMUTABLES)

### 1.3.1 Manejo de Sesión Activa
- La sesión se almacena en **SharedPreferences** cifradas (EncryptedSharedPreferences de AndroidX Security).
- Campos persistidos: `userId`, `username`, `fullName`, `role`, `loginTimestamp`, `lastActivityTimestamp`, `isTemporaryPassword`.
- Clase `SessionManager` en paquete `util/` encapsula toda la lógica.
- En cada `Activity.onResume()` se llama a `SessionManager.checkSessionValidity()`. Si `System.currentTimeMillis() - lastActivityTimestamp > 30 * 60 * 1000`, se limpia la sesión y se redirige a LoginActivity.
- Cada interacción del usuario actualiza `lastActivityTimestamp` via `dispatchTouchEvent()` en `MainActivity`.

### 1.3.2 Control de Roles en Runtime
- Enum `UserRole { VENDEDOR, ADMINISTRADOR, GESTOR_PRODUCTOS }` en `data/entity/`.
- `MainActivity` al iniciar sesión evalúa `SessionManager.getRole()` y navega al destino correspondiente.
- Cada Fragment que requiere un rol específico verifica en `onViewCreated()` llamando a `SessionManager.hasRole()`.
- Los menús del Navigation Drawer se inflan dinámicamente según rol usando `menu.setGroupVisible()`.
- **Los DAO NO filtran por rol.** El control de acceso es en la capa UI/ViewModel exclusivamente.

### 1.3.3 Navegación con Navigation Component
- Un solo `MainActivity` con un `NavHostFragment`.
- `DrawerLayout` + `NavigationView` como menú lateral.
- 3 menús XML: `menu_vendedor.xml`, `menu_admin.xml`, `menu_gestor.xml`.
- Al hacer login, se infla el menú correspondiente y se navega al `startDestination` del rol.

### 1.3.4 Estrategia de Paginación
- Todas las listas con posibilidad de >50 registros usan Paging 3.
- Los DAOs retornan `PagingSource<Integer, Entity>`.
- Los ViewModels exponen `LiveData<PagingData<Entity>>`.
- Los Fragments usan `PagingDataAdapter` con RecyclerView.

### 1.3.5 Almacenamiento de Imágenes
- Las imágenes se guardan en almacenamiento interno (`getFilesDir()/images/`).
- En la BD se almacena solo la ruta relativa (String).
- Glide se usa para cargar con placeholder.

### 1.3.6 Log de Auditoría
- Tabla `audit_log` centralizada.
- Clase `AuditLogger` en `util/` con métodos estáticos.
- Se llama desde los Repositories, NUNCA desde la UI.

---

## 1.4 Esquema Completo de la Base de Datos

### Tabla: `users`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| full_name | TEXT | NOT NULL | Nombre completo |
| username | TEXT | NOT NULL, UNIQUE | Nombre de usuario |
| password_hash | TEXT | NOT NULL | Hash BCrypt |
| role | TEXT | NOT NULL | VENDEDOR/ADMINISTRADOR/GESTOR_PRODUCTOS |
| profile_image_path | TEXT | NULLABLE | Ruta foto perfil |
| is_active | INTEGER | NOT NULL, DEFAULT 1 | 1=activo |
| is_temporary_password | INTEGER | NOT NULL, DEFAULT 1 | 1=debe cambiar |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `clients`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| full_name | TEXT | NOT NULL | Nombre completo |
| phone | TEXT | NULLABLE | Teléfono 10 dígitos |
| email | TEXT | NULLABLE | Correo electrónico |
| rfc | TEXT | NULLABLE | RFC México |
| category_id | INTEGER | FK → client_categories.id, NULLABLE | Categoría |
| notes | TEXT | NULLABLE | Notas (máx 500) |
| is_active | INTEGER | NOT NULL, DEFAULT 1 | Estado |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `client_categories`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| name | TEXT | NOT NULL, UNIQUE | Nombre categoría |
| description | TEXT | NULLABLE | Descripción |
| created_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `brands`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| name | TEXT | NOT NULL, UNIQUE | Nombre marca |
| description | TEXT | NULLABLE | Descripción |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `product_categories`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| name | TEXT | NOT NULL, UNIQUE | Nombre |
| description | TEXT | NULLABLE | Descripción |
| created_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `products`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| sku | TEXT | NOT NULL, UNIQUE | Código SKU |
| name | TEXT | NOT NULL | Nombre |
| price | REAL | NOT NULL | Precio venta |
| stock | INTEGER | NOT NULL, DEFAULT 0 | Stock |
| category_id | INTEGER | FK → product_categories.id | Categoría |
| brand_id | INTEGER | FK → brands.id, NULLABLE | Marca |
| image_path | TEXT | NULLABLE | Ruta imagen |
| weight | TEXT | NULLABLE | Peso |
| dimensions | TEXT | NULLABLE | Dimensiones |
| tags | TEXT | NULLABLE | Etiquetas CSV |
| is_active | INTEGER | NOT NULL, DEFAULT 1 | Estado |
| version | INTEGER | NOT NULL, DEFAULT 1 | Bloqueo optimista |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `suppliers`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| name | TEXT | NOT NULL | Nombre comercial |
| rfc | TEXT | NULLABLE | RFC |
| phone | TEXT | NULLABLE | Teléfono |
| email | TEXT | NULLABLE | Email |
| notes | TEXT | NULLABLE | Notas |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `purchase_orders`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| supplier_id | INTEGER | FK → suppliers.id, NOT NULL | Proveedor |
| total | REAL | NOT NULL, DEFAULT 0 | Total calculado |
| status | TEXT | NOT NULL, DEFAULT 'PENDIENTE' | Estado |
| created_at | INTEGER | NOT NULL | Fecha creación |
| received_at | INTEGER | NULLABLE | Fecha recepción |

### Tabla: `purchase_order_items`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| order_id | INTEGER | FK → purchase_orders.id | Orden |
| product_id | INTEGER | FK → products.id | Producto |
| quantity | INTEGER | NOT NULL | Cantidad |
| unit_cost | REAL | NOT NULL | Costo unitario |

### Tabla: `sales`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| client_id | INTEGER | FK → clients.id, NULLABLE | Cliente |
| seller_id | INTEGER | FK → users.id, NOT NULL | Vendedor |
| subtotal | REAL | NOT NULL | Subtotal |
| discount_percentage | REAL | DEFAULT 0 | % descuento |
| discount_code_id | INTEGER | FK → discount_codes.id, NULLABLE | Código |
| total | REAL | NOT NULL | Total final |
| payment_method | TEXT | NOT NULL | Método pago |
| amount_received | REAL | DEFAULT 0 | Monto recibido |
| change_amount | REAL | DEFAULT 0 | Cambio |
| status | TEXT | NOT NULL, DEFAULT 'PENDIENTE' | Estado |
| cash_register_id | INTEGER | FK → cash_registers.id, NULLABLE | Sesión caja |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `sale_items`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| sale_id | INTEGER | FK → sales.id | Venta |
| product_id | INTEGER | FK → products.id | Producto |
| quantity | INTEGER | NOT NULL | Cantidad |
| unit_price | REAL | NOT NULL | Precio unitario |
| catalog_price | REAL | NOT NULL | Precio catálogo |

### Tabla: `sale_status_history`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| sale_id | INTEGER | FK → sales.id | Venta |
| previous_status | TEXT | NOT NULL | Estado anterior |
| new_status | TEXT | NOT NULL | Estado nuevo |
| changed_by | INTEGER | FK → users.id | Usuario |
| changed_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `discount_codes`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| code | TEXT | NOT NULL, UNIQUE | Código alfanumérico |
| discount_percentage | REAL | NOT NULL | Porcentaje |
| expiration_date | INTEGER | NOT NULL | Fecha expiración |
| is_active | INTEGER | DEFAULT 1 | Estado |
| created_at | INTEGER | NOT NULL | Timestamp |
| updated_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `cash_registers`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| seller_id | INTEGER | FK → users.id | Vendedor |
| base_amount | REAL | NOT NULL | Monto base |
| expected_amount | REAL | NULLABLE | Monto esperado |
| physical_amount | REAL | NULLABLE | Efectivo físico |
| difference | REAL | NULLABLE | Diferencia |
| opened_at | INTEGER | NOT NULL | Apertura |
| closed_at | INTEGER | NULLABLE | Cierre |

### Tabla: `system_events`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| event_type | TEXT | NOT NULL | NEW_CLIENT/NEW_SALE |
| entity_id | INTEGER | NOT NULL | ID entidad |
| is_reviewed | INTEGER | DEFAULT 0 | Estado |
| reviewed_at | INTEGER | NULLABLE | Timestamp revisión |
| created_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `audit_log`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| user_id | INTEGER | FK → users.id | Usuario |
| action | TEXT | NOT NULL | Tipo acción |
| entity_type | TEXT | NULLABLE | Tipo entidad |
| entity_id | INTEGER | NULLABLE | ID entidad |
| details | TEXT | NULLABLE | Detalles |
| created_at | INTEGER | NOT NULL | Timestamp |

### Tabla: `user_preferences`
| Columna | Tipo | Restricción | Descripción |
|---------|------|-------------|-------------|
| id | INTEGER | PK, AUTOINCREMENT | ID único |
| user_id | INTEGER | FK → users.id, UNIQUE | Usuario |
| theme_mode | TEXT | DEFAULT 'SYSTEM' | LIGHT/DARK/SYSTEM |
| color_scheme | TEXT | DEFAULT 'DEFAULT' | Esquema color |
| font_size | TEXT | DEFAULT 'NORMAL' | SMALL/NORMAL/LARGE |
| compact_view | INTEGER | DEFAULT 0 | Vista compacta |
| animations_enabled | INTEGER | DEFAULT 1 | Animaciones |
| images_enabled | INTEGER | DEFAULT 1 | Cargar imágenes |
| notification_sound | TEXT | NULLABLE | Tono |
| wallpaper_path | TEXT | NULLABLE | Fondo |
| accessibility_mode | INTEGER | DEFAULT 0 | Accesibilidad |


---

# Plan Maestro — PARTE 2: Estructura del Proyecto + Sprints 0-1

---

## 2. Estructura Definitiva del Proyecto

```
com.swiftpay/
├── SwiftPayApplication.java          — Clase Application. Inicializa BD, SessionManager, registra ActivityLifecycleCallbacks
├── MainActivity.java                 — Host de navegación. DrawerLayout + NavHostFragment. Controla menú por rol y timeout
│
├── data/
│   ├── db/
│   │   ├── SwiftPayDatabase.java     — Clase @Database de Room. Define todas las entities y DAOs. Versión y migraciones
│   │   ├── Converters.java           — TypeConverters para Date/Timestamp
│   │   └── DatabaseSeeder.java       — Clase para insertar datos iniciales (admin por defecto)
│   │
│   ├── dao/
│   │   ├── UserDao.java              — CRUD usuarios + búsqueda por username
│   │   ├── ClientDao.java            — CRUD clientes + filtros + paginación
│   │   ├── ClientCategoryDao.java    — CRUD categorías de clientes
│   │   ├── BrandDao.java             — CRUD marcas + paginación
│   │   ├── ProductCategoryDao.java   — CRUD categorías de productos
│   │   ├── ProductDao.java           — CRUD productos + paginación + bloqueo optimista
│   │   ├── SupplierDao.java          — CRUD proveedores
│   │   ├── PurchaseOrderDao.java     — CRUD órdenes de compra + items
│   │   ├── SaleDao.java              — CRUD ventas + filtros + paginación
│   │   ├── SaleItemDao.java          — Items de venta
│   │   ├── SaleStatusHistoryDao.java — Historial de estados de venta
│   │   ├── DiscountCodeDao.java      — CRUD códigos de descuento
│   │   ├── CashRegisterDao.java      — Apertura/cierre de caja
│   │   ├── SystemEventDao.java       — Eventos del sistema
│   │   ├── AuditLogDao.java          — Log de auditoría
│   │   └── UserPreferencesDao.java   — Preferencias de usuario
│   │
│   ├── entity/
│   │   ├── User.java                 — Entidad Room tabla users
│   │   ├── UserRole.java             — Enum VENDEDOR, ADMINISTRADOR, GESTOR_PRODUCTOS
│   │   ├── Client.java               — Entidad Room tabla clients
│   │   ├── ClientCategory.java       — Entidad Room tabla client_categories
│   │   ├── Brand.java                — Entidad Room tabla brands
│   │   ├── ProductCategory.java      — Entidad Room tabla product_categories
│   │   ├── Product.java              — Entidad Room tabla products
│   │   ├── Supplier.java             — Entidad Room tabla suppliers
│   │   ├── PurchaseOrder.java        — Entidad Room tabla purchase_orders
│   │   ├── PurchaseOrderItem.java    — Entidad Room tabla purchase_order_items
│   │   ├── Sale.java                 — Entidad Room tabla sales
│   │   ├── SaleItem.java             — Entidad Room tabla sale_items
│   │   ├── SaleStatusHistory.java    — Entidad Room tabla sale_status_history
│   │   ├── DiscountCode.java         — Entidad Room tabla discount_codes
│   │   ├── CashRegister.java         — Entidad Room tabla cash_registers
│   │   ├── SystemEvent.java          — Entidad Room tabla system_events
│   │   ├── AuditLog.java             — Entidad Room tabla audit_log
│   │   └── UserPreferences.java      — Entidad Room tabla user_preferences
│   │
│   ├── repository/
│   │   ├── AuthRepository.java       — Login, logout, gestión usuarios, hash contraseñas
│   │   ├── ClientRepository.java     — Lógica clientes + categorías + métricas
│   │   ├── ProductRepository.java    — Lógica productos + categorías + bloqueo optimista
│   │   ├── BrandRepository.java      — Lógica marcas
│   │   ├── SaleRepository.java       — Lógica ventas + carrito + transiciones estado + stock atómico
│   │   ├── DiscountRepository.java   — Lógica descuentos + validación vigencia
│   │   ├── SupplierRepository.java   — Lógica proveedores
│   │   ├── PurchaseOrderRepository.java — Lógica órdenes compra + recepción + stock atómico
│   │   ├── CashRegisterRepository.java  — Apertura/cierre caja + arqueo
│   │   ├── SystemEventRepository.java   — Eventos + contadores
│   │   ├── AuditLogRepository.java      — Inserción registros auditoría
│   │   └── UserPreferencesRepository.java — Preferencias usuario
│   │
│   └── preferences/
│       └── SessionManager.java       — SharedPreferences cifradas. Sesión activa, timeout, rol actual
│
├── viewmodel/
│   ├── LoginViewModel.java           — Login, validación credenciales
│   ├── UserViewModel.java            — CRUD usuarios (admin), perfil, cambio contraseña
│   ├── ClientViewModel.java          — CRUD clientes + filtros + paginación
│   ├── ClientCategoryViewModel.java  — CRUD categorías clientes
│   ├── ProductViewModel.java         — CRUD productos + filtros + paginación
│   ├── ProductCategoryViewModel.java — CRUD categorías productos
│   ├── BrandViewModel.java           — CRUD marcas + paginación
│   ├── SaleViewModel.java            — Registro venta + carrito + estados + historial
│   ├── DiscountViewModel.java        — CRUD descuentos + validación código
│   ├── SupplierViewModel.java        — CRUD proveedores
│   ├── PurchaseOrderViewModel.java   — CRUD órdenes compra + recepción
│   ├── CashRegisterViewModel.java    — Apertura/cierre caja + arqueo
│   ├── DashboardViewModel.java       — Contadores panel control + eventos
│   └── SettingsViewModel.java        — Preferencias + tema + accesibilidad
│
├── ui/
│   ├── auth/
│   │   ├── LoginFragment.java        — Pantalla login (RF 1.1, 1.2)
│   │   ├── ProfileFragment.java      — Ver/editar perfil (RF 1.11, 1.12)
│   │   ├── ChangePasswordFragment.java — Cambio contraseña (RF 1.10)
│   │   ├── ForcePasswordChangeFragment.java — Cambio obligatorio contraseña temporal (RF 1.9)
│   │   └── UserManagementFragment.java — Lista/crear/desactivar usuarios (RF 1.7, 1.8, 1.9) [ADMIN]
│   │
│   ├── ventas/
│   │   ├── SaleListFragment.java     — Lista ventas con filtros (RF 2.4)
│   │   ├── SaleDetailFragment.java   — Detalle venta (RF 2.5)
│   │   ├── SaleCartFragment.java     — Carrito de compras (RF 2.10)
│   │   ├── SalePaymentFragment.java  — Registro pago (RF 2.1, 2.2, 2.3)
│   │   ├── SaleStatusHistoryFragment.java — Historial estados (RF 2.7)
│   │   ├── CashOpenFragment.java     — Apertura caja (RF 2.11)
│   │   ├── CashCloseFragment.java    — Cierre caja (RF 2.12)
│   │   └── CashReportFragment.java   — Arqueo caja (RF 2.13)
│   │
│   ├── clientes/
│   │   ├── ClientListFragment.java   — Lista clientes con filtros (RF 3.2)
│   │   ├── ClientFormFragment.java   — Crear/editar cliente (RF 3.1, 3.3)
│   │   ├── ClientDetailFragment.java — Perfil detallado (RF 3.8)
│   │   ├── ClientStatsFragment.java  — Estadísticas (RF 3.6)
│   │   └── ClientCategoryFragment.java — Gestión categorías (RF 3.5)
│   │
│   ├── productos/
│   │   ├── ProductListFragment.java  — Catálogo productos (RF 4.1)
│   │   ├── ProductDetailFragment.java — Detalle producto (RF 4.2)
│   │   ├── ProductFormFragment.java  — Crear/editar producto (RF 4.3, 4.5)
│   │   ├── ProductCategoryFragment.java — Categorías productos (RF 4.7, 4.8, 4.9)
│   │   └── BarcodeScannerFragment.java  — Escáner código barras (RF 4.4)
│   │
│   ├── marcas/
│   │   ├── BrandListFragment.java    — Lista marcas (RF 5.1)
│   │   ├── BrandDetailFragment.java  — Detalle marca (RF 5.2)
│   │   └── BrandFormFragment.java    — Crear/editar marca (RF 5.3, 5.4)
│   │
│   ├── descuentos/
│   │   ├── DiscountListFragment.java — Lista descuentos (RF 6.1)
│   │   ├── DiscountDetailFragment.java — Detalle descuento (RF 6.2)
│   │   └── DiscountFormFragment.java — Crear/editar descuento (RF 6.3, 6.4)
│   │
│   ├── proveedores/
│   │   ├── SupplierListFragment.java — Lista proveedores (RF 7.1)
│   │   ├── SupplierDetailFragment.java — Detalle proveedor (RF 7.2)
│   │   ├── SupplierFormFragment.java — Crear/editar proveedor (RF 7.3, 7.4)
│   │   ├── PurchaseOrderListFragment.java — Lista órdenes (RF 7.5)
│   │   ├── PurchaseOrderDetailFragment.java — Detalle orden (RF 7.6)
│   │   ├── PurchaseOrderFormFragment.java — Crear/editar orden (RF 7.7, 7.9)
│   │   └── PurchaseOrderReceiveFragment.java — Recepción mercancía (RF 7.10)
│   │
│   ├── dashboard/
│   │   ├── DashboardFragment.java    — Panel control (RF 8.1, 8.5)
│   │   └── EventListFragment.java    — Lista eventos (RF 8.2, 8.3)
│   │
│   ├── settings/
│   │   └── SettingsFragment.java     — Configuración: tema, colores, accesibilidad, notificaciones
│   │
│   ├── common/
│   │   ├── AccessDeniedFragment.java — Pantalla acceso denegado
│   │   └── LoadingStateView.java     — Vista reutilizable para estados de carga/error
│   │
│   └── adapter/
│       ├── ClientPagingAdapter.java
│       ├── ProductPagingAdapter.java
│       ├── BrandPagingAdapter.java
│       ├── SalePagingAdapter.java
│       ├── DiscountPagingAdapter.java
│       ├── SupplierAdapter.java
│       ├── PurchaseOrderAdapter.java
│       ├── SaleItemAdapter.java
│       ├── CartItemAdapter.java
│       ├── EventAdapter.java
│       └── UserAdapter.java
│
└── util/
    ├── Constants.java                — Constantes globales (timeout, formatos, límites)
    ├── ValidationUtils.java          — Validaciones: email, RFC, teléfono, contraseña, SKU
    ├── PasswordUtils.java            — Hash BCrypt + verificación
    ├── ImageUtils.java               — Guardar/cargar/comprimir imágenes
    ├── PdfGenerator.java             — Generar comprobante PDF con iTextPDF
    ├── BluetoothPrintHelper.java     — Impresión por Bluetooth
    ├── AuditLogger.java              — Registrar acciones en audit_log
    ├── DateUtils.java                — Formateo y conversión de fechas
    ├── CurrencyUtils.java            — Formateo moneda MXN
    └── NotificationHelper.java       — Crear notificaciones del sistema
```

### Recursos XML

```
res/
├── layout/
│   ├── activity_main.xml
│   ├── fragment_login.xml
│   ├── fragment_profile.xml
│   ├── fragment_change_password.xml
│   ├── fragment_force_password_change.xml
│   ├── fragment_user_management.xml
│   ├── fragment_sale_list.xml
│   ├── fragment_sale_detail.xml
│   ├── fragment_sale_cart.xml
│   ├── fragment_sale_payment.xml
│   ├── fragment_sale_status_history.xml
│   ├── fragment_cash_open.xml
│   ├── fragment_cash_close.xml
│   ├── fragment_cash_report.xml
│   ├── fragment_client_list.xml
│   ├── fragment_client_form.xml
│   ├── fragment_client_detail.xml
│   ├── fragment_client_stats.xml
│   ├── fragment_client_category.xml
│   ├── fragment_product_list.xml
│   ├── fragment_product_detail.xml
│   ├── fragment_product_form.xml
│   ├── fragment_product_category.xml
│   ├── fragment_barcode_scanner.xml
│   ├── fragment_brand_list.xml
│   ├── fragment_brand_detail.xml
│   ├── fragment_brand_form.xml
│   ├── fragment_discount_list.xml
│   ├── fragment_discount_detail.xml
│   ├── fragment_discount_form.xml
│   ├── fragment_supplier_list.xml
│   ├── fragment_supplier_detail.xml
│   ├── fragment_supplier_form.xml
│   ├── fragment_purchase_order_list.xml
│   ├── fragment_purchase_order_detail.xml
│   ├── fragment_purchase_order_form.xml
│   ├── fragment_purchase_order_receive.xml
│   ├── fragment_dashboard.xml
│   ├── fragment_event_list.xml
│   ├── fragment_settings.xml
│   ├── fragment_access_denied.xml
│   ├── item_client.xml
│   ├── item_product.xml
│   ├── item_brand.xml
│   ├── item_sale.xml
│   ├── item_discount.xml
│   ├── item_supplier.xml
│   ├── item_purchase_order.xml
│   ├── item_sale_item.xml
│   ├── item_cart_item.xml
│   ├── item_event.xml
│   ├── item_user.xml
│   ├── dialog_confirm_delete.xml
│   ├── dialog_discount_code.xml
│   ├── nav_header.xml
│   └── view_loading_state.xml
│
├── menu/
│   ├── menu_vendedor.xml
│   ├── menu_admin.xml
│   └── menu_gestor.xml
│
├── navigation/
│   └── nav_graph.xml
│
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   ├── dimens.xml
│   ├── themes.xml
│   └── styles.xml
│
├── values-night/
│   ├── colors.xml
│   └── themes.xml
│
├── font/
│   ├── roboto_regular.ttf
│   └── roboto_medium.ttf
│
└── drawable/
    └── (todos los ic_*.xml vectoriales según sección 4.3)
```

---

## 3. Plan de Sprints Detallado

---

### SPRINT 0 — Fundación del Proyecto

**Duración estimada:** 2 días
**Módulos:** Ninguno (infraestructura)
**RF cubiertos:** Ninguno
**RNF cubiertos:** Ninguno
**UX cubiertos:** UX-B5, UX-B6, UX-B8 (sistema de diseño base)

#### Objetivo
Crear el proyecto Android Studio con toda la configuración base: Gradle, dependencias, sistema de diseño (colors, dimens, themes, fonts), estructura de paquetes vacía, base de datos Room con todas las entities/DAOs compilables, y la navegación esqueleto.

#### Criterios de Aceptación
1. El proyecto compila sin errores con `./gradlew assembleDebug`
2. La BD Room se crea correctamente al iniciar la app (las 16 tablas existen)
3. Los temas claro/oscuro se aplican correctamente
4. Los colores, dimensiones y tipografía coinciden EXACTAMENTE con la sección 4 del documento
5. El NavigationDrawer se muestra con el layout base
6. El DatabaseSeeder inserta un usuario admin por defecto (username: `admin`, password: `Admin1234`)

#### Archivos a Crear

**Configuración Gradle:**
- `build.gradle` (project) — plugins AGP, kotlin-kapt (para Room annotation processor con Java)
- `build.gradle` (app) — todas las dependencias (ver sección 5 del plan), compileSdk 34, minSdk 26

**Entities (todas, compilables con campos y anotaciones Room):**
- `data/entity/UserRole.java`
- `data/entity/User.java`
- `data/entity/Client.java`
- `data/entity/ClientCategory.java`
- `data/entity/Brand.java`
- `data/entity/ProductCategory.java`
- `data/entity/Product.java`
- `data/entity/Supplier.java`
- `data/entity/PurchaseOrder.java`
- `data/entity/PurchaseOrderItem.java`
- `data/entity/Sale.java`
- `data/entity/SaleItem.java`
- `data/entity/SaleStatusHistory.java`
- `data/entity/DiscountCode.java`
- `data/entity/CashRegister.java`
- `data/entity/SystemEvent.java`
- `data/entity/AuditLog.java`
- `data/entity/UserPreferences.java`

**DAOs (todos, con métodos básicos insert/update/delete/getById):**
- Todos los 16 DAOs listados en la estructura

**Database:**
- `data/db/SwiftPayDatabase.java`
- `data/db/Converters.java`
- `data/db/DatabaseSeeder.java`

**Preferencias:**
- `data/preferences/SessionManager.java` — esqueleto con métodos de sesión

**Utils:**
- `util/Constants.java`
- `util/DateUtils.java`
- `util/CurrencyUtils.java`

**Recursos:**
- `res/values/colors.xml` — TODA la paleta HEX de la sección 4.1
- `res/values-night/colors.xml` — colores modo oscuro
- `res/values/dimens.xml` — TODOS los valores dp de la sección 4.4
- `res/values/themes.xml` — tema claro Material3
- `res/values-night/themes.xml` — tema oscuro
- `res/values/styles.xml` — estilos de texto según sección 4.2
- `res/values/strings.xml` — strings iniciales
- `res/font/roboto_regular.ttf` y `roboto_medium.ttf`
- `res/layout/activity_main.xml` — DrawerLayout + NavHostFragment + NavigationView
- `res/layout/nav_header.xml` — Header del drawer (avatar + nombre + rol)
- `res/menu/menu_vendedor.xml`, `menu_admin.xml`, `menu_gestor.xml`
- `res/navigation/nav_graph.xml` — esqueleto con destino placeholder
- Todos los drawables vectoriales `ic_*.xml` de la sección 4.3 (28 íconos)

**App base:**
- `SwiftPayApplication.java`
- `MainActivity.java` — esqueleto con drawer y NavController

#### Dependencias: Ninguna (es el primer sprint)
#### Seed Data: Usuario admin insertado por DatabaseSeeder

#### Decisiones Técnicas
- `compileSdk = 34`, `minSdk = 26`, `targetSdk = 34`
- Java 17 (`sourceCompatibility = JavaVersion.VERSION_17`)
- Room annotation processor con `annotationProcessor` (no kapt ya que es Java puro)
- Todos los timestamps se almacenan como `long` (millis epoch). Converters.java NO necesita TypeConverter para esto
- DatabaseSeeder se ejecuta en `RoomDatabase.Callback.onCreate()` con un `Executors.newSingleThreadExecutor()`

#### Riesgos y Verificación
- **Riesgo:** Conflicto de versiones de dependencias → verificar que todas las dependencias compilen juntas
- **Verificar:** La app se instala en emulador, se ve el drawer, la BD tiene las 16 tablas, el admin seed existe

---

### SPRINT 1 — Autenticación y Gestión de Usuarios

**Duración estimada:** 4 días
**Módulos:** MO_01 Autenticación
**RF cubiertos:** RF 1.1, RF 1.2, RF 1.3, RF 1.4, RF 1.5, RF 1.6, RF 1.7, RF 1.8, RF 1.9, RF 1.10, RF 1.11, RF 1.12
**RNF cubiertos:** RNF 1.1, RNF 1.2, RNF 1.3, RNF 1.4, RNF 1.5
**UX cubiertos:** UX-A1, UX-A3, UX-A4, UX-C4

#### Objetivo
Implementar el sistema completo de autenticación: login, logout, timeout de sesión, gestión de usuarios por admin, perfil, cambio de contraseña, foto de perfil, y control de acceso por rol con navegación dinámica.

#### Criterios de Aceptación
1. El login valida credenciales contra BD en <2 seg y redirige al panel correcto según rol
2. Credenciales incorrectas muestran mensaje de error claro
3. Contraseñas se almacenan como hash BCrypt
4. Sesión expira tras 30 min de inactividad, redirige a login
5. Botón logout visible en drawer, limpia sesión y redirige a login
6. Admin puede crear usuario con nombre, username, password temporal y rol
7. Admin puede desactivar/reactivar usuarios
8. Admin puede restablecer contraseña → usuario forzado a cambiar al siguiente login
9. Cualquier usuario cambia su contraseña (verificando actual). Mínimo 8 chars, letras+números
10. Cualquier usuario ve su perfil (nombre, username, rol, foto)
11. Cualquier usuario sube/actualiza foto perfil (JPG/JPEG/PNG, máx 5MB)
12. Drawer muestra solo opciones del rol actual
13. Todo intento de login/logout/cambio contraseña queda en audit_log

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/AuthRepository.java` — implementar login, createUser, deactivate, resetPassword, changePassword, uploadPhoto
- `data/repository/AuditLogRepository.java` — implementar insert
- `viewmodel/LoginViewModel.java` — implementar login flow
- `viewmodel/UserViewModel.java` — implementar CRUD usuarios, perfil, cambio contraseña
- `ui/auth/LoginFragment.java` — pantalla login
- `ui/auth/ProfileFragment.java` — pantalla perfil
- `ui/auth/ChangePasswordFragment.java` — cambio contraseña
- `ui/auth/ForcePasswordChangeFragment.java` — cambio obligatorio
- `ui/auth/UserManagementFragment.java` — lista y gestión usuarios (ADMIN)
- `ui/common/AccessDeniedFragment.java` — acceso denegado
- `ui/adapter/UserAdapter.java` — adapter lista usuarios
- `util/PasswordUtils.java` — hash BCrypt con jBCrypt
- `util/ValidationUtils.java` — validación contraseña
- `util/ImageUtils.java` — guardar foto perfil
- `util/AuditLogger.java` — registrar en audit_log
- Completar `SessionManager.java` — toda la lógica de sesión
- Modificar `MainActivity.java` — implementar timeout, menú dinámico, dispatchTouchEvent
- Modificar `SwiftPayApplication.java` — registrar lifecycle callbacks

**Layouts:**
- `res/layout/fragment_login.xml`
- `res/layout/fragment_profile.xml`
- `res/layout/fragment_change_password.xml`
- `res/layout/fragment_force_password_change.xml`
- `res/layout/fragment_user_management.xml`
- `res/layout/fragment_access_denied.xml`
- `res/layout/item_user.xml`
- Actualizar `res/navigation/nav_graph.xml` — destinos de auth

**Strings:** Agregar todos los strings de autenticación a `strings.xml`

#### Dependencias del Sprint: Sprint 0 completado
#### Seed Data: admin/Admin1234 del Sprint 0

#### Decisiones Técnicas
- **BCrypt:** Usar `implementation 'org.mindrot:jbcrypt:0.4'`. Método: `BCrypt.hashpw(password, BCrypt.gensalt())` y `BCrypt.checkpw()`
- **Timeout:** `MainActivity.dispatchTouchEvent()` actualiza `SessionManager.updateLastActivity()`. `onResume()` llama `checkSessionValidity()`. Si expirada → `navController.navigate(R.id.loginFragment)` con `popUpTo(R.id.nav_graph) { inclusive = true }`
- **Contraseña temporal:** Campo `is_temporary_password` en User. En `LoginViewModel`, tras login exitoso, si `isTemporaryPassword == true`, navegar a `ForcePasswordChangeFragment` en vez del dashboard
- **Foto de perfil:** `ImageUtils.saveProfileImage(context, userId, bitmap)` guarda en `getFilesDir()/images/profiles/user_{id}.jpg`. Se comprime a 80% JPEG. Validar tamaño <5MB ANTES de comprimir
- **Audit log:** `AuditLogger.log(context, userId, action, entityType, entityId, details)` inserta asíncronamente via Repository

#### Riesgos
- **Riesgo:** BCrypt es CPU-intensivo, podría bloquear UI → ejecutar en background thread via Repository con `Executors`
- **Verificar:** Login con 3 roles distintos muestra menús diferentes. Timeout funciona. Contraseña temporal fuerza cambio. Audit log registra acciones


---

# Plan Maestro — PARTE 3: Sprints 2, 3 y 4

---

### SPRINT 2 — Marcas, Categorías y Catálogo de Productos

**Duración estimada:** 4 días
**Módulos:** MO_05 Marcas, MO_04 Productos
**RF cubiertos:** RF 5.1, RF 5.2, RF 5.3, RF 5.4, RF 5.5, RF 4.1, RF 4.2, RF 4.3, RF 4.4, RF 4.5, RF 4.6, RF 4.7, RF 4.8, RF 4.9, RF 4.10, RF 4.11
**RNF cubiertos:** RNF 4.1, RNF 4.2, RNF 4.3, RNF 4.4, RNF 5.1
**UX cubiertos:** UX-A2, UX-C1, UX-C2, UX-C3

#### Objetivo
Implementar la gestión completa de marcas y el catálogo de productos incluyendo: CRUD de marcas, CRUD de categorías de productos, CRUD de productos con imágenes, escáner de código de barras, paginación con Paging 3 y bloqueo optimista.

#### Criterios de Aceptación
1. GESTOR_PRODUCTOS puede listar, ver detalle, crear y editar marcas
2. ADMIN puede eliminar marcas sin productos asociados; si tiene productos, muestra error
3. Lista de marcas paginada con Paging 3
4. ADMIN puede crear/editar/eliminar categorías de productos; no elimina si tiene productos
5. Cualquier usuario ve el catálogo de productos con nombre, precio, stock, imagen, categoría, marca
6. Catálogo paginado con Paging 3 y filtros de búsqueda
7. Cualquier usuario ve el detalle completo de un producto
8. GESTOR_PRODUCTOS puede crear producto con SKU único, precio, stock, categoría obligatoria, marca, imagen opcional
9. GESTOR_PRODUCTOS puede editar producto (precio, stock, categoría, marca, imagen, estado)
10. ADMIN puede eliminar producto sin ventas asociadas
11. GESTOR_PRODUCTOS sube imagen (JPG/JPEG/PNG/GIF/WEBP, máx 5MB) con compresión
12. GESTOR_PRODUCTOS elimina imagen de producto
13. Imágenes cargan con placeholder via Glide en <2 seg
14. Escáner de barras con cámara funciona y busca producto por SKU
15. Bloqueo optimista: si dos usuarios editan el mismo producto, el segundo recibe error

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/BrandRepository.java` — CRUD marcas + validación eliminación
- `data/repository/ProductRepository.java` — CRUD productos + categorías + bloqueo optimista + imagen
- `viewmodel/BrandViewModel.java` — lógica marcas + paginación
- `viewmodel/ProductViewModel.java` — lógica productos + filtros + paginación
- `viewmodel/ProductCategoryViewModel.java` — lógica categorías
- `ui/marcas/BrandListFragment.java` — lista marcas paginada
- `ui/marcas/BrandDetailFragment.java` — detalle marca
- `ui/marcas/BrandFormFragment.java` — crear/editar marca
- `ui/productos/ProductListFragment.java` — catálogo paginado
- `ui/productos/ProductDetailFragment.java` — detalle producto
- `ui/productos/ProductFormFragment.java` — crear/editar producto
- `ui/productos/ProductCategoryFragment.java` — gestión categorías
- `ui/productos/BarcodeScannerFragment.java` — escáner ZXing
- `ui/adapter/BrandPagingAdapter.java`
- `ui/adapter/ProductPagingAdapter.java`
- Completar `util/ImageUtils.java` — guardar/eliminar/comprimir imagen producto
- Completar `util/ValidationUtils.java` — validar SKU

**Layouts:**
- `res/layout/fragment_brand_list.xml`
- `res/layout/fragment_brand_detail.xml`
- `res/layout/fragment_brand_form.xml`
- `res/layout/fragment_product_list.xml`
- `res/layout/fragment_product_detail.xml`
- `res/layout/fragment_product_form.xml`
- `res/layout/fragment_product_category.xml`
- `res/layout/fragment_barcode_scanner.xml`
- `res/layout/item_brand.xml`
- `res/layout/item_product.xml`
- `res/layout/dialog_confirm_delete.xml`
- Actualizar `res/navigation/nav_graph.xml`

#### Dependencias del Sprint: Sprint 0 y Sprint 1 completados
#### Seed Data: Crear 3 marcas y 2 categorías de producto para pruebas; crear 5 productos de ejemplo en DatabaseSeeder

#### Decisiones Técnicas
- **Paging 3 en marcas:** `BrandDao.getAllPaged()` retorna `PagingSource<Integer, Brand>`. En ViewModel: `new Pager(new PagingConfig(20), () -> brandDao.getAllPaged()).getLiveData()`
- **Paging 3 en productos:** Igual que marcas. Para filtros, crear queries separadas en DAO con parámetros de búsqueda
- **Bloqueo optimista:** En `ProductDao`, el update debe ser: `@Query("UPDATE products SET ..., version = version + 1 WHERE id = :id AND version = :expectedVersion")` retornando `int`. Si retorna 0, lanzar error desde Repository
- **ZXing:** Usar `implementation 'com.journeyapps:zxing-android-embedded:4.3.0'`. Iniciar `IntentIntegrator` desde `BarcodeScannerFragment`. Al obtener resultado, buscar producto por SKU en BD
- **Glide:** Cargar con `Glide.with(context).load(new File(imagePath)).placeholder(R.drawable.ic_image).error(R.drawable.ic_image).into(imageView)`
- **Imagen producto:** Guardar en `getFilesDir()/images/products/product_{id}.jpg`. Comprimir a 80% JPEG
- **Eliminación con validación:** En Repository, antes de eliminar marca/categoría/producto, hacer query COUNT de registros relacionados. Si > 0, lanzar excepción con mensaje descriptivo

#### Riesgos
- **Riesgo:** Permisos de cámara denegados para escáner → manejar con resultado de `ActivityResultContracts.RequestPermission`
- **Riesgo:** Bloqueo optimista puede confundir al usuario → mostrar Dialog explicativo pidiendo recargar datos
- **Verificar:** Paginación funciona con 50+ productos. Escáner lee código y encuentra producto. Eliminación bloqueada cuando hay relaciones

---

### SPRINT 3 — Clientes y Descuentos

**Duración estimada:** 4 días
**Módulos:** MO_03 Clientes, MO_06 Descuentos
**RF cubiertos:** RF 3.1, RF 3.2, RF 3.3, RF 3.4, RF 3.5, RF 3.6, RF 3.8, RF 3.9, RF 6.1, RF 6.2, RF 6.3, RF 6.4, RF 6.5
**RNF cubiertos:** RNF 3.1, RNF 3.2, RNF 3.3, RNF 6.1
**UX cubiertos:** UX-A2, UX-A3

#### Objetivo
Implementar la gestión completa de clientes (registro, edición, categorías, estados, métricas, historial) y la gestión de códigos de descuento (CRUD, vigencia, activación).

#### Criterios de Aceptación
1. VENDEDOR registra cliente con datos personales obligatorios, datos fiscales opcionales, categoría y notas
2. VENDEDOR consulta lista clientes con filtros (texto, fecha, categoría, estado) y paginación
3. VENDEDOR edita datos de cliente sin alterar historial de ventas
4. ADMIN cambia estado cliente (activo/inactivo); inactivos no se seleccionan en ventas
5. ADMIN gestiona categorías de clientes (crear, editar, eliminar)
6. ADMIN ve estadísticas: total clientes, frecuentes, ventas totales, ticket promedio
7. ADMIN ve perfil detallado: datos, métricas, historial compras
8. ADMIN elimina cliente sin compras; si tiene compras, solo desactiva
9. Validación de RFC con regex mexicano
10. ADMIN lista descuentos con código, porcentaje, expiración, vigencia
11. ADMIN ve detalle de descuento
12. ADMIN crea descuento con código alfanumérico único, porcentaje válido, fecha expiración futura
13. ADMIN edita porcentaje y fecha expiración de descuento
14. ADMIN activa/desactiva descuento
15. Lista descuentos paginada

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/ClientRepository.java` — CRUD + filtros + métricas + historial
- `data/repository/DiscountRepository.java` — CRUD + validación vigencia
- `viewmodel/ClientViewModel.java` — lógica clientes + paginación + filtros
- `viewmodel/ClientCategoryViewModel.java` — categorías clientes
- `viewmodel/DiscountViewModel.java` — lógica descuentos + paginación
- `ui/clientes/ClientListFragment.java`
- `ui/clientes/ClientFormFragment.java`
- `ui/clientes/ClientDetailFragment.java`
- `ui/clientes/ClientStatsFragment.java`
- `ui/clientes/ClientCategoryFragment.java`
- `ui/descuentos/DiscountListFragment.java`
- `ui/descuentos/DiscountDetailFragment.java`
- `ui/descuentos/DiscountFormFragment.java`
- `ui/adapter/ClientPagingAdapter.java`
- `ui/adapter/DiscountPagingAdapter.java`
- Completar `util/ValidationUtils.java` — validar RFC, email, teléfono

**Layouts:**
- `res/layout/fragment_client_list.xml`
- `res/layout/fragment_client_form.xml`
- `res/layout/fragment_client_detail.xml`
- `res/layout/fragment_client_stats.xml`
- `res/layout/fragment_client_category.xml`
- `res/layout/fragment_discount_list.xml`
- `res/layout/fragment_discount_detail.xml`
- `res/layout/fragment_discount_form.xml`
- `res/layout/item_client.xml`
- `res/layout/item_discount.xml`
- Actualizar `res/navigation/nav_graph.xml`

#### Dependencias del Sprint: Sprints 0, 1 completados. Sprint 2 es recomendado pero no bloqueante
#### Seed Data: 5 clientes de ejemplo, 3 categorías, 3 códigos de descuento (1 activo vigente, 1 activo expirado, 1 inactivo)

#### Decisiones Técnicas
- **Filtros combinados clientes:** En `ClientDao`, usar `@RawQuery` con `SupportSQLiteQuery` construido dinámicamente en Repository para combinar filtros de texto, fecha, categoría y estado
- **Métricas cliente:** Query en DAO: `@Query("SELECT COUNT(*) FROM sales WHERE client_id = :clientId")` y `SUM(total)`. Calcular ticket promedio en ViewModel
- **RFC México regex:** `^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$` — validar en `ValidationUtils`, aplicar en `ClientFormFragment` antes de guardar
- **Email regex:** Usar `android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()`
- **Teléfono:** Exactamente 10 dígitos numéricos: `^\\d{10}$`
- **Descuento vigencia:** Un descuento es "vigente" si `is_active == 1 AND expiration_date > System.currentTimeMillis()`. Esta lógica se implementa en `DiscountRepository.validateDiscountCode(code)`
- **Descuento código:** Alfanumérico 4-20 chars: `^[a-zA-Z0-9]{4,20}$`. Porcentaje: `0 < percentage <= 100`. Fecha expiración: debe ser futura
- **Integridad historial (RNF 3.3):** Las ventas almacenan `client_id` como FK pero también tienen precio unitario copiado. Editar datos del cliente NO afecta ventas ya registradas

#### Riesgos
- **Riesgo:** Filtros combinados con RawQuery pueden generar SQL injection → usar `SimpleSQLiteQuery` con argumentos bind
- **Verificar:** Filtros combinados funcionan. RFC inválido es rechazado. Descuento expirado no se puede usar. Eliminar cliente con compras solo desactiva

---

### SPRINT 4 — Ventas: Carrito, Pago y Registro

**Duración estimada:** 5 días
**Módulos:** MO_02 Ventas (parte core)
**RF cubiertos:** RF 2.1, RF 2.2, RF 2.3, RF 2.4, RF 2.5, RF 2.9, RF 2.10
**RNF cubiertos:** RNF 2.1, RNF 2.2
**UX cubiertos:** UX-A2, UX-A3

#### Objetivo
Implementar el flujo completo de venta: carrito de compras en memoria, selección de productos (manual y por escáner), precio manual con validación del 50%, aplicación de descuento, registro de pago con método y monto, descuento automático de stock, lista de ventas con filtros y detalle.

#### Criterios de Aceptación
1. VENDEDOR gestiona carrito: agregar productos (por búsqueda o escáner), modificar cantidad, eliminar items
2. VENDEDOR puede especificar precio manual por artículo; sistema valida que no sea <50% del precio catálogo
3. Carrito muestra subtotal actualizado en tiempo real
4. VENDEDOR ingresa código de descuento; sistema valida vigencia y aplica porcentaje
5. VENDEDOR registra pago: método (Efectivo/Tarjeta/Transferencia), monto recibido, cambio calculado
6. Al marcar venta como 'Pagada', el stock se descuenta atómicamente via @Transaction
7. ADMIN lista ventas con filtros (texto, estado) ordenadas por fecha desc, paginadas
8. ADMIN ve detalle completo de venta (cliente, artículos, cantidades, precios, descuento, total, estado)
9. La consulta de ventas responde en <2 seg
10. Se genera evento NEW_SALE en system_events al crear una venta (preparación para MO_08)

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/SaleRepository.java` — crear venta con items, descuento stock atómico, filtros
- `viewmodel/SaleViewModel.java` — carrito en memoria (List<CartItem>), validaciones, paginación
- `ui/ventas/SaleCartFragment.java` — pantalla carrito
- `ui/ventas/SalePaymentFragment.java` — registro pago
- `ui/ventas/SaleListFragment.java` — lista ventas
- `ui/ventas/SaleDetailFragment.java` — detalle venta
- `ui/adapter/CartItemAdapter.java` — adapter carrito
- `ui/adapter/SalePagingAdapter.java` — adapter lista ventas
- `ui/adapter/SaleItemAdapter.java` — adapter items del detalle
- Modificar `data/repository/SystemEventRepository.java` — insertar evento NEW_SALE
- Agregar `res/layout/dialog_discount_code.xml` — diálogo para ingresar código

**Layouts:**
- `res/layout/fragment_sale_cart.xml`
- `res/layout/fragment_sale_payment.xml`
- `res/layout/fragment_sale_list.xml`
- `res/layout/fragment_sale_detail.xml`
- `res/layout/item_cart_item.xml`
- `res/layout/item_sale.xml`
- `res/layout/item_sale_item.xml`
- `res/layout/dialog_discount_code.xml`
- Actualizar `res/navigation/nav_graph.xml`

#### Dependencias del Sprint: Sprints 0, 1, 2 y 3 completados
#### Seed Data: Productos del Sprint 2 con stock > 0; clientes del Sprint 3; descuento activo vigente del Sprint 3

#### Decisiones Técnicas
- **Carrito en memoria:** `SaleViewModel` mantiene `MutableLiveData<List<CartItem>>` donde `CartItem` es una clase POJO (no entity) con: `productId, productName, quantity, unitPrice, catalogPrice`. NO se persiste en BD hasta el pago
- **Validación precio manual (RF 2.2):** En ViewModel, al cambiar precio: `if (manualPrice < catalogPrice * 0.5) { error("El precio no puede ser inferior al 50% del catálogo") }`. El 50% se define en `Constants.MIN_PRICE_PERCENTAGE = 0.5`
- **Descuento atómico de stock (RF 2.9):** En `SaleRepository`, método `@Transaction` que: (1) inserta Sale, (2) inserta todos SaleItems, (3) para cada item ejecuta `UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty`, (4) si algún UPDATE retorna 0 filas, lanza `InsufficientStockException`, (5) inserta en sale_status_history, (6) inserta en system_events. Todo dentro de `@Transaction`
- **Método de pago:** Enum `PaymentMethod { EFECTIVO, TARJETA, TRANSFERENCIA }`. Si EFECTIVO, validar que monto_recibido >= total. Cambio = monto_recibido - total. Si TARJETA o TRANSFERENCIA, monto_recibido = total automáticamente, cambio = 0
- **Paginación ventas:** `SaleDao.getAllPaged(status, searchQuery)` con `PagingSource`. Filtrar por estado y texto de búsqueda (nombre cliente o ID venta)

#### Riesgos
- **Riesgo:** Stock insuficiente durante @Transaction → lanzar excepción específica, mostrar Dialog indicando qué producto no tiene stock
- **Riesgo:** El carrito se pierde si el proceso se mata → aceptable por diseño (es un POS, las ventas son inmediatas). Documentar en UX
- **Verificar:** Crear venta completa con 3 productos, descuento, pago efectivo. Verificar stock decrementado. Verificar cambio calculado. Verificar en lista de ventas


---

# Plan Maestro — PARTE 4: Sprints 5, 6 y 7

---

### SPRINT 5 — Ventas Avanzado: Estados, Caja, PDF e Impresión

**Duración estimada:** 5 días
**Módulos:** MO_02 Ventas (parte avanzada)
**RF cubiertos:** RF 2.6, RF 2.7, RF 2.8, RF 2.11, RF 2.12, RF 2.13
**RNF cubiertos:** Ninguno adicional
**UX cubiertos:** UX-A3

#### Objetivo
Completar el módulo de Ventas con: gestión de estados (transiciones válidas con historial auditado), apertura/cierre de caja con arqueo, generación de comprobante PDF e impresión Bluetooth.

#### Criterios de Aceptación
1. ADMIN actualiza estado de venta respetando transiciones válidas: Pendiente→Pagada, Pendiente→Cancelada, Pagada→Completada, Pagada→Cancelada
2. Cada cambio de estado se registra en sale_status_history con estado anterior, nuevo, fecha y usuario
3. ADMIN consulta historial completo de cambios de estado de una venta
4. VENDEDOR realiza Apertura de Caja registrando monto base en efectivo
5. VENDEDOR realiza Cierre de Caja ingresando efectivo físico contado
6. Sistema genera reporte de Arqueo: monto esperado (base + ventas efectivo del turno) vs monto físico, diferencia
7. Diferencia (sobrante/faltante) se notifica al ADMIN (registro en system_events)
8. Se genera comprobante PDF con datos de la venta
9. El PDF se puede compartir (Intent.ACTION_SEND) o imprimir por Bluetooth

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/CashRegisterRepository.java` — apertura, cierre, cálculo arqueo
- `viewmodel/CashRegisterViewModel.java` — lógica caja
- `ui/ventas/CashOpenFragment.java`
- `ui/ventas/CashCloseFragment.java`
- `ui/ventas/CashReportFragment.java`
- `ui/ventas/SaleStatusHistoryFragment.java`
- `util/PdfGenerator.java` — generar PDF comprobante con iTextPDF
- `util/BluetoothPrintHelper.java` — impresión Bluetooth
- Modificar `SaleRepository.java` — transiciones de estado, historial
- Modificar `SaleViewModel.java` — cambio estado, historial
- Modificar `SaleDetailFragment.java` — botones de cambio estado + botón imprimir/compartir PDF
- Modificar `SystemEventRepository.java` — evento diferencia caja

**Layouts:**
- `res/layout/fragment_cash_open.xml`
- `res/layout/fragment_cash_close.xml`
- `res/layout/fragment_cash_report.xml`
- `res/layout/fragment_sale_status_history.xml`
- Actualizar `res/navigation/nav_graph.xml`

#### Dependencias del Sprint: Sprints 0-4 completados
#### Seed Data: Ventas creadas en Sprint 4 para probar cambios de estado

#### Decisiones Técnicas
- **Transiciones de estado válidas:** Definir mapa en `Constants.java`:
  - PENDIENTE → {PAGADA, CANCELADA}
  - PAGADA → {COMPLETADA, CANCELADA}
  - COMPLETADA → {} (estado final)
  - CANCELADA → {} (estado final)
  - En `SaleRepository.updateStatus()`, validar transición antes de ejecutar. Si inválida, lanzar `InvalidStateTransitionException`
- **Historial auditado:** Dentro de `@Transaction` en `SaleRepository.updateStatus()`: (1) obtener estado actual, (2) validar transición, (3) actualizar sale.status, (4) insertar SaleStatusHistory, (5) registrar en audit_log
- **Apertura de caja:** `CashRegisterRepository.openCashRegister(sellerId, baseAmount)`. Validar que el vendedor no tenga otra caja abierta (closed_at == null). Si tiene, lanzar error
- **Cierre de caja:** `CashRegisterRepository.closeCashRegister(cashRegisterId, physicalAmount)`. Calcular expected_amount = base_amount + SUM(sales.total WHERE payment_method='EFECTIVO' AND cash_register_id=:id AND status IN ('PAGADA','COMPLETADA')). difference = physical_amount - expected_amount
- **Diferencia de caja:** Si difference != 0, insertar SystemEvent con type='CASH_DIFFERENCE' para que el ADMIN lo vea en el panel (Sprint 7)
- **PDF con iTextPDF:** `PdfGenerator.generateSaleReceipt(context, sale, saleItems, client)` genera PDF en `getExternalFilesDir()/receipts/venta_{id}.pdf`. Layout: encabezado "SwiftPay", datos venta, tabla items, totales, pie
- **Compartir PDF:** Usar `FileProvider` + `Intent.ACTION_SEND` con type `application/pdf`
- **Impresión Bluetooth:** `BluetoothPrintHelper.printPdf(context, filePath)`. Buscar dispositivos emparejados, conectar por socket, enviar bytes ESC/POS. Si no hay impresora, mostrar Toast sugiriendo compartir PDF. Este es un feature best-effort

#### Riesgos
- **Riesgo:** Impresión Bluetooth depende de hardware → implementar como feature opcional con fallback a compartir PDF
- **Riesgo:** FileProvider mal configurado → declarar en AndroidManifest.xml con `file_paths.xml`
- **Verificar:** Cambiar estado de venta y ver historial completo. Abrir caja, hacer ventas, cerrar caja y ver reporte de arqueo. Generar PDF y compartirlo

---

### SPRINT 6 — Proveedores y Órdenes de Compra

**Duración estimada:** 4 días
**Módulos:** MO_07 Proveedores y Órdenes de Compra
**RF cubiertos:** RF 7.1, RF 7.2, RF 7.3, RF 7.4, RF 7.5, RF 7.6, RF 7.7, RF 7.8, RF 7.9, RF 7.10
**RNF cubiertos:** RNF 7.1, RNF 7.2, RNF 7.3
**UX cubiertos:** UX-A2, UX-A3

#### Objetivo
Implementar la gestión completa de proveedores (CRUD) y el ciclo de vida de órdenes de compra: creación con selección de productos y cantidades, transiciones de estado, y recepción con actualización atómica de stock.

#### Criterios de Aceptación
1. GESTOR_PRODUCTOS lista proveedores en <2 seg
2. GESTOR_PRODUCTOS ve detalle proveedor (contacto, RFC, fecha registro)
3. GESTOR_PRODUCTOS registra proveedor con datos contacto, RFC, notas
4. GESTOR_PRODUCTOS edita proveedor
5. GESTOR_PRODUCTOS lista órdenes de compra ordenadas por fecha desc, mostrando proveedor, estado, total, fecha
6. GESTOR_PRODUCTOS ve detalle orden (proveedor, estado, total, fecha recepción, desglose productos con cantidades y costos)
7. GESTOR_PRODUCTOS crea orden: selecciona proveedor, agrega productos con cantidades y costos; total se calcula automáticamente
8. Sistema gestiona estados: Pendiente→Recibida, Pendiente→Cancelada
9. GESTOR_PRODUCTOS edita orden en estado Pendiente (proveedor y detalle items)
10. GESTOR_PRODUCTOS registra recepción → stock se actualiza atómicamente con cantidades recibidas
11. RFC del proveedor validado con regex mexicano
12. Consistencia de inventario garantizada al recibir orden

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/SupplierRepository.java` — CRUD proveedores
- `data/repository/PurchaseOrderRepository.java` — CRUD órdenes + recepción + stock atómico
- `viewmodel/SupplierViewModel.java`
- `viewmodel/PurchaseOrderViewModel.java` — items en memoria durante creación, recepción
- `ui/proveedores/SupplierListFragment.java`
- `ui/proveedores/SupplierDetailFragment.java`
- `ui/proveedores/SupplierFormFragment.java`
- `ui/proveedores/PurchaseOrderListFragment.java`
- `ui/proveedores/PurchaseOrderDetailFragment.java`
- `ui/proveedores/PurchaseOrderFormFragment.java`
- `ui/proveedores/PurchaseOrderReceiveFragment.java`
- `ui/adapter/SupplierAdapter.java`
- `ui/adapter/PurchaseOrderAdapter.java`

**Layouts:**
- `res/layout/fragment_supplier_list.xml`
- `res/layout/fragment_supplier_detail.xml`
- `res/layout/fragment_supplier_form.xml`
- `res/layout/fragment_purchase_order_list.xml`
- `res/layout/fragment_purchase_order_detail.xml`
- `res/layout/fragment_purchase_order_form.xml`
- `res/layout/fragment_purchase_order_receive.xml`
- `res/layout/item_supplier.xml`
- `res/layout/item_purchase_order.xml`
- Actualizar `res/navigation/nav_graph.xml`

#### Dependencias del Sprint: Sprints 0-2 completados (necesita productos). Sprints 3-5 recomendados
#### Seed Data: 3 proveedores, 2 órdenes de compra (1 pendiente, 1 recibida) con items

#### Decisiones Técnicas
- **Recepción atómica de stock (RF 7.10):** En `PurchaseOrderRepository.receiveOrder(orderId)`, método con `@Transaction`: (1) obtener orden y verificar status == PENDIENTE, (2) para cada PurchaseOrderItem: `UPDATE products SET stock = stock + :quantity WHERE id = :productId`, (3) actualizar orden: status='RECIBIDA', received_at=now, (4) registrar en audit_log. Todo atómico
- **Creación de orden:** `PurchaseOrderViewModel` mantiene `MutableLiveData<List<OrderItemDraft>>` en memoria (POJO con productId, productName, quantity, unitCost). Al confirmar, Repository inserta order + items en @Transaction. Total = SUM(quantity * unitCost)
- **Edición de orden Pendiente:** Solo si status=='PENDIENTE'. Cargar items existentes en el ViewModel, permitir modificar, guardar con @Transaction (DELETE items antiguos + INSERT nuevos)
- **Transiciones de estado:** Pendiente→Recibida (solo via recepción), Pendiente→Cancelada (manual). Recibida y Cancelada son estados finales
- **Lista órdenes:** `PurchaseOrderDao.getAllOrderedByDateDesc()` retorna `LiveData<List<PurchaseOrderWithSupplier>>` usando `@Relation` o query JOIN

#### Riesgos
- **Riesgo:** Recibir orden con producto eliminado → validar que todos los productos del detalle existen antes de actualizar stock
- **Verificar:** Crear orden con 3 productos, recibirla, verificar que el stock de cada producto aumentó correctamente

---

### SPRINT 7 — Panel de Control y Notificaciones

**Duración estimada:** 3 días
**Módulos:** MO_08 Notificaciones y Panel de Control
**RF cubiertos:** RF 8.1, RF 8.2, RF 8.3, RF 8.4, RF 8.5
**RNF cubiertos:** RNF 8.1, RNF 8.2
**UX cubiertos:** UX-D1, UX-D2, UX-D3, UX-D4, UX-D5, UX-D6

#### Objetivo
Implementar el panel de control del ADMIN con indicadores de nuevos clientes y ventas, lista de eventos no revisados, marcado de eventos como revisados, generación automática de eventos, y sistema de notificaciones con alarmas, sonidos y vibración.

#### Criterios de Aceptación
1. ADMIN ve panel con contadores: nuevos clientes y nuevas ventas desde última revisión
2. ADMIN ve lista de eventos no revisados
3. ADMIN marca eventos como revisados (individual o masivo), registra fecha/hora
4. Sistema genera eventos automáticamente al registrar clientes o ventas (ya preparado en Sprints 3-4)
5. Contadores se actualizan en <2 seg tras nuevo evento
6. Número de eventos pendientes visible en panel
7. Notificaciones push con vibración y sonido para alertas críticas
8. Usuario selecciona tono de notificación desde configuración
9. Usuario programa alarmas recurrentes (diaria/semanal/personalizada)
10. Notificaciones incluyen ícono e imagen de contexto
11. Usuario puede silenciar/pausar alarmas desde la notificación
12. Modal de alerta en pantalla completa cuando la app está abierta y llega alarma activa

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/SystemEventRepository.java` — completar: listar no revisados, marcar revisados, contadores
- `viewmodel/DashboardViewModel.java` — contadores, eventos, LiveData reactivo
- `ui/dashboard/DashboardFragment.java` — panel con cards de indicadores
- `ui/dashboard/EventListFragment.java` — lista eventos con opción marcar revisado
- `ui/adapter/EventAdapter.java`
- `util/NotificationHelper.java` — crear canal, notificaciones, vibración, sonido
- Crear `AlarmReceiver.java` en paquete raíz — BroadcastReceiver para alarmas
- Modificar `SettingsFragment.java` — selección tono, programar alarmas
- Modificar `SettingsViewModel.java` — preferencias notificación

**Layouts:**
- `res/layout/fragment_dashboard.xml` — cards con contadores + accesos rápidos
- `res/layout/fragment_event_list.xml`
- `res/layout/item_event.xml`
- `res/layout/dialog_full_screen_alert.xml` — modal alerta pantalla completa
- Actualizar `res/navigation/nav_graph.xml`

**Recursos adicionales:**
- `res/raw/notification_sound_1.mp3`, `notification_sound_2.mp3`, `notification_sound_3.mp3` — 3 tonos predefinidos
- Agregar strings de notificaciones a `strings.xml`

#### Dependencias del Sprint: Sprints 0-4 completados (necesita eventos de clientes y ventas)
#### Seed Data: Eventos generados automáticamente por operaciones de Sprints 3-4

#### Decisiones Técnicas
- **Contadores en tiempo real (RNF 8.1, 8.2):** `SystemEventDao.getUnreviewedCountByType(eventType)` retorna `LiveData<Integer>`. El DashboardFragment observa estos LiveData. Room actualiza automáticamente cuando la tabla cambia
- **Marcar revisado:** `SystemEventDao.markAsReviewed(eventId, reviewedAt)`. Para marcar todos: `@Query("UPDATE system_events SET is_reviewed = 1, reviewed_at = :now WHERE is_reviewed = 0")`
- **Notificaciones Android:** Crear `NotificationChannel` con id `swiftpay_alerts` en `SwiftPayApplication.onCreate()`. Usar `NotificationCompat.Builder` con `setPriority(PRIORITY_HIGH)`, `setVibrate(new long[]{0, 500, 200, 500})`, `setSound(selectedToneUri)`
- **Tono seleccionable:** Almacenar en `user_preferences.notification_sound`. Los tonos están en `res/raw/`. Usar `RingtoneManager` para convertir a URI
- **Alarmas recurrentes:** Usar `AlarmManager.setRepeating()` o `setExactAndAllowWhileIdle()` para alarmas. `AlarmReceiver` extiende `BroadcastReceiver`, genera notificación al recibir broadcast
- **Alarma pantalla completa (UX-D6):** Cuando la app está en foreground y llega una alarma, mostrar `DialogFragment` con layout fullscreen en vez de notificación. Detectar foreground via `ProcessLifecycleOwner`
- **Silenciar alarma (UX-D5):** Agregar acción en notificación con `PendingIntent` que cancela la alarma via `AlarmManager.cancel()`

#### Riesgos
- **Riesgo:** AlarmManager puede no ser exacto en Android 12+ con restricciones de batería → usar `setExactAndAllowWhileIdle()` y documentar limitación
- **Riesgo:** Notificaciones bloqueadas si el usuario deniega permisos (Android 13+) → solicitar permiso `POST_NOTIFICATIONS` en runtime
- **Verificar:** Crear un cliente y una venta, verificar que los contadores del dashboard incrementan. Marcar como revisados y verificar que bajan. Probar notificación con sonido y vibración


---

# Plan Maestro — PARTE 5: Sprint 8, Cobertura, Dependencias, Instrucciones y Resumen

---

### SPRINT 8 — Configuración, UX, Accesibilidad y Pulido Final

**Duración estimada:** 4 días
**Módulos:** Transversal (todos)
**RF cubiertos:** Ninguno adicional (todos cubiertos en Sprints 1-7)
**RNF cubiertos:** Ninguno adicional
**UX cubiertos:** UX-B7, UX-C5, UX-E1, UX-E2, UX-E3, UX-E4, UX-F1, UX-F2

#### Objetivo
Implementar la pantalla de Configuración completa con: alternancia tema claro/oscuro, selección de esquema de color, tamaño de fuente ajustable, modo accesibilidad, vista compacta/expandida, fondo personalizado, opción de desactivar carga de imágenes, y desactivar animaciones. Pulido general de la UI.

#### Criterios de Aceptación
1. Usuario alterna entre tema claro y oscuro manualmente o según preferencia del sistema
2. Usuario selecciona entre 3-5 esquemas de color predefinidos
3. Usuario ajusta tamaño de fuente (pequeño/normal/grande)
4. Usuario activa modo accesibilidad (botones grandes, espaciado extra)
5. Usuario elige vista compacta (íconos pequeños) o expandida
6. Usuario establece imagen o color de fondo personalizado
7. Usuario desactiva carga de imágenes para ahorrar datos
8. Usuario activa/desactiva animaciones de transición
9. Todas las preferencias se persisten en user_preferences y se aplican al reiniciar
10. Contraste mínimo 4.5:1 WCAG AA verificado en todas las combinaciones de tema/color

#### Archivos a Crear/Modificar

**Java:**
- `data/repository/UserPreferencesRepository.java` — CRUD preferencias
- `viewmodel/SettingsViewModel.java` — completar lógica preferencias
- `ui/settings/SettingsFragment.java` — pantalla configuración completa
- Modificar `SwiftPayApplication.java` — aplicar tema/fuente al iniciar
- Modificar `MainActivity.java` — aplicar preferencias de vista en runtime
- Crear `util/ThemeManager.java` — aplicar tema, esquema color, fuente globalmente

**Layouts:**
- `res/layout/fragment_settings.xml` — formulario completo de configuración
- Actualizar `res/navigation/nav_graph.xml`

**Recursos adicionales:**
- `res/values/colors_scheme_2.xml` a `colors_scheme_5.xml` — esquemas de color alternativos (definir 4 esquemas adicionales)
- Actualizar `res/values/styles.xml` — estilos de texto para tamaños small/large
- Actualizar `res/values/dimens.xml` — dimensiones alternativas para modo accesibilidad

#### Dependencias del Sprint: Sprints 0-1 completados. Se aplica sobre toda la app
#### Seed Data: Ninguno adicional

#### Decisiones Técnicas
- **Tema claro/oscuro (UX-B5, UX-E1):** Usar `AppCompatDelegate.setDefaultNightMode()`. Opciones: `MODE_NIGHT_NO` (claro), `MODE_NIGHT_YES` (oscuro), `MODE_NIGHT_FOLLOW_SYSTEM`. Persistir elección en `user_preferences.theme_mode`
- **Esquemas de color (UX-B7):** Definir 5 esquemas con colores primarios/acento diferentes. En `ThemeManager.applyColorScheme()`, cambiar dinámicamente los colores del tema usando `getTheme().applyStyle()`. Esquemas:
  1. DEFAULT: Primary #1565C0, Accent #FF6F00 (el del documento)
  2. EMERALD: Primary #2E7D32, Accent #FF6F00
  3. PURPLE: Primary #6A1B9A, Accent #FFB300
  4. CORAL: Primary #D84315, Accent #1565C0
  5. TEAL: Primary #00695C, Accent #FF6F00
- **Tamaño fuente (UX-F1):** Definir 3 escalas en `dimens.xml`: `font_scale_small = 0.85`, `font_scale_normal = 1.0`, `font_scale_large = 1.3`. Aplicar con `Configuration.fontScale` en `attachBaseContext()` de `MainActivity`
- **Modo accesibilidad (UX-F2):** Cuando activado: botones height = 56dp (en vez de 48dp), padding entre elementos = 16dp (en vez de 8dp), touch targets = 56dp mínimo. Aplicar dinámicamente con Resources override
- **Vista compacta/expandida (UX-E2):** En adaptadores de listas, verificar `userPreferences.compactView`. Si compacto, usar item height 48dp y ocultar textos secundarios. Si expandido, usar item height 62dp con texto principal + secundario
- **Fondo personalizado (UX-E3):** Seleccionar imagen de galería o color sólido. Guardar en `getFilesDir()/images/wallpaper.jpg`. Aplicar como background del NavHostFragment
- **Desactivar imágenes (UX-C5):** Bandera `userPreferences.imagesEnabled`. Cuando false, todos los `Glide.load()` se reemplazan por placeholder estático. Implementar wrapper `ImageLoader.java` en util/ que verifica esta preferencia
- **Desactivar animaciones (UX-E4):** Cuando desactivado, usar `navOptions.setEnterAnim(0).setExitAnim(0)` y deshabilitar RecyclerView itemAnimator

#### Riesgos
- **Riesgo:** Cambiar tema en runtime puede causar recreación de Activity y pérdida de estado → guardar estado en ViewModel (que sobrevive a recreación)
- **Verificar:** Cambiar cada preferencia y verificar que se aplica inmediatamente y persiste tras cerrar y abrir la app

---

## 4. Tabla Resumen de Cobertura de Requisitos

### Requisitos Funcionales

| ID | Descripción breve | Sprint | Notas |
|----|-------------------|--------|-------|
| RF 1.1 | Pantalla de login | Sprint 1 | |
| RF 1.2 | Validar credenciales y mostrar panel por rol | Sprint 1 | |
| RF 1.3 | Asignar rol único a cada usuario | Sprint 1 | Enum UserRole |
| RF 1.4 | Restringir acceso según rol | Sprint 1 | Menú dinámico + verificación en Fragment |
| RF 1.5 | Timeout 30 min inactividad | Sprint 1 | dispatchTouchEvent + onResume check |
| RF 1.6 | Botón cerrar sesión | Sprint 1 | |
| RF 1.7 | Admin crea usuarios | Sprint 1 | |
| RF 1.8 | Admin desactiva/reactiva cuentas | Sprint 1 | |
| RF 1.9 | Admin restablece contraseña temporal | Sprint 1 | |
| RF 1.10 | Usuario cambia su contraseña | Sprint 1 | |
| RF 1.11 | Usuario consulta perfil | Sprint 1 | |
| RF 1.12 | Usuario sube/actualiza foto perfil | Sprint 1 | JPG/JPEG/PNG, máx 5MB |
| RF 2.1 | Vendedor registra pago de venta | Sprint 4 | |
| RF 2.2 | Precio unitario manual ≥50% catálogo | Sprint 4 | Validación en ViewModel |
| RF 2.3 | Código de descuento en venta | Sprint 4 | Validar vigencia |
| RF 2.4 | Admin consulta listado ventas con filtros | Sprint 4 | Paging 3 |
| RF 2.5 | Admin consulta detalle de venta | Sprint 4 | |
| RF 2.6 | Admin actualiza estado de venta | Sprint 5 | Transiciones válidas |
| RF 2.7 | Admin consulta historial de estados | Sprint 5 | sale_status_history |
| RF 2.8 | Comprobante PDF + impresión Bluetooth | Sprint 5 | iTextPDF + BluetoothSocket |
| RF 2.9 | Descuento automático de stock al pagar | Sprint 4 | @Transaction atómico |
| RF 2.10 | Carrito de compras | Sprint 4 | En memoria ViewModel |
| RF 2.11 | Apertura de caja | Sprint 5 | |
| RF 2.12 | Cierre de caja | Sprint 5 | |
| RF 2.13 | Reporte arqueo de caja | Sprint 5 | |
| RF 3.1 | Vendedor registra cliente | Sprint 3 | |
| RF 3.2 | Vendedor consulta clientes con filtros | Sprint 3 | Paging 3 |
| RF 3.3 | Vendedor edita datos cliente | Sprint 3 | |
| RF 3.4 | Admin cambia estado cliente | Sprint 3 | |
| RF 3.5 | Admin gestiona categorías clientes | Sprint 3 | |
| RF 3.6 | Admin estadísticas clientes | Sprint 3 | |
| RF 3.8 | Admin perfil detallado cliente | Sprint 3 | |
| RF 3.9 | Admin elimina cliente sin compras | Sprint 3 | |
| RF 4.1 | Consultar catálogo productos | Sprint 2 | |
| RF 4.2 | Consultar detalle producto | Sprint 2 | |
| RF 4.3 | Gestor registra producto | Sprint 2 | SKU único |
| RF 4.4 | Escáner código barras con cámara | Sprint 2 | ZXing |
| RF 4.5 | Gestor modifica producto | Sprint 2 | |
| RF 4.6 | Admin elimina producto sin ventas | Sprint 2 | |
| RF 4.7 | Consultar categorías productos | Sprint 2 | |
| RF 4.8 | Admin crea categoría producto | Sprint 2 | |
| RF 4.9 | Admin modifica/elimina categoría | Sprint 2 | |
| RF 4.10 | Gestor carga imagen producto | Sprint 2 | Glide + compresión |
| RF 4.11 | Gestor elimina imagen producto | Sprint 2 | |
| RF 5.1 | Gestor consulta marcas | Sprint 2 | Paging 3 |
| RF 5.2 | Gestor detalle marca | Sprint 2 | |
| RF 5.3 | Gestor crea marca | Sprint 2 | |
| RF 5.4 | Gestor modifica marca | Sprint 2 | |
| RF 5.5 | Admin elimina marca sin productos | Sprint 2 | |
| RF 6.1 | Admin lista descuentos | Sprint 3 | Paging 3 |
| RF 6.2 | Admin detalle descuento | Sprint 3 | |
| RF 6.3 | Admin crea descuento | Sprint 3 | |
| RF 6.4 | Admin modifica descuento | Sprint 3 | |
| RF 6.5 | Admin activa/desactiva descuento | Sprint 3 | |
| RF 7.1 | Gestor lista proveedores | Sprint 6 | |
| RF 7.2 | Gestor detalle proveedor | Sprint 6 | |
| RF 7.3 | Gestor registra proveedor | Sprint 6 | |
| RF 7.4 | Gestor modifica proveedor | Sprint 6 | |
| RF 7.5 | Gestor lista órdenes compra | Sprint 6 | |
| RF 7.6 | Gestor detalle orden compra | Sprint 6 | |
| RF 7.7 | Gestor crea orden compra | Sprint 6 | |
| RF 7.8 | Gestionar estados orden compra | Sprint 6 | |
| RF 7.9 | Gestor modifica orden pendiente | Sprint 6 | |
| RF 7.10 | Recepción + actualización stock | Sprint 6 | @Transaction atómico |
| RF 8.1 | Panel con contadores | Sprint 7 | LiveData reactivo |
| RF 8.2 | Lista eventos no revisados | Sprint 7 | |
| RF 8.3 | Marcar eventos revisados | Sprint 7 | |
| RF 8.4 | Generación automática eventos | Sprint 7 | Insertados desde Sprints 3-4 |
| RF 8.5 | Total eventos pendientes en panel | Sprint 7 | |

### Requisitos No Funcionales

| ID | Descripción breve | Sprint | Notas |
|----|-------------------|--------|-------|
| RNF 1.1 | Autenticación en <2 seg | Sprint 1 | BCrypt en background thread |
| RNF 1.2 | Hash seguro contraseñas | Sprint 1 | BCrypt |
| RNF 1.3 | Contraseña mín 8 chars, letras+números | Sprint 1 | ValidationUtils |
| RNF 1.4 | Expiración sesión 30 min | Sprint 1 | SessionManager |
| RNF 1.5 | Log auditoría login/logout/cambio pwd | Sprint 1 | AuditLogger |
| RNF 2.1 | Consulta ventas <2 seg | Sprint 4 | Paging 3 + índices |
| RNF 2.2 | Tabla ventas con paginación | Sprint 4 | Paging 3 |
| RNF 3.1 | Consulta clientes <2 seg | Sprint 3 | |
| RNF 3.2 | Lista clientes con paginación | Sprint 3 | Paging 3 |
| RNF 3.3 | Integridad historial compras | Sprint 3 | FK + precio copiado en sale_items |
| RNF 4.1 | Catálogo en <2 seg | Sprint 2 | |
| RNF 4.2 | Paginación y ordenamiento catálogo | Sprint 2 | Paging 3 |
| RNF 4.3 | Imágenes disponibles en <2 seg | Sprint 2 | Glide cache |
| RNF 4.4 | Bloqueo optimista productos | Sprint 2 | Campo version |
| RNF 5.1 | Paginación marcas | Sprint 2 | Paging 3 |
| RNF 6.1 | Paginación descuentos | Sprint 3 | Paging 3 |
| RNF 7.1 | Lista proveedores <2 seg | Sprint 6 | |
| RNF 7.2 | Órdenes ordenadas por fecha desc | Sprint 6 | ORDER BY created_at DESC |
| RNF 7.3 | Consistencia inventario en recepción | Sprint 6 | @Transaction |
| RNF 8.1 | Actualización panel <2 seg | Sprint 7 | LiveData Room |
| RNF 8.2 | Actualización automática contadores | Sprint 7 | Room observable queries |

### Requisitos UX

| ID | Descripción breve | Sprint | Notas |
|----|-------------------|--------|-------|
| UX-A1 | Menú navegación lateral con íconos | Sprint 1 | NavigationDrawer |
| UX-A2 | Máximo 3 toques a cualquier función | Sprint 2+ | Validado por estructura nav |
| UX-A3 | Indicadores de carga/error/confirmación | Sprint 1+ | LoadingStateView |
| UX-A4 | Botón retroceso funcional | Sprint 1 | Navigation Component |
| UX-B5 | Tema claro/oscuro manual y automático | Sprint 0+8 | Base en S0, selector en S8 |
| UX-B6 | Contraste mínimo 4.5:1 WCAG AA | Sprint 0 | Paleta validada |
| UX-B7 | 3-5 combinaciones color predefinidas | Sprint 8 | ThemeManager |
| UX-B8 | Indicadores color estándar | Sprint 0 | colors.xml semánticos |
| UX-C1 | Optimización imágenes | Sprint 2 | Compresión 80% |
| UX-C2 | Carga progresiva con placeholder | Sprint 2 | Glide placeholder |
| UX-C3 | Iconografía descriptiva | Sprint 0 | Material Icons |
| UX-C4 | Subir/actualizar foto perfil | Sprint 1 | ImageUtils |
| UX-C5 | Desactivar carga imágenes | Sprint 8 | Preferencia usuario |
| UX-D1 | Alarmas visuales con vibración y sonido | Sprint 7 | NotificationHelper |
| UX-D2 | Selección tono notificación | Sprint 7 | Preferencias |
| UX-D3 | Alarmas recurrentes | Sprint 7 | AlarmManager |
| UX-D4 | Íconos en notificaciones | Sprint 7 | NotificationCompat |
| UX-D5 | Silenciar/pausar alarmas | Sprint 7 | Action en notificación |
| UX-D6 | Modal alerta pantalla completa | Sprint 7 | DialogFragment fullscreen |
| UX-E1 | Temas claro/oscuro desde configuración | Sprint 8 | AppCompatDelegate |
| UX-E2 | Vista compacta/expandida | Sprint 8 | Preferencia |
| UX-E3 | Imagen/color fondo personalizado | Sprint 8 | |
| UX-E4 | Activar/desactivar animaciones | Sprint 8 | navOptions |
| UX-F1 | Ajustar tamaño fuente | Sprint 8 | fontScale |
| UX-F2 | Modo accesibilidad botones grandes | Sprint 8 | Dimens override |

---

## 5. Dependencias Gradle Definitivas

Todas las dependencias deben agregarse en `build.gradle (app)` desde el Sprint 0.

```groovy
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.swiftpay'
    compileSdk 34
    defaultConfig {
        applicationId "com.swiftpay"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += ["room.schemaLocation": "$projectDir/schemas"]
            }
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    // --- AndroidX Core ---
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // --- Material Design 3 ---
    // Componentes Material (TopAppBar, NavigationDrawer, FAB, Cards, TextFields, etc.)
    implementation 'com.google.android.material:material:1.12.0'

    // --- Room (Base de datos local) ---
    // Entity, DAO, Database
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    // Soporte Paging 3 con Room
    implementation 'androidx.room:room-paging:2.6.1'

    // --- Lifecycle (ViewModel + LiveData) ---
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.8.4'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.8.4'
    implementation 'androidx.lifecycle:lifecycle-process:2.8.4'
    // ProcessLifecycleOwner para detectar foreground/background

    // --- Navigation Component ---
    implementation 'androidx.navigation:navigation-fragment:2.7.7'
    implementation 'androidx.navigation:navigation-ui:2.7.7'

    // --- Paging 3 ---
    // Paginación de listas grandes (clientes, productos, ventas, etc.)
    implementation 'androidx.paging:paging-runtime:3.3.2'

    // --- Security (EncryptedSharedPreferences) ---
    // Almacenamiento seguro de la sesión activa
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'

    // --- BCrypt (Hash de contraseñas) ---
    // RNF 1.2: Algoritmo de hash seguro para contraseñas
    implementation 'org.mindrot:jbcrypt:0.4'

    // --- Glide (Carga de imágenes) ---
    // UX-C2: Carga progresiva con placeholder
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

    // --- ZXing (Escáner de código de barras) ---
    // RF 4.4: Escáner con cámara del dispositivo
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'

    // --- iText 7 (Generación de PDF) ---
    // RF 2.8: Comprobante de venta en PDF
    implementation 'com.itextpdf:itext7-core:7.2.5'

    // --- Fragment y Activity Result API ---
    implementation 'androidx.fragment:fragment:1.8.2'
    implementation 'androidx.activity:activity:1.9.1'
}
```

---

## 6. Instrucciones para el Agente Ejecutor

### 6.1 Protocolo General

1. **Leer este plan COMPLETO (Partes 1-5) antes de comenzar cualquier sprint.** No iniciar un sprint sin entender las decisiones de arquitectura de la sección 1.3. Esas decisiones son INMUTABLES.

2. **Ejecutar sprints en orden estricto:** 0 → 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8. No saltar sprints. No fusionar sprints.

3. **Verificación entre sprints:** Al finalizar cada sprint, el agente ejecutor debe:
   - Verificar que el proyecto compila sin errores (`./gradlew assembleDebug`)
   - Verificar que la app se instala y ejecuta en emulador
   - Verificar TODOS los criterios de aceptación del sprint
   - Solicitar al usuario confirmación verbal antes de pasar al siguiente sprint con el mensaje: *"Sprint X completado. ¿Verificaste los criterios de aceptación? ¿Procedo con el Sprint X+1?"*

4. **No tomar decisiones de arquitectura.** Si algo no está especificado en este plan, el agente debe preguntar al usuario. Ejemplos de cosas que NO debe decidir por su cuenta:
   - Agregar o cambiar dependencias no listadas
   - Cambiar la estructura de paquetes
   - Modificar el esquema de la BD
   - Usar patrones diferentes a MVVM
   - Implementar funcionalidades no documentadas

### 6.2 Manejo de Requisitos Ambiguos

Si el agente encuentra un requisito ambiguo en el documento de requisitos:
1. Buscar primero en este plan si la ambigüedad fue resuelta en las "Decisiones Técnicas" del sprint correspondiente
2. Si no fue resuelta, implementar la interpretación más conservadora (la que menos riesgo técnico tiene)
3. Documentar la decisión tomada como comentario en el código con el formato: `// DECISIÓN: [descripción] — Requisito [RF X.X] era ambiguo`
4. Informar al usuario de la decisión tomada

### 6.3 Manejo de Seed Data

- El Sprint 0 crea el DatabaseSeeder con usuario admin
- Conforme avancen los sprints, el DatabaseSeeder debe ACTUALIZARSE (no reemplazarse) para incluir datos de prueba de los módulos nuevos
- Los datos seed se insertan SOLO en `RoomDatabase.Callback.onCreate()` (primera instalación)
- Para desarrollo, el agente puede usar `fallbackToDestructiveMigration()` para resetear la BD al cambiar el esquema

### 6.4 Formato de Entrega del Código

Para cada archivo que el agente genere:
- Entregar el archivo COMPLETO, sin omisiones, sin `// ... resto del código ...`, sin placeholders
- Incluir como primera línea un comentario con la ruta completa: `// com/swiftpay/data/entity/User.java`
- Incluir TODOS los imports necesarios
- Incluir TODA la documentación JavaDoc para clases y métodos públicos
- Seguir convenciones Java estándar (camelCase métodos, PascalCase clases, UPPER_SNAKE constantes)

### 6.5 Checklist Pre-Completado de Sprint

Antes de declarar un sprint como completado, verificar:

- [ ] El proyecto compila con `./gradlew assembleDebug` sin errores ni warnings críticos
- [ ] Todos los archivos listados en el sprint fueron creados
- [ ] Todas las entities tienen las anotaciones Room correctas (@Entity, @PrimaryKey, @ColumnInfo, @ForeignKey)
- [ ] Todos los DAOs tienen las anotaciones correctas (@Dao, @Insert, @Update, @Delete, @Query)
- [ ] La base de datos (SwiftPayDatabase) incluye todas las entities nuevas del sprint
- [ ] El nav_graph.xml incluye todos los destinos nuevos del sprint
- [ ] Los menús del drawer incluyen las opciones nuevas del sprint para los roles correctos
- [ ] Los colores usados corresponden EXACTAMENTE a la paleta de colors.xml (nunca hardcoded)
- [ ] Las dimensiones usadas referencian dimens.xml (nunca hardcoded)
- [ ] Los textos visibles están en strings.xml (nunca hardcoded)
- [ ] Los layouts usan los estilos de texto definidos en styles.xml
- [ ] No hay lógica de negocio en Fragments ni Activities (solo en Repository/ViewModel)
- [ ] El control de acceso por rol está implementado en cada Fragment nuevo que lo requiera

---

## 7. Resumen Ejecutivo

El proyecto SwiftPay se implementa en **9 sprints (Sprint 0 al 8)** con una duración total estimada de **35 días (~5 semanas)**. La arquitectura es MVVM estricta con Java puro, Room, Navigation Component y Material Design 3.

**Orden crítico:** Autenticación → Marcas/Productos → Clientes/Descuentos → Ventas Core → Ventas Avanzado → Proveedores → Panel de Control → Configuración UX. Este orden elimina dependencias circulares y garantiza que cada sprint produce una app funcional.

**Riesgos técnicos más altos:** (1) Actualización atómica de stock en ventas y recepciones, mitigada con @Transaction de Room; (2) Impresión Bluetooth de PDF, mitigada con fallback a compartir; (3) Bloqueo optimista en productos concurrentes, mitigada con campo version.

Los 8 módulos (MO_01 a MO_08), los 62 requisitos funcionales, los 21 requisitos no funcionales y los 26 requisitos UX quedan cubiertos al 100% con sprint asignado. Ningún requisito queda sin implementar.
