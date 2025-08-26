# Trend Analyzer

Ferramenta CLI em Spring Boot para buscar preços de ativos na Binance, calcular médias móveis simples e gerar um relatório em PDF com gráfico de candles.

## Requisitos

- Java 21
- [SQLite](https://www.sqlite.org/) (`brew install sqlite` no macOS)
- [Gradle](https://gradle.org/) (`brew install gradle` no macOS)

## Primeira execução

1. Clone o repositório.
2. Instale o SQLite (apenas uma vez).
3. Gere o PDF para um ativo (exemplo: ETH):

```bash
gradle bootRun --args="ETH"
```

O banco de dados será criado em `./data/trend.db` e o relatório em `./reports`.

## Execuções subsequentes

Basta executar novamente o comando acima. O banco e os relatórios existentes serão reutilizados.

## Build

Para compilar o projeto:

```bash
gradle build
```

Nenhum artefato binário gerado (PDFs, banco de dados) é versionado no repositório.

