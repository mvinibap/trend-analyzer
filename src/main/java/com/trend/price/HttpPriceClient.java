package com.trend.price;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HTTP client to fetch candles from Binance API.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class HttpPriceClient {
    private final AppProperties props;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.binance.com")
            .build();

    public List<Candle> fetchCandles(String asset) {
        String symbol = asset.toUpperCase() + props.data().baseCurrency().toUpperCase();
        String interval = Optional.ofNullable(props.data().interval()).orElse("1d");
        int limit = props.data().limit();

        String url = String.format("/api/v3/klines?symbol=%s&interval=%s&limit=%d", symbol, interval, limit);
        log.debug("Request URL: {}", url);
        log.info("Fetching {} candles for {} with interval {}", limit, symbol, interval);

        List<List<Object>> raw = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<List<Object>>>() {})
                .block();

        List<Candle> out = new ArrayList<>();
        if (raw != null) {
            for (List<Object> arr : raw) {
                long openTime = ((Number) arr.get(0)).longValue();
                double open = Double.parseDouble(arr.get(1).toString());
                double high = Double.parseDouble(arr.get(2).toString());
                double low = Double.parseDouble(arr.get(3).toString());
                double close = Double.parseDouble(arr.get(4).toString());
                double volume = Double.parseDouble(arr.get(5).toString());
                Candle candle = new Candle(openTime, open, high, low, close, volume);
                out.add(candle);
                log.debug("Parsed candle: {}", candle);
            }
        }
        log.info("Fetched {} candles", out.size());
        return out;
    }
}
