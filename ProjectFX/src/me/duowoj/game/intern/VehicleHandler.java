package me.duowoj.game.intern;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import me.duowoj.game.Game;
import me.duowoj.game.controller.CallCenterController;
import me.duowoj.game.controller.CallCenterFormsController;
import me.duowoj.game.controller.CallCenterOverviewController;
import me.duowoj.game.model.Case;
import me.duowoj.game.model.Vehicle;

public class VehicleHandler {

    private ScheduledFuture<?> future;
    private long last;

    public VehicleHandler() {
    }

    public void start() {
        if (future != null) {
            return;
        }
        last = System.currentTimeMillis();
        future = CallCenterController.service.scheduleAtFixedRate(() -> runHandler(), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void runHandler() {
        if (!Game.isRunning()) {
            cancel();
            return;
        }
        Iterator<Map.Entry<Integer, List<Vehicle>>> it = Vehicle.vehicles.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, List<Vehicle>> en = it.next();
            if (!Case.cases.containsKey(en.getKey())) {
                continue;
            }
            Case c = Case.cases.get(en.getKey());
            CallCenterFormsController controller = (CallCenterFormsController) Game.getControllers()
                    .get("Leitstelle: " + en.getKey());
            if (c.isArchived() || en.getValue().stream().filter(v -> v.isDispatched()).count() == 0) {
                continue;
            }
            for (Vehicle vehicle : en.getValue()) {
                if (vehicle.isDispatched() && !vehicle.isArrived()) {
                    vehicle.move();
                    if (vehicle.isArrived()) {
                        if (c.completed()) {
                            c.setArchived(true);
                            Case call = CallCenterController.callCenter.getCurrentCase();
                            if (call != null && call.getCaseId() == c.getCaseId()) {
                                CallCenterController.callCenter.disconnect();
                            }
                            refresh();
                            break;
                        }
                    }
                }
            }
            if (System.currentTimeMillis() - last > 2000) {
                refresh();
                last = System.currentTimeMillis();
            }
            if (controller != null) {
                Platform.runLater(() -> {
                    controller.update();
                });
            }
        }
    }
    
    public void cancel() {
        if (future != null) {
            future.cancel(false);
        }
    }
    
    private void refresh() {
        Platform.runLater(() -> {
            if (CallCenterOverviewController.overview != null) {
                CallCenterOverviewController.overview.refresh();
            }
        });
    }

}
