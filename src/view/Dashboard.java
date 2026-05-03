package view;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard is the main application window for the Drone Fleet management system.
 * It extends JFrame and organises the UI into three panels:
 *   - Fleet panel (left):     lists all drones in the fleet
 *   - Telemetry panel (centre): displays live telemetry data for a selected drone
 *   - Anomaly panel (bottom): shows a log of detected anomalies
 */
public class Dashboard extends JFrame {
    /**
     * Constructs the Dashboard window, configuring its size, layout,
     * menu bar, and the three main content panels.
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
     * Builds the application menu bar with File, View, and Help menus.
     * - File: New Simulation, Open, Save, Exit
     * - View: Refresh, Clear Anomalies
     * - Help: About
     *
     * @return the fully constructed JMenuBar
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
     * Builds the Fleet panel displayed on the left side of the dashboard.
     * Contains a scrollable JList intended to display the names or IDs
     * of all drones currently in the fleet.
     *
     * @return a JPanel with a preferred width of 200px containing the fleet list
     */
    private JPanel buildFleetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Fleet"));
        panel.add(new JScrollPane(new JList<String>()), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the Telemetry panel displayed in the centre of the dashboard.
     * Shows key real-time metrics for the currently selected drone:
     * ID, Altitude, Battery, Velocity, Latitude, and Longitude.
     * Values are initially set to "—" and should be updated when a drone is selected.
     *
     * @return a JPanel with a 6x2 grid of label/value pairs
     */
    private JPanel buildTelemetryPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Telemetry"));

        panel.add(new JLabel("ID:"));           panel.add(new JLabel("—"));
        panel.add(new JLabel("Altitude (m):")); panel.add(new JLabel("—"));
        panel.add(new JLabel("Battery (%):")); panel.add(new JLabel("—"));
        panel.add(new JLabel("Velocity (m/s):")); panel.add(new JLabel("—"));
        panel.add(new JLabel("Latitude:"));    panel.add(new JLabel("—"));
        panel.add(new JLabel("Longitude:"));   panel.add(new JLabel("—"));

        return panel;
    }

    /**
     * Builds the Anomaly Log panel displayed at the bottom of the dashboard.
     * Contains a scrollable JTable with columns: Drone ID, Type, Details, Timestamp.
     * Rows are added dynamically as anomalies are detected during operation.
     *
     * @return a JPanel with a preferred height of 180px containing the anomaly table
     */
    private JPanel buildAnomalyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 180));
        panel.setBorder(BorderFactory.createTitledBorder("Anomaly Log"));

        String[] columns = {"Drone ID", "Type", "Details", "Timestamp"};
        panel.add(new JScrollPane(new JTable(new Object[0][4], columns)), BorderLayout.CENTER);

        return panel;
    }
}