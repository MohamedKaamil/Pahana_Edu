package com.pahana.bookshop.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Allow requests from multiple React development servers
        String origin = httpRequest.getHeader("Origin");
        String[] allowedOrigins = {
            "http://localhost:3000",
            "http://localhost:3001", 
            "http://localhost:3002", 
            "http://localhost:3003"
        };
        
        // Check if the origin is in our allowed list
        if (origin != null) {
            for (String allowedOrigin : allowedOrigins) {
                if (origin.equals(allowedOrigin)) {
                    httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                    break;
                }
            }
        } else {
            // If no Origin header (like direct server requests), allow the first one
            httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigins[0]);
        }
        
        // Always set these headers for all requests
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cookie, X-CSRF-Token");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}