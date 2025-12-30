package comp;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Whiteboard extends JPanel {
    private BufferedImage canvasImage;
    private PointData lastPoint = null;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(canvasImage == null) {
            canvasImage = new BufferedImage(794, 1123, BufferedImage.TYPE_INT_ARGB);
            clearBoard();
        }
        g.drawImage(canvasImage, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(794, 1123);
    }

    public void addPoint(PointData p) {
        SwingUtilities.invokeLater(() -> {
            if (canvasImage == null) return;

            Graphics2D g2d = canvasImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(p.color);

            g2d.fillOval(p.x - (p.size / 2), p.y - (p.size / 2), p.size, p.size);

            if (lastPoint != null) {
                g2d.setStroke(new BasicStroke(p.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
            }

            lastPoint = p;
            g2d.dispose();
            repaint();
        });
    }

    public void resetLastPoint() {
        this.lastPoint = null;
    }

    public void clearBoard() {
        if (canvasImage != null) {
            Graphics2D g2d = canvasImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
            g2d.dispose();
        }
        lastPoint = null;
        repaint();
    }
}
