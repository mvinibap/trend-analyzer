package com.trend.data.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.trend.core.Candle;
import com.trend.core.CandleSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CandleSourceHttp implements CandleSource {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final long throttleMs;

    public CandleSourceHttp(long throttleMs) {
        this.throttleMs = throttleMs;
    }

    @Override
    public List<Candle> fetchDaily(String asset, String baseCurrency, Instant from, Instant to) throws Exception {
        if (throttleMs > 0) Thread.sleep(throttleMs);
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        String url = String.format("https://api.coingecko.com/api/v3/coins/%s/ohlc?vs_currency=%s&days=%d",
                asset.toLowerCase(), baseCurrency.toLowerCase(), days);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        ArrayNode arr = (ArrayNode) mapper.readTree(resp.body());
        List<Candle> candles = new ArrayList<>();
        for (JsonNode node : arr) {
            long ts = node.get(0).asLong();
            double open = node.get(1).asDouble();
            double high = node.get(2).asDouble();
            double low = node.get(3).asDouble();
            double close = node.get(4).asDouble();
            candles.add(new Candle(ts / 1000, open, high, low, close, null));
        }
        return candles;
    }
}
