package com.roommate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

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

    private final int HASH_ITERATIONS = 3;
    private final int MEMORY_SIZE_KIB = 65536;
    private final int HASHING_THREADS = 2;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /** Returns the value to store in the database for a new password. */
    /**
     * Create a hash from a raw password.
     * @param rawPassword The password to create a hash from.
     * @return The hash to be used for authentication.
     */
    public String hash(String rawPassword) {
        // Passing in a String right away is deprecated. Here, it's converted into a char array.
        char[] rawPasswordArray = rawPassword.toCharArray();
        String hash = argon2.hash(HASH_ITERATIONS,
            MEMORY_SIZE_KIB,
            HASHING_THREADS,
            rawPasswordArray);

        return hash;
    }

    /** Checks a login attempt against the stored value. */
    /**
     * Verify if a password hash matches with a stored hash.
     * @param rawPassword The challenge raw password which (for example could come from) user input from the login screen.
     * @param storedHash The hash that is already stored within the database.
     * @return True if they match.
     */
    public boolean verify(String rawPassword, String storedHash) {
        boolean result = argon2.verify(storedHash, rawPassword.toCharArray());
        return result;
    }
}
