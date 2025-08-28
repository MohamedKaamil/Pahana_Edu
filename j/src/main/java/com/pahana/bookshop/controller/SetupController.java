package com.pahana.bookshop.controller;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.service.AuthenticationService;
import com.pahana.bookshop.dao.UserDAO;
import com.pahana.bookshop.util.JsonUtil;

@WebServlet(name = "setupController", urlPatterns = {"/setup/*"})
public class SetupController extends HttpServlet {
    private AuthenticationService authService;
    private UserDAO userDAO;

    public void init() {
        authService = new AuthenticationService();
        userDAO = new UserDAO();
    }

    // POST /setup/create-admin - Create initial admin account
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if ("/create-admin".equals(pathInfo)) {
                // Check if any admin already exists
                try {
                    if (userDAO.findAll().size() > 0) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(JsonUtil.createErrorJson("Setup already completed. Use /api/auth/register instead."));
                        return;
                    }
                } catch (Exception e) {
                    // If table doesn't exist or other error, continue with setup
                }
                
                // Create default admin account
                if (authService.createUser("admin", "admin123", "admin")) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(JsonUtil.createSuccessJson("Admin account created successfully. Username: admin, Password: admin123"));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(JsonUtil.createErrorJson("Failed to create admin account"));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Endpoint not found"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }
    
    // GET /setup/status - Check setup status
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if ("/status".equals(pathInfo)) {
                try {
                    int userCount = userDAO.findAll().size();
                    if (userCount > 0) {
                        out.print("{\"setupComplete\": true, \"userCount\": " + userCount + ", \"message\": \"System is ready\"}");
                    } else {
                        out.print("{\"setupComplete\": false, \"userCount\": 0, \"message\": \"No users found. Run POST /setup/create-admin\"}");
                    }
                } catch (Exception e) {
                    out.print("{\"setupComplete\": false, \"error\": \"Database error: " + e.getMessage() + "\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Endpoint not found"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    public void destroy() {}
}