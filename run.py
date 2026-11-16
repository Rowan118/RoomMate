import os
import subprocess
import sys
from pathlib import Path

# Pathlib handles Windows (\) and Linux (/) paths automatically
print("Initializing both frontend and backend")

ROOT_DIR = Path(__file__).resolve().parent
CONF_DIR = ROOT_DIR / "conf"
BACKEND_DIR = ROOT_DIR / "code" / "backend"
FRONTEND_DIR = ROOT_DIR / "code" / "frontend"

def main():
    # 1. Set up Environment Variables for the Java Backend
    # We copy the current environment so we don't lose PATH variables
    env = os.environ.copy()
    
    # Point the backend to the config directory (absolute path)
    env["CONF_DIR"] = str(CONF_DIR)
    
    # Point the backend to the SQLite databases
    env["LOGIN_DB"] = str(ROOT_DIR / "databases" / "logins.db")
    env["PREF_DB"] = str(ROOT_DIR / "databases" / "preferences.db")

    print("Building Java Backend...")
    # 2. Build the Java JAR
    # Use shell=True on Windows to find mvn.cmd, but False on Linux
    is_windows = sys.platform.startswith("win")
    subprocess.run(["mvn", "clean", "package"], cwd=BACKEND_DIR, env=env, shell=is_windows)

    print("Starting Backend...")
    # 3. Start the Java Backend
    jar_path = BACKEND_DIR / "target" / "roommate-backend-1.0-SNAPSHOT.jar"
    backend_process = subprocess.Popen(["java", "-jar", str(jar_path)], env=env)

    print(f"Starting Frontend on http://localhost:8000...")
    # 4. Start the Frontend Python Server
    try:
        subprocess.run([sys.executable, "-m", "http.server", "8000"], cwd=FRONTEND_DIR)
    except KeyboardInterrupt:
        print("\nShutting down...")
    finally:
        # Ensure the Java backend is killed when the frontend is stopped
        backend_process.terminate()
        backend_process.wait()

if __name__ == "__main__":
    main()
