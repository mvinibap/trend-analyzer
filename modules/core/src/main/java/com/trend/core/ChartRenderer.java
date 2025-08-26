package com.trend.core;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ChartRenderer {
    BufferedImage renderDaily(String asset,
                              List<Candle> candles,
                              double[] maFast, int fastN,
                              double[] maSlow, int slowN,
                              boolean darkTheme,
                              int widthPx, int heightPx);
}
