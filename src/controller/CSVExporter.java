package controller;

import model.AnomalyRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports a list of AnomalyRecords to a CSV file.
 *
 * Column order: DroneID, Type, Severity, Details, Timestamp
 */
public class CSVExporter {

    private static final String CSV_HEADER = "DroneID,Type,Severity,Details,Timestamp";

    /**
     * Writes the provided anomaly records to the given file path.
     *
     * @param records  the list of anomaly records to export
     * @param filePath destination file path (e.g. "anomaly_log.csv")
     * @throws IOException if the file cannot be written
     */
    public static void export(List<AnomalyRecord> records, String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (AnomalyRecord record : records) {
                writer.write(record.toCSVRow());
                writer.newLine();
            }
        }
        System.out.println("[CSV] Exported " + records.size() + " records to: " + file.getAbsolutePath());
    }
}
