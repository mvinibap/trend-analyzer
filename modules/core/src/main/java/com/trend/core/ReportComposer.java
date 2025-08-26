package com.trend.core;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface ReportComposer {
    void composeSinglePage(BufferedImage chart,
                           String header,
                           String footer,
                           Path outPdf) throws Exception;
}
