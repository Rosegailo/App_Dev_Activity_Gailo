package com.example.appdev_debugging.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdev_debugging.R;
import com.example.appdev_debugging.ServiceLocator;
import com.example.appdev_debugging.auth.AuthManager;
import com.example.appdev_debugging.auth.AuthResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity{
    /** Key used to return the newly registered email to LoginActivity. */
    public static final String EXTRA_EMAIL = "com.example.simplelogin.EXTRA_EMAIL";

    private AuthManager authManager;

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = ServiceLocator.getAuthManager();

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError = findViewById(R.id.tvError);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());

        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });

        // Going back to login = just close this screen.
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    // TODO(DEBUG #3): Valid Philippine school / government emails are rejected.
    //  Steps to reproduce:
    //    1. Register with email "juan@school.ph"  ->  "Enter a valid email address".
    //    2. "juan@school.com" is accepted. "juan@up.edu.ph" is also rejected.
    //  Your tasks:
    //    a) Find where an email is decided to be "valid" (follow authManager.register(...) below).
    //       Answer: Decided in Validator.java line 9 inside validateEmail() using EMAIL_PATTERN regex.
    //    b) Run the existing unit tests: right-click app/src/test/java -> Run 'Tests in ...'.
    //       Read the failing test's name and message. Which test fails, and what does it tell you?
    //       Answer: Running unit tests on the regex fails on 2-letter TLDs (like juan@school.ph or juan@up.edu.ph), giving the message "Enter a valid email address".
    //    c) Test your guess quickly in an online regex tester or a tiny JUnit test before fixing.
    //       Answer: Tested regex ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{3,}$ against "juan@school.ph"; failed because .ph is only 2 letters long.
    //    d) Fix it, re-run the tests until they pass, and explain what part of the rule was wrong.
    //       Answer: Fixed in Validator.java line 9 by changing {3,} to {2,}. Country-code Top Level Domains (ccTLDs) like .ph, .uk, and .au are 2 letters long. Relaxing {3,} to {2,} allows valid 2-letter extensions while continuing to reject invalid single-letter extensions.
    //
    // TODO(DEBUG #4): Weak passwords are being accepted.
    //  The helper text promises "At least 8 characters, with letters and numbers".
    //  Steps to reproduce:
    //    1. Register with password "abcdefgh" (no numbers) -> account is created. It should be rejected.
    //    2. Try "12345678" (no letters) -> also accepted.
    //    3. Try "abcd1234" -> accepted (correct).
    //  Your tasks:
    //    a) Locate the password strength rule and read the boolean logic carefully.
    //       Answer: Located in Validator.java line 34: if (!hasLetter && !hasDigit) return "Password must contain letters and numbers";
    //    b) Make a truth table for hasLetter / hasDigit (4 rows). For each row, write what the
    //       rule SHOULD return and what the code ACTUALLY returns.
    //       Answer:
    //       hasLetter=F, hasDigit=F -> Should: Error, Actual: Error
    //       hasLetter=F, hasDigit=T ("12345678") -> Should: Error, Actual: Accepted (BUG)
    //       hasLetter=T, hasDigit=F ("abcdefgh") -> Should: Error, Actual: Accepted (BUG)
    //       hasLetter=T, hasDigit=T ("abcd1234") -> Should: Accepted, Actual: Accepted
    //    c) Fix the condition. Confirm with the unit tests (one of them should be failing right now).
    //       Answer: Fixed in Validator.java line 34 by changing condition to if (!hasLetter || !hasDigit) and confirmed with ValidatorTest.java.
    private void attemptRegister() {
        clearErrors();

        AuthResult result = authManager.register(
                textOf(etFullName),
                textOf(etEmail),
                textOf(etPassword),
                textOf(etConfirmPassword));

        if (result.isSuccess()) {
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            Intent data = new Intent();
            data.putExtra(EXTRA_EMAIL, result.getUser().getEmail());
            setResult(RESULT_OK, data);
            finish();
            return;
        }

        switch (result.getField()) {
            case FULL_NAME:
                showFieldError(tilFullName, etFullName, result.getErrorMessage());
                break;
            case EMAIL:
                showFieldError(tilEmail, etEmail, result.getErrorMessage());
                break;
            case PASSWORD:
                showFieldError(tilPassword, etPassword, result.getErrorMessage());
                break;
            case CONFIRM_PASSWORD:
                showFieldError(tilConfirmPassword, etConfirmPassword, result.getErrorMessage());
                break;
            default:
                tvError.setText(result.getErrorMessage());
                tvError.setVisibility(TextView.VISIBLE);
                break;
        }
    }

    private void showFieldError(TextInputLayout layout, TextInputEditText field, String message) {
        layout.setError(message);
        field.requestFocus();
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tvError.setVisibility(TextView.GONE);
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}
