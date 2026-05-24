package controller;

import model.AnomalyRecord;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Plays audio alerts for critical and warning anomalies.
 *
 * Uses java.awt.Toolkit.beep() which works on all platforms without
 * external dependencies. Critical anomalies trigger a rapid double-beep;
 * warnings trigger a single beep. Alerts are rate-limited to avoid
 * beep storms when many anomalies fire in the same cycle.
 */
public class AudioAlertManager {

    /** Minimum milliseconds between any two audio alerts. */
    private static final long ALERT_COOLDOWN_MS = 3000;

    private long lastCriticalAlert = 0;
    private long lastWarningAlert  = 0;
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "audio-alert");
            t.setDaemon(true);
            return t;
        });

    /**
     * Scans the anomaly list and plays appropriate audio alerts.
     * Critical anomalies take priority over warnings.
     *
     * @param anomalies detected this cycle
     */
    public void processAnomalies(List<AnomalyRecord> anomalies) {
        boolean hasCritical = anomalies.stream()
            .anyMatch(a -> "CRITICAL".equals(a.getSeverity()));
        boolean hasWarning  = anomalies.stream()
            .anyMatch(a -> "WARNING".equals(a.getSeverity()));

        long now = System.currentTimeMillis();

        if (hasCritical && (now - lastCriticalAlert) > ALERT_COOLDOWN_MS) {
            lastCriticalAlert = now;
            playCritical();
        } else if (hasWarning && (now - lastWarningAlert) > ALERT_COOLDOWN_MS) {
            lastWarningAlert = now;
            playWarning();
        }
    }

    /** Double-beep for critical alerts (battery/crash risk). */
    private void playCritical() {
        scheduler.execute(() -> {
            Toolkit.getDefaultToolkit().beep();
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
            Toolkit.getDefaultToolkit().beep();
        });
    }

    /** Single beep for warnings. */
    private void playWarning() {
        scheduler.execute(() -> Toolkit.getDefaultToolkit().beep());
    }

    /** Shuts down the scheduler gracefully on application exit. */
    public void shutdown() {
        scheduler.shutdown();
    }
}
