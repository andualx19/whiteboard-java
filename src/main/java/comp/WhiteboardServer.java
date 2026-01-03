package comp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import utils.Command;
import utils.PointData;
import utils.ProtocolException;
import utils.WhiteboardListener;

import java.awt.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WhiteboardServer extends WebSocketServer {
    static final Logger LOGGER = Logger.getLogger(WhiteboardServer.class.getName());

    private final WhiteboardListener listener;
    private final ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public WhiteboardServer(int port, WhiteboardListener listener) {
        super(new InetSocketAddress(port));
        this.listener = listener;
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        workerPool.submit(() -> {
            try {
                // binary parse
                int x = message.getShort(0) & 0xFFFF;
                int y = message.getShort(2) & 0xFFFF;
                int r = message.get(4) & 0xFF;
                int g = message.get(5) & 0xFF;
                int b = message.get(6) & 0xFF;
                int size = message.get(7) & 0xFF;
                int type = message.get(8) & 0xFF;

                if (type == Command.DRAW.getValue()) {
                    int alpha = (size > 20) ? 120 : 225;
                    Color color = new Color(r, g, b, alpha);

                    listener.onPointReceived(new PointData(x, y, color, size));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Binary data processing error", e);
            }
        });
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        workerPool.submit(() -> {
            try {
                String cmd = s.contains(",") ? s.split(",")[0] : s;

                Command type;
                try {
                    type = Command.valueOf(cmd);
                } catch (IllegalArgumentException e) {
                    throw new ProtocolException("Unknown command received: " + cmd);
                }

                listener.onCommandReceived(type, s);
            } catch (ProtocolException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Processing command error: " + s, e);
            }
        });
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        LOGGER.log(Level.SEVERE, "WebSocket error", e);
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
    public void onStart() {
        System.out.println("PORT: " + getPort());
    }
}
