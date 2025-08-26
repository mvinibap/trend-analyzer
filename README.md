# Trend Analyzer

CLI em Spring Boot para buscar dados de preço na Binance, calcular médias móveis simples e gerar um PDF com o gráfico.

## Requisitos
- Java 21
- Gradle 8+

## Como executar
1. Ajuste os parâmetros desejados em `src/main/resources/application.yml`.
2. Rode a aplicação informando o ativo (default `ETH`):
   ```bash
   gradle bootRun --args="BTC"
   ```
   O PDF será salvo em `./reports` com o nome `<ATIVO>-<timestamp>.pdf`.

Os logs utilizam Log4j2 com nível **DEBUG** por padrão para facilitar o debug.
