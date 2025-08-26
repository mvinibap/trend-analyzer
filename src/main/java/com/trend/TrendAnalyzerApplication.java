package com.trend;

import com.trend.orchestrator.ReportOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@Log4j2
public class TrendAnalyzerApplication implements CommandLineRunner {

    private final ReportOrchestrator orchestrator;

    public static void main(String[] args) {
        SpringApplication.run(TrendAnalyzerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String asset = args.length > 0 ? args[0] : "ETH";
        log.debug("Running Trend Analyzer for asset {}", asset);
        orchestrator.generate(asset);
    }
}
