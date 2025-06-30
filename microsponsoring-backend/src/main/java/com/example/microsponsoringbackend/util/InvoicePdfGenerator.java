package com.example.microsponsoringbackend.util;

import com.example.microsponsoringbackend.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
import java.awt.Color;
import com.lowagie.text.html.WebColors;

public class InvoicePdfGenerator {
    public static void generateInvoicePdf(Invoice invoice, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // --- Custom Palette Colors ---
        Color darkTeal = WebColors.getRGBColor("#222831");
        Color mediumTeal = WebColors.getRGBColor("#393E46");
        Color lightTeal = WebColors.getRGBColor("#948979");
        Color cream = WebColors.getRGBColor("#DFD0B8");

        // --- Gradient Header (simulate by drawing rectangles) ---
        PdfContentByte canvas = writer.getDirectContentUnder();
        float headerHeight = 60f;
        for (int i = 0; i < 100; i++) {
            float ratio = i / 100f;
            Color blend = new Color(
                (int)(mediumTeal.getRed() * (1 - ratio) + lightTeal.getRed() * ratio),
                (int)(mediumTeal.getGreen() * (1 - ratio) + lightTeal.getGreen() * ratio),
                (int)(mediumTeal.getBlue() * (1 - ratio) + lightTeal.getBlue() * ratio)
            );
            canvas.setColorFill(blend);
            canvas.rectangle(50 + (ratio * (PageSize.A4.getWidth() - 100)),
                PageSize.A4.getHeight() - 50 - headerHeight,
                (PageSize.A4.getWidth() - 100) / 100f,
                headerHeight);
            canvas.fill();
        }

        // --- Title on Gradient Header ---
        Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, cream);
        Paragraph title = new Paragraph("DONATION RECEIPT", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        title.setSpacingAfter(20);
        title.setSpacingBefore(10);
        document.add(title);

        // --- Info Section ---
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);
        infoTable.setWidths(new int[]{2, 2});

        // ISSUED TO
        Sponsor sponsor = invoice.getSponsor();
        User sponsorUser = sponsor != null ? sponsor.getUser() : null;
        StringBuilder issuedTo = new StringBuilder();
        issuedTo.append("DONOR:\n");
        if (sponsorUser != null) {
            issuedTo.append(sponsorUser.getFullName() != null ? sponsorUser.getFullName() + "\n" : "");
            issuedTo.append(sponsorUser.getEmail() != null ? sponsorUser.getEmail() + "\n" : "");
            issuedTo.append(sponsorUser.getLocation() != null ? sponsorUser.getLocation() : "");
        }
        PdfPCell issuedToCell = new PdfPCell(new Phrase(issuedTo.toString(), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
        issuedToCell.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(issuedToCell);

        // Receipt meta
        StringBuilder meta = new StringBuilder();
        meta.append("RECEIPT NO:  ").append(invoice.getInvoiceId()).append("\n");
        meta.append("DATE:  ").append(invoice.getInvoiceDate()).append("\n");
        meta.append("DONATION DATE:  ").append(invoice.getInvoiceDate()).append("\n");
        PdfPCell metaCell = new PdfPCell(new Phrase(meta.toString(), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
        metaCell.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(metaCell);
        document.add(infoTable);

        // --- PAY TO Section ---
        StringBuilder payTo = new StringBuilder();
        payTo.append("RECIPIENT ORGANIZATION:\n");
        companyNonProfits company = invoice.getCompany();
        if (company != null) {
            List<BankAccount> accounts = SpringContext.getBean(com.example.microsponsoringbackend.service.BankAccountService.class)
                .getByUserId(company.getUser().getUserId());
            if (!accounts.isEmpty()) {
                BankAccount acc = accounts.get(0); // Use first/default
                payTo.append(acc.getBankName() != null ? "Bank: " + acc.getBankName() + "\n" : "");
                payTo.append(acc.getAccountHolderName() != null ? "Account Name: " + acc.getAccountHolderName() + "\n" : "");
                payTo.append(acc.getIban() != null ? "Account No.: " + acc.getIban() + "\n" : "");
            }
        }
        Paragraph payToPara = new Paragraph(payTo.toString(), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal));
        payToPara.setSpacingAfter(10);
        document.add(payToPara);

        // --- Table of Benefits ---
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{4, 2, 1, 2});
        Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD, cream);
        String[] headers = {"DESCRIPTION", "DONATION AMOUNT", "QTY", "TOTAL"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
            cell.setBackgroundColor(mediumTeal);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        List<RecognitionBenefits> benefits = invoice.getBenefits();
        double subtotal = 0;
        if (benefits != null) {
            for (RecognitionBenefits b : benefits) {
                String desc = b.getRewardType() != null ? b.getRewardType() : "Donation Benefit";
                double price = b.getCurrency() != null ? b.getCurrency() : 0;
                int qty = 1;
                double total = price * qty;
                subtotal += total;
                table.addCell(new Phrase(desc, new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
                table.addCell(new Phrase(String.format("%.2f", price), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
                table.addCell(new Phrase(String.valueOf(qty), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
                table.addCell(new Phrase(String.format("$%.2f", total), new Font(Font.HELVETICA, 10, Font.NORMAL, darkTeal)));
            }
        }
        document.add(table);

        // --- Total Donation Amount (No Tax) ---
        Paragraph summary = new Paragraph();
        summary.setSpacingBefore(10);
        summary.add(new Phrase(String.format("TOTAL DONATION: $%.2f", subtotal), new Font(Font.HELVETICA, 12, Font.BOLD, mediumTeal)));
        summary.add(new Phrase("\n\nThis is a charitable donation. No tax is applicable.", new Font(Font.HELVETICA, 10, Font.ITALIC, lightTeal)));
        document.add(summary);

        // --- Signature (optional placeholder) ---
        Paragraph sign = new Paragraph("\n\n__________________________\nAuthorized Signature", new Font(Font.HELVETICA, 10, Font.ITALIC, mediumTeal));
        sign.setAlignment(Element.ALIGN_RIGHT);
        document.add(sign);

        document.close();
    }
} 