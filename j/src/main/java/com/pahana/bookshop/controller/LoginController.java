package com.pahana.bookshop.controller;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.service.AuthenticationService;
import com.pahana.bookshop.model.User;

@WebServlet(name = "loginController", value = "/login")
public class LoginController extends HttpServlet {
    private AuthenticationService authService;

    public void init() {
        authService = new AuthenticationService();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><body>");
        out.println("<h2>Pahana Educational Bookshop - Login</h2>");
        out.println("<form method='post' action='/login'>");
        out.println("<p>Username: <input type='text' name='username' required></p>");
        out.println("<p>Password: <input type='password' name='password' required></p>");
        out.println("<p><input type='submit' value='Login'></p>");
        out.println("</form>");
        out.println("</body></html>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        try {
            User user = authService.authenticate(username, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                
                out.println("<html><body>");
                out.println("<h2>Login Successful!</h2>");
                out.println("<p>Welcome, " + user.getUsername() + " (" + user.getRole() + ")</p>");
                out.println("<p><a href='/dashboard'>Go to Dashboard</a></p>");
                out.println("</body></html>");
            } else {
                out.println("<html><body>");
                out.println("<h2>Login Failed</h2>");
                out.println("<p>Invalid username or password.</p>");
                out.println("<p><a href='/login'>Try Again</a></p>");
                out.println("</body></html>");
            }
        } catch (Exception e) {
            out.println("<html><body>");
            out.println("<h2>Error</h2>");
            out.println("<p>An error occurred during login: " + e.getMessage() + "</p>");
            out.println("<p><a href='/login'>Try Again</a></p>");
            out.println("</body></html>");
        }
    }

    public void destroy() {
    }
}