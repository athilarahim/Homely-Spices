package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.LedgerEntry;
import com.firstProject.Homely_Spices.model.OrderItems;
import com.firstProject.Homely_Spices.model.OrderStatus;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.OrderStatusRepo;
import com.firstProject.Homely_Spices.service.LedgerService;
import com.firstProject.Homely_Spices.service.OrderService;
import com.firstProject.Homely_Spices.service.OrderStatusService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;


import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    OrderStatusRepo orderStatusRepo;

    @Autowired
    OrderStatusService orderStatusService;

    @Autowired
    LedgerService ledgerService;

    @GetMapping("/view-orders")
    public String getUserOrders(Model model, Principal principal){
        List<Orders> orders = orderService.getOrders(principal.getName());
        model.addAttribute("orders",orders);
        return "viewOrders";
    }

    @GetMapping("/order-summary/{orderId}")
    public String getOrderSummary(@PathVariable int orderId, Model model, Principal principal){
        Orders order = orderService.getOrderSummary(orderId,principal.getName());
        model.addAttribute("order",order);
        return "orderSummary";
    }

    @PostMapping("/orders/{orderId}/update-status")
    public String updateOrderStatus(
            @PathVariable int orderId,
            @RequestParam String status) {
        orderStatusService.updateOrderStatus(orderId, status);

        return "redirect:/admin/orders";
    }

    // User views the order status
    @GetMapping("/orders/{orderId}")
    public String viewOrderStatus(
            @PathVariable int orderId,
            Model model) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        
        return "orderStatus";
    }

    @GetMapping("/return-order/{orderId}")
    public String returnOrder(@PathVariable int orderId,
                              Model model){

        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        return "returnOrder";
    }

    @GetMapping("/order/invoice/{orderId}")
    public void generateInvoice(@PathVariable int orderId, HttpServletResponse response) throws IOException, DocumentException {
        Orders order = orderRepo.findById(orderId).orElseThrow(()->new RuntimeException("Order doesn't found!"));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

        // Title
        Paragraph title = new Paragraph("Invoice", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Order Details
        document.add(new Paragraph("Order ID: " + order.getId(), boldFont));
        document.add(new Paragraph("Order Date: " + order.getOrderDate()));

        if(Objects.equals(order.getLatestStatus().getStatus(), "Payment Pending")){
            document.add(new Paragraph("Payment Method: Payment Pending"));
        }
        else{
            document.add(new Paragraph("Payment Method: " + order.getPayment().getPaymentMethod()));
        }
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Bill To: " + order.getUser().getName(), boldFont));
        document.add(new Paragraph("Email: " + order.getUser().getEmail()));
        document.add(new Paragraph("Phone: " + order.getUser().getPhone()));


        document.add(new Paragraph("\n"));
        // Product Table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1});

        table.addCell(new PdfPCell(new Phrase("Product", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Quantity", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Price", boldFont)));


        for (OrderItems item : order.getOrderItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getItemcount()));
            table.addCell("₹" + (item.getPrice() * item.getItemcount()));
        }

        document.add(table);


        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Delivery Address: " + order.getAddress().getHousename(), boldFont));
        document.add(new Paragraph("Street, City: " + order.getAddress().getStreet()+", "+order.getAddress().getCity()));
        document.add(new Paragraph("State, Country: " + order.getAddress().getState()+", "+order.getAddress().getCountry()));
        document.add(new Paragraph("Pin Code: " + order.getAddress().getZipcode()));
        document.add(new Paragraph("\n"));

        // Total Amount
        document.add(new Paragraph("Total: ₹" + order.getTotalAmount(), boldFont));
        document.add(new Paragraph("Delivery Charge: ₹" + order.getDeliveryFee()));
        document.add(new Paragraph("Coupon Discount: ₹" + order.getCouponDeduction()));
        document.add(new Paragraph("Final Amount: ₹" + (order.getTotalAmount() - order.getCouponDeduction() + order.getDeliveryFee()), boldFont));

        document.close();
    }

    //for changing the status after razorpay payment resume
    @PostMapping("/update-order-status/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable int orderId, @RequestBody Map<String, String> request) {
        Optional<Orders> optionalOrder = orderRepo.findById(orderId);

        if (optionalOrder.isPresent()) {
            Orders order = optionalOrder.get();

            OrderStatus latestStatus = new OrderStatus();
            latestStatus.setStatus(request.get("status"));
            latestStatus.setStatusChangeDate(LocalDateTime.now());
            latestStatus.setOrder(order);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(latestStatus);
            order.setOrderStatuses(orderStatuses);

            // Save the latest status first
            latestStatus = orderStatusRepo.save(latestStatus);

            // Now set the latest status in the order and save the order
            order.setLatestStatus(latestStatus);
            orderRepo.save(order);
            ledgerService.recordOrderTransaction(order);

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("success", false));
        }
    }

}
