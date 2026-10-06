package com.se1020.vehicleparking.config;

import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalViewAdvice {
    private final NotificationService notificationService;

    public GlobalViewAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addCommonModelAttributes(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedUser");
        if (user != null) {
            model.addAttribute("unreadNotificationCount", notificationService.getUnreadCount(user.getUserId()));
        }
    }
}
