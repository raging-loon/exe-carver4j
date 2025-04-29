package org.cppisbetter.execarver.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import org.cppisbetter.execarver.carver.PE32.PE32;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.UnpackedValue;

import java.lang.reflect.Method;
import java.util.Map;

///
/// PURPOSE
///     Create and manage views/tables for parts of a PE
///
public class PEViewController {
    /// PURPOSE: where tables/views go
    private final AnchorPane m_infoPane;
    ///
    /// PURPOSE
    ///     Updated with table *listings*
    ///     based on the contents of the executable
    private final TreeView<String>   m_infoView;
    private final PE32       m_exe;

    /// LRU Cache for tables/views
    private final TableCache m_tcache;

    public PEViewController(PE32 exe, TreeView<String> infoView, AnchorPane infoPane) {
        this.m_exe = exe;
        this.m_infoView = infoView;
        this.m_infoPane = infoPane;

        // FIXME: THIS IS HARD CODED
        m_tcache = new TableCache(5);

        m_exe.parse();
    }
    ///
    /// PURPOSE
    ///     Added sub views based on what is in the Executable
    ///     Then add callbacks for the root tree's click handler
    ///
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

        if(m_exe.getSectionHeaders() != null) {
            TreeItem<String> newItem = new TreeItem<>("Section Headers");
            root.getChildren().add(newItem);

        }

        if(m_exe.getExportDirectory() != null) {
            TreeItem<String> exportDir = new TreeItem<>("Export Directory");
            TreeItem<String> exports =new TreeItem<>("Export Listing");

            exportDir.getChildren().add(exports);
            exportDir.setExpanded(true);

            root.getChildren().add(exportDir);

        }

