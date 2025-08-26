package com.trend.report;

import com.trend.core.ReportComposer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class PdfBoxComposer implements ReportComposer {
    @Override
    public void composeSinglePage(BufferedImage chart, String header, String footer, Path outPdf) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(chart.getWidth(), chart.getHeight())) ;
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                var img = LosslessFactory.createFromImage(doc, chart);
                cs.drawImage(img, 0, 0, chart.getWidth(), chart.getHeight());
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(20, chart.getHeight() - 20);
                cs.showText(header);
                cs.endText();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(20, 20);
                cs.showText(footer);
                cs.endText();
            }
            doc.save(outPdf.toFile());
        }
    }
}
