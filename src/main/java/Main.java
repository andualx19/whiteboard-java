import comp.HttpFileServer;
import comp.WhiteboardServer;
import comp.Whiteboard;
import utils.Palette;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Whiteboard");
        Whiteboard board = new Whiteboard();

        JScrollPane scrollPane = new JScrollPane(board);
        scrollPane.getViewport().setBackground(Palette.UI_BACKGROUND.get());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Palette.UI_BACKGROUND.get());
        centerPanel.add(board);
        scrollPane.setViewportView(centerPanel);

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
