package com.trend.core;

public final class MovingAverageService {
    private MovingAverageService() {}

    public static double[] sma(double[] closes, int period) {
        double[] out = new double[closes.length];
        double sum = 0;
        for (int i = 0; i < closes.length; i++) {
            sum += closes[i];
            if (i >= period) sum -= closes[i - period];
            out[i] = (i >= period - 1) ? sum / period : Double.NaN;
        }
        return out;
    }
}
