package com.red.generator.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.red.generator.exception.DbDriverLoadingException;
import com.red.generator.model.DataBaseConfig;
import com.red.generator.model.DbType;
import com.red.generator.model.UITableColumnVO;
import com.red.generator.view.AlertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mybatis.generator.internal.util.ClassloaderUtility;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * db util
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/14     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-14 15:18
 * @since 1.0.0
 */
@Slf4j
public class DbUtil {
    private static final int DB_CONNECTION_TIMEOUTS_SECONDS = 1;
    private static Map<DbType, Driver> driverMap = new HashMap<>();
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static volatile boolean portForwaring = false;
    private static Map<Integer, Session> portForwardingSession = new ConcurrentHashMap<>();

    public static Session getSSHSession(DataBaseConfig dataBaseConfig) {
        if (StringUtils.isBlank(dataBaseConfig.getSshHost()) ||
                StringUtils.isBlank(dataBaseConfig.getSshPort()) || StringUtils
                .isBlank(dataBaseConfig.getSshUser()) || StringUtils.isBlank(dataBaseConfig.getSshPassword())) {
            return null;
        }
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jSch = new JSch();
            Integer sshPort = NumberUtils.createInteger(dataBaseConfig.getPort());
            int port = sshPort == null ? 22 : sshPort;
            session = jSch.getSession(dataBaseConfig.getSshUser(), dataBaseConfig.getSshHost(), port);
            session.setPassword(dataBaseConfig.getSshPassword());
            session.setConfig(config);
        } catch (JSchException e) {
            log.error("{}", e.getCause().getMessage());
        }
        return session;
    }

    public static void engagePortForwarding(Session sshSession, DataBaseConfig config) {
        if (sshSession != null) {
            AtomicInteger atomicInteger = new AtomicInteger();
            Future<?> future = executorService.submit(() -> {
                try {
                    Integer localPort = NumberUtils.createInteger(config.getLport());
                    Integer remotePort = NumberUtils.createInteger(config.getRport());
                    int lport = localPort == null ? Integer.parseInt(config.getPort()) : localPort;
                    int rport = remotePort == null ? Integer.parseInt(config.getPort()) : remotePort;
                    Session session = portForwardingSession.get(lport);
                    if (session != null && session.isConnected()) {
                        String s = session.getPortForwardingL()[0];
                        String[] split = StringUtils.split(s, ":");
                        boolean portForwarding = String
                                .format("%s:%s", split[0], split[1]).equals(lport + ":" + config.getHost());
                        if (portForwarding) {
                            return;
                        }
                    }
                    sshSession.connect();
                    atomicInteger.set(sshSession.setPortForwardingL(lport, config.getHost(), rport));
                    portForwardingSession.put(lport, sshSession);
                    portForwaring = true;
                    log.info("port forwarding enabled, {}", atomicInteger);
                } catch (JSchException e) {
                    log.error("connect over ssh failed", e);
                    if (e.getCause() != null && e.getCause().getMessage().equals("Address already in use: JVM_Bind")) {
                        throw new RuntimeException("Address already in use: JVM_Bind");
                    }
                    throw new RuntimeException(e.getMessage());
                }
            });
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                shutdownPortForwarding(sshSession);
                if (e instanceof TimeoutException) {
                    throw (RuntimeException) e.getCause();
                }
                if (e instanceof TimeoutException) {
                    throw new RuntimeException("OverSSH 连接超时i:超时5秒");
                }
                log.info("executorService isShutdown:{}", executorService.isShutdown());
                AlertUtil.showErrorAlert("OverSSH失败，请检查连接设置:" + e.getMessage());
            }
        }
    }

    public static void shutdownPortForwarding(Session session) {
        portForwaring = false;
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("portFording turn OFF");
        }
    }

    public static Connection getConnection(DataBaseConfig config) throws ClassNotFoundException, SQLException {
        DbType dbType = DbType.valueOf(config.getDbType());
        if (driverMap.get(dbType) == null) {
            loadDbDriver(dbType);
        }
        String url = getConnectionUrlWithSchema(config);
        Properties properties = new Properties();

        properties.setProperty("user", config.getUsername());
        properties.setProperty("password", config.getPassword());

        DriverManager.setLoginTimeout(DB_CONNECTION_TIMEOUTS_SECONDS);
        Connection connection = driverMap.get(dbType).connect(url, properties);
        log.info("get connection, connection url: {}", connection);
        return connection;
    }

    public static List<String> getTableNames(DataBaseConfig dbConfig) throws Exception {
        Session sshSession = getSSHSession(dbConfig);
        engagePortForwarding(sshSession, dbConfig);
        Connection connection = getConnection(dbConfig);

        try {
            List<String> tables = new ArrayList<>();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = null;

            if (DbType.valueOf(dbConfig.getDbType()) == DbType.SQL_Server) {
                String sql = "select name from sysobjects where xtype='u' or xtype='v' order by name";
                rs = connection.createStatement().executeQuery(sql);

                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } else if (DbType.valueOf(dbConfig.getDbType()) == DbType.Oracle) {
                rs = md.getTables(null, dbConfig.getUsername().toUpperCase(), null, new String[]{"TABLE", "VIEW"});
            } else if (DbType.valueOf(dbConfig.getDbType()) == DbType.Sqlite) {
                String sql = "Select name from sqlite_master;";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } else {
                rs = md.getTables(dbConfig.getSchema(), null, "%", new String[]{"TABLE", "VIEW"});
            }
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            if (tables.size() > 1) {
                Collections.sort(tables);
            }
            return tables;
        } finally {
            connection.close();
            shutdownPortForwarding(sshSession);
        }
    }

    public static List<UITableColumnVO> getTableColumns(DataBaseConfig dataBaseConfig, String tableName)
            throws Exception {
        String url = getConnectionUrlWithSchema(dataBaseConfig);
        log.info("get table columns, connection URL: {}", url);
        Session sshSession = getSSHSession(dataBaseConfig);
        engagePortForwarding(sshSession, dataBaseConfig);

        Connection conn = getConnection(dataBaseConfig);

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(dataBaseConfig.getSchema(), null, tableName, null);
            List<UITableColumnVO> columns = new ArrayList<>();

            while (rs.next()) {
                UITableColumnVO columnVO = new UITableColumnVO();
                String columnName = rs.getString("COLUMN_NAME");
                columnVO.setColumnName(columnName);
                columnVO.setJdbcType(rs.getString("TYPE_NAME"));

                columns.add(columnVO);
            }
            return columns;
        } finally {
            conn.close();
            shutdownPortForwarding(sshSession);
        }
    }

    public static String getConnectionUrlWithSchema(DataBaseConfig config) throws ClassNotFoundException {
        DbType dbType = DbType.valueOf(config.getDbType());
        String connectionUrl = String.format(dbType.getConnectionUrlPattern(),
                portForwaring ? "127.0.0.1" : config.getHost(), portForwaring ? config.getLport() : config.getPort(),
                config.getSchema(), config.getEncoding());
        log.info(" get connection url with schema, connection url: {}", connectionUrl);
        return connectionUrl;
    }

    private static void loadDbDriver(DbType dbType) {
        List<String> driverJars = ConfigHelper.getAllJDBCDriverjarPath();
        ClassLoader classLoader = ClassloaderUtility.getCustomClassloader(driverJars);

        try {
            Class clazz = Class.forName(dbType.getDriverClass(), true, classLoader);
            Driver driver = (Driver) clazz.newInstance();
            log.info("load driver class: {}", driver);

            driverMap.put(dbType, driver);
        } catch (Exception e) {
            log.error("load driver error: {}", e.getMessage());
            throw new DbDriverLoadingException("找不到" + dbType.getConnectorJarFile() + "驱动");
        }

    }
}
