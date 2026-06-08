package view;

import controller.AnomalyDatabase;
import controller.CSVExporter;
import controller.DroneMonitorApp;
import model.AnomalyRecord;
import model.Drone;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private static final Color COL_INFO = new Color(56, 189, 248);

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

    // Settings menu items — held as fields so their text can be toggled
    private JMenuItem pauseItem;
    private JMenuItem muteItem;

    // -------------------------
    // DATA STATE
    // -------------------------
    private List<Drone> currentDrones = new ArrayList<>();
    private final List<AnomalyRecord> anomalyLog = new ArrayList<>();
    private AnomalyDatabase db;

    /** Reference to the app, injected so Settings menu can call togglePause/toggleMute. */
    private DroneMonitorApp app;

    /**
     * Constructs and initializes the graphical dashboard.
     */
    public Dashboard() {
        setTitle("Drone Fleet Security Monitor");
        setSize(1100, 760);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            /**
             * Handles window-closing events and performs any required
             * shutdown operations.
             *
             * @param e the window event
             */
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

    /**
     * Assigns the database used by the dashboard.
     *
     * @param db the anomaly database
     */
    public void setDatabase(AnomalyDatabase db) {
        this.db = db;
    }

    /**
     * Assigns the telemetry generator used by the dashboard.
     *
     * @param generator the telemetry generator
     */
    public void setTelemetryGenerator(controller.TelemetryGenerator generator) {
        this.generator = generator;
    }

    /**
     * Injected by DroneMonitorApp so the Settings menu can toggle pause/mute.
     * */
    public void setApp(DroneMonitorApp app) {
        this.app = app;
    }

    /**
     * Refreshes dashboard components using the latest fleet
     * telemetry and anomaly information.
     *
     * @param drones current fleet state
     * @param anomalies detected anomalies
     */
    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {

        currentDrones = new ArrayList<>(drones);

        // Preserve selection across updates
        String selectedId = fleetList.getSelectedValue();

        fleetListModel.clear();
        for (Drone d : drones) {
            fleetListModel.addElement(d.getId());
        }

        // Restore previously selected drone, or default to first
        if (selectedId != null) {
            fleetList.setSelectedValue(selectedId, false);
        }
        if (fleetList.getSelectedIndex() == -1 && !drones.isEmpty()) {
            fleetList.setSelectedIndex(0);
        }

        refreshTelemetry();

        anomalyLog.addAll(anomalies);

        applyFilterAndRebuildTable();

        long criticalCount = anomalyLog.stream()
                .filter(a -> "CRITICAL".equals(a.getSeverity()))
                .count();

        long warningCount = anomalyLog.stream()
                .filter(a -> "WARNING".equals(a.getSeverity()))
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
    // FLEET PANEL
    // -------------------------
    private JPanel buildFleetPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(130, 0));

        JLabel title = new JLabel("FLEET");
        title.setForeground(TEXT_MUT);
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(title, BorderLayout.NORTH);

        fleetListModel = new DefaultListModel<>();
        fleetList = new JList<>(fleetListModel);
        fleetList.setBackground(BG_CARD);
        fleetList.setForeground(TEXT_PRI);
        fleetList.setSelectionBackground(ACCENT.darker());
        fleetList.setSelectionForeground(Color.WHITE);
        fleetList.setFont(new Font("Monospaced", Font.BOLD, 13));
        fleetList.setFixedCellHeight(32);
        fleetList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // Update telemetry panel when a drone is selected
        fleetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshTelemetry();
            }
        });

        JScrollPane scroll = new JScrollPane(fleetList);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------
    // MENU BAR
    // -------------------------
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // FILE menu
        JMenu fileMenu = styledMenu("File");

        JMenuItem exportItem = styledMenuItem("Export CSV…");
        exportItem.addActionListener(e -> exportCSV());

        JMenuItem queryItem = styledMenuItem("Query Anomaly Database…");
        queryItem.addActionListener(e -> openQueryDialog());

        JMenuItem exitItem = styledMenuItem("Exit");
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(exportItem);
        fileMenu.add(queryItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // SETTINGS menu
        JMenu settingsMenu = styledMenu("Settings");

        pauseItem = styledMenuItem("Pause Telemetry");
        pauseItem.addActionListener(e -> {
            if (app != null) {
                app.togglePause();
                pauseItem.setText(app.isPaused() ? "Resume Telemetry" : "Pause Telemetry");
            }
        });

        muteItem = styledMenuItem("Mute Alerts");
        muteItem.addActionListener(e -> {
            if (app != null) {
                app.toggleMute();
                muteItem.setText(app.isMuted() ? "Unmute Alerts" : "Mute Alerts");
            }
        });

        settingsMenu.add(pauseItem);
        settingsMenu.add(muteItem);

        bar.add(fileMenu);
        bar.add(settingsMenu);

        return bar;
    }

    // -------------------------
    // STATISTICS PANEL
    // -------------------------
    private JPanel buildStatisticsPanel() {

        JPanel panel = darkCard();
        panel.setLayout(new GridLayout(1, 4, 10, 0));

        totalAnomaliesValue = createStatCard(panel, "TOTAL ANOMALIES", "0", TEXT_PRI);
        criticalValue       = createStatCard(panel, "CRITICAL",        "0", COL_CRIT);
        warningValue        = createStatCard(panel, "WARNINGS",        "0", COL_WARN);
        avgBatteryValue     = createStatCard(panel, "AVG BATTERY",     "0%", ACCENT);

        return panel;
    }

    private JLabel createStatCard(JPanel panel, String title, String value, Color valueColor) {

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUT);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 20));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        panel.add(card);

        return valueLabel;
    }

    // -------------------------
    // SEVERITY FILTER
    // -------------------------
    private void applySeverityFilter() {
        applyFilterAndRebuildTable();
    }

    /** Rebuilds the anomaly table respecting the current severity filter. */
    private void applyFilterAndRebuildTable() {
        String selected = (String) severityFilter.getSelectedItem();
        anomalyTableModel.setRowCount(0);

        // Show most-recent first
        for (int i = anomalyLog.size() - 1; i >= 0; i--) {
            AnomalyRecord a = anomalyLog.get(i);
            if ("ALL".equals(selected) || Objects.equals(selected, a.getSeverity())) {
                anomalyTableModel.addRow(new Object[]{
                        a.getSeverityIcon() + " " + a.getDroneId(),
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
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel grid = new JPanel(new GridLayout(1, 6, 10, 0));
        grid.setOpaque(false);

        String[] labels = {
                "DRONE ID", "ALTITUDE (m)", "BATTERY (%)",
                "VELOCITY", "LATITUDE", "LONGITUDE"
        };

        telemetryValues = new JLabel[6];

        for (int i = 0; i < 6; i++) {
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));

            JLabel title = new JLabel(labels[i]);
            title.setForeground(TEXT_MUT);
            title.setFont(new Font("SansSerif", Font.PLAIN, 10));

            JLabel value = new JLabel("—");
            value.setForeground(ACCENT);
            value.setFont(new Font("Monospaced", Font.BOLD, 14));

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
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        mapPanel = new DroneMapPanel();
        mapPanel.setPreferredSize(new Dimension(0, 220));
        panel.add(mapPanel, BorderLayout.CENTER);
        return panel;
    }

    // -------------------------
    // ANOMALY PANEL
    // -------------------------
    private JPanel buildAnomalyPanel() {

        JPanel panel = darkCard();
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(0, 200));

        // Header row: label + filter
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);

        JLabel label = new JLabel("ANOMALY LOG");
        label.setForeground(TEXT_MUT);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setForeground(TEXT_MUT);
        filterLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        severityFilter = new JComboBox<>(new String[]{
                "ALL", "INFO", "WARNING", "CRITICAL"
        });
        severityFilter.setBackground(BG_CARD);
        severityFilter.setForeground(TEXT_PRI);
        severityFilter.setFont(new Font("SansSerif", Font.PLAIN, 11));
        severityFilter.addActionListener(e -> applySeverityFilter());

        header.add(label);
        header.add(filterLabel);
        header.add(severityFilter);

        String[] cols = {"ID", "TYPE", "SEVERITY", "DETAILS", "TIME"};
        anomalyTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(anomalyTableModel);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRI);
        table.setGridColor(BORDER);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_MUT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 11));
        table.getColumnModel().getColumn(3).setPreferredWidth(320);

        // Colour rows by severity
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            /**
             * This renderer customizes anomaly table rows based on the severity
             * of the anomaly record.
             * <p>
             * The method is invoked automatically by Swing whenever a cell must
             * be drawn or refreshed.
             *
             * @param tbl the table requesting the renderer
             * @param val the value stored in the cell
             * @param selected true if the cell is currently selected
             * @param focused true if the cell currently has focus
             * @param row the row being rendered
             * @param col the column being rendered
             * @return the configured table cell to display
             */
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                                                           boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, selected, focused, row, col);
                setBackground(BG_CARD);
                String sev = (String) tbl.getModel().getValueAt(row, 2);
                if ("CRITICAL".equals(sev)) {
                    setForeground(COL_CRIT);
                } else if ("WARNING".equals(sev)) {
                    setForeground(COL_WARN);
                } else {
                    setForeground(COL_INFO);
                }
                if (selected) setBackground(BG_PANEL);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);

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
        telemetryValues[1].setText(String.format("%.1f", d.getAltitude()));
        telemetryValues[2].setText(String.format("%.1f", d.getBattery()));
        telemetryValues[3].setText(String.format("%.5f", d.getVelocity()));
        telemetryValues[4].setText(String.format("%.5f", d.getLatitude()));
        telemetryValues[5].setText(String.format("%.5f", d.getLongitude()));

        // Colour battery value by level
        double battery = d.getBattery();
        if (battery <= 5.0) {
            telemetryValues[2].setForeground(COL_CRIT);
        } else if (battery < 15.0) {
            telemetryValues[2].setForeground(COL_WARN);
        } else {
            telemetryValues[2].setForeground(ACCENT);
        }
    }

    // -------------------------
    // MENU ACTIONS
    // -------------------------
    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("anomaly_log.csv"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                CSVExporter.export(anomalyLog, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Exported " + anomalyLog.size() + " records.",
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
                "Exit the Drone Fleet Monitor?",
                "Confirm Exit",
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
    private JMenu styledMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(TEXT_PRI);
        return m;
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem mi = new JMenuItem(text);
        mi.setBackground(BG_PANEL);
        mi.setForeground(TEXT_PRI);
        return mi;
    }

    private JPanel darkCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        return p;
    }
}