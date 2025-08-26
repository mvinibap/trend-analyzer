package com.trend.util;

import com.trend.model.Candle;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MissingDays {
    public List<LocalDate> findMissing(List<Candle> candles, int lookbackDays) {
        Set<LocalDate> existing = new HashSet<>();
        for (Candle c : candles) {
            LocalDate d = Instant.ofEpochMilli(c.timestamp()).atZone(ZoneOffset.UTC).toLocalDate();
            existing.add(d);
        }
        List<LocalDate> missing = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (int i = 0; i < lookbackDays; i++) {
            LocalDate d = today.minusDays(i);
            if (!existing.contains(d)) {
                missing.add(d);
            }
        }
        return missing;
    }
}
