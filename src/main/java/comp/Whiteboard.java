package comp;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Whiteboard extends JPanel {
    private BufferedImage canvasImage;
    private PointData lastPoint = null;
    private double zoom = 1.0;

    public void setZoom(double z) {
        this.zoom = z;

        int newW = (int) (794 * z);
        int newH = (int) (1123 * z);

        this.setPreferredSize(new Dimension(newW, newH));
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(canvasImage == null) {
            canvasImage = new BufferedImage(794, 1123, BufferedImage.TYPE_INT_ARGB);
            clearBoard();
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = (int)(794 * zoom);
        int h = (int)(1123 * zoom);

        int x = Math.max(0, (getWidth() - w) / 2);
        int y = Math.max(0, (getHeight() - h) / 2);

        g2d.drawImage(canvasImage, x, y, w, h, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int)(794 * zoom),
                (int)(1123 * zoom)
        );
    }

    public void addPoint(PointData p) {
        SwingUtilities.invokeLater(() -> {
            if (canvasImage == null) return;

            Graphics2D g2d = canvasImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(p.color);

            int drawSize = (int)(p.size);
            g2d.fillOval(p.x - (drawSize / 2), p.y - (drawSize / 2), drawSize, drawSize);

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
