package com.sh.sh.pos.system.service.serviceimpl.purchaseorderspdfserviceimpl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sh.sh.pos.system.exception.PdfGenerationException;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.service.purchaseorderspdfservice.PurchaseOrderPdfService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PurchaseOrderPdfServiceImpl implements PurchaseOrderPdfService {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    private static final String STATUS_RECEIVED           = "RECEIVED";
    private static final String STATUS_PARTIALLY_RECEIVED = "PARTIALLY_RECEIVED";
    private static final String EMPTY_VALUE               = "—";

    // ── Colors ────────────────────────────────────────────────────────────
    private Color black()     { return new DeviceRgb(0,   0,   0);   }
    private Color darkGray()  { return new DeviceRgb(60,  60,  60);  }
    private Color lightGray() { return new DeviceRgb(200, 200, 200); }
    private Color headerBg()  { return new DeviceRgb(225, 225, 225); }
    private Color rowAlt()    { return new DeviceRgb(248, 248, 248); }
    private Color totalBg()   { return new DeviceRgb(240, 240, 240); }
    private Color grandBg()   { return new DeviceRgb(220, 220, 220); }
    // Stamp colors — red for RECEIVED, orange for PARTIAL
    private DeviceRgb stampRed()    { return new DeviceRgb(185, 28,  28);  }
    private DeviceRgb stampOrange() { return new DeviceRgb(180, 83,  9);   }

    @Override
    public byte[] generatePdf(PurchaseOrder order) {
        log.info("📄 generatePdf() called for PO: {}", order.getPoNumber());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String brand = brand(order);
            boolean isReceived    = hasStatus(order, STATUS_RECEIVED, STATUS_PARTIALLY_RECEIVED);
            boolean fullyReceived = hasStatus(order, STATUS_RECEIVED);

            addTitle(doc, brand, bold, normal);
            addReceivedBadge(doc, order, isReceived, fullyReceived, bold, normal);
            addSupplierAndMeta(doc, order, bold, normal);
            addDeliveryInfo(doc, order, bold, normal);
            addItemsTable(doc, order, isReceived, bold, normal);
            addTotals(doc, order, bold, normal);
            addNotes(doc, order, bold, normal);
            addSignatureRow(doc, bold, normal);

            if (isReceived) {
                drawReceivedStamp(pdf, order, brand, fullyReceived);
            }
            addFooter(pdf, normal);

            doc.close();
            byte[] bytes = out.toByteArray();
            log.info("✅ PDF generated: {} bytes for PO {}", bytes.length, order.getPoNumber());
            return bytes;

        } catch (Exception e) {
            log.error("❌ PDF generation failed for PO {}: {}", order.getPoNumber(), e.getMessage(), e);
            throw new PdfGenerationException("PDF generation failed for PO " + order.getPoNumber(), e);
        }
    }

    private boolean hasStatus(PurchaseOrder order, String... statuses) {
        if (order.getStatus() == null) {
            return false;
        }
        String name = order.getStatus().name();
        for (String status : statuses) {
            if (name.equals(status)) {
                return true;
            }
        }
        return false;
    }

    // ── Document sections ─────────────────────────────────────────────────

    private void addTitle(Document doc, String brand, PdfFont bold, PdfFont normal) {
        doc.add(new Paragraph("PURCHASE ORDER")
                .setFont(bold).setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(black()).setMarginBottom(2));

        doc.add(new Paragraph(brand)
                .setFont(normal).setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(darkGray()).setMarginBottom(4));
    }

    private void addReceivedBadge(Document doc, PurchaseOrder order,
            boolean isReceived, boolean fullyReceived, PdfFont bold, PdfFont normal) {

        if (!isReceived) {
            doc.add(new Paragraph(" ").setMarginBottom(8));
            return;
        }

        String badgeText = fullyReceived ? "✓ GOODS RECEIVED" : "◑ PARTIALLY RECEIVED";
        String dateStr = order.getReceivedDate() != null
                ? order.getReceivedDate().format(DATE_FMT)
                : LocalDate.now().format(DATE_FMT);

        doc.add(new Paragraph(badgeText)
                .setFont(bold).setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(darkGray()).setMarginBottom(1));
        doc.add(new Paragraph("Date Received: " + dateStr)
                .setFont(normal).setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(darkGray()).setMarginBottom(10));
    }

    private void addSupplierAndMeta(Document doc, PurchaseOrder order, PdfFont bold, PdfFont normal) {
        Supplier supplier = order.getSupplier();

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);

        Cell left = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
        left.add(lv("Supplier",       supplier != null ? nvl(supplier.getName())          : EMPTY_VALUE, bold, normal));
        left.add(lv("Contact Person", supplier != null ? nvl(supplier.getContactPerson()) : EMPTY_VALUE, bold, normal));
        left.add(lv("Address",        supplier != null ? nvl(supplier.getAddress())       : EMPTY_VALUE, bold, normal));
        left.add(lv("TIN",            supplier != null ? nvl(supplier.getTaxNumber())     : EMPTY_VALUE, bold, normal));
        left.add(lv("Phone No.",      supplier != null ? nvl(supplier.getPhone())         : EMPTY_VALUE, bold, normal));
        infoTable.addCell(left);

        Cell right = new Cell().setBorder(Border.NO_BORDER);
        right.add(lvRight("P.O No.", nvl(order.getPoNumber()), bold, normal));
        right.add(lvRight("Date",
                order.getOrderDate() != null ? order.getOrderDate().format(DATE_FMT) : EMPTY_VALUE, bold, normal));
        right.add(lvRight("Mode of Procurement",
                nvl(order.getReferenceNo(), "Direct Contracting"), bold, normal));
        right.add(lvRight("Status",
                order.getStatus() != null ? order.getStatus().name() : EMPTY_VALUE, bold, normal));
        infoTable.addCell(right);

        doc.add(infoTable);
    }

    private void addDeliveryInfo(Document doc, PurchaseOrder order, PdfFont bold, PdfFont normal) {
        Supplier supplier = order.getSupplier();

        Table deliveryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);

        Cell dlLeft = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
        dlLeft.add(lv("Place of Delivery",
                order.getBranch() != null ? nvl(order.getBranch().getName()) : EMPTY_VALUE, bold, normal));
        dlLeft.add(lv("Date of Delivery",
                order.getExpectedDelivery() != null ? order.getExpectedDelivery().format(DATE_FMT) : EMPTY_VALUE,
                bold, normal));
        deliveryTable.addCell(dlLeft);

        Cell dlRight = new Cell().setBorder(Border.NO_BORDER);
        dlRight.add(lvRight("Delivery Term",
                nvl(order.getCarrierId(), "Cost, Insurance, and Freight"), bold, normal));
        dlRight.add(lvRight("Payment Term", resolvePaymentTerm(order, supplier), bold, normal));
        deliveryTable.addCell(dlRight);

        doc.add(deliveryTable);
    }

    private String resolvePaymentTerm(PurchaseOrder order, Supplier supplier) {
        if (order.getPaymentTermDays() != null) {
            return "Net " + order.getPaymentTermDays() + " days";
        }
        if (supplier != null && supplier.getPaymentTermDays() != null) {
            return "Net " + supplier.getPaymentTermDays() + " days";
        }
        return "Cash next delivery";
    }

    private void addItemsTable(Document doc, PurchaseOrder order,
            boolean isReceived, PdfFont bold, PdfFont normal) {

        float[] cw = isReceived
                ? new float[]{0.4f, 1f, 2f, 0.8f, 1f, 1f, 1f, 1f, 1f, 1.2f}
                : new float[]{0.4f, 1.2f, 2.5f, 0.9f, 1.2f, 1.2f, 0.7f, 1.2f, 1.3f};

        String[] headers = isReceived
                ? new String[]{"#", "SKU", "Description", "Unit", "Brand", "Variant", "Ordered", "Received", "Remaining", "Total"}
                : new String[]{"#", "SKU", "Description", "Unit", "Brand", "Variant", "Qty", "Price", "Total"};

        Table items = new Table(UnitValue.createPercentArray(cw))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(4);
        for (String h : headers) {
            items.addHeaderCell(thCell(h, bold));
        }

        int rn = 1;
        for (PurchaseOrderItem item : order.getItems()) {
            addItemRow(items, item, rn++, isReceived, bold, normal);
        }

        doc.add(items);
    }

    private void addItemRow(Table items, PurchaseOrderItem item, int rowNumber,
            boolean isReceived, PdfFont bold, PdfFont normal) {

        boolean alt = rowNumber % 2 == 0;
        String pn = item.getProduct() != null ? nvl(item.getProduct().getName()) : EMPTY_VALUE;
        String sk = item.getProduct() != null && item.getProduct().getSku()   != null ? item.getProduct().getSku()          : "";
        String un = item.getProduct() != null && item.getProduct().getUnit()  != null ? item.getProduct().getUnit().name()  : "";
        String br = item.getProduct() != null && item.getProduct().getBrand() != null ? item.getProduct().getBrand()        : "";
        String vr = item.getVariant() != null ? nvl(item.getVariant().getName()) : EMPTY_VALUE;

        items.addCell(tdCell(String.valueOf(rowNumber), alt, normal, TextAlignment.CENTER));
        items.addCell(tdCell(sk, alt, normal, TextAlignment.LEFT));
        items.addCell(tdCell(pn, alt, normal, TextAlignment.LEFT));
        items.addCell(tdCell(un, alt, normal, TextAlignment.CENTER));
        items.addCell(tdCell(br, alt, normal, TextAlignment.CENTER));
        items.addCell(tdCell(vr, alt, normal, TextAlignment.CENTER));
        items.addCell(tdCell(String.valueOf(item.getRequestedQuantity()), alt, normal, TextAlignment.CENTER));

        if (isReceived) {
            items.addCell(tdCell(String.valueOf(item.getReceivedQuantity()),  alt, bold,   TextAlignment.CENTER));
            items.addCell(tdCell(String.valueOf(item.getRemainingQuantity()), alt, normal, TextAlignment.CENTER));
        } else {
            items.addCell(tdCell(fmt(item.getUnitCost()), alt, normal, TextAlignment.RIGHT));
        }
        items.addCell(tdCell(fmt(item.getTotalCost()), alt, normal, TextAlignment.RIGHT));
    }

    private void addTotals(Document doc, PurchaseOrder order, PdfFont bold, PdfFont normal) {
        String sym = currencySymbol(order);

        Table totals = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(35))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(16);

        totals.addCell(totalLabelCell("Subtotal", bold));
        totals.addCell(totalValueCell(sym + " " + fmt(order.getSubtotal()), normal));

        if (gt0(order.getDiscount())) {
            totals.addCell(totalLabelCell("Discount", bold));
            totals.addCell(totalValueCell("- " + sym + " " + fmt(order.getDiscount()), normal));
        }
        if (gt0(order.getTax())) {
            totals.addCell(totalLabelCell("Tax", bold));
            totals.addCell(totalValueCell("+ " + sym + " " + fmt(order.getTax()), normal));
        }
        if (gt0(order.getShippingCost())) {
            totals.addCell(totalLabelCell("Shipping", bold));
            totals.addCell(totalValueCell("+ " + sym + " " + fmt(order.getShippingCost()), normal));
        }

        totals.addCell(new Cell().setBackgroundColor(grandBg())
                .setBorder(new SolidBorder(lightGray(), 0.5f)).setPadding(6)
                .add(new Paragraph("Grand Total").setFont(bold).setFontSize(10).setFontColor(black())));
        totals.addCell(new Cell().setBackgroundColor(grandBg())
                .setBorder(new SolidBorder(lightGray(), 0.5f)).setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(sym + " " + fmt(order.getTotalAmount()))
                        .setFont(bold).setFontSize(11).setFontColor(black())));

        doc.add(totals);
    }

    private void addNotes(Document doc, PurchaseOrder order, PdfFont bold, PdfFont normal) {
        if (order.getNotes() == null || order.getNotes().isBlank()) {
            return;
        }
        doc.add(new Paragraph("Note:")
                .setFont(bold).setFontSize(10).setFontColor(black()).setMarginBottom(2));
        doc.add(new Paragraph(order.getNotes())
                .setFont(normal).setFontSize(10).setFontColor(darkGray()).setMarginBottom(16));
    }

    private void addSignatureRow(Document doc, PdfFont bold, PdfFont normal) {
        doc.add(new Paragraph(" ").setMarginBottom(16));
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));
        sigTable.addCell(sigCell("Prepared by", bold, normal));
        sigTable.addCell(sigCell("Approved by", bold, normal));
        sigTable.addCell(sigCell("Received by", bold, normal));
        doc.add(sigTable);
    }

    private void addFooter(PdfDocument pdf, PdfFont normal) {
        String generatedAt = "Generated: " + LocalDateTime.now().format(DATETIME_FMT);
        int pageCount = pdf.getNumberOfPages();
        Color footerGray = new DeviceRgb(150, 150, 150);

        for (int p = 1; p <= pageCount; p++) {
            PdfCanvas fc = new PdfCanvas(pdf.getPage(p));
            Rectangle ps = pdf.getPage(p).getPageSize();
            drawFixedText(fc, ps,
                    generatedAt + "   |   Page " + p + " of " + pageCount,
                    normal, 7f, footerGray, 0, 16, ps.getWidth());
            fc.release();
        }
    }

    // ── Stamp ─────────────────────────────────────────────────────────────

    private void drawReceivedStamp(PdfDocument pdf, PurchaseOrder order,
            String brand, boolean fullyReceived) throws java.io.IOException {

        DeviceRgb stampRgb = fullyReceived ? stampRed() : stampOrange();
        String stampLabel  = fullyReceived ? STATUS_RECEIVED : "PARTIAL";
        String dateVal     = order.getReceivedDate() != null
                ? order.getReceivedDate().format(DATE_FMT)
                : LocalDate.now().format(DATE_FMT);
        String brandLine   = brand.toUpperCase();

        PdfFont stBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont stNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        int pages = pdf.getNumberOfPages();
        for (int p = 1; p <= pages; p++) {
            drawStampOnPage(pdf.getPage(p).getPageSize(), new PdfCanvas(pdf.getPage(p)),
                    stampRgb, stampLabel, dateVal, brandLine, stBold, stNormal);
        }
    }

    private void drawStampOnPage(Rectangle pageRect, PdfCanvas pc, DeviceRgb stampRgb,
            String stampLabel, String dateVal, String brandLine, PdfFont stBold, PdfFont stNormal) {

        float cx = pageRect.getWidth()  / 2f;
        float cy = pageRect.getHeight() / 2f;

        // ── Stamp box dimensions — rectangle centered on page ─────────────
        float bw = 160f;
        float bh =  90f;
        float bx = cx - bw / 2f;
        float by = cy - bh / 2f;

        // ── Double rectangle border ────────────────────────────────────────
        PdfExtGState gsBox = new PdfExtGState()
                .setFillOpacity(0.0f)
                .setStrokeOpacity(0.20f);
        pc.saveState().setExtGState(gsBox);
        pc.setStrokeColor(stampRgb);

        pc.setLineWidth(3f);
        pc.rectangle(bx, by, bw, bh);
        pc.stroke();

        float inset = 4f;
        pc.setLineWidth(1f);
        pc.rectangle(bx + inset, by + inset, bw - inset * 2, bh - inset * 2);
        pc.stroke();

        pc.restoreState();

        // ── Text inside box ────────────────────────────────────────────────
        PdfExtGState gsText = new PdfExtGState()
                .setFillOpacity(0.22f)
                .setStrokeOpacity(0.22f);
        pc.saveState().setExtGState(gsText);
        pc.setFillColor(stampRgb);

        float padTop  = 10f;
        float lineH9  = 11f;
        float lineH8  =  9f;
        float lineH22 = 26f;

        float yComp = by + bh - padTop - lineH9;
        float yRecv = yComp - 3f - lineH22;
        float yLine = yRecv - 4f;
        float yDate = yLine - 3f - lineH8;
        float yBy   = yDate - 2f - lineH8;

        drawFixedText(pc, pageRect, brandLine, stBold, 9f, stampRgb, bx, yComp, bw);
        drawFixedText(pc, pageRect, stampLabel, stBold, 22f, stampRgb, bx, yRecv, bw);

        // Thin separator line
        PdfExtGState gsLine = new PdfExtGState().setStrokeOpacity(0.18f);
        pc.restoreState();
        pc.saveState().setExtGState(gsLine);
        pc.setStrokeColor(stampRgb).setLineWidth(0.6f)
                .moveTo(bx + 16f, yLine)
                .lineTo(bx + bw - 16f, yLine)
                .stroke();
        pc.restoreState();

        pc.saveState().setExtGState(gsText);

        drawFixedText(pc, pageRect, "DATE:  " + dateVal, stBold, 8f, stampRgb, bx, yDate, bw);
        drawFixedText(pc, pageRect, "BY: " + brandLine, stNormal, 8f, stampRgb, bx, yBy, bw);

        pc.restoreState().release();
    }

    /**
     * Draws a fixed-position, centered paragraph via a short-lived Canvas,
     * closed with try-with-resources (java:S2095).
     */
    private void drawFixedText(PdfCanvas pc, Rectangle pageRect,
            String text, PdfFont font, float fontSize, Color color,
            float x, float y, float width) {
        try (Canvas canvas = new Canvas(pc, pageRect)) {
            canvas.add(new Paragraph(text)
                    .setFont(font).setFontSize(fontSize)
                    .setFontColor(color)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedPosition(x, y, width));
        }
    }

    // ── Cell builders ─────────────────────────────────────────────────────

    private Cell thCell(String text, PdfFont bold) {
        return new Cell()
                .setBackgroundColor(headerBg())
                .setBorder(new SolidBorder(lightGray(), 0.5f))
                .setPadding(5)
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(black()));
    }

    private Cell tdCell(String text, boolean alt, PdfFont font, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(alt ? rowAlt() : ColorConstants.WHITE)
                .setBorder(new SolidBorder(lightGray(), 0.5f))
                .setPadding(5)
                .setTextAlignment(align)
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(9).setFontColor(black()));
    }

    private Cell totalLabelCell(String label, PdfFont bold) {
        return new Cell()
                .setBackgroundColor(totalBg())
                .setBorder(new SolidBorder(lightGray(), 0.5f))
                .setPadding(5)
                .add(new Paragraph(label).setFont(bold).setFontSize(9).setFontColor(black()));
    }

    private Cell totalValueCell(String value, PdfFont normal) {
        return new Cell()
                .setBackgroundColor(totalBg())
                .setBorder(new SolidBorder(lightGray(), 0.5f))
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(value).setFont(normal).setFontSize(9).setFontColor(darkGray()));
    }

    private Paragraph lv(String label, String value, PdfFont bold, PdfFont normal) {
        return new Paragraph().setMarginBottom(3)
                .add(new Text(label).setFont(bold).setFontSize(9).setFontColor(black()))
                .add(new Text(" : ").setFont(normal).setFontSize(9).setFontColor(black()))
                .add(new Text(value).setFont(normal).setFontSize(9).setFontColor(darkGray()));
    }

    private Paragraph lvRight(String label, String value, PdfFont bold, PdfFont normal) {
        return new Paragraph().setMarginBottom(3).setTextAlignment(TextAlignment.RIGHT)
                .add(new Text(label).setFont(bold).setFontSize(9).setFontColor(black()))
                .add(new Text(" : ").setFont(normal).setFontSize(9).setFontColor(black()))
                .add(new Text(value).setFont(normal).setFontSize(9).setFontColor(darkGray()));
    }

    private Cell sigCell(String label, PdfFont bold, PdfFont normal) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(8);
        cell.add(new Paragraph("\n\n\n").setFont(normal).setFontSize(9));
        cell.add(new Paragraph("_______________________________").setFont(normal).setFontSize(9).setFontColor(darkGray()));
        cell.add(new Paragraph(label).setFont(bold).setFontSize(9).setFontColor(black()));
        return cell;
    }

    // ── Util ──────────────────────────────────────────────────────────────

    private String currencySymbol(PurchaseOrder order) {
        return order.getCurrency() != null ? order.getCurrency().getSymbol() : "$";
    }

    private String brand(PurchaseOrder order) {
        return Optional.ofNullable(order.getBranch())
                .map(branch -> branch.getStore())
                .map(store -> store.getBrand())
                .orElse("SH-POS");
    }

    private String fmt(BigDecimal v)  { return v != null ? String.format("%.2f", v) : "0.00"; }
    private boolean gt0(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) > 0; }
    private String nvl(String v)      { return v != null && !v.isBlank() ? v : EMPTY_VALUE; }
    private String nvl(String v, String fallback) { return v != null && !v.isBlank() ? v : fallback; }
}