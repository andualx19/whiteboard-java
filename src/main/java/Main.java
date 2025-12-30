import comp.HttpFileServer;
import comp.WhiteboarServer;
import comp.Whiteboard;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Whiteboard");
        Whiteboard board = new Whiteboard();

        board.setPreferredSize(new java.awt.Dimension(794, 1123));

        JScrollPane scrollPane = new JScrollPane(board);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());

        frame.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> board.clearBoard());
        frame.add(clearButton, BorderLayout.SOUTH);

        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        WhiteboarServer server = new WhiteboarServer(5000, board);
        server.start();

        HttpFileServer.start(8080);
        System.out.println("LINK: http://" + InetAddress.getLocalHost().getHostAddress() + ":8080");
    }
}
