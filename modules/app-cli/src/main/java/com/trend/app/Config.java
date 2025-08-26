package com.trend.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    public String timeframe;
    public String lookback;
    public MaConfig ma;
    public DataConfig data;
    public StorageConfig storage;
    public ThemeConfig theme;
    public ReportConfig report;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaConfig {
        public String type;
        public int fast;
        public int slow;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataConfig {
        public String provider;
        public String baseCurrency;
        public long throttleMs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StorageConfig {
        public String sqlitePath;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThemeConfig {
        public boolean dark;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReportConfig {
        public int widthPx;
        public int heightPx;
        public String outDir;
    }
}
