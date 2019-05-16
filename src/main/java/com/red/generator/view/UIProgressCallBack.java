package com.red.generator.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import lombok.Data;
import org.mybatis.generator.api.ProgressCallback;

/**
 * ui process call back
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/15     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-15 16:22
 * @since 1.0.0
 */
@Data
public class UIProgressCallBack extends Alert implements ProgressCallback {
    private StringProperty progressText = new SimpleStringProperty();

    public UIProgressCallBack(AlertType alertType) {
        super(alertType);
        this.contentTextProperty().bindBidirectional(progressText);
    }

    @Override
    public void introspectionStarted(int i) {
        progressText.setValue("开始检查代码");
    }

    @Override
    public void generationStarted(int i) {
        progressText.setValue("开始代码生成");
    }

    @Override
    public void saveStarted(int i) {
        progressText.setValue("开始保存生成文件");
    }

    @Override
    public void startTask(String s) {
        progressText.setValue("代码生成任务开始");
    }

    @Override
    public void done() {
        progressText.setValue("代码生成结束");
    }

    @Override
    public void checkCancel() throws InterruptedException {

    }
}
