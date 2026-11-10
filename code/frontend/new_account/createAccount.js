//some of these are not arbitrary names due to the type of data being used. PLEASE do not refactor them
const submitInput = document.getElementById("submit");
const accountInput = document.getElementById("accountType");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");
const API_BASE = window.location.hostname === "127.0.0.1" 
    ? "http://127.0.0.1:8080" 
    : "http://localhost:8080";

submitInput.addEventListener("click", async function () {

    //takes in the input from the html page
    error.innerText = "";

    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const index = accountInput.selectedIndex;
    const account = accountInput.options[index].value;

    if (account === "none") {
        error.innerText = "Account type must be selected";
        return false;
    }

    // --- BASIC EMAIL VALIDATION --- //

    //Ensures everyone enters a valid, current WSU email address.
    //In addition, RAs must have an email that is listed as a valid RA email

    if (!email.includes("@wsu.edu")) {
        error.innerText = "Not a valid WSU email";
        return false;
    }

    if (account.includes("RA")) {
        //access json file with all valid RA emails, and throw error is account is not in that list

        try {
            const response = await fetch(`${API_BASE}/api/ra_emails`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({email})
            });
            if (!response.ok) {
                error.innerText = "Failed complete check for RA email validity. Please try again later";
                return false;
            }

            const emailValid = await response.json();

            if (!emailValid) {
                error.innerText = "Cannot create an RA account with this email";
                return false;
            }
        }
        catch (error) {
            error.innerText = "Failed to fetch RA email list from backend";
            return false;
        }
    }    

    //TODO: email verification for RA and student accounts
    //students will receive an email with an OTP (probably a psuedorandom number)
    //email will also be used when we send the results

    // --- BASIC PASSWORD VALIDATION --- //

    //Ensures that any chosen password is reasonably secure
    //All password will be 10 or more chars, have at least one capital and lowercase letter, and contain a number

    //Once we finish most of the testing, I'll add a symbol requirement as well

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
            body: JSON.stringify({ account, email, password })
        });
        const data = await response.json();

        if (!response.ok) {
            error.innerText = data.message || "Could not create account";
            return false;
        }

        window.location.href = "../homepage/homepage.html";
    } 
    catch (err) {
        error.innerText = "Could not reach the server. Is the backend running?";
    }

    //redirection here means we don't need these to run anymore
    return false;
});