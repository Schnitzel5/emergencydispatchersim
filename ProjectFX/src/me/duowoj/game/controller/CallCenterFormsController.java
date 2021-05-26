package me.duowoj.game.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import me.duowoj.game.Game;
import me.duowoj.game.canvas.ControlCanvas;
import me.duowoj.game.intern.Loader;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;
import me.duowoj.game.types.CallOuts;
import me.duowoj.game.types.VehicleType;

public class CallCenterFormsController extends Controller {

    public int currentCaseID = -1;

    @FXML
    protected ChoiceBox<CallOuts> callouts;
    @FXML
    protected Button streetBtn;
    @FXML
    public Label streetLabel;
    @FXML
    protected TextField caseNr;
    @FXML
    protected TextField editor;
    @FXML
    protected TextField caller;
    @FXML
    protected TextField phoneNr;
    @FXML
    protected TextField houseNr;
    @FXML
    protected TextField patient;
    @FXML
    protected TextArea description;
    @FXML
    protected CheckBox reanimation;
    @FXML
    protected TextField extra;
    @FXML
    protected Button saveBtn;
    @FXML
    protected Button phoneBtn;
    @FXML
    protected Button archiveBtn;
    @FXML
    protected Button alarmBtn;
    @FXML
    protected Button clearBtn;
    @FXML
    protected Button naBtn;
    @FXML
    protected Button rtwBtn;
    @FXML
    protected Button chrBtn;
    @FXML
    protected Button katBtn;
    @FXML
    protected Button system;
    @FXML
    protected ListView<Vehicle> openBox;
    @FXML
    protected ListView<Vehicle> alarmedBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void chooseStreet() {
        Dialog<String> filter = new TextInputDialog("");
        filter.setTitle("Straßenfilter");
        filter.setHeaderText("Straßen suchen/filtern:");
        Optional<String> filterTag = filter.showAndWait();
        List<String> streets = Loader.streets.stream().filter(s -> filterTag.isEmpty() || filterTag.get().isBlank()
                || s.toLowerCase().contains(filterTag.get().toLowerCase())).collect(Collectors.toList());
        if (streets.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Fehler!");
            alert.setContentText("Keine Straßen gefunden!");
            alert.showAndWait();
            return;
        }
        Dialog<String> dialog = new ChoiceDialog<String>(streets.get(0), streets);
        dialog.setTitle("Straßenauswahl");
        dialog.setHeaderText("Wähle eine Straße aus (Filter -> " + filterTag.orElseGet(() -> "Keine") + "):");
        dialog.getDialogPane().getStylesheets().add(Game.files.get("fxml/general.css").toExternalForm());
        Optional<String> street = dialog.showAndWait();
        streetLabel.setText(street.orElseGet(() -> "Keine Straße ausgewählt"));
    }

    @FXML
    public void save() {
        saveAfter();
        if (Game.controlCanvas.containsKey(currentCaseID)) {
            Game.controlCanvas.get(currentCaseID).stop();
        }
        Game.getStages().get("Leitstelle: " + currentCaseID).close();
        Game.getStages().remove("Leitstelle: " + currentCaseID);
    }

    @FXML
    public void insertPhone() {
        Case currentCase = Case.cases.get(currentCaseID);
        if (currentCase != null) {
            phoneNr.setText(currentCase.getMessage("phone"));
        }
    }

