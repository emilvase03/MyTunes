module dk.easv.mytunes {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.mytunes to javafx.fxml;
    exports dk.easv.mytunes;
}