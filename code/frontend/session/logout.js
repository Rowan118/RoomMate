(() => {
const logoutButton = document.getElementById("logout");
const logoutError = document.getElementById("logoutError");
const API_BASE = window.location.hostname === "127.0.0.1"
    ? "http://127.0.0.1:8080"
    : "http://localhost:8080";

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
