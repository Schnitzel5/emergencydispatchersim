package me.duowoj.game.types;

public enum SimSpeed {

    VERY_SLOW(0.12f, 8f, "0.12x"), SLOWER(0.25f, 4f, "0.25x"), SLOW(0.5f, 2f, "0.5x"), NORMAL(1, 1, "1x"),
    FAST(1.25f, 0.5f, "1.25x"), FASTER(1.5f, 0.25f, "1.5x"), VERY_FAST(2f, 0.12f, "2x"), SUPER_FAST(4f, 0.06f, "4x");

    private float rate;
    private float delay;
    private String display;

    private SimSpeed(float rate, float delay, String display) {
        this.rate = rate;
        this.delay = delay;
        this.display = display;
    }

    public float getRate() {
        return rate;
    }

    public float getDelay() {
        return delay;
    }

    public String getDisplay() {
        return display;
    }

    public static SimSpeed getNextSpeed(SimSpeed ss) {
        switch (ss) {
        case VERY_SLOW:
            return SLOWER;
        case SLOWER:
            return SLOW;
        case SLOW:
            return NORMAL;
        case NORMAL:
            return FAST;
        case FAST:
            return FASTER;
        case FASTER:
            return VERY_FAST;
        case VERY_FAST:
            return SUPER_FAST;
        case SUPER_FAST:
            return VERY_SLOW;
        default:
            return NORMAL;
        }
    }

}
