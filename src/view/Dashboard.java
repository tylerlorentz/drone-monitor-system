package view;

import model.AnomalyRecord;
import model.Drone;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dashboard extends JFrame {

    private static final Color BG_DARK  = new Color(15, 17, 23);
    private static final Color BG_PANEL = new Color(22, 26, 35);
    private static final Color BG_CARD  = new Color(30, 35, 48);

    private static final Color ACCENT   = new Color(56, 189, 248);

    private static final Color TEXT_PRI = new Color(226, 232, 240);
    private static final Color TEXT_MUT = new Color(100, 116, 139);

    private static final Color BORDER   = new Color(40, 48, 65);

    private DefaultTableModel anomalyTableModel;

    private Set<String> displayedAnomalies =
            new HashSet<>();

    // Live telemetry labels
    private JLabel droneIdValue;
    private JLabel altitudeValue;
    private JLabel batteryValue;
    private JLabel velocityValue;
    private JLabel latitudeValue;
    private JLabel longitudeValue;

    private Drone selectedDrone;
    private List<Drone> currentDrones;

    public Dashboard() {

        setTitle("Drone Fleet Dashboard");

        setSize(960, 640);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_DARK);

        setLayout(new BorderLayout(8, 8));

        getRootPane().setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        add(buildFleetPanel(), BorderLayout.WEST);

        add(buildTelemetryPanel(), BorderLayout.CENTER);

        add(buildAnomalyPanel(), BorderLayout.SOUTH);
    }

    public void display(List<Drone> drones,
                        List<AnomalyRecord> anomalies) {

        currentDrones = drones;

        // Default selected drone
        if (selectedDrone == null
                && !drones.isEmpty()) {

            selectedDrone = drones.get(0);
        }

        // -------------------------
        // LIVE TELEMETRY UPDATES
        // -------------------------
        if (selectedDrone != null) {

            droneIdValue.setText(
                    selectedDrone.getId()
            );

            altitudeValue.setText(
                    String.format(
                            "%.2f m",
                            selectedDrone.getAltitude()
                    )
            );

            batteryValue.setText(
                    String.format(
                            "%.1f%%",
                            selectedDrone.getBattery()
                    )
            );

            velocityValue.setText(
                    String.format(
                            "%.3f",
                            selectedDrone.getVelocity()
                    )
            );

            latitudeValue.setText(
                    String.format(
                            "%.5f",
                            selectedDrone.getLatitude()
                    )
            );

            longitudeValue.setText(
                    String.format(
                            "%.5f",
                            selectedDrone.getLongitude()
                    )
            );
        }

        updateAnomalyTable(anomalies);
    }

    private JPanel buildFleetPanel() {

        JPanel panel = darkCard();

        panel.setPreferredSize(
                new Dimension(170, 0)
        );

        panel.setLayout(
                new BorderLayout()
        );

        panel.add(
                sectionLabel("FLEET"),
                BorderLayout.NORTH
        );

        DefaultListModel<String> model =
                new DefaultListModel<>();

        model.addElement("D1");
        model.addElement("D2");
        model.addElement("D3");

        JList<String> list =
                new JList<>(model);

        list.setBackground(BG_CARD);

        list.setForeground(TEXT_PRI);

        list.setSelectionBackground(
                new Color(56, 189, 248, 40)
        );

        list.setSelectionForeground(ACCENT);

        // --------------------------------------
        // INTERACTIVE DRONE SELECTION
        // --------------------------------------
        list.addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            String selectedId =
                    list.getSelectedValue();

            if (selectedId == null
                    || currentDrones == null) {
                return;
            }

            for (Drone d : currentDrones) {

                if (d.getId().equals(selectedId)) {

                    selectedDrone = d;

                    break;
                }
            }
        });

        JScrollPane scroll =
                new JScrollPane(list);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTelemetryPanel() {

        JPanel wrapper = darkCard();

        wrapper.setLayout(
                new GridLayout(2, 3, 10, 10)
        );

        droneIdValue =
                telemetryCard(wrapper, "DRONE ID");

        altitudeValue =
                telemetryCard(wrapper, "ALTITUDE");

        batteryValue =
                telemetryCard(wrapper, "BATTERY");

        velocityValue =
                telemetryCard(wrapper, "VELOCITY");

        latitudeValue =
                telemetryCard(wrapper, "LATITUDE");

        longitudeValue =
                telemetryCard(wrapper, "LONGITUDE");

        return wrapper;
    }

    private JLabel telemetryCard(
            JPanel parent,
            String title
    ) {

        JPanel card =
                new JPanel(
                        new BorderLayout()
                );

        card.setBackground(BG_CARD);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                10,
                                10,
                                10,
                                10
                        )
                )
        );

        JLabel lbl =
                new JLabel(title);

        lbl.setForeground(TEXT_MUT);

        JLabel value =
                new JLabel("—");

        value.setForeground(ACCENT);

        value.setFont(
                new Font(
                        "Monospaced",
                        Font.BOLD,
                        20
                )
        );

        card.add(lbl, BorderLayout.NORTH);

        card.add(value, BorderLayout.CENTER);

        parent.add(card);

        return value;
    }

    private JPanel buildAnomalyPanel() {

        JPanel panel = darkCard();

        panel.setPreferredSize(
                new Dimension(0, 180)
        );

        panel.setLayout(
                new BorderLayout()
        );

        String[] cols = {
                "DRONE",
                "TYPE",
                "DETAILS",
                "TIME"
        };

        anomalyTableModel =
                new DefaultTableModel(cols, 0);

        JTable table =
                new JTable(anomalyTableModel);

        table.setBackground(BG_CARD);

        table.setForeground(TEXT_PRI);

        table.setGridColor(BORDER);

        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component
                    getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component cell =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        String type =
                                table.getValueAt(row, 1)
                                        .toString();

                        if (type.contains("CRITICAL")
                                || type.contains("CRASH")
                                || type.contains("EMERGENCY")) {

                            cell.setForeground(Color.RED);

                        } else if (type.contains("LOW")
                                || type.contains("GPS")) {

                            cell.setForeground(Color.ORANGE);

                        } else {

                            cell.setForeground(TEXT_PRI);
                        }

                        cell.setBackground(BG_CARD);

                        return cell;
                    }
                });

        JScrollPane scroll =
                new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void updateAnomalyTable(
            List<AnomalyRecord> anomalies
    ) {

        for (AnomalyRecord anomaly : anomalies) {

            String key =
                    anomaly.getDroneId()
                            + anomaly.getType();

            if (displayedAnomalies.contains(key)) {
                continue;
            }

            displayedAnomalies.add(key);

            anomalyTableModel.addRow(
                    new Object[]{
                            anomaly.getDroneId(),
                            anomaly.getType(),
                            anomaly.getDetails(),
                            anomaly.getTimestamp()
                    }
            );
        }
    }

    private JPanel darkCard() {

        JPanel p = new JPanel();

        p.setBackground(BG_PANEL);

        p.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                10,
                                10,
                                10,
                                10
                        )
                )
        );

        return p;
    }

    private JLabel sectionLabel(String text) {

        JLabel lbl =
                new JLabel(text);

        lbl.setForeground(TEXT_MUT);

        lbl.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        11
                )
        );

        return lbl;
    }
}