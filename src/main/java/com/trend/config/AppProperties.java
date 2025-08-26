package com.trend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for Trend Analyzer.
 */
@ConfigurationProperties(prefix = "ta")
public record AppProperties(
        String lookback,
        Ma ma,
        Data data,
        Theme theme,
        Report report
) {
    public record Ma(String type, int fast, int slow) {}
    public record Data(String provider, String baseCurrency, String interval, int limit, int throttleMs) {}
    public record Theme(boolean dark) {}
    public record Report(int widthPx, int heightPx, String outDir) {}
}
