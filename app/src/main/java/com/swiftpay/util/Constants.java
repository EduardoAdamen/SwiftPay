package com.swiftpay.util;

/**
 * Constantes globales del sistema SwiftPay.
 */
public final class Constants {

    private Constants() {} // No instanciable

    // --- Sesión ---
    public static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutos

    // --- Contraseñas ---
    public static final int MIN_PASSWORD_LENGTH = 8;

    // --- Imágenes ---
    public static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final int IMAGE_COMPRESSION_QUALITY = 80;
    public static final String IMAGE_DIR = "images";
    public static final String PROFILE_IMAGE_DIR = "images/profiles";
    public static final String PRODUCT_IMAGE_DIR = "images/products";

    // --- Ventas ---
    public static final double MIN_PRICE_PERCENTAGE = 0.5; // 50% del precio catálogo
    public static final String STATUS_PENDIENTE = "PENDIENTE";
    public static final String STATUS_PAGADA = "PAGADA";
    public static final String STATUS_COMPLETADA = "COMPLETADA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    // --- Métodos de pago ---
    public static final String PAYMENT_EFECTIVO = "EFECTIVO";
    public static final String PAYMENT_TARJETA = "TARJETA";
    public static final String PAYMENT_TRANSFERENCIA = "TRANSFERENCIA";

    // --- Eventos del sistema ---
    public static final String EVENT_NEW_CLIENT = "NEW_CLIENT";
    public static final String EVENT_NEW_SALE = "NEW_SALE";
    public static final String EVENT_CASH_DIFFERENCE = "CASH_DIFFERENCE";

    // --- Órdenes de compra ---
    public static final String ORDER_PENDIENTE = "PENDIENTE";
    public static final String ORDER_RECIBIDA = "RECIBIDA";
    public static final String ORDER_CANCELADA = "CANCELADA";

    // --- Paginación ---
    public static final int PAGE_SIZE = 20;

    // --- Formatos ---
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_SHORT = "dd/MM/yyyy";
    public static final String CURRENCY_SYMBOL = "$";
}
