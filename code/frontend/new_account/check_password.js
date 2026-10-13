const submitBtn = document.getElementById("submit");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const error = document.getElementById("error");

submitBtn.addEventListener("click", function() {

    //takes in the input from the html page
    error.innerText = "";

    const email = emailInput.value;
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
    if(password == "Securepassword3") {
        error.innerText = "Using the example as your password is not allowed";
        return false;
    }

    //tests for password
    const has_lowercase = /[a-z]/.test(password);
    const has_uppercase = /[A-Z]/.test(password);
    const has_number = /[0-9]/.test(password);

    if (!has_lowercase) {
        error.innerText = "Password must contain at least one lowercase letter";
        return false;
    }
    if (!has_uppercase) {
        error.innerText = "Password must contain at least one capital letter";
        return false;
    }
    if (!has_number) {
        error.innerText = "Password must contain at least one number";
        return false;
    }

    //for backend -- add email and password to database 

    window.location.href = "https://www.merriam-webster.com/dictionary/placeholder";
    //redirection here means we don't need these to run anymore
    return false;

});
