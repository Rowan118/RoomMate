const submitInput = document.getElementById("submit");
const firstNameInput = document.getElementById("firstName");
const lastNameInput = document.getElementById("lastName");
const majorInput = document.getElementById("major");
const dormPrefInput = document.getElementById("dormPref");
const roomTypeInput = document.getElementById("roomType");
const ageInput = document.getElementById("age");
const genderInput = document.getElementById("gender");
const sameGenderPrefInput = document.getElementById("sameGender");
const wakeTimeInput = document.getElementById("wakeTime");
const sleepTimeInput = document.getElementById("sleepTime");
const alcoholPrefInput = document.getElementById("alcohol");
const error = document.getElementById("error");
const API_BASE = "http://localhost:8080";

submitInput.addEventListener("click", async function() {
    
  //future: implement processing of information for js and backend

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

  try {
        const response = await fetch(`${API_BASE}/api/addPreferences`, {
            method: "POST",
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

        window.location.href = "submission.html";
    } catch (err) {
        error.innerText = "Something went wrong. Try again later";
    }

    //redirection here means we don't need this to run anymore
    return false;

});
