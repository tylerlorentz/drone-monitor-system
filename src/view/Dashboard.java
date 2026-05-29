package view;

import controller.AnomalyDatabase;
import controller.CSVExporter;
import model.AnomalyRecord;
import model.Drone;
import model.DroneStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends JFrame {

    // -------------------------
    // COLORS
    // -------------------------
    private static final Color BG_DARK  = new Color(15, 17, 23);
    private static final Color BG_PANEL = new Color(22, 26, 35);
    private static final Color BG_CARD  = new Color(30, 35, 48);
    private static final Color ACCENT   = new Color(56, 189, 248);
    private static final Color TEXT_MUT = new Color(100, 116, 139);
    private static final Color TEXT_PRI = new Color(226, 232, 240);
    private static final Color BORDER   = new Color(40, 48, 65);
    private static final Color COL_WARN = new Color(251, 191, 36);
    private static final Color COL_CRIT = new Color(239, 68, 68);

    // -------------------------
    // UI STATE
    // -------------------------
    private DefaultListModel<String> fleetListModel;
    private JList<String> fleetList;

    private JLabel[] telemetryValues;

    private DefaultTableModel anomalyTableModel;
    private DroneMapPanel mapPanel;

    private JLabel totalAnomaliesValue;
    private JLabel criticalValue;
    private JLabel warningValue;
    private JLabel avgBatteryValue;

    private JComboBox<String> severityFilter;

    private controller.TelemetryGenerator generator;

    // -------------------------
    // DATA STATE
    // -------------------------
    private List<Drone> currentDrones = new ArrayList<>();
    private final List<AnomalyRecord> anomalyLog = new ArrayList<>();
    private AnomalyDatabase db;

    // -------------------------
    // CONSTRUCTOR
    // -------------------------
    public Dashboard() {
        setTitle("Drone Fleet Security Monitor");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setOpaque(false);

        centre.add(buildTelemetryPanel(), BorderLayout.NORTH);
        centre.add(buildStatisticsPanel(), BorderLayout.CENTER);
        centre.add(buildMapPanel(), BorderLayout.SOUTH);

        add(buildFleetPanel(), BorderLayout.WEST);
        add(centre, BorderLayout.CENTER);
        add(buildAnomalyPanel(), BorderLayout.SOUTH);
    }

    // -------------------------
    // PUBLIC API
    // -------------------------
    public void setDatabase(AnomalyDatabase db) {
        this.db = db;
    }

    public void setTelemetryGenerator(controller.TelemetryGenerator generator) {
        this.generator = generator;
    }

    // -------------------------
    // DISPLAY UPDATE
    // -------------------------
    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {

        currentDrones = new ArrayList<>(drones);

        // fleet list
        fleetListModel.clear();
        for (Drone d : drones) {
            fleetListModel.addElement(d.getId());
        }

        if (!drones.isEmpty() && fleetList.getSelectedIndex() == -1) {
            fleetList.setSelectedIndex(0);
        }

        refreshTelemetry();

        for (AnomalyRecord a : anomalies) {
            anomalyLog.add(a);
            anomalyTableModel.insertRow(0, new Object[]{
                a.getDroneId(),
                a.getType(),
                a.getSeverity(),
                a.getDetails(),
                a.getFormattedTimestamp()
            });
        }

        long criticalCount = anomalyLog.stream()
                .filter(a -> a.getSeverity().equals("CRITICAL"))
                .count();

        long warningCount = anomalyLog.stream()
                .filter(a -> a.getSeverity().equals("WARNING"))
                .count();

        double avgBattery = drones.stream()
                .mapToDouble(Drone::getBattery)
                .average()
                .orElse(0);

        totalAnomaliesValue.setText(String.valueOf(anomalyLog.size()));
        criticalValue.setText(String.valueOf(criticalCount));
        warningValue.setText(String.valueOf(warningCount));
        avgBatteryValue.setText(String.format("%.1f%%", avgBattery));

        mapPanel.updateDrones(drones);
    }

    // -------------------------
    // STATISTICS PANEL
    // -------------------------
    private JPanel buildStatisticsPanel() {

        JPanel panel = darkCard();
        panel.setLayout(new GridLayout(1, 4, 10, 0));

        totalAnomaliesValue = createStatCard(panel, "TOTAL ANOMALIES", "0");
        criticalValue       = createStatCard(panel, "CRITICAL", "0");
        warningValue        = createStatCard(panel, "WARNINGS", "0");
        avgBatteryValue     = createStatCard(panel, "AVG BATTERY", "0%");

        return panel;
    }

    private JLabel createStatCard(JPanel panel, String title, String value) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(ACCENT);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 18));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        panel.add(card);

        return valueLabel;
    }

    // -------------------------
    // SEVERITY FILTER
    // -------------------------
    private void applySeverityFilter() {

        String selected = (String) severityFilter.getSelectedItem();

        anomalyTableModel.setRowCount(0);

        for (AnomalyRecord a : anomalyLog) {
            if ("ALL".equals(selected) || selected.equals(a.getSeverity())) {
                anomalyTableModel.addRow(new Object[]{
                        a.getDroneId(),
                        a.getType(),
                        a.getSeverity(),
                        a.getDetails(),
                        a.getFormattedTimestamp()
                });
            }
        }
    }

    // -------------------------
    // TELEMETRY PANEL
    // -------------------------
    private JPanel buildTelemetryPanel() {

        JPanel wrapper = darkCard();
        wrapper.setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(1, 6, 10, 0));
        grid.setOpaque(false);

        String[] labels = {
                "DRONE ID", "ALT", "BATTERY",
                "VEL", "LAT", "LON"
        };

        telemetryValues = new JLabel[6];

        for (int i = 0; i < 6; i++) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(BG_CARD);

            JLabel title = new JLabel(labels[i]);
            title.setForeground(TEXT_MUT);

            JLabel value = new JLabel("—");
            value.setForeground(ACCENT);

            telemetryValues[i] = value;

            card.add(title, BorderLayout.NORTH);
            card.add(value, BorderLayout.CENTER);
            grid.add(card);
        }

        wrapper.add(grid, BorderLayout.CENTER);

        return wrapper;
    }

    // -------------------------
    // MAP
    // -------------------------
    private JPanel buildMapPanel() {
        JPanel panel = darkCard();
        mapPanel = new DroneMapPanel();
        panel.add(mapPanel);
        return panel;
    }

    // -------------------------
    // ANOMALY PANEL
    // -------------------------
    private JPanel buildAnomalyPanel() {

        JPanel panel = darkCard();
        panel.setLayout(new BorderLayout());

        String[] cols = {"ID", "TYPE", "SEVERITY", "DETAILS", "TIME"};

        anomalyTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(anomalyTableModel);

        severityFilter = new JComboBox<>(new String[]{
                "ALL", "INFO", "WARNING", "CRITICAL"
        });

        severityFilter.addActionListener(e -> applySeverityFilter());

        panel.add(severityFilter, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // -------------------------
    // TELEMETRY UPDATE
    // -------------------------
    private void refreshTelemetry() {

        int idx = fleetList.getSelectedIndex();
        if (idx < 0 || idx >= currentDrones.size()) return;

        Drone d = currentDrones.get(idx);

        telemetryValues[0].setText(d.getId());
        telemetryValues[1].setText(String.valueOf(d.getAltitude()));
        telemetryValues[2].setText(String.valueOf(d.getBattery()));
        telemetryValues[3].setText(String.valueOf(d.getVelocity()));
        telemetryValues[4].setText(String.valueOf(d.getLatitude()));
        telemetryValues[5].setText(String.valueOf(d.getLongitude()));
    }

    // -------------------------
    // MENU ACTIONS
    // -------------------------
    private void openQueryDialog() {

        if (db == null) {
            JOptionPane.showMessageDialog(this,
                    "Database not connected.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new QueryDialog(this, db).setVisible(true);
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Exit?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (db != null) db.close();
            System.exit(0);
        }
    }

    // -------------------------
    // HELPERS
    // -------------------------
    private JPanel darkCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        return p;
    }
}
