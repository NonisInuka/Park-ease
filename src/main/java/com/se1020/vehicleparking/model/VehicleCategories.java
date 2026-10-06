package com.se1020.vehicleparking.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central vehicle-category codes used by registered vehicles and parking-slot compatibility.
 * Database values stay compact/stable while UI labels remain human friendly.
 */
public final class VehicleCategories {

    public static final String MOTOR_CYCLE = "MOTOR_CYCLE";
    public static final String MOTOR_TRICYCLE = "MOTOR_TRICYCLE";
    public static final String MOTOR_CAR = "MOTOR_CAR";
    public static final String DUAL_PURPOSE_VEHICLE = "DUAL_PURPOSE_VEHICLE";
    public static final String MOTOR_LORRY = "MOTOR_LORRY";
    public static final String BUS = "BUS";
    public static final String LAND_VEHICLE_TRACTOR = "LAND_VEHICLE_TRACTOR";
    public static final String SPECIAL_PURPOSE_VEHICLE = "SPECIAL_PURPOSE_VEHICLE";

    public static final List<String> ALL = List.of(
            MOTOR_CYCLE,
            MOTOR_TRICYCLE,
            MOTOR_CAR,
            DUAL_PURPOSE_VEHICLE,
            MOTOR_LORRY,
            BUS,
            LAND_VEHICLE_TRACTOR,
            SPECIAL_PURPOSE_VEHICLE
    );

    private VehicleCategories() {}

    public static boolean isValid(String value) {
        return ALL.contains(normalize(value));
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return value.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_')
                .replaceAll("_+", "_");
    }

    /**
     * Converts legacy project values to the new vehicle-category codes.
     * This deliberately uses only information that existed in the old type field:
     * BIKE -> MOTOR_CYCLE, CAR -> MOTOR_CAR, VAN -> DUAL_PURPOSE_VEHICLE.
     */
    public static String migrateLegacy(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "BIKE", "MOTORBIKE", "MOTOR_BIKE", "MOTORCYCLE", "MOTOR_CYCLES" -> MOTOR_CYCLE;
            case "THREE_WHEELER", "THREEWHEELER", "TUK_TUK", "TUKTUK", "MOTOR_TRICYCLES" -> MOTOR_TRICYCLE;
            case "CAR", "CARS", "MOTOR_CARS" -> MOTOR_CAR;
            case "VAN", "VANS", "DUAL_PURPOSE", "DUAL_PURPOSE_VEHICLES" -> DUAL_PURPOSE_VEHICLE;
            case "LORRY", "TRUCK", "TRUCKS", "MOTOR_LORRIES" -> MOTOR_LORRY;
            case "BUSES" -> BUS;
            case "TRACTOR", "TRACTORS", "LAND_VEHICLE", "LAND_VEHICLES", "LAND_VEHICLES_&_TRACTORS" -> LAND_VEHICLE_TRACTOR;
            case "SPECIAL_PURPOSE", "SPECIAL_PURPOSE_VEHICLES" -> SPECIAL_PURPOSE_VEHICLE;
            default -> isValid(normalized) ? normalized : "";
        };
    }

    public static String displayName(String value) {
        String normalized = migrateLegacy(value);
        return switch (normalized) {
            case MOTOR_CYCLE -> "Motor Cycles";
            case MOTOR_TRICYCLE -> "Motor Tricycles";
            case MOTOR_CAR -> "Motor Cars";
            case DUAL_PURPOSE_VEHICLE -> "Dual Purpose Vehicles";
            case MOTOR_LORRY -> "Motor Lorries";
            case BUS -> "Buses";
            case LAND_VEHICLE_TRACTOR -> "Land Vehicles & Tractors";
            case SPECIAL_PURPOSE_VEHICLE -> "Special Purpose Vehicles";
            default -> value == null || value.isBlank() ? "Unknown" : value;
        };
    }

    public static Map<String, String> options() {
        Map<String, String> options = new LinkedHashMap<>();
        for (String code : ALL) {
            options.put(code, displayName(code));
        }
        return options;
    }
}
