package com.trend.config;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

@Configuration
@RequiredArgsConstructor
public class DbConfig {

    private final AppProperties props;

    @Bean
    DataSource dataSource() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + props.storage().sqlitePath());
        return ds;
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    CommandLineRunner ddl(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS candles (" +
                        "asset TEXT NOT NULL," +
                        "timestamp INTEGER NOT NULL," +
                        "open REAL, high REAL, low REAL, close REAL, volume REAL," +
                        "PRIMARY KEY(asset, timestamp))");
    }
}
