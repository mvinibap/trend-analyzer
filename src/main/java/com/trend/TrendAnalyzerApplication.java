package com.trend;

import com.trend.config.AppProperties;
import com.trend.orchestrator.ReportOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TrendAnalyzerApplication implements CommandLineRunner {

    private final ReportOrchestrator orchestrator;

    public TrendAnalyzerApplication(ReportOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public static void main(String[] args) {
        SpringApplication.run(TrendAnalyzerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: <ASSET-BASE>");
            return;
        }
        orchestrator.run(args[0]);
    }
}
