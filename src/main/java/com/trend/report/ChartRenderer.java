package com.trend.report;

import com.trend.model.Candle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.OHLCSeries;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.springframework.stereotype.Service;

@Service
public class ChartRenderer {
    public BufferedImage renderDaily(String asset, List<Candle> candles,
                                     double[] maFast, int fastN,
                                     double[] maSlow, int slowN,
                                     boolean dark, int width, int height) {
        List<Date> x = candles.stream().map(c -> new Date(c.timestamp())).collect(Collectors.toList());
        List<Double> opens = candles.stream().map(Candle::open).collect(Collectors.toList());
        List<Double> highs = candles.stream().map(Candle::high).collect(Collectors.toList());
        List<Double> lows = candles.stream().map(Candle::low).collect(Collectors.toList());
        List<Double> closes = candles.stream().map(Candle::close).collect(Collectors.toList());
        OHLCChart chart = new OHLCChartBuilder().width(width).height(height).title(asset).build();
        chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        chart.addSeries("Price", x, opens, highs, lows, closes);
        List<Double> fastList = Arrays.stream(maFast).boxed().collect(Collectors.toList());
        OHLCSeries fast = chart.addSeries("MA " + fastN, x, fastList, fastList, fastList, fastList);
        fast.setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Line);
        List<Double> slowList = Arrays.stream(maSlow).boxed().collect(Collectors.toList());
        OHLCSeries slow = chart.addSeries("MA " + slowN, x, slowList, slowList, slowList, slowList);
        slow.setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Line);
        return BitmapEncoder.getBufferedImage(chart);
    }
}
