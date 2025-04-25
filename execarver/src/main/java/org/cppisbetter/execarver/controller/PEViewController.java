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
import javafx.util.Pair;
import org.cppisbetter.execarver.carver.PE32.PE32;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.UnpackedValue;

import java.util.Map;
import java.util.Optional;

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

        if(m_exe.getNTHeaders() != null) {
            createNTHeaderTreeView(root);
        }

        m_infoView.setRoot(root);
        m_infoView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, _new) -> {
                switch(_new.getValue())
                {
                    case "DOS Header"       -> createDOSTable2();
                    case "NT Headers"       -> createNTTable();
                    case "File Header"      -> createFileHeaderTable();
                    case "Optional Header"  -> createOptionalHeaderTable();
                    case "Data Directories" -> createDataDirectoriesTable();
                }
            }
        );
    }

    private void createNTHeaderTreeView(TreeItem<String> root) {
        TreeItem<String> ntRoot = new TreeItem<>("NT Headers");

        ntRoot.getChildren().add(new TreeItem<>("File Header"));
        ntRoot.setExpanded(true);

        TreeItem<String> optHeaderRoot = new TreeItem<>("Optional Header");

        optHeaderRoot.getChildren().add(new TreeItem<>("Data Directories"));
        optHeaderRoot.setExpanded(true);

        ntRoot.getChildren().add(optHeaderRoot);

        root.getChildren().add(ntRoot);
    }

    private void createDOSTable2() {
            var table =
                createMOSVTable()
                    .setData(FXCollections.observableArrayList(m_exe.getDOSHeader().entrySet()))
                    .build();

            setTable(table);
    }

    private void createNTTable() {
        // sloppy
        AssocMap ntHeader = m_exe.getNTHeaders();
        // we only have one value
        var table =
            createMOSVTable()
                .setData(FXCollections.observableArrayList(ntHeader.entrySet().stream().findFirst().get()))
                .build();
        setTable(table);


    }

    private void createFileHeaderTable() {
        var data = m_exe.getNTHeaders().entrySet().stream().toList().subList(1, 8);

        var table =
            createMOSVTable()
                .newColumn("Meaning", cell -> {
                        String value = "";
                        System.out.println(cell.getValue().getKey());

                        if (cell.getValue().getKey().equals("Machine")) {
                            value = m_exe.getMachineType();
                        }
                        return new SimpleObjectProperty<>(
                            value
                        );
                    }
                )
                .setData(FXCollections.observableArrayList(data))
                .build();
        setTable(table);


    }

    private void createOptionalHeaderTable() {
        var data = m_exe.getNTHeaders().entrySet().stream().toList().subList(8, 37);

        var table =
            createMOSVTable()
                .newColumn("Meaning", cell -> {
                    String value = "";

                    if(cell.getValue().getKey().equals("Magic")) {
                        value = m_exe.is32Bit() ? "PE32": "PE32+";
                    }

                    return new SimpleObjectProperty<>(
                            value
                    );
                }
                )
                .setData(FXCollections.observableArrayList(data))
                .build();
        setTable(table);

    }

    private void createDataDirectoriesTable() {
        var data = m_exe.getDataDirectories();

        var table = createMOSVTable().setData(FXCollections.observableArrayList(data.entrySet())).build();

        setTable(table);


    }

    ///
    /// PURPOSE
    ///     A lot of tables use the format "Member | Offset | Size | Value"
    ///     This automates construction of such fields
    ///
    private TableBuilder<Map.Entry> createMOSVTable() {
        return TableBuilder.of(Map.Entry.class)
                .newColumn("Member", cell -> {
                    return new SimpleObjectProperty<>(
                            cell.getValue().getKey()
                    );
                })
                .newColumn("Offset", cell -> {
                    UnpackedValue uv = (UnpackedValue) cell.getValue().getValue();
                    return new SimpleObjectProperty<>(
                            String.format("%08X", uv.getOffset())
                    );
                })
                .newColumn("Size", cell -> {
                    UnpackedValue uv = (UnpackedValue) cell.getValue().getValue();
                    return new SimpleObjectProperty<>(uv.getSizeType());
                })
                .newColumn("Value", cell -> {
                    return new SimpleObjectProperty<>(
                            cell.getValue().getValue()
                    );
                });
    }

    private void setTable(TableView<?> newTable) {
        if(!m_infoPane.getChildren().isEmpty())
            m_infoPane.getChildren().removeFirst();
        m_infoPane.getChildren().add(newTable);
    }
}
