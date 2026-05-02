package view;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

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

    private JPanel buildFleetPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Fleet"));
        panel.add(new JScrollPane(new JList<String>()), BorderLayout.CENTER);
        return panel;
    }

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

    private JPanel buildAnomalyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 180));
        panel.setBorder(BorderFactory.createTitledBorder("Anomaly Log"));

        String[] columns = {"Drone ID", "Type", "Details", "Timestamp"};
        panel.add(new JScrollPane(new JTable(new Object[0][4], columns)), BorderLayout.CENTER);

        return panel;
    }
}