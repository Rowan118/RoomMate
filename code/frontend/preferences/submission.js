(async function() {
    const API_BASE = window.location.hostname === "127.0.0.1" 
        ? "http://127.0.0.1:8080" 
        : "http://localhost:8080";


    const preferences = JSON.parse(sessionStorage.getItem("submittedPreferences") || "null");
    const preferencesList = document.getElementById("submittedPreferences");

    if (preferences) {
        let schema = {};
        try {
            const response = await fetch(`${API_BASE}/api/config`);
            if (response.ok) schema = await response.json();
        } catch (e) {
            console.error("Could not load schema for labels", e);
        }

        for (const [key, value] of Object.entries(preferences)) {
            const term = document.createElement("dt");
            const val = document.createElement("dd");
            
            // Use the label from schema if available, otherwise use the raw key
            term.textContent = (schema[key] && schema[key].label) ? schema[key].label : key;
            
            // Format booleans nicely for the user
            if (value === "true") val.textContent = "Yes";
            else if (value === "false") val.textContent = "No";
            else val.textContent = value;
            
            preferencesList.append(term, val);
        }

        sessionStorage.removeItem("submittedPreferences");
    } else {
        preferencesList.textContent = "No backend response is available in this tab.";
    }
})();