package com.trend.util;

import com.trend.model.Candle;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class CandleNormalizer {
    public Candle normalizeD1(Candle c) {
        LocalDate d = Instant.ofEpochMilli(c.timestamp()).atZone(ZoneOffset.UTC).toLocalDate();
        long ts = d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return new Candle(ts, c.open(), c.high(), c.low(), c.close(), c.volume());
    }
}
