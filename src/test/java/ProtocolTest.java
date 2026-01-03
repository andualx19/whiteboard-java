import comp.Whiteboard;
import org.junit.jupiter.api.Test;
import utils.Command;
import utils.PointData;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {
    @Test
    void testCommandParsing() {
        String msg = "ZOOM,1.5";
        String cmdName = msg.split(",")[0];

        Command cmd = Command.valueOf(cmdName);
        assertEquals(Command.ZOOM, cmd);
    }

    @Test
    void testInvalidCommandThrowsException() {
        String invalidMessage = "UNKNOWN";
        assertThrows(IllegalAccessError.class, () -> {
            Command.valueOf(invalidMessage);
        });
    }

    @Test
    void testMultiThreadedPointersInsertion() throws InterruptedException {
        Whiteboard board = new Whiteboard();
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    board.onPointReceived(new PointData(j, j, Color.BLACK, 2));
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) t.join();
    }
}
