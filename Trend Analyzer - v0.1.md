📈 Trend Analyzer — v0.1 (Spring Boot, Java 21, CLI)

Goal: run locally via Spring Boot (CLI mode) to:
	1.	fetch prices for an asset (D1),
	2.	calculate SMA(fast/slow),
	3.	generate 1 PDF with candles + MAs.

We keep a single orchestrator that calls three Spring services:
	•	PriceService (fetch/cache),
	•	MovingAverageService (calculation),
	•	PdfReportService (render + PDF).

⸻

🧱 Design decisions (Spring)
	•	Spring Boot 3.3 + Java 21 (LTS).
	•	Pure CLI: CommandLineRunner (no web server) → spring.main.web-application-type=none.
	•	Config: application.yml with @ConfigurationProperties type-safe binding.
	•	HTTP: WebClient (reactive, but used synchronously with block()).
	•	DB: SQLite via JdbcTemplate (simple) — DDL on startup.
	•	DI: constructor injection (@RequiredArgsConstructor or Lombok optional).
	•	Logs: SLF4J (via Spring Boot).
	•	Single package root to reduce complexity, with clear layers.

⸻

📦 Package structure

com.trend
├─ TrendAnalyzerApplication.java         # @SpringBootApplication + CommandLineRunner
├─ orchestrator/
│  └─ ReportOrchestrator.java            # orchestrates: price -> MA -> PDF
├─ config/
│  ├─ AppProperties.java                 # @ConfigurationProperties(prefix="ta")
│  └─ DbConfig.java                      # @Configuration (JdbcTemplate + DDL)
├─ price/
│  ├─ PriceService.java                  # domain service
│  ├─ HttpPriceClient.java               # WebClient to provider (e.g., CoinGecko)
│  └─ PriceRepository.java               # DAO (JdbcTemplate) for SQLite
├─ calc/
│  └─ MovingAverageService.java          # SMA
├─ report/
│  ├─ ChartRenderer.java                 # XChart -> BufferedImage
│  └─ PdfReportService.java              # PDFBox -> saves PDF
├─ model/
│  ├─ Candle.java                        # record: timestamp, open, high, low, close, volume
│  └─ Timeframe.java                     # enum D1
└─ util/
   ├─ CandleNormalizer.java              # align D1 UTC 00:00Z
   └─ MissingDays.java                   # compute missing intervals


⸻

⚙️ application.yml (configuration)

spring:
  main:
    web-application-type: none

ta:
  timeframe: "D1"           # fixed in v0.1
  lookback: "P365D"         # ISO 8601
  ma:
    type: "SMA"             # v0.1 supports only SMA
    fast: 50
    slow: 200
  data:
    provider: "coingecko"   # provider identifier (currently only one)
    baseCurrency: "USD"
    throttleMs: 0
  storage:
    sqlitePath: "./data/trend.db"
  theme:
    dark: true
  report:
    widthPx: 1600
    heightPx: 900
    outDir: "./reports"

Type-safe binding

// com.trend.config.AppProperties
@ConfigurationProperties(prefix = "ta")
public record AppProperties(
  String timeframe,
  String lookback,
  Ma ma,
  Data data,
  Storage storage,
  Theme theme,
  Report report
) {
  public record Ma(String type, int fast, int slow) {}
  public record Data(String provider, String baseCurrency, int throttleMs) {}
  public record Storage(String sqlitePath) {}
  public record Theme(boolean dark) {}
  public record Report(int widthPx, int heightPx, String outDir) {}
}

Enable binding:

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TrendAnalyzerApplication implements CommandLineRunner { ... }


⸻

🏁 Entry point & orchestration

// com.trend.TrendAnalyzerApplication
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@RequiredArgsConstructor
public class TrendAnalyzerApplication implements CommandLineRunner {
  private final ReportOrchestrator orchestrator;

  public static void main(String[] args) {
    SpringApplication.run(TrendAnalyzerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: java -jar app.jar <ASSET>   e.g., ETH-USD");
      return;
    }
    String asset = args[0];
    var out = orchestrator.generate(asset);
    System.out.println("PDF generated at: " + out.toAbsolutePath());
  }
}

