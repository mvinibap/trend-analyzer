package com.trend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ta")
public record AppProperties(
        Duration lookback,
        MaProperties ma,
        DataProperties data,
        StorageProperties storage,
        ThemeProperties theme,
        ReportProperties report
) {
    public record MaProperties(String type, int fast, int slow) {}
    public record DataProperties(String provider, String baseCurrency, String interval, int throttleMs) {}
    public record StorageProperties(String sqlitePath) {}
    public record ThemeProperties(boolean dark) {}
    public record ReportProperties(int widthPx, int heightPx, String outDir) {}
}
