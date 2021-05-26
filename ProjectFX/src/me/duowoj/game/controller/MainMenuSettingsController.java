package me.duowoj.game.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.intern.CaseGenerator;
import me.duowoj.game.intern.Exporter;
import me.duowoj.game.intern.Loader;
import me.duowoj.game.intern.VehicleHandler;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;

public class MainMenuSettingsController extends Controller {

    @FXML
    private CheckBox debug;
    @FXML
    private CheckBox dnd;
    @FXML
    private CheckBox smartdelay;
    @FXML
    private CheckBox path;
    @FXML
    private ChoiceBox<KeyCode> hotkey1;
    @FXML
    private ChoiceBox<String> canvas;
    @FXML
    private Label fileLocation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hotkey1.getItems().addAll(KeyCode.values());
        hotkey1.getSelectionModel().select(Settings.settings.getHotKeys().getOrDefault("1", KeyCode.E));
        canvas.getItems().addAll("Klein (500 x 382)", "Normal (1000 x 763) [empfohlen!]", "Groß (2000 x 1526)");
        canvas.getSelectionModel().select(1);
    }

    @FXML
    public void save() {
        Map<String, Boolean> setting = Settings.settings.getSetting();
        setting.put("debug", debug.isSelected());
        setting.put("dnd", dnd.isSelected());
        setting.put("smartdelay", smartdelay.isSelected());
        setting.put("path", path.isSelected());
        Map<String, KeyCode> hotKeys = Settings.settings.getHotKeys();
        hotKeys.put("1", hotkey1.getSelectionModel().getSelectedItem());
        CallCenterController.dnd.set(dnd.isSelected());
        int width = 1000;
        int height = 763;
        switch (canvas.getSelectionModel().getSelectedItem()) {
        case "Klein (500 x 382)":
            width = 500;
            height = 382;
            break;
        case "Groß (2000 x 1526)":
            width = 2000;
            height = 1526;
            break;
        default:
        }
        Settings.settings.setCanvas(width, height);
        cancel();
    }

    @FXML
    public void startGame() {
        if (!path.isSelected() && fileLocation.getText().isBlank()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Der Speicherort muss noch angegeben werden!");
            alert.showAndWait();
            return;
        }
        save();
        Loader.translateLocs();
        Settings.settings.setPath(fileLocation.getText());
        try {
            Game.setRunning(true);
            Game.generator = new CaseGenerator();
            Game.vehicleHandler = new VehicleHandler();
            Game.vehicleHandler.start();
            Game.loadScene("call_center", "Anrufszentrum", (e) -> {
                if (Game.vehicleHandler != null) {
                    Game.vehicleHandler.cancel();
                }
                Game.setRunning(false);
                Exporter.export(new File(Settings.settings.getPath()));
                if (Settings.settings.isSet("path")) {
                    Exporter.export(new File(
                            new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath(),
                            "simulator_" + new SimpleDateFormat("dd.MM.yyyy_hh-mm").format(new Date()) + ".eds"));
                }
                CallCenterController.getCaseList().clear();
                CaseGenerator.clearCases();
                Case.cases.clear();
                Case.resetCounter();
                Vehicle.vehicles.clear();
                CallCenterOverviewController.clear();
                Game.getStages().values().forEach(Stage::close);
                Game.getStages().clear();
                Settings.reset();
                Game.getPrimaryStage().show();
            });
            Game.getStages().get("Anrufszentrum").addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == Settings.settings.getHotKeys().getOrDefault("1", KeyCode.E)) {
                    try {
                        Case currentCase = CallCenterController.callCenter.getCurrentCase();
                        if (currentCase != null) {
                            Game.loadScene("call_center_forms", "Leitstelle: " + currentCase.getCaseId(), (e1) -> {
                                ((CallCenterFormsController) Game.getControllers()
                                        .get("Leitstelle: " + currentCase.getCaseId())).saveAfter();
                                if (Game.controlCanvas.containsKey(currentCase.getCaseId())) {
                                    Game.controlCanvas.get(currentCase.getCaseId()).stop();
                                }
                            });
                            CallCenterFormsController controller = (CallCenterFormsController) Game.getControllers()
                                    .get("Leitstelle: " + currentCase.getCaseId());
                            controller.currentCaseID = currentCase.getCaseId();
                            CallCenterFormsController.prepareForms(currentCase, controller, true);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            Game.getPrimaryStage().close();
        } catch (IOException e) {
            e.printStackTrace();
            Game.setRunning(false);
        }
    }

    @FXML
    public void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EDS", "*.eds"));
        File file = chooser.showSaveDialog(Game.getPrimaryStage());
        if (file != null) {
            fileLocation.setText(file.getAbsolutePath());
        }
        Game.getStages().get("Simulator erstellen").toFront();
    }

    @FXML
    public void cancel() {
        Game.getStages().get("Simulator erstellen").close();
        Game.getStages().remove("Simulator erstellen");
    }

}
