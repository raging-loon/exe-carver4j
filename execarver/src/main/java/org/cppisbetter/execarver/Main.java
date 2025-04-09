package org.cppisbetter.execarver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cppisbetter.execarver.controller.PrimaryController;
import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.Struct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("start.fxml"));
        Scene scene = new Scene(loader.load());
        PrimaryController controller = loader.getController();
        controller.initialize(stage);
        stage.setScene(scene);
        stage.show();
    }
}
