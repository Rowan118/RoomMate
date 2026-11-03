package com.roommate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PreferencesDatabase {
    
    private final Database db;

    public PreferencesDatabase(Database db){
        this.db = db;
        init();
    }

    private void init(){
        String[] preferences = {
            """
            CREATE TABLE IF NOT EXISTS placement_profile (
                user_id    INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
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
                user_id        INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
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
    }
}
