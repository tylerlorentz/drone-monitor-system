package view;

import controller.AnomalyDatabase;
import controller.CSVExporter;
import model.AnomalyRecord;
import model.Drone;
import model.DroneStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard is the main application window for the Drone Fleet management system.
 *
 * Implements the View layer in MVC. Provides:
 * - Fleet list panel (left)
 * - Live telemetry grid (centre-top)
 * - Grid-based drone map (centre-bottom)
 * - Anomaly log table (bottom)
 * - Fully wired File / Help menu bar
 * - Query dialog for searching stored anomalies
 *
 * The display(List<Drone>, List<AnomalyRecord>) method is called from
 * the controller (DroneMonitorApp) on every telemetry cycle.
 */
public class Dashboard extends JFrame {

    // -------------------------
    // COLOUR PALETTE
    // -------------------------
    private static final Color BG_DARK    = new Color(15,  17,  23);
    private static final Color BG_PANEL   = new Color(22,  26,  35);
    private static final Color BG_CARD    = new Color(30,  35,  48);
    private static final Color ACCENT     = new Color(56,  189, 248);
    private static final Color TEXT_PRI   = new Color(226, 232, 240);
    private static final Color TEXT_MUT   = new Color(100, 116, 139);
    private static final Color BORDER     = new Color(40,  48,  65);
    private static final Color COL_NORMAL = new Color(34,  197, 94);
    private static final Color COL_WARN   = new Color(251, 191, 36);
    private static final Color COL_CRIT   = new Color(239, 68,  68);

    // -------------------------
    // LIVE UI COMPONENTS
    // -------------------------
    private DefaultListModel<String> fleetListModel;
    private JList<String>            fleetList;
    private JLabel[]                 telemetryValues;   // 6 value labels
    private DefaultTableModel        anomalyTableModel;
    private DroneMapPanel            mapPanel;

    // -------------------------
    // STATE
    // -------------------------
    /** The most recent snapshot of all drones, used for telemetry display. */
    private List<Drone>          currentDrones   = new ArrayList<>();
    /** Running log of all anomalies shown in the table (session-only). */
    private final List<AnomalyRecord> anomalyLog = new ArrayList<>();
    /** Reference to the database, injected via setter after construction. */
    private AnomalyDatabase db;

    // -------------------------
    // CONSTRUCTOR
    // -------------------------

    public Dashboard() {
        setTitle("Drone Fleet Security Monitor");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Main centre column: telemetry top, map below
        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setOpaque(false);
        centre.add(buildTelemetryPanel(), BorderLayout.NORTH);
        centre.add(buildMapPanel(),       BorderLayout.CENTER);

        add(buildFleetPanel(), BorderLayout.WEST);
        add(centre,            BorderLayout.CENTER);
        add(buildAnomalyPanel(), BorderLayout.SOUTH);
    }

    // -------------------------
    // PUBLIC API (called by controller)
    // -------------------------

    /**
     * Injects the database reference so menus can trigger queries.
     * Call this before making the window visible.
     */
    public void setDatabase(AnomalyDatabase db) {
        this.db = db;
    }

    /**
     * Refreshes all UI elements with the latest telemetry and anomalies.
     * Must be called on the EDT (DroneMonitorApp wraps this in invokeLater).
     *
     * @param drones    current drone states
     * @param anomalies anomalies detected this cycle
     */
    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {
        currentDrones = new ArrayList<>(drones);

        // Update fleet list (preserves selection)
        int selectedIndex = fleetList.getSelectedIndex();
        fleetListModel.clear();
        for (Drone d : drones) {
            fleetListModel.addElement(formatDroneListEntry(d));
        }
        if (selectedIndex >= 0 && selectedIndex < drones.size()) {
            fleetList.setSelectedIndex(selectedIndex);
        } else if (!drones.isEmpty()) {
            fleetList.setSelectedIndex(0);
        }

        // Refresh telemetry cards for the selected drone
        refreshTelemetry();

        // Append new anomalies to the table
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

        // Update map
        mapPanel.updateDrones(drones);
    }

