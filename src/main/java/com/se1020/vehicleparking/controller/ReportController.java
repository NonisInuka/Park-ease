package com.se1020.vehicleparking.controller;

import com.se1020.vehicleparking.service.DashboardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    private final DashboardService dashboardService;

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          Model model) {
        model.addAllAttributes(dashboardService.getReportData(startDate, endDate));
        return "dashboard/reports";
    }

    @GetMapping(value = "/admin/reports/export/operations", produces = "text/csv")
    public ResponseEntity<String> exportOperations(@RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate) {
        return csvResponse("parking-operations-report.csv",
                dashboardService.buildOperationsCsv(startDate, endDate));
    }

    @GetMapping(value = "/admin/reports/export/financial", produces = "text/csv")
    public ResponseEntity<String> exportFinancial(@RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        return csvResponse("financial-report.csv",
                dashboardService.buildFinancialCsv(startDate, endDate));
    }

    private ResponseEntity<String> csvResponse(String filename, String content) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}
