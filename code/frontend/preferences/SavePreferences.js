(() => {
    const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
    const API_BASE = isLocal && window.location.port !== "8080"
        ? `http://${window.location.hostname}:8080` : "";

    const formContainer = document.getElementById("preference-form-container");
    const submitInput = document.getElementById("submit");
    const error = document.getElementById("error");
    
    let schema = {}, dorms = {}, rules = [];

    async function initForm() {
        try {
            const configs = await window.PreferenceUtils.fetchConfigs(API_BASE);
            schema = configs.schema;
            dorms = configs.dorms;
            rules = configs.rules;
            
            window.PreferenceUtils.buildForm(formContainer, schema);
            submitInput.style.display = "block";
        } catch (err) {
            formContainer.innerHTML = `<p style='color:red;'>Error: ${err.message}</p>`;
        }
    }

    submitInput.addEventListener("click", async function(e) {
        e.preventDefault();
        error.innerText = "";

        const formData = {};
        for (const key of Object.keys(schema)) {
            formData[key] = document.getElementById(key).value;
        }

        // Call our shared validation function
        const validationError = window.PreferenceUtils.validate(formData, schema, dorms, rules);
        if (validationError) {
            error.innerText = validationError;
            return false;
        }

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