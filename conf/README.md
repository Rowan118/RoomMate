# Configuration
The conf directory stores some important configuration files that are necessary for the backend and frontend to run.
The Git repo provides WSU Pullman examples.

# Purposes
## dorms.json
Determines what dorms are available at your university as well as constraints.
A constraint is a preference that is specifically allowed for whichever dorm the constraint lives in. For example, for a dorm that is strictly for women, you'd add "gender" as a constraint and then add "Woman" as one of the constraints in a JSON array. Whatever preference options are not specified are disallowed and students signing up would get an error telling them that their preferences are incompatible with their chosen dorm.

### Example
```json
{
        "Regents": {
                "constraints": {
                        "gender": ["Woman"]
                }
        },
        "Orton": {}
}
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

## rules.json
For any preferences that may conflict with each other or may vary depending on circumstances, they can be added in rules.json. For example, asking a student that's under the legal alcohol minimum drinking age if they would like to stay with a roommate who drinks alcohol can be restricted.
Make an object field with `{}` and specify `"if": {},` and `"then": {},` and `"error":` to warn of conflicting options.

### Example
```json
[
    {
        "if": {
            "age": ["<=18", "19-20"]
        },
        "then": {
            "alcohol_preference": ["false"]
        },
        "error": "You cannot prefer to be in a room with someone who drinks alcohol if you are under 21."
    }
]
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
