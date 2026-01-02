package comp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import utils.Command;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhiteboardServer extends WebSocketServer {
    private final Whiteboard board;
    private final ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public WhiteboardServer(int port, Whiteboard board) {
        super(new InetSocketAddress(port));
        this.board = board;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Client connected: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("Client disconnected: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        workerPool.submit(() -> {
            try {
                int x = message.getShort(0) & 0xFFFF; // Citim 2 bytes
                int y = message.getShort(2) & 0xFFFF; // Citim 2 bytes
                int r = message.get(4) & 0xFF;        // Citim 1 byte
                int g = message.get(5) & 0xFF;
                int b = message.get(6) & 0xFF;
                int size = message.get(7) & 0xFF;
                int type = message.get(8) & 0xFF;

                if (type == 1) {
                    int alpha = (size > 20) ? 120 : 225;
                    Color color = new Color(r, g, b, alpha);

                    board.addPoint(new PointData(x, y, color, size));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        workerPool.submit(() -> {
            try {
                Command cmd = Command.fromString(s);

                switch (cmd) {
                    case ZOOM:
                        double scale = Double.parseDouble(s.split(",")[1]);
                        SwingUtilities.invokeLater(() -> {
                            board.setZoom(scale);
                        });
                        break;
                    case SCROLL:
                        double percentX = Double.parseDouble(s.split(",")[1]);
                        double percentY = Double.parseDouble(s.split(",")[2]);
                        SwingUtilities.invokeLater(() -> {
                            board.syncScroll(percentX, percentY);
                        });
                        break;
                    case CLEAR:
                        board.clearBoard();
                        break;
                    case STOP:
                        board.resetLastPoint();
                        break;
                    default:
                        System.err.println("Unknown command: " + s);
                }
            } catch (Exception e) {
                System.err.println("ERROR: " + s);
            }
        });
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("PORT: " + getPort());
    }
}