    // -------------------------
    // MENU BAR
    // -------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // FILE
        JMenu fileMenu = styledMenu("File");
        JMenuItem saveCsvItem    = styledMenuItem("Save Anomaly Log to CSV");
        JMenuItem queryItem      = styledMenuItem("Query Anomaly Database…");
        JMenuItem exitItem       = styledMenuItem("Exit");

        saveCsvItem.addActionListener(e -> saveAnomalyLogToCSV());
        queryItem.addActionListener(e -> openQueryDialog());
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(saveCsvItem);
        fileMenu.add(queryItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // HELP
        JMenu helpMenu    = styledMenu("Help");
        JMenuItem aboutItem = styledMenuItem("About");
        JMenuItem instrItem = styledMenuItem("Instructions");

        aboutItem.addActionListener(e -> showAbout());
        instrItem.addActionListener(e -> showInstructions());

        helpMenu.add(aboutItem);
        helpMenu.add(instrItem);

        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }

    // -------------------------
    // PANEL BUILDERS
    // -------------------------

    /**
     * Builds the Fleet panel with a live-updated drone list.
     */
    private JPanel buildFleetPanel() {
        JPanel panel = darkCard();
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setLayout(new BorderLayout(0, 8));
        panel.add(sectionLabel("FLEET"), BorderLayout.NORTH);

        fleetListModel = new DefaultListModel<>();
        fleetList = new JList<>(fleetListModel);
        fleetList.setBackground(BG_CARD);
        fleetList.setForeground(TEXT_PRI);
        fleetList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        fleetList.setSelectionBackground(new Color(56, 189, 248, 40));
        fleetList.setSelectionForeground(ACCENT);
        fleetList.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        fleetList.setFixedCellHeight(36);

        // Selecting a drone updates the telemetry panel
        fleetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshTelemetry();
        });

        JScrollPane scroll = new JScrollPane(fleetList);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the Telemetry panel with 6 live metric cards.
     * Labels: Drone ID, Altitude, Battery, Velocity, Latitude, Longitude
     */
    private JPanel buildTelemetryPanel() {
        JPanel wrapper = darkCard();
        wrapper.setLayout(new BorderLayout(0, 10));
        wrapper.add(sectionLabel("LIVE TELEMETRY"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 6, 10, 0));
        grid.setOpaque(false);

        String[] names = {"DRONE ID", "ALTITUDE (m)", "BATTERY (%)",
                          "VELOCITY", "LATITUDE", "LONGITUDE"};
        telemetryValues = new JLabel[names.length];

        for (int i = 0; i < names.length; i++) {
            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            JLabel lbl = new JLabel(names[i]);
            lbl.setForeground(TEXT_MUT);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            JLabel val = new JLabel("—");
            val.setForeground(ACCENT);
            val.setFont(new Font("Monospaced", Font.BOLD, 18));
            telemetryValues[i] = val;
            card.add(lbl, BorderLayout.NORTH);
            card.add(val, BorderLayout.CENTER);
            grid.add(card);
        }

        wrapper.add(grid, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 110));
        return wrapper;
    }

    /**
     * Builds the grid-based drone map panel.
     */
    private JPanel buildMapPanel() {
        JPanel wrapper = darkCard();
        wrapper.setLayout(new BorderLayout(0, 8));
        wrapper.add(sectionLabel("DRONE POSITIONS (GRID MAP)"), BorderLayout.NORTH);
        mapPanel = new DroneMapPanel();
        wrapper.add(mapPanel, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Builds the Anomaly Log panel with a live-appended table.
     * Columns: Drone ID, Type, Severity, Details, Timestamp
     */
    private JPanel buildAnomalyPanel() {
        JPanel panel = darkCard();
        panel.setPreferredSize(new Dimension(0, 195));
        panel.setLayout(new BorderLayout(0, 8));
        panel.add(sectionLabel("ANOMALY LOG"), BorderLayout.NORTH);

        String[] cols = {"DRONE ID", "TYPE", "SEVERITY", "DETAILS", "TIMESTAMP"};
        anomalyTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(anomalyTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                String sev = (String) getModel().getValueAt(row, 2);
                if ("CRITICAL".equals(sev)) {
                    c.setForeground(COL_CRIT);
                } else if ("WARNING".equals(sev)) {
                    c.setForeground(COL_WARN);
                } else {
                    c.setForeground(TEXT_PRI);
                }
                c.setBackground(isRowSelected(row) ? new Color(56, 189, 248, 30) : BG_CARD);
                return c;
            }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRI);
        table.setGridColor(BORDER);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_MUT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 11));
        table.setShowVerticalLines(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // -------------------------
    // TELEMETRY REFRESH
    // -------------------------

    /** Updates the 6 telemetry cards for the currently selected drone. */
    private void refreshTelemetry() {
        int idx = fleetList.getSelectedIndex();
        if (idx < 0 || idx >= currentDrones.size()) return;

        Drone d = currentDrones.get(idx);
        telemetryValues[0].setText(d.getId());
        telemetryValues[1].setText(String.format("%.1f", d.getAltitude()));
        telemetryValues[2].setText(String.format("%.1f", d.getBattery()));
        telemetryValues[3].setText(String.format("%.4f", d.getVelocity()));
        telemetryValues[4].setText(String.format("%.5f", d.getLatitude()));
        telemetryValues[5].setText(String.format("%.5f", d.getLongitude()));

        // Colour-code battery card
        DroneStatus status = d.getStatus();
        Color statusColor = switch (status) {
            case CRITICAL -> COL_CRIT;
            case WARNING  -> COL_WARN;
            default       -> ACCENT;
        };
        telemetryValues[2].setForeground(statusColor);
    }

    // -------------------------
    // MENU ACTIONS
    // -------------------------

    private void saveAnomalyLogToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("anomaly_log.csv"));
        chooser.setDialogTitle("Save Anomaly Log as CSV");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVExporter.export(anomalyLog, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                    "Anomaly log saved successfully.\n(" + anomalyLog.size() + " records)",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to save: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openQueryDialog() {
        new QueryDialog(this, db).setVisible(true);
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Exit Drone Fleet Monitor?", "Confirm Exit",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (db != null) db.close();
            System.exit(0);
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "<html><b>Drone Fleet Security Monitor</b><br>" +
            "Version 1.0<br><br>" +
            "Monitors autonomous drone fleets for anomalies<br>" +
            "including GPS spoofing, low battery, and unsafe maneuvers.<br><br>" +
            "Anomalies are persisted to a local SQLite database.</html>",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
            "<html><b>Instructions</b><br><br>" +
            "• The fleet list (left) shows all active drones with their status.<br>" +
            "• Click a drone to view its live telemetry in the cards above the map.<br>" +
            "• The grid map shows relative drone positions in real time.<br>" +
            "• The anomaly log (bottom) records all detected issues, colour-coded by severity.<br><br>" +
            "<b>Anomaly Types:</b><br>" +
            "  CRITICAL_BATTERY — battery ≤ 5%<br>" +
            "  LOW_BATTERY      — battery &lt; 15%<br>" +
            "  CRASH_RISK       — altitude ≤ 2 m<br>" +
            "  ALTITUDE_RISK    — altitude &lt; 5 m<br>" +
            "  GPS_SPOOFING     — unexpected location jump<br>" +
            "  ALTITUDE_DROP    — sudden descent (&gt;10 m/cycle)<br>" +
            "  SHARP_TURN       — heading change &gt;90°/cycle<br><br>" +
            "<b>File Menu:</b><br>" +
            "  Save Anomaly Log to CSV — exports all session anomalies<br>" +
            "  Query Anomaly Database  — search the SQLite anomaly log<br>" +
            "  Exit — close the application</html>",
            "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------
    // HELPERS
    // -------------------------

    private String formatDroneListEntry(Drone d) {
        String dot = switch (d.getStatus()) {
            case CRITICAL -> "● ";
            case WARNING  -> "◆ ";
            default       -> "○ ";
        };
        return dot + d.getId();
    }

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
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_MUT);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }
}
