import os
import subprocess
import sys
from pathlib import Path

### UPDATE BACKEND IF THE WEBPAGE CONTENTS ARE NOT IN THE SAME WORKING DIRECTORY ###
BACKEND_DIR = Path(os.getcwd())
CONF_DIR = Path(BACKEND_DIR).parent.parent / "conf"
DATABASE_DIR = BACKEND_DIR.parent.parent / "databases"

print("Current working directory:", str(BACKEND_DIR))
print("Configuration directory:", CONF_DIR)

env = os.environ.copy()
env["CONF_DIR"] = str(CONF_DIR)
env["LOGIN_DB"] = str(DATABASE_DIR / "logins.db")
env["PREF_DB"] = str(DATABASE_DIR / "preferences.db")

print("Building Java Backend...")
is_windows = sys.platform.startswith("win")
subprocess.run(["mvn", "clean", "package"], cwd=BACKEND_DIR, env=env, shell=is_windows, check=True)

try:
    print("Starting Backend...")
    jar_path = BACKEND_DIR / "target" / "roommate-backend-1.0-SNAPSHOT.jar"
    backend_process = subprocess.run(["java", "-jar", str(jar_path)], env=env)
except KeyboardInterrupt:
    print("\nShutting down...")
#finally:
    #backend_process.terminate()
    #backend_process.wait()
