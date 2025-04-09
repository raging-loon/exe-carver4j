module org.cppisbetter.execarver {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.cppisbetter.execarver to javafx.fxml;
    exports org.cppisbetter.execarver;

    opens org.cppisbetter.execarver.controller to javafx.fxml;
    exports org.cppisbetter.execarver.controller;
}