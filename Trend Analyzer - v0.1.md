📈 Trend Analyzer — v0.1 (Spring Boot, Java 21, CLI)

Goal: run locally using Spring Boot (CLI mode), to:
	1.	fetch prices of an asset,
	2.	calculate SMA(fast/slow),
	3.	generate a PDF with candles + MAs.

We keep a single orchestrator that calls three Spring services:
	•	PriceService (fetch/cache),
	•	MovingAverageService (calculation),
	•	PdfReportService (render + PDF).

⸻

🧱 Design decisions (Spring)
	•	Spring Boot 3.3 + Java 21 (LTS).
	•	CLI only: CommandLineRunner (no web server) → spring.main.web-application-type=none.
	•	Config: application.yml with @ConfigurationProperties (type-safe).
	•	HTTP: WebClient (reactive, but used synchronously with block()).
	•	DB: SQLite via JdbcTemplate (simple) — DDL at startup.
	•	DI: constructor injection (@RequiredArgsConstructor or optional Lombok).
	•	Logs: SLF4J (via Spring Boot).
	•	Single package to reduce complexity, while keeping clear layers.

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
│  ├─ HttpPriceClient.java               # WebClient for provider (CoinGecko/Binance)
│  └─ PriceRepository.java               # DAO (JdbcTemplate) for SQLite
├─ calc/
│  └─ MovingAverageService.java          # SMA
├─ report/
│  ├─ ChartRenderer.java                 # XChart -> BufferedImage
│  └─ PdfReportService.java              # PDFBox -> save PDF
├─ model/
│  ├─ Candle.java                        # record: timestamp, open, high, low, close, volume
│  └─ Timeframe.java                     # enum D1
└─ util/
   ├─ CandleNormalizer.java              # aligns candles to UTC boundaries
   └─ MissingDays.java                   # computes missing intervals


⸻

⚙️ application.yml (configuration)

spring:
  main:
    web-application-type: none

ta:
  lookback: "P365D"         # ISO 8601 duration
  ma:
    type: "SMA"             # v0.1 only SMA
    fast: 50
    slow: 200
  data:
    provider: "binance"     # can be "coingecko" or "binance"
    baseCurrency: "USDT"     # Binance example: ETHUSDT
    interval: "1d"          # Binance candle interval (1m, 5m, 15m, 1h, 4h, 1d, 1w, 1M)
    throttleMs: 0
  storage:
    sqlitePath: "./data/trend.db"
  theme:
    dark: true
  report:
    widthPx: 1600
    heightPx: 900
    outDir: "./reports"


⸻

🔗 Binance Kline API (candlesticks)

Endpoint:

GET https://api.binance.com/api/v3/klines?symbol=ETHUSDT&interval=1d&limit=100

	•	symbol=ETHUSDT → trading pair.
	•	interval=1d → configurable candle size (supported: 1m, 5m, 15m, 1h, 4h, 1d, 1w, 1M).
	•	limit=100 → number of candles (max 1000).

Response (example, each array = 1 candle):

[
  [
    1737744000000,    // 0: Open time (ms since epoch)
    "2421.10",        // 1: Open price
    "2430.55",        // 2: High price
    "2415.00",        // 3: Low price
    "2420.50",        // 4: Close price
    "1200.45",        // 5: Volume (base asset, ETH)
    1737747599999,    // 6: Close time
    "2900000.00",     // 7: Quote asset volume (USDT)
    3456,             // 8: Number of trades
    "600.30",         // 9: Taker buy base asset volume
    "1450000.00",     // 10: Taker buy quote asset volume
    "0"               // 11: Ignore
  ]
]

👉 For our Candle model we only need:
	•	timestamp = 0 (open time)
	•	open = 1
	•	high = 2
	•	low = 3
	•	close = 4
	•	volume = 5

⸻

🧩 Implementation in HttpPriceClient

// com.trend.price.HttpPriceClient
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpPriceClient {
  private final AppProperties props;
  private final WebClient webClient = WebClient.builder()
      .baseUrl("https://api.binance.com")
      .build();

  public List<Candle> fetchCandles(String asset, String base, Instant from, Instant to, int throttleMs) {
    String symbol = asset.toUpperCase() + base.toUpperCase(); // e.g. ETH + USDT = ETHUSDT
    String interval = props.data().interval();
    String url = "/api/v3/klines?symbol=" + symbol + "&interval=" + interval + "&limit=1000";

    List<List<Object>> raw = webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<List<Object>>>() {})
        .block();

    List<Candle> out = new ArrayList<>();
    for (List<Object> arr : raw) {
      long openTime = ((Number) arr.get(0)).longValue();
      double open = Double.parseDouble(arr.get(1).toString());
      double high = Double.parseDouble(arr.get(2).toString());
      double low = Double.parseDouble(arr.get(3).toString());
      double close = Double.parseDouble(arr.get(4).toString());
      double volume = Double.parseDouble(arr.get(5).toString());
      out.add(new Candle(openTime, open, high, low, close, volume));
    }

    if (throttleMs > 0) {
      try { Thread.sleep(throttleMs); } catch (InterruptedException ignored) {}
    }

    return out;
  }
}

Notes:
	•	interval is now configurable via application.yml (default 1d).
	•	Binance supports granular intervals from 1m up to 1M.
	•	Extra fields from Binance response are ignored.

⸻

🚀 Run

./gradlew bootRun --args="ETH"
# Output:
# PDF generated at ./reports/ETH_MA_50-200_YYYYMMDD_HHmm.pdf


⸻

✅ Why Binance API
	•	Free, no API key required.
	•	Flexible intervals (1m to 1M).
	•	Provides volume, which CoinGecko does not.
	•	Stable and widely used.