package com.red.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * RepositoryPlugin
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/16     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-16 09:04
 * @since 1.0.0
 */
public class RepositoryPlugin extends PluginAdapter {
    private FullyQualifiedJavaType annotationRepository;
    private String annotation = "@Repository";

    public RepositoryPlugin() {
        annotationRepository = new FullyQualifiedJavaType("org.springframework.stereotype.Repository");
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        interfaze.addImportedType(annotationRepository);
        interfaze.addAnnotation(annotation);
        return true;
    }
}
