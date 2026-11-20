const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_BASE = isLocal && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080`
    : "";

const sessionMessage = document.getElementById("sessionMessage");

async function loadUser() {
    try {
        const response = await fetch(`${API_BASE}/api/session`, {
            credentials: "include",
        });

        if (!response.ok) {
            // Only redirect if we know for sure the user is not logged in
            if (response.status === 401) {
                window.location.href = "../login/login.html";
            }
            return;
        }

        const data = await response.json();
        if (sessionMessage) {
            sessionMessage.textContent = data.email;
        }
    } catch (err) {
        // Ignore AbortError (happens when you click a link before the fetch finishes)
        if (err.name === 'AbortError') {
            return;
        }
        
        if (sessionMessage) {
            sessionMessage.textContent = "Could not reach server.";
        }
    }
}

loadUser();
