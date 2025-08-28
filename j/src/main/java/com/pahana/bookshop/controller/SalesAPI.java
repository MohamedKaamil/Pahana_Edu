package com.pahana.bookshop.controller;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.dao.SalesDAO;
import com.pahana.bookshop.dao.ProductDAO;
import com.pahana.bookshop.model.Sale;
import com.pahana.bookshop.model.SaleItem;
import com.pahana.bookshop.model.Product;
import com.pahana.bookshop.model.User;
import com.pahana.bookshop.util.JsonUtil;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@WebServlet(name = "salesAPI", urlPatterns = {"/api/sales/*"})
public class SalesAPI extends HttpServlet {
    private SalesDAO salesDAO;
    private ProductDAO productDAO;

    public void init() {
        salesDAO = new SalesDAO();
        productDAO = new ProductDAO();
    }

    // GET /api/sales - Get all sales
    // GET /api/sales/{id} - Get sale by ID
    // GET /api/sales/status/{status} - Get sales by status
    // GET /api/sales/customer/{customerId} - Get sales by customer
    // GET /api/sales/analytics - Get sales analytics
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all sales
                List<Sale> sales = salesDAO.findAll();
                out.print(formatSalesListJson(sales));
            } else {
                handleGetByPath(pathInfo, response, out);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }
    
    private void handleGetByPath(String pathInfo, HttpServletResponse response, PrintWriter out) throws Exception {
        String[] pathParts = pathInfo.split("/");
        
        if (pathParts.length == 2) {
            String param = pathParts[1];
            
            if ("analytics".equals(param)) {
                // Get sales analytics
                BigDecimal totalSales = salesDAO.getTotalSalesAmount();
                int totalCount = salesDAO.getTotalSalesCount();
                
                out.print("{\"totalSalesAmount\": " + totalSales + 
                         ", \"totalSalesCount\": " + totalCount + "}");
            } else {
                try {
                    // Get sale by ID
                    int saleId = Integer.parseInt(param);
                    Sale sale = salesDAO.findById(saleId);
                    if (sale != null) {
                        out.print(saleToJson(sale));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(JsonUtil.createErrorJson("Sale not found"));
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("Invalid sale ID"));
                }
            }
        } else if (pathParts.length == 3) {
            String filterType = pathParts[1];
            String filterValue = pathParts[2];
            
            if ("status".equals(filterType)) {
                List<Sale> sales = salesDAO.findByStatus(filterValue);
                out.print(formatSalesListJson(sales));
            } else if ("customer".equals(filterType)) {
                List<Sale> sales = salesDAO.findByCustomer(filterValue);
                out.print(formatSalesListJson(sales));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Invalid filter type"));
            }
        }
    }

    // POST /api/sales - Create new sale (sell products)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Get current user from session
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(JsonUtil.createErrorJson("Authentication required"));
                return;
            }
            
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(JsonUtil.createErrorJson("User not found in session"));
                return;
            }
            
            Map<String, String> saleData = JsonUtil.parseSimpleJson(request.getReader());
            
            String customerId = saleData.get("customerId");
            String customerName = saleData.get("customerName");
            String itemsStr = saleData.get("items"); // Format: "productId1:quantity1,productId2:quantity2"
            String paymentMethod = saleData.get("paymentMethod");
            String discountStr = saleData.get("discount");
            String notes = saleData.get("notes");
            
            if (itemsStr == null || itemsStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.createErrorJson("items are required"));
                return;
            }
            
            // Parse items (format: "1:2,3:5" means productId 1 quantity 2, productId 3 quantity 5)
            String[] itemPairs = itemsStr.split(",");
            List<SaleItem> saleItems = new ArrayList<>();
            
            for (String itemPair : itemPairs) {
                String[] parts = itemPair.trim().split(":");
                if (parts.length != 2) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("Invalid items format. Use 'productId:quantity,productId:quantity'"));
                    return;
                }
                
                try {
                    int productId = Integer.parseInt(parts[0].trim());
                    int quantity = Integer.parseInt(parts[1].trim());
                    
                    if (quantity <= 0) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(JsonUtil.createErrorJson("Quantity must be greater than 0"));
                        return;
                    }
                    
                    // Get product details
                    Product product = productDAO.findById(productId);
                    if (product == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(JsonUtil.createErrorJson("Product not found: " + productId));
                        return;
                    }
                    
                    // Create sale item
                    SaleItem saleItem = new SaleItem(
                        0, // saleId will be set after sale creation
                        productId,
                        product.getName(),
                        product.getProductCode(),
                        product.getCategory(),
                        product.getPrice(),
                        quantity
                    );
                    
                    saleItems.add(saleItem);
                    
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("Invalid product ID or quantity format"));
                    return;
                }
            }
            
            // Create sale
            Sale sale = new Sale(customerId, customerName, currentUser.getId(), currentUser.getUsername());
            sale.setSaleItems(saleItems);
            
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                sale.setPaymentMethod(paymentMethod.toUpperCase());
            }
            
            if (discountStr != null && !discountStr.trim().isEmpty()) {
                try {
                    sale.setDiscount(new BigDecimal(discountStr));
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("Invalid discount format"));
                    return;
                }
            }
            
            if (notes != null && !notes.trim().isEmpty()) {
                sale.setNotes(notes);
            }
            
            // Set sale as completed by default
            sale.setSaleStatus("COMPLETED");
            
            int newSaleId = salesDAO.createSale(sale);
            
            if (newSaleId > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\": \"Sale completed successfully\", " +
                         "\"saleId\": " + newSaleId + ", " +
                         "\"saleNumber\": \"" + sale.getSaleNumber() + "\", " +
                         "\"totalAmount\": " + sale.getTotalAmount() + ", " +
                         "\"finalAmount\": " + sale.getFinalAmount() + ", " +
                         "\"itemsCount\": " + sale.getTotalItemsCount() + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.createErrorJson("Failed to create sale"));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Sale creation failed: " + e.getMessage()));
        }
    }

    // PUT /api/sales/{id}/status - Update sale status
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            
            if (pathParts.length == 3 && "status".equals(pathParts[2])) {
                int saleId = Integer.parseInt(pathParts[1]);
                
                Map<String, String> updateData = JsonUtil.parseSimpleJson(request.getReader());
                String newStatus = updateData.get("status");
                
                if (newStatus != null) {
                    if (salesDAO.updateSaleStatus(saleId, newStatus.toUpperCase())) {
                        out.print(JsonUtil.createSuccessJson("Sale status updated successfully"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(JsonUtil.createErrorJson("Sale not found"));
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("status is required"));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // Handle OPTIONS preflight requests
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cookie");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // JSON formatting methods
    private String formatSalesListJson(List<Sale> sales) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < sales.size(); i++) {
            if (i > 0) json.append(",");
            json.append(saleToJson(sales.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String saleToJson(Sale sale) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"saleId\": ").append(sale.getSaleId()).append(",");
        json.append("\"saleNumber\": \"").append(sale.getSaleNumber()).append("\",");
        json.append("\"customerId\": \"").append(sale.getCustomerId() != null ? sale.getCustomerId() : "").append("\",");
        json.append("\"customerName\": \"").append(sale.getCustomerName() != null ? sale.getCustomerName() : "").append("\",");
        json.append("\"totalAmount\": ").append(sale.getTotalAmount()).append(",");
        json.append("\"discount\": ").append(sale.getDiscount()).append(",");
        json.append("\"finalAmount\": ").append(sale.getFinalAmount()).append(",");
        json.append("\"paymentMethod\": \"").append(sale.getPaymentMethod()).append("\",");
        json.append("\"saleStatus\": \"").append(sale.getSaleStatus()).append("\",");
        json.append("\"soldBy\": ").append(sale.getSoldBy()).append(",");
        json.append("\"soldByName\": \"").append(sale.getSoldByName()).append("\",");
        json.append("\"itemsCount\": ").append(sale.getTotalItemsCount()).append(",");
        json.append("\"saleDate\": \"").append(sale.getSaleDate()).append("\",");
        json.append("\"notes\": \"").append(sale.getNotes() != null ? sale.getNotes() : "").append("\"");
        
        if (sale.getSaleItems() != null && !sale.getSaleItems().isEmpty()) {
            json.append(", \"items\": [");
            for (int i = 0; i < sale.getSaleItems().size(); i++) {
                if (i > 0) json.append(",");
                SaleItem item = sale.getSaleItems().get(i);
                json.append("{");
                json.append("\"productId\": ").append(item.getProductId()).append(",");
                json.append("\"productName\": \"").append(item.getProductName()).append("\",");
                json.append("\"productCode\": \"").append(item.getProductCode()).append("\",");
                json.append("\"productCategory\": \"").append(item.getProductCategory()).append("\",");
                json.append("\"unitPrice\": ").append(item.getUnitPrice()).append(",");
                json.append("\"quantity\": ").append(item.getQuantity()).append(",");
                json.append("\"subTotal\": ").append(item.getSubTotal());
                json.append("}");
            }
            json.append("]");
        }
        
        json.append("}");
        return json.toString();
    }

    public void destroy() {}
}