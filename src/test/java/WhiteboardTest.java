import comp.Whiteboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.PointData;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class WhiteboardTest {
    private Whiteboard board;

    @BeforeEach
    void setUp() {
        board = new Whiteboard();
    }

    @Test
    void testPointAddition() {
        PointData p = new PointData(10, 20, Color.RED, 5);
        board.onPointReceived(p);

        assertDoesNotThrow(() -> board.onPointReceived(p));
    }

    @Test
    void testZoomLimits() {
        board.setZoom(5.0);
    }
}
