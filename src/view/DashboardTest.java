package view;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardTest {
    private Dashboard dashboard;

    @BeforeEach
    void setUp() throws Exception {
        // GUI construction must happen on the EDT
        SwingUtilities.invokeAndWait(() -> dashboard = new Dashboard());
    }

    @AfterEach
    void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            dashboard.dispose();
            dashboard = null;
        });
    }

    // --- Frame properties ---

    @Test
    void testTitle() {
        assertEquals("Drone Fleet Dashboard", dashboard.getTitle());
    }

    @Test
    void testSize() {
        assertEquals(new Dimension(900, 600), dashboard.getSize());
    }

    @Test
    void testCloseOperation() {
        assertEquals(JFrame.EXIT_ON_CLOSE, dashboard.getDefaultCloseOperation());
    }

    // --- Menu bar ---

    @Test
    void testMenuBarExists() {
        assertNotNull(dashboard.getJMenuBar());
    }

    @Test
    void testMenuBarHasThreeMenus() {
        assertEquals(3, dashboard.getJMenuBar().getMenuCount());
    }

    @Test
    void testMenuNames() {
        JMenuBar bar = dashboard.getJMenuBar();
        assertEquals("File", bar.getMenu(0).getText());
        assertEquals("View", bar.getMenu(1).getText());
        assertEquals("Help", bar.getMenu(2).getText());
    }

    @Test
    void testFileMenuItems() {
        JMenu fileMenu = dashboard.getJMenuBar().getMenu(0);
        // getItemCount includes separators; getItem(i) returns null for separators
        assertEquals("New Simulation", fileMenu.getItem(0).getText());
        assertEquals("Open",           fileMenu.getItem(1).getText());
        assertEquals("Save",           fileMenu.getItem(2).getText());
        // index 3 is the separator → getItem returns null
        assertNull(fileMenu.getItem(3));
        assertEquals("Exit",           fileMenu.getItem(4).getText());
    }

    @Test
    void testViewMenuItems() {
        JMenu viewMenu = dashboard.getJMenuBar().getMenu(1);
        assertEquals("Refresh",         viewMenu.getItem(0).getText());
        assertEquals("Clear Anomalies", viewMenu.getItem(1).getText());
    }

    @Test
    void testHelpMenuItems() {
        JMenu helpMenu = dashboard.getJMenuBar().getMenu(2);
        assertEquals("About", helpMenu.getItem(0).getText());
    }

    // --- Layout panels ---

    @Test
    void testBorderLayoutIsUsed() {
        assertTrue(dashboard.getContentPane().getLayout() instanceof BorderLayout);
    }

    @Test
    void testFleetPanelIsWest() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        Component west = layout.getLayoutComponent(BorderLayout.WEST);
        assertNotNull(west, "Fleet panel should be in WEST");
        assertTrue(west instanceof JPanel);
    }

    @Test
    void testTelemetryPanelIsCenter() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        assertNotNull(center, "Telemetry panel should be in CENTER");
        assertTrue(center instanceof JPanel);
    }

    @Test
    void testAnomalyPanelIsSouth() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        Component south = layout.getLayoutComponent(BorderLayout.SOUTH);
        assertNotNull(south, "Anomaly panel should be in SOUTH");
        assertTrue(south instanceof JPanel);
    }

    // --- Panel sizing ---

    @Test
    void testFleetPanelPreferredWidth() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel west = (JPanel) layout.getLayoutComponent(BorderLayout.WEST);
        assertEquals(200, west.getPreferredSize().width);
    }

    @Test
    void testAnomalyPanelPreferredHeight() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel south = (JPanel) layout.getLayoutComponent(BorderLayout.SOUTH);
        assertEquals(180, south.getPreferredSize().height);
    }

    // --- Telemetry panel content ---

    @Test
    void testTelemetryHasSixRowGridLayout() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel center = (JPanel) layout.getLayoutComponent(BorderLayout.CENTER);
        assertTrue(center.getLayout() instanceof GridLayout);
        GridLayout grid = (GridLayout) center.getLayout();
        assertEquals(6, grid.getRows());
        assertEquals(2, grid.getColumns());
    }

    @Test
    void testTelemetryLabelsInitialisedToDash() {
        BorderLayout layout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel telemetry = (JPanel) layout.getLayoutComponent(BorderLayout.CENTER);
        // Value labels are at odd indices: 1, 3, 5, 7, 9, 11
        for (int i = 1; i < telemetry.getComponentCount(); i += 2) {
            JLabel valueLabel = (JLabel) telemetry.getComponent(i);
            assertEquals("—", valueLabel.getText(),
                    "Value label at index " + i + " should be initialised to '—'");
        }
    }

    // --- Anomaly table columns ---

    @Test
    void testAnomalyTableColumns() {
        BorderLayout outerLayout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel south = (JPanel) outerLayout.getLayoutComponent(BorderLayout.SOUTH);

        // Drill through: JPanel → JScrollPane → JViewport → JTable
        JScrollPane scroll = (JScrollPane) south.getComponent(0);
        JTable table = (JTable) scroll.getViewport().getView();

        assertEquals(4, table.getColumnCount());
        assertEquals("Drone ID",  table.getColumnName(0));
        assertEquals("Type",      table.getColumnName(1));
        assertEquals("Details",   table.getColumnName(2));
        assertEquals("Timestamp", table.getColumnName(3));
    }

    @Test
    void testAnomalyTableStartsEmpty() {
        BorderLayout outerLayout = (BorderLayout) dashboard.getContentPane().getLayout();
        JPanel south = (JPanel) outerLayout.getLayoutComponent(BorderLayout.SOUTH);
        JScrollPane scroll = (JScrollPane) south.getComponent(0);
        JTable table = (JTable) scroll.getViewport().getView();
        assertEquals(0, table.getRowCount());
    }
}