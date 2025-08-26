package com.trend.price;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpPriceClient {
    private final AppProperties props;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.binance.com")
            .build();

    public List<Candle> fetchCandles(String asset, String base, Instant from, Instant to, int throttleMs) {
        String symbol = asset.toUpperCase() + base.toUpperCase();
        String interval = props.data().interval();
        String url = "/api/v3/klines?symbol=" + symbol + "&interval=" + interval + "&limit=1000";

        List<List<Object>> raw = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<List<Object>>>() {})
                .block();

        List<Candle> out = new ArrayList<>();
        for (List<Object> arr : raw) {
            long openTime = ((Number) arr.get(0)).longValue();
            double open = Double.parseDouble(arr.get(1).toString());
            double high = Double.parseDouble(arr.get(2).toString());
            double low = Double.parseDouble(arr.get(3).toString());
            double close = Double.parseDouble(arr.get(4).toString());
            double volume = Double.parseDouble(arr.get(5).toString());
            out.add(new Candle(openTime, open, high, low, close, volume));
        }

        if (throttleMs > 0) {
            try {
                Thread.sleep(throttleMs);
            } catch (InterruptedException ignored) {
            }
        }

        return out;
    }
}
