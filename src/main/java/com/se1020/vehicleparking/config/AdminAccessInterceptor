package com.se1020.vehicleparking.config;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.model.UserRoles;
import com.se1020.vehicleparking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Central session-based authorization guard.
 *
 * The application intentionally keeps its existing HttpSession authentication model rather than
 * introducing Spring Security's authentication/filter chain. Every guarded request reloads the
 * current user from SQL Server so account status and role changes take effect immediately.
 *
 * /admin/**       -> legacy ADMIN tier
 * /user/**        -> legacy USER tier
 * /operations/**  -> ADMINISTRATOR / OPERATIONS_MANAGER / PARKING_STAFF
 * /admin/slots|facilities|zones -> ADMINISTRATOR / OPERATIONS_MANAGER / FACILITY_OWNER
 * /admin/users -> ADMINISTRATOR only
 */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String contextPath = request.getContextPath();
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);
        User sessionUser = session != null ? (User) session.getAttribute("loggedUser") : null;

        boolean adminPath = path.startsWith(contextPath + "/admin/");
        boolean userPath = path.startsWith(contextPath + "/user/");
        boolean operationsPath = path.startsWith(contextPath + "/operations/");
        boolean notificationPath = path.startsWith(contextPath + "/notifications");

        if (!adminPath && !userPath && !operationsPath && !notificationPath) {
            return true;
        }

        if (sessionUser == null) {
            boolean expired = request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();
            response.sendRedirect(contextPath + (expired ? "/login?expired=true" : "/login"));
            return false;
        }

        User current = userService.getUserById(sessionUser.getUserId());
        if (current == null || !current.isActive()) {
            session.invalidate();
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        // Keep the session snapshot in sync with the current SQL Server row before controllers run.
        session.setAttribute("loggedUser", current);

        if (notificationPath) {
            return true;
        }

        if (operationsPath) {
            if (!UserRoles.canManageParkingOperations(current.getRole(), current.getStakeholderRole())) {
                response.sendRedirect(contextPath + current.getDashboardPath());
                return false;
            }
            return true;
        }

        if (adminPath && (path.startsWith(contextPath + "/admin/slots")
                || path.startsWith(contextPath + "/admin/facilities")
                || path.startsWith(contextPath + "/admin/zones"))) {
            if (!UserRoles.canManageParkingFacilities(current.getRole(), current.getStakeholderRole())) {
                response.sendRedirect(contextPath + current.getDashboardPath());
                return false;
            }
        }

        if (adminPath && path.startsWith(contextPath + "/admin/users")) {
            if (!UserRoles.canManageUsers(current.getRole(), current.getStakeholderRole())) {
                response.sendRedirect(contextPath + current.getDashboardPath());
                return false;
            }
        }

        if (adminPath && (path.startsWith(contextPath + "/admin/bills")
                || path.startsWith(contextPath + "/admin/refunds"))) {
            if (!UserRoles.canManageFinancials(current.getRole(), current.getStakeholderRole())) {
                response.sendRedirect(contextPath + current.getDashboardPath());
                return false;
            }
        }

        String requiredRole = adminPath ? "ADMIN" : "USER";
        if (!requiredRole.equals(current.getRole())) {
            session.invalidate();
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        return true;
    }
}
