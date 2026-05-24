package view;

import model.Drone;
import model.DroneStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/**
 * Renders a live grid-based map of all drone positions.
 *
 * GPS coordinates are mapped into a normalised 0–1 space
 * based on the bounding box of all drone positions, then
 * drawn onto a fixed canvas with padding.
 *
 * Each drone is drawn as an arrow (showing orientation) with
 * a status-coloured indicator and ID label.
 */
public class DroneMapPanel extends JPanel {

    private static final Color BG          = new Color(18, 22, 32);
    private static final Color GRID_COL    = new Color(35, 42, 58);
    private static final Color COL_NORMAL  = new Color(34,  197, 94);
    private static final Color COL_WARN    = new Color(251, 191, 36);
    private static final Color COL_CRIT    = new Color(239, 68,  68);
    private static final Color TEXT_COLOR  = new Color(200, 210, 230);
    private static final int   GRID_LINES  = 10;
    private static final int   DRONE_RADIUS = 8;
    private static final int   PADDING     = 30;

    private List<Drone> drones = new ArrayList<>();

    public DroneMapPanel() {
        setBackground(BG);
        setMinimumSize(new Dimension(400, 200));
    }

    /** Called by Dashboard.display() on every telemetry cycle. */
    public void updateDrones(List<Drone> updated) {
        this.drones = new ArrayList<>(updated);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);

        if (drones.isEmpty()) {
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("Waiting for telemetry…", 20, 30);
            return;
        }

        // Compute bounding box with a small buffer so drones on the edge don't clip
        double minLat = drones.stream().mapToDouble(Drone::getLatitude).min().orElse(0)  - 0.002;
        double maxLat = drones.stream().mapToDouble(Drone::getLatitude).max().orElse(1)  + 0.002;
        double minLon = drones.stream().mapToDouble(Drone::getLongitude).min().orElse(0) - 0.002;
        double maxLon = drones.stream().mapToDouble(Drone::getLongitude).max().orElse(1) + 0.002;

        int w = getWidth()  - PADDING * 2;
        int h = getHeight() - PADDING * 2;

        for (Drone d : drones) {
            double normX = (d.getLongitude() - minLon) / (maxLon - minLon);
            double normY = 1.0 - (d.getLatitude()  - minLat) / (maxLat - minLat); // flip Y

            int px = PADDING + (int)(normX * w);
            int py = PADDING + (int)(normY * h);

            Color droneColor = switch (d.getStatus()) {
                case CRITICAL -> COL_CRIT;
                case WARNING  -> COL_WARN;
                default       -> COL_NORMAL;
            };

            drawDrone(g2, px, py, d.getOrientation(), droneColor, d.getId());
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COL);
        g2.setStroke(new BasicStroke(0.5f));
        int w = getWidth();
        int h = getHeight();

        for (int i = 1; i < GRID_LINES; i++) {
            int x = i * w / GRID_LINES;
            int y = i * h / GRID_LINES;
            g2.drawLine(x, 0, x, h);
            g2.drawLine(0, y, w, y);
        }
    }

    /**
     * Draws a drone as an arrow pointing in its heading direction,
     * with a glow ring and ID label.
     */
    private void drawDrone(Graphics2D g2, int px, int py,
                           double orientationDeg, Color color, String id) {
        // Glow ring
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g2.fillOval(px - DRONE_RADIUS * 2, py - DRONE_RADIUS * 2,
                    DRONE_RADIUS * 4, DRONE_RADIUS * 4);

        // Arrow body — rotate to match heading (0 = North = up)
        AffineTransform old = g2.getTransform();
        g2.translate(px, py);
        g2.rotate(Math.toRadians(orientationDeg));

        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));

        // Arrow shape: line + arrowhead
        int len = DRONE_RADIUS + 4;
        g2.drawLine(0, len, 0, -len);          // body
        g2.drawLine(0, -len, -5, -len + 8);    // left wing
        g2.drawLine(0, -len,  5, -len + 8);    // right wing

        g2.setTransform(old);

        // Solid dot at centre
        g2.setColor(color);
        g2.fillOval(px - DRONE_RADIUS / 2, py - DRONE_RADIUS / 2,
                    DRONE_RADIUS, DRONE_RADIUS);

        // ID label
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString(id, px + DRONE_RADIUS + 2, py - DRONE_RADIUS);
    }
}
