package org.nano.nstitle.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.nano.nstitle.data.db.FileCore;
import org.nano.nstitle.data.db.file.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private HikariDataSource dataSource;
    private final FileCore fileCore;

    private DatabaseManager(FileCore core) {
        this.fileCore = core;
    }

    public static DatabaseManager getInstance(FileCore core){
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager(core);
                }
            }
        }
        return instance;
    }

    public void connect() {
        if (dataSource != null) {
            return;
        }
        if (!isEnabled()) {
            LOGGER.warning("Database is disabled in config.");
            return;
        }
        Config fc = fileCore.getConfig();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(fc.getUrl());
        config.setUsername(fc.getUsername());
        config.setPassword(fc.getPassword());
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(5);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(10000);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
        LOGGER.info("Database connected successfully.");
    }

    public Connection getConnection() throws SQLException {
        connect();
        if (dataSource == null) {
            throw new SQLException("Database is not connected!");
        }
        if ( isEnabled() && dataSource.getConnection() == null ){
            fileCore.getConfig().setEnabled(false);
            System.out.println(" Failed Connect Database. ");
            System.out.println(" 데이터베이스 연결에 실패하여 자동으로 데이터베이스 기능을 종료합니다. ");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            LOGGER.info("Database connection closed.");
        }
    }

    public boolean isEnabled() {
        return fileCore.getConfig().isEnable();
    }
}
