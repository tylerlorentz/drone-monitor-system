package test.controller;

import controller.AnomalyDetector;
import model.AnomalyRecord;
import model.Drone;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class AnomalyDetectorTest {

    @Test
    public void testLowBatteryDetection() {

        Drone drone =
                new Drone(
                        "D1",
                        47.60,
                        -122.33,
                        50,
                        10,
                        0.2
                );

        List<Drone> drones =
                new ArrayList<>();

        drones.add(drone);

        AnomalyDetector detector =
                new AnomalyDetector();

        List<AnomalyRecord> anomalies =
                detector.detect(drones);

        boolean found = false;

        for (AnomalyRecord a : anomalies) {

            if (a.getType().equals("LOW_BATTERY")) {

                found = true;

                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testCrashRiskDetection() {

        Drone drone =
                new Drone(
                        "D2",
                        47.60,
                        -122.33,
                        1,
                        80,
                        0.2
                );

        List<Drone> drones =
                new ArrayList<>();

        drones.add(drone);

        AnomalyDetector detector =
                new AnomalyDetector();

        List<AnomalyRecord> anomalies =
                detector.detect(drones);

        boolean found = false;

        for (AnomalyRecord a : anomalies) {

            if (a.getType().equals("CRASH_RISK")) {

                found = true;

                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testGpsErrorDetection() {

        Drone drone =
                new Drone(
                        "D3",
                        200,
                        -122.33,
                        50,
                        80,
                        0.2
                );

        List<Drone> drones =
                new ArrayList<>();

        drones.add(drone);

        AnomalyDetector detector =
                new AnomalyDetector();

        List<AnomalyRecord> anomalies =
                detector.detect(drones);

        boolean found = false;

        for (AnomalyRecord a : anomalies) {

            if (a.getType().equals("GPS_ERROR")) {

                found = true;

                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testHighVelocityDetection() {

        Drone drone =
                new Drone(
                        "D4",
                        47.60,
                        -122.33,
                        50,
                        80,
                        1.2
                );

        List<Drone> drones =
                new ArrayList<>();

        drones.add(drone);

        AnomalyDetector detector =
                new AnomalyDetector();

        List<AnomalyRecord> anomalies =
                detector.detect(drones);

        boolean found = false;

        for (AnomalyRecord a : anomalies) {

            if (a.getType().equals("HIGH_VELOCITY")) {

                found = true;

                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testEmergencyRiskDetection() {

        Drone drone =
                new Drone(
                        "D5",
                        47.60,
                        -122.33,
                        2,
                        10,
                        0.2
                );

        List<Drone> drones =
                new ArrayList<>();

        drones.add(drone);

        AnomalyDetector detector =
                new AnomalyDetector();

        List<AnomalyRecord> anomalies =
                detector.detect(drones);

        boolean found = false;

        for (AnomalyRecord a : anomalies) {

            if (a.getType().equals("EMERGENCY_RISK")) {

                found = true;

                break;
            }
        }

        assertTrue(found);
    }
}