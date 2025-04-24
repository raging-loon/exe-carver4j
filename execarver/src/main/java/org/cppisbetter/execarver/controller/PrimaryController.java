package org.cppisbetter.execarver.controller;


import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.cppisbetter.execarver.carver.PE32.PE32;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PrimaryController {

    @FXML
    private MenuItem m_miOpenFile;

    @FXML
    private AnchorPane m_infoPane;

    private Stage m_mainStage;

    @FXML
    private TreeView<String> m_itemView;

    private PEViewController m_pevc = null;

    public void initialize(Stage mainStage) {
        assert(mainStage != null);

        m_mainStage = mainStage;
    }
    private PE32 m_tempPE;
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

        openFile(selectedFile);

    }

    private void openFile(File file) {
        try {
            byte[] contents = Files.readAllBytes(file.toPath());
            m_tempPE = new PE32(contents);
            m_pevc = new PEViewController(m_tempPE, m_itemView, m_infoPane);
            m_pevc.initializeViews();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
