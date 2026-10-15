const submitInput = document.getElementById("submit");
// Currently unused and buggy. Uncomment and fix later.
// const emailInput = document.getElementById("email");
// const passwordInput = document.getElementById("password");
const error = document.getElementById("error");

submitInput.addEventListener("click", function() {
    
  //future: implement processing of information for js and backend

    window.location.href = "https://rowan118.github.io/RoomMate/code/frontend/submission.html";
    //redirection here means we don't need this to run anymore
    return false;

});
