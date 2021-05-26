package me.duowoj.game;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.duowoj.game.canvas.ControlCanvas;
import me.duowoj.game.controller.Controller;
import me.duowoj.game.intern.CallBack;
import me.duowoj.game.intern.CaseGenerator;
import me.duowoj.game.intern.Loader;
import me.duowoj.game.intern.VehicleHandler;
import me.duowoj.game.types.SimSpeed;

public class Game extends Application {

    private final static AtomicBoolean running = new AtomicBoolean();
    private final static Map<String, Stage> stages = new HashMap<>();
    private final static Map<String, Controller> controllers = new HashMap<>();
    private static Stage primaryStage;
    public final static Map<String, URL> files = new HashMap<>();
    public static CaseGenerator generator;
    public static VehicleHandler vehicleHandler;
    public final static Map<Integer, ControlCanvas> controlCanvas = new HashMap<>();
    public static SimSpeed speed = SimSpeed.NORMAL;

    @Override
    public void start(Stage primaryStage) throws IOException {
        loadFile("fxml/main_menu.fxml");
        loadFile("fxml/main_menu_settings.fxml");
        loadFile("fxml/call_center.fxml");
        loadFile("fxml/call_center_overview.fxml");
        loadFile("fxml/call_center_forms.fxml");
        loadFile("fxml/general.css");
        Parent root = FXMLLoader.load(files.get("fxml/main_menu.fxml"));
        Scene scene = new Scene(root);

        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
            Loader.interrupt();
            Game.getStages().values().forEach(Stage::close);
            System.exit(0);
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Game");
        primaryStage.show();
        Game.primaryStage = primaryStage;

        Thread loadData = new Thread(() -> {
            Loader.loadStreets(getClass().getClassLoader().getResourceAsStream("data/streets.csv"));
            Loader.loadCases(getClass().getClassLoader().getResourceAsStream("data/cases.csv"));
        });
        loadData.start();
    }

    private void loadFile(String path) {
        files.put(path, getClass().getClassLoader().getResource(path));
    }

    /*
     * public static void startSimulator() { try { running.set(true); generator =
     * new CaseGenerator(); loadScene("call_center", "Anrufszentrum");
     * stages.get("call_center").addEventHandler(KeyEvent.KEY_PRESSED, e -> { if
     * (e.getCode() == Settings.settings.getHotKeys().getOrDefault("1", KeyCode.H))
     * { try { loadScene("call_center_forms", "Leitstelle"); } catch (IOException
     * e1) { e1.printStackTrace(); } } }); controlCanvas = new ControlCanvas();
     * //controlCanvas.loadCanvas(); primaryStage.close(); } catch (IOException e) {
     * e.printStackTrace(); running.set(false); } }
     */

    public static void loadScene(String name, String displayName, CallBack call) throws IOException {
        if (stages.containsKey(displayName)) {
            stages.get(displayName).toFront();
            return;
        }
        FXMLLoader loader = new FXMLLoader(files.get("fxml/" + name + ".fxml"));
        Parent root = (Parent) loader.load();
        root.getStylesheets().add(files.get("fxml/general.css").toExternalForm());
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(displayName);
        stage.setResizable(false);
        stages.put(displayName, stage);
        controllers.put(displayName, loader.getController());
        stage.show();
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
            call.run(e);
            stages.remove(displayName);
            controllers.remove(displayName);
        });
    }

    public synchronized static void setRunning(boolean running) {
        Game.running.set(running);
    }

    public synchronized static boolean isRunning() {
        return running.get();
    }

    public static Map<String, Stage> getStages() {
        return stages;
    }
    
    public static Map<String, Controller> getControllers() {
        return controllers;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
