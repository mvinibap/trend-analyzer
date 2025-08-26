package com.trend.data.sqlite;

import com.trend.core.Candle;
import com.trend.core.CandleStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CandleStoreSqlite implements CandleStore {
    private final String path;

    public CandleStoreSqlite(String path) throws Exception {
        this.path = path;
        Path p = Path.of(path).getParent();
        if (p != null) {
            Files.createDirectories(p);
        }
        try (Connection conn = getConn(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS candles (asset TEXT, ts INTEGER, open REAL, high REAL, low REAL, close REAL, volume REAL, PRIMARY KEY(asset, ts))");
        }
    }

    private Connection getConn() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    @Override
    public void upsertDaily(String asset, List<Candle> candles) throws Exception {
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO candles(asset, ts, open, high, low, close, volume) VALUES(?,?,?,?,?,?,?)")) {
            for (Candle c : candles) {
                ps.setString(1, asset);
                ps.setLong(2, c.timestamp());
                ps.setDouble(3, c.open());
                ps.setDouble(4, c.high());
                ps.setDouble(5, c.low());
                ps.setDouble(6, c.close());
                if (c.volume() != null) ps.setDouble(7, c.volume()); else ps.setNull(7, java.sql.Types.REAL);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<Candle> rangeDaily(String asset, Instant from, Instant to) throws Exception {
        List<Candle> list = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement("SELECT ts, open, high, low, close, volume FROM candles WHERE asset=? AND ts BETWEEN ? AND ? ORDER BY ts")) {
            ps.setString(1, asset);
            ps.setLong(2, from.getEpochSecond());
            ps.setLong(3, to.getEpochSecond());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Candle(
                            rs.getLong(1),
                            rs.getDouble(2),
                            rs.getDouble(3),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getObject(6) == null ? null : rs.getDouble(6)
                    ));
                }
            }
        }
        return list;
    }
}
