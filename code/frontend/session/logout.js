(() => {
const logoutButton = document.getElementById("logout");
const logoutError = document.getElementById("logoutError");
const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_BASE = isLocal && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080`
    : "";

logoutButton.addEventListener("click", async () => {
    logoutError.textContent = "";

    try {
        const response = await fetch(`${API_BASE}/api/logout`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            const data = await response.json();
            logoutError.textContent = data.message || "Could not log out.";
            return;
        }

        window.location.href = "../login/login.html";
    } catch (err) {
        logoutError.textContent = "Could not reach the server. Try again.";
    }
});
})();
