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

    public boolean hasProfile(String email) throws SQLException{
        try (Connection c = connect();
            PreparedStatement ps = c.prepareStatement("SELECT 1 FROM placement_profile WHERE email =?")){
                ps.setString(1, normalize(email));
                try (ResultSet rs = ps.executeQuery()){
                    return rs.next();
                }
            }
    }

    private static String normalize(String email){
        return email.trim().toLowerCase();
    }
}