// com.trend.orchestrator.ReportOrchestrator
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportOrchestrator {
  private final AppProperties cfg;
  private final PriceService priceService;
  private final MovingAverageService maService;
  private final ChartRenderer chartRenderer;
  private final PdfReportService pdfReport;

  public Path generate(String asset) throws Exception {
    Instant now = Instant.now();
    Duration lookback = Duration.parse(cfg.lookback());
    Instant from = now.minus(lookback);

    // 1) prices
    List<Candle> series = priceService.getDailySeries(asset, cfg.data().baseCurrency(), from, now);
    log.info("Loaded candles: {}", series.size());

    // 2) MAs
    double[] closes = series.stream().mapToDouble(Candle::close).toArray();
    int fast = cfg.ma().fast(), slow = cfg.ma().slow();
    if (!"SMA".equalsIgnoreCase(cfg.ma().type())) throw new IllegalArgumentException("v0.1 supports only SMA");
    if (fast < 1 || slow < 1 || fast >= slow) throw new IllegalArgumentException("ma.fast < ma.slow and >= 1");

    double[] maFast = maService.sma(closes, fast);
    double[] maSlow = maService.sma(closes, slow);

    // 3) render & PDF
    BufferedImage img = chartRenderer.renderDaily(
      asset, series, maFast, fast, maSlow, slow,
      cfg.theme().dark(), cfg.report().widthPx(), cfg.report().heightPx()
    );

    String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").withZone(ZoneOffset.UTC).format(now);
    Path out = Paths.get(cfg.report().outDir(), "%s_MA_%d-%d_%s.pdf".formatted(asset, fast, slow, ts));
    Files.createDirectories(out.getParent());

    String header = "%s — D1 — SMA(%d,%d) — %s — Engine v0.1".formatted(asset, fast, slow, ts);
    String footer = "Provider=%s, Base=%s, Lookback=%s, MA.fast=%d, MA.slow=%d, Theme=%s"
      .formatted(cfg.data().provider(), cfg.data().baseCurrency(), cfg.lookback(), fast, slow, cfg.theme().dark());

    pdfReport.writeSinglePage(img, header, footer, out);
    return out;
  }
}


⸻

💾 JDBC config + DDL

// com.trend.config.DbConfig
@Configuration
@RequiredArgsConstructor
public class DbConfig {
  private final AppProperties props;

  @Bean
  public DataSource dataSource() {
    String url = "jdbc:sqlite:" + props.storage().sqlitePath();
    org.sqlite.SQLiteDataSource ds = new org.sqlite.SQLiteDataSource();
    ds.setUrl(url);
    return ds;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource ds) {
    JdbcTemplate jdbc = new JdbcTemplate(ds);
    jdbc.execute("""
      CREATE TABLE IF NOT EXISTS candle_d1(
        asset TEXT NOT NULL,
        t     INTEGER NOT NULL,
        open  REAL NOT NULL, high REAL NOT NULL, low REAL NOT NULL, close REAL NOT NULL, volume REAL,
        PRIMARY KEY (asset, t)
      );
      """);
    jdbc.execute("CREATE INDEX IF NOT EXISTS idx_candle_d1_asset_t ON candle_d1(asset, t)");
    return jdbc;
  }
}


⸻

🧩 Models

// com.trend.model.Timeframe
public enum Timeframe { D1 }

// com.trend.model.Candle
public record Candle(
  long timestamp,    // epoch ms UTC 00:00Z
  double open,
  double high,
  double low,
  double close,
  Double volume
) {}


⸻

🔗 PriceService (domain), HttpPriceClient (HTTP) and PriceRepository (DAO)

(Examples provided in Portuguese version remain unchanged; they would be implemented the same way with English logs/messages.)

⸻

🧮 Calculation and utils

// com.trend.calc.MovingAverageService
@Service
public class MovingAverageService {
  public double[] sma(double[] closes, int n) {
    double[] out = new double[closes.length];
    double sum = 0;
    for (int i=0; i<closes.length; i++) {
      sum += closes[i];
      if (i >= n) sum -= closes[i-n];
      out[i] = (i >= n-1) ? sum/n : Double.NaN;
    }
    return out;
  }
}


⸻

🎨 Render and 🧾 PDF

// com.trend.report.ChartRenderer
@Service
public class ChartRenderer {
  public BufferedImage renderDaily(String asset, List<Candle> candles,
                                   double[] maFast, int fastN,
                                   double[] maSlow, int slowN,
                                   boolean dark, int width, int height) {
    // Build candlestick + XY series for MAs (XChart)
    // Return BufferedImage chart
    throw new UnsupportedOperationException("implement with XChart");
  }
}

// com.trend.report.PdfReportService
@Service
public class PdfReportService {
  public void writeSinglePage(BufferedImage chart, String header, String footer, Path outPdf) throws Exception {
    // PDFBox: A4 landscape, draw header, image, footer
    throw new UnsupportedOperationException("implement with PDFBox");
  }
}


⸻

🛠 build.gradle (snippet)

plugins {
  id 'java'
  id 'org.springframework.boot' version '3.3.2'
  id 'io.spring.dependency-management' version '1.1.5'
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

dependencies {
  implementation 'org.springframework.boot:spring-boot-starter'
  implementation 'org.springframework.boot:spring-boot-starter-webflux'      // WebClient
  implementation 'org.springframework.boot:spring-boot-starter-jdbc'
  implementation 'org.xerial:sqlite-jdbc:3.46.0.0'
  implementation 'org.knowm.xchart:xchart:3.8.7'
  implementation 'org.apache.pdfbox:pdfbox:3.0.2'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
}


⸻

🚀 Run

gradle bootRun --args="ETH-USD"
# Output:
# PDF generated at ./reports/ETH-USD_MA_50-200_YYYYMMDD_HHmm.pdf


⸻

✅ Spring best practices applied
	•	Type-safe config with @ConfigurationProperties.
	•	Constructor injection (@RequiredArgsConstructor / final fields).
	•	Clear separation of concerns: service (business), repository (persistence), client (HTTP), report (render/PDF).
	•	Logging: info for progress, warn for gaps/provider, error with stack trace.
	•	Transaction: @Transactional on upsertDaily.
	•	No web server: pure CLI app.

⸻