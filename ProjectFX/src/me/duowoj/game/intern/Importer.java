package me.duowoj.game.intern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.controller.CallCenterController;
import me.duowoj.game.controller.CallCenterFormsController;
import me.duowoj.game.controller.CallCenterOverviewController;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;
import me.duowoj.game.types.CallOuts;
import me.duowoj.game.types.SimSpeed;
import me.duowoj.game.types.VehicleType;

public class Importer {

    public static void importSim(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int caseID = -1;
            boolean dnd = false;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(";");
                switch (data[0]) {
                case "C":
                    Case c = new Case(Integer.parseInt(data[1]), data[11], data[12], data[13], data[14], data[15],
                            data[16], data[17], data[18], data[19]);
                    if (!data[2].equals("null")) {
                        c.setCaseTag(CallOuts.valueOf(data[2]));
                    }
                    if (!data[3].equals("null")) {
                        c.setDescription(data[3]);
                    }
                    if (!data[4].equals("null")) {
                        c.setEditor(data[4]);
                    }
                    if (!data[5].equals("null")) {
                        c.setExcessInfo(data[5]);
                    }
                    c.setHouseNr(Integer.parseInt(data[6]));
                    if (!data[7].equals("null")) {
                        c.setName(data[7]);
                    }
                    if (!data[8].equals("null")) {
                        c.setPatient(data[8]);
                    }
                    if (!data[9].equals("null")) {
                        c.setPhone(data[9]);
                    }
                    if (!data[10].equals("null")) {
                        c.setStreet(data[10]);
                    }
                    c.setAccepted(Boolean.parseBoolean(data[20]));
                    c.setArchived(Boolean.parseBoolean(data[21]));
                    c.setReanimation(Boolean.parseBoolean(data[22]));
                    c.setMessage("place", data[23]);
                    c.setMessage("phone", data[24]);
                    break;
                case "V":
                    Point2D destPos = new Point2D(Double.parseDouble(data[4].split(",")[0]),
                            Double.parseDouble(data[4].split(",")[1]));
                    Point2D startPos = new Point2D(Double.parseDouble(data[5].split(",")[0]),
                            Double.parseDouble(data[5].split(",")[1]));
                    Point2D currentPos = new Point2D(Double.parseDouble(data[6].split(",")[0]),
                            Double.parseDouble(data[6].split(",")[1]));
                    Vehicle.load(Integer.parseInt(data[1]), VehicleType.valueOf(data[2]), data[3], destPos, startPos,
                            currentPos, Double.parseDouble(data[7]));
                    break;
                case "CL":
                    if (Case.cases.containsKey(Integer.parseInt(data[1]))) {
                        CallCenterController.getCaseList().add(Case.cases.get(Integer.parseInt(data[1])));
                    }
                    break;
                case "CLR":
                    caseID = Integer.parseInt(data[1]);
                    break;
                case "SH":
                    Settings.settings.setHeight(Integer.parseInt(data[1]));
                    break;
                case "SW":
                    Settings.settings.setWidth(Integer.parseInt(data[1]));
                    break;
                case "HK":
                    Settings.settings.getHotKeys().put(data[1], KeyCode.valueOf(data[2]));
                    break;
                case "HB":
                    Settings.settings.getSetting().put(data[1], Boolean.valueOf(data[2]));
                    break;
                case "GS":
                    Game.speed = SimSpeed.valueOf(data[1]);
                    break;
                case "DND":
                    dnd = Boolean.parseBoolean(data[1]);
                    break;
                case "COUNTER":
                    Case.setCounter(Integer.parseInt(data[1]));
                    break;
                }
            }
            Settings.settings.setPath(file.getAbsolutePath());
            run(caseID, dnd);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void run(int caseID, boolean dnd) throws IOException {
        Loader.translateLocs();
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
                Exporter.export(new File(new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath(),
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
        if (caseID != -1) {
            CallCenterController.callCenter.setCurrentCase(caseID);
            CallCenterController.callCenter.activateAll();
        }
        CallCenterController.callCenter.dnd(dnd);
        CallCenterController.callCenter.simspeed.setText("SimSpeed: " + Game.speed.getDisplay());
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
    }

}
