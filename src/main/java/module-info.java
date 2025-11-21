module dk.easv.mytunes {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.mytunes to javafx.fxml;
    exports dk.easv.mytunes;
    exports dk.easv.mytunes.GUI.Controllers;
    opens dk.easv.mytunes.GUI.Controllers to javafx.fxml;
}