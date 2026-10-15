const submitInput = document.getElementById("submit");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");
const API_BASE = "http://localhost:8080";

submitInput.addEventListener("click", async function () {

    //takes in the input from the html page
    error.innerText = "";

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email.includes("@wsu.edu")) {
        error.innerText = "Not a valid WSU email";
        return false;
    }

    //assume implementation of some form of validation here later on
    if (password.length < 10) {
        error.innerText = "Password must be at least 10 characters long";
        return false;
    }
    if (password == "Securepassword3") {
        error.innerText = "Using the example as your password is not allowed";
        return false;
    }

    //tests for password
    if (!/[a-z]/.test(password)) {
        error.innerText = "Password must contain at least one lowercase letter";
        return false;
    }
    if (!/[A-Z]/.test(password)) {
        error.innerText = "Password must contain at least one capital letter";
        return false;
    }
    if (!/[0-9]/.test(password)) {
        error.innerText = "Password must contain at least one number";
        return false;
    }

    try {
        const response = await fetch(`${API_BASE}/api/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });
        const data = await response.json();

        if (!response.ok) {
            error.innerText = data.message || "Could not create account";
            return false;
        }

        window.location.href = "https://rowan118.github.io/RoomMate/code/frontend/homepage.html";
    } catch (err) {
        error.innerText = "Could not reach the server. Is the backend running?";
    }

    //redirection here means we don't need these to run anymore
    return false;
});