package com.trend.price;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import java.time.Period;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    private final HttpPriceClient client;
    private final PriceRepository repo;
    private final AppProperties props;

    public PriceService(HttpPriceClient client, PriceRepository repo, AppProperties props) {
        this.client = client;
        this.repo = repo;
        this.props = props;
    }

    public List<Candle> loadDaily(String assetArg) {
        String[] parts = assetArg.split("-");
        String symbol = parts[0];
        String vs = parts.length > 1 ? parts[1] : props.data().baseCurrency();
        int days = Period.parse(props.lookback()).getDays();
        List<Candle> candles = client.fetchDaily(symbol, vs, days);
        repo.upsertDaily(assetArg, candles);
        return candles;
    }

    public List<Candle> getFromDb(String assetArg) {
        return repo.findAll(assetArg);
    }
}
