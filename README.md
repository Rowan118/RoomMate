# RoomMate
RoomMate is a system designed to assist students in finding compatible roommates within university dormitories.

## Setup
Refer to the README.md files in frontend and backend directories to learn how to set up and run this service.
There exist configuration files in conf which determine your university's dorm setup and the student preferences that you may want to prompt and record as well as which accounts belong to an RA. See README.md in the conf directory to know more.

## Running
You can run `python run.py` to run both the frontend and backend at once (useful for testing).
You can run the backend and frontend separately via `python run-frontend.py` or `python run-backend.py` assuming your current working directory is already in those directories.
These scripts automatically pass in environment variables that determine the path of the configuration files and databases. You may edit them to point to a different directory that contains your configuration files and databases.

## FAQ
> I am getting permission denied errors from `mvn package` on Windows.
Wherever you have placed the Git repo files may live in a Onedrive directory. This causes issues with Maven.
It's recommended to instead clone the repo into a directory that doesn't belong in Onedrive.
