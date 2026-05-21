package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import model.Drone;
import model.AnomalyRecord;

/**
 * Dashboard is the main application window for the Drone Fleet management system.
 */
public class Dashboard extends JFrame {

    // Color Palette
    private static final Color BG_DARK  = new Color(15,  17,  23);
    private static final Color BG_PANEL = new Color(22,  26,  35);
    private static final Color BG_CARD  = new Color(30,  35,  48);
    private static final Color ACCENT   = new Color(56, 189, 248);
    private static final Color TEXT_PRI = new Color(226, 232, 240);
    private static final Color TEXT_MUT = new Color(100, 116, 139);
    private static final Color BORDER   = new Color(40,  48,  65);

    private DefaultListModel<String> fleetModel;
    private DefaultTableModel anomalyModel;
    private JLabel[] telemetryValues;

    /**
     * Constructs the Dashboard window.
     */
    public Dashboard() {
        setTitle("Drone Fleet Dashboard");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildFleetPanel(),     BorderLayout.WEST);
        add(buildTelemetryPanel(), BorderLayout.CENTER);
        add(buildAnomalyPanel(),   BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        String[][] items = {
                {"New Simulation", "Open", "Save", null, "Exit"},
                {"Refresh", "Clear Anomalies"},
                {"About"}
        };

        for (String name : new String[]{"File", "View", "Help"}) {
            JMenu m = new JMenu(name);
            m.setForeground(TEXT_PRI);
            bar.add(m);
        }

        for (int i = 0; i < 3; i++) {
            JMenu menu = bar.getMenu(i);
            for (String item : items[i]) {
                if (item == null) menu.addSeparator();
                else {
                    JMenuItem mi = new JMenuItem(item);
                    mi.setBackground(BG_PANEL);
                    mi.setForeground(TEXT_PRI);
                    menu.add(mi);
                }
            }
        }
        return bar;
    }

    private JPanel buildFleetPanel() {
        JPanel panel = darkCard();
        panel.setPreferredSize(new Dimension(170, 0));
        panel.setLayout(new BorderLayout(0, 8));
        panel.add(sectionLabel("FLEET"), BorderLayout.NORTH);

        fleetModel = new DefaultListModel<>();
        fleetModel.addElement("D1");
        fleetModel.addElement("D2");
        fleetModel.addElement("D3");

        JList<String> list = new JList<>(fleetModel);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_PRI);
        list.setFont(new Font("Monospaced", Font.PLAIN, 13));
        list.setSelectionBackground(new Color(56, 189, 248, 40));
        list.setSelectionForeground(ACCENT);
        list.setFixedCellHeight(34);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel buildTelemetryPanel() {
        JPanel wrapper = darkCard();
        wrapper.setLayout(new BorderLayout(0, 12));
        wrapper.add(sectionLabel("LIVE TELEMETRY"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);

        String[] names = {
                "DRONE ID", "ALTITUDE (m)", "BATTERY (%)",
                "VELOCITY (m/s)", "LATITUDE", "LONGITUDE"
        };

        telemetryValues = new JLabel[names.length];

        for (int i = 0; i < names.length; i++) {

            JPanel card = new JPanel(new BorderLayout(0, 4));
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));

            JLabel lbl = new JLabel(names[i]);
            lbl.setForeground(TEXT_MUT);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JLabel val = new JLabel("—");
            val.setForeground(ACCENT);
            val.setFont(new Font("Monospaced", Font.BOLD, 22));

            telemetryValues[i] = val;

            card.add(lbl, BorderLayout.NORTH);
            card.add(val, BorderLayout.CENTER);

            grid.add(card);
        }

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildAnomalyPanel() {
        JPanel panel = darkCard();
        panel.setPreferredSize(new Dimension(0, 185));
        panel.setLayout(new BorderLayout(0, 8));
        panel.add(sectionLabel("ANOMALY LOG"), BorderLayout.NORTH);

        String[] cols = {"DRONE ID", "TYPE", "DETAILS", "TIMESTAMP"};

        anomalyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(anomalyModel);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRI);
        table.setGridColor(BORDER);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Updates fleet list + anomaly table (called from controller)
     */
    public void display(List<Drone> drones, List<AnomalyRecord> anomalies) {

        if (fleetModel != null) {
            fleetModel.clear();
            for (Drone d : drones) {
                fleetModel.addElement(d.getId());
            }
        }

        if (anomalyModel != null && anomalies != null) {
            for (AnomalyRecord a : anomalies) {
                anomalyModel.addRow(new Object[]{
                        a.getDroneId(),
                        a.getType(),
                        a.getDetails(),
                        a.getFormattedTimestamp()
                });
            }
        }
    }

    /**
     * Updates live telemetry panel for a selected drone
     */
    public void updateTelemetry(Drone d) {
        if (d == null || telemetryValues == null) return;

        telemetryValues[0].setText(d.getId());
        telemetryValues[1].setText(String.format("%.2f", d.getAltitude()));
        telemetryValues[2].setText(String.format("%.1f", d.getBattery()));
        telemetryValues[3].setText(String.format("%.3f", d.getVelocity()));
        telemetryValues[4].setText(String.format("%.5f", d.getLatitude()));
        telemetryValues[5].setText(String.format("%.5f", d.getLongitude()));
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

    private JLabel sectionLabel(String theText) {
        JLabel lbl = new JLabel(theText);
        lbl.setForeground(TEXT_MUT);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }
}
