package com.pahana.bookshop.util;

import jakarta.servlet.http.HttpServletResponse;

public class CorsUtil {
    
    public static void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cookie");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}