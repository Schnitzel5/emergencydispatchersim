package me.duowoj.game;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.input.KeyCode;

public class Settings {

    public final static Settings settings = new Settings();
    
    private String path;
    private final Map<String, Boolean> setting;
    private final Map<String, KeyCode> hotKeys;
    private int width;
    private int height;
    
    private Settings() {
        setting = new HashMap<>();
        hotKeys = new HashMap<>();
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCanvas(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public double getRW() {
        return width / 1000d;
    }
    
    public double getRH() {
        return height / 763d;
    }

    public Map<String, Boolean> getSetting() {
        return setting;
    }
    
    public Map<String, KeyCode> getHotKeys() {
        return hotKeys;
    }
    
    public boolean isSet(String setting) {
        return this.setting.getOrDefault(setting, false);
    }
    
    public static void reset() {
        settings.setting.clear();
        settings.hotKeys.clear();
    }
    
}
