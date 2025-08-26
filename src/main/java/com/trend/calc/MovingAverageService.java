package com.trend.calc;

import org.springframework.stereotype.Service;

@Service
public class MovingAverageService {
    public double[] sma(double[] closes, int n) {
        double[] out = new double[closes.length];
        double sum = 0;
        for (int i = 0; i < closes.length; i++) {
            sum += closes[i];
            if (i >= n) sum -= closes[i - n];
            out[i] = (i >= n - 1) ? sum / n : Double.NaN;
        }
        return out;
    }
}
