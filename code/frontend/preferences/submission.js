const preferences = JSON.parse(sessionStorage.getItem("submittedPreferences") || "null");
const preferencesList = document.getElementById("submittedPreferences");

if (preferences) {
    const labels = {
        firstName: "First name",
        lastName: "Last name",
        major: "Major",
        dormPref: "Preferred residence hall",
        roomType: "Room preference",
        age: "Age range",
        gender: "Gender",
        sameGenderPref: "Same-gender roommate preference",
        wakeTime: "Wake-up time",
        sleepTime: "Bedtime",
        alcoholPref: "Alcohol preference"
    };

    for (const [key, label] of Object.entries(labels)) {
        const term = document.createElement("dt");
        const value = document.createElement("dd");
        term.textContent = label;
        value.textContent = preferences[key];
        preferencesList.append(term, value);
    }

    sessionStorage.removeItem("submittedPreferences");
} else {
    preferencesList.textContent = "No backend response is available in this tab.";
}
