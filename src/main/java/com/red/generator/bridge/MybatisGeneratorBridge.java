package com.red.generator.bridge;

import com.red.generator.model.DataBaseConfig;
import com.red.generator.model.DbType;
import com.red.generator.model.GeneratorConfig;
import com.red.generator.plugins.DbRemarksCommentGenerator;
import com.red.generator.utils.ConfigHelper;
import com.red.generator.utils.DbUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * mybatis generator bridge
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/14     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-14 11:33
 * @since 1.0.0
 */
@Slf4j
@Data
public class MybatisGeneratorBridge {
    private GeneratorConfig generatorConfig;
    private DataBaseConfig dataBaseConfig;
    private ProgressCallback progressCallBack;
    private List<IgnoredColumn> ignoredColumns;
    private List<ColumnOverride> columnOverrides;

    public void generate() throws Exception {
        Configuration configuration = new Configuration();
        Context context = new Context(ModelType.CONDITIONAL);
        configuration.addContext(context);

        context.addProperty("javaFileEncoding", "UTF-8");

        String dbType = dataBaseConfig.getDbType();
        String connectorLibPath = ConfigHelper.findConnectionLibPath(dbType);

        log.info("connectorLibPath: {}", connectorLibPath);

        configuration.addClasspathEntry(connectorLibPath);
        //Table configuration
        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(generatorConfig.getTableName());
        tableConfiguration.setDomainObjectName(generatorConfig.getDomainObjectName());

        if (!generatorConfig.isUseExample()) {
            tableConfiguration.setUpdateByExampleStatementEnabled(false);
            tableConfiguration.setCountByExampleStatementEnabled(false);
            tableConfiguration.setDeleteByExampleStatementEnabled(false);
            tableConfiguration.setSelectByExampleStatementEnabled(false);
        }

        context.addProperty("autoDelimitKeywords", "true");

        if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)) {
            tableConfiguration.setSchema(dataBaseConfig.getSchema());
            // 由于beginningDelimiter和endingDelimiter的默认值为双引号(")，在Mysql中不能这么写，所以还要将这两个默认值改为`
            context.addProperty("beginningDelimiter", "`");
            context.addProperty("endingDelimiter", "`");
        } else {
            tableConfiguration.setCatalog(dataBaseConfig.getSchema());
        }
        if (generatorConfig.isUseSchemaPrefix()) {
            if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)) {
                tableConfiguration.setSchema(dataBaseConfig.getSchema());
            } else if (DbType.Oracle.name().equals(dbType)) {
                //Oracle的schema为用户名，如果连接用户拥有dba等高级权限，若不设schema，会导致把其他用户下同名的表也生成一遍导致mapper中代码重复
                tableConfiguration.setSchema(dataBaseConfig.getUsername());
            } else {
                tableConfiguration.setCatalog(dataBaseConfig.getSchema());
            }
        }
        //针对postgresql单独配置
        if (DbType.PostgreSQL.name().equals(dbType)) {
            tableConfiguration.setDelimitIdentifiers(true);
        }

        //添加GeneratedKey生成主键
        if (StringUtils.isNotEmpty(generatorConfig.getGenerateKeys())) {
            String dbType2 = dbType;
            if (DbType.MySQL.name().equals(dbType2) || DbType.MySQL_8.name().equals(dbType2)) {
                dbType2 = "JDBC";
                //dbType为JDBC，且配置中开启useGeneratedKeys时，Mybatis会使用Jdbc3KeyGenerator,
                //使用该KeyGenerator的好处就是直接在一次INSERT 语句内，通过resultSet获取得到 生成的主键值，
                //并很好的支持设置了读写分离代理的数据库
                //例如阿里云RDS + 读写分离代理
                //无需指定主库
                //当使用SelectKey时，Mybatis会使用SelectKeyGenerator，INSERT之后，多发送一次查询语句，获得主键值
                //在上述读写分离被代理的情况下，会得不到正确的主键
            }
            tableConfiguration
                    .setGeneratedKey(new GeneratedKey(generatorConfig.getGenerateKeys(), dbType2, true, null));
        }
        if (generatorConfig.getMapperName() != null) {
            tableConfiguration.setMapperName(generatorConfig.getMapperName());
        }
        // add ignore columns
        if (ignoredColumns != null) {
            ignoredColumns.forEach(tableConfiguration::addIgnoredColumn);
        }
        if (columnOverrides != null) {
            columnOverrides.forEach(tableConfiguration::addColumnOverride);
        }
        if (generatorConfig.isUseActualColumnNames()) {
            tableConfiguration.addProperty("useActualColumnNames", "true");
        }
        if (generatorConfig.isUseTableNameAlias()) {
            tableConfiguration.setAlias(generatorConfig.getTableName());
        }

        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
        if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)) {
            jdbcConfig.addProperty("nullCatalogMeansCurrent", "true");
        }
        jdbcConfig.setDriverClass(DbType.valueOf(dbType).getDriverClass());
        jdbcConfig.setConnectionURL(DbUtil.getConnectionUrlWithSchema(dataBaseConfig));
        jdbcConfig.setUserId(dataBaseConfig.getUsername());
        jdbcConfig.setPassword(dataBaseConfig.getPassword());
        if (DbType.Oracle.name().equals(dbType)) {
            jdbcConfig.getProperties().setProperty("remarkReporting", "true");
        }
        // java model
        JavaModelGeneratorConfiguration modelConfig = new JavaModelGeneratorConfiguration();
        modelConfig.setTargetPackage(generatorConfig.getModelPackage());
        modelConfig.setTargetProject(
                generatorConfig.getProjectFolder() + "/" + generatorConfig.getModelPackageTargetFolder());
        // mapper configuration
        SqlMapGeneratorConfiguration mapperConfig = new SqlMapGeneratorConfiguration();
        mapperConfig.setTargetPackage(generatorConfig.getMappingXMLPackage());
        mapperConfig.setTargetProject(
                generatorConfig.getProjectFolder() + "/" + generatorConfig.getMappingXMLTargetFolder());
        // DAO
        JavaClientGeneratorConfiguration daoConfig = new JavaClientGeneratorConfiguration();
        daoConfig.setConfigurationType("XMLMAPPER");
        daoConfig.setTargetPackage(getGeneratorConfig().getDaoPackage());
        daoConfig.setTargetProject(generatorConfig.getProjectFolder() + "/" + generatorConfig.getDaoTargetFolder());

        context.setId("myId");
        context.addTableConfiguration(tableConfiguration);
        context.setJdbcConnectionConfiguration(jdbcConfig);
        context.setJavaModelGeneratorConfiguration(modelConfig);
        context.setSqlMapGeneratorConfiguration(mapperConfig);
        context.setJavaClientGeneratorConfiguration(daoConfig);
        // Comment
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(DbRemarksCommentGenerator.class.getName());
        if (generatorConfig.isComment()) {
            commentConfig.addProperty("columnRemarks", "true");
        }
        context.setCommentGeneratorConfiguration(commentConfig);
        // set java file encoding
        context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, generatorConfig.getEncoding());

        // 实体序列化
        PluginConfiguration serializablePluginConfiguration = new PluginConfiguration();
        serializablePluginConfiguration.addProperty("type", "org.mybatis.generator.plugins.SerializablePlugin");
        serializablePluginConfiguration.setConfigurationType("org.mybatis.generator.plugins.SerializablePlugin");
        context.addPluginConfiguration(serializablePluginConfiguration);
        // toString, hashCode, equals插件
        if (generatorConfig.isNeedToStringHashcodeEquals()) {
            PluginConfiguration pluginConfiguration1 = new PluginConfiguration();
            pluginConfiguration1.addProperty("type", "org.mybatis.generator.plugins.EqualsHashCodePlugin");
            pluginConfiguration1.setConfigurationType("org.mybatis.generator.plugins.EqualsHashCodePlugin");
            context.addPluginConfiguration(pluginConfiguration1);
            PluginConfiguration pluginConfiguration2 = new PluginConfiguration();
            pluginConfiguration2.addProperty("type", "org.mybatis.generator.plugins.ToStringPlugin");
            pluginConfiguration2.setConfigurationType("org.mybatis.generator.plugins.ToStringPlugin");
            context.addPluginConfiguration(pluginConfiguration2);
        }
        // limit/offset插件
        if (generatorConfig.isOffsetLimit()) {
            if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)
                    || DbType.PostgreSQL.name().equals(dbType)) {
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.addProperty("type", "com.red.generator.plugins.MySQLLimitPlugin");
                pluginConfiguration.setConfigurationType("com.red.generator.plugins.MySQLLimitPlugin");
                context.addPluginConfiguration(pluginConfiguration);
            }
        }
        //for JSR310
        if (generatorConfig.isJsr310Support()) {
            JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
            javaTypeResolverConfiguration.setConfigurationType("com.red.generator.plugins.JavaTypeResolverJsr310Impl");
            context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);
        }
        //forUpdate 插件
        if (generatorConfig.isNeedForUpdate()) {
            if (DbType.MySQL.name().equals(dbType)
                    || DbType.PostgreSQL.name().equals(dbType)) {
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.addProperty("type", "com.red.generator.plugins.MySQLForUpdatePlugin");
                pluginConfiguration.setConfigurationType("com.red.generator.plugins.MySQLForUpdatePlugin");
                context.addPluginConfiguration(pluginConfiguration);
            }
        }
        //repository 插件
        if (generatorConfig.isAnnotationDAO()) {
            if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)
                    || DbType.PostgreSQL.name().equals(dbType)) {
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.addProperty("type", "com.red.generator.plugins.RepositoryPlugin");
                pluginConfiguration.setConfigurationType("com.red.generator.plugins.RepositoryPlugin");
                context.addPluginConfiguration(pluginConfiguration);
            }
        }
        if (generatorConfig.isUseDAOExtendStyle()) {
            if (DbType.MySQL.name().equals(dbType) || DbType.MySQL_8.name().equals(dbType)
                    || DbType.PostgreSQL.name().equals(dbType)) {
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                pluginConfiguration.addProperty("useExample", String.valueOf(generatorConfig.isUseExample()));
                pluginConfiguration.addProperty("type", "com.red.generator.plugins.CommonDAOInterfacePlugin");
                pluginConfiguration.setConfigurationType("com.red.generator.plugins.CommonDAOInterfacePlugin");
                context.addPluginConfiguration(pluginConfiguration);
            }
        }

        context.setTargetRuntime("MyBatis3");
        List<String> warnings = new ArrayList<>();
        Set<String> fullyQualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();
        ShellCallback shellCallback = new DefaultShellCallback(true); // override=true
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        // if overrideXML selected, delete oldXML ang generate new one
        if (generatorConfig.isOverrideXML()) {
            String mappingXMLFilePath = getMappingXMLFilePath(generatorConfig);
            File mappingXMLFile = new File(mappingXMLFilePath);
            if (mappingXMLFile.exists()) {
                mappingXMLFile.delete();
            }
        }
        myBatisGenerator.generate(progressCallBack, contexts, fullyQualifiedTables);
    }

    private String getMappingXMLFilePath(GeneratorConfig generatorConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(generatorConfig.getProjectFolder()).append("/");
        sb.append(generatorConfig.getMappingXMLTargetFolder()).append("/");
        String mappingXMLPackage = generatorConfig.getMappingXMLPackage();
        if (StringUtils.isNotEmpty(mappingXMLPackage)) {
            sb.append(mappingXMLPackage.replace(".", "/")).append("/");
        }
        if (StringUtils.isNotEmpty(generatorConfig.getMapperName())) {
            sb.append(generatorConfig.getMapperName()).append(".xml");
        } else {
            sb.append(generatorConfig.getDomainObjectName()).append("Mapper.xml");
        }

        return sb.toString();
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallBack = progressCallback;
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }
}
