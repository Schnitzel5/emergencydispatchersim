package me.duowoj.game.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import me.duowoj.game.Game;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;

public class CallCenterOverviewController extends Controller {

    private final static ObservableList<Case> openList = FXCollections.observableArrayList();
    private final static ObservableList<Case> doneList = FXCollections.observableArrayList();
    public static CallCenterOverviewController overview;

    @FXML
    private ListView<Case> openBox;
    @FXML
    private ListView<Case> doneBox;
    @FXML
    private Button editBtn;
    @FXML
    private Button archiveBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button openBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        overview = this;
        openList.setAll(Case.cases.values().stream().filter(c -> c.isAccepted() && !c.isArchived())
                .collect(Collectors.toList()));
        doneList.setAll(Case.cases.values().stream().filter(c -> c.isAccepted() && c.isArchived())
                .collect(Collectors.toList()));
        openBox.setItems(openList);
        doneBox.setItems(doneList);
        openBox.setCellFactory(cell -> new ListCell<Case>() {
            @Override
            protected void updateItem(Case item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setMinWidth(cell.getWidth());
                    setMaxWidth(cell.getWidth());
                    setPrefWidth(cell.getWidth());
                    setWrapText(true);
                    setText(item.toString() + " [" + Math.round(item.progress()) + "%/100%]");
                } else {
                    setGraphic(null);
                    setText(null);
                }
            };
        });
        doneBox.setCellFactory(cell -> new ListCell<Case>() {
            @Override
            protected void updateItem(Case item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setMinWidth(cell.getWidth());
                    setMaxWidth(cell.getWidth());
                    setPrefWidth(cell.getWidth());
                    setWrapText(true);
                    setText(item.toString() + " [" + Math.round(item.progress()) + "%]");
                } else {
                    setGraphic(null);
                    setText(null);
                }
            };
        });
    }

    @FXML
    public void editCase() {
        Case selectedCase = openBox.getSelectionModel().getSelectedItem();
        if (selectedCase != null) {
            int caseId = selectedCase.getCaseId();
            try {
                if (Game.getStages().containsKey("Leitstelle: " + caseId)) {
                    return;
                }
                Game.loadScene("call_center_forms", "Leitstelle: " + caseId, (e1) -> {
                    ((CallCenterFormsController) Game.getControllers().get("Leitstelle: " + caseId)).saveAfter();
                });
                ((CallCenterFormsController) Game.getControllers().get("Leitstelle: " + caseId)).currentCaseID = caseId;
                CallCenterFormsController.prepareForms(selectedCase,
                        (CallCenterFormsController) Game.getControllers().get("Leitstelle: " + caseId), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void archiveCase() {
        Case selectedCase = doneBox.getSelectionModel().getSelectedItem();
        if (selectedCase != null) {
            if (!selectedCase.completed()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Nicht fertig!");
                alert.setContentText("Es sind noch nicht alle Fahrzeuge angekommen!");
                alert.showAndWait();
                return;
            }
            int caseId = selectedCase.getCaseId();
            if (Game.controlCanvas.containsKey(caseId)) {
                Game.controlCanvas.get(caseId).stop();
            }
            if (Game.getStages().containsKey("Leitstelle: " + caseId)) {
                Game.getStages().get("Leitstelle: " + caseId).close();
                Game.getStages().remove("Leitstelle: " + caseId);
                Game.getControllers().remove("Leitstelle: " + caseId);
            }
            if (Game.getStages().containsKey("Geo Informations System | " + caseId)) {
                Game.getStages().get("Geo Informations System | " + caseId).close();
                Game.getStages().remove("Geo Informations System | " + caseId);
            }
            Case c = CallCenterController.callCenter.getCurrentCase();
            if (c != null && c.getCaseId() == caseId) {
                CallCenterController.callCenter.disconnect();
            }
            Case.cases.remove(caseId);
            Vehicle.vehicles.remove(caseId);
            doneBox.getSelectionModel().clearSelection();
            refresh();
        }
    }

    @FXML
    public void deleteCase() {
        Case selectedCase = openBox.getSelectionModel().getSelectedItem();
        if (selectedCase != null) {
            int caseId = selectedCase.getCaseId();
            if (Game.getStages().containsKey("Leitstelle: " + caseId)) {
                Game.getStages().get("Leitstelle: " + caseId).close();
                Game.getStages().remove("Leitstelle: " + caseId);
                Game.getControllers().remove("Leitstelle: " + caseId);
            }
            if (Game.getStages().containsKey("Geo Informations System | " + caseId)) {
                Game.getStages().get("Geo Informations System | " + caseId).close();
                Game.getStages().remove("Geo Informations System | " + caseId);
            }
            Case call = CallCenterController.callCenter.getCurrentCase();
            if (call != null && call.getCaseId() == caseId) {
                CallCenterController.callCenter.disconnect();
            }
            openList.remove(openBox.getSelectionModel().getSelectedIndex());
            Vehicle.destroy(caseId);
            Case.cases.remove(caseId);
            doneBox.getSelectionModel().clearSelection();
            refresh();
        }
    }

    @FXML
    public void openCase() {
        Case selectedCase = doneBox.getSelectionModel().getSelectedItem();
        if (selectedCase != null) {
            try {
                if (Game.getStages().containsKey("Leitstelle: " + selectedCase.getCaseId())) {
                    return;
                }
                Game.loadScene("call_center_forms", "Leitstelle: " + selectedCase.getCaseId(), (e1) -> {
                });
                ((CallCenterFormsController) Game.getControllers()
                        .get("Leitstelle: " + selectedCase.getCaseId())).currentCaseID = selectedCase.getCaseId();
                CallCenterFormsController.prepareForms(selectedCase, (CallCenterFormsController) Game.getControllers()
                        .get("Leitstelle: " + selectedCase.getCaseId()), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void refresh() {
        Case open = openBox.getSelectionModel().getSelectedItem();
        Case done = doneBox.getSelectionModel().getSelectedItem();
        openList.setAll(Case.cases.values().stream().filter(c -> c.isAccepted() && !c.isArchived())
                .collect(Collectors.toList()));
        doneList.setAll(Case.cases.values().stream().filter(c -> c.isAccepted() && c.isArchived())
                .collect(Collectors.toList()));
        openBox.refresh();
        doneBox.refresh();
        if (open != null) {
            openBox.getSelectionModel().select(open);
        }
        if (done != null) {
            doneBox.getSelectionModel().select(done);
        }
    }
    
    public static void clear() {
        openList.clear();
        doneList.clear();
    }

}
