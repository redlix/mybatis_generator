package com.red.generator.model;

import lombok.Data;

/**
 * generator config
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 11:01
 * @since 1.0.0
 */
@Data
public class GeneratorConfig {
    /**
     * 本配置的名称
     */
    private String name;

    private String connectorJarPath;

    private String projectFolder;

    private String modelPackage;

    private String modelPackageTargetFolder;

    private String daoPackage;

    private String daoTargetFolder;

    private String mapperName;

    private String mappingXMLPackage;

    private String mappingXMLTargetFolder;

    private String tableName;

    private String domainObjectName;

    private boolean offsetLimit;

    private boolean comment;

    private boolean overrideXML;

    private boolean needToStringHashcodeEquals;

    private boolean needForUpdate;

    private boolean annotationDAO;

    private boolean annotation;

    private boolean useActualColumnNames;

    private boolean useExample;

    private String generateKeys;

    private String encoding;

    private boolean useTableNameAlias;

    public boolean getUseTableNameAlias(){
        return this.useTableNameAlias;
    }

    private boolean useDAOExtendStyle;

    private boolean useSchemaPrefix;

    private boolean jsr310Support;

    public boolean isJsr310Support() {
        return jsr310Support;
    }

    public boolean isUseSchemaPrefix() {
        return useSchemaPrefix;
    }

    public boolean isUseExample() {
        return useExample;
    }

    public boolean isOffsetLimit() {
        return offsetLimit;
    }

    public boolean isComment() {
        return comment;
    }

    public boolean isNeedToStringHashcodeEquals() {
        return needToStringHashcodeEquals;
    }

    public boolean isNeedForUpdate() {
        return needForUpdate;
    }

    public boolean isAnnotationDAO() {
        return annotationDAO;
    }

    public boolean isAnnotation() {
        return annotation;
    }

    public boolean isUseActualColumnNames() {
        return useActualColumnNames;
    }

    public boolean isUseTableNameAlias() {
        return useTableNameAlias;
    }

    public boolean isOverrideXML() {
        return overrideXML;
    }

    public boolean isUseDAOExtendStyle() {
        return useDAOExtendStyle;
    }
}

