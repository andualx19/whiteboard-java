package utils;

import java.awt.*;

public enum Palette {
    BLACK(0, 0, 0),
    RED(255, 68, 68),
    GREEN(76, 175, 80),
    BLUE(33, 150, 243),
    WHITE(255, 255, 255),

    UI_BACKGROUND(45, 48, 50),
    UI_SHADOW(0, 0, 0, 100),
    UI_BORDER(100, 100, 100);

    private final Color color;

    Palette(int r, int g, int b) {
        this.color = new Color(r, g, b);
    }

    Palette(int r, int g, int b, int a) {
        this.color = new Color(r, g, b, a);
    }

    public Color get() {
        return this.color;
    }

    public static Color parse(int r, int g, int b) {
        for (Palette p : values()) {
            Color c = p.get();
            if (c.getRed() == r && c.getGreen() == g && c.getBlue() == b) {
                return c;
            }
        }
        return new Color(r, g, b);
    }
}
