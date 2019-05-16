package com.red.generator.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * connection manager
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 10:26
 * @since 1.0.0
 */
@Slf4j
public class ConnectionManager {
    private static final String DB_URL = "jdbc:sqlite:./config/sqlite3.db";

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        File file = new File(DB_URL.substring("jdbc:sqlite:".length())).getAbsoluteFile();
        log.info("database file path :{}", file.getAbsolutePath());
        return DriverManager.getConnection(DB_URL);
    }
}
