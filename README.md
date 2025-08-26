# Trend Analyzer

CLI tool to fetch daily prices, calculate simple moving averages and generate a PDF report with candlestick chart.

## Requisitos
- Java 21
- Gradle instalado localmente
- SQLite disponível (macOS: `brew install sqlite`)

## Primeira execução
```bash
gradle bootRun --args="BTC-USD"
```
- O arquivo de banco de dados SQLite será criado em `./data/trend.db`.
- Um PDF será gerado em `./reports`.

## Execuções posteriores
Somente repita o comando acima. Os dados existentes são reutilizados e atualizados quando necessário.

Para reiniciar do zero, apague `./data/trend.db`.

## Configuração
Parâmetros de lookback, médias móveis, tema e diretórios de saída podem ser ajustados em `src/main/resources/application.yml`.
