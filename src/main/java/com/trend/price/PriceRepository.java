package com.trend.price;

import com.trend.model.Candle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PriceRepository {

    private final JdbcTemplate jdbc;

    public PriceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void upsertDaily(String asset, List<Candle> candles) {
        for (Candle c : candles) {
            jdbc.update("""
                INSERT INTO candle_d1(asset, t, open, high, low, close, volume)
                VALUES(?,?,?,?,?,?,?)
                ON CONFLICT(asset, t) DO UPDATE SET
                  open=excluded.open,
                  high=excluded.high,
                  low=excluded.low,
                  close=excluded.close,
                  volume=excluded.volume
                """,
                asset, c.timestamp(), c.open(), c.high(), c.low(), c.close(), c.volume());
        }
    }

    public List<Candle> findAll(String asset) {
        return jdbc.query("""
            SELECT t, open, high, low, close, volume FROM candle_d1
            WHERE asset=? ORDER BY t ASC
            """, (rs, i) -> map(rs), asset);
    }

    private Candle map(ResultSet rs) throws SQLException {
        return new Candle(
                rs.getLong("t"),
                rs.getDouble("open"),
                rs.getDouble("high"),
                rs.getDouble("low"),
                rs.getDouble("close"),
                rs.getObject("volume") != null ? rs.getDouble("volume") : null
        );
    }
}