    @FXML
    public void archiveCase() {
        if (!Case.cases.get(currentCaseID).completed()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Nicht fertig!");
            alert.setContentText("Es sind noch nicht alle Fahrzeuge angekommen!");
            alert.showAndWait();
            return;
        }
        saveAfter();
        if (Game.controlCanvas.containsKey(currentCaseID)) {
            Game.controlCanvas.get(currentCaseID).stop();
        }
        if (Game.getStages().containsKey("Leitstelle: " + currentCaseID)) {
            Game.getStages().get("Leitstelle: " + currentCaseID).close();
            Game.getStages().remove("Leitstelle: " + currentCaseID);
            Game.getControllers().remove("Leitstelle: " + currentCaseID);
        }
        if (Game.getStages().containsKey("Geo Informations System | " + currentCaseID)) {
            Game.getStages().get("Geo Informations System | " + currentCaseID).close();
            Game.getStages().remove("Geo Informations System | " + currentCaseID);
        }
        Case c = CallCenterController.callCenter.getCurrentCase();
        if (c != null && c.getCaseId() == currentCaseID) {
            CallCenterController.callCenter.disconnect();
        }
        Case.cases.remove(currentCaseID);
        Vehicle.vehicles.remove(currentCaseID);
        if (CallCenterOverviewController.overview != null) {
            CallCenterOverviewController.overview.refresh();
        }
    }

    @FXML
    public void dispatch() {
        openBox.getItems().forEach(Vehicle::dispatch);
        alarmedBox.getItems().addAll(openBox.getItems());
        openBox.getItems().clear();
    }

    @FXML
    public void clearDispatch() {
        Vehicle.destroy(currentCaseID);
        openBox.getItems().clear();
    }

    @FXML
    public void addNA() {
        if (emptyStreet(streetLabel)) {
            return;
        }
        if (Vehicle.vehicles.get(currentCaseID).stream().filter(v -> v.getType() == VehicleType.NEF).count() < 15) {
            openBox.getItems().add(Vehicle.create(currentCaseID, VehicleType.NEF, streetLabel.getText()));
        }
    }

    @FXML
    public void addRTW() {
        if (emptyStreet(streetLabel)) {
            return;
        }
        if (Vehicle.vehicles.get(currentCaseID).stream().filter(v -> v.getType() == VehicleType.RTW).count() < 15) {
            openBox.getItems().add(Vehicle.create(currentCaseID, VehicleType.RTW, streetLabel.getText()));
        }
    }

    @FXML
    public void addCHR() {
        if (emptyStreet(streetLabel)) {
            return;
        }
        if (Vehicle.vehicles.get(currentCaseID).stream().filter(v -> v.getType() == VehicleType.CHRIS).count() < 1) {
            openBox.getItems().add(Vehicle.create(currentCaseID, VehicleType.CHRIS, streetLabel.getText()));
        }
    }

    @FXML
    public void addKAT() {
        if (emptyStreet(streetLabel)) {
            return;
        }
        if (Vehicle.vehicles.get(currentCaseID).stream().filter(v -> v.getType() == VehicleType.KAT).count() < 8) {
            openBox.getItems().add(Vehicle.create(currentCaseID, VehicleType.KAT, streetLabel.getText()));
        }
    }

    @FXML
    public void openSystem() {
        Game.controlCanvas.put(currentCaseID, new ControlCanvas(currentCaseID));
        Game.controlCanvas.get(currentCaseID).loadCanvas();
    }

    public void saveAfter() {
        Case currentCase = Case.cases.get(currentCaseID);
        currentCase.setEditor(editor.getText());
        currentCase.setName(caller.getText());
        currentCase.setPhone(phoneNr.getText());
        currentCase.setStreet(streetLabel.getText());
        String trans = houseNr.getText().replaceAll("[^0-9]", "");
        currentCase.setHouseNr(Integer.parseInt(trans.matches("\\d+") ? trans : "0"));
        currentCase.setPatient(patient.getText());
        currentCase.setDescription(description.getText());
        currentCase.setReanimation(reanimation.isSelected());
        currentCase.setCaseTag(callouts.getSelectionModel().getSelectedItem());
        currentCase.setExcessInfo(extra.getText());
    }

    public void update() {
        alarmedBox.refresh();
    }

