package com.pahana.bookshop.controller;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.service.AuthenticationService;
import com.pahana.bookshop.model.User;
import com.pahana.bookshop.model.Customer;
import com.pahana.bookshop.model.Cashier;
import com.pahana.bookshop.dao.CustomerDAO;
import com.pahana.bookshop.dao.CashierDAO;
import com.pahana.bookshop.factory.UserFactory;
import com.pahana.bookshop.util.JsonUtil;
import java.util.Map;

@WebServlet(name = "authAPI", urlPatterns = {"/api/auth/*"})
public class AuthAPI extends HttpServlet {
    private AuthenticationService authService;
    private CustomerDAO customerDAO;
    private CashierDAO cashierDAO;

    public void init() {
        authService = new AuthenticationService();
        customerDAO = new CustomerDAO();
        cashierDAO = new CashierDAO();
    }

    // POST /api/auth/login - User login
    // POST /api/auth/register - Register new user
    // POST /api/auth/register/customer - Register customer (admin only)
    // POST /api/auth/register/cashier - Register cashier (admin only)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            System.out.println("AuthAPI received pathInfo: '" + pathInfo + "'");
            
            if ("/login".equals(pathInfo) || (pathInfo != null && pathInfo.endsWith("/login"))) {
                handleLogin(request, response, out);
            } else if ("/register".equals(pathInfo) || (pathInfo != null && pathInfo.endsWith("/register"))) {
                handleRegister(request, response, out);
            } else if ("/register/customer".equals(pathInfo)) {
                handleRegisterCustomer(request, response, out);
            } else if ("/register/cashier".equals(pathInfo)) {
                handleRegisterCashier(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Available endpoints: /login, /register, /register/customer, /register/cashier. Received: " + pathInfo));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        Map<String, String> loginData = JsonUtil.parseSimpleJson(request.getReader());
        
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Username and password are required"));
            return;
        }
        
        User user = authService.authenticate(username, password);
        
        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            out.print("{\"message\": \"Login successful\", \"user\": {\"id\": " + user.getId() + 
                     ", \"username\": \"" + user.getUsername() + "\", \"role\": \"" + user.getRole() + "\"}, " +
                     "\"sessionId\": \"" + session.getId() + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.createErrorJson("Invalid username or password"));
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        Map<String, String> registerData = JsonUtil.parseSimpleJson(request.getReader());
        
        String username = registerData.get("username");
        String password = registerData.get("password");
        String role = registerData.get("role");
        
        if (username == null || password == null || role == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Username, password, and role are required"));
            return;
        }
        
        if (!UserFactory.isValidUserType(role)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Invalid role. Valid roles are: " + String.join(", ", UserFactory.getValidUserTypes())));
            return;
        }
        
        if (authService.createUser(username, password, role)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.createSuccessJson("User registered successfully with username: " + username));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Failed to register user. Username may already exist."));
        }
    }

    private void handleRegisterCustomer(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        // Check if user is admin
        if (!isAdminLoggedIn(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.createErrorJson("Admin access required"));
            return;
        }
        
        Map<String, String> registerData = JsonUtil.parseSimpleJson(request.getReader());
        
        String username = registerData.get("username");
        String password = registerData.get("password");
        String email = registerData.get("email");
        String fullName = registerData.get("fullName");
        String customerAddress = registerData.get("customerAddress");
        String comment = registerData.get("comment");
        
        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Username and password are required"));
            return;
        }
        
        Customer customer = (Customer) UserFactory.createUser("CUSTOMER", username, password, email, fullName);
        if (customerAddress != null) customer.setCustomerAddress(customerAddress);
        if (comment != null) customer.setComment(comment);
        
        int customerId = customerDAO.create(customer);
        if (customerId > 0) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.createSuccessJson("Customer registered successfully. Customer ID: " + customer.getCustomerId()));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Failed to register customer. Username may already exist."));
        }
    }
    
    private void handleRegisterCashier(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        // Check if user is admin
        if (!isAdminLoggedIn(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.createErrorJson("Admin access required"));
            return;
        }
        
        Map<String, String> registerData = JsonUtil.parseSimpleJson(request.getReader());
        
        String username = registerData.get("username");
        String password = registerData.get("password");
        String email = registerData.get("email");
        String fullName = registerData.get("fullName");
        String salaryStr = registerData.get("salary");
        String record = registerData.get("record");
        
        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Username and password are required"));
            return;
        }
        
        Cashier cashier = (Cashier) UserFactory.createUser("CASHIER", username, password, email, fullName);
        if (salaryStr != null) {
            try {
                cashier.setSalary(new java.math.BigDecimal(salaryStr));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.createErrorJson("Invalid salary format"));
                return;
            }
        }
        if (record != null) cashier.setRecord(record);
        
        int cashierId = cashierDAO.create(cashier);
        if (cashierId > 0) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.createSuccessJson("Cashier registered successfully. Cashier ID: " + cashier.getCashierId()));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Failed to register cashier. Username may already exist."));
        }
    }
    
    private boolean isAdminLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            return user != null && "ADMIN".equals(user.getRole());
        }
        return false;
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

    public void destroy() {}
}