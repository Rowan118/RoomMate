(() => {
    const API_BASE = window.location.hostname === "127.0.0.1" 
        ? "http://127.0.0.1:8080" 
        : "http://localhost:8080";

    const formContainer = document.getElementById("preference-form-container");
    const submitInput = document.getElementById("submit");
    const error = document.getElementById("error");
    
    //Fetches the saved preferences information from the backend and displays it
    //The user preferences are added to the dropdown as the default option
    //Returns an error if either the preference options or the user data can't be found

    let schema = {};
    let dorms = {};
    let preferences = {};

    async function initForm() {
        try {
            // Fetch the json data for the preference options and the user's saved preferences
            const [schemaRes, dormsRes, preferencesRes] = await Promise.all([
                fetch(`${API_BASE}/api/config`),
                fetch(`${API_BASE}/api/dorms`),
                fetch(`${API_BASE}/api/getPreferences`, { credentials: "include" })
            ]);
            
            if (!schemaRes.ok) {
                const errorCheck = await schemaRes.json();
                error.innerText = errorCheck.message || "Failed to load preferences information";
                return;
            }

            if (!dormsRes.ok) {
                const errorCheck = await dormsRes.json();
                error.innerText = errorCheck.message || "Failed to load dorm information"
                return;
            }

            if (!preferencesRes.ok) {
                const errorCheck = await preferencesRes.json();
                window.location.href = "add_preferences.html";
                return;
            }
            
            //Loads the information into the option dropdowns
            //Dynamic, so options can be changed later on
            schema = await schemaRes.json();
            dorms = await dormsRes.json();
            preferences = await preferencesRes.json();
            formContainer.innerHTML = ""; 

            for (const [key, config] of Object.entries(schema)) {
                const label = document.createElement("label");
                label.htmlFor = key;
                label.innerText = config.label + ":";
                
                let input;
                if (config.type === "select") {
                    input = document.createElement("select");
                    const defaultOption = document.createElement("option");
                    defaultOption.value = "none";
                    defaultOption.innerText = "";
                    input.appendChild(defaultOption);

                    config.options.forEach(opt => {
                        const option = document.createElement("option");
                        option.value = opt;
                        option.innerText = opt;
                        input.appendChild(option);
                    });
                } else if (config.type === "boolean") {
                    input = document.createElement("select");
                    input.innerHTML = `
                        <option value="none"></option>
                        <option value="true">Yes</option>
                        <option value="false">No</option>
                    `;
                } else {
                    input = document.createElement("input");
                    input.type = "text";
                }

                input.id = key;
                input.name = key;
                if (config.required) input.required = true;
                if (preferences.preferences[key] != null) {
                    input.value = String(preferences.preferences[key]);
                }
                else {
                    error.innerText = "Some preferences could not be found";
                    return;
                }

                formContainer.appendChild(label);
                formContainer.appendChild(document.createElement("br"));
                formContainer.appendChild(input);
                formContainer.appendChild(document.createElement("br"));
                formContainer.appendChild(document.createElement("br"));
            }

            submitInput.style.display = "block";
        } catch (err) {
            error.innerText = "Error loading configuration from backend.";
            console.error(err);
        }
    }

    submitInput.addEventListener("click", async function(e) {
        e.preventDefault();
        error.innerText = "";

        const formData = {};
        for (const key of Object.keys(schema)) {
            const element = document.getElementById(key);
            let value = element.value;

            if (value === "none" || value === "") {
                error.innerText = `Please fill out all required fields.`;
                return false;
            }
            formData[key] = value;
        }

        // --- DYNAMIC BUSINESS LOGIC ---
        // Find the selected dorm, gender, and room type by checking values against dorms.json
        let selectedDormName = null;
        let selectedGender = null;
        let selectedRoomType = null;

        for (const key in formData) {
            const val = formData[key];
            if (dorms[val]) selectedDormName = val;
            
            if (schema[key] && schema[key].options) {
                if (schema[key].options.includes("Man") || schema[key].options.includes("Woman")) selectedGender = val;
                if (schema[key].options.includes("Single") || schema[key].options.includes("Double")) selectedRoomType = val;
            }
        }

        if (selectedDormName && selectedGender) {
            const dormData = dorms[selectedDormName];
            if (dormData.allowed_genders && !dormData.allowed_genders.includes(selectedGender)) {
                error.innerText = `${selectedDormName} does not allow ${selectedGender}`;
                return false;
            }
            if (selectedRoomType === "Single" && !dormData.has_singles_rooms) {
                error.innerText = `${selectedDormName} does not have single rooms`;
                return false;
            }
            if (selectedRoomType === "Double" && !dormData.has_doubles_rooms) {
                error.innerText = `${selectedDormName} does not have double rooms`;
                return false;
            }
        }

        // Dynamic Age/Alcohol check
        let ageVal = null;
        let alcoholVal = null;
        for (const key in schema) {
            if (schema[key].options && schema[key].options.includes("<=18")) ageVal = formData[key];
            if (schema[key].type === "boolean" && schema[key].label.toLowerCase().includes("alcohol")) alcoholVal = formData[key];
        }
        if ((ageVal === "<=18" || ageVal === "19-20") && alcoholVal === "true") {
            error.innerText = "Alcohol preference must be set to No if under 21.";
            return false;
        }

        try {
            const response = await fetch(`${API_BASE}/api/modifyPreferences`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });
            const data = await response.json();

            if (!response.ok) {
                error.innerText = data.message || "Could not save preferences at this time";
                return false;
            }

            sessionStorage.setItem("submittedPreferences", JSON.stringify(data.preferences));
            window.location.href = "submission.html";
        } 
        catch (err) {
            error.innerText = "Something went wrong. Try again later.";
        }
    });

    initForm();
})();
