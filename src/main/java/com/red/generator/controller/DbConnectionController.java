package com.red.generator.controller;

import com.red.generator.model.DataBaseConfig;
import com.red.generator.utils.ConfigHelper;
import com.red.generator.view.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * db cnnection controller
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/15     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-15 17:33
 * @since 1.0.0
 */
@Slf4j
public class DbConnectionController extends BaseFXController {
    @FXML
    protected TextField nameField;
    @FXML
    protected TextField hostField;
    @FXML
    protected TextField portField;
    @FXML
    protected TextField userNameField;
    @FXML
    protected TextField passwordField;
    @FXML
    protected TextField schemaField;
    @FXML
    protected ChoiceBox<String> encodingChoice;
    @FXML
    protected ChoiceBox<String> dbTypeChoice;
    protected MainUIController mainUIController;
    protected boolean isUpdate = false;
    protected Integer primayKey;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    final void saveConnection() {
        DataBaseConfig config = extractConfigForUI();
        if (config == null) {
            return;
        }
        try {
            ConfigHelper.saveDataBaseConfig(this.isUpdate, primayKey, config);
            getDialogStage().close();
            mainUIController.loadLeftDBTree();
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    void setMainUIController(MainUIController controller) {
        this.mainUIController = controller;
        super.setDialogStage(mainUIController.getDialogStage());
    }

    public void setConfig(DataBaseConfig config) {
        isUpdate = true;
        primayKey = config.getId(); // save id for update config
        nameField.setText(config.getName());
        hostField.setText(config.getHost());
        portField.setText(config.getPort());
        userNameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
        encodingChoice.setValue(config.getEncoding());
        dbTypeChoice.setValue(config.getDbType());
        schemaField.setText(config.getSchema());
    }

    public DataBaseConfig extractConfigForUI() {
        String name = nameField.getText();
        String host = hostField.getText();
        String port = portField.getText();
        String userName = userNameField.getText();
        String password = passwordField.getText();
        String encoding = encodingChoice.getValue();
        String dbType = dbTypeChoice.getValue();
        String schema = schemaField.getText();
        DataBaseConfig config = new DataBaseConfig();
        config.setName(name);
        config.setDbType(dbType);
        config.setHost(host);
        config.setPort(port);
        config.setUsername(userName);
        config.setPassword(password);
        config.setSchema(schema);
        config.setEncoding(encoding);
        if (StringUtils.isAnyEmpty(name, host, port, userName, encoding, dbType, schema)) {
            AlertUtil.showWarnAlert("密码以外其他字段必填");
            return null;
        }
        return config;
    }
}
