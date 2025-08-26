package com.trend.core;

import java.time.Instant;
import java.util.List;

public interface CandleSource {
    List<Candle> fetchDaily(String asset, String baseCurrency, Instant from, Instant to) throws Exception;
}
