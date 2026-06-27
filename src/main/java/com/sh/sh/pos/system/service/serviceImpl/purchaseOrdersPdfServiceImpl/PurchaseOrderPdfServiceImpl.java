package com.sh.sh.pos.system.service.serviceImpl.purchaseOrdersPdfServiceImpl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.service.purchaseOrdersPdfService.PurchaseOrderPdfService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PurchaseOrderPdfServiceImpl implements PurchaseOrderPdfService {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

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
            PdfWriter   writer = new PdfWriter(out);
            PdfDocument pdf    = new PdfDocument(writer);
            Document    doc    = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            Supplier supplier = order.getSupplier();
            String   brand    = brand(order);
            String   sym      = currencySymbol(order);

            boolean isReceived    = order.getStatus() != null &&
                    (order.getStatus().name().equals("RECEIVED") ||
                     order.getStatus().name().equals("PARTIALLY_RECEIVED"));
            boolean fullyReceived = order.getStatus() != null &&
                    order.getStatus().name().equals("RECEIVED");

            // ── Title ─────────────────────────────────────────────────────

            doc.add(new Paragraph("PURCHASE ORDER")
                    .setFont(bold).setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(black()).setMarginBottom(2));

            doc.add(new Paragraph(brand)
                    .setFont(normal).setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(darkGray()).setMarginBottom(4));

            // ── Received badge (small text under title) ───────────────────

            if (isReceived) {
                
                String badgeText   = fullyReceived ? "✓ GOODS RECEIVED" : "◑ PARTIALLY RECEIVED";
                String dateStr     = order.getReceivedDate() != null
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
            } else {
                doc.add(new Paragraph(" ").setMarginBottom(8));
            }

            // ── Supplier (left) + PO Meta (right) ────────────────────────

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);

            Cell left = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
            left.add(lv("Supplier",       supplier != null ? nvl(supplier.getName())          : "—", bold, normal));
            left.add(lv("Contact Person", supplier != null ? nvl(supplier.getContactPerson()) : "—", bold, normal));
            left.add(lv("Address",        supplier != null ? nvl(supplier.getAddress())       : "—", bold, normal));
            left.add(lv("TIN",            supplier != null ? nvl(supplier.getTaxNumber())     : "—", bold, normal));
            left.add(lv("Phone No.",      supplier != null ? nvl(supplier.getPhone())         : "—", bold, normal));
            infoTable.addCell(left);

            Cell right = new Cell().setBorder(Border.NO_BORDER);
            right.add(lvRight("P.O No.",              nvl(order.getPoNumber()),                            bold, normal));
            right.add(lvRight("Date",                 order.getOrderDate() != null ? order.getOrderDate().format(DATE_FMT) : "—", bold, normal));
            right.add(lvRight("Mode of Procurement",  nvl(order.getReferenceNo(), "Direct Contracting"),   bold, normal));
            right.add(lvRight("Status",               order.getStatus() != null ? order.getStatus().name() : "—", bold, normal));
            infoTable.addCell(right);
            doc.add(infoTable);

            // ── Delivery info ─────────────────────────────────────────────

            Table deliveryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);

            Cell dlLeft = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(10);
            dlLeft.add(lv("Place of Delivery", order.getBranch() != null ? nvl(order.getBranch().getName()) : "—", bold, normal));
            dlLeft.add(lv("Date of Delivery",  order.getExpectedDelivery() != null ? order.getExpectedDelivery().format(DATE_FMT) : "—", bold, normal));
            deliveryTable.addCell(dlLeft);

            Cell dlRight = new Cell().setBorder(Border.NO_BORDER);
            dlRight.add(lvRight("Delivery Term", nvl(order.getCarrierId(), "Cost, Insurance, and Freight"), bold, normal));
            String payTerm = order.getPaymentTermDays() != null ? "Net " + order.getPaymentTermDays() + " days"
                    : supplier != null && supplier.getPaymentTermDays() != null ? "Net " + supplier.getPaymentTermDays() + " days"
                    : "Cash next delivery";
            dlRight.add(lvRight("Payment Term", payTerm, bold, normal));
            deliveryTable.addCell(dlRight);
            doc.add(deliveryTable);

            // ── Items table ───────────────────────────────────────────────

            if (isReceived) {
                float[] cw = {0.4f, 1f, 2f, 0.8f, 1f, 1f, 1f, 1f, 1f, 1.2f};
                Table items = new Table(UnitValue.createPercentArray(cw))
                        .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(4);
                for (String h : new String[]{"#","SKU","Description","Unit","Brand","Variant","Ordered","Received","Remaining","Total"})
                    items.addHeaderCell(thCell(h, bold));

                int rn = 1;
                for (PurchaseOrderItem item : order.getItems()) {
                    boolean alt = rn % 2 == 0;
                    String pn = item.getProduct() != null ? nvl(item.getProduct().getName()) : "—";
                    String sk = item.getProduct() != null && item.getProduct().getSku()  != null ? item.getProduct().getSku()  : "";
                    String un = item.getProduct() != null && item.getProduct().getUnit() != null ? item.getProduct().getUnit().name() : "";
                    String br = item.getProduct() != null && item.getProduct().getBrand()!= null ? item.getProduct().getBrand() : "";
                    String vr = item.getVariant() != null ? nvl(item.getVariant().getName()) : "—";
                    items.addCell(tdCell(String.valueOf(rn++),                      alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(sk,                                        alt, normal, TextAlignment.LEFT));
                    items.addCell(tdCell(pn,                                        alt, normal, TextAlignment.LEFT));
                    items.addCell(tdCell(un,                                        alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(br,                                        alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(vr,                                        alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(String.valueOf(item.getRequestedQuantity()),alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(String.valueOf(item.getReceivedQuantity()), alt, bold,   TextAlignment.CENTER));
                    items.addCell(tdCell(String.valueOf(item.getRemainingQuantity()),alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(fmt(item.getTotalCost()),                  alt, normal, TextAlignment.RIGHT));
                }
                doc.add(items);
            } else {
                float[] cw = {0.4f, 1.2f, 2.5f, 0.9f, 1.2f, 1.2f, 0.7f, 1.2f, 1.3f};
                Table items = new Table(UnitValue.createPercentArray(cw))
                        .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(4);
                for (String h : new String[]{"#","SKU","Description","Unit","Brand","Variant","Qty","Price","Total"})
                    items.addHeaderCell(thCell(h, bold));

                int rn = 1;
                for (PurchaseOrderItem item : order.getItems()) {
                    boolean alt = rn % 2 == 0;
                    String pn = item.getProduct() != null ? nvl(item.getProduct().getName()) : "—";
                    String sk = item.getProduct() != null && item.getProduct().getSku()  != null ? item.getProduct().getSku()  : "";
                    String un = item.getProduct() != null && item.getProduct().getUnit() != null ? item.getProduct().getUnit().name() : "";
                    String br = item.getProduct() != null && item.getProduct().getBrand()!= null ? item.getProduct().getBrand() : "";
                    String vr = item.getVariant() != null ? nvl(item.getVariant().getName()) : "—";
                    items.addCell(tdCell(String.valueOf(rn++),                       alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(sk,                                         alt, normal, TextAlignment.LEFT));
                    items.addCell(tdCell(pn,                                         alt, normal, TextAlignment.LEFT));
                    items.addCell(tdCell(un,                                         alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(br,                                         alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(vr,                                         alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(String.valueOf(item.getRequestedQuantity()), alt, normal, TextAlignment.CENTER));
                    items.addCell(tdCell(fmt(item.getUnitCost()),                    alt, normal, TextAlignment.RIGHT));
                    items.addCell(tdCell(fmt(item.getTotalCost()),                   alt, normal, TextAlignment.RIGHT));
                }
                doc.add(items);
            }

            // ── Totals ────────────────────────────────────────────────────

            Table totals = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(35))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                    .setMarginBottom(16);
            totals.addCell(totalLabelCell("Subtotal", bold));
            totals.addCell(totalValueCell(sym + " " + fmt(order.getSubtotal()), normal));
            if (gt0(order.getDiscount())) { totals.addCell(totalLabelCell("Discount", bold)); totals.addCell(totalValueCell("- " + sym + " " + fmt(order.getDiscount()), normal)); }
            if (gt0(order.getTax()))      { totals.addCell(totalLabelCell("Tax",      bold)); totals.addCell(totalValueCell("+ " + sym + " " + fmt(order.getTax()),      normal)); }
            if (gt0(order.getShippingCost())) { totals.addCell(totalLabelCell("Shipping", bold)); totals.addCell(totalValueCell("+ " + sym + " " + fmt(order.getShippingCost()), normal)); }
            totals.addCell(new Cell().setBackgroundColor(grandBg()).setBorder(new SolidBorder(lightGray(), 0.5f)).setPadding(6)
                    .add(new Paragraph("Grand Total").setFont(bold).setFontSize(10).setFontColor(black())));
            totals.addCell(new Cell().setBackgroundColor(grandBg()).setBorder(new SolidBorder(lightGray(), 0.5f)).setPadding(6)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph(sym + " " + fmt(order.getTotalAmount())).setFont(bold).setFontSize(11).setFontColor(black())));
            doc.add(totals);

            // ── Notes ─────────────────────────────────────────────────────

            if (order.getNotes() != null && !order.getNotes().isBlank()) {
                doc.add(new Paragraph("Note:").setFont(bold).setFontSize(10).setFontColor(black()).setMarginBottom(2));
                doc.add(new Paragraph(order.getNotes()).setFont(normal).setFontSize(10).setFontColor(darkGray()).setMarginBottom(16));
            }

            // ── Signature row ─────────────────────────────────────────────

            doc.add(new Paragraph(" ").setMarginBottom(16));
            Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            sigTable.addCell(sigCell("Prepared by", bold, normal));
            sigTable.addCell(sigCell("Approved by", bold, normal));
            sigTable.addCell(sigCell("Received by",  bold, normal));
            doc.add(sigTable);

            // ── CIRCULAR STAMP — drawn last so it overlays content ────────

            if (isReceived) {
                DeviceRgb stampRgb  = fullyReceived ? stampRed() : stampOrange();
                String stampLabel   = fullyReceived ? "RECEIVED" : "PARTIAL";
                String dateVal      = order.getReceivedDate() != null
                        ? order.getReceivedDate().format(DATE_FMT)
                        : LocalDate.now().format(DATE_FMT);
                String byLine       = brand.toUpperCase();
                String topLine      = brand.toUpperCase();

                int pages = pdf.getNumberOfPages();
                for (int p = 1; p <= pages; p++) {
                    com.itextpdf.kernel.geom.Rectangle pageRect = pdf.getPage(p).getPageSize();
                    float cx = pageRect.getWidth()  / 2f;
                    float cy = pageRect.getHeight() / 2f;

                    PdfCanvas pc = new PdfCanvas(pdf.getPage(p));
                    PdfFont stBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                    PdfFont stNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                    // ── Stamp box dimensions ───────────────────────────────
                    // Stamp is a rectangle centered on page
                    float bw = 160f; // box width
                    float bh =  90f; // box height
                    float bx = cx - bw / 2f;
                    float by = cy - bh / 2f;

                    // ── Draw outer rectangle border (double border) ────────
                    PdfExtGState gsBox = new PdfExtGState()
                            .setFillOpacity(0.0f)
                            .setStrokeOpacity(0.20f);
                    pc.saveState().setExtGState(gsBox);
                    pc.setStrokeColor(stampRgb);

                    // Outer border — thick
                    pc.setLineWidth(3f);
                    pc.rectangle(bx, by, bw, bh);
                    pc.stroke();

                    // Inner border — thin, 4px inset
                    float inset = 4f;
                    pc.setLineWidth(1f);
                    pc.rectangle(bx + inset, by + inset, bw - inset * 2, bh - inset * 2);
                    pc.stroke();

                    pc.restoreState();

                    // ── Text inside box ───────────────────────────────────
                    PdfExtGState gsText = new PdfExtGState()
                            .setFillOpacity(0.22f)
                            .setStrokeOpacity(0.22f);
                    pc.saveState().setExtGState(gsText);
                    pc.setFillColor(stampRgb);

                    // Layout (top to bottom inside box):
                    //   padding top = 10px
                    //   company name  9pt  → ~10px
                    //   gap 3px
                    //   RECEIVED     22pt  → ~26px
                    //   gap 3px
                    //   thin line
                    //   gap 3px
                    //   DATE          8pt  → ~10px
                    //   gap 2px
                    //   BY            8pt  → ~10px
                    //   padding bot = 8px
                    // total ≈ 10+10+3+26+3+1+3+10+2+10+8 = 86 ✓ fits in bh=90

                    float padTop = 10f;
                    float lineH9 = 11f;  // rendered height of 9pt text
                    float lineH8 =  9f;
                    float lineH22= 26f;  // rendered height of 22pt text

                    float yComp = by + bh - padTop - lineH9;
                    float yRecv = yComp - 3f - lineH22;
                    float yLine = yRecv - 4f;
                    float yDate = yLine - 3f - lineH8;
                    float yBy   = yDate - 2f - lineH8;

                    // Company name
                    new Canvas(pc, pageRect)
                            .add(new Paragraph(topLine)
                                    .setFont(stBold).setFontSize(9f)
                                    .setFontColor(stampRgb)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setFixedPosition(bx, yComp, bw))
                            .close();

                    // RECEIVED / PARTIAL — main label
                    new Canvas(pc, pageRect)
                            .add(new Paragraph(stampLabel)
                                    .setFont(stBold).setFontSize(22f)
                                    .setFontColor(stampRgb)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setFixedPosition(bx, yRecv, bw))
                            .close();

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

                    // DATE
                    new Canvas(pc, pageRect)
                            .add(new Paragraph("DATE:  " + dateVal)
                                    .setFont(stBold).setFontSize(8f)
                                    .setFontColor(stampRgb)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setFixedPosition(bx, yDate, bw))
                            .close();

                    // BY
                    new Canvas(pc, pageRect)
                            .add(new Paragraph("BY: " + byLine)
                                    .setFont(stNormal).setFontSize(8f)
                                    .setFontColor(stampRgb)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setFixedPosition(bx, yBy, bw))
                            .close();

                    pc.restoreState().release();
                }
            }

            // ── Footer timestamp ──────────────────────────────────────────

            String generatedAt = "Generated: " + LocalDateTime.now().format(DATETIME_FMT);
            int pageCount = pdf.getNumberOfPages();
            for (int p = 1; p <= pageCount; p++) {
                PdfCanvas fc = new PdfCanvas(pdf.getPage(p));
                com.itextpdf.kernel.geom.Rectangle ps = pdf.getPage(p).getPageSize();
                new Canvas(fc, ps)
                        .add(new Paragraph(generatedAt + "   |   Page " + p + " of " + pageCount)
                                .setFont(normal).setFontSize(7)
                                .setFontColor(new DeviceRgb(150, 150, 150))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFixedPosition(0, 16, ps.getWidth()))
                        .close();
                fc.release();
            }

            doc.close();
            byte[] bytes = out.toByteArray();
            log.info("✅ PDF generated: {} bytes for PO {}", bytes.length, order.getPoNumber());
            return bytes;

        } catch (Exception e) {
            log.error("❌ PDF generation failed for PO {}: {}", order.getPoNumber(), e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ── Stamp helpers ─────────────────────────────────────────────────────

    /**
     * Draws a filled 5-pointed star centered at (cx, cy) with outer radius r.
     */
    private void drawStar(PdfCanvas canvas, float cx, float cy, float r) {
        try {
            float inner = r * 0.4f;
            canvas.moveTo(
                    cx + r * (float) Math.cos(Math.toRadians(90)),
                    cy + r * (float) Math.sin(Math.toRadians(90)));
            for (int i = 1; i <= 10; i++) {
                double angle = Math.toRadians(90 - i * 36);
                float  rad   = (i % 2 == 0) ? r : inner;
                canvas.lineTo(
                        cx + rad * (float) Math.cos(angle),
                        cy + rad * (float) Math.sin(angle));
            }
            canvas.closePathFillStroke();
        } catch (Exception ignored) {}
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
        try { return order.getCurrency() != null ? order.getCurrency().getSymbol() : "₱"; }
        catch (Exception e) { return "₱"; }
    }

    private String brand(PurchaseOrder order) {
        try {
            if (order.getBranch() != null && order.getBranch().getStore() != null
                    && order.getBranch().getStore().getBrand() != null)
                return order.getBranch().getStore().getBrand();
        } catch (Exception ignored) {}
        return "SH-POS";
    }

    private String fmt(BigDecimal v)  { return v != null ? String.format("%.2f", v) : "0.00"; }
    private boolean gt0(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) > 0; }
    private String nvl(String v)      { return v != null && !v.isBlank() ? v : "—"; }
    private String nvl(String v, String fallback) { return v != null && !v.isBlank() ? v : fallback; }
}