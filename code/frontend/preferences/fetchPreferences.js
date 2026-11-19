(() => {
    const API_BASE = window.location.hostname === "127.0.0.1" 
        ? "http://127.0.0.1:8080" 
        : "http://localhost:8080";

    const formContainer = document.getElementById("preference-form-container");
    const submitInput = document.getElementById("submit");
    const error = document.getElementById("error");
    
    let schema = {};
    let dorms = {};

    async function initForm() {
        try {
            // Fetch both configs
            const [schemaRes, dormsRes, preferencesRes] = await Promise.all([
                fetch(`${API_BASE}/api/config`),
                fetch(`${API_BASE}/api/dorms`),
                fetch(`${API_BASE}/api/getPreferences`, { credentials: "include" })
            ]);
            
            if (!schemaRes.ok || !dormsRes.ok) throw new Error("Failed to load config");
            if (!preferencesRes.ok) {
                const data = await preferencesRes.json();
                throw new Error(data.message || "Failed to load saved preferences");
            }
            
            schema = await schemaRes.json();
            dorms = await dormsRes.json();
            const preferencesData = await preferencesRes.json();
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

                formContainer.appendChild(label);
                formContainer.appendChild(document.createElement("br"));
                formContainer.appendChild(input);
                formContainer.appendChild(document.createElement("br"));
                formContainer.appendChild(document.createElement("br"));
            }

            for (const [key, value] of Object.entries(preferencesData.preferences)) {
                const input = document.getElementById(key);
                if (input && value != null) {
                    input.value = String(value);
                }
            }
            submitInput.style.display = "block";
        } catch (err) {
            formContainer.innerHTML = "<p style='color:red;'>Error loading configuration from backend.</p>";
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
