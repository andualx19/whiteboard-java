import comp.HttpFileServer;
import comp.WhiteboardServer;
import comp.Whiteboard;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Whiteboard");
        Whiteboard board = new Whiteboard();

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(82, 86, 89));
        centerPanel.add(board);

        JScrollPane scrollPane = new JScrollPane(board);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(82, 86, 89));

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(frame);
        } else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        }

        WhiteboardServer server = new WhiteboardServer(5000, board);
        server.start();

        HttpFileServer.start(8080);
        System.out.println("LINK: http://" + InetAddress.getLocalHost().getHostAddress() + ":8080");
    }
}
