package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.BillService;
import com.se1020.vehicleparking.service.RefundRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RefundRequestController {

    @Autowired private RefundRequestService refundRequestService;
    @Autowired private BillService billService;

    @GetMapping("/user/refunds")
    public String myRefunds(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        model.addAttribute("refunds", refundRequestService.getRefundsByUser(user.getUserId()));
        return "bill/my-refunds";
    }

    @GetMapping("/user/refunds/request/{billId}")
    public String requestPage(@PathVariable String billId, HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Bill bill = billService.getOwnBill(billId, user.getUserId());
        if (bill == null || !"PAID".equals(bill.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This bill is not eligible for a refund request.");
            return "redirect:/user/bills";
        }
        if (billService.hasOpenRefund(billId)) {
            redirectAttributes.addFlashAttribute("error", "A pending or approved refund already exists for this bill.");
            return "redirect:/user/refunds";
        }
        model.addAttribute("bill", bill);
        return "bill/refund-request";
    }

    @PostMapping("/user/refunds/request/{billId}")
    public String submitRefund(@PathVariable String billId, @RequestParam String reason,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = refundRequestService.requestRefund(billId, user.getUserId(), reason);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/refunds/request/" + billId;
        }
        redirectAttributes.addFlashAttribute("success", "Refund request submitted for finance review.");
        return "redirect:/user/refunds";
    }

    @GetMapping("/admin/refunds")
    public String allRefunds(Model model) {
        model.addAttribute("refunds", refundRequestService.getAllRefunds());
        return "bill/admin-refund-list";
    }

    @PostMapping("/admin/refunds/{refundId}/process")
    public String processRefund(@PathVariable String refundId,
                                @RequestParam String decision,
                                @RequestParam(required = false) String adminNote,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = refundRequestService.processRefund(refundId, decision, adminNote, user.getUserId());
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Refund request updated to " + decision + ".");
        return "redirect:/admin/refunds";
    }
}
