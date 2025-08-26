package com.trend.price;

import com.trend.model.Candle;
import com.trend.util.CandleNormalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HttpPriceClient {

    private final WebClient client = WebClient.create("https://api.coingecko.com/api/v3");
    private final CandleNormalizer normalizer = new CandleNormalizer();

    public List<Candle> fetchDaily(String symbol, String vsCurrency, int days) {
        String coinId = mapSymbol(symbol);
        String uri = String.format("/coins/%s/market_chart?vs_currency=%s&days=%d&interval=daily", coinId, vsCurrency.toLowerCase(), days);
        Map<String, Object> resp = client.get().uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        List<List<Number>> prices = (List<List<Number>>) resp.get("prices");
        List<List<Number>> volumes = (List<List<Number>>) resp.get("total_volumes");
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            long ts = prices.get(i).get(0).longValue();
            double close = prices.get(i).get(1).doubleValue();
            Double volume = volumes != null && i < volumes.size() ? volumes.get(i).get(1).doubleValue() : null;
            Candle c = new Candle(ts, close, close, close, close, volume);
            candles.add(normalizer.normalizeD1(c));
        }
        return candles;
    }

    private String mapSymbol(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "BTC" -> "bitcoin";
            case "ETH" -> "ethereum";
            case "ADA" -> "cardano";
            case "SOL" -> "solana";
            default -> symbol.toLowerCase();
        };
    }
}
