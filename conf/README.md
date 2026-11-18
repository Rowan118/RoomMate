# Configuration
The conf directory stores some important configuration files that are necessary for the backend and frontend to run.
The Git repo provides WSU Pullman examples.

# Purposes
## dorms.json
Determines what dorms are available at your university.

### Example
```json
"Regents": {
        "allowed_genders": ["Woman"],
        "has_doubles_rooms": true,
        "has_singles_rooms": true
    },
```

## preferences.json
Determines what questions students will be asked and how data is stored within the database.

**YOU MUST MANUALLY MIGRATE OR DELETE YOUR PREFERENCES DATABASE AFTER CHANGING OPTIONS**

The key is how the database and APIs will recognize the name of the student preference.
You must provide a label with the "label" option that will be the text that is presented to students on the website.
**Supported types**:
- text - A string of text that the user inputs with their keyboard
- select - A drop down box of predefined options (requires the "option" JSON entry)
- boolean - A true/false question.
If "select" is the chosen type, then an "options" option of an array is required to present all possible options student can select.
If the question is required to proceed, then set the "required" option to true, otherwise set to false.

```json
"first_name": {
        "label": "First name",
        "type": "text",
        "required": true
    },
```

## emailList.json 
Records pre-approved e-mail addresses that belong to residential advisors.

### Example
```json
        {
            "email": "sophia.lane@wsu.edu",
            "dorm": "Regents",
            "floor": "2"
        },
```
