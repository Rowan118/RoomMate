(() => {
const submitInput = document.getElementById("submit");
const firstNameInput = document.getElementById("firstName");
const lastNameInput = document.getElementById("lastName");
const majorInput = document.getElementById("major");
const dormPrefInput = document.getElementById("dormPref");
const roomTypeInput = document.getElementById("roomType");
const ageInput = document.getElementById("age");
const genderInput = document.getElementById("gender");
const sameGenderPrefInput = document.getElementById("sameGenderPref");
const wakeTimeInput = document.getElementById("wakeTime");
const sleepTimeInput = document.getElementById("sleepTime");
const alcoholPrefInput = document.getElementById("alcoholPref");
const error = document.getElementById("error");
const API_BASE = window.location.hostname === "127.0.0.1" 
    ? "http://127.0.0.1:8080" 
    : "http://localhost:8080";

submitInput.addEventListener("click", async function() {

  error.innerText = "";

  const firstName = firstNameInput.value;
  const lastName = lastNameInput.value;
  const major = majorInput.value;
  const dormPref = dormPrefInput.value;
  const roomType = roomTypeInput.value;
  const age = ageInput.value;
  const gender = genderInput.value;
  const sameGenderPref = sameGenderPrefInput.value;
  const wakeTime = wakeTimeInput.value;
  const sleepTime = sleepTimeInput.value;
  const alcoholPref = alcoholPrefInput.value;

  //basic checks to ensure information isn't contradicting
  //helps simplify the matching process in the backend

  // -- Reference for dorm rules: --
  // Regents = female only
  // Stimson = male only
  // Rogers = double only
  // McEachern = single only, must be 19 or older
  // McCrosky = gender inclusive only
  // Streit Perham/Orton = co-ed (no special rules)

  if (dormPref === "Regents" && gender === "male") {
    error.innerText = "Regents is a residence hall for females";
    return false;
  }

  if (dormPref === "Stimson" && gender === "female") {
    error.innerText = "Stimson is a residence hall for males";
    return false;
  }

  if (dormPref === "Rogers" && roomType === "single") {
    error.innerText = "Rogers hall only has double rooms";
    return false;
  }

  if (dormPref === "McEachern" && roomType === "double") {
    error.innerText = "McEachern hall only has single rooms";
    return false;
  }

  if (dormPref === "McEachern" && age === "under 19") {
    error.innerText = "Must be 19 or older to live in McEachern hall";
    return false;
  }

  if (dormPref === "McCrosky" && sameGenderPref === "yes") {
    error.innerText = "Pick a different hall for rooming with the same gender";
    return false;
  }

  if (age === "under 21" && alcoholPref !== "under21") {
      error.innerText = "Alcohol preference must be set as 'under 21'";
      return false;
  }

  if (age === "21 or more" && alcoholPref === "under21") {
      error.innerText = "Please select 'No' or 'Yes' instead";
      return false;
  }

  //moves information to backend once processed

  try {
        const response = await fetch(`${API_BASE}/api/addPreferences`, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ firstName, lastName, major, dormPref, roomType, age, gender, 
                sameGenderPref, wakeTime, sleepTime, alcoholPref
             })
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
        error.innerText = "Something went wrong. Try again later";
    }

    //redirection here means we don't need this to run anymore
    return false;

});
})();
