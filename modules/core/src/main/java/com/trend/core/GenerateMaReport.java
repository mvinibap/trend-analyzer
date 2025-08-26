package com.trend.core;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GenerateMaReport {
    private final CandleSource candleSource;
    private final CandleStore candleStore;
    private final ChartRenderer chartRenderer;
    private final ReportComposer reportComposer;

    public GenerateMaReport(CandleSource candleSource,
                            CandleStore candleStore,
                            ChartRenderer chartRenderer,
                            ReportComposer reportComposer) {
        this.candleSource = candleSource;
        this.candleStore = candleStore;
        this.chartRenderer = chartRenderer;
        this.reportComposer = reportComposer;
    }

    public Path execute(String asset, String baseCurrency,
                        Instant from, Instant to,
                        int fast, int slow,
                        boolean darkTheme,
                        int widthPx, int heightPx,
                        Path outDir) throws Exception {
        List<Candle> candles = candleStore.rangeDaily(asset, from, to);
        if (candles.isEmpty()) {
            candles = candleSource.fetchDaily(asset, baseCurrency, from, to);
            candleStore.upsertDaily(asset, candles);
        }
        double[] closes = candles.stream().mapToDouble(Candle::close).toArray();
        double[] maFast = MovingAverageService.sma(closes, fast);
        double[] maSlow = MovingAverageService.sma(closes, slow);
        BufferedImage chart = chartRenderer.renderDaily(asset, candles, maFast, fast, maSlow, slow, darkTheme, widthPx, heightPx);
        String header = String.format("%s MA %d/%d", asset, fast, slow);
        String footer = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").withZone(ZoneOffset.UTC).format(Instant.now());
        Files.createDirectories(outDir);
        Path outPdf = outDir.resolve(String.format("%s_MA_%d-%d_%s.pdf", asset, fast, slow, ts));
        reportComposer.composeSinglePage(chart, header, footer, outPdf);
        return outPdf;
    }
}
