package view;

import model.AnomalyRecord;
import model.Drone;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard is the main application window for the Drone Fleet management system.
 * Displays the fleet list, telemetry data, and anomaly records.
 */
public class Dashboard extends JFrame {

    private DefaultListModel<String> fleetListModel;
    private JList<String> fleetList;

    private JLabel idValue;
    private JLabel altitudeValue;
    private JLabel batteryValue;
    private JLabel velocityValue;
    private JLabel latitudeValue;
    private JLabel longitudeValue;

    private DefaultTableModel anomalyTableModel;

    /**
     * Constructs the Dashboard window.
     */
    public Dashboard() {

        setTitle("Drone Fleet Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout());

        add(buildFleetPanel(), BorderLayout.WEST);
        add(buildTelemetryPanel(), BorderLayout.CENTER);
        add(buildAnomalyPanel(), BorderLayout.SOUTH);
    }

    /**
     * Builds the menu bar.
     */
    private JMenuBar buildMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("New Simulation"));
        fileMenu.add(new JMenuItem("Open"));
        fileMenu.add(new JMenuItem("Save"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit"));

        JMenu viewMenu = new JMenu("View");
        viewMenu.add(new JMenuItem("Refresh"));
        viewMenu.add(new JMenuItem("Clear Anomalies"));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("About"));

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Builds the fleet panel.
     */
    private JPanel buildFleetPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Fleet"));

        fleetListModel = new DefaultListModel<>();
        fleetList = new JList<>(fleetListModel);

        panel.add(new JScrollPane(fleetList), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Builds the telemetry panel.
     */
    private JPanel buildTelemetryPanel() {

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.setBorder(BorderFactory.createTitledBorder("Telemetry"));

        idValue = new JLabel("—");
        altitudeValue = new JLabel("—");
        batteryValue = new JLabel("—");
        velocityValue = new JLabel("—");
        latitudeValue = new JLabel("—");
        longitudeValue = new JLabel("—");

        panel.add(new JLabel("ID:"));
        panel.add(idValue);

        panel.add(new JLabel("Altitude (m):"));
        panel.add(altitudeValue);

        panel.add(new JLabel("Battery (%):"));
        panel.add(batteryValue);

        panel.add(new JLabel("Velocity (m/s):"));
        panel.add(velocityValue);

        panel.add(new JLabel("Latitude:"));
        panel.add(latitudeValue);

        panel.add(new JLabel("Longitude:"));
        panel.add(longitudeValue);

        return panel;
    }

    /**
     * Builds the anomaly panel.
     */
    private JPanel buildAnomalyPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setPreferredSize(new Dimension(0, 180));
        panel.setBorder(BorderFactory.createTitledBorder("Anomaly Log"));

        String[] columns = {
                "Drone ID",
                "Type",
                "Details",
                "Timestamp"
        };

        anomalyTableModel = new DefaultTableModel(columns, 0);

        JTable anomalyTable = new JTable(anomalyTableModel);

        panel.add(new JScrollPane(anomalyTable), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the dashboard with live drone and anomaly data.
     */
    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {

        updateFleetList(drones);

        updateTelemetry(drones);

        updateAnomalyTable(anomalies);
    }

    /**
     * Updates fleet list.
     */
    private void updateFleetList(List<Drone> drones) {

        fleetListModel.clear();

        for (Drone drone : drones) {
            fleetListModel.addElement(drone.getId());
        }
    }

    /**
     * Updates telemetry labels using the first drone in the list.
     */
    private void updateTelemetry(List<Drone> drones) {

        if (drones.isEmpty()) {
            return;
        }

        Drone drone = drones.get(0);

        idValue.setText(drone.getId());

        altitudeValue.setText(
                String.format("%.2f", drone.getAltitude())
        );

        batteryValue.setText(
                String.format("%.2f", drone.getBattery())
        );

        velocityValue.setText(
                String.format("%.2f", drone.getVelocity())
        );

        latitudeValue.setText(
                String.format("%.4f", drone.getLatitude())
        );

        longitudeValue.setText(
                String.format("%.4f", drone.getLongitude())
        );
    }

    /**
     * Adds anomaly rows to the anomaly table.
     */
    private void updateAnomalyTable(List<AnomalyRecord> anomalies) {

        for (AnomalyRecord anomaly : anomalies) {

            anomalyTableModel.addRow(new Object[] {
                    anomaly.getDroneId(),
                    anomaly.getType(),
                    anomaly.getDetails(),
                    anomaly.getTimestamp()
            });
        }
    }
}