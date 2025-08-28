package com.pahana.bookshop.factory;

import com.pahana.bookshop.model.User;
import com.pahana.bookshop.model.Customer;
import com.pahana.bookshop.model.Cashier;

public class UserFactory {
    
    public static User createUser(String userType, String username, String password, String email, String fullName) {
        switch (userType.toUpperCase()) {
            case "CUSTOMER":
                return new Customer(username, password, email, fullName);
            case "CASHIER":
                return new Cashier(username, password, email, fullName);
            case "ADMIN":
            case "USER":
                User user = new User(username, password, userType.toUpperCase());
                user.setEmail(email);
                user.setFullName(fullName);
                return user;
            default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }
    
    public static boolean isValidUserType(String userType) {
        if (userType == null) return false;
        String type = userType.toUpperCase();
        return type.equals("ADMIN") || type.equals("USER") || 
               type.equals("CUSTOMER") || type.equals("CASHIER");
    }
    
    public static String[] getValidUserTypes() {
        return new String[]{"ADMIN", "USER", "CUSTOMER", "CASHIER"};
    }
}