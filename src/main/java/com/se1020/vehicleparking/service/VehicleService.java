package com.se1020.vehicleparking.service;

import com.se1020.vehicleparking.model.Vehicle;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Validation helper methods. These mirror the form rules so the server enforces them too.
    private static final java.util.regex.Pattern PLATE_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 -]{1,14}$");

    private boolean isValidPlateNumber(String plate) {
        return plate != null && PLATE_PATTERN.matcher(plate.trim()).matches();
    }

    private boolean isValidText(String value, int maxLength) {
        return value != null && !value.trim().isEmpty() && value.trim().length() <= maxLength;
    }

    private boolean isValidType(String type) {
        return VehicleCategories.isValid(type);
    }

    /** The image URL is optional, but when given it must be a short http(s) link. */
    private boolean isValidVehicleUrl(String url) {
        if (url == null || url.trim().isEmpty()) return true;
        String u = url.trim().toLowerCase();
        return u.length() <= 255 && (u.startsWith("http://") || u.startsWith("https://"));
    }

    /** True when this owner already has a different vehicle with the same plate (case-insensitive). */
    private boolean isPlateTaken(String ownerId, String plate, String excludeVehicleId) {
        String wanted = plate.trim();
        for (Vehicle v : vehicleRepository.findByOwnerId(ownerId)) {
            if (v.getPlateNumber() != null
                    && v.getPlateNumber().trim().equalsIgnoreCase(wanted)
                    && (excludeVehicleId == null || !excludeVehicleId.equals(v.getVehicleId()))) {
                return true;
            }
        }
        return false;
    }

    /** Validates the vehicle fields. Returns an error message or null. */
    private String validateVehicleFields(String plateNumber, String make, String model,
                                         String color, String vehicleUrl, String type) {
        if (!isValidPlateNumber(plateNumber)) {
            return "Plate number must be 2 to 15 characters (letters, digits, spaces or hyphens).";
        }
        if (!isValidText(make, 50)) return "Make is required (maximum 50 characters).";
        if (!isValidText(model, 50)) return "Model is required (maximum 50 characters).";
        if (!isValidText(color, 30)) return "Color is required (maximum 30 characters).";
        if (!isValidVehicleUrl(vehicleUrl)) {
            return "Image URL must start with http:// or https:// (maximum 255 characters).";
        }
        if (!isValidType(type)) return "Please select a valid vehicle category.";
        return null;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getVehiclesByOwner(String ownerId) {
        return vehicleRepository.findByOwnerId(ownerId);
    }

    public Vehicle getVehicleById(String vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    /** Returns the selected vehicle only when it belongs to the specified user. */
    public Vehicle getOwnedVehicleByPlate(String plateNumber, String ownerId) {
        if (plateNumber == null || plateNumber.trim().isEmpty() || ownerId == null || ownerId.trim().isEmpty()) {
            return null;
        }
        String trimmedPlate = plateNumber.trim();
        for (Vehicle vehicle : vehicleRepository.findByOwnerId(ownerId)) {
            if (trimmedPlate.equals(vehicle.getPlateNumber())) {
                return vehicle;
            }
        }
        return null;
    }

    /** Used to verify server-side (not just via the UI's dropdown) that a plate really belongs to this user. */
    public boolean isVehicleOwnedByUser(String plateNumber, String ownerId) {
        return getOwnedVehicleByPlate(plateNumber, ownerId) != null;
    }

    /** Adds a vehicle. Returns an error message, or null when the vehicle was saved. */
    public String addVehicle(
        String ownerId,
        String plateNumber,
        String make,
        String model,
        String color,
        String vehicleUrl,
        String type
    ) {
        if (ownerId == null || ownerId.trim().isEmpty()) return "You must be signed in to add a vehicle.";
        String error = validateVehicleFields(plateNumber, make, model, color, vehicleUrl, type);
        if (error != null) return error;
        if (isPlateTaken(ownerId, plateNumber, null)) {
            return "You have already registered a vehicle with this plate number.";
        }

        String vehicleId = UUID.randomUUID().toString();
        Vehicle vehicle = new Vehicle(
            vehicleId,
            ownerId,
            plateNumber.trim(),
            make.trim(),
            model.trim(),
            color.trim(),
            vehicleUrl != null ? vehicleUrl.trim() : "",
            VehicleCategories.normalize(type)
        );
        vehicleRepository.save(vehicle);
        return null;
    }

    /** Updates a vehicle. Returns an error message, or null when the changes were saved. */
    public String updateVehicle(
        String vehicleId,
        String plateNumber,
        String make,
        String model,
        String color,
        String vehicleUrl,
        String type
    ) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) return "Vehicle not found.";
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) return "Vehicle not found.";

        String error = validateVehicleFields(plateNumber, make, model, color, vehicleUrl, type);
        if (error != null) return error;
        if (isPlateTaken(vehicle.getOwnerId(), plateNumber, vehicleId)) {
            return "This owner already has another vehicle with this plate number.";
        }

        vehicle.setPlateNumber(plateNumber.trim());
        vehicle.setMake(make.trim());
        vehicle.setModel(model.trim());
        vehicle.setColor(color.trim());
        vehicle.setVehicleUrl(vehicleUrl != null ? vehicleUrl.trim() : "");
        vehicle.setType(VehicleCategories.normalize(type));
        vehicleRepository.update(vehicle);
        return null;
    }

    /** Deletes a vehicle. Returns an error message, or null when it was deleted. */
    public String deleteVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) return "Vehicle not found.";
        if (vehicleRepository.findById(vehicleId) == null) return "Vehicle not found.";
        vehicleRepository.delete(vehicleId);
        return null;
    }
}
