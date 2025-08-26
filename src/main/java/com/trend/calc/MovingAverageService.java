package com.trend.calc;

import com.trend.model.Candle;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to calculate simple moving averages.
 */
@Service
@Log4j2
public class MovingAverageService {

    public List<Double> sma(List<Candle> candles, int period) {
        log.debug("Calculating SMA with period {} over {} candles", period, candles.size());
        List<Double> out = new ArrayList<>();
        double sum = 0.0;
        for (int i = 0; i < candles.size(); i++) {
            sum += candles.get(i).close();
            if (i >= period) {
                sum -= candles.get(i - period).close();
            }
            if (i >= period - 1) {
                double avg = sum / period;
                out.add(avg);
                log.trace("SMA at index {} = {}", i, avg);
            } else {
                out.add(Double.NaN);
            }
        }
        log.debug("Calculated {} SMA values", out.size());
        return out;
    }
}
