package me.duowoj.game.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.model.Case;
import me.duowoj.game.types.SimSpeed;

public class CallCenterController extends Controller {

    private static ObservableList<Case> cases = FXCollections.observableArrayList();
    public final static ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    public final static AtomicBoolean dnd = new AtomicBoolean();
    public static CallCenterController callCenter;

    private Case currentCase;

    @FXML
    private ListView<String> chatBox;
    @FXML
    private Button acceptFirstCaseBtn;
    @FXML
    private Button acceptCaseBtn;
    @FXML
    private Button disconnectBtn;
    @FXML
    private Button clearChatBtn;
    @FXML
    private Button dndBtn;
    @FXML
    private Button overviewBtn;
    @FXML
    private Button greetBtn;
    @FXML
    private Button callerBtn;
    @FXML
    private Button patientBtn;
    @FXML
    private Button locBtn;
    @FXML
    private Button eventBtn;
    @FXML
    private Button speakBtn;
    @FXML
    private Button breathBtn;
    @FXML
    private Button hurtBtn;
    @FXML
    private Button ageBtn;
    @FXML
    private Button preBtn;
    @FXML
    private Button helpBtn;
    @FXML
    public Button simspeed;

    @FXML
    private ListView<Case> listCase;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        callCenter = this;
        listCase.setItems(cases);
        listCase.refresh();
        deactivateAll();
        chatBox.setCellFactory(cell -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setMinWidth(cell.getWidth());
                    setMaxWidth(cell.getWidth());
                    setPrefWidth(cell.getWidth());
                    setWrapText(true);
                    setText(item.toString());
                } else {
                    setGraphic(null);
                    setText(null);
                }
            };
        });
        chatBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            chatBox.getSelectionModel().clearSelection();
        });
        dnd.set(!Settings.settings.isSet("dnd"));
        dnd();
    }

    public Case getCurrentCase() {
        return currentCase;
    }
    
    public void setCurrentCase(int caseID) {
        currentCase = Case.cases.getOrDefault(caseID, null);
    }

    @FXML
    public void acceptFirstCase() {
        if (cases.size() > 0 && currentCase == null) {
            currentCase = cases.get(0);
            currentCase.setAccepted(true);
            cases.remove(0);
            chatBox.getItems().clear();
            activateAll();
            if (CallCenterOverviewController.overview != null) {
                CallCenterOverviewController.overview.refresh();
            }
        }
    }

    @FXML
    public void acceptCase() {
        Case selectedCase = listCase.getSelectionModel().getSelectedItem();
        if (cases.size() > 0 && currentCase == null && selectedCase != null) {
            currentCase = selectedCase;
            currentCase.setAccepted(true);
            cases.remove(selectedCase);
            chatBox.getItems().clear();
            activateAll();
            if (CallCenterOverviewController.overview != null) {
                CallCenterOverviewController.overview.refresh();
            }
        }
    }

    @FXML
    public void disconnect() {
        if (currentCase == null) {
            return;
        }
        deactivateAll();
        currentCase = null;
    }

    @FXML
    public void clearChat() {
        chatBox.getItems().clear();
        chatBox.refresh();
    }

    @FXML
    public void dnd() {
        if (dnd.get()) {
            dndBtn.setText("Allow Disturb");
            dnd.set(false);
        } else {
            dndBtn.setText("Do Not Disturb");
            dnd.set(true);
        }
    }
    
    public void dnd(boolean dnd) {
        if (dnd) {
            dndBtn.setText("Do Not Disturb");
            CallCenterController.dnd.set(true);
        } else {
            dndBtn.setText("Allow Disturb");
            CallCenterController.dnd.set(false);
        }
    }

    @FXML
    public void openOverview() throws IOException {
        Game.loadScene("call_center_overview", "Übersicht", (e) -> {
            CallCenterOverviewController.overview = null;
        });
    }
    
    @FXML
    public void greet() {
        chatBox.getItems().add("Guten Tag! Wie kann ich Ihnen helfen?");
        receiveMsg("greeting", 450, 450 + 42 * currentCase.getMessage("event").length());
    }

    @FXML
    public void caller() {
        chatBox.getItems().add("Wie ist Ihr Name?");
        receiveMsg("call", 150, 420);
    }

    @FXML
    public void patient() {
        chatBox.getItems().add("Wie lautet der Name der Betroffenen?");
        receiveMsg("patient", 210, 520);
    }

    @FXML
    public void place() {
        chatBox.getItems().add("Wo genau ist der Notfallort?");
        receiveMsg("place", 510, 1900);
    }

    @FXML
    public void event() {
        chatBox.getItems().add("Was ist genau passiert?");
        receiveMsg("event", 540, 540 + 42 * currentCase.getMessage("event").length());
    }

    @FXML
    public void speak() {
        chatBox.getItems().add("Ist er ansprechbar?");
        receiveMsg("speak", 230, 1300);
    }

    @FXML
    public void breath() {
        chatBox.getItems().add("Ist die Atmung vorhanden oder sind Atembeschwerden vorhanden?");
        receiveMsg("breath", 390, 4000);
    }

    @FXML
    public void hurt() {
        chatBox.getItems().add("Treten Schmerzen auf, wo genau?");
        receiveMsg("hurt", 390, 2100);
    }

    @FXML
    public void age() {
        chatBox.getItems().add("Wie alt ist er?");
        receiveMsg("age", 200, 730);
    }

    @FXML
    public void pre() {
        chatBox.getItems().add("Hat der Betroffene Vorerkrankungen?");
        receiveMsg("pre", 480, 1900);
    }

    @FXML
    public void help() {
        chatBox.getItems().add("Die Rettung kommt!");
        receiveMsg("help", 190, 620);
    }

    @FXML
    public void simspeedUp() {
        Game.speed = SimSpeed.getNextSpeed(Game.speed);
        simspeed.setText("SimSpeed: " + Game.speed.getDisplay());
    }
    
    public void receiveMsg(String key, int min, int max) {
        deactivateAll();
        Random random = new Random();
        service.schedule(() -> {
            Platform.runLater(() -> {
                chatBox.getItems().add(currentCase.getMessage(key));
                chatBox.scrollTo(chatBox.getItems().size() - 1);
                activateAll();
            });
        }, random.nextInt(max - min) + min, TimeUnit.MILLISECONDS);
    }

    public void activateAll() {
        greetBtn.setDisable(false);
        callerBtn.setDisable(false);
        patientBtn.setDisable(false);
        locBtn.setDisable(false);
        eventBtn.setDisable(false);
        speakBtn.setDisable(false);
        breathBtn.setDisable(false);
        hurtBtn.setDisable(false);
        ageBtn.setDisable(false);
        preBtn.setDisable(false);
        helpBtn.setDisable(false);
    }

    public void deactivateAll() {
        greetBtn.setDisable(true);
        callerBtn.setDisable(true);
        patientBtn.setDisable(true);
        locBtn.setDisable(true);
        eventBtn.setDisable(true);
        speakBtn.setDisable(true);
        breathBtn.setDisable(true);
        hurtBtn.setDisable(true);
        ageBtn.setDisable(true);
        preBtn.setDisable(true);
        helpBtn.setDisable(true);
    }

    public static ObservableList<Case> getCaseList() {
        return cases;
    }

}
