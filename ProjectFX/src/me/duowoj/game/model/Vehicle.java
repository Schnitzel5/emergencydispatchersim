package me.duowoj.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.canvas.Sprite;
import me.duowoj.game.intern.Loader;
import me.duowoj.game.types.VehicleType;

public class Vehicle extends Sprite {

    public final static HashMap<Integer, List<Vehicle>> vehicles = new HashMap<>();

    private VehicleType type;
    private String destination;
    private Point2D destPos;
    private Point2D startPos;
    private Point2D currentPos;
    // private final List<Point2D> route;
    private double speed;

    private Vehicle(VehicleType type, String destination) {
        this.type = type;
        this.destination = destination;
        // route = new ArrayList<>();
        startPos = Loader.transloc.get(ThreadLocalRandom.current().nextInt(Loader.transloc.size()));
        currentPos = new Point2D(startPos.getX(), startPos.getY());
        destPos = Loader.transloc.get(Loader.streets.indexOf(destination));
        /*
         * route.addAll(Loader.transloc.stream().filter(p -> check(p)).sorted((p1, p2)
         * -> { return (int) Math.round(p1.subtract(p2).getX() +
         * p1.subtract(p2).getY()); }).collect(Collectors.toList()));
         * System.out.println(route.toString() + "   " + currentPos.toString() + "   " +
         * destPos.toString());
         */
    }

    public Vehicle(VehicleType type, String destination, Point2D destPos, Point2D startPos, Point2D currentPos,
            double speed) {
        this.type = type;
        this.destination = destination;
        this.destPos = destPos;
        this.startPos = startPos;
        this.currentPos = currentPos;
        this.speed = speed;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    /*
     * public List<Point2D> getRoute() { return route; }
     */

    public Point2D getDestPos() {
        return destPos;
    }

    public Point2D getStartPos() {
        return startPos;
    }

    public Point2D getCurrentPos() {
        return currentPos;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval((currentPos.getX() - 5) * Settings.settings.getRW(),
                (currentPos.getY() - 5) * Settings.settings.getRH(), 5 * Settings.settings.getRW(),
                5 * Settings.settings.getRH());
        double x = (currentPos.getX() - 15 - 2 * destination.length()) * Settings.settings.getRW();
        double y = (currentPos.getY() - 20) * Settings.settings.getRH();
        gc.setFill(Color.BEIGE);
        gc.fillRect(x, y, (30 + 4 * destination.length()) * Settings.settings.getRW(), 16 * Settings.settings.getRH());
        gc.setFill(Color.BLACK);
        gc.fillText(type.name() + " -> " + destination, x, y + 10 * Settings.settings.getRH(),
                (29 + 4 * destination.length()) * Settings.settings.getRW());
    }

    public void dispatch() {
        switch (type) {
        case CHRIS:
            speed = 0.00012;
            break;
        case KAT:
            speed = 0.00042;
            break;
        case NEF:
            speed = 0.00007;
            break;
        case RTW:
            speed = 0.000095;
            break;
        default:
        }
        speed *= (Settings.settings.getWidth() + Settings.settings.getHeight());
    }

    public boolean isDispatched() {
        return speed != 0;
    }

    public void move() {
        currentPos = currentPos.add(speed * (currentPos.getX() < destPos.getX() ? 1 : -1) * Game.speed.getRate(),
                speed * (currentPos.getY() < destPos.getY() ? 1 : -1) * Game.speed.getRate());
        if (isArrived()) {
            currentPos = new Point2D(destPos.getX(), destPos.getY());
            return;
        }
    }

    public boolean isArrived() {
        return currentPos.distance(destPos) < destPos.distance(destPos.getX() + speed * Game.speed.getRate(),
                destPos.getY() + speed * Game.speed.getRate());
    }

    public int progress() {
        return (int) (100 - Math.round(currentPos.distance(destPos) / startPos.distance(destPos) * 100));
    }

    @Override
    public String toString() {
        return type.getDes() + " [" + progress() + "%/100%]";
    }

    /*
     * private boolean check(Point2D p) { return (p.getX() > destPos.getX() &&
     * p.getX() <= destPos.getX() && p.getY() > currentPos.getY() && p.getY() <=
     * destPos.getY()) || (p.getX() < destPos.getX() && p.getX() >= destPos.getX()
     * && p.getY() < currentPos.getY() && p.getY() >= destPos.getY()); }
     */

    public static Vehicle create(int caseID, VehicleType type, String destination) {
        Vehicle vehicle = new Vehicle(type, destination);
        vehicles.putIfAbsent(caseID, new ArrayList<>());
        vehicles.get(caseID).add(vehicle);
        return vehicle;
    }

    public static void load(int caseID, VehicleType type, String destination, Point2D destPos, Point2D startPos,
            Point2D currentPos, double speed) {
        Vehicle vehicle = new Vehicle(type, destination, destPos, startPos, currentPos, speed);
        vehicles.putIfAbsent(caseID, new ArrayList<>());
        vehicles.get(caseID).add(vehicle);
    }

    public static void destroy(int caseID) {
        vehicles.get(caseID).clear();
    }

}
