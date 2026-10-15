const submitInput = document.getElementById("submit");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");

submitInput.addEventListener("click", function() {
    //prevents page from reloading and not displaying errors

    //takes in the input from the html page
    error.innerText = "";

    const email = emailInput.value;
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

    //for backend -- take email and password input, return something indicating whether passwords match

    //to do for me -- implement url redirection for backend return 

    window.location.href = "https://www.merriam-webster.com/dictionary/placeholder";
    //redirection here means we don't need this to run anymore
    return false;

});
