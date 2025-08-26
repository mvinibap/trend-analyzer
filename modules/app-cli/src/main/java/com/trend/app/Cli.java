package com.trend.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.trend.core.*;
import com.trend.data.http.CandleSourceHttp;
import com.trend.data.sqlite.CandleStoreSqlite;
import com.trend.render.XChartRenderer;
import com.trend.report.PdfBoxComposer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "trend-analyzer", mixinStandardHelpOptions = true)
public class Cli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Asset symbol, e.g. ETH-USD")
    private String symbol;

    public static void main(String[] args) {
        int exit = new CommandLine(new Cli()).execute(args);
        System.exit(exit);
    }

    @Override
    public Integer call() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path configPath = Path.of("config/config.yaml");
        if (!configPath.toFile().exists()) configPath = Path.of("../config/config.yaml");
        if (!configPath.toFile().exists()) configPath = Path.of("../../config/config.yaml");
        Config cfg = mapper.readValue(configPath.toFile(), Config.class);
        String[] parts = symbol.split("-");
        String asset = parts[0];
        String base = parts.length > 1 ? parts[1] : cfg.data.baseCurrency;
        long days = Period.parse(cfg.lookback).getDays();
        Instant to = Instant.now();
        Instant from = to.minus(days, ChronoUnit.DAYS);
        Path dbPath = Path.of(cfg.storage.sqlitePath);
        if (!dbPath.toFile().exists()) dbPath = Path.of("../" + cfg.storage.sqlitePath);
        if (!dbPath.toFile().exists()) dbPath = Path.of("../../" + cfg.storage.sqlitePath);
        if (!"coingecko".equalsIgnoreCase(cfg.data.provider)) {
            throw new IllegalArgumentException("Unsupported data provider: " + cfg.data.provider);
        }
        CandleSource source = new CandleSourceHttp(cfg.data.throttleMs);
        CandleStore store = new CandleStoreSqlite(dbPath.toString());
        ChartRenderer renderer = new XChartRenderer();
        ReportComposer composer = new PdfBoxComposer();
        int fast = Integer.parseInt(System.getenv().getOrDefault("TA_MA_FAST", Integer.toString(cfg.ma.fast)));
        int slow = Integer.parseInt(System.getenv().getOrDefault("TA_MA_SLOW", Integer.toString(cfg.ma.slow)));
        GenerateMaReport useCase = new GenerateMaReport(source, store, renderer, composer);
        Path outPdf = useCase.execute(asset, base, from, to, fast, slow, cfg.theme.dark, cfg.report.widthPx, cfg.report.heightPx, Path.of(cfg.report.outDir));
        System.out.println("Generated: " + outPdf);
        return 0;
    }
}
