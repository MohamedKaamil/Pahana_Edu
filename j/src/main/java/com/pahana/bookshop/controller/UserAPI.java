package com.pahana.bookshop.controller;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.service.AuthenticationService;
import com.pahana.bookshop.dao.UserDAO;
import com.pahana.bookshop.model.User;
import com.pahana.bookshop.util.JsonUtil;
import java.util.List;
import java.util.Map;

@WebServlet(name = "userAPI", urlPatterns = {"/api/users", "/api/users/*"})
public class UserAPI extends HttpServlet {
    private AuthenticationService authService;
    private UserDAO userDAO;

    public void init() {
        authService = new AuthenticationService();
        userDAO = new UserDAO();
    }

    // GET /api/users - Get all users
    // GET /api/users/{id} - Get user by ID
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all users
                List<User> users = userDAO.findAll();
                out.print(formatUsersListJson(users));
            } else {
                // Get user by ID
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int userId = Integer.parseInt(pathParts[1]);
                    User user = userDAO.findById(userId);
                    if (user != null) {
                        out.print(JsonUtil.userToJson(user));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(JsonUtil.createErrorJson("User not found"));
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // POST /api/users - Create new user
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            Map<String, String> userData = JsonUtil.parseSimpleJson(request.getReader());
            
            String username = userData.get("username");
            String password = userData.get("password");
            String role = userData.get("role");
            
            if (authService.createUser(username, password, role)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.createSuccessJson("User created successfully"));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.createErrorJson("Failed to create user"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // PUT /api/users/{id} - Update user
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            
            if (pathParts.length == 2) {
                int userId = Integer.parseInt(pathParts[1]);
                
                Map<String, String> userData = JsonUtil.parseSimpleJson(request.getReader());
                
                User user = userDAO.findById(userId);
                if (user != null) {
                    user.setUsername(userData.get("username"));
                    user.setRole(userData.get("role"));
                    
                    if (userDAO.update(user)) {
                        out.print(JsonUtil.createSuccessJson("User updated successfully"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(JsonUtil.createErrorJson("Failed to update user"));
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.createErrorJson("User not found"));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // DELETE /api/users/{id} - Delete user
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            
            if (pathParts.length == 2) {
                int userId = Integer.parseInt(pathParts[1]);
                
                if (userDAO.delete(userId)) {
                    out.print(JsonUtil.createSuccessJson("User deleted successfully"));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.createErrorJson("User not found"));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    private String formatUsersListJson(List<User> users) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) json.append(",");
            json.append(JsonUtil.userToJson(users.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    public void destroy() {}
}