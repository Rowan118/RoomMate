const submitInput = document.getElementById("submit");
// Currently unused and buggy. Uncomment and fix later.
// const emailInput = document.getElementById("email");
// const passwordInput = document.getElementById("password");
const error = document.getElementById("error");
const API_BASE = "http://localhost:8080";

submitInput.addEventListener("click", async function() {
    
  //future: implement processing of information for js and backend

  try {
        const response = await fetch(`${API_BASE}/api/addPreferences`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
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
