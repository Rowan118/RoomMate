const API_BASE = "http://localhost:8080";
const sessionMessage = document.getElementById("sessionMessage");

async function loadUser() {
    try {
        const response = await fetch(`${API_BASE}/api/session`, {
            credentials: "include",
        });

        if (!response.ok) {
            window.location.href = "../login/login.html";
            return;
        }

        const data = await response.json();
        if (sessionMessage) {
            sessionMessage.textContent = "User:  " + data.email;
        }
    } 
    catch (err) {
        if (sessionMessage) {
            sessionMessage.textContent = "Something went wrong. Please log in again.";
        }
        window.location.href = "../login/login.html";
    }
}

loadUser();
