# Trend Analyzer

CLI tool to fetch daily prices, calculate simple moving averages and generate a PDF report with a candlestick chart.

## Requirements
- Java 21
- Local Gradle installation
- SQLite installed (macOS: `brew install sqlite`)

## First run
```bash
gradle bootRun --args="BTC-USD"
```
- The SQLite database file will be created at `./data/trend.db`.
- A PDF will be generated in `./reports`.

## Subsequent runs
Repeat the command above. Existing data is reused and updated when needed.

To reset from scratch, delete `./data/trend.db`.

## Configuration
Lookback period, moving averages, theme and output directories can be adjusted in `src/main/resources/application.yml`.
