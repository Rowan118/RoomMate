package com.roommate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.JsonObject;

/**
 * PreferencesDatabase
 */
public class PreferencesDatabase extends Database {
    
    /**
     * Everything on the preferences form, already validated.
     *
     * @param firstName      The student's first name.
     * @param lastName       The student's last name.
     * @param major          The student's major.
     * @param dorm           The preferred residence hall (e.g. "Regents").
     * @param gender         The student's gender, as chosen on the form.
     * @param roomType       The preferred room type (e.g. "single", "double").
     * @param sameGenderPref True if the student wants a roommate of the same gender.
     * @param ageRange       The student's age bracket ("under 19", "under 21", or "21 or more").
     * @param wakeTime       The usual wake-up time, formatted HH:MM.
     * @param sleepTime      The usual bedtime, formatted HH:MM.
     * @param alcoholPref    The alcohol preference ("under21", "no", or "yes").
     */
    public record Preferences(String firstName, String lastName, String major, String dorm, String gender, String roomType, boolean sameGenderPref, String ageRange,
        String wakeTime, String sleepTime, String alcoholPref){
            public JsonObject toJson(){
                JsonObject o = new JsonObject();
                o.addProperty("ok", true);
                o.addProperty("firstname", firstName);
                o.addProperty("lastname", lastName);
                o.addProperty("major", major);
                o.addProperty("dorm", dorm);
                o.addProperty("gender", gender);
                o.addProperty("roomtype", roomType);
                o.addProperty("sameGenderPref", sameGenderPref ? "yes" : "no");
                o.addProperty("ageRange", ageRange);
                o.addProperty("wakeTime", wakeTime);
                o.addProperty("sleepTime", sleepTime);
                o.addProperty("alcoholPref", alcoholPref);
                return o;
            }
        }
    /**
     * Create a new student preferences SQLite database.
     * Contains the user's e-mail along with a plethora of user preferences for which dorm to be selected for.
     * @param filePath Path to the SQLite database file.
     */
    public PreferencesDatabase(String filePath) {
        super(filePath);
    }

    /**    (non-Javadoc)
     * 
     * @see com.roommate.Database#init()
     */
    @Override 
    void init() {
        String[] sql = {
            """
            CREATE TABLE IF NOT EXISTS placement_profile (
                email            TEXT PRIMARY KEY COLLATE NOCASE,
                first_name       TEXT NOT NULL,
                last_name        TEXT NOT NULL,
                major            TEXT NOT NULL,
                dorm             TEXT NOT NULL,
                gender           TEXT NOT NULL CHECK (gender IN ('male','female','nonbinary','other')),
                room_type        TEXT NOT NULL CHECK (room_type IN ('single','double','triple','suite')),
                same_gender_pref INTEGER NOT NULL CHECK (same_gender_pref IN (0,1)),
                age_range        TEXT NOT NULL CHECK (age_range IN ('under 19', 'under 21', '21 or more')),
                updated_at TEXT  NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS lifestyle_preferences (
                email          TEXT PRIMARY KEY COLLATE NOCASE,
                sleep_time     TEXT,
                wake_time      TEXT,
                drug_tolerance INTEGER NOT NULL DEFAULT 0 CHECK (drug_tolerance BETWEEN 0 AND 3),
                alcohol_pref   TEXT NOT NULL CHECK (alcohol_pref IN ('under 21', 'no', 'yes')),
                updated_at     TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_placement_match ON placement_profile(dorm, gender, room_type)",
            """
            CREATE TRIGGER IF NOT EXISTS lock_placement
            BEFORE UPDATE OF dorm, gender, room_type ON placement_profile
            WHEN OLD.dorm IS NOT NEW.dorm
                OR OLD.gender IS NOT new.gender
                OR OLD.room_type IS NOT new.room_type
            BEGIN
                SELECT RAISE(ABORT, 'Placement profile is locked');
            END
            """
        };

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            // Loop through each SQL statement in the array since it's an array of Strings.
            for (String statement : sql) {
                st.execute(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    /**
     * Check whether an e-mail address has already submitted the preferences form.
     * Only {@code placement_profile} is checked, because {@link #save} writes both tables together.
     *
     * @param email The user's e-mail address (any casing).
     * @return True if a profile exists for that e-mail.
     * @throws SQLException If the database query fails.
     */

    public boolean hasProfile(String email) throws SQLException{
        try (Connection c = connect();
            PreparedStatement ps = c.prepareStatement("SELECT 1 FROM placement_profile WHERE email =?")){
                ps.setString(1, normalize(email));
                try (ResultSet rs = ps.executeQuery()){
                    return rs.next();
                }
            }
    }

    /**
     * Save a user's preferences for the first time.
     * Both tables are written in one transaction, so a profile is never half-saved.
     *
     * @param email The user's e-mail address (any casing).
     * @param p     The validated preferences to store.
     * @throws SQLException If either insert fails (for example, a profile already exists).
     *                      The transaction is rolled back before the exception is rethrown.
     */
    public void save(String email, Preferences p) throws SQLException{
        String key = normalize(email);
        try (Connection c = connect()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement ps = c.prepareStatement("INSERT INTO placement_profile" + 
                "(email, first_name, last_name, major, dorm, gender, room_type, same_gender_pref, age_range)"
                 + " VALUES (?,?,?,?,?,?,?,?,?")){
                    ps.setString(1, key);
                    ps.setString(2, p.firstName());
                    ps.setString(3, p.lastName());
                    ps.setString(4, p.major());
                    ps.setString(5, p.dorm());
                    ps.setString(6, p.gender());
                    ps.setString(7, p.roomType());
                    ps.setInt(8, p.sameGenderPref() ? 1 : 0);
                    ps.setString(9, p.ageRange());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO lifestyle_preferences" + "(email, wake_time, sleep_time, alcohol_pref)"
                    + " VALUES (?,?,?,?)")){
                        ps.setString(1, key);
                        ps.setString(2, p.wakeTime());
                        ps.setString(3, p.sleepTime);
                        ps.setString(4, p.alcoholPref());
                        ps.executeUpdate();
                    }
                c.commit();
            } catch (SQLException e){
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Trim an e-mail address and make it lowercase, matching how {@code UserDao} stores e-mails.
     * The session may hold the address exactly as the user typed it.
     *
     * @param email The e-mail address to clean up.
     * @return The normalized e-mail address.
     */
    private static String normalize(String email){
        return email.trim().toLowerCase();
    }
}
