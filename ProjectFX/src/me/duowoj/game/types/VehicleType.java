package me.duowoj.game.types;

public enum VehicleType {

    RTW("Rettungswagen"),
    NEF("Notarzt"),
    KAT("Katastrophenzug"),
    CHRIS("Christophorus");
    
    private String des;
    
    private VehicleType(String des) {
        this.des = des;
    }
    
    public String getDes() {
        return des;
    }
    
}
