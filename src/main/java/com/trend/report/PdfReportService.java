package com.trend.report;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {
    public void writeSinglePage(BufferedImage chart, String header, String footer, Path outPdf) throws Exception {
        Files.createDirectories(outPdf.getParent());
        try (PDDocument doc = new PDDocument()) {
            PDRectangle size = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(size);
            doc.addPage(page);
            PDImageXObject image = LosslessFactory.createFromImage(doc, chart);
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            float imgW = image.getWidth();
            float imgH = image.getHeight();
            float scale = Math.min((width - 40) / imgW, (height - 80) / imgH);
            float imgX = (width - imgW * scale) / 2;
            float imgY = (height - imgH * scale) / 2;
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font footerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                cs.beginText();
                cs.setFont(headerFont, 14);
                cs.newLineAtOffset(40, height - 30);
                cs.showText(header);
                cs.endText();
                cs.beginText();
                cs.setFont(footerFont, 12);
                cs.newLineAtOffset(40, 20);
                cs.showText(footer);
                cs.endText();
                cs.drawImage(image, imgX, imgY, imgW * scale, imgH * scale);
            }
            doc.save(outPdf.toFile());
        }
    }
}