        m_infoView.setRoot(root);
        m_infoView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, _new) -> {
                /// These correspond to the actual names of the nodes
                switch(_new.getValue())
                {
                    case "DOS Header"       -> createDOSTable2();
                    case "NT Headers"       -> createNTTable();
                    case "File Header"      -> createFileHeaderTable();
                    case "Optional Header"  -> createOptionalHeaderTable();
                    case "Data Directories" -> createDataDirectoriesTable();
                    case "Section Headers"  -> createSectionHeadersTable();
                    case "Export Directory" -> createExportDirTable();
                    case "Export Listing"   -> createExportListingTable();
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
        if(getAndSetFromCache("DOS TABLE"))
            return;

            var table =
                createMOSVTable()
                    .setData(FXCollections.observableArrayList(m_exe.getDOSHeader().entrySet()))
                    .build();

            setTable(table, "DOS TABLE");
    }

    private void createNTTable() {

        if(getAndSetFromCache("NTHDR"))
            return;
        // sloppy
        AssocMap ntHeader = m_exe.getNTHeaders();
        // we only have one value
        var table =
            createMOSVTable()
                .setData(FXCollections.observableArrayList(ntHeader.entrySet().stream().findFirst().get()))
                .build();
        setTable(table, "NTHDR");


    }

    private void createFileHeaderTable() {

        if(getAndSetFromCache("FILEHDR"))
            return;
        var data = m_exe.getNTHeaders().entrySet().stream().toList().subList(1, 8);

        var table =
            createMOSVTable()
                .newColumn("Meaning", cell -> {
                        String value = "";

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
        setTable(table, "FILEHDR");


    }

    private void createOptionalHeaderTable() {

        if(getAndSetFromCache("OPTHDR"))
            return;
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

        setTable(table, "OPTHDR");

    }

    private void createDataDirectoriesTable() {

        if(getAndSetFromCache("DATA DIR"))
            return;
        var data = m_exe.getDataDirectories();

        var table = createMOSVTable().setData(FXCollections.observableArrayList(data.entrySet())).build();

        setTable(table, "DATA DIR");


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
    ///
    /// PURPOSE
    ///     Set and cache a table
    ///
    private void setTable(TableView<?> newTable, String name) {
        if(!m_infoPane.getChildren().isEmpty())
            m_infoPane.getChildren().clear();
        m_infoPane.getChildren().add(newTable);

        cacheTable(name, newTable);
    }

    private void createSectionHeadersTable() {
        if(getAndSetFromCache("SECHDR"))
            return;
        var table = TableBuilder.of(Map.Entry.class)
                .newColumn("Name", cell -> new SimpleObjectProperty<>(cell.getValue().getKey()))
                .newColumn("Virtual Size", cell -> {
                    return extractAndFormatFromVariant(cell, "getVirtualSize", 4);
                })
                .newColumn("Virtual Address", cell -> {
                    return extractAndFormatFromVariant(cell, "getVirtualAddress", 4);
                })
                .newColumn("Raw Size", cell -> {
                    return extractAndFormatFromVariant(cell, "getRawSize", 4);
                })
                .newColumn("Raw Address", cell -> {
                    return extractAndFormatFromVariant(cell, "getRawAddress", 4);
                })
                .newColumn("Reloc Address", cell -> {
                    return extractAndFormatFromVariant(cell, "getRelocAddress", 4);
                })
                .newColumn("Linenumbers", cell -> {
                    return extractAndFormatFromVariant(cell, "getLineNumbers", 4);
                })
                .newColumn("Relocations Number", cell -> {
                    return extractAndFormatFromVariant(cell, "getRelocationsNumber", 2);
                })
                .newColumn("Line Numbers", cell -> {
                    return extractAndFormatFromVariant(cell, "getLineNumbersNumber", 2);
                })
                .newColumn("Characteristics", cell -> {
                    return extractAndFormatFromVariant(cell, "getCharacteristics", 4);
                })
                .setData(FXCollections.observableArrayList(m_exe.getSectionHeaders().entrySet()))
                .build();

        setTable(table, "SECHDR");
    }

    ///
    /// PURPOSE
    ///     Extract methods from any arbitrary class, execute them, and format the result as hex
    ///
    /// NOTE
    ///     This is a bit hacky and not entirely typesafe....
    ///
    private <S, T> SimpleObjectProperty<T>
    extractAndFormatFromVariant(TableColumn.CellDataFeatures<Map.Entry, Object> cell, String name, int size) {
        S header = (S)(cell.getValue().getValue());
        try {

            Method method = header.getClass().getMethod(name);

            Object value = method.invoke(header);

            String fmt = String.format("%%0%dX", size * 2);

            return new SimpleObjectProperty<T>((T)String.format(fmt, value));


        } catch(Exception e) {
            throw new RuntimeException(e);
        }



    }

    private void createExportDirTable() {

        if(getAndSetFromCache("EXPORT DIR"))
            return;
        var data = m_exe.getExportDirectory().entrySet();

        var table = createMOSVTable().setData(FXCollections.observableArrayList(data)).build();

        setTable(table, "EXPORT DIR");

    }

    private void createExportListingTable() {

        if(getAndSetFromCache("EXPORT LISTING"))
            return;

        var table = TableBuilder.of(Map.Entry.class)
            .newColumn("Ordinal", cell -> {
                return extractAndFormatFromVariant(cell, "getOrdinal", 4);
            })
            .newColumn("Function RVA", cell -> {
                return extractAndFormatFromVariant(cell, "getFunctionRVA", 4);
            })
            .newColumn("Name Ordinal", cell -> {
                return extractAndFormatFromVariant(cell, "getNameOrdinal", 2);
            })
            .newColumn("Name RVA", cell -> {
                return extractAndFormatFromVariant(cell, "getNameRVA", 4);
            })
            .newColumn("Name", cell -> {
                return new SimpleObjectProperty<>(cell.getValue().getKey());
            })
            .setData(FXCollections.observableArrayList(m_exe.getExports().entrySet()))
            .build();

        setTable(table, "EXPORT LISTING");
    }

    ///
    /// PURPOSE
    ///     *Get* a table from the cache and *set* it if it exists
    ///
    private boolean getAndSetFromCache(String name) {
        TableView<?> table = m_tcache.get(name);
        m_tcache.printCache();
        if(table != null) {
            setTable(table, name);
            System.out.println("Pulling " + name + " from cache");
            return true;
        }

        return false;
    }

    private void cacheTable(String name, TableView<?> table) {
        m_tcache.put(name, table);
    }
}
