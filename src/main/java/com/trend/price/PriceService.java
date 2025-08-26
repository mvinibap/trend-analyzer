package com.trend.price;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {
    private final HttpPriceClient client;
    private final PriceRepository repository;
    private final AppProperties props;

    public List<Candle> load(String asset, String base, Instant from, Instant to) {
        List<Candle> candles = client.fetchCandles(asset, base, from, to, props.data().throttleMs());
        repository.saveAll(asset, candles);
        return candles;
    }
}
