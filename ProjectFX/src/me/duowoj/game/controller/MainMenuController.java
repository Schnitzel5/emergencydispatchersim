package me.duowoj.game.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import me.duowoj.game.Game;
import me.duowoj.game.intern.Importer;

public class MainMenuController extends Controller {

    public static ProgressBar bar;
    public static Button newBtn;
    public static Button loadBtn;
    public static Button quitBtn;

    @FXML
    private ProgressBar loadingBar;
    @FXML
    private Button newSim;
    @FXML
    private Button loadSim;
    @FXML
    private Button quit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bar = loadingBar;
        newBtn = newSim;
        loadBtn = loadSim;
        quitBtn = quit;
        loadingBar.setVisible(false);
        newSim.setVisible(false);
        loadSim.setVisible(false);
        quit.setVisible(false);
    }

    @FXML
    public void createSim() throws IOException {
        Game.loadScene("main_menu_settings", "Simulator erstellen", (e) -> {
        });
    }

    @FXML
    public void loadSim() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EDS", "*.eds"));
        File file = chooser.showOpenDialog(Game.getPrimaryStage());
        if (file != null) {
            Importer.importSim(file);
        }
    }

    @FXML
    public void quitGame() {
        Game.getPrimaryStage().close();
    }

}
