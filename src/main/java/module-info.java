module ensisa.lines {
    requires transitive javafx.controls;
    requires javafx.fxml;


    opens ensisa.lines to javafx.fxml;
    exports ensisa.lines;
}