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

    private DefaultListModel<String> myFleetListModel;
    private JList<String> myFleetList;

    private JLabel myIdValue;
    private JLabel myAltitudeValue;
    private JLabel myBatteryValue;
    private JLabel myVelocityValue;
    private JLabel myLatitudeValue;
    private JLabel myLongitudeValue;

    private DefaultTableModel myAnomalyTableModel;

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
     * Builds the menu bar with ActionListeners wired to each item.
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(buildFileMenu());
        menuBar.add(buildViewMenu());
        menuBar.add(buildHelpMenu());

        return menuBar;
    }


    /**
     * Builds the fleet panel.
     */
    private JPanel buildFleetPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Fleet"));

        myFleetListModel = new DefaultListModel<>();
        myFleetList = new JList<>(myFleetListModel);

        panel.add(new JScrollPane(myFleetList), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Builds the telemetry panel.
     */
    private JPanel buildTelemetryPanel() {

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.setBorder(BorderFactory.createTitledBorder("Telemetry"));

        myIdValue = new JLabel("—");
        myAltitudeValue = new JLabel("—");
        myBatteryValue = new JLabel("—");
        myVelocityValue = new JLabel("—");
        myLatitudeValue = new JLabel("—");
        myLongitudeValue = new JLabel("—");

        panel.add(new JLabel("ID:"));
        panel.add(myIdValue);

        panel.add(new JLabel("Altitude (m):"));
        panel.add(myAltitudeValue);

        panel.add(new JLabel("Battery (%):"));
        panel.add(myBatteryValue);

        panel.add(new JLabel("Velocity (m/s):"));
        panel.add(myVelocityValue);

        panel.add(new JLabel("Latitude:"));
        panel.add(myLatitudeValue);

        panel.add(new JLabel("Longitude:"));
        panel.add(myLongitudeValue);

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

        myAnomalyTableModel = new DefaultTableModel(columns, 0);

        JTable anomalyTable = new JTable(myAnomalyTableModel);

        panel.add(new JScrollPane(anomalyTable), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the dashboard with live drone and anomaly data.
     */
    public void display(List<Drone> theDrones, List<AnomalyRecord> theAnomalies) {

        updateFleetList(theDrones);

        updateTelemetry(theDrones);

        updateAnomalyTable(theAnomalies);
    }

    // HELPER METHODS --------------------------------------------------------//

    /**
     * Updates fleet list.
     */
    private void updateFleetList(List<Drone> theDrones) {

        myFleetListModel.clear();

        for (Drone drone : theDrones) {
            myFleetListModel.addElement(drone.getId());
        }
    }

    /**
     * Updates telemetry labels using the first drone in the list.
     */
    private void updateTelemetry(List<Drone> theDrones) {

        if (theDrones.isEmpty()) {
            return;
        }

        Drone drone = theDrones.get(0);

        myIdValue.setText(drone.getId());

        myAltitudeValue.setText(
                String.format("%.2f", drone.getAltitude())
        );

        myBatteryValue.setText(
                String.format("%.2f", drone.getBattery())
        );

        myVelocityValue.setText(
                String.format("%.2f", drone.getVelocity())
        );

        myLatitudeValue.setText(
                String.format("%.4f", drone.getLatitude())
        );

        myLongitudeValue.setText(
                String.format("%.4f", drone.getLongitude())
        );
    }

    /**
     * Adds anomaly rows to the anomaly table.
     */
    private void updateAnomalyTable(List<AnomalyRecord> theAnomalies) {

        for (AnomalyRecord anomaly : theAnomalies) {

            myAnomalyTableModel.addRow(new Object[] {
                    anomaly.getDroneId(),
                    anomaly.getType(),
                    anomaly.getDetails(),
                    anomaly.getTimestamp()
            });
        }
    }

    // JMenu Methods ------------------------------------------------------//
    /**
     * Builds the File menu.
     */
    private JMenu buildFileMenu() {

        JMenu fileMenu = new JMenu("File");

        JMenuItem newSimulation = new JMenuItem("New Simulation");
        newSimulation.addActionListener(e -> onNewSimulation());

        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(e -> onOpen());

        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(e -> onSave());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> onExit());

        fileMenu.add(newSimulation);
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        return fileMenu;
    }

    /**
     * Builds the View menu.
     */
    private JMenu buildViewMenu() {

        JMenu viewMenu = new JMenu("View");

        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> onRefresh());

        JMenuItem clearAnomalies = new JMenuItem("Clear Anomalies");
        clearAnomalies.addActionListener(e -> onClearAnomalies());

        viewMenu.add(refresh);
        viewMenu.add(clearAnomalies);

        return viewMenu;
    }

    /**
     * Builds the Help menu.
     */
    private JMenu buildHelpMenu() {

        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> onAbout());

        helpMenu.add(about);

        return helpMenu;
    }

    /**
     * Resets the dashboard to a blank state for a new simulation.
     * Note: Still needs to reset fleet.
     */
    private void onNewSimulation() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Start a new simulation? All current data will be cleared.",
                "New Simulation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            myFleetListModel.clear();
            myAnomalyTableModel.setRowCount(0);
            clearTelemetry();
        }
    }

    /**
     * Opens a file chooser so the user can load a simulation file.
     * Wire this to your file-loading logic as needed.
     * Needs to fetch from DB
     */
    private void onOpen() {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Open Simulation File");
//        int result = fileChooser.showOpenDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            // TODO: pass fileChooser.getSelectedFile() to your loader
//            JOptionPane.showMessageDialog(
//                    this,
//                    "Selected: " + fileChooser.getSelectedFile().getAbsolutePath(),
//                    "Open",
//                    JOptionPane.INFORMATION_MESSAGE
//            );
//        }
    }

    /**
     * Opens a file chooser so the user can save the current simulation state.
     * Wire this to your file-saving logic as needed.
     * Note: We will need it to save to a DB.
     */
    private void onSave() {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Save Simulation File");
//        int result = fileChooser.showSaveDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            // TODO: pass fileChooser.getSelectedFile() to your saver
//            JOptionPane.showMessageDialog(
//                    this,
//                    "Saved to: " + fileChooser.getSelectedFile().getAbsolutePath(),
//                    "Save",
//                    JOptionPane.INFORMATION_MESSAGE
//            );
//        }
    }

    /**
     * Prompts the user before closing the application.
     */
    private void onExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Triggers a UI refresh.
     * Override or extend this to re-fetch live data from your controller/service.
     */
    private void onRefresh() {
        // TODO: call your controller to re-fetch and pass updated lists to display()
        JOptionPane.showMessageDialog(
                this,
                "Dashboard refreshed.",
                "Refresh",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Clears all rows from the anomaly log table.
     */
    private void onClearAnomalies() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Clear all anomaly records?",
                "Clear Anomalies",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            myAnomalyTableModel.setRowCount(0);
        }
    }

    /**
     * Shows an About dialog.
     */
    private void onAbout() {
        JOptionPane.showMessageDialog(
                this,
                "Drone Fleet Dashboard\nVersion 1.0\n\nMonitors drone telemetry and anomaly events.",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Resets all telemetry labels to their default placeholder.
     */
    private void clearTelemetry() {
        myIdValue.setText("—");
        myAltitudeValue.setText("—");
        myBatteryValue.setText("—");
        myVelocityValue.setText("—");
        myLatitudeValue.setText("—");
        myLongitudeValue.setText("—");
    }
}
