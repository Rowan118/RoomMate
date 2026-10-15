package com.roommate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Single place where password storage rules live.
 *
 * !!! PROOF OF CONCEPT ONLY !!!
 * Passwords are currently stored as plaintext. Before this handles any real
 * user data, replace hash() and verify() with Argon2id, bcrypt, or scrypt
 * (e.g. the org.mindrot:jbcrypt or de.mkammerer:argon2-jvm libraries).
 * Nothing outside this class needs to change when you do.
 */
public class PasswordService {

    /** Returns the value to store in the database for a new password. */
    public String hash(String rawPassword) {
        return rawPassword;
    }

    /** Checks a login attempt against the stored value. */
    public boolean verify(String rawPassword, String storedValue) {
        return MessageDigest.isEqual(
            rawPassword.getBytes(StandardCharsets.UTF_8),
            storedValue.getBytes(StandardCharsets.UTF_8)
        );
    }
}
