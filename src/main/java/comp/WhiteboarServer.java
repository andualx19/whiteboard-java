package comp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.awt.*;
import java.net.InetSocketAddress;

public class WhiteboarServer extends WebSocketServer {
    private final Whiteboard board;

    public WhiteboarServer(int port, Whiteboard board) {
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
    public void onMessage(WebSocket webSocket, String s) {
        if (s.equalsIgnoreCase("CLEAR")) {
            board.clearBoard();
            return;
        }

        if (s.equalsIgnoreCase("STOP")) {
            board.resetLastPoint();
            return;
        }

        try {
            String[] parts = s.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int r = Integer.parseInt(parts[2]);
            int g = Integer.parseInt(parts[3]);
            int b = Integer.parseInt(parts[4]);
            int size = Integer.parseInt(parts[5]);

            Color color = new Color(r, g, b);

            board.addPoint(new PointData(x, y, color, size));
        } catch (Exception e) {
            System.err.println("ERROR: " + s);
        }
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
