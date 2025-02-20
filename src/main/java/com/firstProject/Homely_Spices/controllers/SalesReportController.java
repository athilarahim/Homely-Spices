package com.firstProject.Homely_Spices.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.repo.OrderItemsRepo;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import com.lowagie.text.Font;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/report")
public class SalesReportController {

    @Autowired
    private OrderRepo ordersRepository;

    @Autowired
    private OrderItemsRepo orderItemsRepository;

    @Autowired
    private WalletRepo walletRepo;


    @GetMapping
    public String showSalesReport(@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  @RequestParam(required = false) String dateRange,
                                  Model model) throws JsonProcessingException {

        // Default Date Range (Last 30 Days)
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<Orders> orders = ordersRepository.findOrdersBetweenDates(startDate, endDate);

        // Compute Metrics
        double totalRevenue = orders.stream().mapToDouble(Orders::getTotalAmount).sum();
        int totalOrders = orders.size();
        Integer totalProductsSold = orderItemsRepository.getTotalProductsSold(startDate, endDate);

        double couponDeductions = orders.stream()
                .mapToDouble(order -> order.getCouponDeduction() != null ? order.getCouponDeduction() : 0.0)
                .sum();

        double deliveryFee = orders.stream().mapToDouble(Orders::getDeliveryFee).sum();

        List<Object[]> results = walletRepo.findDailyCreditsWithinRange(startDate, endDate);

        double totalRefunds = results.stream()
                .mapToDouble(entry -> (Double) entry[1]) // Sum up total credits
                .sum();

        // Group by Date for Chart Representation
        Map<LocalDate, Double> salesData = orders.stream()
                .collect(Collectors.groupingBy(
                        Orders::getOrderDate,  // Extract Date from orderDate
                        TreeMap::new,  // Keep it sorted
                        Collectors.summingDouble(Orders::getTotalAmount)
                ));


        List<Double> values = new ArrayList<>(salesData.values());


        model.addAttribute("currentPage", "report");
        model.addAttribute("pageTitle", "Sales Report");
        model.addAttribute("adminName", "Admin Name");


        model.addAttribute("orders", orders);
        model.addAttribute("couponDeductions",couponDeductions);
        model.addAttribute("totalRevenue", totalRevenue-couponDeductions+deliveryFee-totalRefunds);
        model.addAttribute("totalRefunds",totalRefunds);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProductsSold", totalProductsSold!=null?totalProductsSold:0);
        model.addAttribute("salesData", salesData);
        model.addAttribute("dateRange", dateRange);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("chartValues", values);

        return "salesReport";
    }

    @GetMapping("/download/pdf")
    public void downloadPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.pdf");

        // Fetching order data
        List<Orders> orders = ordersRepository.findOrdersBetweenDates(LocalDate.now().minusDays(30), LocalDate.now());
        Map<LocalDate, Double> salesData = orders.stream()
                .collect(Collectors.groupingBy(Orders::getOrderDate, TreeMap::new, Collectors.summingDouble(Orders::getTotalAmount)));

        // Creating PDF document
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Adding Title
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Paragraph title = new Paragraph("Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Adding generated date
        Font dateFont = new Font(Font.HELVETICA, 10);
        Paragraph date = new Paragraph("Generated on: " + LocalDate.now(), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        // Adding Space
        document.add(new Paragraph("\n"));

        // Creating table with 2 columns
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{1, 2});

        // Adding Table Headers
        PdfPCell header1 = new PdfPCell(new Phrase("Date", new Font(Font.HELVETICA, 12, Font.BOLD)));
        PdfPCell header2 = new PdfPCell(new Phrase("Total Sales (₹)", new Font(Font.HELVETICA, 12, Font.BOLD)));

        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(header1);
        table.addCell(header2);

        // Adding Data Rows
        for (Map.Entry<LocalDate, Double> entry : salesData.entrySet()) {
            PdfPCell cellDate = new PdfPCell(new Phrase(entry.getKey().toString(), new Font(Font.HELVETICA, 12)));
            PdfPCell cellAmount = new PdfPCell(new Phrase(String.format("₹ %.2f", entry.getValue()), new Font(Font.HELVETICA, 12)));

            cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellAmount.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(cellDate);
            table.addCell(cellAmount);
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/download/excel")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.xlsx");

        List<Orders> orders = ordersRepository.findOrdersBetweenDates(LocalDate.now().minusDays(30), LocalDate.now());
        Map<LocalDate, Double> salesData = orders.stream()
                .collect(Collectors.groupingBy(Orders::getOrderDate, TreeMap::new, Collectors.summingDouble(Orders::getTotalAmount)));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales Report");

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("Total Sales (₹)");

        int rowIdx = 1;
        for (Map.Entry<LocalDate, Double> entry : salesData.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey().toString());
            row.createCell(1).setCellValue(entry.getValue());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
