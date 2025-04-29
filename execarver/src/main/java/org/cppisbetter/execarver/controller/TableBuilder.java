package org.cppisbetter.execarver.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.util.ArrayList;

///
/// PURPOSE
///     Building tables from hand is disgusting
///     This wraps around that
///
///     This is a mixture of the builder and fluent interface pattern.
/// EXAMPLE
///     var table = TableBuilder.of<Map.Entry.class>()
///         .newColumn("Key", cell -> cell.getKey())
///         .newColumn("Value", cell -> cell.getValue())
///         .setData(FXCollections.....blah blah blah)
///         .build();
 ///
///
public class TableBuilder<T> {
    /// All columns to be added when @ref build() is called
    private final ArrayList<TableColumn<T, ?> > m_cols;

    private ObservableList<T> m_data;

    private TableBuilder() {
        m_cols = new ArrayList<>();
        m_data = null;
    }
    /// Mostly here for type-safety
    public static <T> TableBuilder<T> of(Class<T> type) {
        return new TableBuilder<>();
    }

    ///
    /// PURPOSE
    ///     Add a new column representing Type T built with
    ///     callbacks of type C
    ///
    public <C> TableBuilder<T> newColumn(
            String Label,
            Callback<TableColumn.CellDataFeatures<T, C>, ObservableValue<C>> cb
    ) {

        TableColumn<T, C> nCol = new TableColumn<>(Label);
        nCol.setCellValueFactory(cb);
        m_cols.add(nCol);
        return this;
    }

    public TableBuilder<T> setData(ObservableList<T> list) {
        m_data = list;
        return this;
    }

    ///
    /// PURPOSE
    ///     Create the table object, set the columns, set the data,
    ///     fix-up sizing
    ///
    /// RETURNS
    ///     A new TableView<T>
    ///
    public TableView<T> build() {
        TableView<T> table = new TableView<>();
        if (m_data != null)
            table.setItems(m_data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(m_cols);


        table.setFixedCellSize(25);
        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(30));
        table.prefWidth(TableView.USE_COMPUTED_SIZE);
        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        return table;
    }

}
