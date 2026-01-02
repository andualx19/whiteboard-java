package comp;

import java.awt.*;

public class PointData {
    public final int x, y, size;
    public final Color color;

    public PointData(int x, int y, Color color, int size) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = size;
    }
}
