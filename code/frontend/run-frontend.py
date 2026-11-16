import os
import subprocess
import sys
from pathlib import Path
import inspect

### UPDATE FRONTEND_DIR IF THE WEBPAGE CONTENTS ARE NOT IN THE SAME WORKING DIRECTORY ###
FRONTEND_DIR = os.getcwd()
CONF_DIR = Path(FRONTEND_DIR).parent.parent / "conf"
PORT = "8000"
URL = "http://localhost:" + PORT

print("Current working directory:", FRONTEND_DIR)
print("Configuration directory:", CONF_DIR)

env = os.environ.copy()
env["CONF_DIR"] = str(CONF_DIR)

# Start frontend

print("Starting frontend on", URL)
try:
    subprocess.run([sys.executable, "-m", "http.server", PORT], cwd=FRONTEND_DIR)
except KeyboardInterrupt:
    print("\nShutting down...")
