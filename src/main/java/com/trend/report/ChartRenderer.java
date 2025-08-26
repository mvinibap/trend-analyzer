package com.trend.report;

import com.trend.model.Candle;
import lombok.extern.log4j.Log4j2;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders price series and moving averages to a BufferedImage using XChart.
 */
@Service
@Log4j2
public class ChartRenderer {

    public BufferedImage render(List<Candle> series, List<Double> fastMa, List<Double> slowMa, int width, int height) {
        log.debug("Rendering chart with {} candles", series.size());
        List<Double> closes = series.stream().map(Candle::close).collect(Collectors.toList());
        List<Long> times = series.stream().map(Candle::timestamp).collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(width)
                .height(height)
                .title("Trend Analyzer")
                .xAxisTitle("Time")
                .yAxisTitle("Price")
                .build();

        chart.addSeries("Close", times, closes);
        chart.addSeries("MA Fast", times, fastMa);
        chart.addSeries("MA Slow", times, slowMa);

        BufferedImage img = BitmapEncoder.getBufferedImage(chart);
        log.debug("Chart rendered");
        return img;
    }
}
