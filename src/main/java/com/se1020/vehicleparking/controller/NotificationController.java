package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("notifications", notificationService.getNotificationsForUser(user.getUserId()));
        return "notification/list";
    }

    @PostMapping("/notifications/read/{notificationId}")
    public String markRead(@PathVariable String notificationId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        if (!notificationService.markRead(notificationId, user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Notification not found.");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        notificationService.markAllRead(user.getUserId());
        return "redirect:/notifications";
    }
}
