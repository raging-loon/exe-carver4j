package org.cppisbetter.execarver.controller;


import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;

public class PrimaryController {

    @FXML
    private MenuItem m_miOpenFile;

    @FXML
    private TabPane m_tabPane;

    private Stage m_mainStage;

//    private Tab m_currentTab;

    public void initialize(Stage mainStage) {
        assert(mainStage != null);

        m_mainStage = mainStage;
    }

    @FXML
    private void onOpenFilePressed() {
        // https://docs.oracle.com/javase/8/javafx/api/javafx/stage/FileChooser.html
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new ExtensionFilter("Executable Files", "*.exe", "*.dll", "*.sys"),
                new ExtensionFilter("All Files", "*")
        );

        File selectedFile = fc.showOpenDialog(m_mainStage);

        if(selectedFile == null)
            return;

        System.out.println(selectedFile.getName());



    }

}
