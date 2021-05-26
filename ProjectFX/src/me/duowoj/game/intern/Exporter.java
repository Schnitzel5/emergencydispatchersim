package me.duowoj.game.intern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import javafx.scene.input.KeyCode;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.controller.CallCenterController;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;

public class Exporter {

    public static void export(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter fw = new FileWriter(file, false)) {
            fw.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (Entry<Integer, Case> e : Case.cases.entrySet()) {
                Case c = e.getValue();
                writer.println(String.join(";", "C", String.valueOf(c.getCaseId()),
                        c.getCaseTag() != null ? c.getCaseTag().name() : "null", c.getDescription(), c.getEditor(),
                        c.getExcessInfo(), String.valueOf(c.getHouseNr()), c.getName(), c.getPatient(), c.getPhone(),
                        c.getStreet()) + ";" + String.join(";", c.getDialogues()) + ";"
                        + String.join(";", String.valueOf(c.isAccepted()), String.valueOf(c.isArchived()),
                                String.valueOf(c.needReanimation()), c.getMessage("place"), c.getMessage("phone")));
            }
            for (Entry<Integer, List<Vehicle>> e : Vehicle.vehicles.entrySet()) {
                for (Vehicle v : e.getValue()) {
                    writer.println(String.join(";", "V", String.valueOf(e.getKey()), v.getType().name(),
                            v.getDestination(), v.getDestPos().getX() + "," + v.getDestPos().getY(),
                            v.getStartPos().getX() + "," + v.getStartPos().getY(),
                            v.getCurrentPos().getX() + "," + v.getCurrentPos().getY(), String.valueOf(v.getSpeed())));
                }
            }
            for (Case c : CallCenterController.getCaseList()) {
                writer.println("CL;" + c.getCaseId());
            }
            Case clr = CallCenterController.callCenter.getCurrentCase();
            if (clr != null) {
                writer.println("CLR;" + clr.getCaseId());
            }
            writer.println("SH;" + Settings.settings.getHeight());
            writer.println("SW;" + Settings.settings.getWidth());
            for (Entry<String, KeyCode> e : Settings.settings.getHotKeys().entrySet()) {
                writer.println(String.join(";", "HK", e.getKey(), e.getValue().name()));
            }
            for (Entry<String, Boolean> e : Settings.settings.getSetting().entrySet()) {
                writer.println(String.join(";", "HB", e.getKey(), e.getValue().toString()));
            }
            writer.println("GS;" + Game.speed.name());
            writer.println("DND;" + CallCenterController.dnd.get());
            writer.println("COUNTER;" + Case.getCounter());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
