package com.trend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ta")
public record AppProperties(
    String timeframe,
    String lookback,
    Ma ma,
    Data data,
    Storage storage,
    Theme theme,
    Report report
) {
    public record Ma(String type, int fast, int slow) {}
    public record Data(String provider, String baseCurrency, int throttleMs) {}
    public record Storage(String sqlitePath) {}
    public record Theme(boolean dark) {}
    public record Report(int widthPx, int heightPx, String outDir) {}
}
