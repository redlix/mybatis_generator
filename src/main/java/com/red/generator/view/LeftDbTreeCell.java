package com.red.generator.view;

import com.red.generator.model.DataBaseConfig;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

import java.lang.ref.WeakReference;

/**
 * LeftDbTreeCell
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/16     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-16 09:05
 * @since 1.0.0
 */
public class LeftDbTreeCell extends TreeCell<DataBaseConfig> {
    private HBox hbox;

    private WeakReference<TreeItem<DataBaseConfig>> treeItemRef;

    private InvalidationListener treeItemGraphicListener = observable -> {
        updateDisplay(getItem(), isEmpty());
    };

    private InvalidationListener treeItemListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            TreeItem<DataBaseConfig> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
            if (oldTreeItem != null) {
                oldTreeItem.graphicProperty().removeListener(weakTreeItemGraphicListener);
            }

            TreeItem<DataBaseConfig> newTreeItem = getTreeItem();
            if (newTreeItem != null) {
                newTreeItem.graphicProperty().addListener(weakTreeItemGraphicListener);
                treeItemRef = new WeakReference<TreeItem<DataBaseConfig>>(newTreeItem);
            }
        }
    };

    private WeakInvalidationListener weakTreeItemGraphicListener =
            new WeakInvalidationListener(treeItemGraphicListener);

    private WeakInvalidationListener weakTreeItemListener =
            new WeakInvalidationListener(treeItemListener);

    public LeftDbTreeCell() {
        treeItemProperty().addListener(weakTreeItemListener);

        if (getTreeItem() != null) {
            getTreeItem().graphicProperty().addListener(weakTreeItemGraphicListener);
        }
    }

    void updateDisplay(DataBaseConfig item, boolean empty) {
        if (item == null || empty) {
            hbox = null;
            setText(null);
            setGraphic(null);
        } else {
            // update the graphic if one is set in the TreeItem
            TreeItem<DataBaseConfig> treeItem = getTreeItem();
            if (treeItem != null && treeItem.getGraphic() != null) {
                hbox = null;
                setText(item.toString());
                setGraphic(treeItem.getGraphic());
            } else {
                hbox = null;
                setText(item.getName());
                setGraphic(null);
            }
        }
    }

    @Override
    public void updateItem(DataBaseConfig item, boolean empty) {
        super.updateItem(item, empty);
        updateDisplay(item, empty);
    }
}
