package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null) {
            return "redirect:" + user.getDashboardPath();
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAllAttributes(dashboardService.getAdminDashboardData());
        return "dashboard/admin-dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAllAttributes(dashboardService.getUserDashboardData(user.getUserId()));
        return "dashboard/user-dashboard";
    }
}
