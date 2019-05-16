package com.red.generator.controller;

import com.red.generator.common.FXMLPage;
import com.red.generator.view.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * base fxml controller
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 11:47
 * @since 1.0.0
 */
@Slf4j
@Data
public abstract class BaseFXController implements Initializable {
    private Stage primaryStage;
    private Stage dialogStage;

    private static Map<FXMLPage, SoftReference<? extends BaseFXController>> cacheNodeMap = new HashMap<>();

    public BaseFXController loadFXMLPage(String title, FXMLPage fxmlPage, boolean cache) {
        SoftReference<? extends BaseFXController> parentNodeRef = cacheNodeMap.get(fxmlPage);

        if (cache && parentNodeRef != null) {
            return parentNodeRef.get();
        }
        URL skeletonResource = Thread.currentThread().getContextClassLoader().getResource(fxmlPage.getFxml());
        FXMLLoader loader = new FXMLLoader(skeletonResource);
        Parent loginNode;
        try {
            loginNode = loader.load();
            BaseFXController controller = loader.getController();
            dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getPrimaryStage());
            dialogStage.setScene(new Scene(loginNode));
            dialogStage.setMaximized(false);
            dialogStage.setResizable(false);
            dialogStage.show();
            controller.setDialogStage(dialogStage);

            SoftReference<BaseFXController> softReference = new SoftReference<>(controller);
            cacheNodeMap.put(fxmlPage, softReference);

            return controller;
        } catch (IOException e) {
            log.error(e.getMessage());
            AlertUtil.showErrorAlert(e.getMessage());
        }

        return null;
    }

    public void showDialogStage() {
        if (dialogStage != null) {
            dialogStage.show();
        }
    }

    public void closeDialogStage() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
