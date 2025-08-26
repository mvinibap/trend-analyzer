# Trend Analyzer

Simple CLI tool that fetches daily candles, calculates moving averages and generates a single-page PDF report.

## Requirements
- Java 21 (`brew install openjdk@21`)
- Gradle 8.14.3 or newer (`brew install gradle`)
- SQLite (`brew install sqlite`)

## First run
1. Install the dependencies above on macOS using Homebrew.
2. Clone the repository.
3. Run `gradle :modules:app-cli:run --args="ETH-USD"` (replace the symbol as needed). The SQLite DB at `./data/trend.db` and report directory will be created automatically.

## Subsequent runs
- Re-run the command with another asset symbol. Cached data is reused.
- Optionally override MA periods: `TA_MA_FAST=20 TA_MA_SLOW=100 gradle :modules:app-cli:run --args="ETH-USD"`.

Reports are written to `./reports`.
