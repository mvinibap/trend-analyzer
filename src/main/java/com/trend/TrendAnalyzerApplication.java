package com.trend;

import com.trend.orchestrator.ReportOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(com.trend.config.AppProperties.class)
@RequiredArgsConstructor
public class TrendAnalyzerApplication implements CommandLineRunner {

    private final ReportOrchestrator orchestrator;

    public static void main(String[] args) {
        SpringApplication.run(TrendAnalyzerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: <asset>");
            return;
        }
        var pdf = orchestrator.generate(args[0]);
        System.out.println("PDF generated at " + pdf.toAbsolutePath());
    }
}
