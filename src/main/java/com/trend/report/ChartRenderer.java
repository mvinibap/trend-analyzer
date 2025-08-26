package com.trend.report;

import com.trend.config.AppProperties;
import com.trend.model.Candle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.OHLCSeries;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.style.Styler;

@Service
@RequiredArgsConstructor
public class ChartRenderer {
    private final AppProperties props;

    public BufferedImage render(List<Candle> candles, List<Double> fast, List<Double> slow) {
        int width = props.report().widthPx();
        int height = props.report().heightPx();
        OHLCChart chart = new OHLCChartBuilder().width(width).height(height).title("Trend").build();

        double[] x = candles.stream().mapToDouble(Candle::timestamp).toArray();
        double[] open = candles.stream().mapToDouble(Candle::open).toArray();
        double[] high = candles.stream().mapToDouble(Candle::high).toArray();
        double[] low = candles.stream().mapToDouble(Candle::low).toArray();
        double[] close = candles.stream().mapToDouble(Candle::close).toArray();

        chart.addSeries("Price", x, open, high, low, close);

        double[] fastArr = fast.stream().mapToDouble(d -> d == null ? Double.NaN : d).toArray();
        double[] slowArr = slow.stream().mapToDouble(d -> d == null ? Double.NaN : d).toArray();
        OHLCSeries fastSeries = chart.addSeries("MA " + props.ma().fast(), x, fastArr, fastArr, fastArr, fastArr);
        fastSeries.setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Line);
        OHLCSeries slowSeries = chart.addSeries("MA " + props.ma().slow(), x, slowArr, slowArr, slowArr, slowArr);
        slowSeries.setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.Line);

        if (props.theme().dark()) {
            chart.getStyler().setChartBackgroundColor(Color.DARK_GRAY);
            chart.getStyler().setPlotBackgroundColor(Color.DARK_GRAY);
            chart.getStyler().setChartFontColor(Color.WHITE);
            chart.getStyler().setAxisTickLabelsColor(Color.WHITE);
            chart.getStyler().setPlotGridLinesColor(Color.GRAY);
            chart.getStyler().setLegendBackgroundColor(Color.DARK_GRAY);
        }

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        return BitmapEncoder.getBufferedImage(chart);
    }
}
