package com.trend.report;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {
    private final ChartRenderer renderer;
    private final AppProperties props;

    public Path create(String asset, List<Candle> candles, List<Double> fast, List<Double> slow) throws IOException {
        BufferedImage image = renderer.render(candles, fast, slow);
        Path outDir = Path.of(props.report().outDir());
        Files.createDirectories(outDir);
        String fileName = asset.toUpperCase() + "_MA_" + props.ma().fast() + "-" + props.ma().slow() + "_" +
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(LocalDateTime.now()) + ".pdf";
        Path outFile = outDir.resolve(fileName);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
            doc.addPage(page);
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
            }
            doc.save(outFile.toFile());
        }
        return outFile;
    }
}
