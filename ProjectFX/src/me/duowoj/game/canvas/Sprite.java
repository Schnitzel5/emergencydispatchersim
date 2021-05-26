package me.duowoj.game.canvas;

import javafx.scene.image.Image;
import me.duowoj.game.Settings;
import javafx.scene.canvas.GraphicsContext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Sprite {

    private static final List<Sprite> sprites = new ArrayList<>();
    
    private Image image;

    private double positionX;
    private double positionY;

    private double width;
    private double height;

    public Sprite() {
        positionX = 0;
        positionY = 0;
        sprites.add(this);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setImage(String filename) {
        Image i = null;
        try {
            i = new Image(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setImage(i);
    }

    public void setImage(InputStream in) {
        Image i = new Image(in, Settings.settings.getWidth(), Settings.settings.getHeight(), true, true);
        setImage(i);
    }

    public void setImage(Image i) {
        image = i;
    }

    public void translateBy(double x, double y) {
        positionX = positionX + x;
        positionY = positionY + y;
    }

    public void setPosition(double x, double y) {
        positionX = x;
        positionY = y;
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(image, positionX, positionY);
    }

    public String toString() {
        return " Position: [" + positionX + "," + positionY + "]";
    }
    
    public static List<Sprite> getSprites() {
        return sprites;
    }

}
