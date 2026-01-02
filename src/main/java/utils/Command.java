package utils;

public enum Command {
    DRAW, ZOOM, SCROLL, CLEAR, STOP, UNKNOWN;

    public static Command fromString(String s) {
        if (s.startsWith("ZOOM,")) return ZOOM;
        if (s.startsWith("SCROLL,")) return SCROLL;
        if (s.equalsIgnoreCase("CLEAR")) return CLEAR;
        if (s.equalsIgnoreCase("STOP")) return STOP;
        if (s.contains(",")) return DRAW;
        return UNKNOWN;
    }
}
