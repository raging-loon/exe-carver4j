package org.cppisbetter.execarver.controller;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.ArrayList;

public class TableBuilder<T> {
    private ArrayList<TableColumn<T, ?> > m_cols;
    private ObservableList<T> m_data;
    private TableBuilder() {
        m_cols = new ArrayList<>();
        m_data = null;
    }

    public static <T> TableBuilder<T> of(Class<T> type) {
        return new TableBuilder<>();
    }
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
    public TableView<T> build() {
        TableView<T> table = new TableView<>();
        if (m_data != null)
            table.setItems(m_data);

        table.getColumns().addAll(m_cols);
        return table;
    }

}
