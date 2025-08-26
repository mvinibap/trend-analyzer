package com.trend.core;

import java.time.Instant;
import java.util.List;

public interface CandleStore {
    void upsertDaily(String asset, List<Candle> candles) throws Exception;
    List<Candle> rangeDaily(String asset, Instant from, Instant to) throws Exception;
}