    public static void prepareForms(Case currentCase, CallCenterFormsController forms, boolean editable) {
        forms.caseNr.setText(String.valueOf(currentCase.getCaseId()));
        forms.caseNr.setEditable(false);
        forms.editor.setText(!empty(currentCase.getEditor()) ? currentCase.getEditor() : "You");
        forms.editor.setEditable(editable);
        forms.caller.setText(!empty(currentCase.getName()) ? currentCase.getName() : "");
        forms.caller.setEditable(editable);
        forms.phoneNr.setText(!empty(currentCase.getPhone()) ? currentCase.getPhone() : "");
        forms.phoneNr.setEditable(editable);
        forms.streetLabel
                .setText(!empty(currentCase.getStreet()) ? currentCase.getStreet() : "Keine Straße ausgewählt!");
        forms.houseNr.setText(String.valueOf(currentCase.getHouseNr()));
        forms.houseNr.setEditable(editable);
        forms.patient.setText(!empty(currentCase.getPatient()) ? currentCase.getPatient() : "");
        forms.patient.setEditable(editable);
        forms.description.setText(!empty(currentCase.getDescription()) ? currentCase.getDescription() : "");
        forms.description.setEditable(editable);
        forms.reanimation.setSelected(currentCase.needReanimation());
        forms.reanimation.setDisable(!editable);
        forms.callouts.getItems().setAll(CallOuts.values());
        if (currentCase.getCaseTag() != null) {
            forms.callouts.getSelectionModel().select(currentCase.getCaseTag());
        }
        setupCallouts(currentCase, forms);
        forms.callouts.setDisable(!editable);
        forms.extra.setText(!empty(currentCase.getExcessInfo()) ? currentCase.getExcessInfo() : "");
        forms.extra.setEditable(editable);
        forms.streetBtn.setDisable(!editable);
        forms.alarmBtn.setDisable(!editable);
        forms.chrBtn.setDisable(!editable);
        forms.clearBtn.setDisable(!editable);
        forms.naBtn.setDisable(!editable);
        forms.phoneBtn.setDisable(!editable);
        forms.rtwBtn.setDisable(!editable);
        forms.katBtn.setDisable(!editable);
        forms.saveBtn.setDisable(!editable);
        forms.alarmedBox.getItems().setAll(Vehicle.vehicles.get(currentCase.getCaseId()).stream()
                .filter(v -> v.isDispatched()).collect(Collectors.toList()));
        forms.alarmedBox.setDisable(!editable);
        forms.openBox.getItems().setAll(Vehicle.vehicles.get(currentCase.getCaseId()).stream()
                .filter(v -> !v.isDispatched()).collect(Collectors.toList()));
        forms.openBox.setDisable(!editable);
        forms.system.setDisable(!editable);
    }

    private static void setupCallouts(Case currentCase, CallCenterFormsController forms) {
        forms.callouts.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends CallOuts> val, CallOuts prev, CallOuts now) -> {
                    if (emptyStreet(forms.streetLabel)) {
                        return;
                    }
                    forms.clearDispatch();
                    switch (now) {
                    case NOTF:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        break;
                    case NOTF_NA:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        break;
                    case NOTF2_NA:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        break;
                    case NOTF2_NA2:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        break;
                    case KAT:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case KAT2:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case KAT3:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case KAT4:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case NOTF_CHRIS:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        break;
                    case NOTF_NA_CHRIS:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        break;
                    case NOTF2_NA_CHRIS:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        break;
                    case NOTF2_NA2_CHRIS:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        break;
                    case BERG:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case WASSER:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    case VU:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        break;
                    case VU2:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        break;
                    case VU3:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        break;
                    case VU4:
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.RTW, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.NEF, forms.streetLabel.getText()));
                        forms.openBox.getItems().add(Vehicle.create(currentCase.getCaseId(), VehicleType.CHRIS,
                                forms.streetLabel.getText()));
                        forms.openBox.getItems().add(
                                Vehicle.create(currentCase.getCaseId(), VehicleType.KAT, forms.streetLabel.getText()));
                        break;
                    default:
                        break;
                    }
                });
    }

    private static boolean emptyStreet(Label label) {
        if (empty(label.getText()) || label.getText().equals("Keine Straße ausgewählt!")) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Straße fehlt!");
            alert.setContentText("Wähle eine Straße aus");
            alert.setTitle("Street missing error");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    private static boolean empty(String s) {
        return s == null || s.isBlank();
    }

}
