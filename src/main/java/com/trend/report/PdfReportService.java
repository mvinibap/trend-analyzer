package com.trend.report;

import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates PDF reports containing charts.
 */
@Service
@Log4j2
public class PdfReportService {

    public Path save(BufferedImage chart, Path outDir, String fileName) throws IOException {
        log.debug("Saving PDF report to {}", outDir);
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
            log.debug("Created output directory {}", outDir);
        }
        Path pdfPath = outDir.resolve(fileName);
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(chart.getWidth(), chart.getHeight()));
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.drawImage(LosslessFactory.createFromImage(doc, chart), 0, 0);
            }
            doc.save(pdfPath.toFile());
        }
        log.info("PDF saved at {}", pdfPath);
        return pdfPath;
    }
}
