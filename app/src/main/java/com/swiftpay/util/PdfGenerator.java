// app/src/main/java/com/swiftpay/util/PdfGenerator.java
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
import com.swiftpay.data.entity.SaleItemWithProduct;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.HorizontalAlignment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.swiftpay.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Generates immutable sale receipt PDFs using the sale snapshot stored in Room.
 */
public final class PdfGenerator {

    private PdfGenerator() {
    }

    /**
     * Creates a PDF receipt under the app private files directory.
     *
     * @param context Android context used to resolve private storage
     * @param sale persisted sale header
     * @param items persisted sale item snapshots with unit and catalog prices
     * @return generated receipt file
     * @throws Exception when the PDF cannot be written
     */
    public static File generateSaleReceipt(Context context, Sale sale, List<SaleItemWithProduct> items) throws Exception {
        File dir = new File(context.getFilesDir(), "receipts");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear el directorio de recibos");
        }

        File pdfFile = new File(dir, "venta_" + sale.getId() + ".pdf");
        PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapData = stream.toByteArray();
                Image logo = new Image(ImageDataFactory.create(bitmapData));
                logo.setWidth(UnitValue.createPointValue(80));
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Continue without logo if it fails
        }

        Paragraph title = new Paragraph("SwiftPay POS")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold();
        document.add(title);

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        String dateStr = sdf.format(new Date(sale.getCreatedAt()));

        document.add(new Paragraph("Ticket de Venta #" + sale.getId()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Fecha: " + dateStr).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Atendido por: Vendedor #" + sale.getSellerId()).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 50, 20, 20})).useAllAvailableWidth();
        table.addHeaderCell("Cant");
        table.addHeaderCell("Descripcion");
        table.addHeaderCell("P.Unit");
        table.addHeaderCell("Subtotal");

        for (SaleItemWithProduct wrappedItem : items) {
            com.swiftpay.data.entity.SaleItem item = wrappedItem.saleItem;
            table.addCell(String.valueOf(item.getQuantity()));
            
            if (wrappedItem.product != null) {
                table.addCell(wrappedItem.product.getName());
            } else {
                table.addCell("Prod #" + item.getProductId());
            }
            
            table.addCell(String.format(Locale.getDefault(), "$%.2f", item.getUnitPrice()));
            double itemSubtotal = item.getQuantity() * item.getUnitPrice();
            table.addCell(String.format(Locale.getDefault(), "$%.2f", itemSubtotal));
        }
        document.add(table);
        document.add(new Paragraph("\n"));

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

        document.add(new Paragraph("\n\nGracias por su compra").setTextAlignment(TextAlignment.CENTER));
        document.close();
        return pdfFile;
    }
}
