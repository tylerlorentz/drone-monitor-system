package view;

import controller.AnomalyDatabase;
import model.AnomalyRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modal dialog that lets operators search the SQLite anomaly database.
 *
 * Query modes:
 *   1. All records
 *   2. By drone ID
 *   3. By anomaly type
 *   4. By date range
 *
 * Results are shown in a scrollable table inside the dialog.
 */
public class QueryDialog extends JDialog {

    private static final Color BG_DARK  = new Color(15,  17,  23);
    private static final Color BG_PANEL = new Color(22,  26,  35);
    private static final Color BG_CARD  = new Color(30,  35,  48);
    private static final Color ACCENT   = new Color(56,  189, 248);
    private static final Color TEXT_PRI = new Color(226, 232, 240);
    private static final Color TEXT_MUT = new Color(100, 116, 139);
    private static final Color BORDER   = new Color(40,  48,  65);

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AnomalyDatabase db;
    private DefaultTableModel resultModel;

    // Query inputs
    private JComboBox<String> queryTypeBox;
    private JTextField        param1Field;
    private JTextField        param2Field;
    private JLabel            param1Label;
    private JLabel            param2Label;

    public QueryDialog(Frame parent, AnomalyDatabase db) {
        super(parent, "Query Anomaly Database", true);
        this.db = db;
        setSize(780, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildQueryBar(),   BorderLayout.NORTH);
        add(buildResultTable(), BorderLayout.CENTER);
    }

    private JPanel buildQueryBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] modes = {"All Records", "By Drone ID", "By Type", "By Date Range"};
        queryTypeBox = new JComboBox<>(modes);
        style(queryTypeBox);

        param1Label = styledLabel("Drone ID:");
        param2Label = styledLabel("End (yyyy-MM-dd HH:mm:ss):");
        param1Field = styledField(14);
        param2Field = styledField(19);

        JButton runBtn = new JButton("Run Query");
        runBtn.setBackground(ACCENT);
        runBtn.setForeground(BG_DARK);
        runBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> runQuery());

        // Initial state
        updateParamLabels("All Records");
        queryTypeBox.addActionListener(e ->
            updateParamLabels((String) queryTypeBox.getSelectedItem()));

        panel.add(styledLabel("Query:"));
        panel.add(queryTypeBox);
        panel.add(param1Label);
        panel.add(param1Field);
        panel.add(param2Label);
        panel.add(param2Field);
        panel.add(runBtn);

        return panel;
    }

    private JScrollPane buildResultTable() {
        String[] cols = {"DRONE ID", "TYPE", "SEVERITY", "DETAILS", "TIMESTAMP"};
        resultModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(resultModel);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRI);
        table.setGridColor(BORDER);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_MUT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 11));
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private void runQuery() {
        if (db == null) {
            JOptionPane.showMessageDialog(this,
                "Database not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mode = (String) queryTypeBox.getSelectedItem();
        List<AnomalyRecord> results;

        try {
            results = switch (mode) {
                case "By Drone ID"    -> db.queryByDroneId(param1Field.getText().trim());
                case "By Type"        -> db.queryByType(param1Field.getText().trim());
                case "By Date Range"  -> {
                    LocalDateTime from = LocalDateTime.parse(param1Field.getText().trim(), FMT);
                    LocalDateTime to   = LocalDateTime.parse(param2Field.getText().trim(), FMT);
                    yield db.queryByDateRange(from, to);
                }
                default -> db.queryAll();
            };
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Use: yyyy-MM-dd HH:mm:ss",
                "Parse Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Populate results
        resultModel.setRowCount(0);
        for (AnomalyRecord r : results) {
            resultModel.addRow(new Object[]{
                r.getDroneId(), r.getType(), r.getSeverity(),
                r.getDetails(), r.getFormattedTimestamp()
            });
        }
        setTitle("Query Results — " + results.size() + " record(s)");
    }

    private void updateParamLabels(String mode) {
        switch (mode) {
            case "By Drone ID" -> {
                param1Label.setText("Drone ID:");
                param1Label.setVisible(true);
                param1Field.setVisible(true);
                param2Label.setVisible(false);
                param2Field.setVisible(false);
            }
            case "By Type" -> {
                param1Label.setText("Anomaly Type:");
                param1Label.setVisible(true);
                param1Field.setVisible(true);
                param2Label.setVisible(false);
                param2Field.setVisible(false);
            }
            case "By Date Range" -> {
                param1Label.setText("Start (yyyy-MM-dd HH:mm:ss):");
                param2Label.setText("End (yyyy-MM-dd HH:mm:ss):");
                param1Label.setVisible(true);
                param1Field.setVisible(true);
                param2Label.setVisible(true);
                param2Field.setVisible(true);
            }
            default -> {
                param1Label.setVisible(false);
                param1Field.setVisible(false);
                param2Label.setVisible(false);
                param2Field.setVisible(false);
            }
        }
    }

    // ---- styling helpers ----
    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(BG_CARD);
        f.setForeground(TEXT_PRI);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        f.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return f;
    }

    private void style(JComboBox<String> box) {
        box.setBackground(BG_CARD);
        box.setForeground(TEXT_PRI);
        box.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }
}
