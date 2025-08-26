# 📈 Trend Analyzer — v0.1 (Local, Java 21, CLI)

Repositório para o **Trend Analyzer**, um serviço em **Java 21** que realiza **análise gráfica simples** de ativos financeiros.
Na **v0.1**, o sistema roda **localmente** via **CLI** e gera um **PDF** com **candles diários** e **duas médias móveis (MA)**.

---

## ✨ Funcionalidades

* **Entrada (CLI)**: símbolo do ativo (ex.: `ETH-USD`).
* **Configuração**: parâmetros em `config/config.yaml` (SMA fast/slow, lookback, tema, dimensões, provedor de dados).
* **Dados de preço**: baixa via HTTP (ex.: CoinGecko) e **cacheia em SQLite** local (`./data/trend.db`).
* **Processamento**: calcula **SMA(fast)** e **SMA(slow)**; renderiza candles + MAs com **XChart**.
* **Saída**: PDF **1 página** salvo em `./reports/{ASSET}_MA_{FAST}-{SLOW}_{YYYYMMDD_HHmm}.pdf`.

---

## 🧱 Decisões de Design

* **Arquitetura**: hexagonal leve (ports/adapters) para facilitar troca de fontes/armazenamentos.

  * `CandleSource` (porta) ← `CandleSourceHttp` (adapter HTTP)
  * `CandleStore` (porta) ← `CandleStoreSqlite` (adapter local)
  * `ChartRenderer` ← `XChartRenderer`
  * `ReportComposer` ← `PdfBoxComposer`
  * Caso de uso: `GenerateMaReport`
* **Frameworks / libs**:

  * CLI: **Picocli**
  * Config: **Jackson YAML**
  * HTTP: `java.net.http.HttpClient` (JDK)
  * DB local: **SQLite** (driver `org.xerial:sqlite-jdbc`)
  * Gráficos: **XChart**
  * PDF: **Apache PDFBox**
  * Logging (v0.1): **SLF4J API + slf4j-simple** (leve e suficiente)
* **Timezone**: **UTC**. Velas **D1** alinhadas a **00:00:00Z**.
* **Sem Spring** na v0.1 (inicialização rápida, menor binário). Futuro: módulo opcional `api-spring` sem alterar `core`.

---

## 📂 Estrutura do Projeto

```
modules/
  core/              # Domínio, contratos, use case, MovingAverageService, validação
  data-http/         # Fonte de candles via HTTP
  data-sqlite/       # Cache local em SQLite
  render-xchart/     # Renderização do gráfico em imagem
  report-pdfbox/     # Composição em PDF (1 página)
  app-cli/           # CLI (picocli) + Wire (composition root)
config/
  config.yaml        # Parâmetros v0.1
data/
  trend.db           # SQLite (auto-criado)
reports/
  *.pdf              # Relatórios gerados
```

**Pacotes sugeridos**

```
com.trend.core.*
com.trend.data.http.*
com.trend.data.sqlite.*
com.trend.render.*
com.trend.report.*
com.trend.app.*
```

---

## ⚙️ Configuração (`config/config.yaml`)

```yaml
timeframe: "D1"            # fixo na v0.1
lookback: "P365D"          # ISO 8601 (ex.: 365 dias)

ma:
  type: "SMA"              # v0.1 implementa somente SMA
  fast: 50
  slow: 200

data:
  provider: "coingecko"
  baseCurrency: "USD"      # ETH-USD => asset=ETH, base=USD
  throttleMs: 0

storage:
  sqlitePath: "./data/trend.db"

theme:
  dark: true

report:
  widthPx: 1600
  heightPx: 900
  outDir: "./reports"
```

> Dica: Você pode expor overrides por variáveis de ambiente (ex.: `TA_MA_FAST`, `TA_MA_SLOW`) se desejar.

---

## 🧩 Interfaces principais (ports)

```java
// modules/core
public enum Timeframe { D1 }

public record Candle(long timestamp, double open, double high, double low, double close, Double volume) {}

public interface CandleSource {
  java.util.List<Candle> fetchDaily(String asset, String baseCurrency,
                                    java.time.Instant from, java.time.Instant to) throws Exception;
}

public interface CandleStore {
  void upsertDaily(String asset, java.util.List<Candle> candles) throws Exception;
  java.util.List<Candle> rangeDaily(String asset, java.time.Instant from, java.time.Instant to) throws Exception;
}

public interface ChartRenderer {
  java.awt.image.BufferedImage renderDaily(String asset,
      java.util.List<Candle> candles,
      double[] maFast, int fastN,
      double[] maSlow, int slowN,
      boolean darkTheme,
      int widthPx, int heightPx);
}

public interface ReportComposer {
  void composeSinglePage(java.awt.image.BufferedImage chart,
                         String header, String footer,
                         java.nio.file.Path outPdf) throws Exception;
}
```

---

## 🧮 Lógica de Negócio

```java
// modules/core
public final class MovingAverageService {
  public static double[] sma(double[] closes, int period) {
    double[] out = new double[closes.length];
    double sum = 0;
    for (int i=0; i<closes.length; i++) {
      sum += closes[i];
      if (i >= period) sum -= closes[i - period];
      out[i] = (i >= period - 1) ? sum/period : Double.NaN;
    }
    return out;
  }
}
```

---

## 🚀 Como executar

```bash
./gradlew :modules:app-cli:run --args="ETH-USD"
```

Saída esperada em `./reports/`:

```
./reports/ETH-USD_MA_50-200_YYYYMMDD_HHmm.pdf
```

---

## 🗺 Roadmap

* **v0.2**: execução em **AWS Lambda** (cache em **S3**).
* **v0.3**: alternativa **DynamoDB** como `CandleStore`.
* **v0.4**: múltiplos timeframes no mesmo PDF.
* **v0.5**: novos analyzers (resistências, canais, projeções) via SPI.
* **v1.0**: API/UI na nuvem, agendamento EventBridge.

```
```
