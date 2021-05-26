package me.duowoj.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import me.duowoj.game.intern.Loader;
import me.duowoj.game.types.CallOuts;

public class Case {

    public static Map<Integer, Case> cases = new HashMap<>();

    private static int counter;
    private int caseId;
    private String editor;
    private String name;
    private String phone;
    private String street;
    private int houseNr;
    private String patient;
    private String description;
    private boolean reanimation;
    private CallOuts caseTag;
    private String excessInfo;
    private final HashMap<String, String> dialogue;
    private boolean accepted;
    private boolean archived;
    private String[] dialogueVal;

    public Case(String... dialogueVal) {
        this(++counter, dialogueVal);
    }
    
    public Case(int caseId, String... dialogueVal) {
        dialogue = new HashMap<>();
        if (dialogueVal.length < 9) {
            System.err.println("Error: Create case object: missing dialogues");
            return;
        }
        this.caseId = caseId;
        this.dialogueVal = dialogueVal;
        dialogue.put("greeting", dialogueVal[2]);
        dialogue.put("call", dialogueVal[0]);
        dialogue.put("patient", dialogueVal[1]);
        dialogue.put("place", Loader.streets.get(ThreadLocalRandom.current().nextInt(Loader.streets.size())) + " "
                + (ThreadLocalRandom.current().nextInt(70) + 1));
        dialogue.put("event", dialogueVal[2]);
        dialogue.put("speak", dialogueVal[3]);
        dialogue.put("breath", dialogueVal[4]);
        dialogue.put("hurt", dialogueVal[5]);
        dialogue.put("age", dialogueVal[7]);
        dialogue.put("pre", dialogueVal[6]);
        dialogue.put("help", dialogueVal[8]);
        cases.put(caseId, this);
        Vehicle.vehicles.putIfAbsent(caseId, new ArrayList<>());
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getHouseNr() {
        return houseNr;
    }

    public void setHouseNr(int houseNr) {
        this.houseNr = houseNr;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CallOuts getCaseTag() {
        return caseTag;
    }

    public void setCaseTag(CallOuts caseTag) {
        this.caseTag = caseTag;
    }

    public boolean needReanimation() {
        return reanimation;
    }

    public void setReanimation(boolean reanimation) {
        this.reanimation = reanimation;
    }

    public String getExcessInfo() {
        return excessInfo;
    }

    public void setExcessInfo(String excessInfo) {
        this.excessInfo = excessInfo;
    }

    public String getMessage(String tag) {
        return dialogue.get(tag);
    }

    public void setMessage(String tag, String message) {
        dialogue.put(tag, message);
    }
    
    public String[] getDialogues() {
        return dialogueVal;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean completed() {
        if (!Vehicle.vehicles.containsKey(caseId)) {
            return false;
        }
        List<Vehicle> vehicles = Vehicle.vehicles.get(caseId);
        long arrived = vehicles.stream().filter(v -> v.isArrived()).count();
        boolean oneProper = vehicles.stream()
                .anyMatch(v -> v.isArrived() && v.getDestination().equals(getMessage("place").split(" ")[0]));
        return vehicles.size() > 0 && arrived == vehicles.size() && oneProper;
    }

    public double progress() {
        if (!Vehicle.vehicles.containsKey(caseId)) {
            return 0.0;
        }
        List<Vehicle> vehicles = Vehicle.vehicles.get(caseId);
        if (vehicles.size() == 0) {
            return 0.0;
        }
        double arrived = vehicles.stream().map(v -> v.progress()).reduce((d1, d2) -> d1 + d2).get();
        return arrived / (vehicles.size() * 100) * 100;
    }

    @Override
    public String toString() {
        return dialogue.get("phone");
    }
    
    public static int getCounter() {
        return counter;
    }
    
    public static void setCounter(int count) {
        counter = count;
    }
    
    public static void resetCounter() {
        counter = 0;
    }

}
