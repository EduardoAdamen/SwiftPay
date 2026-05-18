package com.swiftpay.util;

import android.content.Context;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.data.entity.SaleItem;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    public static File generateSaleReceipt(Context context, Sale sale, List<SaleItem> items) throws Exception {
        File dir = new File(context.getExternalFilesDir(null), "receipts");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File pdfFile = new File(dir, "venta_" + sale.getId() + ".pdf");
        PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        Paragraph title = new Paragraph("SwiftPay POS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24).setBold();
        document.add(title);

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        String dateStr = sdf.format(new Date(sale.getCreatedAt()));

        document.add(new Paragraph("Ticket de Venta #" + sale.getId()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Fecha: " + dateStr).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Atendido por: Vendedor #" + sale.getSellerId()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Items Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 50, 20, 20})).useAllAvailableWidth();
        table.addHeaderCell("Cant");
        table.addHeaderCell("Descripción");
        table.addHeaderCell("P.Unit");
        table.addHeaderCell("Subtotal");

        for (SaleItem item : items) {
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("Prod #" + item.getProductId());
            table.addCell(String.format(Locale.getDefault(), "$%.2f", item.getUnitPrice()));
            double subtotal = item.getQuantity() * item.getUnitPrice();
            table.addCell(String.format(Locale.getDefault(), "$%.2f", subtotal));
        }
        document.add(table);
        document.add(new Paragraph("\n"));

        // Totals
        document.add(new Paragraph(String.format(Locale.getDefault(), "Subtotal: $%.2f", sale.getSubtotal())).setTextAlignment(TextAlignment.RIGHT));
        if (sale.getDiscountPercentage() > 0) {
            document.add(new Paragraph(String.format(Locale.getDefault(), "Descuento (%.1f%%)", sale.getDiscountPercentage())).setTextAlignment(TextAlignment.RIGHT));
        }
        document.add(new Paragraph(String.format(Locale.getDefault(), "TOTAL: $%.2f", sale.getTotal())).setBold().setFontSize(16).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(String.format(Locale.getDefault(), "Pagado con: %s", sale.getPaymentMethod())).setTextAlignment(TextAlignment.RIGHT));
        if (Constants.PAYMENT_EFECTIVO.equals(sale.getPaymentMethod())) {
            document.add(new Paragraph(String.format(Locale.getDefault(), "Recibido: $%.2f", sale.getAmountReceived())).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph(String.format(Locale.getDefault(), "Cambio: $%.2f", sale.getChangeAmount())).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(new Paragraph("\n\n¡Gracias por su compra!").setTextAlignment(TextAlignment.CENTER));

        document.close();
        return pdfFile;
    }
}
