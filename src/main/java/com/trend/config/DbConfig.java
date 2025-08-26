package com.trend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

@Configuration
public class DbConfig {

    @Bean
    public DataSource dataSource(AppProperties props) throws Exception {
        Path dbPath = Path.of(props.storage().sqlitePath()).toAbsolutePath();
        Files.createDirectories(dbPath.getParent());
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + dbPath);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS candle_d1 (
              asset TEXT NOT NULL,
              t INTEGER NOT NULL,
              open REAL NOT NULL,
              high REAL NOT NULL,
              low REAL NOT NULL,
              close REAL NOT NULL,
              volume REAL,
              PRIMARY KEY(asset, t)
            );
            """);
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_candle_d1_asset_t ON candle_d1(asset, t)");
        return jdbc;
    }
}
