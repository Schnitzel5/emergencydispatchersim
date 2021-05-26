package me.duowoj.game.intern;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import me.duowoj.game.Game;
import me.duowoj.game.Settings;
import me.duowoj.game.controller.CallCenterController;
import me.duowoj.game.model.Case;

public class CaseGenerator {

    private final static Queue<Case> caseQueue = new ConcurrentLinkedQueue<>();
    private Thread thread;

    public CaseGenerator() {
        if (Settings.settings.isSet("debug")) {
            Case debug = new Case("greeting", "call", "patient", "place", "event", "speak", "breath", "hurt", "age",
                    "pre", "reanimation", "help");
            debug.setMessage("phone", "11111111111");
            caseQueue.add(debug);
        }
        thread = new Thread(() -> runGenerator());
        thread.start();
    }

    public void runGenerator() {
        Random random = ThreadLocalRandom.current();
        while (Game.isRunning()) {
            try {
                AtomicInteger size = new AtomicInteger(-1);
                Platform.runLater(() -> {
                    size.set(CallCenterController.getCaseList().size());
                });
                while (size.get() == -1)
                    ;
                if (random.nextInt(100) < 50 - 3 * size.get() && size.get() < 15 && !CallCenterController.dnd.get()) {
                    Case randomCase = new Case(Loader.cases.get(random.nextInt(Loader.cases.size())));
                    randomCase.setMessage("phone", prepareNumber());
                    caseQueue.add(randomCase);
                }
                if (peekNextCase() != null) {
                    Platform.runLater(() -> {
                        CallCenterController.getCaseList().add(pollNextCase());
                    });
                }
                if (Settings.settings.isSet("smartdelay")) {
                    Thread.sleep((long) ((3000 + 1200 * caseQueue.size()) * Game.speed.getDelay()));
                } else {
                    Thread.sleep((long) (3000 * Game.speed.getDelay()));
                }
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    private String prepareNumber() {
        String nums = "0123456789";
        String pre = "56789";
        StringBuilder builder;
        Random random = new Random();
        builder = new StringBuilder("+43 6");
        builder.append(pre.charAt(random.nextInt(pre.length())));
        for (int j = 0; j < 5; j++) {
            builder.append(nums.charAt(random.nextInt(nums.length())));
        }
        return builder.toString();
    }

    /*
     * OLD GENERATOR private void runGenerator() { Random random = new Random();
     * while (Game.isRunning()) { try { if (random.nextInt(100) < 15 &&
     * caseQueue.size() < 15) { List<String> testDialogues = new ArrayList<>();
     * IntStream.range(0, 12).forEach(i -> { testDialogues.add("Test" +
     * test.charAt(random.nextInt(test.length()))); }); Case caseTest = new
     * Case(testDialogues.toArray(new String[0])); caseTest.setName("John Test");
     * caseQueue.add(caseTest); // System.out.println(caseTest.getMessage("age"));
     * Only for debug purposes } if (peekNextCase() != null) { Platform.runLater(()
     * -> { CallCenterController.getCaseList().add(pollNextCase()); }); } //
     * System.out.println(Game.getStages().size()); Thread.sleep(2000 + 1000 *
     * caseQueue.size()); } catch (InterruptedException exception) {
     * exception.printStackTrace(); } } }
     */

    public synchronized static Case peekNextCase() {
        return caseQueue.peek();
    }

    public synchronized static Case pollNextCase() {
        return caseQueue.poll();
    }

    public static void clearCases() {
        caseQueue.clear();
    }

}
