package com.se1020.vehicleparking.model;

import java.util.List;
import java.util.Set;

/**
 * The existing "role" field on User (ADMIN / USER) is left completely untouched, because it is
 * used for authorization elsewhere in the codebase (VehicleController, HomeController) and to
 * pick the sidebar/dashboard in layout/base.html. Changing those values would break existing
 * access control, so it was NOT touched in Phase 2.
 *
 * Instead, "stakeholderRole" is a new, purely descriptive field that expands towards the
 * proposal's six stakeholders. Because AdminUser/RegularUser is a real JPA single-table
 * inheritance split (a different Java/DB "kind" of account, not just a label), a given account
 * can only be assigned a stakeholderRole from whichever tier matches its actual kind:
 *
 *  - AdminUser-kind accounts (role=ADMIN): ADMINISTRATOR, OPERATIONS_MANAGER, FINANCE_OFFICER, FACILITY_OWNER
 *  - RegularUser-kind accounts (role=USER): DRIVER, PARKING_STAFF
 *
 * The legacy dashboard split remains in place, while selected later features can apply finer-grained
 * stakeholder permissions. Parking operations, for example, are shared by PARKING_STAFF,
 * OPERATIONS_MANAGER and ADMINISTRATOR without changing the underlying ADMIN/USER account kind.
 */
public final class UserRoles {

    private UserRoles() {}

    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String OPERATIONS_MANAGER = "OPERATIONS_MANAGER";
    public static final String FINANCE_OFFICER = "FINANCE_OFFICER";
    public static final String FACILITY_OWNER = "FACILITY_OWNER";

    public static final String DRIVER = "DRIVER";
    public static final String PARKING_STAFF = "PARKING_STAFF";

    public static final List<String> ADMIN_TIER_ROLES =
            List.of(ADMINISTRATOR, OPERATIONS_MANAGER, FINANCE_OFFICER, FACILITY_OWNER);

    public static final List<String> USER_TIER_ROLES =
            List.of(DRIVER, PARKING_STAFF);

    private static final Set<String> ADMIN_TIER_SET = Set.copyOf(ADMIN_TIER_ROLES);
    private static final Set<String> USER_TIER_SET = Set.copyOf(USER_TIER_ROLES);

    public static boolean isValidAdminTierRole(String stakeholderRole) {
        return stakeholderRole != null && ADMIN_TIER_SET.contains(stakeholderRole);
    }

    public static boolean isValidUserTierRole(String stakeholderRole) {
        return stakeholderRole != null && USER_TIER_SET.contains(stakeholderRole);
    }

    /**
     * Parking operations are shared by the proposal's Parking Staff and Operations Manager,
     * while Administrator retains super-user access. The legacy ADMIN/USER role is still kept
     * intact for the rest of the application's navigation and authorization model.
     */
    public static boolean canManageParkingOperations(String legacyRole, String stakeholderRole) {
        if ("ADMIN".equalsIgnoreCase(legacyRole)) {
            return ADMINISTRATOR.equals(stakeholderRole) || OPERATIONS_MANAGER.equals(stakeholderRole);
        }
        return "USER".equalsIgnoreCase(legacyRole) && PARKING_STAFF.equals(stakeholderRole);
    }


    /** Facility/space administration belongs to facility owners, operations managers and administrators. */
    public static boolean canManageParkingFacilities(String legacyRole, String stakeholderRole) {
        return "ADMIN".equalsIgnoreCase(legacyRole)
                && (ADMINISTRATOR.equals(stakeholderRole)
                || OPERATIONS_MANAGER.equals(stakeholderRole)
                || FACILITY_OWNER.equals(stakeholderRole));
    }

    /** Only administrators/operations managers may remove an incorrect parking-session record. */
    public static boolean canCorrectParkingSessions(String legacyRole, String stakeholderRole) {
        return "ADMIN".equalsIgnoreCase(legacyRole)
                && (ADMINISTRATOR.equals(stakeholderRole) || OPERATIONS_MANAGER.equals(stakeholderRole));
    }

    /** Creating, editing and deleting accounts (including admin-tier accounts) is Administrator-only. */
    public static boolean canManageUsers(String legacyRole, String stakeholderRole) {
        return "ADMIN".equalsIgnoreCase(legacyRole) && ADMINISTRATOR.equals(stakeholderRole);
    }

    /** Financial workflows belong to Finance Officer and Operations Manager; Administrator retains super-user access. */
    public static boolean canManageFinancials(String legacyRole, String stakeholderRole) {
        return "ADMIN".equalsIgnoreCase(legacyRole)
                && (ADMINISTRATOR.equals(stakeholderRole)
                || OPERATIONS_MANAGER.equals(stakeholderRole)
                || FINANCE_OFFICER.equals(stakeholderRole));
    }

    /** Roles a caller may pick from for an account of the given legacy "role" (ADMIN/USER). */
    public static List<String> availableRolesFor(String legacyRole) {
        return "ADMIN".equalsIgnoreCase(legacyRole) ? ADMIN_TIER_ROLES : USER_TIER_ROLES;
    }
}
