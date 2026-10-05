package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.model.Bill;
import com.se1020.vehicleparking.model.RefundRequest;
import com.se1020.vehicleparking.model.User;
import com.se1020.vehicleparking.service.BillService;
import com.se1020.vehicleparking.service.RefundRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BillController {

    @Autowired private BillService billService;
    @Autowired private RefundRequestService refundRequestService;

    @GetMapping("/user/bills")
    public String myBills(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        List<Bill> bills = billService.getBillsByUser(user.getUserId());
        Map<String, RefundRequest> refundByBill = new HashMap<>();
        for (Bill bill : bills) {
            RefundRequest refund = refundRequestService.getLatestForBill(bill.getBillId());
            if (refund != null) refundByBill.put(bill.getBillId(), refund);
        }
        model.addAttribute("bills", bills);
        model.addAttribute("refundByBill", refundByBill);
        return "bill/my-bills";
    }

    @GetMapping("/admin/bills")
    public String allBills(Model model) {
        model.addAttribute("bills", billService.getAllBills());
        return "bill/admin-bill-list";
    }

    @PostMapping("/admin/bills/generate")
    public String generateBill(@RequestParam String reservationId, RedirectAttributes redirectAttributes) {
        String error = billService.generateBill(reservationId);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Bill generated successfully.");
        return "redirect:/admin/bills";
    }

    @GetMapping("/user/bills/{billId}")
    public String viewBill(@PathVariable String billId, HttpSession session, Model model,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Bill bill = billService.getOwnBill(billId, user.getUserId());
        if (bill == null) {
            redirectAttributes.addFlashAttribute("error", "Bill not found.");
            return "redirect:/user/bills";
        }
        model.addAttribute("bill", bill);
        model.addAttribute("refund", refundRequestService.getLatestForBill(billId));
        return "bill/bill-detail";
    }

    @GetMapping("/user/bills/pay/{billId}")
    public String paymentPage(@PathVariable String billId, HttpSession session, Model model,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Bill bill = billService.getOwnBill(billId, user.getUserId());
        if (bill == null || !"UNPAID".equals(bill.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This bill is not available for payment.");
            return "redirect:/user/bills";
        }
        model.addAttribute("bill", bill);
        return "bill/payment";
    }

    @PostMapping("/user/bills/pay/{billId}")
    public String payBill(@PathVariable String billId,
                          @RequestParam String cardholderName,
                          @RequestParam String cardNumber,
                          @RequestParam String expiryDate,
                          @RequestParam String cvv,
                          @RequestParam String cardType,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        String error = billService.payBill(billId, user.getUserId(), cardholderName, cardNumber, expiryDate, cvv, cardType);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/user/bills/pay/" + billId;
        }
        redirectAttributes.addFlashAttribute("success", "Payment completed successfully. Your receipt is now available.");
        return "redirect:/user/bills/" + billId;
    }

    @GetMapping("/user/bills/{billId}/receipt")
    public String receipt(@PathVariable String billId, HttpSession session, Model model,
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedUser");
        Bill bill = billService.getOwnBill(billId, user.getUserId());
        if (bill == null || (!"PAID".equals(bill.getStatus()) && !"REFUNDED".equals(bill.getStatus()))) {
            redirectAttributes.addFlashAttribute("error", "A receipt is available only for a completed payment.");
            return "redirect:/user/bills";
        }
        model.addAttribute("bill", bill);
        return "bill/receipt";
    }

    @GetMapping("/admin/bills/discount/{billId}")
    public String discountPage(@PathVariable String billId, Model model, RedirectAttributes redirectAttributes) {
        Bill bill = billService.getBillById(billId);
        if (bill == null || !"UNPAID".equals(bill.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Discounts can only be applied to unpaid bills.");
            return "redirect:/admin/bills";
        }
        model.addAttribute("bill", bill);
        return "bill/apply-discount";
    }

    @PostMapping("/admin/bills/discount")
    public String applyDiscount(@RequestParam String billId, @RequestParam double discount,
                                RedirectAttributes redirectAttributes) {
        String error = billService.applyDiscount(billId, discount);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Discount applied successfully.");
        return "redirect:/admin/bills";
    }

    @PostMapping("/admin/bills/void/{billId}")
    public String voidBill(@PathVariable String billId, @RequestParam String reason,
                           RedirectAttributes redirectAttributes) {
        String error = billService.voidBill(billId, reason);
        if (error != null) redirectAttributes.addFlashAttribute("error", error);
        else redirectAttributes.addFlashAttribute("success", "Incorrect bill was voided and retained for audit history.");
        return "redirect:/admin/bills";
    }
}
