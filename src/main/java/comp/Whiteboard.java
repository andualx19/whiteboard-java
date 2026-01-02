package comp;

import utils.Palette;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Whiteboard extends JPanel {
    private final ConcurrentLinkedQueue<PointData> queue = new ConcurrentLinkedQueue<>();
    private PointData lastPoint = null;
    private BufferedImage canvasImage;
    private double zoom = 1.0;

    public Whiteboard() {
        Timer renderTimer =  new Timer(16, e -> flushBatch());
        renderTimer.start();
    }

    public void addPoint(PointData p) {
        queue.add(p);
    }

    private void flushBatch() {
        if (queue.isEmpty() || canvasImage == null) return;

        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        PointData p;
        while ((p = queue.poll()) != null) {
            g2d.setColor(p.color);

            if (this.lastPoint != null) {
                g2d.setStroke(new BasicStroke(p.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(this.lastPoint.x, this.lastPoint.y, p.x, p.y);
            }

            int r = p.size / 2;
            g2d.fillOval(p.x - r, p.y - r, p.size, p.size);

            this.lastPoint = p;
        }
        g2d.dispose();
        repaint();
    }

    public void setZoom(double z) {
        this.zoom = z;

        int newW = (int) (794 * z);
        int newH = (int) (1123 * z);

        this.setPreferredSize(new Dimension(newW, newH));
        this.revalidate();
        this.repaint();
    }

    public void syncScroll(double pctX, double pctY) {
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

        if (scrollPane != null) {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            JScrollBar vBar = scrollPane.getVerticalScrollBar();

            int hVal = (int) (pctX * (hBar.getMaximum() - hBar.getVisibleAmount()));
            int vVal = (int) (pctY * (vBar.getMaximum() - vBar.getVisibleAmount()));

            hBar.setValue(hVal);
            vBar.setValue(vVal);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Palette.UI_BACKGROUND.get());
        g.fillRect(0, 0, getWidth(), getHeight());

        if(canvasImage == null) {
            canvasImage = new BufferedImage(794, 1123, BufferedImage.TYPE_INT_ARGB);
            clearBoard();
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = (int)(794 * zoom);
        int h = (int)(1123 * zoom);

        int x = (getWidth() > w) ? (getWidth() - w) / 2 : 0;
        int y = (getHeight() > h) ? (getHeight() - h) / 2 : 0;

        g2d.setColor(Palette.UI_SHADOW.get());
        g2d.fillRect(x + 4, y + 4, w, h);
        g2d.drawImage(canvasImage, x, y, w, h, null);

        g2d.setColor(Palette.UI_BORDER.get());
        g2d.drawRect(x, y, w, h);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int)(794 * zoom),
                (int)(1123 * zoom)
        );
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
