//some of these are not arbitrary names due to the type of data being used. PLEASE do not refactor them
const submitInput = document.getElementById("submit");
const accountType = document.getElementById("accountType");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");
const API_BASE = "http://localhost:8080";

submitInput.addEventListener("click", async function () {

    //takes in the input from the html page
    error.innerText = "";

    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const index = accountType.selectedIndex;
    const account = accountType.options[index].value;

    if (!email.includes("@wsu.edu")) {
        error.innerText = "Not a valid WSU email";
        return false;
    }

    //TODO: email verification for RA and student accounts
    //RA will be a list of valid emails for making RA accounts (since the school likely tracks that anyway)
    //students will receive an email with an OTP (probably a psuedorandom number)
    //email will also be used when we send the results

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

    if (account.includes("RA")) {
        //this is just an example of how this check might work for RAs
        const testEmail = "bob.smith@wsu.edu";
        if (!(email === testEmail)) {
            error.innerText = "Cannot create an RA account with this email"
        }
    }

    //BACKEND: attempt to fetch email list of RA accounts (just create a couple random emails for demo)
    //if possible, return the specific message that its not a valid email for RAs

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

        window.location.href = "homepage.html";
    } catch (err) {
        error.innerText = "Could not reach the server. Is the backend running?";
    }

    //redirection here means we don't need these to run anymore
    return false;
});