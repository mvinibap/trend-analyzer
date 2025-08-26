package com.trend.orchestrator;

import com.trend.calc.MovingAverageService;
import com.trend.config.AppProperties;
import com.trend.model.Candle;
import com.trend.price.PriceService;
import com.trend.report.ChartRenderer;
import com.trend.report.PdfReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Orchestrates the price fetching, moving average calculation and PDF report generation.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ReportOrchestrator {
    private final AppProperties cfg;
    private final PriceService priceService;
    private final MovingAverageService maService;
    private final ChartRenderer chartRenderer;
    private final PdfReportService pdfReport;

    public Path generate(String asset) throws Exception {
        log.debug("Starting report generation for {}", asset);
        List<Candle> series = priceService.getSeries(asset);
        log.debug("Series length: {}", series.size());

        List<Double> fast = maService.sma(series, cfg.ma().fast());
        List<Double> slow = maService.sma(series, cfg.ma().slow());

        BufferedImage chart = chartRenderer.render(series, fast, slow, cfg.report().widthPx(), cfg.report().heightPx());
        String fileName = String.format("%s-%d.pdf", asset.toUpperCase(), Instant.now().toEpochMilli());
        Path out = pdfReport.save(chart, Path.of(cfg.report().outDir()), fileName);
        log.info("Report generated at {}", out);
        return out;
    }
}
