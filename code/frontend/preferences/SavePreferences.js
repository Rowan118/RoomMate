(() => {
    const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
    const API_BASE = isLocal && window.location.port !== "8080"
        ? `http://${window.location.hostname}:8080`
        : "";

    const formContainer = document.getElementById("preference-form-container");
    const submitInput = document.getElementById("submit");
    const error = document.getElementById("error");
    
    let schema = {};
    let dorms = {};
    let rules = [];

    async function initForm() {
        try {
            const [schemaRes, dormsRes, rulesRes] = await Promise.all([
                fetch(`${API_BASE}/api/config`),
                fetch(`${API_BASE}/api/dorms`),
                fetch(`${API_BASE}/api/rules`)
            ]);
            
            if (!schemaRes.ok || !dormsRes.ok || !rulesRes.ok) {
                throw new Error("Failed to load configuration files");
            }

            schema = await schemaRes.json();
            dorms = await dormsRes.json();
            rules = await rulesRes.json();
            
            formContainer.innerHTML = ""; 

            // Dynamically build the form
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
            submitInput.style.display = "block";
        } catch (err) {
            formContainer.innerHTML = `<p style='color:red;'>Error: ${err.message}</p>`;
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

        // 1. DYNAMIC DORM CONSTRAINTS
        let selectedDormName = null;
        for (const val of Object.values(formData)) {
            if (dorms[val]) { selectedDormName = val; break; }
        }

        if (selectedDormName && dorms[selectedDormName].constraints) {
            const constraints = dorms[selectedDormName].constraints;
            for (const [prefKey, allowedValues] of Object.entries(constraints)) {
                if (!allowedValues.includes(formData[prefKey])) {
                    error.innerText = `${selectedDormName} requires your ${prefKey} to be one of: ${allowedValues.join(", ")}`;
                    return false;
                }
            }
        }

        // 2. DYNAMIC CROSS-PREFERENCE RULES
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
                        error.innerText = rule.error;
                        return false;
                    }
                }
            }
        }

        // 3. Send to backend
        try {
            const response = await fetch(`${API_BASE}/api/addPreferences`, {
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