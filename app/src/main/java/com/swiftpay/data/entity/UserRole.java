// com/swiftpay/data/entity/UserRole.java
package com.swiftpay.data.entity;

/**
 * Enum que define los roles de usuario disponibles en el sistema SwiftPay.
 * Cada usuario tiene exactamente un rol asignado que determina su acceso
 * a las diferentes funcionalidades del sistema.
 */
public enum UserRole {
    /** Rol para empleados que realizan ventas y gestionan clientes */
    VENDEDOR,
    /** Rol para administradores con acceso completo al sistema */
    ADMINISTRADOR,
    /** Rol para gestores que administran productos, marcas y proveedores */
    GESTOR_PRODUCTOS
}
