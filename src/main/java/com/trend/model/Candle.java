package com.trend.model;

/**
 * Represents a single OHLCV candle.
 */
public record Candle(
        long timestamp,
        double open,
        double high,
        double low,
        double close,
        double volume
) {
}
