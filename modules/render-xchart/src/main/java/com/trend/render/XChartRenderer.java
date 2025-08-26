package com.trend.render;

import com.trend.core.Candle;
import com.trend.core.ChartRenderer;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class XChartRenderer implements ChartRenderer {
    @Override
    public BufferedImage renderDaily(String asset, List<Candle> candles, double[] maFast, int fastN, double[] maSlow, int slowN, boolean darkTheme, int widthPx, int heightPx) {
        OHLCChart chart = new OHLCChartBuilder().width(widthPx).height(heightPx).title(asset).build();
        if (darkTheme) {
            chart.getStyler().setChartBackgroundColor(Color.DARK_GRAY);
            chart.getStyler().setPlotBackgroundColor(Color.DARK_GRAY);
            chart.getStyler().setLegendBackgroundColor(Color.DARK_GRAY);
            chart.getStyler().setChartFontColor(Color.WHITE);
            chart.getStyler().setPlotGridLinesColor(Color.GRAY);
        }
        List<Date> xData = new ArrayList<>();
        List<Double> open = new ArrayList<>();
        List<Double> high = new ArrayList<>();
        List<Double> low = new ArrayList<>();
        List<Double> close = new ArrayList<>();
        for (Candle c : candles) {
            xData.add(new Date(c.timestamp() * 1000));
            open.add(c.open());
            high.add(c.high());
            low.add(c.low());
            close.add(c.close());
        }
        chart.addSeries("Price", xData, open, high, low, close);
        chart.addSeries("MA " + fastN, xData, Arrays.stream(maFast).boxed().toList());
        chart.addSeries("MA " + slowN, xData, Arrays.stream(maSlow).boxed().toList());
        return BitmapEncoder.getBufferedImage(chart);
    }
}
