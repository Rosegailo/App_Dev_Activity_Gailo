package com.example.appdev_debugging;

import com.example.appdev_debugging.auth.AuthManager;
import com.example.appdev_debugging.auth.AuthResult;
import com.example.appdev_debugging.data.InMemoryUserRepository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuthManagerTest {

    private AuthManager authManager;

    @Before
    public void setUp() {
        authManager = new AuthManager(new InMemoryUserRepository());
    }

    @Test
    public void testDemoAccountLogin() {
        // Register demo user as done in ServiceLocator
        authManager.register(ServiceLocator.DEMO_NAME, ServiceLocator.DEMO_EMAIL, ServiceLocator.DEMO_PASSWORD, ServiceLocator.DEMO_PASSWORD);

        AuthResult result = authManager.login(ServiceLocator.DEMO_EMAIL, ServiceLocator.DEMO_PASSWORD);
        assertTrue("Demo account login should succeed", result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals(ServiceLocator.DEMO_NAME, result.getUser().getFullName());
    }

    @Test
    public void testCapitalizedEmailLoginAndRegistration() {
        String fullName = "Juan Dela Cruz";
        String emailWithCaps = "Juan.DelaCruz@Mail.com";
        String password = "Password123";

        // Register with uppercase email
        AuthResult regResult = authManager.register(fullName, emailWithCaps, password, password);
        assertTrue("Registration with capitalized email should succeed", regResult.isSuccess());

        // Login using exact same capitalized email
        AuthResult loginResultCaps = authManager.login(emailWithCaps, password);
        assertTrue("Login with capitalized email should succeed", loginResultCaps.isSuccess());

        // Login using lowercase version of email
        AuthResult loginResultLower = authManager.login("juan.delacruz@mail.com", password);
        assertTrue("Login with lowercase email should succeed", loginResultLower.isSuccess());

        // Re-registration with lowercased email should fail as duplicate
        AuthResult reRegResult = authManager.register(fullName, "juan.delacruz@mail.com", password, password);
        assertFalse("Re-registering same email with different casing should fail", reRegResult.isSuccess());
        assertEquals("An account with this email already exists", reRegResult.getErrorMessage());
    }
}
