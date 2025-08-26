# Trend Analyzer

Simple CLI tool that fetches daily candles, calculates moving averages and generates a single-page PDF report.

## Requirements
- Java 21
- Gradle 8.14.3 or newer (install via `brew install gradle` on macOS)
- SQLite (first run on macOS: `brew install sqlite`)

## First run
1. Install dependencies above.
2. Clone the repository.
3. Run `gradle :modules:app-cli:run --args="bitcoin-USD"` (replace asset symbol). The SQLite DB will be created at `./data/trend.db`.

## Subsequent runs
- Re-run the command with another asset symbol. Cached data is reused.

Reports are written to `./reports`.
