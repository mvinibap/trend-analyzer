package com.trend.price;

import com.trend.model.Candle;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PriceRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<Candle> findCandles(String asset, long from, long to) {
        return jdbcTemplate.query(
                "select timestamp, open, high, low, close, volume from candles where asset = ? and timestamp between ? and ? order by timestamp",
                (rs, i) -> new Candle(
                        rs.getLong("timestamp"),
                        rs.getDouble("open"),
                        rs.getDouble("high"),
                        rs.getDouble("low"),
                        rs.getDouble("close"),
                        rs.getDouble("volume")),
                asset, from, to);
    }

    public void saveAll(String asset, List<Candle> candles) {
        jdbcTemplate.batchUpdate(
                "insert or replace into candles(asset, timestamp, open, high, low, close, volume) values(?,?,?,?,?,?,?)",
                candles,
                candles.size(),
                (ps, c) -> {
                    ps.setString(1, asset);
                    ps.setLong(2, c.timestamp());
                    ps.setDouble(3, c.open());
                    ps.setDouble(4, c.high());
                    ps.setDouble(5, c.low());
                    ps.setDouble(6, c.close());
                    ps.setDouble(7, c.volume());
                });
    }
}
