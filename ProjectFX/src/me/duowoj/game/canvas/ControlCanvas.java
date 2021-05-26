package me.duowoj.game.canvas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.controller.CallCenterFormsController;
import me.duowoj.game.intern.Loader;
import me.duowoj.game.model.Vehicle;

public class ControlCanvas {

    private Timeline tl;
    private int caseID;
    private Sprite background;

    public ControlCanvas(int caseID) {
        this.caseID = caseID;
    }

    public void loadCanvas() {
        if (Game.getStages().containsKey("Geo Informations System | " + caseID)) {
            Game.getStages().get("Geo Informations System | " + caseID).toFront();
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Geo Informations System | " + caseID);
        Canvas canvas = new Canvas(Settings.settings.getWidth(), Settings.settings.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        tl = new Timeline(new KeyFrame(Duration.millis(1000), e -> run(gc)));
        tl.setCycleCount(Timeline.INDEFINITE);

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
            Game.getStages().remove("Geo Informations System | " + caseID);
            Game.controlCanvas.remove(caseID);
        });

        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setResizable(false);
        stage.show();
        Game.getStages().put("Geo Informations System | " + caseID, stage);

        background = new Sprite();
        background.setImage(getClass().getClassLoader().getResourceAsStream("sprites/map.png"));

        tl.play();
    }

    public void stop() {
        tl.stop();
        if (Game.getStages().containsKey("Geo Informations System | " + caseID)) {
            Game.getStages().get("Geo Informations System | " + caseID).close();
            Game.getStages().remove("Geo Informations System | " + caseID);
            Game.controlCanvas.remove(caseID);
        }
    }

    private void run(GraphicsContext gc) {
        if (!Game.isRunning()) {
            tl.stop();
            return;
        }
        gc.fillRect(0, 0, Settings.settings.getWidth(), Settings.settings.getHeight());

        background.render(gc);
        
        CallCenterFormsController controller = (CallCenterFormsController) Game.getControllers()
                .get("Leitstelle: " + caseID);
        if (controller != null && !controller.streetLabel.getText().equals("Keine Straße ausgewählt!")) {
            int index = Loader.streets.indexOf(controller.streetLabel.getText());
            if (index == -1) {
                return;
            }
            Point2D currentPos = Loader.transloc.get(index);
            String destination = controller.streetLabel.getText();
            gc.setFill(Color.RED);
            gc.fillOval((currentPos.getX() - 5) * Settings.settings.getRW(), (currentPos.getY() - 5) * Settings.settings.getRH(), 5 * Settings.settings.getRW(), 5 * Settings.settings.getRH());
            double x = (currentPos.getX() - 15 - 2 * destination.length()) * Settings.settings.getRW();
            double y = (currentPos.getY() - 20) * Settings.settings.getRH();
            gc.setFill(Color.CRIMSON);
            gc.fillRect(x, y, (30 + 4 * destination.length()) * Settings.settings.getRW(), 16 * Settings.settings.getRH());
            gc.setFill(Color.WHITE);
            gc.fillText(destination, x, y + 10 * Settings.settings.getRH(), (29 + 4 * destination.length()) * Settings.settings.getRW());
        }
        
        Vehicle.vehicles.get(caseID).forEach(v -> v.render(gc));
    }

}
