package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.Feedback;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/user/feedbacks")
    public String myFeedbacks(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("feedbacks", feedbackService.getFeedbacksByUser(user.getUserId()));
        return "feedback/my-feedbacks";
    }

    @GetMapping("/admin/feedbacks")
    public String allFeedbacks(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedbacks());
        return "feedback/admin-feedback-list";
    }

    @GetMapping("/user/feedbacks/submit")
    public String submitPage() {
        return "feedback/submit-feedback";
    }

    @PostMapping("/user/feedbacks/submit")
    public String submitFeedback(@RequestParam String type,
                                 @RequestParam String message,
                                 @RequestParam int rating,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = feedbackService.submitFeedback(user.getUserId(), user.getName(), type, message, rating);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("type", type);
            redirectAttributes.addFlashAttribute("message", message);
            redirectAttributes.addFlashAttribute("rating", rating);
            return "redirect:/user/feedbacks/submit";
        }
        redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully.");
        return "redirect:/user/feedbacks";
    }

    @GetMapping("/user/feedbacks/edit/{feedbackId}")
    public String editFeedbackPage(@PathVariable String feedbackId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Feedback feedback = feedbackService.getOwnFeedback(feedbackId, user.getUserId());
        if (feedback == null) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found.");
            return "redirect:/user/feedbacks";
        }
        if (!"PENDING".equals(feedback.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending feedback can be edited.");
            return "redirect:/user/feedbacks";
        }
        model.addAttribute("feedback", feedback);
        return "feedback/edit-feedback";
    }

    @PostMapping("/user/feedbacks/update")
    public String updateFeedback(@RequestParam String feedbackId,
                                 @RequestParam String type,
                                 @RequestParam String message,
                                 @RequestParam int rating,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = feedbackService.updateFeedback(feedbackId, user.getUserId(), type, message, rating);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Feedback updated successfully.");
        return "redirect:/user/feedbacks";
    }

    @GetMapping("/admin/feedbacks/resolve/{feedbackId}")
    public String resolvePage(@PathVariable String feedbackId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        if (feedback == null) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found.");
            return "redirect:/admin/feedbacks";
        }
        if (!"PENDING".equals(feedback.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Only pending feedback can be resolved.");
            return "redirect:/admin/feedbacks";
        }
        model.addAttribute("feedback", feedback);
        return "feedback/resolve-feedback";
    }

    @PostMapping("/admin/feedbacks/resolve")
    public String resolveFeedback(@RequestParam String feedbackId,
                                  @RequestParam String adminResponse,
                                  RedirectAttributes redirectAttributes) {
        String error = feedbackService.resolveFeedback(feedbackId, adminResponse);
        if (error != null) {
            // Send the admin back to the form with their text preserved.
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("adminResponse", adminResponse);
            return "redirect:/admin/feedbacks/resolve/" + feedbackId;
        }
        redirectAttributes.addFlashAttribute("success", "Feedback resolved successfully.");
        return "redirect:/admin/feedbacks";
    }

    @PostMapping("/admin/feedbacks/delete/{feedbackId}")
    public String deleteFeedback(@PathVariable String feedbackId,
                                 RedirectAttributes redirectAttributes) {
        String error = feedbackService.deleteFeedback(feedbackId);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Feedback deleted.");
        }
        return "redirect:/admin/feedbacks";
    }
}
