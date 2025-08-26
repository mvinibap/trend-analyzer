package com.trend.orchestrator;

import com.trend.calc.MovingAverageService;
import com.trend.config.AppProperties;
import com.trend.model.Candle;
import com.trend.price.PriceService;
import com.trend.report.ChartRenderer;
import com.trend.report.PdfReportService;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportOrchestrator {

    private final PriceService priceService;
    private final MovingAverageService maService;
    private final ChartRenderer renderer;
    private final PdfReportService pdfService;
    private final AppProperties props;

    public ReportOrchestrator(PriceService priceService, MovingAverageService maService,
                              ChartRenderer renderer, PdfReportService pdfService,
                              AppProperties props) {
        this.priceService = priceService;
        this.maService = maService;
        this.renderer = renderer;
        this.pdfService = pdfService;
        this.props = props;
    }

    public void run(String assetArg) throws Exception {
        List<Candle> candles = priceService.loadDaily(assetArg);
        double[] closes = candles.stream().mapToDouble(Candle::close).toArray();
        double[] maFast = maService.sma(closes, props.ma().fast());
        double[] maSlow = maService.sma(closes, props.ma().slow());
        BufferedImage chart = renderer.renderDaily(assetArg, candles, maFast, props.ma().fast(), maSlow, props.ma().slow(),
                props.theme().dark(), props.report().widthPx(), props.report().heightPx());
        String header = assetArg + " MA " + props.ma().fast() + "-" + props.ma().slow();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path outDir = Path.of(props.report().outDir());
        Path outFile = outDir.resolve(assetArg + "_MA_" + props.ma().fast() + "-" + props.ma().slow() + "_" + date + ".pdf");
        pdfService.writeSinglePage(chart, header, "", outFile);
    }
}
