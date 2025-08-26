package com.trend.calc;

import com.trend.model.Candle;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MovingAverageService {
    public List<Double> sma(List<Candle> candles, int period) {
        List<Double> out = new ArrayList<>();
        for (int i = 0; i < candles.size(); i++) {
            if (i + 1 < period) {
                out.add(null);
            } else {
                double sum = 0;
                for (int j = i + 1 - period; j <= i; j++) {
                    sum += candles.get(j).close();
                }
                out.add(sum / period);
            }
        }
        return out;
    }
}
