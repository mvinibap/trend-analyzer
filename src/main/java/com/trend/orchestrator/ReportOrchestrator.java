package com.trend.orchestrator;

import com.trend.calc.MovingAverageService;
import com.trend.config.AppProperties;
import com.trend.model.Candle;
import com.trend.price.PriceService;
import com.trend.report.PdfReportService;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportOrchestrator {
    private final PriceService priceService;
    private final MovingAverageService maService;
    private final PdfReportService pdfService;
    private final AppProperties props;

    public Path generate(String asset) throws Exception {
        String base = props.data().baseCurrency();
        Instant now = Instant.now();
        Instant from = now.minus(props.lookback());
        List<Candle> candles = priceService.load(asset, base, from, now);
        List<Double> fast = maService.sma(candles, props.ma().fast());
        List<Double> slow = maService.sma(candles, props.ma().slow());
        return pdfService.create(asset, candles, fast, slow);
    }
}
