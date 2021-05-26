package me.duowoj.game.intern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import me.duowoj.game.controller.MainMenuController;
import me.duowoj.game.model.Location;

public class Loader {

    private final static AtomicBoolean interrupt = new AtomicBoolean();
    public final static List<String> streets = new ArrayList<>();
    public final static List<Location> locs = new ArrayList<>();
    public final static List<String[]> cases = new ArrayList<>();
    public static final List<Point2D> transloc = new ArrayList<>();

    public static void loadCases(InputStream in) {
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            reader.readLine();
            while ((line = reader.readLine()) != null && !interrupt.get()) {
                String[] data = Arrays.asList(line.split("\t")).stream().skip(2).collect(Collectors.toList())
                        .toArray(new String[0]);
                cases.add(data);
                Platform.runLater(() -> {
                    MainMenuController.bar.setVisible(true);
                    MainMenuController.bar.setProgress((streets.size() + cases.size()) / (6849.0 + 30.0));
                });
                if (streets.size() + cases.size() == 6849 + 30) {
                    Platform.runLater(() -> {
                        MainMenuController.newBtn.setVisible(true);
                        MainMenuController.loadBtn.setVisible(true);
                        MainMenuController.quitBtn.setVisible(true);
                        MainMenuController.bar.setVisible(false);
                    });
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            interrupt.set(false);
        }
    }

    public static void loadStreets(InputStream in) {
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null && !interrupt.get()) {
                if (line.startsWith("Unbenannte Verkehrsfläche")) {
                    continue;
                }
                // System.out.println(line);
                String[] data = line.split(";");
                if (streets.contains(data[0])) {
                    continue;
                }
                List<Location> locations = new ArrayList<>();
                String[] tempLocs = data[1].replace("[", "").replace("]", "").split(", ");
                for (String loc : tempLocs) {
                    double latitude = Double.parseDouble(loc.split(" ")[0]);
                    double longitude = Double.parseDouble(loc.split(" ")[1]);
                    locations.add(new Location(latitude, longitude));
                }
                streets.add(data[0]);
                locs.add(locations.get(0));

                /*
                 * System.out.println("NAME: " + data[0] + "   LOCS: " +
                 * locations.stream().map(l -> l.getLatitude() + " " + l.getLongitude())
                 * .collect(Collectors.toList()).toString() + "   D: " + streets.size());
                 */

                Platform.runLater(() -> {
                    MainMenuController.bar.setVisible(true);
                    MainMenuController.bar.setProgress((streets.size() + cases.size()) / (6849.0 + 30.0));
                });
                if (streets.size() + cases.size() == 6849 + 30) {
                    Platform.runLater(() -> {
                        MainMenuController.newBtn.setVisible(true);
                        MainMenuController.loadBtn.setVisible(true);
                        MainMenuController.quitBtn.setVisible(true);
                        MainMenuController.bar.setVisible(false);
                    });
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            interrupt.set(false);
        }
        for (Location street : Loader.locs) {
            if (interrupt.get()) {
                break;
            }
            double[] current = convertLatLong(street);
            transloc.add(new Point2D(current[0] / 1000d, current[1] / 763d));
        }
    }

    public static void translateLocs() {
        for (int i = 0; i < transloc.size(); i++) {
            // transloc.set(i, new Point2D(transloc.get(i).getX() * Settings.settings.getWidth(), transloc.get(i).getY() * Settings.settings.getHeight()));
            transloc.set(i, new Point2D(transloc.get(i).getX() * 1000d, transloc.get(i).getY() * 763d));
        }
    }

    /*
     * OLD IMPORTER public static void load(File file) { String line; try
     * (BufferedReader reader = new BufferedReader(new FileReader(file))) {
     * reader.readLine(); while ((line = reader.readLine()) != null &&
     * !interrupt.get()) { // System.out.println(line); String[] data =
     * line.split(","); List<Location> locations = new ArrayList<>(); for (int i =
     * 3; i < data.length - 17; i++) { double latitude = Double
     * .parseDouble(data[3].replace("\"MULTILINESTRING ((", "").replace("))\"",
     * "").split(" ")[0]); double longitude = Double
     * .parseDouble(data[3].replace("\"MULTILINESTRING ((", "").replace("))\"",
     * "").split(" ")[1]); locations.add(new Location(latitude, longitude)); }
     * streets.add(data[data.length - 16]); locs.add(locations);
     * 
     * System.out.println("NAME: " + data[data.length - 16] + "   LOCS: " +
     * locations.stream().map(l -> l.getLatitude() + " " + l.getLongitude())
     * .collect(Collectors.toList()).toString() + "   D: " + streets.size());
     * 
     * Platform.runLater(() -> { MainMenuController.bar.setVisible(true);
     * MainMenuController.bar.setProgress(streets.size() / 28483.0); }); if
     * (streets.size() == 28483) { Platform.runLater(() -> {
     * MainMenuController.newBtn.setVisible(true);
     * MainMenuController.loadBtn.setVisible(true);
     * MainMenuController.settingsBtn.setVisible(true);
     * MainMenuController.bar.setVisible(false); }); } } } catch (IOException
     * exception) { exception.printStackTrace(); } finally { interrupt.set(false); }
     * }
     */

    public static void interrupt() {
        interrupt.set(true);
    }

    private static double[] convertLatLong(Location location) {
        final double cx = 0.000437004;
        final double cy = -0.000291327;
        final double b = 48.20701;
        final double y = 402;
        final double l = 16.42086;
        final double x = 582;
//        double q = 16.381390;
//        double r = 48.255512;
        double[] xy = new double[2];
        xy[0] = ((location.getLatitude() - l) / cx) + x + 10;
        xy[1] = ((location.getLongitude() - b) / cy) + y + 27;
//        System.out.println(xy[0] + " " + xy[1] + " " + temp.size() + " " + Loader.locs.size());
        return xy;
    }
    
}
