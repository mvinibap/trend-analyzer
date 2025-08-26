# 📈 Trend Analyzer — v0.1 (Spring Boot, Java 21, CLI)

**Goal**: run **locally** using **Spring Boot** (CLI mode), to:

1. **fetch prices** of an asset directly from Binance API,
2. **calculate SMA(fast/slow)**,
3. **generate a PDF** with candles + MAs.

We keep a **single orchestrator** that calls three Spring services:

* `PriceService` (fetch from Binance),
* `MovingAverageService` (calculation),
* `PdfReportService` (render + PDF).

---

## 🧱 Design decisions (Spring)

* **Spring Boot 3.3 + Java 21** (LTS).
* **CLI only**: `CommandLineRunner` (no web server) → `spring.main.web-application-type=none`.
* **Config**: `application.yml` with **@ConfigurationProperties** (type-safe).
* **HTTP**: `WebClient` for calling Binance REST API.
* **No local DB**: every run fetches fresh data from Binance.
* **DI**: **constructor injection** (`@RequiredArgsConstructor` or optional Lombok).
* **Logs**: Log4j2 (Spring Boot starter logging overridden).
* **Single package** to reduce complexity, while keeping **clear layers**.

---

## 📦 Package structure

```
com.trend
├─ TrendAnalyzerApplication.java         # @SpringBootApplication + CommandLineRunner
├─ orchestrator/
│  └─ ReportOrchestrator.java            # orchestrates: price -> MA -> PDF
├─ config/
│  └─ AppProperties.java                 # @ConfigurationProperties(prefix="ta")
├─ price/
│  ├─ PriceService.java                  # domain service
│  └─ HttpPriceClient.java               # WebClient for Binance
├─ calc/
│  └─ MovingAverageService.java          # SMA
├─ report/
│  ├─ ChartRenderer.java                 # XChart -> BufferedImage
│  └─ PdfReportService.java              # PDFBox -> save PDF
├─ model/
│  ├─ Candle.java                        # record: timestamp, open, high, low, close, volume
│  └─ Timeframe.java                     # enum D1
```

---

## ⚙️ application.yml (configuration)

```yaml
spring:
  main:
    web-application-type: none

logging:
  config: classpath:log4j2.xml

ta:
  timeframe: "D1"           # fixed in v0.1
  lookback: "P365D"         # ISO 8601
  ma:
    type: "SMA"             # v0.1 only SMA
    fast: 50
    slow: 200
  data:
    provider: "binance"
    baseCurrency: "USDT"     # used for Binance pairs like ETHUSDT
    interval: "4h"           # Binance kline interval (e.g., 1h, 4h, 1d)
    limit: 1000              # number of candles to fetch
    throttleMs: 0
  theme:
    dark: true
  report:
    widthPx: 1600
    heightPx: 900
    outDir: "./reports"
```

**Type-safe binding (AppProperties)**

```java
// com.trend.config.AppProperties
@ConfigurationProperties(prefix = "ta")
public record AppProperties(
  String lookback,
  Ma ma,
  Data data,
  Theme theme,
  Report report
) {
  public record Ma(String type, int fast, int slow) {}
  public record Data(String provider, String baseCurrency, String interval, int limit, int throttleMs) {}
  public record Theme(boolean dark) {}
  public record Report(int widthPx, int heightPx, String outDir) {}
}
```

---

## 🔗 Binance Kline API (candlesticks)

**Endpoint**:

```
https://api.binance.com/api/v3/klines?symbol=ETHUSDT&interval=4h&limit=1000
```

---

## 🧩 Implementation in `HttpPriceClient`

```java
// com.trend.price.HttpPriceClient
@Service
@RequiredArgsConstructor
@Log4j2
public class HttpPriceClient {
  private final AppProperties props;
  private final WebClient webClient = WebClient.builder()
      .baseUrl("https://api.binance.com")
      .build();

  public List<Candle> fetchCandles(String asset) {
    String symbol = asset.toUpperCase() + props.data().baseCurrency().toUpperCase();
    String interval = Optional.ofNullable(props.data().interval()).orElse("1d");
    int limit = props.data().limit();

    String url = String.format("/api/v3/klines?symbol=%s&interval=%s&limit=%d", symbol, interval, limit);

    log.info("Fetching {} candles for {} with interval {}", limit, symbol, interval);

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

    log.info("Fetched {} candles", out.size());
    return out;
  }
}
```

---

## 🧩 PriceService

```java
// com.trend.price.PriceService
@Service
@RequiredArgsConstructor
@Log4j2
public class PriceService {
  private final HttpPriceClient client;

  public List<Candle> getSeries(String asset) {
    log.info("Retrieving series for {}", asset);
    return client.fetchCandles(asset);
  }
}
```

---

## 🧩 Orchestrator

```java
// com.trend.orchestrator.ReportOrchestrator
@Service
@RequiredArgsConstructor
@Log4j2
public class ReportOrchestrator {
  private final AppProperties cfg;
  private final PriceService priceService;
  private final MovingAverageService maService;
  private final ChartRenderer chartRenderer;
  private final PdfReportService pdfReport;

  public Path generate(String asset) throws Exception {
    List<Candle> series = priceService.getSeries(asset);
    log.info("Series length: {}", series.size());
    // ... rest of the pipeline
    return null;
  }
}
```

---

## 🛠 build.gradle (snippet)

```gradle
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter'
  implementation 'org.springframework.boot:spring-boot-starter-webflux'
  implementation 'org.knowm.xchart:xchart:3.8.7'
  implementation 'org.apache.pdfbox:pdfbox:3.0.2'

  // Logging with Log4j2
  implementation 'org.springframework.boot:spring-boot-starter-log4j2'

  testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## ✅ Logging with Log4j2

* Added dependency `spring-boot-starter-log4j2`.
* Annotated services with `@Log4j2` (Lombok).
* Configured logging with `log4j2.xml` (to be added under `src/main/resources`).
* Removed default Spring Boot logging (Logback) by overriding starter.
