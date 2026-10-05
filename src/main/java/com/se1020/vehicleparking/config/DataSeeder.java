package com.se1020.vehicleparking.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.model.Feedback;
import com.se1020.vehicleparking.model.Notification;
import com.se1020.vehicleparking.model.RefundRequest;
import com.se1020.vehicleparking.model.ParkingFacility;
import com.se1020.vehicleparking.model.ParkingSlot;
import com.se1020.vehicleparking.model.ParkingZone;
import com.se1020.vehicleparking.model.Reservation;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.UserRoles;
import com.se1020.vehicleparking.model.Vehicle;
import com.se1020.vehicleparking.model.VehicleCategories;
import com.se1020.vehicleparking.repository.jpa.BillJpaRepository;
import com.se1020.vehicleparking.repository.jpa.FeedbackJpaRepository;
import com.se1020.vehicleparking.repository.jpa.NotificationJpaRepository;
import com.se1020.vehicleparking.repository.jpa.RefundRequestJpaRepository;
import com.se1020.vehicleparking.repository.jpa.ParkingFacilityJpaRepository;
import com.se1020.vehicleparking.repository.jpa.ParkingSlotJpaRepository;
import com.se1020.vehicleparking.repository.jpa.ParkingZoneJpaRepository;
import com.se1020.vehicleparking.repository.jpa.ReservationJpaRepository;
import com.se1020.vehicleparking.repository.jpa.UserJpaRepository;
import com.se1020.vehicleparking.repository.jpa.VehicleJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One-time import of the project's original JSON sample data into SQL Server.
 *
 * This mirrors the exact source-of-truth rules the old {@code JsonDataAccess} used, so that
 * anyone who was already running the JSON-file version of the app (and has data saved under
 * {@code ~/.online-vehicleparking/data/}) does not lose it when switching to SQL Server:
 *
 *  - For every entity EXCEPT users: if a valid external file exists at
 *    {@code ~/.online-vehicleparking/data/<file>.json}, it is used as-is (even if it is an
 *    empty list, exactly like {@code JsonDataAccess.readList} did). Otherwise the bundled
 *    classpath file under {@code src/main/resources/data/} is used.
 *  - For users specifically: the old {@code UserRepository.findAll()} merged the bundled
 *    classpath list with the external overlay (external wins per userId, but seeded/bundled
 *    accounts are never lost even if the external file was partial/truncated). That exact
 *    merge is reproduced here.
 *  - Each SQL table is only seeded when it is still completely empty, so this always runs
 *    safely on every startup and never overwrites or duplicates data already in SQL Server.
 *  - The JSON files (bundled and external) are only ever read here, never modified or deleted.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ObjectMapper mapper = new ObjectMapper();

    private final UserJpaRepository userRepo;
    private final VehicleJpaRepository vehicleRepo;
    private final ParkingFacilityJpaRepository facilityRepo;
    private final ParkingZoneJpaRepository zoneRepo;
    private final ParkingSlotJpaRepository slotRepo;
    private final ReservationJpaRepository reservationRepo;
    private final BillJpaRepository billRepo;
    private final RefundRequestJpaRepository refundRequestRepo;
    private final NotificationJpaRepository notificationRepo;
    private final FeedbackJpaRepository feedbackRepo;

    public DataSeeder(UserJpaRepository userRepo, VehicleJpaRepository vehicleRepo,
                       ParkingFacilityJpaRepository facilityRepo, ParkingZoneJpaRepository zoneRepo,
                       ParkingSlotJpaRepository slotRepo, ReservationJpaRepository reservationRepo,
                       BillJpaRepository billRepo, RefundRequestJpaRepository refundRequestRepo,
                       NotificationJpaRepository notificationRepo, FeedbackJpaRepository feedbackRepo) {
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
        this.facilityRepo = facilityRepo;
        this.zoneRepo = zoneRepo;
        this.slotRepo = slotRepo;
        this.reservationRepo = reservationRepo;
        this.billRepo = billRepo;
        this.refundRequestRepo = refundRequestRepo;
        this.notificationRepo = notificationRepo;
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            List<User> users = loadSeedUsers();
            userRepo.saveAll(users);
            System.out.println("DataSeeder: imported " + users.size() + " user(s)");
        }
        backfillUserAccountFields();
        if (slotRepo.count() == 0) {
            List<ParkingSlot> slots = loadPreferringExternal("data/slots.json", "slots.json",
                    new TypeReference<List<ParkingSlot>>() {});
            slotRepo.saveAll(slots);
            System.out.println("DataSeeder: imported " + slots.size() + " parking slot(s)");
        }
        backfillParkingStructure();
        if (vehicleRepo.count() == 0) {
            List<Vehicle> vehicles = loadPreferringExternal("data/vehicles.json", "vehicles.json",
                    new TypeReference<List<Vehicle>>() {});
            vehicleRepo.saveAll(vehicles);
            System.out.println("DataSeeder: imported " + vehicles.size() + " vehicle(s)");
        }
        backfillVehicleCategories();
        if (reservationRepo.count() == 0) {
            List<Reservation> reservations = loadPreferringExternal("data/reservations.json", "reservations.json",
                    new TypeReference<List<Reservation>>() {});
            reservationRepo.saveAll(reservations);
            System.out.println("DataSeeder: imported " + reservations.size() + " reservation(s)");
        }
        if (billRepo.count() == 0) {
            List<Bill> bills = loadPreferringExternal("data/bills.json", "bills.json",
                    new TypeReference<List<Bill>>() {});
            billRepo.saveAll(bills);
            System.out.println("DataSeeder: imported " + bills.size() + " bill(s)");
        }
        backfillCleanBillIds();
        if (feedbackRepo.count() == 0) {
            List<Feedback> feedbacks = loadPreferringExternal("data/feedbacks.json", "feedbacks.json",
                    new TypeReference<List<Feedback>>() {});
            feedbackRepo.saveAll(feedbacks);
            System.out.println("DataSeeder: imported " + feedbacks.size() + " feedback(s)");
        }
    }

    /**
     * Converts legacy UUID-style bill primary keys to short, readable bill IDs (B001, B002, ...).
     * Existing clean B-number IDs are preserved. Refund references and notification links are
     * updated before the old bill row is removed so payment/refund history remains usable.
     * This is idempotent and becomes a no-op once every bill has a clean ID.
     */
    private void backfillCleanBillIds() {
        List<Bill> allBills = new ArrayList<>(billRepo.findAll());
        if (allBills.isEmpty()) return;

        Set<String> used = new HashSet<>();
        int max = 0;
        for (Bill bill : allBills) {
            String id = bill.getBillId();
            if (id != null && id.matches("B\\d+")) {
                used.add(id);
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        List<Bill> legacyBills = allBills.stream()
                .filter(b -> b.getBillId() == null || !b.getBillId().matches("B\\d+"))
                .sorted(Comparator
                        .comparing(Bill::getCreatedDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Bill::getBillId, Comparator.nullsLast(String::compareTo)))
                .toList();

        int migrated = 0;
        for (Bill oldBill : legacyBills) {
            String oldId = oldBill.getBillId();
            String newId;
            do {
                newId = String.format("B%03d", ++max);
            } while (used.contains(newId));
            used.add(newId);

            Bill clean = copyBillWithId(oldBill, newId);
            billRepo.save(clean);

            if (oldId != null) {
                for (RefundRequest refund : refundRequestRepo.findByBillIdOrderByCreatedAtDesc(oldId)) {
                    refund.setBillId(newId);
                    refundRequestRepo.save(refund);
                }
                for (Notification notification : notificationRepo.findAll()) {
                    String link = notification.getLink();
                    if (link != null && link.contains(oldId)) {
                        notification.setLink(link.replace(oldId, newId));
                        notificationRepo.save(notification);
                    }
                }
                billRepo.deleteById(oldId);
            }
            migrated++;
        }

        if (migrated > 0) {
            System.out.println("DataSeeder: converted " + migrated + " legacy bill ID(s) to clean B-number IDs");
        }
    }

    private Bill copyBillWithId(Bill source, String newId) {
        Bill copy = new Bill(newId, source.getReservationId(), source.getUserId(), source.getSlotNumber(),
                source.getVehiclePlate(), source.getDurationHours(), source.getRatePerHour(),
                source.getTotalAmount(), source.getDiscount(), source.getFinalAmount(),
                source.getStatus(), source.getCreatedDate());
        copy.setSessionId(source.getSessionId());
        copy.setPaymentReference(source.getPaymentReference());
        copy.setPaymentMethod(source.getPaymentMethod());
        copy.setCardType(source.getCardType());
        copy.setCardLast4(source.getCardLast4());
        copy.setPaidAt(source.getPaidAt());
        copy.setVoidedAt(source.getVoidedAt());
        copy.setVoidReason(source.getVoidReason());
        return copy;
    }

    /**
     * Phase 4 migration for existing databases. Older versions had only parking_slots and also
     * mixed two different meanings into ParkingSlot.type (CAR/BIKE/VAN in seed data versus
     * NORMAL/DISABLED/PREMIUM in the old admin form). This migration is idempotent:
     *  - creates one default facility when no facility structure exists yet;
     *  - creates a zone for each legacy floor and assigns legacy slots to it;
     *  - keeps type consistently as the allowed vehicle category;
     *  - migrates legacy CAR/BIKE/VAN values to the expanded vehicle-category model;
     *  - moves legacy NORMAL/DISABLED/PREMIUM values into spaceCategory;
     *  - defaults missing categories to STANDARD.
     * Existing valid Phase 4 assignments are never overwritten.
     */
    private void backfillParkingStructure() {
        List<ParkingSlot> slots = slotRepo.findAll();

        ParkingFacility defaultFacility;
        if (facilityRepo.count() == 0) {
            defaultFacility = new ParkingFacility(
                    "FAC-DEFAULT",
                    "Main Parking Facility",
                    "Main parking location",
                    "Migrated default facility for existing parking spaces",
                    "ACTIVE");
            facilityRepo.save(defaultFacility);
            System.out.println("DataSeeder: created default parking facility");
        } else {
            defaultFacility = facilityRepo.findAll().get(0);
        }

        Map<String, ParkingZone> zonesByFloor = new HashMap<>();
        for (ParkingZone zone : zoneRepo.findByFacilityId(defaultFacility.getFacilityId())) {
            zonesByFloor.put(zone.getFloor() == null ? "" : zone.getFloor().trim().toLowerCase(), zone);
        }

        int updated = 0;
        for (ParkingSlot slot : slots) {
            boolean changed = false;

            String oldType = slot.getType() == null ? "" : slot.getType().trim().toUpperCase();
            if (List.of("NORMAL", "DISABLED", "PREMIUM").contains(oldType)) {
                slot.setSpaceCategory("DISABLED".equals(oldType) ? "ACCESSIBLE" :
                        ("PREMIUM".equals(oldType) ? "PREMIUM" : "STANDARD"));
                oldType = "CAR";
                changed = true;
            }

            String migratedType = VehicleCategories.migrateLegacy(oldType);
            if (migratedType.isBlank()) {
                migratedType = VehicleCategories.MOTOR_CAR;
            }
            if (!migratedType.equals(slot.getType())) {
                slot.setType(migratedType);
                changed = true;
            }

            if (slot.getSpaceCategory() == null || slot.getSpaceCategory().isBlank()) {
                slot.setSpaceCategory("STANDARD");
                changed = true;
            }

            if (slot.getFacilityId() == null || slot.getFacilityId().isBlank()
                    || slot.getZoneId() == null || slot.getZoneId().isBlank()) {
                String floor = slot.getFloor() == null || slot.getFloor().isBlank() ? "Ground" : slot.getFloor().trim();
                String floorKey = floor.toLowerCase();
                ParkingZone zone = zonesByFloor.get(floorKey);
                if (zone == null) {
                    zone = new ParkingZone(
                            java.util.UUID.randomUUID().toString(),
                            defaultFacility.getFacilityId(),
                            floor + " Zone",
                            floor,
                            "Migrated zone for existing parking spaces",
                            "ACTIVE");
                    zoneRepo.save(zone);
                    zonesByFloor.put(floorKey, zone);
                }
                slot.setFacilityId(defaultFacility.getFacilityId());
                slot.setZoneId(zone.getZoneId());
                slot.setFloor(zone.getFloor());
                changed = true;
            }

            if (changed) {
                slotRepo.save(slot);
                updated++;
            }
        }

        if (updated > 0) {
            System.out.println("DataSeeder: backfilled Phase 4 facility/zone/category fields on " + updated + " parking slot(s)");
        }
    }


    /**
     * Migrates existing SQL Server vehicle rows from the original broad project types to the
     * expanded vehicle categories. This is idempotent and preserves all other vehicle fields.
     * Legacy mapping: BIKE -> MOTOR_CYCLE, CAR -> MOTOR_CAR, VAN -> DUAL_PURPOSE_VEHICLE.
     * Existing values already using a current category are left unchanged.
     */
    private void backfillVehicleCategories() {
        List<Vehicle> vehicles = vehicleRepo.findAll();
        int updated = 0;
        for (Vehicle vehicle : vehicles) {
            String migrated = VehicleCategories.migrateLegacy(vehicle.getType());
            if (migrated.isBlank()) {
                // Do not guess unknown custom values; leave them visible for manual correction.
                continue;
            }
            if (!migrated.equals(vehicle.getType())) {
                vehicle.setType(migrated);
                vehicleRepo.save(vehicle);
                updated++;
            }
        }
        if (updated > 0) {
            System.out.println("DataSeeder: migrated " + updated + " vehicle(s) to the expanded vehicle categories");
        }
    }

    /**
     * Reproduces the old UserRepository.findAll() merge: bundled classpath users are the base,
     * and a valid non-empty external users.json overlays on top (external wins per userId).
     * If the external file is missing, unreadable, or an empty list, bundled classpath alone is used.
     */
    private List<User> loadSeedUsers() {
        List<User> classpathUsers = readClasspathList("data/users.json", new TypeReference<List<User>>() {});
        List<User> externalUsers = readExternalListOrNull("users.json", new TypeReference<List<User>>() {});

        if (externalUsers == null || externalUsers.isEmpty()) {
            return classpathUsers;
        }

        Map<String, User> byId = new LinkedHashMap<>();
        for (User u : classpathUsers) {
            if (u.getUserId() != null) {
                byId.put(u.getUserId(), u);
            }
        }
        for (User u : externalUsers) {
            if (u.getUserId() != null) {
                byId.put(u.getUserId(), u);
            }
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * Phase 2 migration: every user row created before status/stakeholderRole existed will have
     * been loaded with status=null / stakeholderRole=null (new columns on existing rows). This
     * safely backfills them once, in place, without touching anything else on the row:
     *  - status null -> "ACTIVE" (nobody is locked out by this migration)
     *  - stakeholderRole null/blank -> mapped from the existing legacy role: ADMIN -> ADMINISTRATOR,
     *    USER -> DRIVER (the proposal's default "kind" for each existing account type)
     * Runs on every startup but is a no-op once every row has already been backfilled, so it is
     * safe to leave in place permanently.
     */
    private void backfillUserAccountFields() {
        List<User> all = userRepo.findAll();
        int updated = 0;
        for (User user : all) {
            boolean changed = false;
            if (user.getStatus() == null || user.getStatus().isBlank()) {
                user.setStatus("ACTIVE");
                changed = true;
            }
            if (user.getStakeholderRole() == null || user.getStakeholderRole().isBlank()) {
                user.setStakeholderRole("ADMIN".equals(user.getRole()) ? UserRoles.ADMINISTRATOR : UserRoles.DRIVER);
                changed = true;
            }
            if (changed) {
                userRepo.save(user);
                updated++;
            }
        }
        if (updated > 0) {
            System.out.println("DataSeeder: backfilled status/stakeholderRole on " + updated + " existing user(s)");
        }
    }

    /**
     * For all non-User entities: if a valid external JSON file exists under
     * ~/.online-vehicleparking/data/, use it as-is (matches JsonDataAccess.readList's
     * "external file present -> external wins entirely" behavior). Otherwise fall back
     * to the bundled classpath file.
     */
    private <T> List<T> loadPreferringExternal(String classpathResource, String fileName, TypeReference<List<T>> type) {
        List<T> external = readExternalListOrNull(fileName, type);
        if (external != null) {
            return external;
        }
        return readClasspathList(classpathResource, type);
    }

    /**
     * Returns the parsed external list, or {@code null} if the external file does not exist
     * or fails to parse (invalid JSON), so the caller can fall back to classpath data.
     * An external file that exists and parses to an empty list is still considered "valid"
     * and is returned as-is (an empty list), matching the original JsonDataAccess semantics.
     */
    private <T> List<T> readExternalListOrNull(String fileName, TypeReference<List<T>> type) {
        Path ext = Path.of(System.getProperty("user.home"), ".online-vehicleparking", "data", fileName);
        if (!Files.exists(ext)) {
            return null;
        }
        try {
            List<T> list = mapper.readValue(ext.toFile(), type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("DataSeeder: external file " + ext + " is invalid, falling back to bundled data: " + e.getMessage());
            return null;
        }
    }

    private <T> List<T> readClasspathList(String classpathResource, TypeReference<List<T>> type) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                return new ArrayList<>();
            }
            List<T> list = mapper.readValue(in, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("DataSeeder: could not read bundled " + classpathResource + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
