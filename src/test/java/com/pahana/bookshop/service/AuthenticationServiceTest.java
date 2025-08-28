package com.pahana.bookshop.service;

import com.pahana.bookshop.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {
    
    private AuthenticationService authService;
    
    @BeforeEach
    public void setUp() {
        authService = new AuthenticationService();
    }
    
    @Test
    public void testIsValidRole() {
        assertTrue(authService.isValidRole("admin"));
        assertTrue(authService.isValidRole("manager"));
        assertTrue(authService.isValidRole("cashier"));
        assertTrue(authService.isValidRole("user"));
        assertFalse(authService.isValidRole("invalid"));
        assertFalse(authService.isValidRole(null));
    }
    
    @Test
    public void testAuthenticateWithNullValues() throws Exception {
        assertNull(authService.authenticate(null, "password"));
        assertNull(authService.authenticate("username", null));
        assertNull(authService.authenticate("", "password"));
        assertNull(authService.authenticate("username", ""));
    }
}