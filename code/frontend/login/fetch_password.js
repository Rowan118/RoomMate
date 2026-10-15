const submitInput = document.getElementById("submit");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");
const API_BASE = "http://localhost:8080";

submitInput.addEventListener("click", async function () {

    error.innerText = "";

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    //keeps basic checks, since there's no point in querying for an invalid password
    //doesn't name the actual error for security

    if (!email.includes("@wsu.edu")) {
        error.innerText = "Not a registered email";
        return false;
    }

    const hasLowercase = /[a-z]/.test(password);
    const hasUppercase = /[A-Z]/.test(password);
    const hasNumber = /[0-9]/.test(password);

    if (password.length < 10 || !hasLowercase || !hasUppercase || !hasNumber) {
        error.innerText = "Invalid password";
        return false;
    }

    try {
        const response = await fetch(`${API_BASE}/api/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });
        const data = await response.json();

        if (!response.ok) {
            error.innerText = "Invalid email or password";
            return false;
        }

        window.location.href = "https://rowan118.github.io/RoomMate/code/frontend/homepage.html";
    } catch (err) {
        error.innerText = "Could not reach the server. Is the backend running?";
    }

    return false;
});