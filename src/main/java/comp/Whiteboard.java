package comp;

import utils.Command;
import utils.Palette;
import utils.PointData;
import utils.WhiteboardListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import static comp.WhiteboardServer.LOGGER;

public class Whiteboard extends JPanel implements WhiteboardListener {
    private final ConcurrentLinkedQueue<PointData> queue = new ConcurrentLinkedQueue<>();
    private PointData lastPoint = null;
    private BufferedImage canvasImage;
    private double zoom = 1.0;

    // Dimension constants (A4)
    private static final int BASE_WIDTH = 794;
    private static final int BASE_HEIGHT = 1123;

    public Whiteboard() {
        // Render at ~60 FPS
        this.setOpaque(true);
        Timer renderTimer =  new Timer(16, ignored -> flushBatch());
        renderTimer.start();
    }

    // Interface implementation

    @Override
    public void onPointReceived(PointData point) {
        queue.add(point);
    }

    @Override
    public void onCommandReceived(Command cmd, Object data) {
        SwingUtilities.invokeLater(() -> {
            switch (cmd) {
                case STOP -> resetLastPoint();
                case CLEAR -> clearBoard();
                case ZOOM -> {
                    if (data instanceof String s) {
                        try {
                            setZoom(Double.parseDouble(s.split(",")[1]));
                        } catch (Exception ignored) {}
                    }
                }
                case SCROLL -> {
                    if (data instanceof String s) {
                        try {
                            String[] p = s.split(",");
                            syncScroll(Double.parseDouble(p[1]), Double.parseDouble(p[2]));
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    // Drawing logic

    private void flushBatch() {
        if (queue.isEmpty() || canvasImage == null) return;

        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        PointData p;
        while ((p = queue.poll()) != null) {
            g2d.setColor(p.color);

            if (this.lastPoint != null) {
                g2d.setStroke(new BasicStroke(p.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(this.lastPoint.x, this.lastPoint.y, p.x, p.y);
            } else {
                int r = p.size / 2;
                g2d.fillOval(p.x - r, p.y - r, p.size, p.size);
            }
            this.lastPoint = p;
        }
        g2d.dispose();
        repaint();
    }

    // Support methods

    public void setZoom(double z) {
        this.zoom = Math.max(0.4, Math.min(z, 3.0));
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
        super.paintComponent(g);

        g.setColor(Palette.UI_BACKGROUND.get());
        g.fillRect(0, 0, getWidth(), getHeight());

        if(canvasImage == null) {
            canvasImage = new BufferedImage(BASE_WIDTH, BASE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            clearBoard();
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = (int) (BASE_WIDTH * zoom);
        int h = (int) (BASE_HEIGHT * zoom);

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
                (int) (BASE_WIDTH * zoom),
                (int) (BASE_HEIGHT * zoom)
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

    @Override
    public void onError(Exception e) {
        LOGGER.log(Level.WARNING, "Latency or communication error: " + e.getMessage());
    }
}
