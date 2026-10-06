package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.SupportTicket;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.SupportTicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    // READ - User views own support tickets
    @GetMapping("/user/support")
    public String myTickets(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("tickets", supportTicketService.getTicketsByUser(user.getUserId()));
        return "support/my-tickets";
    }

    // CREATE - Submit support request page
    @GetMapping("/user/support/submit")
    public String submitPage() {
        return "support/submit-ticket";
    }

    // CREATE - Submit support request
    @PostMapping("/user/support/submit")
    public String submitTicket(@RequestParam String subject,
                               @RequestParam String message,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = supportTicketService.submitTicket(user.getUserId(), user.getName(), subject, message);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/user/support/submit";
        }
        redirectAttributes.addFlashAttribute("success", "Support request submitted.");
        return "redirect:/user/support";
    }

    // UPDATE - Driver closes their own resolved ticket
    @PostMapping("/user/support/close/{ticketId}")
    public String closeTicket(@PathVariable String ticketId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = supportTicketService.closeTicket(ticketId, user.getUserId());
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Support ticket closed.");
        }
        return "redirect:/user/support";
    }

    // READ - Admin views all support tickets
    @GetMapping("/admin/support")
    public String allTickets(Model model) {
        model.addAttribute("tickets", supportTicketService.getAllTickets());
        return "support/admin-ticket-list";
    }

    // UPDATE - Admin resolve ticket page; only OPEN tickets are resolvable
    @GetMapping("/admin/support/resolve/{ticketId}")
    public String resolvePage(@PathVariable String ticketId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        SupportTicket ticket = supportTicketService.getTicketById(ticketId);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("error", "Support ticket not found.");
            return "redirect:/admin/support";
        }
        if (!"OPEN".equals(ticket.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only open support tickets can be resolved.");
            return "redirect:/admin/support";
        }
        model.addAttribute("ticket", ticket);
        return "support/resolve-ticket";
    }

    // UPDATE - Admin resolve ticket submit
    @PostMapping("/admin/support/resolve")
    public String resolveTicket(@RequestParam String ticketId,
                                @RequestParam String adminResponse,
                                RedirectAttributes redirectAttributes) {
        String error = supportTicketService.resolveTicket(ticketId, adminResponse);
        if (error != null) {
            // Send the admin back to the form with their text preserved.
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("adminResponse", adminResponse);
            return "redirect:/admin/support/resolve/" + ticketId;
        }
        redirectAttributes.addFlashAttribute("success", "Support ticket resolved.");
        return "redirect:/admin/support";
    }

    // DELETE - Admin deletes a ticket
    @PostMapping("/admin/support/delete/{ticketId}")
    public String deleteTicket(@PathVariable String ticketId,
                               RedirectAttributes redirectAttributes) {
        String error = supportTicketService.deleteTicket(ticketId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Support ticket deleted.");
        }
        return "redirect:/admin/support";
    }
}
