package com.trend.model;

public record Candle(
    long timestamp,
    double open,
    double high,
    double low,
    double close,
    Double volume
) {}
