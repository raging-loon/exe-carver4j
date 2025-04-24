package org.cppisbetter.execarver.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.cppisbetter.execarver.carver.PE32.PE32;
import org.cppisbetter.execarver.struct.UnpackedValue;

import java.util.Map;

public class PEViewController {
    private AnchorPane m_infoPane;
    private TreeView<String>   m_infoView;
    private PE32       m_exe;


    public PEViewController(PE32 exe, TreeView<String> infoView, AnchorPane infoPane) {
        this.m_exe = exe;
        this.m_infoView = infoView;
        this.m_infoPane = infoPane;

        m_exe.parse();
    }

    public void initializeViews() {
        TreeItem<String> root = new TreeItem<>("File");
        root.setExpanded(true);
        if(m_exe.getDOSHeader() != null){
            TreeItem<String> dosHeaderItem = new TreeItem<>("DOS Header");
            root.getChildren().add(dosHeaderItem);

        }
        m_infoView.setRoot(root);
        m_infoView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, _new) -> {
                if(_new != null) {
                    createDOSTable();
                }
            }
        );
    }

    public void createDOSTable() {
        ObservableList<Map.Entry<String, UnpackedValue>> items =
                FXCollections.observableArrayList(m_exe.getDOSHeader().entrySet());

        TableView<Map.Entry<String, UnpackedValue>> dosTable = new TableView<>();
        dosTable.setItems(items);

        TableColumn<Map.Entry<String, UnpackedValue>, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getKey()));


        TableColumn<Map.Entry<String, UnpackedValue>, String> value = new TableColumn<>("Value");
        value.setCellValueFactory(
                cell -> {
                    return new SimpleObjectProperty<>(
                        cell.getValue().getValue().toString()
                    );
                }
        );

        TableColumn<Map.Entry<String, UnpackedValue>, String> offsets = new TableColumn<>("Offset");

        offsets.setCellValueFactory(
                cell -> {
                    return new SimpleObjectProperty<>(
                        String.format("%08X", cell.getValue().getValue().getOffset())
                    );
                }
        );

        TableColumn<Map.Entry<String, UnpackedValue>, String> types = new TableColumn<>("Size");
        types.setCellValueFactory(
                cell -> {
                    return new SimpleObjectProperty<>(cell.getValue().getValue().getSizeType());
                }
        );

        dosTable.getColumns().addAll(memberCol, offsets, types, value);

        m_infoPane.getChildren().setAll(dosTable);
    }

}
