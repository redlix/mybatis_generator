package com.red.generator.common;

/**
 * fxml page enum
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 23:54
 * @since 1.0.0
 */
public enum FXMLPage {
    NEW_CONNECTION("fxml/newConnection.fxml"),
    SELECT_TABLE_COLUMN("fxml/selectTableColumn.fxml"),
    GENERATOR_CONFIG("fxml/generatorConfigs.fxml"),
    ;

    private String fxml;

    FXMLPage(String fxml) {
        this.fxml = fxml;
    }

    public String getFxml() {
        return this.fxml;
    }

}
