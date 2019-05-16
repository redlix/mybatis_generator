package com.red.generator.utils;

import com.alibaba.fastjson.JSON;
import com.red.generator.model.DataBaseConfig;
import com.red.generator.model.DbType;
import com.red.generator.model.GeneratorConfig;
import lombok.extern.slf4j.Slf4j;
import sun.awt.windows.ThemeReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * config helper
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/11     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-11 22:18
 * @since 1.0.0
 */
@Slf4j
public class ConfigHelper {
    private static final String BASE_DIR = "config";
    private static final String CONFIG_FILE = "/sqlite3.db";

    public static void createEmptyFiles() throws Exception {
        File file = new File(BASE_DIR);

        if (!file.exists()) {
            file.mkdir();
        }
        File uiConfigFile = new File(BASE_DIR + CONFIG_FILE);
        if (!uiConfigFile.exists()) {
            createEmptyXMLFile(uiConfigFile);
        }
    }

    private static void createEmptyXMLFile(File uiConfigFile) throws IOException {

        try (InputStream fis = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("sqlite3.db"); FileOutputStream fos = new FileOutputStream(uiConfigFile)) {
            byte[] buffer = new byte[1024];
            int byteread = 0;

            while ((byteread = Objects.requireNonNull(fis).read(buffer)) != 1) {
                fos.write(buffer, 0, byteread);
            }
        }
    }

    public static List<DataBaseConfig> loadDataBaseConfig() throws Exception {

        try (Connection conn = ConnectionManager.getConnection(); Statement stat = conn
                .createStatement(); ResultSet rs = stat.executeQuery("SELECT * FROM dbs")) {
            List<DataBaseConfig> configs = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String value = rs.getString("value");
                DataBaseConfig dataBaseConfig = JSON.parseObject(value, DataBaseConfig.class);
                dataBaseConfig.setId(id);
                configs.add(dataBaseConfig);
            }
            return configs;
        }
    }

    public static void saveDataBaseConfig(boolean isUpdate, Integer primaryKey, DataBaseConfig dataBaseConfig)
            throws Exception {
        String configName = dataBaseConfig.getName();

        try (Connection conn = ConnectionManager.getConnection(); Statement statement = conn.createStatement()) {
            if (!isUpdate) {
                ResultSet rs1 = statement.executeQuery("SELECT * FROM DBS WHERE name = '" + configName + "'");
                if (rs1.next()) {
                    throw new RuntimeException("配置已经存在，请使用其他名字");
                }
            }
            String jsonStr = JSON.toJSONString(dataBaseConfig);
            String sql;
            if (isUpdate) {
                sql = String
                        .format("UPDATE dbs SET name = '%s', value = '%s' WHERE id = %d", configName, jsonStr,
                                primaryKey);
            } else {
                sql = String.format("INSERT INTO dbs (name, value) values('%s', '%s')", configName, jsonStr);
            }
            statement.executeUpdate(sql);
        }
    }

    public static void deleteDataBaseConfig(DataBaseConfig dataBaseConfig) throws Exception {

        try (Connection conn = ConnectionManager.getConnection(); Statement statement = conn.createStatement()) {

            String sql = String.format("DELETE FROM dbs WHERE id = %d", dataBaseConfig.getId());
            statement.executeUpdate(sql);
        }
    }

    public static void saveDataBaseConfig(GeneratorConfig generatorConfig) throws Exception {

        try (Connection conn = ConnectionManager.getConnection();
             Statement statement = conn.createStatement()) {

            String jsonStr = JSON.toJSONString(generatorConfig);
            String sql = String
                    .format("INSERT INTO generator_config values('%s', '%s')", generatorConfig.getName(), jsonStr);
            statement.executeUpdate(sql);
        }
    }

    public static GeneratorConfig loadGeneratorConfig(String name) throws Exception {
        String sql = String.format("SELECT * FROM generator_config WHERE name = '%s'", name);
        log.info("sql :{}", sql);
        try (Connection conn = ConnectionManager.getConnection(); Statement statement = conn
                .createStatement(); ResultSet rs = statement.executeQuery(sql)) {

            GeneratorConfig generatorConfig = null;
            if (rs.next()) {
                String value = rs.getString("value");
                generatorConfig = JSON.parseObject(value, GeneratorConfig.class);
            }
            return generatorConfig;
        }
    }

    public static List<GeneratorConfig> loadGeneratorConfigs() throws Exception {
        String sql = "SELECT * FROM generator_config";
        log.info("sql: {}", sql);
        try (Connection conn = ConnectionManager.getConnection(); Statement statement = conn
                .createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            List<GeneratorConfig> generatorConfigs = new ArrayList<>();

            while (rs.next()) {
                String value = rs.getString("value");
                generatorConfigs.add(JSON.parseObject(value, GeneratorConfig.class));
            }
            return generatorConfigs;
        }
    }

    public static int deleteGeneratorConfig(String name) throws Exception {
        try (Connection conn = ConnectionManager.getConnection(); Statement statement = conn.createStatement()) {
            String sql = String.format("DELETE FROM generator_config where name = '%s'", name);

            return statement.executeUpdate(sql);
        }
    }

    public static String findConnectionLibPath(String dbType) {
        DbType type = DbType.valueOf(dbType);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
        log.info("resource:{}", resource);

        if (resource != null) {
            try {
                File file = new File(resource.toURI().getRawPath() + "/../lib/" + type.getConnectorJarFile());
                return URLDecoder.decode(file.getCanonicalPath(), Charset.forName("UTF-8").displayName());
            } catch (Exception e) {
                throw new RuntimeException("can't find driver file!");
            }
        } else {
            throw new RuntimeException("lib can't find!");
        }
    }

    public static List<String> getAllJDBCDriverjarPath() {
        List<String> jarFilePathList = new ArrayList<>();

        URL url = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
        try {
            File file;

            if (Objects.requireNonNull(url).getPath().contains(".jar")) {
                file = new File("lib/");
            } else {
                file = new File("src/main/resources/lib");
            }

            log.info("jar lib path :{}", file.getCanonicalPath());

            File[] jarFiles = file.listFiles();

            if (jarFiles != null && jarFiles.length > 0) {
                for (File jarFile : jarFiles) {
                    log.info("jar file :{}", jarFile.getCanonicalPath());
                    if (jarFile.isFile() && jarFile.getAbsolutePath().endsWith(".jar")) {
                        jarFilePathList.add(jarFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("can't find driver file!");
        }
        return jarFilePathList;
    }
}
