window.PreferenceUtils = {
    
    // 1. Fetch all three configuration files at once
    fetchConfigs: async function(API_BASE) {
        const [schemaRes, dormsRes, rulesRes] = await Promise.all([
            fetch(`${API_BASE}/api/config`),
            fetch(`${API_BASE}/api/dorms`),
            fetch(`${API_BASE}/api/rules`)
        ]);
        
        if (!schemaRes.ok || !dormsRes.ok || !rulesRes.ok) {
            throw new Error("Failed to load configuration files from the server.");
        }
        
        return {
            schema: await schemaRes.json(),
            dorms: await dormsRes.json(),
            rules: await rulesRes.json()
        };
    },

    // 2. Dynamically build the form
    buildForm: function(container, schema, savedPreferences = null) {
        container.innerHTML = ""; 

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

            // If we are modifying preferences, populate the field with saved data
            if (savedPreferences && savedPreferences[key] != null) {
                input.value = String(savedPreferences[key]);
            }

            container.appendChild(label);
            container.appendChild(document.createElement("br"));
            container.appendChild(input);
            container.appendChild(document.createElement("br"));
            container.appendChild(document.createElement("br"));
        }
    },

    // 3. Validate the form data against Dorm Constraints and Cross-Preference Rules
    // Returns an error message string if invalid, or null if valid
    validate: function(formData, schema, dorms, rules) {
        // A. Check for empty required fields
        for (const key of Object.keys(schema)) {
            if (formData[key] === "none" || formData[key] === "") {
                return `Please fill out all required fields.`;
            }
        }

        // B. DYNAMIC DORM CONSTRAINTS
        let selectedDormName = null;
        for (const val of Object.values(formData)) {
            if (dorms[val]) { selectedDormName = val; break; }
        }

        if (selectedDormName && dorms[selectedDormName].constraints) {
            const constraints = dorms[selectedDormName].constraints;
            for (const [prefKey, allowedValues] of Object.entries(constraints)) {
                if (!allowedValues.includes(formData[prefKey])) {
                    return `${selectedDormName} only allows ${prefKey} preferences to be ${allowedValues.join(", ")}`;
                }
            }
        }

        // C. DYNAMIC CROSS-PREFERENCE RULES
        for (const rule of rules) {
            let conditionMet = true;
            for (const [key, allowedValues] of Object.entries(rule.if)) {
                if (!allowedValues.includes(formData[key])) {
                    conditionMet = false;
                    break;
                }
            }

            if (conditionMet) {
                for (const [key, requiredValues] of Object.entries(rule.then)) {
                    if (!requiredValues.includes(formData[key])) {
                        return rule.error;
                    }
                }
            }
        }
        
        return null; // No errors found!
    }
};