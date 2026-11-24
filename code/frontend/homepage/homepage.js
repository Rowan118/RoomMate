(() => {

    const input = document.getElementById("button");
    const email = document.getElementById("sessionMessage");

    submitInput.addEventListener("click", async function(e) {
        e.preventDefault();
        error.innerText = "";

        try {
            const response = await fetch(`${API_BASE}/api/accountType`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(email)
            });
            const data = await response.json();

            if (!response.ok) {
                error.innerText = data.message || "Something went wrong when verifying account type";
                return false;
            }
        } 
        catch (err) {
            error.innerText = "Something went wrong";
        }
    });

})();