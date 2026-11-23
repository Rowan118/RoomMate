Dependencies: Maven, Java v21, Python v3 (optional but recommended)

# How to run
Make sure your current working directory is `backend`.

## With Python (recommended)
`python run-backend.py`

### Setup
The Python file is responsible for setting the environment variables needed for pointing to the configuration files and databases. They point to directories that the database and configuration files are expected to live. You may edit them to fit your system.

## Manually (not recommended)
To set up, run
`mvn clean package`

To execute the backend, run
`java -jar target/roommate-backend-1.0-SNAPSHOT.jar`

# Caveats
If you're using Windows and your Git repo is cloned inside of a directory synced by Onedrive, you may get permissions errors when `mvn clean` is ran. It's recommended to instead clone this repository in a directory not synced with Onedrive.
