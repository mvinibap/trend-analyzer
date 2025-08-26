package com.trend.price;

import com.trend.model.Candle;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service to access price data.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PriceService {
    private final HttpPriceClient client;

    public List<Candle> getSeries(String asset) {
        log.debug("Getting series for asset {}", asset);
        List<Candle> candles = client.fetchCandles(asset);
        log.debug("Retrieved {} candles", candles.size());
        return candles;
    }
}
