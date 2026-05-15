package view;

import model.AnomalyRecord;
import model.Drone;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dashboard is the main application window for the Drone Fleet management system.
 * It extends JFrame and organises the UI into three panels:
 *   - Fleet panel (left): lists all drones in the fleet
 *   - Telemetry panel (centre): displays live telemetry data
 *   - Anomaly panel (bottom): shows detected anomalies
 */
public class Dashboard extends JFrame {

    // Color Palette
    private static final Color BG_DARK  = new Color(15, 17, 23);
    private static final Color BG_PANEL = new Color(22, 26, 35);
    private static final Color BG_CARD  = new Color(30, 35, 48);
    private static final Color ACCENT   = new Color(56, 189, 248);
    private static final Color TEXT_PRI = new Color(226, 232, 240);
    private static final Color TEXT_MUT = new Color(100, 116, 139);
    private static final Color BORDER   = new Color(40, 48, 65);

    // Duplicate anomaly filtering
    private Set<String> displayedAnomalies = new HashSet<>();

    // Dashboard models
    private DefaultTableModel anomalyTableModel;

    /**
     * Constructs dashboard window.
     */
    public Dashboard() {

        setTitle("Drone Fleet Dashboard");

        setSize(960, 640);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_DARK);

        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(8, 8));

        getRootPane().setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );

        add(buildFleetPanel(), BorderLayout.WEST);

        add(buildTelemetryPanel(), BorderLayout.CENTER);

        add(buildAnomalyPanel(), BorderLayout.SOUTH);
    }

    /**
     * Updates dashboard using current drone and anomaly data.
     */
    public void display(List<Drone> drones,
                        List<AnomalyRecord> anomalies) {

        updateAnomalyTable(anomalies);
    }

    /**
     * Builds menu bar.
     */
    private JMenuBar buildMenuBar() {

        JMenuBar bar = new JMenuBar();

        bar.setBackground(BG_PANEL);

        bar.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER)
        );

        String[][] items = {
                {"New Simulation", "Open", "Save", null, "Exit"},
                {"Refresh", "Clear Anomalies"},
                {"About"}
        };

        for (String name : new String[]{"File", "View", "Help"}) {

            JMenu menu = new JMenu(name);

            menu.setForeground(TEXT_PRI);

            bar.add(menu);
        }

        for (int i = 0; i < 3; i++) {

            JMenu menu = bar.getMenu(i);

            for (String item : items[i]) {

                if (item == null) {

                    menu.addSeparator();

                } else {

                    JMenuItem mi = new JMenuItem(item);

                    mi.setBackground(BG_PANEL);

                    mi.setForeground(TEXT_PRI);

                    menu.add(mi);
                }
            }
        }

        return bar;
    }

    /**
     * Builds fleet panel.
     */
    private JPanel buildFleetPanel() {

        JPanel panel = darkCard();

        panel.setPreferredSize(new Dimension(170, 0));

        panel.setLayout(new BorderLayout(0, 8));

        panel.add(sectionLabel("FLEET"), BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();

        for (String d : new String[]{
                "DRONE-001",
                "DRONE-002",
                "DRONE-003",
                "DRONE-004"
        }) {
            model.addElement(d);
        }

        JList<String> list = new JList<>(model);

        list.setBackground(BG_CARD);

        list.setForeground(TEXT_PRI);

        list.setFont(new Font("Monospaced", Font.PLAIN, 13));

        list.setSelectionBackground(new Color(56, 189, 248, 40));

        list.setSelectionForeground(ACCENT);

        list.setBorder(
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        );

        list.setFixedCellHeight(34);

        list.setSelectedIndex(0);

        JScrollPane scroll = new JScrollPane(list);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scroll.getViewport().setBackground(BG_CARD);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Builds telemetry panel.
     */
    private JPanel buildTelemetryPanel() {

        JPanel wrapper = darkCard();

        wrapper.setLayout(new BorderLayout(0, 12));

        wrapper.add(sectionLabel("LIVE TELEMETRY"),
                BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));

        grid.setOpaque(false);

        String[] names = {
                "DRONE ID",
                "ALTITUDE (m)",
                "BATTERY (%)",
                "VELOCITY (m/s)",
                "LATITUDE",
                "LONGITUDE"
        };

        for (String name : names) {

            JPanel card = new JPanel(new BorderLayout(0, 4));

            card.setBackground(BG_CARD);

            card.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER),
                            BorderFactory.createEmptyBorder(10, 14, 10, 14)
                    )
            );

            JLabel lbl = new JLabel(name);

            lbl.setForeground(TEXT_MUT);

            lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JLabel val = new JLabel("—");

            val.setForeground(ACCENT);

            val.setFont(new Font("Monospaced", Font.BOLD, 22));

            card.add(lbl, BorderLayout.NORTH);

            card.add(val, BorderLayout.CENTER);

            grid.add(card);
        }

        wrapper.add(grid, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * Builds anomaly table panel.
     */
    private JPanel buildAnomalyPanel() {

        JPanel panel = darkCard();

        panel.setPreferredSize(new Dimension(0, 185));

        panel.setLayout(new BorderLayout(0, 8));

        panel.add(sectionLabel("ANOMALY LOG"),
                BorderLayout.NORTH);

        String[] cols = {
                "DRONE ID",
                "TYPE",
                "DETAILS",
                "TIMESTAMP"
        };

        anomalyTableModel = new DefaultTableModel(cols, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(anomalyTableModel);

        table.setBackground(BG_CARD);

        table.setForeground(TEXT_PRI);

        table.setGridColor(BORDER);

        table.setFont(new Font("Monospaced", Font.PLAIN, 12));

        table.setRowHeight(24);

        table.getTableHeader().setBackground(BG_PANEL);

        table.getTableHeader().setForeground(TEXT_MUT);

        table.getTableHeader().setFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );

        table.setShowVerticalLines(false);

        table.setSelectionBackground(
                new Color(56, 189, 248, 30)
        );

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scroll.getViewport().setBackground(BG_CARD);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates anomaly table while preventing duplicate spam.
     */
    private void updateAnomalyTable(List<AnomalyRecord> anomalies) {

        for (AnomalyRecord anomaly : anomalies) {

            String anomalyKey =
                    anomaly.getDroneId()
                            + "-"
                            + anomaly.getType();

            // Prevent repeated spam
            if (displayedAnomalies.contains(anomalyKey)) {
                continue;
            }

            displayedAnomalies.add(anomalyKey);

            anomalyTableModel.addRow(new Object[]{
                    anomaly.getDroneId(),
                    anomaly.getType(),
                    anomaly.getDetails(),
                    anomaly.getFormattedTimestamp()
            });
        }
    }

    /**
     * Builds reusable dark panel.
     */
    private JPanel darkCard() {

        JPanel p = new JPanel();

        p.setBackground(BG_PANEL);

        p.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        );

        return p;
    }

    /**
     * Creates section label.
     */
    private JLabel sectionLabel(String theText) {

        JLabel lbl = new JLabel(theText);

        lbl.setForeground(TEXT_MUT);

        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));

        lbl.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 4, 0)
        );

        return lbl;
    }
}