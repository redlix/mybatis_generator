package com.red.generator.controller;

import com.red.generator.model.GeneratorConfig;
import com.red.generator.utils.ConfigHelper;
import com.red.generator.view.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * generator config controller
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/15     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-15 18:07
 * @since 1.0.0
 */
@Slf4j
public class GeneratorConfigController extends BaseFXController {
    @FXML
    private TableView<GeneratorConfig> configTable;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn opsColumn;

    private MainUIController mainUIController;

    private GeneratorConfigController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        // 自定义操作列
        opsColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        opsColumn.setCellFactory(cell -> {
            return new TableCell() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Button btn1 = new Button("应用");
                        Button btn2 = new Button("删除");
                        HBox hBox = new HBox();
                        hBox.setSpacing(10);
                        hBox.getChildren().add(btn1);
                        hBox.getChildren().add(btn2);
                        btn1.setOnAction(event -> {
                            try {
                                // 应用配置
                                GeneratorConfig generatorConfig = ConfigHelper.loadGeneratorConfig(item.toString());
                                mainUIController.setGeneratorConfigIntoUI(generatorConfig);
                                controller.closeDialogStage();
                            } catch (Exception e) {
                                AlertUtil.showErrorAlert(e.getMessage());
                            }
                        });
                        btn2.setOnAction(event -> {
                            try {
                                // 删除配置
                                log.debug("item: {}", item);
                                ConfigHelper.deleteGeneratorConfig(item.toString());
                                refreshTableView();
                            } catch (Exception e) {
                                AlertUtil.showErrorAlert(e.getMessage());
                            }
                        });
                        setGraphic(hBox);
                    }
                }
            };
        });
        refreshTableView();
    }

    private void refreshTableView() {
        try {
            List<GeneratorConfig> configs = ConfigHelper.loadGeneratorConfigs();
            configTable.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    void setMainUIController(MainUIController mainUIController) {
        this.mainUIController = mainUIController;
    }
}
