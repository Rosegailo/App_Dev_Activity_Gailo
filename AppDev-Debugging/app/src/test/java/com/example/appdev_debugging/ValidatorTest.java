package com.example.appdev_debugging;

import com.example.appdev_debugging.util.Validator;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ValidatorTest {

    @Test
    public void testPhilippineAndTwoLetterTLDEmails() {
        assertNull("juan@school.ph should be valid", Validator.validateEmail("juan@school.ph"));
        assertNull("juan@up.edu.ph should be valid", Validator.validateEmail("juan@up.edu.ph"));
        assertNull("user@domain.com should be valid", Validator.validateEmail("user@domain.com"));
        assertNull("gov@agency.gov.ph should be valid", Validator.validateEmail("gov@agency.gov.ph"));

        assertNotNull("Invalid email without domain extension should be rejected", Validator.validateEmail("user@domain"));
    }

    @Test
    public void testPasswordStrengthValidation() {
        // Must contain both letters and numbers, length >= 8
        assertNotNull("Password with only letters should be rejected", Validator.validateNewPassword("abcdefgh"));
        assertNotNull("Password with only numbers should be rejected", Validator.validateNewPassword("12345678"));
        assertNotNull("Short password should be rejected", Validator.validateNewPassword("abc1"));

        assertNull("Password with letters and numbers should be accepted", Validator.validateNewPassword("abcd1234"));
        assertNull("Password with letters, numbers, and special chars should be accepted", Validator.validateNewPassword("Password123!"));
    }
}
